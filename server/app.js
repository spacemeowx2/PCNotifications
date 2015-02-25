var http = require("http");
var codeTable = {};   //key: code       value: {devices[],msgs[]}
var deviceTable = {}; //key: deviceid   value: {name,role,lasttime}
var CODECHAR = "0x12345678x9";
var CODELEN = 10;
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
function handler(args) {
  var ret = {'ok':true};
  switch (args.cmd) {
  case 'broadcast':
    if (args.dest=='[all]') {
      deviceTable[args.did]
    }
    break;
  case 'code':
    ret.code = newCode(codeTable);
    ret.did = newCode(deviceTable);
    break;
  }
  return ret;
}
http.createServer(function(req, res) {
  req.setEncoding('utf-8');
  var postData = '';
  req.addListener('data', function (c) {
    postData += c;
  });
  req.addListener('end', function () {
    res.writeHead(200, {'Content-Type': 'text/plain;charset=utf-8', 'Access-Control-Allow-Origin': '*'});
    if (postData.length > 0) {
      try {
        var tobj = JSON.parse(postData);
      } catch (e) {
        res.write('Error: JSON.parse.');
        res.end();
        return;
      }
      var tRet = handler(tobj);
      res.write(JSON.stringify(tRet));
      res.end();
    } else {
      res.write('Hello World 0.0');
      res.end();
    }
  });
}).listen(8765);  
console.log("nodejs start listen 8765 port!");  