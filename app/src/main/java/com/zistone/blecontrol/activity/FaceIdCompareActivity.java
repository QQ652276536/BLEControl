package com.zistone.blecontrol.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.zistone.blecontrol.R;
import com.zistone.blecontrol.baiduface.BaseActivity;
import com.zistone.blecontrol.baiduface.FaceDepthLivenessActivity;
import com.zistone.blecontrol.baiduface.FaceIRLivenessActivity;
import com.zistone.blecontrol.baiduface.FaceRGBDetectActivity;
import com.zistone.blecontrol.baiduface.PicoFaceDepthLivenessActivity;
import com.zistone.blecontrol.baiduface.manager.FaceSDKManager;
import com.zistone.blecontrol.baiduface.model.SingleBaseConfig;
import com.zistone.blecontrol.baiduface.utils.ImageUtils;

import java.io.FileNotFoundException;

public class FaceIdCompareActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "FaceIdCompareActivity";
    private static final int PICK_PHOTO_FRIST = 100;
    private static final int PICK_VIDEO_FRIST = 101;
    private static final int PICK_PHOTO_SECOND = 102;
    private static final int PICK_VIDEO_SECOND = 103;

    private ImageView imgFirst, imgSecond;
    private TextView tvState, tvScore, tvCurrentThreshold, modelType;
    private byte[] firstFeature = new byte[512];
    private byte[] secondFeature = new byte[512];
    private volatile boolean firstFeatureFinished = false;
    private volatile boolean secondFeatureFinished = false;
    public static final int SOURCE_DETECT = 0; // 采集
    public static final int SOURCE_IR_DETECT = 0; // 采集


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_compare);
        initView();
    }

    private void initView() {
        Button btFirstPickFromPhoto = findViewById(R.id.bt_first_pick_from_photo);
        Button btFirstPickFromVideo = findViewById(R.id.bt_first_pick_from_video);
        Button btSecondPickFromPhoto = findViewById(R.id.bt_second_pick_from_photo);
        Button btSecondPickFromVideo = findViewById(R.id.bt_second_pick_from_video);
        Button btStartCompare = findViewById(R.id.bt_start_compare);
        Button btSetting = findViewById(R.id.btn_setting);
        Button backBtn = findViewById(R.id.btn_back);
        imgFirst = findViewById(R.id.img_first);
        imgSecond = findViewById(R.id.img_second);
        tvScore = findViewById(R.id.tv_score);
        tvState = findViewById(R.id.tv_state);
        modelType = findViewById(R.id.tv_model_type);
        tvCurrentThreshold = findViewById(R.id.tv_current_threshold);
        btFirstPickFromPhoto.setOnClickListener(this);
        btSecondPickFromPhoto.setOnClickListener(this);
        btFirstPickFromVideo.setOnClickListener(this);
        btSecondPickFromVideo.setOnClickListener(this);
        btStartCompare.setOnClickListener(this);
        btSetting.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        PreferencesUtil.initPrefs(this);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.bt_first_pick_from_photo:
                // 从相册取图片
                firstFeatureFinished = false;
                clear();
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_PHOTO_FRIST);
                break;
            case R.id.bt_first_pick_from_video:
                // 从视频流取图片
                firstFeatureFinished = false;
                clear();
                startToActivity(PICK_VIDEO_FRIST);
                break;
            case R.id.bt_second_pick_from_photo:
                // 从相册取图片
                secondFeatureFinished = false;
                clear();
                Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent1, PICK_PHOTO_SECOND);
                break;
            case R.id.bt_second_pick_from_video:
                // 从视频流取图片
                secondFeatureFinished = false;
                clear();
                startToActivity(PICK_VIDEO_SECOND);
                break;
            case R.id.bt_start_compare:
                match();
                break;
            case R.id.btn_setting:
                break;
            case R.id.btn_back:
                FaceIdCompareActivity.this.finish();
                break;
            default:
                break;
        }
    }

    private void startToActivity(int pickVideoCode) {

        int type = SingleBaseConfig.getBaseConfig().getType();
        switch (Integer.valueOf(type)) {
            case 1: { // 非活体
                Toast.makeText(this, "当前活体策略：无活体", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(FaceIdCompareActivity.this, FaceRGBDetectActivity.class);
                intent1.putExtra("pageType", SOURCE_DETECT);
                startActivityForResult(intent1, pickVideoCode);
                break;
            }
            case 2: { //  RGB活体
                Toast.makeText(this, "当前活体策略：RGB活体", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(FaceIdCompareActivity.this, FaceRGBDetectActivity.class);
                intent1.putExtra("pageType", SOURCE_DETECT);
                startActivityForResult(intent1, pickVideoCode);
                break;
            }
            case 3: { // IR活体
                Toast.makeText(this, "当前活体策略：RGB+IR活体", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(FaceIdCompareActivity.this, FaceIRLivenessActivity.class);
                intent1.putExtra("pageType", SOURCE_IR_DETECT);
                startActivityForResult(intent1, pickVideoCode);
                break;
            }
            case 4: { // 深度活体
                Toast.makeText(this, "当前活体策略：RGB+Depth活体，需要使用带深度的摄像头", Toast.LENGTH_SHORT).show();
                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
                switch (cameraType) {
                    case 1: { // pro
                        Intent proIntent = new Intent(FaceIdCompareActivity.this, FaceDepthLivenessActivity.class);
                        startActivityForResult(proIntent, pickVideoCode);
                        break;
                    }
                    case 2: { // atlas
                        Intent proIntent = new Intent(FaceIdCompareActivity.this, FaceDepthLivenessActivity.class);
                        startActivityForResult(proIntent, pickVideoCode);
                        break;
                    }
                    case 6: { // Pico
                        Intent proIntent = new Intent(FaceIdCompareActivity.this, PicoFaceDepthLivenessActivity.class);
                        startActivityForResult(proIntent, pickVideoCode);
                        break;
                    }
                    default:
                        Intent proIntent = new Intent(FaceIdCompareActivity.this, FaceDepthLivenessActivity.class);
                        startActivityForResult(proIntent, pickVideoCode);
                        break;
                }
            }
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_FRIST && (data != null && data.getData() != null)) {
            Uri uri1 = ImageUtils.geturi(data, this);
            try {
                final Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri1));
                if (bitmap != null) {
                    imgFirst.setImageBitmap(bitmap);
                    // 提取特征值
                    syncFeature(bitmap, firstFeature, 1, true);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_PHOTO_SECOND && (data != null && data.getData() != null)) {
            Uri uri2 = ImageUtils.geturi(data, this);
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri2));
                if (bitmap != null) {
                    imgSecond.setImageBitmap(bitmap);
                    // 提取特征值
                    syncFeature(bitmap, secondFeature, 2, true);

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_VIDEO_FRIST && (data != null)) {
            String faceImagePath = data.getStringExtra("file_path");
            Bitmap bitmap = BitmapFactory.decodeFile(faceImagePath);
            if (bitmap != null) {
                imgFirst.setImageBitmap(bitmap);
                // 提取特征值
                syncFeature(bitmap, firstFeature, 1, false);
            }
        } else if (requestCode == PICK_VIDEO_SECOND && (data != null)) {
            String faceImagePath = data.getStringExtra("file_path");
            Bitmap bitmap = BitmapFactory.decodeFile(faceImagePath);
            if (bitmap != null) {
                imgSecond.setImageBitmap(bitmap);
                // 提取特征值
                syncFeature(bitmap, secondFeature, 2, false);
            }
        }
    }

    /**
     * bitmap -提取特征值
     *
     * @param bitmap
     * @param feature
     * @param index
     */
    private void syncFeature(final Bitmap bitmap, final byte[] feature, final int index, boolean isFromPhotoLibrary) {
        float ret = -1;
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(bitmap);

        FaceInfo[] faceInfos = null;
        int count = 10;
        // 现在人脸检测加入了防止多线程重入判定，假如之前线程人脸检测未完成，本次人脸检测有可能失败，需要多试几次
        while (count != 0) {
            faceInfos = FaceSDKManager.getInstance().getFaceDetect().detect(BDFaceSDKCommon.DetectType.DETECT_VIS, rgbInstance);
            count--;
            if (faceInfos != null) {
                break;
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // 检测结果判断
        if (faceInfos != null && faceInfos.length > 0) {

            // 判断质量检测，针对模糊度、遮挡、角度
            if (qualityCheck(faceInfos[0], isFromPhotoLibrary)) {
                ret = FaceSDKManager.getInstance().getFaceFeature().feature(BDFaceSDKCommon.FeatureType.
                        BDFACE_FEATURE_TYPE_ID_PHOTO, rgbInstance, faceInfos[0].landmarks, feature);
                Log.i("qing", "ret:" + ret);
                if (ret == 128 && index == 1) {
                    firstFeatureFinished = true;
                } else if (ret == 128 && index == 2) {
                    secondFeatureFinished = true;
                }
                if (ret == 128) {
                    toast("图片" + index + "特征抽取成功");
                } else if (ret == -100) {
                    toast("未完成人脸比对，可能原因，图片1为空");
                } else if (ret == -101) {
                    toast("未完成人脸比对，可能原因，图片2为空");
                } else if (ret == -102) {
                    toast("未完成人脸比对，可能原因，图片1未检测到人脸");
                } else if (ret == -103) {
                    toast("未完成人脸比对，可能原因，图片2未检测到人脸");
                } else {
                    toast("未完成人脸比对，可能原因，" + "人脸太小（小于sdk初始化设置的最小检测人脸）" + "人脸不是朝上，sdk不能检测出人脸");
                }
            }

        } else {
            toast("未检测到人脸,可能原因人脸太小");
        }

    }


    /**
     * argb - 特征比对
     */

    private void match() {

        if (!firstFeatureFinished) {
            toast("图片一特征抽取失败");
            return;
        }

        if (!secondFeatureFinished) {
            toast("图片二特征抽取失败");
            return;
        }

        int idFeatureValue = SingleBaseConfig.getBaseConfig().getThreshold();
        float score = 0;
        //  比较两个人脸
        score = FaceSDKManager.getInstance().getFaceFeature().featureCompare(BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO, firstFeature, secondFeature, true);
        Log.e("qing", "分数：" + String.valueOf(score));
        if (score > idFeatureValue) {
            tvState.setTextColor(getResources().getColor(R.color.material_green));
            tvState.setText("核验通过");
        } else {
            tvState.setTextColor(getResources().getColor(R.color.read));
            tvState.setText("核验不通过");
        }

        tvScore.setText("相似度：" + score);
        modelType.setText("当前特征抽取模型:" + "证件照模型");
        tvCurrentThreshold.setText("相似度阈值：" + idFeatureValue);

    }

    private void toast(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FaceIdCompareActivity.this, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clear() {
        tvState.setText("");
        tvCurrentThreshold.setText(" ");
        tvScore.setText(" ");
        modelType.setText("");
    }


    /**
     * 质量检测
     * FaceInfo faceInfo
     *
     * @return
     */
    public boolean qualityCheck(final FaceInfo faceInfo, boolean isFromPhotoLibrary) {
        // 不是相册选的图片，不必再次进行质量检测，因为采集图片的时候已经做过了
        if (!isFromPhotoLibrary) {
            return true;
        }

        if (!SingleBaseConfig.getBaseConfig().isQualityControl()) {
            return true;
        }

        if (faceInfo != null) {

            // 模糊结果过滤
            float blur = faceInfo.bluriness;
            if (blur > SingleBaseConfig.getBaseConfig().getBlur()) {
                toast("图片模糊");
                return false;
            }

            // 光照结果过滤
            float illum = faceInfo.illum;
            if (illum < SingleBaseConfig.getBaseConfig().getIllumination()) {
                toast("图片光照不通过");
                return false;
            }

            // 遮挡结果过滤
            if (faceInfo.occlusion != null) {
                BDFaceOcclusion occlusion = faceInfo.occlusion;

                if (occlusion.leftEye > SingleBaseConfig.getBaseConfig().getLeftEye()) {
                    // 左眼遮挡置信度
                    toast("左眼遮挡");
                } else if (occlusion.rightEye > SingleBaseConfig.getBaseConfig().getRightEye()) {
                    // 右眼遮挡置信度
                    toast("右眼遮挡");
                } else if (occlusion.nose > SingleBaseConfig.getBaseConfig().getNose()) {
                    // 鼻子遮挡置信度
                    toast("鼻子遮挡");
                } else if (occlusion.mouth > SingleBaseConfig.getBaseConfig().getMouth()) {
                    // 嘴巴遮挡置信度
                    toast("嘴巴遮挡");
                } else if (occlusion.leftCheek > SingleBaseConfig.getBaseConfig().getLeftCheek()) {
                    // 左脸遮挡置信度
                    toast("左脸遮挡");
                } else if (occlusion.rightCheek > SingleBaseConfig.getBaseConfig().getRightCheek()) {
                    // 右脸遮挡置信度
                    toast("右脸遮挡");
                } else if (occlusion.chin > SingleBaseConfig.getBaseConfig().getChinContour()) {
                    // 下巴遮挡置信度
                    toast("下巴遮挡");
                } else {
                    return true;
                }
            }
        }
        return false;
    }


}
