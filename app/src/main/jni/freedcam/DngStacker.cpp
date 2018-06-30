//
// Created by troop on 25.10.2016.
//

#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include "../tiff/libtiff/tiffio.h"
#include "DngTags.h"
#include "libraw.h"
#include "DngProfile.h"
#include "CustomMatrix.h"
#include "DngWriter.h"
#include <string>
#include "mergstacka.h"
#include "HalideBuffer.h"

#define  LOG_TAG    "freedcam.RawToDngNative"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

extern "C"
{
JNIEXPORT void JNICALL Java_freed_jni_DngStack_startStack(JNIEnv *env, jobject thiz, jobjectArray filesToStack, jstring outputfile);
}



//move in pointer values to different mem region that it not get cleared on TIFFClose(tif);
void moveToMem(float * in, float *out, int count)
{
    for (int i = 0; i < count; ++i) {
        out[i] = in[i];
    }
}

void copyMatrix(float* dest, float colormatrix[4][3])
{
    int m = 0;
    for (int i = 0; i < 3; i++)
    {
        for (int t = 0; t < 3; t++)
        {
            dest[m++] = colormatrix[i][t];
        }
    }
}
void copyMatrix(float* dest, float colormatrix[3][4])
{
    int m = 0;
    for (int i = 0; i < 3; i++)
    {
        for (int t = 0; t < 3; t++)
        {
            dest[m++] = colormatrix[i][t];
        }
    }
}

void copyMatrix(float* dest, float colormatrix[4])
{
    int m = 0;
    for (int i = 0; i < 3; i++)
    {
        dest[m++] = colormatrix[i];
    }
}


