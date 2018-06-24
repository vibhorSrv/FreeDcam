//
// Created by troop on 21.06.2018.
//

#include "mergstacka.h"
#include "HalideBuffer.h"
#include "DngProfile.h"
#include "CustomMatrix.h"
#include "DngWriter.h"
#include <jni.h>
#include <stdlib.h>
#include <android/log.h>

#ifndef FREEDCAM_RAWSTACKPIPENATIVE_H
#define FREEDCAM_RAWSTACKPIPENATIVE_H

#define  LOG_TAG    "freedcam.RawStackPipeNative"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

class RawStackPipeNative
{
public:
    RawStackPipeNative()
    {}
    Halide::Runtime::Buffer<uint16_t> input;
    Halide::Runtime::Buffer<uint16_t> input_to_merge;
    Halide::Runtime::Buffer<uint16_t> output;
    uint16_t * inputdata, *mergedata, *outdata;
    int widht;
    int height;
    int offset;

    void init(int width, int height, uint16_t * firstdata)
    {
        LOGD("init");
        this->widht = width;
        this->height = height;
        offset = width*height;
        LOGD("init input");
        Halide::Runtime::Buffer<uint16_t> tmp(width, height, 2);
        input = tmp;
        LOGD("init input_to_merge");
        Halide::Runtime::Buffer<uint16_t> tmp2(width, height, 2);
        input_to_merge = tmp2;
        LOGD("init output");
        Halide::Runtime::Buffer<uint16_t> tmp3(width, height, 1);
        output = tmp3;
        inputdata = input.data();
        mergedata = input_to_merge.data();
        outdata = output.data();
        LOGD("copy data");
        for (int i = 0; i < offset; ++i) {
            inputdata[i] = ((firstdata[i])<<2);
            mergedata[i] = ((firstdata[i])<<2);
        }
        LOGD("init done");
        delete[] firstdata;
    }

    void stackFrame(uint16_t * nextdata)
    {
        LOGD("stackframe");
        for (int i = 0; i < offset; ++i) {
            inputdata[i+offset] = ((nextdata[i])<<2);
            mergedata[i+offset] = ((nextdata[i])<<2);
        }
        mergstacka(input,input_to_merge,output);
        for (int i = 0; i < offset; ++i) {
            mergedata[i] = outdata[i];
        }
        delete[] nextdata;
        LOGD("stackframedone");
    }

    void writeDng(DngProfile * profile, CustomMatrix * customMatrix, char* outfile)
    {
        LOGD("write dng");
        DngWriter * writer = new DngWriter();
        writer->customMatrix = customMatrix;
        profile->rawType = 6;
        writer->dngProfile = profile;
        writer->bayerBytes = (unsigned char*) output.data();
        writer->rawSize = widht*height*2;
        writer->_make = "hdr+";
        writer->_model = "model";
        writer->fileSavePath = (char*)outfile;
        writer->WriteDNG();

        delete writer;
        /*
        input_to_merge.deallocate();
        delete[] input_to_merge.data();
        delete input_to_merge;
        input.deallocate();
        delete[] input.data();
        delete input;
        output.deallocate();
        delete[] output.data();
        delete output;
        delete outfile;
        delete customMatrix;
        delete profile;*/
        LOGD("write dng done");
    }
};
#endif //FREEDCAM_RAWSTACKPIPENATIVE_H
