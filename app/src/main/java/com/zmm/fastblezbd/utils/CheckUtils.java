package com.zmm.fastblezbd.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;


/**
 * Description:
 * Author:zhangmengmeng
 * Date:2018/4/19
 * Time:下午3:32
 */

public class CheckUtils {

    private static String TAG = CheckUtils.class.getSimpleName();


    public static int CRC_XModem(byte[] bytes){
        int crc = 0x0000;          // initial value
        int polynomial = 0x1021;
        for (int index = 0 ; index< bytes.length; index++) {
            byte b = bytes[index];
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        crc &= 0xffff;
        return crc;
    }

    /**
     * 计算CRC16校验码
     *
     * @param bytes 字节数组
     * @return {@link byte[]} 校验码
     * @since 1.0
     */
    public static byte[] getBytesCRC(byte[] bytes) {
//        int CRC = 0x0000ffff;
//        int POLYNOMIAL = 0x0000a001;
        int CRC = 0x0000;
        int POLYNOMIAL = 0x1021;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        String crcStr = Integer.toHexString(CRC);
        System.out.println("CRC验证码："+crcStr);
        System.out.println("CRC验证码字节数组："+Arrays.toString(StringUtils.toBytes(crcStr)));
        return StringUtils.toBytes(crcStr);
    }

    /**
     * 计算CRC16校验码
     *
     * @param data 字节数组
     * @return {@link byte[]} 校验码
     * @since 1.0
     */
    public static byte[] getBytesCRC2(byte[] data) {
        byte[] buf = new byte[data.length];// 存储需要产生校验码的数据
        for (int i = 0; i < data.length; i++) {
            buf[i] = data[i];
        }
        int len = buf.length;
//        int crc = 0xFFFF;//16位
        int crc = 0x0000;//16位
        for (int pos = 0; pos < len; pos++) {
            if (buf[pos] < 0) {
                crc ^= (int) buf[pos] + 256; // XOR byte into least sig. byte of
                // crc
            } else {
                crc ^= (int) buf[pos]; // XOR byte into least sig. byte of crc
            }
            for (int i = 8; i != 0; i--) { // Loop over each bit
                if ((crc & 0x0001) != 0) { // If the LSB is set
                    crc >>= 1; // Shift right and XOR 0xA001
//                    crc ^= 0xA001;
                    crc ^= 0x1021;
                } else
                    // Else LSB is not set
                    crc >>= 1; // Just shift right
            }
        }
        String c = Integer.toHexString(crc);
        System.out.println("c.length = "+c.length());
        if (c.length() == 4) {
            c = c.substring(2, 4) + c.substring(0, 2);
        } else if (c.length() == 3) {
            c = "0" + c;
            c = c.substring(2, 4) + c.substring(0, 2);
        } else if (c.length() == 2) {
            c = "0" + c.substring(1, 2) + "0" + c.substring(0, 1);
        }

        System.out.println("CRC校验码："+c);
        return StringUtils.toBytes(c);
    }


    /**
     * 计算CRC16校验码
     *
     * @param bytes 字节数组
     * @return {@link String} 校验码
     * @since 1.0
     */
    public static String getCRC(byte[] bytes) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }

