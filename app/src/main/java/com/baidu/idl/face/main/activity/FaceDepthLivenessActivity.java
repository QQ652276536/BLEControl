package com.baidu.idl.face.main.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.zistone.blecontrol.R;
import com.zistone.blecontrol.activity.FaceIdCompareActivity;
import com.baidu.idl.face.main.callback.CameraDataCallback;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.camera.AutoTexturePreviewView;
import com.baidu.idl.face.main.camera.CameraPreviewManager;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.ImageUtils;
import com.baidu.idl.face.main.view.FaceRoundView;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.ImageRegistrationMode;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoMode;
import org.openni.VideoStream;
import org.openni.android.OpenNIHelper;
import org.openni.android.OpenNIView;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * RGB+Depth检测页面（活体+非活体）
 */
public class FaceDepthLivenessActivity extends BaseOrbbecActivity implements OpenNIHelper.DeviceOpenListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private int source;
    private static final int DEPTH_NEED_PERMISSION = 33;
    private Context mContext;

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
    private AutoTexturePreviewView rgbTexture;
    // 显示Depth图
    private OpenNIView mDepthGLView;

    // RGB摄像头图像宽和高
    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    // Depth摄像头图像宽和高
    private static final int DEPTH_WIDTH = SingleBaseConfig.getBaseConfig().getDepthWidth();
    private static final int DEPTH_HEIGHT = SingleBaseConfig.getBaseConfig().getDepthHeight();
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


    // 包含适配屏幕后后的人脸的x坐标，y坐标，和width
    private float[] pointXY = new float[3];
    private boolean requestToInner = false;

    // 人脸框绘制
    private TextureView mDrawDetectFaceView;
    private Paint paint;
    private RectF rectF;
    private TextView detectInfoTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_face_depth_detect);

        mContext = this;

        PreferencesUtil.initPrefs(this);
        cameraType = SingleBaseConfig.getBaseConfig().getCameraType();

        initView();
    }

    /**
     * 开启Debug View
     */
    private void initView() {
        // 双目摄像头RGB 图像预览
        rgbTexture = findViewById(R.id.rgb_depth_preview_view);
        rgbTexture.setVisibility(View.VISIBLE);

        // 深度摄像头数据回显
        mDepthGLView = findViewById(R.id.depth_camera_preview_view);
        // 调试模式打开 显示深度摄像头数据回显
        if (SingleBaseConfig.getBaseConfig().getNirOrDepth()) {
            mDepthGLView.setVisibility(View.VISIBLE);
        } else {
            mDepthGLView.setVisibility(View.INVISIBLE);
        }

        testImageview = findViewById(R.id.rgb_depth_test_view);

        // 初始化 深度摄像头
        mOpenNIHelper = new OpenNIHelper(this);
        mOpenNIHelper.requestDeviceOpen(this);

        tipTextView = findViewById(R.id.tip_tv);
        detectTimeTx = findViewById(R.id.detect_time_tx);
        liveTimeTx = findViewById(R.id.live_time_tx);
        liveScoreTx = findViewById(R.id.live_score_tx);
        depthLiveTimeTx = findViewById(R.id.binocular_live_time_tx);
        depthLiveScoreTx = findViewById(R.id.binocular_score_tx);
        featureTimeTx = findViewById(R.id.feature_time_tx);

        detectInfoTxt = findViewById(R.id.detect_info_txt);
        mDrawDetectFaceView = findViewById(R.id.texture_view_draw);
        mDrawDetectFaceView.setOpaque(false);
        // 不需要屏幕自动变黑。
        mDrawDetectFaceView.setKeepScreenOn(true);
        mDrawDetectFaceView.setOpaque(false);
        paint = new Paint();
        rectF = new RectF();
        // 根据配置项展示 显示样式
//        String displayType = SingleBaseConfig.getBaseConfig().getDetectFrame();
//        if ("fixed_area".equals(displayType)) { // 固定区域
//            // 遮罩
//            rectView = findViewById(R.id.rect_view_depth_detect);
//            rectView.setVisibility(View.VISIBLE);
//        }
//
//        DisplayMetrics dm = new DisplayMetrics();
//        Display display = this.getWindowManager().getDefaultDisplay();
//        display.getMetrics(dm);
//        // 显示Size
//        int mDisplayWidth = dm.widthPixels;
//        int mDisplayHeight = dm.heightPixels;
//        int w = mDisplayWidth;
//        int h = mDisplayHeight;
//        // 适配 显示器
//        if (w > h) {
//            w = h;
//            FrameLayout.LayoutParams cameraFL = new FrameLayout.LayoutParams(
//                    (int) (w * GlobalSet.SURFACE_RATIO), (int) (h * GlobalSet.SURFACE_RATIO),
//                    Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
//            rgbTexture.setLayoutParams(cameraFL);
//        }


    }

    /**
     * 在device 启动时候初始化USB 驱动
     *
     * @param device
     */
    private void initUsbDevice(UsbDevice device) {

        List<DeviceInfo> opennilist = OpenNI.enumerateDevices();
        if (opennilist.isEmpty()) {
            Toast.makeText(this, " openni enumerateDevices 0 devices", Toast.LENGTH_LONG).show();
            return;
        }
        this.mDevice = null;
        // Find mDevice ID
        for (int i = 0; i < opennilist.size(); i++) {
            if (opennilist.get(i).getUsbProductId() == device.getProductId()) {
//                if (cameraType == 1 && (device.getProductId() == 1555 || device.getProductId() == 1547
//                        || device.getProductId() == 1550)) { // pro =1 ROS1
//                    Toast.makeText(this, "当前模式跟镜头不匹配", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (cameraType == 4 && device.getProductId() == 1550) { // deeyea
//                    Toast.makeText(this, "当前模式跟镜头不匹配", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                this.mDevice = Device.open();
                break;
            }
        }

        if (this.mDevice == null) {
            Toast.makeText(this, " openni open devices failed: " + device.getDeviceName(),
                    Toast.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    public void onDeviceOpened(UsbDevice device) {
        initUsbDevice(device);
        mDepthStream = VideoStream.create(this.mDevice, SensorType.DEPTH);
        if (mDepthStream != null) {
            List<VideoMode> mVideoModes = mDepthStream.getSensorInfo().getSupportedVideoModes();
            for (VideoMode mode : mVideoModes) {
                int x = mode.getResolutionX();
                int y = mode.getResolutionY();
//                if (cameraType == 3 || cameraType == 4) {
//                    if (x == RGB_WIDTH && y == RGB_HEIGHT && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
//                        mDepthStream.setVideoMode(mode);
//                        this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
//                        break;
//                    }
//                } else if (cameraType == 2) { // Atlas
//                    if (x == DEPTH_HEIGHT && y == DEPTH_WIDTH && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
//                        mDepthStream.setVideoMode(mode);
//                        this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
//                        break;
//                    }
//                } else {
//                    if (x == RGB_WIDTH && y == RGB_HEIGHT && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
//                        mDepthStream.setVideoMode(mode);
//                        break;
//                    }
//                }


                if (cameraType == 2) {
                    if (x == DEPTH_HEIGHT && y == DEPTH_WIDTH && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                        mDepthStream.setVideoMode(mode);
                        this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
                        break;
                    }
                } else {
                    if (x == DEPTH_WIDTH && y == DEPTH_HEIGHT && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                        mDepthStream.setVideoMode(mode);
                        this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
                        break;
                    }
                }

            }
            startThread();
        }
    }

    @Override
    public void onDeviceOpenFailed(String msg) {
        showAlertAndExit("Open Device failed: " + msg);
    }

    @Override
    public void onDeviceNotFound() {

    }

    /**
     * 开启线程接收深度数据
     */
    private void startThread() {
        initOk = true;
        thread = new Thread() {

            @Override
            public void run() {

                List<VideoStream> streams = new ArrayList<VideoStream>();

                streams.add(mDepthStream);
                mDepthStream.start();
                while (!exit) {

                    try {
                        OpenNI.waitForAnyStream(streams, 2000);

                    } catch (TimeoutException e) {
                        e.printStackTrace();
                        continue;
                    }

                    synchronized (sync) {
                        if (mDepthStream != null) {
                            mDepthGLView.update(mDepthStream);
                            VideoFrameRef videoFrameRef = mDepthStream.readFrame();
                            ByteBuffer depthByteBuf = videoFrameRef.getData();
                            if (depthByteBuf != null) {
                                int depthLen = depthByteBuf.remaining();
                                byte[] depthByte = new byte[depthLen];
                                depthByteBuf.get(depthByte);
                                dealDepth(depthByte);
                            }
                            videoFrameRef.release();
                        }
                    }

                }
            }
        };

        thread.start();
    }


    /**
     * 摄像头图像预览
     */
    private void startCameraPreview() {
        // 设置前置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);

        CameraPreviewManager.getInstance().startPreview(this, rgbTexture,
                RGB_WIDTH, RGB_HEIGHT, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] rgbData, Camera camera, int srcWidth, int srcHeight) {
                        // 处理数据
                        dealRgb(rgbData);
                        // 调试模式打开 显示实际送检图片的方向，SDK只检测人脸朝上的图
                        boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
                        if (isRGBDisplay) {
                            showDetectImage(rgbData);
                        }
                    }
                });
    }


    /**
     * 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断。实际应用中可注释掉
     *
     * @param rgb
     */
    private void showDetectImage(byte[] rgb) {
        if (rgb == null) {
            return;
        }
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, RGB_HEIGHT,
                RGB_WIDTH, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                SingleBaseConfig.getBaseConfig().getDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorRGB());
        BDFaceImageInstance imageInstance = rgbInstance.getImage();
        final Bitmap bitmap = BitmapUtils.getInstaceBmp(imageInstance);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testImageview.setVisibility(View.VISIBLE);
                testImageview.setImageBitmap(bitmap);
            }
        });

        // 流程结束销毁图片，开始下一帧图片检测，否则内存泄露
        rgbInstance.destory();

    }


    @Override
    protected void onResume() {
        super.onResume();
        // 摄像头图像预览
        startCameraPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // CameraPreviewManager.getInstance().stopPreview();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.v("gangzi", "onDestroy:");
        super.onDestroy();
        exit = true;
        if (initOk) {
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mDepthStream != null) {
                mDepthStream.stop();
                mDepthStream.destroy();
                mDepthStream = null;
            }
            if (mDevice != null) {
                mDevice.close();
            }
        }
        if (mOpenNIHelper != null) {
            mOpenNIHelper.shutdown();
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == DEPTH_NEED_PERMISSION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "Permission Grant", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
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

    private void dealRgb(byte[] data) {
        rgbData = data;
        checkData();
    }

    private void dealDepth(byte[] data) {
        depthData = data;
        Log.e("qing", "depthData:---" + String.valueOf(depthData));
        checkData();
    }

    private synchronized void checkData() {
        if (rgbData != null && depthData != null) {
            FaceSDKManager.getInstance().onDetectCheck(rgbData, null, depthData, RGB_HEIGHT,
                    RGB_WIDTH, 4, 1, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            if (livenessModel == null || livenessModel.getTrackFaceInfo() == null
                                    || livenessModel.getTrackFaceInfo().length == 0) {

                                clearTip();
                                if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("wireframe")) {
                                    Canvas canvas = mDrawDetectFaceView.lockCanvas();
                                    if (canvas == null) {
                                        mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                                        return;
                                    }
                                    // 清空canvas
                                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                                }
                                return;
                            }

                            // 根据返回结果展示
                            if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("fixedarea")) {
                                // 输出结果
                                isInserLimit(livenessModel);
                                checkResult(livenessModel);
                            }

                            if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("wireframe")) {
                                showFrame(livenessModel);
                                checkResult(livenessModel);
                            }

                        }

                        @Override
                        public void onTip(int code, String msg) {
                            displayTip(msg);
                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                        }
                    });
        }
    }

    private void checkResult(final LivenessModel livenessModel) {
        if (requestToInner) {
            clearTip();
            return;
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    detectInfoTxt.setText("");
                }
            });
        }

        displayTip("检测成功");
        // 展示分数数据
        displayResult(livenessModel);

        Float rgbLiveScore = livenessModel.getRgbLivenessScore();
        Float depthLiveScore = livenessModel.getDepthLivenessScore();

        if (rgbLiveScore >= SingleBaseConfig.getBaseConfig().getRgbLiveScore() &&
                depthLiveScore >= SingleBaseConfig.getBaseConfig().getDepthLiveScore()) {
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
                if (requestToInner) {
                    detectInfoTxt.setText("预览区域内人脸不全");
                }
            }
        });

    }

    private void displayTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (requestToInner) {
                    detectInfoTxt.setText("预览区域内人脸不全");
                } else {
                    detectInfoTxt.setText("");
                }
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
                exit = true;
                CameraPreviewManager.getInstance().stopPreview();
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
                exit = true;
                CameraPreviewManager.getInstance().stopPreview();
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


    /**
     * 绘制人脸框。
     */
    private void showFrame(final LivenessModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mDrawDetectFaceView.lockCanvas();
                if (canvas == null) {
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                if (model == null) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // 检测图片的坐标和显示的坐标不一样，需要转换。
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        rgbTexture, model.getBdFaceImageInstance());
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                // 绘制框
                canvas.drawRect(rectF, paint);
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);

            }
        });
    }

    // 判断人脸是否在园内
    private void isInserLimit(final LivenessModel livenessModel) {
        if (livenessModel == null) {
            requestToInner = false;
            return;
        }
        FaceInfo[] faceInfos = livenessModel.getTrackFaceInfo();
        if (faceInfos == null || faceInfos.length == 0) {
            requestToInner = false;
            return;
        }

        pointXY[0] = livenessModel.getFaceInfo().centerX;
        pointXY[1] = livenessModel.getFaceInfo().centerY;
        pointXY[2] = livenessModel.getFaceInfo().width;
        FaceOnDrawTexturViewUtil.converttPointXY(pointXY, rgbTexture,
                livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);
        float lfetLimitX = AutoTexturePreviewView.circleX - AutoTexturePreviewView.circleRadius;
        float rightLimitX = AutoTexturePreviewView.circleX + AutoTexturePreviewView.circleRadius;
        float topLimitY = AutoTexturePreviewView.circleY - AutoTexturePreviewView.circleRadius;
        float bottomLimitY = AutoTexturePreviewView.circleY + AutoTexturePreviewView.circleRadius;

        if (pointXY[0] - pointXY[2] / 2 < lfetLimitX
                || pointXY[0] + pointXY[2] / 2 > rightLimitX
                || pointXY[1] - pointXY[2] / 2 < topLimitY
                || pointXY[1] + pointXY[2] / 2 > bottomLimitY) {
            requestToInner = true;
        } else {
            requestToInner = false;
        }
    }

}
