//
// Created by 许舰 on 2018/10/13.
//

#ifndef PHOTOR_BLUR_H
#define PHOTOR_BLUR_H

int stackBlur(float* radius, unsigned char* srcRed, unsigned char* srcGreen, unsigned char* srcBlue, int* width, int* height,
              unsigned char* dstRed, unsigned char* dstGreen, unsigned char* dstBlue);

#endif //PHOTOR_BLUR_H