        return Integer.toHexString(CRC);
    }


    /**
     * 计算产生校验码 81c9
     * @param data 需要校验的数据
     * @return 校验码
     */
    public static String Make_CRC(byte[] data) {
        byte[] buf = new byte[data.length];// 存储需要产生校验码的数据
        for (int i = 0; i < data.length; i++) {
            buf[i] = data[i];
        }
        int len = buf.length;
        int crc = 0xFFFF;//16位
        for (int pos = 0; pos < len; pos++) {
            if (buf[pos] < 0) {
                crc ^= (int) buf[pos] + 256; // XOR byte into least sig. byte of
                // crc
            } else {
                crc ^= (int) buf[pos]; // XOR byte into least sig. byte of crc
            }
            for (int i = 8; i != 0; i--) { // Loop over each bit
                if ((crc & 0x0001) != 0) { // If the LSB is set
                    crc >>= 1; // Shift right and XOR 0xA001
                    crc ^= 0xA001;
                } else
                    // Else LSB is not set
                    crc >>= 1; // Just shift right
            }
        }
        String c = Integer.toHexString(crc);
        System.out.println("c.length = "+c.length());
        if (c.length() == 4) {
            c = c.substring(2, 4) + c.substring(0, 2);
        } else if (c.length() == 3) {
            c = "0" + c;
            c = c.substring(2, 4) + c.substring(0, 2);
        } else if (c.length() == 2) {
            c = "0" + c.substring(1, 2) + "0" + c.substring(0, 1);
        }

        return c;
    }


    public static String getBCC(byte[] data) {

        String ret = "";
        byte BCC[]= new byte[1];
        for(int i=0;i<data.length;i++)
        {
            BCC[0]=(byte) (BCC[0] ^ data[i]);
        }
        String hex = Integer.toHexString(BCC[0] & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        ret += hex.toUpperCase();
        return ret;
    }

    public static int getBCC2(byte[] data) {

        int ret;
        byte BCC[]= new byte[1];
        for(int i=0;i<data.length;i++) {
            BCC[0]=(byte) (BCC[0] ^ data[i]);
        }
        ret = BCC[0] & 0xFF;
        return ret;
    }

    public static String checkcode_0007(String para) {
        int length = para.length() / 2;
        String[] dateArr = new String[length];

        for (int i = 0; i < length; i++) {
            dateArr[i] = para.substring(i * 2, i * 2 + 2);
        }
        String code = "00";
        for (int i = 0; i < dateArr.length; i++) {
            code = xor(code, dateArr[i]);
        }
        return code;
    }


    private static String xor(String strHex_X,String strHex_Y){
        //将x、y转成二进制形式
        String anotherBinary=Integer.toBinaryString(Integer.valueOf(strHex_X,16));
        String thisBinary=Integer.toBinaryString(Integer.valueOf(strHex_Y,16));
        String result = "";
        //判断是否为8位二进制，否则左补零
        if(anotherBinary.length() != 8){
            for (int i = anotherBinary.length(); i <8; i++) {
                anotherBinary = "0"+anotherBinary;
            }
        }
        if(thisBinary.length() != 8){
            for (int i = thisBinary.length(); i <8; i++) {
                thisBinary = "0"+thisBinary;
            }
        }
        //异或运算
        for(int i=0;i<anotherBinary.length();i++){
            //如果相同位置数相同，则补0，否则补1
            if(thisBinary.charAt(i)==anotherBinary.charAt(i))
                result+="0";
            else{
                result+="1";
            }
        }
        Log.e("code",result);
        return Integer.toHexString(Integer.parseInt(result, 2));
    }

    public static byte[] xor(byte[] text,String keys) throws UnsupportedEncodingException {
        byte[] result = new byte[text.length];
        byte[] keyArray = keys.getBytes("UTF-8");
        byte key;
        int size = text.length;
        for (int i = 0; i < size; i++) {
            key = keyArray[i % keyArray.length];
            result[i] = (byte) (text[i] ^ key);
        }
        return result;
    }


    public static byte[] encrypt(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        int len = bytes.length;
        int key = 0x12;
        for (int i = 0; i < len; i++) {
            bytes[i] ^= key;
        }
        return bytes;
    }

    public static byte[] encrypt2(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        int len = bytes.length;
        int key = 0x12;
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) (bytes[i] ^ key);
            key = bytes[i];
        }
        return bytes;
    }

    /**
     *  异或校验
     *
     * @param data 十六进制串
     * @return checkData  十六进制串
     *
     * */
    public static String checkXor(String data) {
        int checkData = 0;
        for (int i = 0; i < data.length(); i = i + 2) {
            //将十六进制字符串转成十进制
            int start = Integer.parseInt(data.substring(i, i + 2), 16);
            //进行异或运算
            checkData = start ^ checkData;
        }
        return integerToHexString(checkData);
    }

    /**
     * 将十进制整数转为十六进制数，并补位
     */
    public static String integerToHexString(int s) {
        String ss = Integer.toHexString(s);
        if (ss.length() % 2 != 0) {
            ss = "0" + ss;//0F格式
        }
        return ss.toUpperCase();
    }

    /**
     * 累加和校验，并取反
     */
    public static String makeCheckSum(String data) {
        if (data == null || data.equals("")) {
            return "";
        }
        int total = 0;
        int len = data.length();
        int num = 0;
        while (num < len) {
            String s = data.substring(num, num + 2);
            System.out.println(s);
            total += Integer.parseInt(s, 16);
            num = num + 2;
        }

        //用256求余最大是255，即16进制的FF
        int mod = total % 256;
        if (mod == 0) {
            return "FF";
        } else {
            String hex = Integer.toHexString(mod).toUpperCase();

            Log.e(TAG, "取反前校验位: " + hex);
            //十六进制数取反结果
            hex = parseHex2Opposite(hex);
            return hex;
        }
    }

    /**
     * 取反
     */
    public static String parseHex2Opposite(String str) {
        String hex;
        //十六进制转成二进制
        byte[] er = parseHexStr2Byte(str);

        //取反
        byte erBefore[] = new byte[er.length];
        for (int i = 0; i < er.length; i++) {
            erBefore[i] = (byte) ~er[i];
        }

        //二进制转成十六进制
        hex = parseByte2HexStr(erBefore);

        // 如果不够校验位的长度，补0,这里用的是两位校验
        hex = (hex.length() < 2 ? "0" + hex : hex);

        return hex;
    }


    /**
     * 将二进制转换成十六进制
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将十六进制转换为二进制
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }
}
