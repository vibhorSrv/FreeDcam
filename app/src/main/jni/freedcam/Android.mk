LOCAL_PATH := $(call my-dir)

LOCAL_ARM_MODE := arm

include $(CLEAR_VARS)
LOCAL_MODULE    := libmergstacka
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/mergstacka.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := libfreedcam
LOCAL_SRC_FILES := RawToDng.cpp librawutils.cpp DngWriter.cpp DngStacker.cpp ExifInfo.cpp GpsInfo.cpp DngProfile.cpp CustomMatrix.cpp RawStackPipeNative.cpp
LOCAL_CPPFLAGS := -fexceptions -fopenmp -Wno-c++11-narrowing -frtti -std=c++11 -fpic
LOCAL_CFLAGS += -fopenmp
LOCAL_LDFLAGS += -fopenmp -Wl,-Bsymbolic
LOCAL_LDLIBS := -lz \
	-L $(LOCAL_PATH)/libs \
	-lm \
	-llog \
	-fopenmp \
	-ljnigraphics \
	-landroid
LOCAL_C_INCLUDES += \
					$(NDK_APP_PROJECT_PATH)/tiff/libtiff/ \
                    ../../libjpeg/jpeg-9b/ \

LOCAL_STATIC_LIBRARIES := libtiff libraw libjpeg libmergstacka
include $(BUILD_SHARED_LIBRARY)


APP_OPTIM := debug
LOCAL_CFLAGS := -g 