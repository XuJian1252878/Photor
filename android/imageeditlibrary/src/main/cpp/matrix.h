//
// Created by 许舰 on 2018/10/2.
//

#ifndef PHOTOR_MATRIX_H
#define PHOTOR_MATRIX_H

void identMatrix(float *matrix);

void saturateMatrix(float matrix[4][4], float* saturation);

void applyMatrix(Bitmap* bitmap, float matrix[4][4]);

void applyMatrixToPixel(unsigned char* red, unsigned char* green, unsigned char* blue, float matrix[4][4]);

#endif //PHOTOR_MATRIX_H
