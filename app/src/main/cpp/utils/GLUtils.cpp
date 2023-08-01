//
// Created by didi on 2023/5/19.
//

#include "GLUtils.h"
#include "Logutils.h"


GLint GLUtils::LoadShader(GLenum shadeType, const char *pSource) {
    GLuint shader = 0;
    FUN_BEGIN_TIME(" GLUtils::LoadShader");
    shader = glCreateShader(shadeType);
    if(shader){
        glShaderSource(shader,1,&pSource,NULL);
        glCompileShader(shader);
        GLint compiled = 0;
        glGetShaderiv(shader,GL_COMPILE_STATUS,&compiled);

        if(!compiled){
            GLint errorInfoLength = 0;
            glGetShaderiv(shader,GL_INFO_LOG_LENGTH,&errorInfoLength);

            if(errorInfoLength){
                char* buff = (char *)malloc((size_t)errorInfoLength);
                if(buff){
                    glGetShaderInfoLog(shader,errorInfoLength,NULL,buff);
                    LogutilsI("GLUtils::LoadShader,shader type :%d, but compile error ,%s",shadeType,buff);
                    free(buff);
                }
            }
            glDeleteShader(shader);
            shader = 0;
        }
    }
    FUN_END_TIME("GLUtils::LoadShader");
    return shader;
}


GLint GLUtils::CreateProgram(const char *pVertexShaderSource, const char *pFragShader, GLuint &vertexId,
                       GLuint &fragmentId)
 {

    GLuint program = 0;
    FUN_BEGIN_TIME("GLUtils::CreateProgram");
        vertexId = LoadShader(GL_VERTEX_SHADER,pVertexShaderSource);
        if(!vertexId) return program;
        fragmentId = LoadShader(GL_FRAGMENT_SHADER,pFragShader);
        if(!fragmentId) return program;

        program = glCreateProgram();
        if(program){
            //attach vertex 和 frament 着色器状态
//            glAttachShader(vertexId,program);
            glAttachShader(program,vertexId);
            GlCheckError("glAttachShader");
//            glAttachShader(fragmentId,program);
            glAttachShader(program,fragmentId);
            GlCheckError("glAttachShader");
            glLinkProgram(program);
            GLint lingStatus = GL_FALSE;
            glGetProgramiv(program,GL_LINK_STATUS,&lingStatus);

            //detach vertex 和 frament
            glDetachShader(program, vertexId);
            glDeleteShader(vertexId);
            vertexId = 0;
            glDetachShader(program, fragmentId);
            glDeleteShader(fragmentId);
            fragmentId = 0;

            if(lingStatus != GL_TRUE){
                GLint lingStatusLength = 0;
                glGetProgramiv(program,GL_INFO_LOG_LENGTH,&lingStatusLength);

                char* buffer = (char *)malloc((size_t )lingStatusLength);
                if (buffer)
                {
                    glGetProgramInfoLog(program, lingStatusLength, NULL, buffer);
                    LogutilsI("GLUtils::CreateProgram Could not link program:\n%s\n", buffer);
                    free(buffer);
                }
                glDeleteProgram(program);
                program = 0;
            }
        } else{
            LogutilsI("GLUtils::CreateProgram failed");
        }
    FUN_END_TIME("GLUtils::CreateProgram");
    LogutilsI("GLint GLUtils::CreateProgram end program %d " ,program);
    return program;
}



void GLUtils::GlCheckError(const char *pOpenation) {
    for (GLint error = glGetError(); error; error = glGetError())
    {
        LogutilsI("GLUtils::CheckGLError GL Operation %s() glError (0x%x)\n", pOpenation, error);
    }
}

void GLUtils::DeleteProgram(GLint &program) {
    LogutilsI("GLUtils::DeleteProgram");
    if (program)
    {
        glUseProgram(0);
        glDeleteProgram(program);
        program = 0;
    }
}

GLint GLUtils::CreateProgram(const char *pVertexShaderSource, const char *pFragShader) {
    GLuint vertexShaderHandle, fragShaderHandle;
    return  CreateProgram(pVertexShaderSource,pFragShader,vertexShaderHandle,fragShaderHandle);
}

