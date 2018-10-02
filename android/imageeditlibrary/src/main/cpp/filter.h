//
// Created by 许舰 on 2018/10/2.
//

#ifndef PHOTOR_FILTER_H
#define PHOTOR_FILTER_H

void applyInstafix(Bitmap* bitmap);
void applyAnselFilter(Bitmap* bitmap);
void applyTestino(Bitmap* bitmap);
void applyXPro(Bitmap* bitmap);
void applyRetro(Bitmap* bitmap);
void applyBlackAndWhiteFilter(Bitmap* bitmap);
void applySepia(Bitmap* bitmap);
void applyCyano(Bitmap* bitmap);
void applyGeorgia(Bitmap* bitmap);
int applySahara(Bitmap* bitmap);
int applyHDR(Bitmap* bitmap);

#endif //PHOTOR_FILTER_H
