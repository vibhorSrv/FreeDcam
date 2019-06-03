//
// Created by troop on 25.10.2016.
//

#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include "../tiff/libtiff/tiffio.h"
#include "libraw.h"
#include "DngProfile.h"
#include "CustomMatrix.h"
#include "DngWriter.h"
#include <string>

#include "../include/stage1_align_merge.h"
#include "../include/HalideBuffer.h"

#define  LOG_TAG    "freedcam.DngStack"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

extern "C"
{
JNIEXPORT void JNICALL
Java_freed_jni_DngStack_startStack(JNIEnv *env, jobject obj, jstring outputfile,
                                   jobjectArray filesToStack,
                                   jint bufsize, jint minoffset, jint maxoffset,
                                   jint l1mindistance, jint l1maxdistance);
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

void writeBinaryFile(uint16_t * data, int size, const char * file)
{
    FILE *filep = fopen(file,"wb");
    fwrite(data, size*2, 1, filep);

    //fwrite(&data, size, 1, filep);
    fclose(filep);
}

uint16_t * readBinaryFile(const char *name, int size)
{
    FILE *file;
    uint16_t *buffer;
    unsigned long fileLen;

    //Open file
    file = fopen(name, "rb");
    if (!file)
    {
        fprintf(stderr, "Unable to open file %s", name);

    }


    buffer= new uint16_t[size];
    if (!buffer)
    {
        fprintf(stderr, "Memory error!");
        fclose(file);

    }

    //Read file contents into buffer
    fread(buffer, size*2, 1, file);
    fclose(file);

    //Do what ever with buffer

    return buffer;
}


JNIEXPORT void JNICALL
Java_freed_jni_DngStack_startStack(JNIEnv *env, jobject obj, jstring outputfile, jobjectArray filesToStack,
                                    jint bufsize, jint minoffset, jint maxoffset,
                                   jint l1mindistance, jint l1maxdistance) {

    int buffsize = bufsize;
    LOGD("Java_freed_jni_DngStack_startStack");
    int stringCount = (*env).GetArrayLength(filesToStack);
    if (stringCount < buffsize)
        buffsize = stringCount;
    int width, height, outputcount;
    const char *files[stringCount];
    const char *outfile = (*env).GetStringUTFChars(outputfile, NULL);
    int stacktostackfilecount = stringCount / buffsize;

    LOGD("FilesToReadCount: %i", stringCount);
    for (int i = 0; i < stringCount; i++) {
        jstring string = (jstring) (*env).GetObjectArrayElement(filesToStack, i);
        files[i] = (*env).GetStringUTFChars(string, NULL);
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
    //raw.imgdata.params.output_tiff = 0;
    raw.imgdata.params.no_interpolation = 1;
    LOGD("opend raw %s, unpack", files[0]);
    if ((ret = raw.open_file(files[0]) != LIBRAW_SUCCESS))
        return;
    LOGD("opend raw, unpack");
    if ((ret = raw.unpack()) != LIBRAW_SUCCESS)
        return;
    LOGD("unpackd, read width and height");
    width = (int) raw.imgdata.sizes.raw_width;
    height = (int) raw.imgdata.sizes.raw_height;
    LOGD("WxH %i x %i, getDngProfile", width, height);
    DngProfile * dngprofile =new DngProfile();
    float* bl = new float[4];
    for (size_t i = 0; i < 4; i++)
    {
        bl[i] = raw.imgdata.color.dng_levels.dng_cblack[6];
    }
    dngprofile->blacklevel = bl;
    dngprofile->whitelevel = raw.imgdata.color.dng_levels.dng_whitelevel[0];
    dngprofile->rawwidht = (int)raw.imgdata.sizes.raw_width;
    dngprofile->rawheight = (int)raw.imgdata.sizes.raw_height;
    dngprofile->rowSize = 0;

    char cfaar[4];
    cfaar[0] = raw.imgdata.idata.cdesc[raw.COLOR(0, 0)];
    cfaar[1] = raw.imgdata.idata.cdesc[raw.COLOR(0, 1)];
    cfaar[2] = raw.imgdata.idata.cdesc[raw.COLOR(1, 0)];
    cfaar[3] = raw.imgdata.idata.cdesc[raw.COLOR(1, 1)];
    std::string cfa = cfaar;
    LOGD("cfa: %s", cfaar);
    if (cfaar[0] == 'B')
    {
        dngprofile->bayerformat = "bggr";
    }
    else if(cfaar[0] == 'R')
    {
        dngprofile->bayerformat = "rggb";
    }
    else if(cfaar[0] == 'G' && cfaar[1] == 'R')
    {
        dngprofile->bayerformat = "grbg";
    }
    else
    {
        dngprofile->bayerformat = "gbrg";
    }

    dngprofile->rawType = 6;
    LOGD("done dngProfile, get Matrix");
    CustomMatrix * matrix = new CustomMatrix();
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
    matrix->neutralColorMatrix[0] = 1 /matrix->neutralColorMatrix[0];
    matrix->neutralColorMatrix[1] = 1 /matrix->neutralColorMatrix[1];
    matrix->neutralColorMatrix[2] = 1 /matrix->neutralColorMatrix[2];
    copyMatrix(matrix->fowardMatrix1, raw.imgdata.color.dng_color[0].forwardmatrix);
    copyMatrix(matrix->fowardMatrix2, raw.imgdata.color.dng_color[1].forwardmatrix);
    copyMatrix(matrix->reductionMatrix1, raw.imgdata.color.dng_color[0].calibration);
    copyMatrix(matrix->reductionMatrix2, raw.imgdata.color.dng_color[1].calibration);
    LOGD("done Matrix, Init halide buffers");
    Halide::Runtime::Buffer<uint16_t> input(width, height, buffsize);
    Halide::Runtime::Buffer<uint16_t> output(width, height, 1);
    Halide::Runtime::Buffer<uint16_t> tmp;

    if (stacktostackfilecount > 1) {
        LOGD("create new input");
        Halide::Runtime::Buffer<uint16_t> tmp2(width, height, stacktostackfilecount);
        tmp = tmp2;
    }

    uint16_t *inputdata = input.data();
    uint16_t *out = output.data();
    uint16_t *tmpdata;
    if (stacktostackfilecount > 1) {
        tmpdata = tmp.data();
    }
    int offsetNextImg = width * height;

    LOGD("data copied");
    raw.recycle();
    int stackedfile = 0;

    //read left dngs and merge them
    for (int i = 0; i < stringCount; i += buffsize) {
        int off = 0;
        for (int t = 0; t < buffsize; ++t) {
            LOGD("open %i", i + t);
            if ((ret = raw.open_file(files[i + t]) != LIBRAW_SUCCESS))
                return;
            if ((ret = raw.unpack()) != LIBRAW_SUCCESS)
                return;
            LOGD("opend %i", i + t);

            for (size_t b = 0; b < offsetNextImg; b++) {
                inputdata[b + off] = raw.imgdata.rawdata.raw_image[b];
            }
            LOGD("input filled", i);
            raw.recycle();
            off += offsetNextImg;
        }

        LOGD("start stack");
        stage1_align_merge(input, minoffset, maxoffset, l1mindistance, l1maxdistance, output);
        LOGD("end stack");
        if (stacktostackfilecount > 1) {

            int noff = stackedfile * offsetNextImg;
            for (int b = 0; b < offsetNextImg; b++) {
                tmpdata[b + noff] = out[b];
            }
            stackedfile++;
        }
    }

    if (stacktostackfilecount > 1) {
        stage1_align_merge(tmp, minoffset, maxoffset, l1mindistance, l1maxdistance, output);
    }

    unsigned char *data1 = (unsigned char *) out;
    unsigned data_size = width * height * 2;

    DngWriter *dngw = new DngWriter();

    dngw->dngProfile = dngprofile;
    dngw->customMatrix = matrix;
    dngw->bayerBytes = data1;
    dngw->rawSize = data_size;
    dngw->fileSavePath = (char *) outfile;
    dngw->_make = "hdr+";
    dngw->_model = "model";

    dngw->WriteDNG();

    LOGD("delete matrix");
    delete matrix;
    LOGD("delete dngprofile");
    delete dngprofile;

    LOGD("delete outfile");
    delete outfile;

}
