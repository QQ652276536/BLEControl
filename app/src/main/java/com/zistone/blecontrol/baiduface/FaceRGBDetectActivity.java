package com.zistone.blecontrol.baiduface;

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
import com.zistone.blecontrol.baiduface.callback.CameraDataCallback;
import com.zistone.blecontrol.baiduface.callback.FaceDetectCallBack;
import com.zistone.blecontrol.baiduface.camera.AutoTexturePreviewView;
import com.zistone.blecontrol.baiduface.camera.CameraPreviewManager;
import com.zistone.blecontrol.baiduface.manager.FaceTrackManager;
import com.zistone.blecontrol.baiduface.model.LivenessModel;
import com.zistone.blecontrol.baiduface.model.SingleBaseConfig;
import com.zistone.blecontrol.baiduface.utils.BitmapUtils;
import com.zistone.blecontrol.baiduface.utils.FaceOnDrawTexturViewUtil;
import com.zistone.blecontrol.baiduface.utils.FileUtils;
import com.zistone.blecontrol.baiduface.utils.ImageUtils;
import com.zistone.blecontrol.baiduface.view.FaceRoundView;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 不检测活体
 */
public class FaceRGBDetectActivity extends BaseActivity {
    private AutoTexturePreviewView mPreviewView;
    private ImageView testImageview;

    private TextView detectTimeTx;
    private TextView liveTimeTx;
    private TextView liveScoreTx;
    private TextView tipTextView;

    // 遮罩
    private FaceRoundView rectView;

    private int source;

    // RGB摄像头图像宽和高
    private static final int mWidth = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int mHeight = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    private static final int imageSize = 300;


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
        setContentView(R.layout.activity_face_detect);

        Intent intent = getIntent();
        if (intent != null) {
            source = intent.getIntExtra("pageType", 0);
        }

