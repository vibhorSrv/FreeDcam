
#include "JniUtils.h"
#include <stdlib.h>
#include "RawStackPipeNative.h"
#include "HalideBuffer.h"
#include "CustomMatrix.h"
#include "DngProfile.h"

extern "C"
{
    JNIEXPORT jobject JNICALL Java_freed_jni_RawStack_init(JNIEnv *env, jobject thiz) {
        RawStackPipeNative * rawStackPipeNative = new RawStackPipeNative();
        return env->NewDirectByteBuffer(rawStackPipeNative, 0);
    }

    JNIEXPORT void JNICALL Java_freed_jni_RawStack_setBaseFrame(JNIEnv *env, jobject thiz, jobject javaHandler, jbyteArray input, jint width, jint height) {
        RawStackPipeNative * rawStackPipeNative =  (RawStackPipeNative*)env->GetDirectBufferAddress(javaHandler);
        uint16_t * ar = (uint16_t*)copyByteArray(env, input);
        rawStackPipeNative->init(width,height, ar);
    }

    JNIEXPORT void JNICALL Java_freed_jni_RawStack_stackFrame(JNIEnv *env, jobject thiz, jobject javaHandler, jbyteArray input) {
        RawStackPipeNative * rawStackPipeNative =  (RawStackPipeNative*)env->GetDirectBufferAddress(javaHandler);
        uint16_t * ar = (uint16_t*)copyByteArray(env, input);
        rawStackPipeNative->stackFrame(ar);
    }

    JNIEXPORT void JNICALL Java_freed_jni_RawStack_writeDng(JNIEnv *env, jobject thiz, jobject javaHandler, jobject dngprofile, jobject matrix,jstring fileout) {
        RawStackPipeNative * rawStackPipeNative = (RawStackPipeNative*)env->GetDirectBufferAddress(javaHandler);
        DngProfile * profile = (DngProfile*)env->GetDirectBufferAddress(dngprofile);
        CustomMatrix * cmatrix = (CustomMatrix*)env->GetDirectBufferAddress(matrix);
        char * outfile = copyString(env,fileout);
        rawStackPipeNative->writeDng(profile, cmatrix, outfile);

    }

};
