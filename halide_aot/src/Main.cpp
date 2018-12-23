
#include "Halide.h"
#include "hdrplus/align.h"
#include "hdrplus/merge.h"

//#include "finish.h"
using namespace Halide;

Func alignmerge(ImageParam imgs, ImageParam imgs2, Param<int> minoffset, Param<int> maxoffset, Param<int> l1mindistance, Param<int> l1maxdistance)
{
	Func alignment = align(imgs);
	return merge(imgs2, alignment, minoffset, maxoffset,l1mindistance,l1maxdistance);
}

Func align_merge(ImageParam imgs, Param<int> minoffset, Param<int> maxoffset, Param<int> l1mindistance, Param<int> l1maxdistance)
{
	Func alignment = align(imgs);
	return merge(imgs, alignment, minoffset, maxoffset, l1mindistance, l1maxdistance);
}

int main(int argc, char* argv[]) {

    int xOs = 32;
    int i = 0;

    while(argv[i][0] == '-') {

        if(argv[i][1] == 'x') {

            xOs = atof(argv[++i]);
            i++;
            continue;
        }
    }
	
	ImageParam imgs(type_of<uint16_t>(), 3);
	ImageParam alignImgs(type_of<uint16_t>(), 3);
	Param<int> minoffset;
	Param<int> maxoffset;
	Param<int> l1mindistance;
	Param<int> l1maxdistance;
	//Func alignbuf;
	//ImageParam alignbuf(type_of<uint16_t>(), 3);

	Target target;
	target.os = Target::Android; // The operating system
	target.arch = Target::ARM;   // The CPU architecture
	target.bits = xOs;            // The bit-width of the architecture
	std::vector<Target::Feature> arm_features; // A list of features to set
	//arm_features.push_back(Target::LargeBuffers);
	//arm_features.push_back(Target::NoNEON);
	target.set_features(arm_features);

	//Func alignment = align(imgs);
	Func stage1_alignmerge = align_merge(imgs,minoffset, maxoffset,l1mindistance,l1maxdistance);
	std::vector<Argument> argss(5);
	argss[0] = imgs;
	argss[1] = minoffset;
	argss[2] = maxoffset;
	argss[3] = l1mindistance;
	argss[4] = l1maxdistance;


	stage1_alignmerge.compile_to_static_library("stage1_align_merge", {  argss }, "stage1_align_merge", target);

	//target.bits = 32;
	//stage1_alignmerge.compile_to_static_library("../../../libs/armeabi-v7a", {  argss }, "stage1_align_merge", target);

	/*ImageParam input(type_of<uint16_t>(), 2);
	Param<int> blackpoint;
	Param<int> whitepoint;
	Param<float> wb_r;
	Param<float> wb_g1;
	Param<float> wb_g2;
	Param<float> wb_b;
	Param<float> compression;
	Param<float> gain;

	Func stage2_RawToRgb = finish(input, blackpoint, whitepoint, wb_r,wb_g1,wb_g2,wb_b, compression, gain);
	std::vector<Argument> args(9);
	args[0] = input;
	args[1] = blackpoint;
	args[2] = whitepoint;
	args[3] = wb_r;
	args[4] = wb_g1;
	args[5] = wb_g2;
	args[6] = wb_b;
	args[7] = compression;
	args[8] = gain;

	stage2_RawToRgb.compile_to_static_library("stage2_RawToRgb", { args }, "stage2_RawToRgb", target);*/

	return 0;
}