JNIEXPORT void JNICALL Java_freed_jni_DngStack_startStack(JNIEnv *env, jobject thiz, jobjectArray filesToStack, jstring outputfile)
{



    int stringCount = (*env).GetArrayLength(filesToStack);
    int width,height, outputcount;
    const char * files[stringCount];
    const char * outfile =(*env).GetStringUTFChars( outputfile, NULL);

    LOGD("FilesToReadCount: %i", stringCount);
    for (int i=0; i<stringCount; i++) {
        jstring string = (jstring) (*env).GetObjectArrayElement(filesToStack, i);
        files[i] = (*env).GetStringUTFChars( string, NULL);
    }


    LOGD("init libraw");
    LibRaw raw;
    int ret;
    raw.imgdata.params.no_auto_bright = 0; //-W
    raw.imgdata.params.use_camera_wb = 1;
    raw.imgdata.params.output_bps = 16; // -6
    raw.imgdata.params.output_color = 0;
    //raw.imgdata.params.user_qual = 0;
    //raw.imgdata.params.half_size = 1;
    raw.imgdata.params.no_auto_scale = 0;
    raw.imgdata.params.gamm[0] = 1.0; //-g 1 1
    raw.imgdata.params.gamm[1] = 1.0; //-g 1 1
    raw.imgdata.params.output_tiff = 0;
    raw.imgdata.params.no_interpolation = 1;
    if ((ret = raw.open_file(files[0]) != LIBRAW_SUCCESS))
        return;
    LOGD("open raw");
    if ((ret = raw.unpack()) != LIBRAW_SUCCESS)
        return;
    LOGD("open unpack");
    width = (int)raw.imgdata.sizes.raw_width;
    height = (int)raw.imgdata.sizes.raw_height;
    DngProfile * dngprofile =new DngProfile();
    CustomMatrix * matrix = new CustomMatrix();
    Halide::Runtime::Buffer<uint16_t> input(width, height, 2);
    Halide::Runtime::Buffer<uint16_t> input_to_merge(width, height, 2);

    Halide::Runtime::Buffer<uint16_t> output(width, height, 1);

    uint16_t * inputdata = input.data();
    uint16_t * input_to_mergedata = input_to_merge.data();
    uint16_t * out = output.data();
    int offsetNextImg = width*height;

    for (size_t i = 0; i <  width *  height; i++)
    {
        inputdata[i] = (raw.imgdata.rawdata.raw_image[i]);
        input_to_mergedata[i] = (raw.imgdata.rawdata.raw_image[i]);
    }
    //inputdata += offsetNextImg;

    float* bl = new float[4];
    for (size_t i = 0; i < 4; i++)
    {
        bl[i] = raw.imgdata.color.dng_levels.dng_cblack[6];
    }
    dngprofile->blacklevel = bl;
    dngprofile->whitelevel = raw.imgdata.color.dng_levels.dng_whitelevel[0];
    dngprofile->rawwidht = width;
    dngprofile->rawheight = height;
    dngprofile->rowSize = 0;

    char cfaar[4];
    cfaar[0] = raw.imgdata.idata.cdesc[raw.COLOR(0, 0)];
    cfaar[1] = raw.imgdata.idata.cdesc[raw.COLOR(0, 1)];
    cfaar[2] = raw.imgdata.idata.cdesc[raw.COLOR(1, 0)];
    cfaar[3] = raw.imgdata.idata.cdesc[raw.COLOR(1, 1)];

    std::string cfa = cfaar;
    if (cfa == std::string("BGGR"))
    {
        dngprofile->bayerformat = "bggr";
    }
    else if (cfa == std::string("RGGB"))
    {
        dngprofile->bayerformat = "rggb";
    }
    else if (cfa == std::string("GRBG"))
    {
        dngprofile->bayerformat = "grbg";
    }
    else
    {
        dngprofile->bayerformat = "gbrg";
    }

    dngprofile->rawType = 6;

    matrix->colorMatrix1 = new float[9];
    matrix->colorMatrix2 = new float[9];
    matrix->neutralColorMatrix = new float[3];
    matrix->fowardMatrix1 = new float[9];
    matrix->fowardMatrix2 = new float[9];
    matrix->reductionMatrix1 = new float[9];
    matrix->reductionMatrix2 = new float[9];

    copyMatrix(matrix->colorMatrix1, raw.imgdata.color.dng_color[0].colormatrix);
    copyMatrix(matrix->colorMatrix2, raw.imgdata.color.dng_color[1].colormatrix);
    copyMatrix(matrix->neutralColorMatrix, raw.imgdata.color.cam_mul);
    copyMatrix(matrix->fowardMatrix1, raw.imgdata.color.dng_color[0].forwardmatrix);
    copyMatrix(matrix->fowardMatrix2, raw.imgdata.color.dng_color[1].forwardmatrix);
    copyMatrix(matrix->reductionMatrix1, raw.imgdata.color.dng_color[0].calibration);
    copyMatrix(matrix->reductionMatrix2, raw.imgdata.color.dng_color[1].calibration);

    LOGD("data copied");
    raw.recycle();

    //read left dngs and merge them
    for (int i = 1; i < stringCount; ++i) {

        if ((ret = raw.open_file(files[i]) != LIBRAW_SUCCESS))
            return;
        if ((ret = raw.unpack()) != LIBRAW_SUCCESS)
            return;
        LOGD("open %i", i);
        int off = offsetNextImg;
        for (size_t t = 0; t <  offsetNextImg; t++)
        {
            inputdata[t+ off] = raw.imgdata.rawdata.raw_image[t];
            input_to_mergedata[t+ off] = raw.imgdata.rawdata.raw_image[t];
        }
        raw.recycle();

        LOGD("end copy algin to alignout");
        LOGD("start merge");
        mergstacka(input,input_to_merge,output);
        LOGD("end merge");
        for (size_t t = 0; t <  offsetNextImg; t++)
        {
            input_to_mergedata[t] = out[t];
        }

    }

    unsigned char *data1 = (unsigned char *)out;
    unsigned data_size = width * height * 2;

    DngWriter *dngw = new DngWriter();

    dngw->dngProfile = dngprofile;
    dngw->customMatrix = matrix;
    dngw->bayerBytes = data1;
    dngw->rawSize = data_size;
    dngw->fileSavePath = (char*)outfile;
    dngw->_make = "hdr+";
    dngw->_model = "model";

    dngw->WriteDNG();

    delete matrix;
    delete dngprofile;
    delete[] data1;
    delete outfile;
    delete input;
    delete input_to_merge;
    delete output;


}