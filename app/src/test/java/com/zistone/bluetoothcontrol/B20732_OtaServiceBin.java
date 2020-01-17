/******************************************************************************
 *
 *  Copyright (C) 2013 Broadcom Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  The offset of the second SS. For EEPROM, SS can be at 0x00, 0x100, 0x200, 0x400, 0x800, 0x1000 and 0x2000. 
 *  SS1 is almost always 0x00. So SS2 can be any of the others. Default SS2 we will use is 0x100. You may use any of the others if you want the EEPROM layout to be different.
 *  We will 'allocate' 256 bytes for SS1 (even though it takes up only 40 bytes leaving a 216 byte hole) and set up 
 *  SS2 at 0x100. Since we know that SS2 will be 40 bytes long, we can set up VS at 0x2C0 and of length 0x200 bytes,
 *  DS1 at 0x4C0 and DS2 at 0x84C0 leaving 0x8000 bytes for DS1 and 0x7B40 (about 31.5KB) for DS2.
 *  So, the layout in EEPROM will be as follows with the following offsets:
 *   # 0-----0x100-------0x2C0-----------0x4C0----------------------------0x84C0----------------------X
 *   # |  SS1  |     SS2   |       VS      |              DS1                |          DS2           |
 *   # X----------------------------------------------------------------------------------------------X
 * These are more or less constants of the OTAFU protocol. If you changed these IDs in the FW, use the ones the FW uses.
 **********************************************************************************************************************/

package com.zistone.bluetoothcontrol;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.UUID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Environment;
import android.util.Log;

public class B20732_OtaServiceBin
{
    private static final String TAG = "OTA_UPdate";

    private static final int maxOtaDataSize = 16; // That is the max size of
    // data in the OTA PDU.
    private short maxSSLen = 0x28; // The static section is always 40 bytes
    // long.
    private short secondSSOffset = 0x100;
    private short firstSSDsOffset = 0x4C0;

    private static final int eeprom_address = 0xFF0004C0;
    // private int offset = 0, pack_id = 0, patch_size = 0;

    private String burnimg_path; // The path of the burn image that will be
    // generated when updating.

    private BluetoothGatt mGattService = null;
    private BluetoothGattCharacteristic mUpdateValueCharact = null;
    private BluetoothGattCharacteristic mUpdateResultCharact = null;

    private BufferedReader image_file_reader = null;
    private short last_length = 0;

    private static final byte ID_ENABLE_OTAFU = 0x70;
    private static final byte ID_SETUP_READ = 0x71;
    private static final byte ID_READ = 0x72;
    private static final byte ID_ERASE = 0x73;
    private static final byte ID_WRITE = 0x74;
    private static final byte ID_LAUNCH = 0x75;

    public static final short UPDATE_STATUS_CONTINUE = 0x00;
    public static final short UPDATE_STATUS_COMPLETE_SUCCESS = 0x01;
    public static final short UPDATE_STATUS_ENABLE_FAIL = 0x02;
    public static final short UPDATE_STATUS_WRITE_FAIL = 0x03;
    public static final short UPDATE_STATUS_LAUNCH_FAIL = 0x04;

    public static final UUID OTA_SERVICE_UUID = UUID.fromString("0000E010-0000-1000-8000-00805f9b34fb");
    public static final UUID OTA_WRITE_CHARACTER_UUID = UUID.fromString("0000E011-0000-1000-8000-00805f9b34fb");
    public static final UUID OTA_RESULT_CHARACTER_UUID = UUID.fromString("0000E012-0000-1000-8000-00805f9b34fb");

    private boolean data_send_error = false;
    private short fail_packet_num = 0;
    private short[] array_fail_packet;

    // jia add
    private DataInputStream dis = null;
    private FileInputStream fin = null;
    private DataOutputStream dos = null;
    private long offset = 0, pack_id = 0, patch_size = 0;
    private byte read_max_buf = maxOtaDataSize;

