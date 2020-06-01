package com.zistone.blecontrol.util;

import java.io.UnsupportedEncodingException;

/**
 * 字符转换工具类
 * 不支持特殊字符
 */
public class MyConvertUtil {
    //16进制数字字符集
    public static final String HEXSTRING = "0123456789ABCDEF";

    public static void main(String[] args) throws Exception {

        System.out.println("生成的检验码为:" + CreateCheckCode("AA 31 1B 00 03 31 39 30 32 41 51 36 38 30 30 30 30 30 32 35 34 32 00 01" + "00 01 05 98 25"));
        System.out.println("CRC_16_CCITT_FALSE生成的校验码:" + CRC_16_CCITT_FALSE(new byte[]{(byte) 0xAA, 0x31, 0x1B, 0x00, 0x03, 0x31, 0x39, 0x30, 0x32,
                0x41, 0x51, 0x36, 0x38, 0x30, 0x30, 0x30, 0x30, 0x30, 0x32,
                0x35, 0x34, 0x32, 0x00, 0x01, 0x00, 0x01, 0x05}, 27));
        byte[] reverseArray = ReverseByteArray(new byte[]{(byte) 0xAA, 0x31, 0x1B, 0x00, 0x03, 0x31, 0x39, 0x30, 0x32, 0x41, 0x51, 0x36,
                0x38, 0x30, 0x30, 0x30, 0x30, 0x30, 0x32, 0x35, 0x34, 0x32, 0x00, 0x01, 0x00,
                0x01, 0x05});
        System.out.println("反转byte[]:");
        for (byte b : reverseArray) {
            System.out.print(ByteToHexStr(b)+" ");
        }
        System.out.println();
        System.out.println("____________________________________________________________________");

        System.out.println("byte转bit:" + ByteToBit((byte) 0x01) + ByteToBit((byte) 0x05));
        System.out.println("bit添加空格:" + MyConvertUtil.StrAddCharacter(ByteToBit((byte) 0x05) + ByteToBit((byte) 0x01), " "));
        System.out.println("byte转16进制的Str:" + ByteToHexStr((byte) 0xAA));
        System.out.println("byte转int:" + MyConvertUtil.ByteToInt((byte) 0x03));
        System.out.println("byte[2]转int:" + ByteArray2ToInt(new byte[]{0x01, 0x2D}));
        System.out.println("byte[]转16进制的Str:" + MyConvertUtil.ByteArrayToHexStr(new byte[]{(byte) 0x98, 0x25}));
        System.out.println("byte[]转16进制的Str:" + ByteArrayToHexStr(new byte[]{(byte) 2, (byte) 97, (byte) 51, (byte) 52}));
        System.out.println((double) ByteArray4ToLong(new byte[]{(byte) 6, (byte) -18, (byte) -9, (byte) -15}) / 1000000);
        System.out.println("____________________________________________________________________");
        System.out.println("普通Str转16进制Str:" + StrToHexStr("rD9TcH"));

        int aaa = Integer.parseInt("1997", 16);
        System.out.println("Byte[]转int:" + Integer.parseInt("1B00", 16));

        System.out.println("16进制Str转普通Str:" + HexStrToStr("075772075F06C3"));
        System.out.println("____________________________________________________________________");
        System.out.println("Unicode编码的中文转16进制的Str:" + DeUnicode("李小伟"));
        System.out.println("Unicode编码的中文转16进制的Str:" + DeUnicode("LiWei"));
        System.out.println("16进制的Str转成Unicode编码的中文:" + EnUnicode("674E5C0F4F1F"));
        System.out.println("16进制的Str转成Unicode编码的中文:" + EnUnicode("004C0069005700650069"));
        System.out.println("____________________________________________________________________");
        //        TestBinaryStrToInt();
    }