        initView();
    }

    private void initView() {


        mPreviewView = findViewById(R.id.auto_rgb_preview_view);
        // textureView用于绘制人脸框等。
        mDrawDetectFaceView = findViewById(R.id.texture_view_draw);
        mDrawDetectFaceView.setOpaque(false);
        // 不需要屏幕自动变黑。
        mDrawDetectFaceView.setKeepScreenOn(true);
        testImageview = findViewById(R.id.test_rgb_view);

        tipTextView = findViewById(R.id.tip_tv);
        detectTimeTx = findViewById(R.id.detect_time_tx);
        liveTimeTx = findViewById(R.id.live_time_tx);
        liveScoreTx = findViewById(R.id.live_score_tx);

        detectInfoTxt = findViewById(R.id.detect_info_txt);
        paint = new Paint();
        rectF = new RectF();

        // 根据配置项展示 显示样式
        //        String displayType = SingleBaseConfig.getBaseConfig().getDetectFrame();
        //        if ("fixedarea".equals(displayType)) { // 固定区域
        //            // 遮罩
        //            rectView = findViewById(R.id.rect_view_rgb);
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
        //            mPreviewView.setLayoutParams(cameraFL);
        //        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        // 摄像头图像预览
        startCameraPreview();
        Log.e("qing", "start camera");
    }

    /**
     * 摄像头图像预览
     */
    private void startCameraPreview() {
        // 设置前置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        //         CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);

        CameraPreviewManager.getInstance().startPreview(this, mPreviewView, mWidth, mHeight, new CameraDataCallback() {
            @Override
            public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                // 拿到相机帧数据
                faceDetect(data, width, height);

                // 调试模式打开 显示实际送检图片的方向，SDK只检测人脸朝上的图
                boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
                if (isRGBDisplay) {
                    showDetectImage(data);
                }
            }
        });
    }

    /**
     * 摄像头数据处理
     *
     * @param data
     * @param width
     * @param height
     */
    private void faceDetect(byte[] data, int width, int height) {

        // 摄像头预览数据进行人脸检测
        int liveType = SingleBaseConfig.getBaseConfig().getType();
        if (Integer.valueOf(liveType) == 1) { // 无活体检测
            FaceTrackManager.getInstance().setAliving(false);
        } else if (Integer.valueOf(liveType) == 2) { // 活体检测
            FaceTrackManager.getInstance().setAliving(true);
        }

        FaceTrackManager.getInstance().faceTrack(data, width, height, new FaceDetectCallBack() {
            @Override
            public void onFaceDetectCallback(LivenessModel livenessModel) {
                if (livenessModel == null || livenessModel.getTrackFaceInfo() == null || livenessModel.getTrackFaceInfo().length == 0) {
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
            public void onTip(int code, final String msg) {
                displayTip(msg);
            }

            @Override
            public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                // 绘制人脸框
                Log.e("qing", "");
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
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, mHeight, mWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21, SingleBaseConfig.getBaseConfig().getDetectDirection(), SingleBaseConfig.getBaseConfig().getMirrorRGB());
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
    protected void onDestroy() {
        super.onDestroy();
        CameraPreviewManager.getInstance().stopPreview();
    }

    // 检测结果输出
    private void checkResult(LivenessModel model) {

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

        int liveType = SingleBaseConfig.getBaseConfig().getType();
        // 无活体
        switch (Integer.valueOf(liveType)) {
            case 1: {

                Log.e("shangtest", requestToInner + "");
                displayResult(model, null);
                // 采集，保存图片
                saveFaceAndFinish(model.getBdFaceImageInstance());
            }
            break;
            case 2: { // RGB活体检测
                // 展示分数
                displayResult(model, "livess");
                boolean livenessSuccess = false;
                float rgbLiveThreshold = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
                livenessSuccess = (model.getRgbLivenessScore() > rgbLiveThreshold) ? true : false;
                if (livenessSuccess) {
                    // 采集图片，需要返回
                    saveFaceAndFinish(model.getBdFaceImageInstance());

                } else {
                    Toast.makeText(this, "活体验证失败", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                break;

        }

    }

    private void displayResult(final LivenessModel livenessModel, final String livess) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livess != null && livess.equals("livess")) {
                    Log.e("qing", "检测耗时：" + String.valueOf(livenessModel.getRgbDetectDuration()));
                    liveTimeTx.setVisibility(View.VISIBLE);
                    liveScoreTx.setVisibility(View.VISIBLE);
                    liveTimeTx.setText("RGB活体得分：" + livenessModel.getRgbLivenessScore());
                    liveScoreTx.setText("RGB活体耗时：" + livenessModel.getRgbLivenessDuration());
                } else {
                    Log.e("qing", "test");
                    liveTimeTx.setVisibility(View.GONE);
                    liveScoreTx.setVisibility(View.GONE);
                }
                detectTimeTx.setText("人脸检测耗时：" + livenessModel.getRgbDetectDuration());
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
                if (requestToInner) {
                    detectInfoTxt.setText("预览区域内人脸不全");
                }
            }
        });

    }


    /**
     * 保存图片
     *
     * @param instance
     */
    private void saveFaceAndFinish(BDFaceImageInstance instance) {

        final Bitmap bitmap = BitmapUtils.getInstaceBmp(instance);
        if (source == FaceIdCompareActivity.SOURCE_DETECT) {
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
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF, mPreviewView, model.getBdFaceImageInstance());
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
        FaceOnDrawTexturViewUtil.converttPointXY(pointXY, mPreviewView, livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);
        float lfetLimitX = AutoTexturePreviewView.circleX - AutoTexturePreviewView.circleRadius;
        float rightLimitX = AutoTexturePreviewView.circleX + AutoTexturePreviewView.circleRadius;
        float topLimitY = AutoTexturePreviewView.circleY - AutoTexturePreviewView.circleRadius;
        float bottomLimitY = AutoTexturePreviewView.circleY + AutoTexturePreviewView.circleRadius;

        if (pointXY[0] - pointXY[2] / 2 < lfetLimitX || pointXY[0] + pointXY[2] / 2 > rightLimitX || pointXY[1] - pointXY[2] / 2 < topLimitY || pointXY[1] + pointXY[2] / 2 > bottomLimitY) {
            requestToInner = true;
        } else {
            requestToInner = false;
        }

    }

}
