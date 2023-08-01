//
// Created by didi on 2023/5/24.
//

#ifndef OPENGLDEMO_GLTRANSITION_H
#define OPENGLDEMO_GLTRANSITION_H

#include "GLSampleBase.h"
#include "detail/type_mat.hpp"
#include "detail/type_mat4x4.hpp"

#define BF_IMG_NUM    6

#define MATH_PI 3.1415926535897932384626433832802
#define BF_LOOP_COUNT 200


class Gltransition :public  GLSampleBase {
public:
    Gltransition();
    virtual ~Gltransition();
    virtual void LoadImage(NativeImage *pImage);
    virtual void LoadMultiImageWithIndex(int index, NativeImage *pImage);

    virtual void Init();
    virtual void Draw(int screenW, int screenH);

    virtual void Destroy();

    virtual void UpdateTransformMatrix(float rotateX, float rotateY, float scaleX, float scaleY);
    virtual void UpdateMVPMatrix(glm::mat4 &mvpMatrix ,int angleX ,int angleY,float ration);

private:
    GLuint m_TextureIds[BF_IMG_NUM];
    GLint mMvpLocation;
    GLuint mVaoId;
    GLuint mVboIds[3];

    NativeImage nativeImage[BF_IMG_NUM];
    glm::mat4 mMvpMatrix;

    int mAngleX = 0;
    int mAngleY = 0;

    float mScaleX = 0;
    float mScaleY = 0;

    int mFrameIndex = 0;
    int mLoopCount = 0;
};


#endif //OPENGLDEMO_GLTRANSITION_H
