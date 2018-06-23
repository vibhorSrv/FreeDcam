//
// Created by troop on 21.06.2018.
//

#include "mergstacka.h"
#include "HalideBuffer.h"
#include <jni.h>
#include <stdlib.h>
#include <android/log.h>

#ifndef FREEDCAM_RAWSTACKPIPENATIVE_H
#define FREEDCAM_RAWSTACKPIPENATIVE_H
class RawStackPipeNative
{
public:
    RawStackPipeNative();
    Halide::Runtime::Buffer<uint16_t> input;
    Halide::Runtime::Buffer<uint16_t> input_to_merge;
    Halide::Runtime::Buffer<uint16_t> output;

    void init(int width, int height)
    {
        Halide::Runtime::Buffer<uint16_t> tmp(width, height, 2);
        input = tmp;
        Halide::Runtime::Buffer<uint16_t> tmp2(width, height, 2);
        input_to_merge = tmp2;
        Halide::Runtime::Buffer<uint16_t> tmp3(width, height, 1);
        output = tmp3;

    }
};
#endif //FREEDCAM_RAWSTACKPIPENATIVE_H
