package com.zistone.libble.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 字符串转换，不支持特殊字符
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public final class MyConvertUtil {

    //16进制数字字符集
    public static final String HEXSTRING = "0123456789ABCDEF";

    /**
     * （禁止外部实例化）
     */
    private MyConvertUtil() {
    }

    /**
     * 隔指定位置插入字符
     *
     * @param input
     * @param interval
     * @param character
     * @return
     */
    public static String StrAddCharacter(String input, int interval, String character) {
        //字符串长度
        int length = input.length();
        //需要添加字符的数量
        int count = 0;
        if (length <= interval) {
            count = 0;
        } else {
            count = length % interval > 0 ? length / interval : input.length() / interval - 1;
        }
        //插入空格
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                input = input.substring(0, (i + 1) * interval + i) + character + input.substring((i + 1) * interval + i, length + i);
            }
        }
        //输入的字符串小于指定位，不需要添加字符
        else {
        }
        return input;
    }

    /**
     * Str前/后面补零
     *
     * @param str
     * @param strLength
     * @return
     */
    public static String AddZeroForNum(String str, int strLength, boolean isLeft) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                //左补零
                if (isLeft) {
                    sb.append("0").append(str);
                }
                //右补零
                else {
                    sb.append(str).append("0");
                }
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    /**
     * Str[]排序
     *
     * @param array
     * @param asc
     * @return
     */
    public static String[] SortStringArray(String[] array, boolean asc) {
        //从小到大
        if (asc) {
            for (int i = 0; i < array.length; i++) {
                for (int j = 0; j < array.length; j++) {
                    if (Integer.parseInt(array[i], 16) < Integer.parseInt(array[j], 16)) {
                        String temp = array[i];
                        array[i] = array[j];
                        array[j] = temp;
                    }
                }
            }
        }
        //从大到小
        else {
            for (int i = 0; i < array.length; i++) {
                for (int j = 0; j < array.length; j++) {
                    if (Integer.parseInt(array[i], 16) > Integer.parseInt(array[j], 16)) {
                        String temp = array[i];
                        array[i] = array[j];
                        array[j] = temp;
                    }
                }
            }
        }
        return array;
    }

    /**
     * 反转byte[]
     *
     * @param byteArray
     * @return
     */
    public static byte[] ReverseByteArray(byte[] byteArray) {
        byte tempByte;
        for (int start = 0, end = byteArray.length - 1; start < end; start++, end--) {
            tempByte = byteArray[end];
            byteArray[end] = byteArray[start];
            byteArray[start] = tempByte;
        }
        return byteArray;
    }

    /**
     * 替换字符串里最后出现的元素
     *
     * @param text
     * @param strToReplace
     * @param replaceWithThis
     * @return
     */
    public static String ReplaceLast(String text, String strToReplace, String replaceWithThis) {
        return text.replaceFirst("(?s)" + strToReplace + "(?!.*?" + strToReplace + ")", replaceWithThis);
    }

    /**
     * 大端模式，低字节在前
     *
     * @param b
     * @return
     */
    public static String ByteToBitBig(byte b) {
        return "" + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1) + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1) + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1) + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }

    /**
     * 小端模式，高字节在前
     *
     * @param b
     * @return
     */
    public static String ByteToBitLittle(byte b) {
        return "" + (byte) ((b >> 0) & 0x1) + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 2) & 0x1) + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 4) & 0x1) + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 6) & 0x1) + (byte) ((b >> 7) & 0x1);
    }

    /**
     * 生成4位不同的随机数
     *
     * @return
     */
    public static int CreateDifferent4Random() {
        int n = 1;
        int array[] = new int[4];
        int result = 0;
        //产生第一位大于0的随机数
        array[0] = (int) (9 * Math.random() + 1);
        //利用循环产生3个不同的随机数,且与第一个随机数不同
        for (int i = 1; i < 4; i++) {
            array[i] = (int) (10 * Math.random());
            //这里是循环判断a[i]与他们前面的每一位是否相同
            for (int j = 0; j < i; j++) {
                //如果a[i]等于a[j]就重新赋随机值在进入这个循环判断
                while (array[i] == array[j]) {
                    j = 0;
                    array[i] = (int) (10 * Math.random());
                }
            }
        }
        //在这里每循环一次,n就会乘以10
        for (int i = 3; i >= 0; i--) {
            result = array[i] * n + result;
            n = n * 10;
        }
        return result;
    }

    /**
     * 不带空格不带0x的16进制str转array
     *
     * @param hexStr
     * @return
     */
    public static String[] HexStrSplit(String hexStr) {
        int strLength = hexStr.length();
        List<String> list = new ArrayList<>();
        String str = "";
        for (int i = 0; i < strLength; i++) {
            if (i % 2 == 0) {
                str = String.valueOf(hexStr.charAt(i));
            } else {
                str += String.valueOf(hexStr.charAt(i));
                list.add(str);
            }
        }
        return list.toArray(new String[0]);
    }

    /**
     * 低功耗蓝牙设备校验码
     * <p>
     * 然后按位叠加和并舍弃高位得到的结果就是校验码
     *
     * @param hexStr 带空格不带0x的16进制字符串,比如81 03 00
     * @return 不足2位前面补零
     */
    public static String CRC_Zistone_BLE(String hexStr) throws Exception {
        if (!hexStr.contains(" ") || hexStr.contains("0x") || hexStr.contains("0X")) {
            throw new Exception("参数必须为带空格不带0x的16进制字符串");
        }
        int binaryNum = 0;
        String[] strArray = hexStr.split(" ");
        for (int i = 0; i < strArray.length; i++) {
            int tempHexNum = Integer.parseInt(strArray[i], 16);
            if (i == 0) {
                binaryNum = tempHexNum;
            } else {
                binaryNum += tempHexNum;
            }
        }
        //舍弃高位
        String result = Integer.toHexString(binaryNum & 0xFF);
        //前面补零使成为偶数，方便截取
        if (result.length() % 2 != 0) {
            result = "0" + result;
        }
        return result;
    }

    /**
     * 生成校验码
     * 仅用于LP108的单片机
     *
     * @param src
     * @return
     * @throws NullPointerException
     */
    public static int CRC_Zistone_LP108(byte[] src) throws NullPointerException {
        if (src == null) {
            throw new NullPointerException("src");
        }
        int crc = 0;
        for (byte ch : src) {
            // 0x80 = 128
            for (int i = 0x80; i != 0; i /= 2) {
                crc *= 2;
                // 0x10000 = 65536
                if ((crc & 0x10000) != 0)
                    crc ^= 0x11021; // 0x11021 = 69665
                if ((ch & i) != 0)
                    crc ^= 0x1021; // 0x1021 = 4129
            }
        }
        return crc;
    }

    /**
     * 生成校验码
     * CRC-16/CCITT_FALSE方式生成校验码
     *
     * @param bytes
     * @param length
     * @return
     */
    public static String CRC_16_CCITT_FALSE(byte[] bytes, int length) {
        int crc = 0xffff;
        int polynomial = 0x1021;
        for (int index = 0; index < bytes.length; index++) {
            byte b = bytes[index];
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }
        crc &= 0xffff;
        //输出String字样的16进制
        String strCrc = Integer.toHexString(crc).toUpperCase();
        return strCrc;
    }

    /**
     * Str[]的每个元素拼接成Str
     *
     * @param strArray
     * @return
     */
    public static String StrArrayToStr(String[] strArray) {
        String str = "";
        for (String tempStr : strArray) {
            str += tempStr;
        }
        return str;
    }

    /**
     * 10进制int转16进制Str
     *
     * @param value
     * @return
     */
    public static String IntToHexStr(int value) {
        StringBuffer stringBuffer = new StringBuffer();
        char[] charArray = HEXSTRING.toCharArray();
        while (value != 0) {
            stringBuffer = stringBuffer.append(charArray[value % 16]);
            value = value / 16;
        }
        String result = stringBuffer.reverse().toString();
        if (result.length() < 2) {
            result = "0" + result;
        }
        return result;
    }

    /**
     * 16进制的Str转Str
     *
     * @param hexStr
     * @return
     */
    public static String HexStrToStr(String hexStr) {
        try {
            //能被16整除,肯定可以被2整除
            byte[] array = new byte[hexStr.length() / 2];
            for (int i = 0; i < array.length; i++) {
                array[i] = (byte) (0xff & Integer.parseInt(hexStr.substring(i * 2, i * 2 + 2), 16));
            }
            hexStr = new String(array, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return hexStr;
    }

    /**
     * Str转16进制Str
     *
     * @param str
     * @return
     */
    public static String StrToHexStr(String str) {
        if (null == str || "".equals(str)) {
            return "";
        }
        //根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        StringBuilder stringBuilder = new StringBuilder(bytes.length * 2);
        //将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            //            stringBuilder.append("0x");
            stringBuilder.append(HEXSTRING.charAt((bytes[i] & 0xf0) >> 4));
            stringBuilder.append(HEXSTRING.charAt((bytes[i] & 0x0f) >> 0));
            //去掉末尾的逗号
            if (i != bytes.length - 1) {
                //                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 二进制Str转byte
     *
     * @param binaryStr
     * @return
     */
    public static byte BinaryStrToByte(String binaryStr) {
        int re, len;
        if (null == binaryStr) {
            return 0;
        }
        len = binaryStr.length();
        if (len != 4 && len != 8) {
            return 0;
        }
        //8bit处理
        if (len == 8) {
            //正数
            if (binaryStr.charAt(0) == '0') {
                re = Integer.parseInt(binaryStr, 2);
            }
            //负数
            else {
                re = Integer.parseInt(binaryStr, 2) - 256;
            }
        }
        //4bit处理
        else {
            re = Integer.parseInt(binaryStr, 2);
        }
        return (byte) re;
    }

    /**
     * long转byte[8]
     *
     * @param value
     * @return
     */
    public byte[] LongToByteArray8(long value) {
        byte[] result = new byte[8];
        int temp;
        for (int i = 0; i < 8; i++) {
            temp = (8 - 1 - i) * 8;
            if (temp == 0) {
                result[i] += (value & 0x0ff);
            } else {
                result[i] += (value >>> temp) & 0x0ff;
            }
        }
        return result;
    }

    /**
     * byte[4]转long
     *
     * @param byteArray 长度为4的字节数组
     * @return
     */
    public static long ByteArray4ToLong(byte[] byteArray) {
        int temp0 = byteArray[0] & 0xFF;
        int temp1 = byteArray[1] & 0xFF;
        int temp2 = byteArray[2] & 0xFF;
        int temp3 = byteArray[3] & 0xFF;
        return (((long) temp0 << 24) + (temp1 << 16) + (temp2 << 8) + temp3);
    }

    /**
     * byte[4]转int
     *
     * @param byteArray 长度为4的字节数组
     * @return
     */
    public static int ByteArray4ToInt(byte[] byteArray) {
        int temp0 = byteArray[0] & 0xFF;
        int temp1 = byteArray[1] & 0xFF;
        int temp2 = byteArray[2] & 0xFF;
        int temp3 = byteArray[3] & 0xFF;
        return ((temp0 << 24) + (temp1 << 16) + (temp2 << 8) + temp3);
    }

    /**
     * byte[2]转int
     *
     * @param byteArray
     * @return
     */
    public static int ByteArray2ToInt(byte[] byteArray) {
        int temp0 = byteArray[0] & 0xFF;
        int temp1 = byteArray[1] & 0xFF;
        return ((temp0 << 8) + temp1);
    }

    /**
     * byte转int
     *
     * @param b
     * @return
     */
    public static int ByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    /**
     * 16进制Str转byte[]
     *
     * @param hexStr 不带空格、不带0x、不带逗号的16进制Str,如:06EEF7F1
     * @return
     */
    public static byte[] HexStrToByteArray(String hexStr) {
        byte[] byteArray = new byte[hexStr.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = hexStr.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    /**
     * byte转16进制Str
     *
     * @param b
     */
    public static String ByteToHexStr(byte b) {
        char[] hexArray = HEXSTRING.toCharArray();
        char[] hexChars = new char[2];
        int temp = b & 0xFF;
        hexChars[0] = hexArray[temp >>> 4];
        hexChars[1] = hexArray[temp & 0x0F];
        return new String(hexChars);
    }

    /**
     * byte[]转16进制Str
     *
     * @param byteArray
     * @param length
     * @return
     */
    public static String ByteArrayToHexStr(byte[] byteArray, int length) {
        StringBuffer stringBuffer = new StringBuffer(length);
        String temp;
        for (int i = 0; i < length; i++) {
            temp = Integer.toHexString(0xFF & byteArray[i]);
            if (temp.length() < 2) {
                stringBuffer.append(0);
            }
            stringBuffer.append(temp.toUpperCase());
        }
        return stringBuffer.toString();
    }

    /**
     * byte[]转16进制Str
     *
     * @param byteArray
     */
    public static String ByteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = HEXSTRING.toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int i = 0; i < byteArray.length; i++) {
            int temp = byteArray[i] & 0xFF;
            hexChars[i * 2] = hexArray[temp >>> 4];
            hexChars[i * 2 + 1] = hexArray[temp & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 获取该字符对应的16进制
     *
     * @param str
     * @return
     */
    private static String GetHexStr(String str) {
        String hexStr = "";
        for (int i = str.length(); i < 4; i++) {
            if (i == str.length()) {
                hexStr = "0";
            } else {
                hexStr = hexStr + "0";
            }
        }
        return hexStr + str;
    }

    /**
     * 16进制的Str转成Unicode编码的中文
     *
     * @param hexStr
     * @return
     */
    public static String EnUnicode(String hexStr) {
        String enUnicode = null;
        String deUnicode = null;
        for (int i = 0; i < hexStr.length(); i++) {
            if (enUnicode == null) {
                enUnicode = String.valueOf(hexStr.charAt(i));
            } else {
                enUnicode = enUnicode + hexStr.charAt(i);
            }
            if (i % 4 == 3) {
                if (enUnicode != null) {
                    if (deUnicode == null) {
                        deUnicode = String.valueOf((char) Integer.valueOf(enUnicode, 16).intValue());
                    } else {
                        deUnicode = deUnicode + (char) Integer.valueOf(enUnicode, 16).intValue();
                    }
                }
                enUnicode = null;
            }
        }
        return deUnicode;
    }

    /**
     * Unicode编码的中文转16进制的Str
     *
     * @param str
     * @return
     */
    public static String DeUnicode(String str) {
        String hexStr = "0x";
        for (int i = 0; i < str.length(); i++) {
            if (i == 0) {
                hexStr = GetHexStr(Integer.toHexString(str.charAt(i)).toUpperCase());
            } else {
                hexStr = hexStr + GetHexStr(Integer.toHexString(str.charAt(i)).toUpperCase());
            }
        }
        return hexStr;
    }

}
