package android_serialport_api;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import android_serialport_api.SerialPort;

public class SerialPortManager
{
    public Context m_context;
    // 串口参数
    public File m_device = new File("/dev/smd11");
    public int m_baudrate = 115200;
    public SerialPort m_serialPort;
    public InputStream m_inputStream;
    public OutputStream m_outputStream;

    public SerialPortManager()
    {
        try
        {
            m_serialPort = new SerialPort(m_device, m_baudrate, 0);
            m_inputStream = m_serialPort.getInputStream();
            m_outputStream = m_serialPort.getOutputStream();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /***
     * 获取设备的标志位
     *
     * @return
     */
    public String GetDeviceFlagNumber()
    {
        try
        {
            String cmd = new String("AT+QNVR=2499,0\r\n");
            WriterSerialPort(cmd.getBytes());
            Thread.sleep(50);
            byte[] array = ReaderSerialPort(1024);
            String result = "";
            if (array != null)
            {
                result = new String(array);
            }
            if (result.contains("OK"))
            {
                return SubTwoStrContent(result, "\"", "\"").substring(0, 8);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    /***
     * 获取设备的SN
     *
     * @return
     */
    public String GetDeviceSN()
    {
        try
        {
            String cmd = new String("AT+QCSN?\r\n");
            WriterSerialPort(cmd.getBytes());
            Thread.sleep(50);
            byte[] array = ReaderSerialPort(1024);
            String result = "";
            if (array != null)
            {
                result = new String(array);
            }
            if (result.contains("OK"))
            {
                return SubTwoStrContent(result, "\"", "\"");
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    /***
     * 获取指定位置的标志是否通过
     *
     * @param index
     * @return
     */
    public boolean GetWriterFlagIsPass(int index)
    {
        String flagNumber = GetDeviceFlagNumber();
        int value = Integer.valueOf(flagNumber.substring(index - 1, index));
        if (value == 1)
        {
            return true;
        }
        return false;
    }

    /***
     * 读取写入命令后返回的内容
     * @param length
     * @return
     * @throws IOException
     */
    public byte[] ReaderSerialPort(int length)
    {
        int size = 0;
        byte[] array = new byte[length];
        try
        {
            size = m_inputStream.read(array, 0, length);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if (size <= 0)
        {
            return null;
        }
        return array;
    }

    /***
     * 截取两个字符串之间的内容
     *
     * @param sourse
     *            要截取的字符串
     * @param startStr
     *            开始字符
     * @param endStr
     *            结束字符
     * @return
     */
    public String SubTwoStrContent(String sourse, String startStr, String endStr)
    {
        String result = "";
        int startIndex, endIndex;
        try
        {
            // 开始字符首次出现的位置
            startIndex = sourse.indexOf(startStr);
            if (startIndex == -1)
            {
                return result;
            }
            // 结束字符首次出现的位置
            endIndex = sourse.lastIndexOf(endStr);
            if (endIndex == -1)
            {
                return result;
            }
            result = sourse.substring(startIndex + 1, endIndex);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /***
     * 写入标志位
     * @param str
     * @throws IOException
     */
    public void WriterFlag(String str)
    {
        if ("".equals(str) || str == null)
        {
            return;
        }
        String cmd = new String("AT+QNVW=2499,0," + "\"" + str + "\"\r\n");
        WriterSerialPort(cmd.getBytes());
    }

    /**
     * 将某个标志写入指定位置
     *
     * @param flagNumber 设备的标志位
     * @param index      索引
     * @param value      标志
     * @return
     */
    public String WriterFlag(String flagNumber, int index, byte value)
    {
        byte[] flagArray = flagNumber.getBytes();
        if (flagArray.length <= 0 || index < 0)
        {
            return "";
        }
        flagArray[index - 1] = value;
        String str = new String(flagArray);
        String cmd = new String("AT+QNVW=2499,0," + "\"" + str + "\"\r\n");
        WriterSerialPort(cmd.getBytes());
        return str;
    }

    /***
     * 写入命令
     * @param data
     * @return
     * @throws IOException
     */
    public void WriterSerialPort(byte[] data)
    {
        if (m_outputStream != null && data != null && m_serialPort != null)
        {
            try
            {
                m_outputStream.write(data);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
