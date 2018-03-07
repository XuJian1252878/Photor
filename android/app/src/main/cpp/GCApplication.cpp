//
// Created by 许舰 on 2018/3/6.
//

#include "GCApplication.h"

GCApplication::GCApplication(){

}
GCApplication::~GCApplication(){

}
void GCApplication::reset()
{
    if( !mask.empty() )
        mask.setTo(Scalar::all(GC_BGD));
    bgdPxls.clear(); fgdPxls.clear();
    prBgdPxls.clear();  prFgdPxls.clear();

    isInitialized = false;
    rectState = NOT_SET;
    lblsState = NOT_SET;
    prLblsState = NOT_SET;
    iterCount = 0;
}

void GCApplication::setImageAndShowId(Mat *_image, jmethodID _showId )
{
    if( _image->empty())
        return;
    image = _image;
    showId = _showId;
    mask.create( image->size(), CV_8UC1);
    reset();
}

void GCApplication::showImage(JNIEnv *env, jobject instance) const
{
    if( image->empty() )
        return;

    Mat res;
    Mat binMask;
    if( !isInitialized )
        image->copyTo( res );
    else
    {
        getBinMask( mask, binMask );
        image->copyTo( res, binMask );
    }

    vector<Point>::const_iterator it;
    for( it = bgdPxls.begin(); it != bgdPxls.end(); ++it )
        circle( res, *it, radius, BLUE, thickness );
    for( it = fgdPxls.begin(); it != fgdPxls.end(); ++it )
        circle( res, *it, radius, RED, thickness );
    for( it = prBgdPxls.begin(); it != prBgdPxls.end(); ++it )
        circle( res, *it, radius, LIGHTBLUE, thickness );
    for( it = prFgdPxls.begin(); it != prFgdPxls.end(); ++it )
        circle( res, *it, radius, PINK, thickness );

    if( rectState == IN_PROCESS || rectState == SET )
        rectangle( res, Point( rect.x, rect.y ), Point(rect.x + rect.width, rect.y + rect.height ), GREEN, 2);
    unsigned long long img = (unsigned long long) &res;
    env->CallVoidMethod(instance,showId,img);
}

void GCApplication::setRectInMask()
{
    CV_Assert( !mask.empty() );
    mask.setTo( GC_BGD );
    rect.x = max(0, rect.x);
    rect.y = max(0, rect.y);
    rect.width = min(rect.width, image->cols-rect.x);
    rect.height = min(rect.height, image->rows-rect.y);
    (mask(rect)).setTo( Scalar(GC_PR_FGD) );
}

void GCApplication::setLblsInMask( int flags, Point p, bool isPr )
{
    vector<Point> *bpxls, *fpxls;
    uchar bvalue, fvalue;
    if( !isPr )
    {
        bpxls = &bgdPxls;
        fpxls = &fgdPxls;
        bvalue = GC_BGD;
        fvalue = GC_FGD;
    }
    else
    {
        bpxls = &prBgdPxls;
        fpxls = &prFgdPxls;
        bvalue = GC_PR_BGD;
        fvalue = GC_PR_FGD;
    }
    if( flags == 2 )
    {
        bpxls->push_back(p);
        circle( mask, p, radius, bvalue, thickness );
    }
    if( flags  == 1)
    {
        fpxls->push_back(p);
        circle( mask, p, radius, fvalue, thickness );
    }
}
// event:DOWN = 0,UP = 1,MOVE = 2
void GCApplication::mouseClick( int event, int x, int y, int flags ,JNIEnv *env, jobject instance)
{
    // TODO add bad args check
    switch(event){
        case 0: {
            if (flags == 0 && rectState == NOT_SET) {
                rectState = IN_PROCESS;
                rect = Rect(x, y, 1, 1);
            }
            if ( flags == 1 && rectState == SET )
                lblsState = IN_PROCESS;
            if ( flags == 2 && rectState == SET )
                prLblsState = IN_PROCESS;
        }
            break;
        case 1:{
            if(flags == 0 || flags == 1){
                if( rectState == IN_PROCESS )
                {
                    rect = Rect( Point(rect.x, rect.y), Point(x,y) );
                    rectState = SET;
                    setRectInMask();
                    CV_Assert( bgdPxls.empty() && fgdPxls.empty() && prBgdPxls.empty() && prFgdPxls.empty() );
                    showImage(env,instance);
                }
                if( lblsState == IN_PROCESS )
                {
                    setLblsInMask(flags, Point(x,y),false);
                    lblsState = SET;
                    showImage(env,instance);
                }
            }
            if(flags == 2 && prLblsState == IN_PROCESS ){
                setLblsInMask(flags, Point(x,y),false);
                prLblsState = SET;
                showImage(env,instance);
            }
        }
            break;
        case 2:{
            if( rectState == IN_PROCESS )
            {
                rect = Rect( Point(rect.x, rect.y), Point(x,y) );
                CV_Assert( bgdPxls.empty() && fgdPxls.empty() && prBgdPxls.empty() && prFgdPxls.empty() );
                showImage(env,instance);
            }
            else if( lblsState == IN_PROCESS )
            {
                setLblsInMask(flags, Point(x,y),false);
                showImage(env,instance);
            }
            else if( prLblsState == IN_PROCESS )
            {
                setLblsInMask(flags, Point(x,y),false);
                showImage(env,instance);
            }
        }
            break;
    }

}

int GCApplication::nextIter()
{
    if( isInitialized )
        grabCut( *image, mask, rect, bgdModel, fgdModel, 1 );
    else
    {
        if( rectState != SET )
            return iterCount;

        if( lblsState == SET || prLblsState == SET )
            grabCut( *image, mask, rect, bgdModel, fgdModel, 1, GC_INIT_WITH_MASK );
        else
            grabCut( *image, mask, rect, bgdModel, fgdModel, 1, GC_INIT_WITH_RECT );

        isInitialized = true;
    }
    iterCount++;

    bgdPxls.clear(); fgdPxls.clear();
    prBgdPxls.clear(); prFgdPxls.clear();
    return iterCount;
};