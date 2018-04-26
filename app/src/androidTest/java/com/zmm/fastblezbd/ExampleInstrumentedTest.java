package com.zmm.fastblezbd;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.zmm.fastblezbd.utils.CRCUtil;
import com.zmm.fastblezbd.utils.CheckUtils;
import com.zmm.fastblezbd.utils.CrcUtil2;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {

        //AA 02 02 BB FF DD
        //D77E
        byte[] bys = {(byte) 0xAA,0x02,0x02, (byte) 0xBB, (byte) 0xFF, (byte) 0xDD};
        byte[] bytesCRC2 = CheckUtils.getBytesCRC2(bys);
        System.out.println("bytesCRC2 = "+bytesCRC2);
        byte[] bytesCRC3 = CrcUtil2.setParamCRC(bys);
        System.out.println("bytesCRC3 = "+bytesCRC3);


    }
}