    // private long new_offset = 0, new_pack_id = 0, new_patch_size = 0;

    public B20732_OtaServiceBin(BluetoothGatt Service, BluetoothGattCharacteristic Characteristic)
    {
        mGattService = Service;
        mUpdateValueCharact = Characteristic;

        if(mGattService != null)
        {
            mUpdateResultCharact = mGattService.getCharacteristic(OTA_SERVICE_UUID, OTA_RESULT_CHARACTER_UUID);
        }

        array_fail_packet = new short[2000];
        for(short i = 0; i < 2000; i++)
            array_fail_packet[i] = (short) -1;

    }

    /*
     * public Boolean Set_PatchFilePath( String path ) { Boolean result =false;
     *
     * if( path != null ) { burnimg_path = new String( path );
     *
     * if( burnimg_path != null ) { if( OpenImageFile( burnimg_path )) { result
     * = true; } } }
     *
     * return result; }
     */
    public Boolean Set_PatchBinFilePath(String path)
    {
        Boolean result = false;

        if(path != null)
        {
            //burnimg_path = new String(path);
            String SDPATH = Environment.getExternalStorageDirectory().getPath();
            burnimg_path = new String(SDPATH + "//a.bin");
            File file = new File(burnimg_path);

            if(file.exists())
            {


                if(burnimg_path != null)
                {
                    if(OpenBinFile(burnimg_path))
                    {
                        result = true;
                    }

                }
            }
        }

        return result;
    }

    class OtaReport_T
    {
        byte operation_id;
        byte operation_result;
        short len;
        byte[] data;
        byte checksum;

        public OtaReport_T(short length)
        {
            if(length > 0)
                data = new byte[length];

            operation_id = 0;
            operation_result = 0;
            len = length;
            checksum = 0;
        }

        private String ByteArrayToDataString(byte[] data)
        {
            String out = null;

            if(data != null)
                out = Arrays.toString(data);

            return out;
        }

        public byte Checksum()
        {
            int tst_sum = 0;
            int i = 0;

            tst_sum = operation_id + operation_result + (len & 0xFF) + ((len >> 8) & 0xFF);

            if(len > 0)
            {
                for(i = 0; i < len; i++)
                {
                    tst_sum += data[i];
                }
            }

            return (byte) ((~tst_sum + 1) & 0xFF);
        }

        /*
         * ############################################################## #
         * Print a report (the hash returned by RxReport/GetReport)
         * ##############################################################
         */
        public void PrintReport()
        {
            Log.v(TAG, "Device OTAFU Update Result: Operation = " + "Result = " + operation_result + operation_id + ", len = " + len + ",data = " + ByteArrayToDataString(data) + ", checksum = " + checksum + "\n");
        }
    }

    /*
     * ############################################################## # Receive
     * an OTAFU report, decode it into returned hash
     * ##############################################################
     */
    private OtaReport_T RxUpdateReport(byte[] data)
    {
        OtaReport_T report;
        short i = 0;

        short length = (short) (((0x000000FF & ((int) data[3])) << 8) | (0x000000FF & ((int) data[2])));

        report = new OtaReport_T(length);

        report.operation_id = data[0];
        report.operation_result = data[1];
        report.len = length;

        if(length > 0)
        {
            for(i = 0; i < length; i++)
            {
                report.data[i] = data[4 + i];
            }

            // And ensure that the received checksum == computed checksum
            report.checksum = data[length + 4];
            if(report.Checksum() != data[length + 4])
            {
                Log.v(TAG, "Report is inconflict \n");
            }
        }

        report.PrintReport();

        return report;
    }

    private Boolean SendACLData(byte[] data)
    {
        Boolean result = false;

        if((mGattService != null) && (mUpdateValueCharact != null))
        {

            if(mUpdateValueCharact.setValue(data))
            {
                // mUpdateValueCharact.setWriteType(
                // BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE );
                result = mGattService.writeCharacteristic(mUpdateValueCharact);
            }
        }

        return result;
    }

