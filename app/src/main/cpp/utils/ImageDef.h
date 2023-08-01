//
// Created by didi on 2023/5/18.
//

#ifndef OPENGLDEMO_IMAGEDEF_H
#define OPENGLDEMO_IMAGEDEF_H

#include "stdint.h"
#include <malloc.h>
#include "Logutils.h"
#include "string.h"

#define IMAGE_FORMAT_RGBA           0x01
#define IMAGE_FORMAT_NV21           0x02
#define IMAGE_FORMAT_NV12           0x03
#define IMAGE_FORMAT_I420           0x04
#define IMAGE_FORMAT_YUYV           0x05
#define IMAGE_FORMAT_GRAY           0x06
#define IMAGE_FORMAT_I444           0x07
#define IMAGE_FORMAT_P010           0x08



struct NativeImage{
    int width;
    int height;
    int format;
    uint8_t *ppPlane[3];

    NativeImage()
    {
        width = 0;
        height = 0;
        format = 0;
        ppPlane[0] = nullptr;
        ppPlane[1] = nullptr;
        ppPlane[2] = nullptr;
    }

};

class NativeImageUtil{

public:
    static void AllocNativeImage(NativeImage *pImage)
    {
        if (pImage->height == 0 || pImage->width == 0) return;

        switch (pImage->format)
        {
            case IMAGE_FORMAT_RGBA:
            {
//                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height * 4));
//                static_cast  用在 void * 指针 强转成 其他类型指针  或者 基本类型之间的转换
                pImage->ppPlane[0] = static_cast<uint8_t *>( malloc(
                        pImage->width * pImage->height * 4));
            }
                break;

            case IMAGE_FORMAT_YUYV:
            {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height * 2));
            }
                break;
            case IMAGE_FORMAT_NV12:
            case IMAGE_FORMAT_NV21:
            {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height * 1.5));
                pImage->ppPlane[1] = pImage->ppPlane[0] + pImage->width * pImage->height;
            }
                break;
            case IMAGE_FORMAT_I420:
            {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height * 1.5));
                pImage->ppPlane[1] = pImage->ppPlane[0] + pImage->width * pImage->height;
                pImage->ppPlane[2] = pImage->ppPlane[1] + pImage->width * (pImage->height >> 2);
            }
                break;
            case IMAGE_FORMAT_GRAY:
            {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height));
            }
                break;
            case IMAGE_FORMAT_I444:
            {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height * 3));
            }
                break;
            case IMAGE_FORMAT_P010:
            {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height * 3));
                pImage->ppPlane[1] = pImage->ppPlane[0] + pImage->width * pImage->height * 2;
            }
                break;
            default:
                LogutilsI("NativeImageUtil::AllocNativeImage do not support the format. Format = %d", pImage->format);
                break;
        }
    }


    static void FreeNativeImage(NativeImage *nativeImage){
        if(nativeImage == nullptr || nativeImage->ppPlane[0] == nullptr){
            return;
        }
        free(nativeImage->ppPlane[0]);

        nativeImage->ppPlane[0] = nullptr;
        nativeImage->ppPlane[1] = nullptr;
        nativeImage->ppPlane[2] = nullptr;
    }

    static void CopyNativeImage(NativeImage *pSrcImage,NativeImage *pDesImage){

        if(pSrcImage == nullptr || pSrcImage->ppPlane[0] == nullptr ){
            LogutilsE("CopyNativeImage pSrcImage or pSrcImage->ppPlane is null");
            return;
        }

        if(pSrcImage->format != pDesImage->format ||
           pSrcImage->width != pDesImage->width ||
           pSrcImage->height != pDesImage->height ) {
            LogutilsE("CopyNativeImage pSrcImage and  pDesImage parm is not equal ");
            return;
        }

        if( pDesImage->ppPlane [0]== nullptr){
            AllocNativeImage(pDesImage);
        }

        switch (pSrcImage->format)
        {
            case IMAGE_FORMAT_I420:
            case IMAGE_FORMAT_NV21:
            case IMAGE_FORMAT_NV12:
            {
                memcpy(pDesImage->ppPlane[0], pSrcImage->ppPlane[0], pSrcImage->width * pSrcImage->height * 1.5);
            }
                break;
            case IMAGE_FORMAT_YUYV:
            {
                memcpy(pDesImage->ppPlane[0], pSrcImage->ppPlane[0], pSrcImage->width * pSrcImage->height * 2);
            }
                break;
            case IMAGE_FORMAT_RGBA:
            {
                memcpy(pDesImage->ppPlane[0], pSrcImage->ppPlane[0], pSrcImage->width * pSrcImage->height * 4);
            }
                break;
            case IMAGE_FORMAT_GRAY:
            {
                memcpy(pDesImage->ppPlane[0], pSrcImage->ppPlane[0], pSrcImage->width * pSrcImage->height);
            }
                break;
            case IMAGE_FORMAT_P010:
            case IMAGE_FORMAT_I444:
            {
                memcpy(pDesImage->ppPlane[0], pSrcImage->ppPlane[0], pSrcImage->width * pSrcImage->height * 3);
            }
                break;
            default:
            {
                LogutilsI("NativeImageUtil::CopyNativeImage do not support the format. Format = %d", pSrcImage->format);
            }
                break;
        }

    }

};

#endif //OPENGLDEMO_IMAGEDEF_H
