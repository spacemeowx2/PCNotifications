package cn.imspace.pcnotifications;

/**
 * Created by space on 15/2/27.
 */
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;
public class Encryption {
    private static final String AESTYPE ="AES/PKCS5Padding";
    private static String getBase64(byte[] s) {
        return Base64.encodeToString(s, Base64.DEFAULT);
    }
    private static byte[] getFromBase64(String s) {
        if (s == null) return null;
        try {
            return Base64.decode(s, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }
    public static String AES_Encrypt(String keyStr, String plainText) {
        byte[] encrypt = null;
        try{
            Key key = generateKey(keyStr);
            Cipher cipher = Cipher.getInstance(AESTYPE);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypt = cipher.doFinal(plainText.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
        return getBase64(encrypt);
    }
    public static String AES_Decrypt(String keyStr, String encryptData) {
        byte[] decrypt = null;
        try{
            Key key = generateKey(keyStr);
            Cipher cipher = Cipher.getInstance(AESTYPE);
            cipher.init(Cipher.DECRYPT_MODE, key);
            decrypt = cipher.doFinal(getFromBase64(encryptData));
            return new String(decrypt).trim();
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }
    private static Key generateKey(String key)throws Exception{
        try{
            return new SecretKeySpec(key.getBytes(), "AES");
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }
}
