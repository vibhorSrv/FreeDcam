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
class RawStackPipeNative
{
public:
    RawStackPipeNative()
    {}
    Halide::Runtime::Buffer<uint16_t> input;
    Halide::Runtime::Buffer<uint16_t> input_to_merge;
    Halide::Runtime::Buffer<uint16_t> output;
    int widht;
    int height;
    int offset;

    void init(int width, int height, uint16_t * firstdata)
    {
        this->widht = width;
        this->height = height;
        offset = width*height;
        Halide::Runtime::Buffer<uint16_t> tmp(width, height, 2);
        input = tmp;
        Halide::Runtime::Buffer<uint16_t> tmp2(width, height, 2);
        input_to_merge = tmp2;
        Halide::Runtime::Buffer<uint16_t> tmp3(width, height, 1);
        output = tmp3;
        uint16_t * inputdata = (uint16_t*) input.data();
        uint16_t * mergedata = (uint16_t*)input_to_merge.data();
        for (int i = 0; i < offset; ++i) {
            inputdata[i] = firstdata[i];
            mergedata[i] = firstdata[i];
        }

    }

    void stackFrame(uint16_t * nextdata)
    {
        uint16_t * inputdata =(uint16_t*) input.data();
        uint16_t * mergedata =(uint16_t*) input_to_merge.data();
        uint16_t * outdata =(uint16_t*) output.data();
        for (int i = 0; i < offset; ++i) {
            inputdata[i+offset] = nextdata[i];
            mergedata[i+offset] = nextdata[i];
        }
        mergstacka(input,input_to_merge,output);
        for (int i = 0; i < offset; ++i) {
            mergedata[i] = outdata[i];
        }
    }

    void writeDng(DngProfile * profile, CustomMatrix * customMatrix, char* outfile)
    {
        DngWriter * writer = new DngWriter();
        writer->customMatrix = customMatrix;
        writer->dngProfile = profile;
        writer->bayerBytes = (unsigned char*) output.data();
        writer->rawSize = widht*height*2;
        writer->_make = "hdr+";
        writer->_model = "model";
        writer->fileSavePath = (char*)outfile;
        writer->WriteDNG();
    }
};
#endif //FREEDCAM_RAWSTACKPIPENATIVE_H
