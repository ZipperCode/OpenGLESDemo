//
// Created by Zipper on 2025/7/6.
//

#ifndef OPENGLEDEMO_IMAGE_PROCESS_H
#define OPENGLEDEMO_IMAGE_PROCESS_H
#include "opengles.h"

int imageBlur(GLEnv *env, uint32_t *pixels, int width, int height, int radius);

int storageLineArt(GLEnv *env, uint32_t *pixels, int width, int height);
#endif //OPENGLEDEMO_IMAGE_PROCESS_H
