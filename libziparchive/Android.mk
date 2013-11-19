LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

include external/stlport/libstlport.mk
LOCAL_CPP_EXTENSION := .cc

LOCAL_SRC_FILES := \
	zip_archive.h \
	zip_archive.cc

LOCAL_STATIC_LIBRARIES := libz
LOCAL_MODULE:= libziparchive

LOCAL_C_INCLUDES += external/zlib

include $(BUILD_STATIC_LIBRARY)

