package com.baidu.idl.face.main.utils;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.TextureView;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.face.main.camera.AutoTexturePreviewView;
import com.baidu.idl.face.main.camera.PicoAutoTexturePreviewView;

public class FaceOnDrawTexturViewUtil {


    private FaceOnDrawTexturViewUtil() {
    }

    /**
     * 通过中心点坐标（x，y） 和 width ，绘制Rect
     *
     * @param faceInfo
     * @return
     */
    public static Rect getFaceRectTwo(FaceInfo faceInfo) {
        Rect rect = new Rect();
        rect.top = (int) ((faceInfo.centerY - faceInfo.width / 2));
        rect.left = (int) ((faceInfo.centerX - faceInfo.width / 2));
        rect.right = (int) ((faceInfo.centerX + faceInfo.width / 2));
        rect.bottom = (int) ((faceInfo.centerY + faceInfo.width / 2));
        return rect;
    }

    public static void mapFromOriginalRect(RectF rectF, AutoTexturePreviewView autoTexturePreviewView, BDFaceImageInstance imageFrame) {
        // 获取屏幕的宽
        int selfWidth = autoTexturePreviewView.getPreviewWidth();
        // 获取屏幕的高
        int selfHeight = autoTexturePreviewView.getPreviewHeight();
        // 新建矩阵对象
        Matrix matrix = new Matrix();
        // 当屏幕宽度/图像宽度>屏幕高度/图像高度时
        if (selfWidth * imageFrame.height > selfHeight * imageFrame.width) {
            // 将高度按照宽度比进行缩放
            int targetHeight = imageFrame.height * selfWidth / imageFrame.width;
            // 计算平移距离
            int delta = (targetHeight - selfHeight) / 2;
            // 计算宽度比
            float ratio = 1.0f * selfWidth / imageFrame.width;
            // 设置矩阵变换缩放比
            matrix.postScale(ratio, ratio);
            // 设置变换矩阵的平移距离
            matrix.postTranslate(0, -delta);
        } else {
            // 将宽度按照高度比进行缩放
            int targetWith = imageFrame.width * selfHeight / imageFrame.height;
            // 计算平移距离
            int delta = (targetWith - selfWidth) / 2;
            // 计算宽度比
            float ratio = 1.0f * selfHeight / imageFrame.height;
            // 设置矩阵变换缩放比
            matrix.postScale(ratio, ratio);
            // 设置变换矩阵的平移距离
            matrix.postTranslate(-delta, 0);
        }
        // 对人脸框数据进行矩阵变换
        matrix.mapRect(rectF);

    }

    public static void mapFromOriginalRect(RectF rectF, TextureView textureView, BDFaceImageInstance imageFrame) {
        int selfWidth = textureView.getWidth();
        int selfHeight = textureView.getHeight();
        Matrix matrix = new Matrix();
        if (selfWidth * imageFrame.height > selfHeight * imageFrame.width) {
            int targetHeight = imageFrame.height * selfWidth / imageFrame.width;
            int delta = (targetHeight - selfHeight) / 2;
            float ratio = 1.0f * selfWidth / imageFrame.width;
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(0, -delta);
        } else {
            int targetWith = imageFrame.width * selfHeight / imageFrame.height;
            int delta = (targetWith - selfWidth) / 2;
            float ratio = 1.0f * selfHeight / imageFrame.height;
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(-delta, 0);
        }
        matrix.mapRect(rectF);

    }

    public static void mapFromOriginalRect(RectF rectF, PicoAutoTexturePreviewView textureView, BDFaceImageInstance imageFrame) {
        int selfWidth = textureView.getWidth();
        int selfHeight = textureView.getHeight();
        Matrix matrix = new Matrix();
        if (selfWidth * imageFrame.height > selfHeight * imageFrame.width) {
            int targetHeight = imageFrame.height * selfWidth / imageFrame.width;
            int delta = (targetHeight - selfHeight) / 2;
            float ratio = 1.0f * selfWidth / imageFrame.width;
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(0, -delta);
        } else {
            int targetWith = imageFrame.width * selfHeight / imageFrame.height;
            int delta = (targetWith - selfWidth) / 2;
            float ratio = 1.0f * selfHeight / imageFrame.height;
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(-delta, 0);
        }
        matrix.mapRect(rectF);

    }


    public static void converttPointXY(float[] pointXY, AutoTexturePreviewView textureView, BDFaceImageInstance imageFrame, float width) {
        int selfWidth = textureView.getWidth();
        int selfHeight = textureView.getHeight();
        if (selfWidth * imageFrame.height > selfHeight * imageFrame.width) {
            int targetHeight = imageFrame.height * selfWidth / imageFrame.width;
            int delta = (targetHeight - selfHeight) / 2;
            float ratio = 1.0f * selfWidth / imageFrame.width;
            pointXY[0] = pointXY[0] * ratio;
            pointXY[1] = pointXY[1] * ratio;
            pointXY[1] = pointXY[1] - delta;
            pointXY[2] = width * ratio;
        } else {
            int targetWith = imageFrame.width * selfHeight / imageFrame.height;
            int delta = (targetWith - selfWidth) / 2;
            float ratio = 1.0f * selfHeight / imageFrame.height;
            pointXY[0] = pointXY[0] * ratio;
            pointXY[1] = pointXY[1] * ratio;
            pointXY[0] = pointXY[0] - delta;
            pointXY[2] = width * ratio;
        }
    }
}
