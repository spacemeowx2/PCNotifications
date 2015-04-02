package cn.imspace.pcnotifications;

/**
 * Created by space on 15/2/27.
 */
import android.test.InstrumentationTestCase;
//http://cjxixi.iteye.com/blog/1746495
public class TestClass extends InstrumentationTestCase {
    public void test() {
        System.out.println(Encryption.AES_Encrypt("key", "test"));
    }
}
