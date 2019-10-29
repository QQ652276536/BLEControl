package zistone.com.bluetoothtest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android_serialport_api.SerialPortManager;
import zistone.com.onecheckactivity.R;

public class BluetoothActivity extends AppCompatActivity implements View.OnClickListener
{

    // Used to load the 'native-lib' library on application startup.

    private Button m_btn1;
    private Button m_btn2;
    private TextView m_textView1;
    private TextView m_textView2;
    private SerialPortManager m_serialPortManager;
    private String m_snNumber = "";
    private String m_flagNumber = "";
    private boolean m_isExit = false;

    private Handler m_txtHandler = new Handler()
    {
        // 接收到消息后处理
        public void handleMessage(Message msg)
        {
            Log.d("FALG", m_flagNumber);
            m_textView1.setText(m_flagNumber);
            m_textView1.invalidate();
            super.handleMessage(msg);
        }
    };

    /**
     * 双击返回键退出
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {

        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if(m_isExit)
            {
                this.finish();
                System.exit(0);
            }
            else
            {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                m_isExit = true;
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        m_isExit = false;
                    }
                }, 2000);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        // Example of a call to a native method
        m_btn1 = findViewById(R.id.button1);
        m_btn1.setOnClickListener(this);
        m_btn2 = findViewById(R.id.button2);
        m_btn2.setOnClickListener(this);
        m_textView1 = findViewById(R.id.textView2);
        m_textView2 = findViewById(R.id.textView2);
        m_serialPortManager = new SerialPortManager();
        m_snNumber = m_serialPortManager.GetDeviceSN();
    }

    @Override
    public void onClick(View v)
    {
        int btnId = v.getId();
        if(R.id.button1 == btnId)
        {
        }
        else if(R.id.button2 == btnId)
        {
        }
    }

    /**
     * 对话框
     *
     * @param param
     */
    private void ShowWarning(int param)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("警告");
        switch(param)
        {
            case 1:
                builder.setMessage("该设备未写入SN");
                builder.setPositiveButton("知道了", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                });
                break;
            case 2:
                builder.setMessage("该设备PCBA测试未通过,禁止写入标志位!");
                builder.setNegativeButton("知道了", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                });
                break;
            case 3:
                builder.setMessage("该设备一检测试未通过,禁止写入标志位!");
                builder.setNegativeButton("知道了", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                });
                break;
        }
        builder.show();
    }
}
