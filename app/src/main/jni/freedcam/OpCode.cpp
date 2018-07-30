//
// Created by troop on 21.07.2018.
//
#include <jni.h>
#include "OpCode.h"
#include "JniUtils.h"
#include <android/log.h>

#define  LOG_TAG    "freedcam.Opcode_native"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

extern "C"
{
    JNIEXPORT jobject JNICALL Java_freed_jni_OpCode_init(JNIEnv *env, jobject thiz) {
        OpCode *writer = new OpCode();
        return env->NewDirectByteBuffer(writer, 0);
    }

    JNIEXPORT void JNICALL Java_freed_jni_OpCode_setOp2(JNIEnv *env, jobject thiz, jobject javaHandler, jbyteArray input)
    {
        OpCode * opcode =  (OpCode*)env->GetDirectBufferAddress(javaHandler);
        opcode->op2 = (unsigned char*)copyByteArrayRegion(env, input);
        opcode->op2Size = env->GetArrayLength(input);
        LOGD("opcode2 size %i", opcode->op2Size);
    }

    JNIEXPORT void JNICALL Java_freed_jni_OpCode_setOp3(JNIEnv *env, jobject thiz, jobject javaHandler, jbyteArray input)
    {
        OpCode * opcode =  (OpCode*)env->GetDirectBufferAddress(javaHandler);
        opcode->op3 = (unsigned char*)copyByteArrayRegion(env, input);
        opcode->op3Size = env->GetArrayLength(input);
        LOGD("opcode3 size %i", opcode->op3Size);
    }

    JNIEXPORT void JNICALL Java_freed_jni_OpCode_clear(JNIEnv *env, jobject thiz, jobject javaHandler)
    {
        OpCode* opcode = (OpCode*)env->GetDirectBufferAddress(javaHandler);
        opcode->clear();
        delete opcode;
    }
}