//
// Created by didi on 2023/6/14.
//

#ifndef OPENGLDEMO_SHADERTOPTEST_H
#define OPENGLDEMO_SHADERTOPTEST_H

#include "GLSampleBase.h"
#include <sys/time.h>
#include <detail/type_mat4x4.hpp>
#include "glm.hpp"


class ShaderTopTest : public GLSampleBase{

public:
    ShaderTopTest();
    virtual ~ShaderTopTest();

    //虚函数可以不实现，也可以实现，为的是动态绑定时，调用实现类的该方法
    virtual void Init();
    virtual  void Draw(int screenWidth,int screenHeight) ;
    virtual void Destroy() ;
    virtual  void UpdateTransformMatrix(float rotateX,float rotateY,float scaleX,float scaleY);
    virtual void UpdateMVPMatrix(glm::mat4 &mvpMatrix ,int angleX ,int angleY,float ration);
private:
    char * vertexArray;
    GLuint mMvpLocation;
    GLuint mVaoId;
    GLuint mVboId[3];
    long long mCurrentTime;
    glm::mat4 mMvpMatrix;
};


#endif //OPENGLDEMO_SHADERTOPTEST_H
