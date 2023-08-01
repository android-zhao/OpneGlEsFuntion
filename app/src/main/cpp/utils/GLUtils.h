//
// Created by didi on 2023/5/18.
//

#ifndef OPENGLDEMO_GLUTILS_H
#define OPENGLDEMO_GLUTILS_H

#include "string"
#include "GLES3/gl3.h"
#include "glm.hpp"

class GLUtils{
    public:
    static GLint LoadShader(GLenum typy ,const char* pSource);
    static GLint CreateProgram(const char * pVertexShaderSource,const char* pFragShader,
                              GLuint  &vertexId,GLuint &fragmentId);
    static GLint CreateProgram(const char * pVertexShaderSource,const char* pFragShader);

    static void GlCheckError(const char * pOpenation);

    static void DeleteProgram(GLint &program);

    static void setBool(GLuint programId, const std::string &name, bool value) {
        glUniform1i(glGetUniformLocation(programId, name.c_str()), (int) value);
    }

    static void setInt(GLuint programId, const std::string &name, int value) {
        glUniform1i(glGetUniformLocation(programId, name.c_str()), value);
    }

    static void setFloat(GLuint programId, const std::string &name, float value) {
        glUniform1f(glGetUniformLocation(programId, name.c_str()), value);
    }

    static void setVec2(GLuint programId, const std::string &name, const glm::vec2 &value) {
        glUniform2fv(glGetUniformLocation(programId, name.c_str()), 1, &value[0]);
    }

    static void setVec2(GLuint programId, const std::string &name, float x, float y) {
        glUniform2f(glGetUniformLocation(programId, name.c_str()), x, y);
    }

    static void setVec3(GLuint programId, const std::string &name, const glm::vec3 &value) {
        glUniform3fv(glGetUniformLocation(programId, name.c_str()), 1, &value[0]);
    }

    static void setVec3(GLuint programId, const std::string &name, float x, float y, float z) {
        glUniform3f(glGetUniformLocation(programId, name.c_str()), x, y, z);
    }

    static void setVec4(GLuint programId, const std::string &name, const glm::vec4 &value) {
        glUniform4fv(glGetUniformLocation(programId, name.c_str()), 1, &value[0]);
    }

    static void setVec4(GLuint programId, const std::string &name, float x, float y, float z, float w) {
        glUniform4f(glGetUniformLocation(programId, name.c_str()), x, y, z, w);
    }

    static void setMat2(GLuint programId, const std::string &name, const glm::mat2 &mat) {
        glUniformMatrix2fv(glGetUniformLocation(programId, name.c_str()), 1, GL_FALSE, &mat[0][0]);
    }

    static void setMat3(GLuint programId, const std::string &name, const glm::mat3 &mat) {
        glUniformMatrix3fv(glGetUniformLocation(programId, name.c_str()), 1, GL_FALSE, &mat[0][0]);
    }

    static void setMat4(GLuint programId, const std::string &name, const glm::mat4 &mat) {
        glUniformMatrix4fv(glGetUniformLocation(programId, name.c_str()), 1, GL_FALSE, &mat[0][0]);
    }

    static glm::vec3 texCoordToVertexCoord(glm::vec2 &texCoord) {
        return glm::vec3(2 * texCoord.x - 1, 1 - 2 * texCoord.y, 0);
    }


};
#endif //OPENGLDEMO_GLUTILS_H
