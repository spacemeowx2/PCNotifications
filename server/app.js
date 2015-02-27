var http = require("http");
var fs = require("fs");
var domain = require('domain');
var codeTable   = {}; //key: code       value: {devices[]}
var deviceTable = {}; //key: deviceid   value: {name,role,lasttime,online(false,response),msgs[]}
var CODECHAR = "0x12345678x9";
var CODELEN = 10;
//only for small server
function initTables() {
  var tables;
  var filename = 'db.json';
  var writeInterval = 10000;
  if (fs.existsSync(filename)) {
    tables = JSON.parse(fs.readFileSync(filename));
    codeTable=tables[0];
    deviceTable=tables[1];
  }
  function write() {
    fs.writeFileSync(filename, JSON.stringify([codeTable, deviceTable], function (k,v){if (k!='online') return v;}));
    setTimeout(write, writeInterval);
  }
  setTimeout(write, writeInterval);
}
initTables();
function newCode(table) {
  var i, tCode;
  while(!tCode || table.hasOwnProperty(tCode)) {
    tCode = '';
    for (i=0; i<CODELEN; i++) {
      tCode += CODECHAR[Math.floor(Math.random()*CODECHAR.length)];
    }
  }
  return tCode;
}
function needNewDID(args) {
  return args.did=='debug';
}
function handler(args) {
  if (!codeTable.hasOwnProperty(args.code)) {
    codeTable[args.code] = [];
  }
  if (!deviceTable.hasOwnProperty(args.did)) {
    deviceTable[args.did] = {name:'No Name',role:args.role,lasttime:0,online:false,msgs:[]};
  }
  if (codeTable[args.code].indexOf(args.did)==-1) {
    codeTable[args.code].push(args.did);
  }
  var ret = {'ok':true};
  switch (args.cmd) {
  case 'broadcast':
    console.log(args);
    var destdevices = codeTable[args.code].filter(function (did) {return did!=args.did;});
    destdevices = destdevices.map(function (did) {
      return deviceTable[did];
    });
    if (args.dest!=='[all]') {
      destdevices = destdevices.filter(function (device){
        return args.dest.indexOf(device.role)!=-1;
      });
    }
    destdevices = destdevices.filter(function (device) {
      if (!device.online) {
        device.msgs.push(args);
      }
      return device.online;
    });
    destdevices.every(function (device) {
      device.online.send(args, function (){ //on error
        device.msgs.push(args);
      });
    });
    break;
  case 'get':
    if (needNewDID(args)) {
      var newdid = newCode(deviceTable);
      ret = {'ok':true,'cmd':'did','did':newdid};
      deviceTable[newdid] = {name:'No Name',role:args.role,online:false,msgs:[]};
    } else if (deviceTable[args.did].msgs.length>0) {
      ret = deviceTable[args.did].msgs.shift();
      ret.ok = true;
    } else {
      return false; //do not end the response, and wait for other messages.
    }
    break;
  }
  return ret;
}
http.createServer(function(req, res) {
  var postData = '';
  var userdata=false;
  var mErr;
  var reqd = domain.create();
  reqd.add(req);
  reqd.add(res);
  reqd.on('error', function(er) {
    console.error('Error', er);
    if (mErr) {
      console.log('invoke err func');
      mErr();
    }
    try {
      res.writeHead(500);
      res.end('Error occurred, sorry.');
    } catch (er) {
      console.error('Error sending 500', er, req.url);
    }
  });
  req.setEncoding('utf-8');
  res.send = function (obj, err) {
    var restext='';
    mErr = err;
    try {
      obj.ok=true;
      restext = JSON.stringify(obj);
      res.write(restext);
      res.end();
    } catch (e) {
      console.log('Send Error.',restext);
      err();
    }
  };
  req.addListener('close', function () {
    if (userdata) {
      console.log('close',userdata);
      if (userdata.hasOwnProperty('did')) {
        if (deviceTable.hasOwnProperty(userdata.did)) {
          deviceTable[userdata.did].online = false;
          deviceTable[userdata.did].lasttime = (new Date()).getTime();
        }
      }
    }
  });
  req.addListener('data', function (c) {
    postData += c;
  });
  req.addListener('end', function () {
    res.writeHead(200, {'Content-Type': 'text/plain;charset=utf-8', 'Access-Control-Allow-Origin': '*'});
    if (postData.length > 0) {
      try {
        userdata = JSON.parse(postData);
      } catch (e) {
        res.write('Error: JSON.parse.');
        res.end();
        return;
      }
      var tRet;
      try {
         tRet = handler(userdata);
      } catch (e) {
        res.write('Error: Handler.');
        res.end();
        return;
      }
      if (tRet) {
        res.write(JSON.stringify(tRet));
        res.end();
      } else {
        deviceTable[userdata.did].online = res;
      }
    } else {
      res.write('Hello World 0.0');
      res.end();
    }
  });
}).listen(8765);
setInterval(function (){
  var c=0;
  for (var i in deviceTable) {
    if (deviceTable[i].online) {
      c++;
    }
  }
  //console.log("Online Number:",c);
}, 2000);
console.log("nodejs start listen 8765 port!");  