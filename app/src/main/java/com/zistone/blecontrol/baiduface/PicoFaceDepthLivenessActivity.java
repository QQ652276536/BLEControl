package com.zistone.blecontrol.baiduface;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.picozense.sdk.IFrameCallback;
import com.picozense.sdk.PsCamera;
import com.picozense.sdk.PsFrame;
import com.zistone.blecontrol.R;
import com.zistone.blecontrol.activity.FaceIdCompareActivity;
import com.zistone.blecontrol.baiduface.callback.FaceDetectCallBack;
import com.zistone.blecontrol.baiduface.camera.PicoAutoTexturePreviewView;
import com.zistone.blecontrol.baiduface.manager.FaceSDKManager;
import com.zistone.blecontrol.baiduface.model.GlobalSet;
import com.zistone.blecontrol.baiduface.model.LivenessModel;
import com.zistone.blecontrol.baiduface.model.SingleBaseConfig;
import com.zistone.blecontrol.baiduface.utils.BitmapUtils;
import com.zistone.blecontrol.baiduface.utils.FileUtils;
import com.zistone.blecontrol.baiduface.utils.ImageUtils;
import com.zistone.blecontrol.baiduface.view.FaceRoundView;
import com.zistone.blecontrol.baiduface.view.PicoRenderer;

import org.openni.Device;
import org.openni.VideoStream;
import org.openni.android.OpenNIHelper;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * RGB+Depth检测页面（活体+非活体）
 */
public class PicoFaceDepthLivenessActivity extends BaseActivity {
    private static final String TAG = "PicoLivenessActivity";
    private int source;
    private static final int DEPTH_NEED_PERMISSION = 33;
    private Context mContext;

    private RelativeLayout relativeLayout;
    // 测试imageView
    private ImageView testImageview;
    // detect-info
    private TextView tipTextView;
    private TextView detectTimeTx;
    private TextView liveTimeTx;
    private TextView liveScoreTx;
    private TextView depthLiveTimeTx;
    private TextView depthLiveScoreTx;
    private TextView featureTimeTx;

    // 显示RGB 预览
    private PicoAutoTexturePreviewView mAutoCameraPreviewView;
    // 显示Depth图
    private GLSurfaceView mDepthGLView;
    private PicoRenderer depthRender;

    // RGB摄像头图像宽和高
    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    private static final int imageSize = 300;

    // 设备初始化状态标记
    private boolean initOk = false;
    // 摄像头驱动
    private Device mDevice;
    private Thread thread;
    private OpenNIHelper mOpenNIHelper;
    private VideoStream mDepthStream;

    private Object sync = new Object();
    // 循环取深度图像数据
    private boolean exit = false;

    // 当前摄像头类型
    private int cameraType;

    // 摄像头采集数据
    private volatile byte[] rgbData;
    private volatile byte[] depthData;

    // 遮罩
    private FaceRoundView rectView;

