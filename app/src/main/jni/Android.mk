#源文件所在的位置,宏函数my-dir是返回当前目录(包含Android.mk文件本身的目录)的路径
LOCAL_PATH := $(call my-dir)

#不会清理LOCAL_PATH变量
include $(CLEAR_VARS)

#OPENCV_INSTALL_MODULES:=off
#OPENCV_LIB_TYPE:=SHARED

#包含OpenCV SDK中OpenCV.mk文件所在路径
include ../../sdk/native/jni/OpenCV.mk
#include E:/Document/opencv-3.4.1-android-sdk/sdk/native/jni/OpenCV.mk

#要构建成模块的文件列表,以空格分开
LOCAL_SRC_FILES  := DetectionBasedTracker_jni.cpp

#指定本地链接库
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl

#模块名称,每个模块的名称必须唯一且不含空格,如果模块名称的开头已是lib,则构建的模块不会附加前缀lib,且添加.so扩展名
LOCAL_MODULE     := detection_based_tracker

#构建动态库
include $(BUILD_SHARED_LIBRARY)