    private static void TestBinaryStrToInt() {
        System.out.println("____________________________________________________________________");
        String bitStr1 = "11111111";
        String bitStr2 = "00000000";
        String bitStr3 = "11000011";
        String bitStr4 = "01111110";
        String bitStr5 = "00111100";
        String bitStr6 = "00011000";
        String bitStr7 = "00010000";
        String bitStr8 = "10000000";
        String bitStr9 = "10000001";
        String bitStr10 = "10101010";
        String bitStr11 = "01010101";
        System.out.println(bitStr1 + "\t二进制Str转十进制Int:" + Integer.parseInt(bitStr1, 2) + "\t\t" + "十进制Int转十六进制Int:" + Integer.toHexString(Integer.parseInt(bitStr1, 2)));
        System.out.println(bitStr2 + "\t二进制Str转十进制Int:" + Integer.parseInt(bitStr2, 2) + "\t\t" + "十进制Int转十六进制Int:" + Integer.toHexString(Integer.parseInt(bitStr2, 2)));
        System.out.println(bitStr3 + "\t二进制Str转十进制Int:" + Integer.parseInt(bitStr3, 2) + "\t\t" + "十进制Int转十六进制Int:" + Integer.toHexString(Integer.parseInt(bitStr3, 2)));
        System.out.println(bitStr4 + "\t二进制Str转十进制Int:" + Integer.parseInt(bitStr4, 2) + "\t\t" + "十进制Int转十六进制Int:" + Integer.toHexString(Integer.parseInt(bitStr4, 2)));
        System.out.println(bitStr5 + "\t二进制Str转十进制Int:" + Integer.parseInt(bitStr5, 2) + "\t\t" + "十进制Int转十六进制Int:" + Integer.toHexString(Integer.parseInt(bitStr5, 2)));
        System.out.println(bitStr6 + "\t二进制Str转十进制Int:" + Integer.parseInt(bitStr6, 2) + "\t\t" + "十进制Int转十六进制Int:" + Integer.toHexString(Integer.parseInt(bitStr6, 2)));
        System.out.println(bitStr7 + "\t二进制Str转十进制Int:" + Integer.parseInt(bitStr7, 2) + "\t\t" + "十进制Int转十六进制Int:" + Integer.toHexString(Integer.parseInt(bitStr7, 2)));
        System.out.println(bitStr8 + "\t二进制Str转十进制Int:" + Integer.parseInt(bitStr8, 2) + "\t\t" + "十进制Int转十六进制Int" + ":" + Integer.toHexString(Integer.parseInt(bitStr8, 2)));
        System.out.println(bitStr9 + "\t二进制Str转十进制Int:" + Integer.parseInt(bitStr9, 2) + "\t\t" + "十进制Int转十六进制Int" + ":" + Integer.toHexString(Integer.parseInt(bitStr9, 2)));
        System.out.println(bitStr10 + "\t二进制Str转十进制Int:" + Integer.parseInt(bitStr10, 2) + "\t\t" + "十进制Int转十六进制Int" + ":" + Integer.toHexString(Integer.parseInt(bitStr10, 2)));
        System.out.println(bitStr11 + "\t二进制Str转十进制Int:" + Integer.parseInt(bitStr11, 2) + "\t\t" + "十进制Int转十六进制Int" + ":" + Integer.toHexString(Integer.parseInt(bitStr11, 2)));
        System.out.println("____________________________________________________________________");
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

    public static byte BitToByte(String bit) {
        int result, len;
        if (null == bit) {
            return 0;
        }
        len = bit.length();
        if (len != 4 && len != 8) {
            return 0;
        }
        if (len == 8) {
            //正数
            if (bit.charAt(0) == '0') {
                result = Integer.parseInt(bit, 2);
            }
            //负数
            else {
                result = Integer.parseInt(bit, 2) - 256;
            }
        }
        //4bit处理
        else {
            result = Integer.parseInt(bit, 2);
        }
        return (byte) result;
    }

    public static String ByteToBit(byte b) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append((b >> 7) & 0x1).append((b >> 6) & 0x1).append((b >> 5) & 0x1).append((b >> 4) & 0x1).append((b >> 3) & 0x1).append((b >> 2) & 0x1).append((b >> 1) & 0x1).append((b >> 0) & 0x1);
        return stringBuffer.toString();
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
     * 不带空格的16进制字符串插入指定字符
     *
     * @param str       不带空格的字符串
     * @param character 指定字符
     * @return
     */
    public static String StrAddCharacter(String str, String character) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            if (i != str.length() - 1) {
                stringBuffer.append(str.charAt(i));
                stringBuffer.append(character);

            } else {
                stringBuffer.append(str.charAt(i));
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 不带空格的16进制字符串插入指定字符
     *
     * @param hexStr    不带空格不带0x的16进制字符串,比如810300
     * @param character 指定字符
     * @return
     */
    public static String HexStrAddCharacter(String hexStr, String character) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < hexStr.length(); i++) {
            if (i != hexStr.length() - 1) {
                if (i % 2 != 0) {
                    stringBuffer.append(hexStr.charAt(i));
                    stringBuffer.append(character);
                } else {
                    stringBuffer.append(hexStr.charAt(i));
                }
            } else {
                stringBuffer.append(hexStr.charAt(i));
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 生成校验码
     * 将收到的消息还原转义后去除标识和校验位,然后按位异或得到的结果就是校验码
     *
     * @param hexStr 带空格不带0x的16进制字符串,比如81 03 00
     * @return 不足2位前面补零
     */
    public static String CreateCheckCode(String hexStr) throws Exception {
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
                binaryNum ^= tempHexNum;
            }
        }
        String result = Integer.toHexString(binaryNum);
        if (result.length() < 2) {
            result = "0" + result;
        }
        return result;
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
        return stringBuffer.reverse().toString();
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
            stringBuilder.append("0x");
            stringBuilder.append(HEXSTRING.charAt((bytes[i] & 0xf0) >> 4));
            stringBuilder.append(HEXSTRING.charAt((bytes[i] & 0x0f) >> 0));
            //去掉末尾的逗号
            if (i != bytes.length - 1) {
                stringBuilder.append(",");
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
            if (i == str.length())
                hexStr = "0";
            else
                hexStr = hexStr + "0";
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