    private PsCamera picoCamera;
    private final Object mySync = new Object();
    private Bitmap mBmpRGB;
    private Bitmap mBmpDepth;
    byte[] mByteBuffer_depth = {0};
    byte[] mByteBuffer_rgb = {0};
    int countNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_face_depth_detect_pico);

        mContext = this;

        PreferencesUtil.initPrefs(this);
        cameraType = SingleBaseConfig.getBaseConfig().getCameraType();

        initView();

        mBmpRGB = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
        mBmpDepth = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
        mByteBuffer_rgb = new byte[640 * 480 * 3];
        mByteBuffer_depth = new byte[640 * 480 * 2];
    }

    /**
     * 开启Debug View
     */
    private void initView() {
        // 双目摄像头RGB 图像预览
        mAutoCameraPreviewView = findViewById(R.id.rgb_depth_preview_view);
        mAutoCameraPreviewView.setVisibility(View.VISIBLE);
        // 深度摄像头数据回显
        depthRender = new PicoRenderer();
        mDepthGLView = findViewById(R.id.depth_camera_preview_view);
        mDepthGLView.setEGLContextClientVersion(2);
        mDepthGLView.setZOrderOnTop(true);
        mDepthGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mDepthGLView.setRenderer(depthRender);
        mDepthGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // 调试模式打开 显示深度摄像头数据回显
        if (SingleBaseConfig.getBaseConfig().getNirOrDepth()) {
            mDepthGLView.setVisibility(View.VISIBLE);
        } else {
            mDepthGLView.setVisibility(View.INVISIBLE);
        }
        testImageview = findViewById(R.id.rgb_depth_test_view);
        // 初始化 深度摄像头
        tipTextView = findViewById(R.id.tip_tv);
        detectTimeTx = findViewById(R.id.detect_time_tx);
        liveTimeTx = findViewById(R.id.live_time_tx);
        liveScoreTx = findViewById(R.id.live_score_tx);
        depthLiveTimeTx = findViewById(R.id.binocular_live_time_tx);
        depthLiveScoreTx = findViewById(R.id.binocular_score_tx);
        featureTimeTx = findViewById(R.id.feature_time_tx);

        // 根据配置项展示 显示样式
        String displayType = SingleBaseConfig.getBaseConfig().getDetectFrame();
        if ("fixed_area".equals(displayType)) { // 固定区域
            // 遮罩
            rectView = findViewById(R.id.rect_view_depth_detect);
            rectView.setVisibility(View.VISIBLE);
        }

        mAutoCameraPreviewView.setPreviewSize(RGB_WIDTH, RGB_HEIGHT);
        DisplayMetrics dm = new DisplayMetrics();
        Display display = this.getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        // 显示Size
        int mDisplayWidth = dm.widthPixels;
        int mDisplayHeight = dm.heightPixels;
        int w = mDisplayWidth;
        int h = mDisplayHeight;
        // 适配 显示器
        if (w > h) {
            w = h;
            FrameLayout.LayoutParams cameraFL = new FrameLayout.LayoutParams((int) (w * GlobalSet.SURFACE_RATIO), (int) (h * GlobalSet.SURFACE_RATIO), Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            mAutoCameraPreviewView.setLayoutParams(cameraFL);
        }

        picoCamera = new PsCamera();
        if (picoCamera != null) {
            picoCamera.init(this, null);
        }
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        if (picoCamera != null) {
            picoCamera.setFrameCallback(depthIFrameCallback);
            if (SingleBaseConfig.getBaseConfig().getMirrorRGB() == 1) {
                Log.i(TAG, "mirror is open");
                picoCamera.setImageMirror(0);
            } else {
                picoCamera.setImageMirror(1);
            }
            picoCamera.setWorkMode(1);
            picoCamera.start(this);
        }
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        synchronized (mySync) {
            if (picoCamera != null) {
                picoCamera.stop();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (picoCamera != null) {
            picoCamera.destroy();
            picoCamera = null;
        }
        super.onDestroy();
    }

    /**
     * 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断。实际应用中可注释掉
     *
     * @param
     */
    private void showDetectImage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBmpRGB != null) {
                    testImageview.setVisibility(View.VISIBLE);
                    testImageview.setImageBitmap(mBmpRGB);
                }
            }
        });
    }


    private void showAlertAndExit(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }


    private synchronized void checkData(byte[] depth, byte[] rgb) {
        if (depth != null && rgb != null) {
            FaceSDKManager.getInstance().onDetectCheck(rgb, null, depth, RGB_HEIGHT, RGB_WIDTH, 4, 1, new FaceDetectCallBack() {
                @Override
                public void onFaceDetectCallback(LivenessModel livenessModel) {
                    checkResult(livenessModel);
                }

                @Override
                public void onTip(int code, String msg) {
                    if (msg != null && !msg.isEmpty()) {
                        displayTip(msg);
                    }
                }

                @Override
                public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                    //                            showFrame(livenessModel);
                }
            });
        }
    }

    private void checkResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        if (livenessModel == null) {
            clearTip();
            return;
        }
        displayTip("检测成功");
        // 展示分数数据
        displayResult(livenessModel);
        Float rgbLiveScore = livenessModel.getRgbLivenessScore();
        Float depthLiveScore = livenessModel.getDepthLivenessScore();

        if (rgbLiveScore >= SingleBaseConfig.getBaseConfig().getRgbLiveScore() && depthLiveScore >= SingleBaseConfig.getBaseConfig().getDepthLiveScore()) {
            // RGB & Depth 活体同时通过
            // 保存并返回
            saveFaceAndFinish(livenessModel);
        } else {
            displayTip("活体检测失败");
        }
    }


    private void displayResult(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("qing", "检测耗时：" + String.valueOf(livenessModel.getRgbDetectDuration()));
                detectTimeTx.setText("人脸检测耗时：" + String.valueOf(livenessModel.getRgbDetectDuration()));
                liveTimeTx.setText("RGB活体得分：" + String.valueOf(livenessModel.getRgbLivenessScore()));
                liveScoreTx.setText("RGB活体耗时：" + String.valueOf(livenessModel.getRgbLivenessDuration()));
                depthLiveTimeTx.setText("depth活体耗时：" + String.valueOf(livenessModel.getDepthtLivenessDuration()));
                depthLiveScoreTx.setText("depth活体得分：" + String.valueOf(livenessModel.getDepthLivenessScore()));
                featureTimeTx.setText("特征抽取耗时：" + String.valueOf(livenessModel.getFeatureDuration()));
            }
        });
    }

    private void clearTip() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detectTimeTx.setText("");
                liveTimeTx.setText("");
                liveScoreTx.setText("");
                depthLiveTimeTx.setText("");
                depthLiveScoreTx.setText("");
                featureTimeTx.setText("");
            }
        });
    }

    private void displayTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tipTextView.setText(tip);
            }
        });
    }


    /**
     * 保存图片
     *
     * @param model
     */
    private void saveFaceAndFinish(LivenessModel model) {
        BDFaceImageInstance image = model.getBdFaceImageInstance();
        Bitmap bitmap = BitmapUtils.getInstaceBmp(image);

        if (source == FaceIdCompareActivity.SOURCE_IR_DETECT) {

            // 注册来源保存到注册人脸目录
            File faceDir = FileUtils.getBatchImportSuccessDirectory();
            if (faceDir == null) {
                Toast.makeText(this, "注册人脸目录未找到", Toast.LENGTH_SHORT).show();

            } else {
                String imageName = UUID.randomUUID().toString();
                File file = new File(faceDir, imageName);
                // 压缩、保存人脸图片至300 * 300，减少网络传输时间
                ImageUtils.resize(bitmap, file, imageSize, imageSize);
                Intent intent = new Intent();
                intent.putExtra("file_path", file.getAbsolutePath());
                setResult(AppCompatActivity.RESULT_OK, intent);
                finish();
            }
        } else {
            try {
                // 其他来源保存到临时目录
                final File file = File.createTempFile(UUID.randomUUID().toString() + "", ".jpg");
                // 人脸识别不需要整张图片。可以对人脸区别进行裁剪。减少流量消耗和，网络传输占用的时间消耗。
                ImageUtils.resize(bitmap, file, imageSize, imageSize);
                Intent intent = new Intent();
                intent.putExtra("file_path", file.getAbsolutePath());
                setResult(AppCompatActivity.RESULT_OK, intent);
                finish();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (0 == msg.what) {
                mAutoCameraPreviewView.draw(mBmpRGB);

                boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
                if (isRGBDisplay) {
                    testImageview.setVisibility(View.VISIBLE);
                    testImageview.setImageBitmap(mBmpRGB);
                }
            }
        }
    };

    // 预览帧回调
    public IFrameCallback depthIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(PsFrame depthFrame, PsFrame irFrame, PsFrame rgbFrame) {
            if (countNum < 3) {
                countNum++;
                return;
            }

            if (rgbFrame != null) {
                rgbFrame.frameData.rewind();
                rgbFrame.frameData.get(mByteBuffer_rgb);
                picoCamera.RgbToRgba_bf(rgbFrame.frameData, mBmpRGB, rgbFrame.width, rgbFrame.height);

                Message fwMsg = mHandler.obtainMessage();
                fwMsg.what = 0;
                mHandler.sendMessage(fwMsg);

            }
            if (depthFrame != null) {
                Log.i(TAG, "onFrame: depth " + depthFrame.width + ", " + depthFrame.height);
                depthFrame.frameData.rewind();
                depthFrame.frameData.get(mByteBuffer_depth);
                depthData = mByteBuffer_depth;
                picoCamera.Y16ToRgba_bf(depthFrame.frameData, mBmpDepth, depthFrame.width, depthFrame.height, 1500);
                depthRender.setBuf(mBmpDepth);
                mDepthGLView.requestRender();
            }
            checkData(mByteBuffer_depth, mByteBuffer_rgb);
        }
    };
}
