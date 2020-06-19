package com.baidu.idl.face.main.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
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
import com.zistone.blecontrol.R;
import com.zistone.blecontrol.activity.FaceIdCompareActivity;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.camera.AutoTexturePreviewView;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.ImageUtils;
import com.baidu.idl.face.main.view.FaceRoundView;
import com.baidu.idl.face.main.view.PreviewTexture;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * RGB+IR检测页面（活体+非活体）
 */
public class FaceIRLivenessActivity extends BaseActivity {
    private int source;

    // textureView用于绘制人脸框等。
    private ImageView testImageview;

    // detect-info
    private TextView tipTextView;
    private TextView detectTimeTx;
    private TextView liveTimeTx;
    private TextView liveScoreTx;
    private TextView irLiveTimeTx;
    private TextView irLiveScoreTx;
    private TextView featureTimeTx;

    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PERFER_HEIGH = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    private static final int imageSize = 300;

    // RGB+IR 控件
    private PreviewTexture[] mPreview;
    private Camera[] mCamera;


    // 摄像头个数
    private int mCameraNum;
    // 摄像头采集数据
    private volatile byte[] rgbData;
    private volatile byte[] irData;
    // 判断摄像头数据源
    private int camemra1DataMean;
    private int camemra2DataMean;
    private volatile boolean camemra1IsRgb = false;


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