    /*
     * private Boolean OpenImageFile( String FilePath ) { Boolean ret = false;
     *
     * try { File patch_file = new File(FilePath ); if( patch_file != null ) {
     * image_file_reader = new BufferedReader(new FileReader( patch_file )); if(
     * null != image_file_reader ) { ret = true; patch_size = (int)
     * patch_file.length(); }
     *
     * } } catch (Exception e) { e.printStackTrace();
     *
     * return false; }
     *
     * return ret; }
     */
    private Boolean OpenBinFile(String FilePath)
    {
        Boolean ret = false;

        try
        {
            // File patch_file = new File(FilePath );
            Log.i("AAAAA", "OpenBinFile FilePath = " + FilePath);
            fin = new FileInputStream(FilePath);
            dis = new DataInputStream(fin);
            if(fin != null)
            {
                if(dis != null)
                {
                    ret = true;
                    patch_size = fin.getChannel().size();
                    Log.i("AAAAA", "OpenBinFile patch_size = " + patch_size);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();

            return false;
        }

        return ret;
    }

    /*
     * private void CloseImageFile( ) { try { if( image_file_reader != null) {
     * image_file_reader.close(); image_file_reader = null; } } catch (Exception
     * e) { e.printStackTrace(); } }
     */
    private void CloseBinFile()
    {
        try
        {
            if(dis != null)
            {
                dis.close();
                fin.close();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
     * private int ReadImageData( byte[] write_buf, int length ) { int read_len
     * = 0; String read_str; byte[] tmp_buf = new byte[64]; int i,j;
     *
     * if ((image_file_reader != null) && ( write_buf != null )) { try {
     * read_str = image_file_reader.readLine();
     *
     * if(( read_str == null )) { CloseImageFile(); } else { Log.v(TAG,
     * "read patch: " + read_str + "\n"); if( read_str.getBytes().length < 64 )
     * { System.arraycopy(read_str.toLowerCase().getBytes(), 0, tmp_buf, 0,
     * read_str.getBytes().length ); read_len = read_str.getBytes().length;
     *
     * for( i=1,j=0;i < read_len;i+=2, j++) { if( tmp_buf[i] > '9') {
     * write_buf[j] = (byte) ((tmp_buf[i] - 'a' + 0xA)<<4);
     *
     * } else { write_buf[j] = (byte) ((tmp_buf[i] - '0')<<4); }
     *
     * if( tmp_buf[i+1] > '9') { write_buf[j] += tmp_buf[i+1] - 'a' + 0xA;
     *
     * } else write_buf[j] += tmp_buf[i+1] - '0'; }
     *
     * read_len = j; } offset += read_str.getBytes().length; } } catch
     * (Exception e) { e.printStackTrace(); }
     *
     * }
     *
     * return read_len; }
     */
    private int ReadBinData(byte[] write_buf)
    {
        int read_len = 0;
        byte[] tmp_buf = new byte[this.read_max_buf + 4];
        boolean isFinish = false;

        if((dis != null) && (write_buf != null))
        {
            try
            {
                if((patch_size - offset) >= this.read_max_buf)
                {
                    read_len = this.read_max_buf;
                    tmp_buf[3] = (byte) 0x00;
                }
                else if((patch_size - offset) > 0)
                {
                    read_len = (int) (patch_size - offset);
                    tmp_buf[3] = (byte) 0x00;
                    isFinish = true;
                }
                else
                {
                    tmp_buf[3] = (byte) 0x01;
                }
                tmp_buf[0] = (byte) read_len;
                tmp_buf[1] = (byte) 0xff;
                tmp_buf[2] = (byte) 0xff;

                for(int i = 0; i < read_len; i++)
                {
                    tmp_buf[i + 4] = dis.readByte();

                }
                if(isFinish)
                {
                    System.arraycopy(tmp_buf, 0, write_buf, 0, read_len);
                    CloseBinFile();
                }
                else
                {
                    System.arraycopy(tmp_buf, 0, write_buf, 0, this.read_max_buf);
                }
                offset += read_len;
                Log.i("AAAAA", "ReadBinData offset=" + offset);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }

        return read_len + 4;
    }

    /*
     * ############################################################## # Compute
     * and return a checksum
     * ##############################################################
     */
    private byte Checksum(short id, int addr, short len, byte[] data)
    {
        int sum = (int) id;
        short i = 0;

        sum += addr & 0xFF;
        sum += (addr >> 8) & 0xFF;
        sum += (addr >> 16) & 0xFF;
        sum += (addr >> 24) & 0xFF;

        sum += len & 0xFF;
        sum += (len >> 8) & 0xFF;

        if((len > 0) && (data != null))
        {
            for(i = 0; i < len; i++)
            {
                sum += data[i];
            }
        }

        // 1-byte 2's complement of all the bytes
        return (byte) ((~sum + 1) & 0xFF);
    }

    /*
     * ############################################################## # Write
     * memory (addr, bytes)
     * ##############################################################
     */
    private Boolean WriteMem(int addr, byte[] data)
    {
        Boolean result = false;

        if(data == null)
            return result;

        Log.v(TAG, "WriteMem: address :" + addr + "data length :" + data.length);

        result = SetReport(ID_WRITE, addr, (short) data.length, data);

        return result;
    }

    /*
     * ############################################################## # Enable
     * OTAFU (send the magic 0x70 report)
     * ##############################################################
     */
    public Boolean EnableOtafu()
    {
        Boolean result = false;

        result = SetReport(ID_ENABLE_OTAFU, 0xFF00, (short) 0, null); // A
        // launch
        // to
        // 0x00
        // will
        // never
        // return...

        Log.v(TAG, "Enabled OTA result: " + result);

        return result;
    }

    /*
     * ############################################################## # Send a
     * set report # The general format of the BRCM OTA Set-report message is: #
     * X
     * -----------------------X-----------------------------X--------------------
     * -
     * X-------------------X--------------------------X----------X--------------
     * ----X # PACKET_LENGTH(2 bytes) | L2CAP_CHANNEL_ID(2 bytes) | REPORT_ID(1
     * byte) | ADDRESS(4 bytes) | REPORT_LENGTH(2 bytes) | DATA() | CHECK_SUM(1
     * byte)| #
     * X-----------------------X-----------------------------X----------
     * ----------
     * -X-------------------X--------------------------X----------X----
     * --------------X
     * ##############################################################
     */
    private Boolean SetReport(byte repid, int addr, short len, byte[] data)
    {
        byte checksum_value = Checksum(repid, addr, len, data);

        byte[] sreportbytes = new byte[len + 8];

        Boolean result = false;

        sreportbytes[0] = repid;

        sreportbytes[1] = (byte) (addr & 0xFF);
        sreportbytes[2] = (byte) ((addr >> 8) & 0xFF);
        sreportbytes[3] = (byte) ((addr >> 16) & 0xFF);
        sreportbytes[4] = (byte) ((addr >> 24) & 0xFF);

        sreportbytes[5] = (byte) (len & 0xFF);
        sreportbytes[6] = (byte) (len >> 8);

        // Put the report bytes together
        if((len > 0) && (data != null))
        {
            short i = 0;
            for(i = 0; i < len; i++)
            {
                sreportbytes[7 + i] = data[i];
            }
        }

        sreportbytes[len + 7] = checksum_value;

        result = SendACLData(sreportbytes);

        return result;
    }

    /*
     * ############################################################## # Launch
     * ##############################################################
     */
    /*
     * private Boolean Launch( int addr ) { Boolean result =false;
     *
     * Log.v( TAG, "Launch: " + addr ); CloseImageFile( ); result = SetReport(
     * ID_LAUNCH, addr, (short) 0, null); // A launch to 0x00 will never
     * return...
     *
     * return result; }
     */
    private Boolean LaunchBin(int addr)
    {
        Boolean result = false;

        Log.v(TAG, "Launch: " + addr);
        CloseBinFile();
        result = SetReport(ID_LAUNCH, addr, (short) 0, null); // A launch to
        // 0x00 will
        // never
        // return...

        return result;
    }

    /*
     * public Boolean WriteImage( Boolean retry ) { Boolean complete = false;
     * byte[] data = null; byte[] tmp_buffer = new byte[64]; int len = 0; byte
     * pdu_len = 0; short pdu_offset = 0; byte rec_type = 0; Boolean pdu_found =
     * false;
     *
     * if( !retry ) { do { len = ReadImageData( tmp_buffer, 64 ); if( len > 4 )
     * { pdu_len = tmp_buffer[0]; pdu_offset = (short)(((0x000000FF &
     * ((int)tmp_buffer[1]))<<8) | (0x000000FF & ((int)tmp_buffer[2])));
     * rec_type = tmp_buffer[3];
     *
     * if (rec_type == 0x01) // End-of-ffile record { complete = true;//last
     * LINE; offset = patch_size; break; } else if (rec_type == 0x04) //
     * Extended linear address record { Log.v(TAG, "Read Image file header\n");
     * } else if (rec_type == 0x00) // Data record { if( pdu_offset >= 0x04C0 )
     * pdu_found = true; }
     *
     * } else break; } while( !pdu_found ); }
     *
     * if( !complete ) { if( pdu_found ) { int sum = 0; if( pdu_len >
     * maxOtaDataSize ) { pdu_len = maxOtaDataSize; } data = new
     * byte[pdu_len+4]; //System.arraycopy(tmp_buffer., 0, data, 0, pdu_len );
     * //data = Arrays.copyOfRange( tmp_buffer, 4, pdu_len + 4); //WriteMem(
     * eeprom_address + offset, data ); //offset += pdu_len; data[0] = ID_WRITE;
     * data[1] = pdu_len; data[2] = (byte)pack_id; for ( int i=0;i<pdu_len;i++)
     * { data[i+3] = tmp_buffer[i+4]; } for ( int j=0;j<pdu_len+3; j++) sum +=
     * data[j];
     *
     * data[pdu_len+3] = (byte)(~sum+1); SendACLData(data); pack_id++; } }
     *
     * return complete; }
     */
    public Boolean WriteBin(Boolean retry)
    {
        Boolean complete = false;
        byte[] data = null;
        byte[] tmp_buffer = new byte[this.read_max_buf + 4];
        int len = 0;
        byte pdu_len = 0;
        short pdu_offset = 0;
        byte rec_type = 0;
        Boolean pdu_found = false;

        if(!retry)
        {
            do
            {
                len = ReadBinData(tmp_buffer);
                if(len >= 4)
                {
                    pdu_len = tmp_buffer[0];
                    pdu_offset = (short) (((0x000000FF & ((int) tmp_buffer[1])) << 8) | (0x000000FF & ((int) tmp_buffer[2])));
                    rec_type = tmp_buffer[3];

                    if(rec_type == 0x01) // End-of-ffile record
                    {
                        complete = true;// last LINE;
                        offset = patch_size;
                        break;
                    }
                    else if(rec_type == 0x00) // Data record
                    {
                        pdu_found = true;
                    }

                }
                else
                {
                    break;
                }
            }
            while(!pdu_found);
        }

        if(!complete)
        {
            if(pdu_found)
            {
                int sum = 0;
                if(pdu_len > read_max_buf)
                {
                    pdu_len = read_max_buf;
                }
                data = new byte[pdu_len + 4];

                // offset += pdu_len;
                data[0] = ID_WRITE;
                data[1] = pdu_len;
                data[2] = (byte) pack_id;
                for(int i = 0; i < pdu_len; i++)
                {
                    data[i + 3] = tmp_buffer[i + 4];
                }
                for(int j = 0; j < pdu_len + 3; j++)
                {
                    sum += data[j];
                }

                data[pdu_len + 3] = (byte) (~sum + 1);
                SendACLData(data);
                pack_id++;
                Log.i("AAAAA", "ReadBinData pack_id=" + pack_id);
            }
        }
        return complete;
    }

    public Boolean GetLastUpdateResult()
    {
        Boolean result = false;

        if(mUpdateResultCharact != null)
        {
            result = mGattService.readCharacteristic(mUpdateResultCharact);
        }

        return result;
    }

    public short OtaHandleWriteResult(byte[] value, int status)
    {
        short result = UPDATE_STATUS_CONTINUE;
        Boolean write_failed = false;

        // RxUpdateReport( value );

        switch(value[0])
        {
            case ID_ENABLE_OTAFU:
                if(0 != status)
                {
                    result = UPDATE_STATUS_ENABLE_FAIL;
                }
                else
                {
                    // GetLastUpdateResult( );
                    // WriteImage(false);
                    // jia add
                    WriteBin(false);
                }

                break;

            case ID_WRITE:
                if(0 != status)
                {
                    write_failed = true;
                    data_send_error = true;
                }

                if(!data_send_error)
                {

                    // if( WriteImage( write_failed ))
                    // jia add
                    if(WriteBin(write_failed))
                    {
                        // if( Launch(0) )
                        // if( GetLastUpdateResult())
                        // jia add
                        if(LaunchBin(0))
                        {
                            result = UPDATE_STATUS_CONTINUE;
                            // GetLastUpdateResult();
                        }
                    }
                }
                else
                    result = UPDATE_STATUS_LAUNCH_FAIL;

                break;

            case ID_LAUNCH:
                if(0 != status)
                {
                    result = UPDATE_STATUS_LAUNCH_FAIL;
                }
                else
                {
                    result = UPDATE_STATUS_CONTINUE;
                }
                break;

            default:
                break;
        }

        return result;
    }

    public short OtaHandleUpdateResult(byte[] value)
    {
        short result = UPDATE_STATUS_CONTINUE;
        Boolean write_failed = false;
        short length = 0;

        RxUpdateReport(value);

        switch(value[0])
        {
            case ID_ENABLE_OTAFU:
                if(0 == value[1])
                {
                    result = UPDATE_STATUS_ENABLE_FAIL;
                }
                // else
                // WriteImage(false);

                break;

            case ID_WRITE:
                if(0 == value[1])
                {
                    write_failed = true;
                }
                else
                {

                }

                length = (short) (((0x000000FF & ((int) value[6])) << 8) | (0x000000FF & ((int) value[5])));
                Log.v(TAG, "Write length: " + length);

                if((length == last_length) && (length != 0))
                {
                    // Launch(0);
                    // jia add
                    LaunchBin(0);
                }
                else
                {
                    try
                    {
                        last_length = length;
                        Thread.sleep(2000);
                    }
                    catch(Exception e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    GetLastUpdateResult();
                }
                break;

            case ID_LAUNCH:
                if(0 == value[1])
                {
                    result = UPDATE_STATUS_LAUNCH_FAIL;
                }
                else
                {
                    result = UPDATE_STATUS_COMPLETE_SUCCESS;
                }
                break;

            default:
                break;
        }

        return result;
    }

    public byte OtaGetUpdateProgress()
    {
        byte percent = 0;

        if((patch_size > 0) && (offset > 0) && (patch_size >= offset))
        {
            percent = (byte) ((float) offset * 100 / patch_size);
        }

        Log.v(TAG, "Update Progress: " + percent);
        return percent;
    }

}