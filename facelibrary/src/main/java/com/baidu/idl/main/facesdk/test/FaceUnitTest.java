package com.baidu.idl.main.facesdk.test;

import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;

public class FaceUnitTest {

    public FaceUnitTest() {

    }

    public native int nativeTestInterface(int type, BDFaceImageInstance instance,
                                          BDFaceImageInstance instanceDepth,
                                          float[] landmark);

}