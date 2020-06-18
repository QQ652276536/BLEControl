package com.baidu.idl.main.facesdk.model;

import android.graphics.Bitmap;

public class BDFaceImageInstance {

    private long index = 0;

    public int height;                              // 图片高度
    public int width;                              // 图片宽度
    public byte[] data;
    public BDFaceSDKCommon.BDFaceImageType imageType;

    public BDFaceImageInstance(byte[] data, int height, int width, int imageType) {
        this.height = height;
        this.width = width;
        this.data = data;
        this.imageType = BDFaceSDKCommon.BDFaceImageType.values()[imageType];
    }

    public BDFaceImageInstance(byte[] data, int height, int width,
                               BDFaceSDKCommon.BDFaceImageType imageType, float angle, int isMbyteArrayror) {
        if (data != null && height > 0 && width > 0) {
            create(data, height, width, imageType.ordinal(), angle, isMbyteArrayror);
        }
    }

    public BDFaceImageInstance(Bitmap bitmap) {
        if (bitmap != null) {
            int[] rgbaData = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(rgbaData, 0, bitmap.getWidth(),
                    0, 0, bitmap.getWidth(), bitmap.getHeight());
            createInt(rgbaData, bitmap.getHeight(), bitmap.getWidth(),
                    BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_BGRA.ordinal(), 0, 0);
        }
    }

    public native BDFaceImageInstance getImage();

    public native int destory();

    private native int create(byte[] img, int rows, int cols, int type, float angle, int isMbyteArrayror);

    private native int createInt(int[] img, int rows, int cols, int type, float angle, int isMbyteArrayror);

}
