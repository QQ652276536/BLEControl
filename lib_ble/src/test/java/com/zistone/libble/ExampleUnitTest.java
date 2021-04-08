package com.zistone.libble;

import com.zistone.libble.util.MyConvertUtil;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void TestCRC_Zistone_BLE() throws Exception {
        String checkCode = MyConvertUtil.CRC_Zistone_BLE("68 00 00 00 00 00 00 68 23 00");
        System.out.println("计算出来的校验码：" + checkCode);
    }
}