    private AutoTexturePreviewView mPreviewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_ir_detect);
        initView();
        Intent intent = getIntent();
        if (intent != null) {
            source = intent.getIntExtra("pageType", 0);
        }

    }


    private void initView() {

        // 双目摄像头RGB 图像预览
        // textureView用于显示摄像头数据。
        mPreviewView = findViewById(R.id.detect_ir_image_view);
        mPreviewView.setVisibility(View.VISIBLE);

        // 双目摄像头IR 图像预览
        TextureView irTexture = findViewById(R.id.texture_preview_ir);
        // 调试模式打开 显示IR 图像回显
        if (SingleBaseConfig.getBaseConfig().getNirOrDepth()) {
            irTexture.setVisibility(View.VISIBLE);
        } else {
            irTexture.setVisibility(View.INVISIBLE);
        }
        testImageview = findViewById(R.id.test_rgb_ir_view);

        // 双摄像头
        mCameraNum = Camera.getNumberOfCameras();
        if (mCameraNum < 2) {
            Toast.makeText(this, "未检测到2个摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {
            mPreview = new PreviewTexture[mCameraNum];
            mCamera = new Camera[mCameraNum];
            mPreview[0] = new PreviewTexture(this, mPreviewView.getTextureView());
            mPreview[1] = new PreviewTexture(this, irTexture);
        }

        tipTextView = findViewById(R.id.tip_tv);
        detectTimeTx = findViewById(R.id.detect_time_tx);
        liveTimeTx = findViewById(R.id.live_time_tx);
        liveScoreTx = findViewById(R.id.live_score_tx);
        irLiveTimeTx = findViewById(R.id.binocular_live_time_tx);
        irLiveScoreTx = findViewById(R.id.binocular_score_tx);
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
//            rectView = findViewById(R.id.rect_view_ir_detect);
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
//        // 适配 显示器  预览变成方形
//        if (w > h) {
//            w = h;
//            FrameLayout.LayoutParams cameraFL = new FrameLayout.LayoutParams(
//                    (int) (w * GlobalSet.SURFACE_RATIO), (int) (h * GlobalSet.SURFACE_RATIO),
//                    Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
//            rgbTexture.setLayoutParams(cameraFL);
//        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraNum < 2) {
            Toast.makeText(this, "未检测到2个摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {
            try {
                mCamera[0] = Camera.open(0);
                mCamera[1] = Camera.open(1);
                mPreview[0].setCamera(mCamera[0], PREFER_WIDTH, PERFER_HEIGH);
                mPreview[1].setCamera(mCamera[1], PREFER_WIDTH, PERFER_HEIGH);
                // 摄像头图像预览角度
                int cameraRotation = SingleBaseConfig.getBaseConfig().getVideoDirection();
                mCamera[0].setDisplayOrientation(cameraRotation);
                mCamera[1].setDisplayOrientation(cameraRotation);
                mCamera[0].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        dealRgb(data);
                    }
                });
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        dealIr(data);
                    }
                });
            } catch (RuntimeException e) {
                Log.e("qing", e.getMessage());
            }
        }

    }

    private synchronized void rgbOrIr(int index, byte[] data) {
        byte[] tmp = new byte[PREFER_WIDTH * PERFER_HEIGH];
        try {
            System.arraycopy(data, 0, tmp, 0, PREFER_WIDTH * PERFER_HEIGH);
        } catch (NullPointerException e) {
            Log.e("qing", String.valueOf(e.getStackTrace()));
        }
        int count = 0;
        int total = 0;
        for (int i = 0; i < PREFER_WIDTH * PERFER_HEIGH; i = i + 10) {
            total += byteToInt(tmp[i]);
            count++;
        }

        if (count == 0) {
            return;
        }

        if (index == 0) {
            camemra1DataMean = total / count;
        } else {
            camemra2DataMean = total / count;
        }
        if (camemra1DataMean != 0 && camemra2DataMean != 0) {
            if (camemra1DataMean > camemra2DataMean) {
                camemra1IsRgb = true;
            } else {
                camemra1IsRgb = false;
            }
        }
    }

    public int byteToInt(byte b) {
        // Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    private void choiceRgbOrIrType(int index, byte[] data) {
        // camera1如果为rgb数据，调用dealRgb，否则为Ir数据，调用Ir
        if (index == 0) {
            if (camemra1IsRgb) {
                dealRgb(data);
            } else {
                dealIr(data);
            }
        } else {
            if (camemra1IsRgb) {
                dealIr(data);
            } else {
                dealRgb(data);
            }
        }
    }

    private void dealRgb(byte[] data) {
        rgbData = data;
        checkData();

    }

    private void dealIr(byte[] data) {
        irData = data;
        checkData();
    }

    private synchronized void checkData() {
        if (rgbData != null && irData != null) {

            // 调试模式打开 显示实际送检图片的方向，SDK只检测人脸朝上的图
            boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
            if (isRGBDisplay) {
                showDetectImage(rgbData);
            }
            FaceSDKManager.getInstance().onDetectCheck(rgbData, irData, null,
                    PERFER_HEIGH, PREFER_WIDTH, 3, 1, new FaceDetectCallBack() {
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

                            Log.e("qing", "");

                        }
                    });

            rgbData = null;
            irData = null;
        }
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
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, PERFER_HEIGH,
                PREFER_WIDTH, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                SingleBaseConfig.getBaseConfig().getDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorRGB());
        BDFaceImageInstance imageInstance = rgbInstance.getImage();
        Bitmap bitmap = null;
        try {
            bitmap = BitmapUtils.getInstaceBmp(imageInstance);
        } catch (Exception e) {
            Log.e("qing", String.valueOf(e.getStackTrace()));
        }
        final Bitmap finalBitmap = bitmap;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testImageview.setVisibility(View.VISIBLE);
                testImageview.setImageBitmap(finalBitmap);
            }
        });
        // 流程结束销毁图片，开始下一帧图片检测，否则内存泄露
        rgbInstance.destory();
    }


    @Override
    protected void onPause() {
        if (mCameraNum >= 2) {
            for (int i = 0; i < mCameraNum; i++) {
                if (mCamera[i] != null) {
                    mCamera[i].setPreviewCallback(null);
                    mCamera[i].stopPreview();
                    mPreview[i].release();
                    mCamera[i].release();
                    mCamera[i] = null;
                }
            }
        }
        super.onPause();
    }

    private void checkResult(LivenessModel livenessModel) {

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

        Float rgbLiveScore = livenessModel.getRgbLivenessScore();
        Float irLiveScore = livenessModel.getIrLivenessScore();
        // 展示分数
        displayResult(livenessModel);

        if (rgbLiveScore >= SingleBaseConfig.getBaseConfig().getRgbLiveScore() &&
                irLiveScore >= SingleBaseConfig.getBaseConfig().getNirLiveScore()) { // RGB & NIR 活体同时通过

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
                detectTimeTx.setText("人脸检测耗时：" + String.valueOf(livenessModel.getRgbDetectDuration()));
                liveTimeTx.setText("RGB活体得分：" + String.valueOf(livenessModel.getRgbLivenessScore()));
                liveScoreTx.setText("RGB活体耗时：" + String.valueOf(livenessModel.getRgbLivenessDuration()));
                irLiveTimeTx.setText("IR活体耗时：" + String.valueOf(livenessModel.getIrLivenessDuration()));
                irLiveScoreTx.setText("IR活体得分：" + String.valueOf(livenessModel.getIrLivenessScore()));
                featureTimeTx.setText("特征抽取耗时：" + String.valueOf(livenessModel.getFeatureDuration()));
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

    private void clearTip() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detectTimeTx.setText("");
                liveTimeTx.setText("");
                liveScoreTx.setText("");
                irLiveTimeTx.setText("");
                irLiveScoreTx.setText("");
                featureTimeTx.setText("");
                if (requestToInner) {
                    detectInfoTxt.setText("预览区域内人脸不全");
                }
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
                        mPreviewView, model.getBdFaceImageInstance());
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

        pointXY[0] = livenessModel.getFaceInfo().centerX;
        pointXY[1] = livenessModel.getFaceInfo().centerY;
        pointXY[2] = livenessModel.getFaceInfo().width;
        FaceOnDrawTexturViewUtil.converttPointXY(pointXY, mPreviewView,
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
