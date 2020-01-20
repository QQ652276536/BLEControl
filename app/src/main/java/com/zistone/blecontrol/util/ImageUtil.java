package com.zistone.blecontrol.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

public class ImageUtil
{
    /**
     * byte[]转Bitmap
     *
     * @param byteArray
     * @return
     */
    public static Bitmap ByteArrayToBitmap(byte[] byteArray)
    {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    /**
     * Bitmap转byte[]
     *
     * @param bitmap
     * @return
     */
    public static byte[] BitmapToByteArray(Bitmap bitmap)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 图片缩放
     *
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap ZoomBitmap(Bitmap bitmap, int width, int height)
    {
        int tempWdith = bitmap.getWidth();
        int tempHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / tempWdith);
        float scaleHeight = ((float) height / tempHeight);
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }


}
