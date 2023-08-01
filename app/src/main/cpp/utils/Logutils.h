//
// Created by didi on 2023/5/13.
//


#ifndef OPENGLDEMO_LOGUTILS_H
#define OPENGLDEMO_LOGUTILS_H

#include "android/log.h"
#include "sys/time.h"
#include "time.h"
#define LOG_TAG "opengl_ndk"


#define  LogutilsE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LogutilsV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define  LogutilsD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LogutilsI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)


#define FUN_BEGIN_TIME(FUN) { \
    LogutilsI("%s:%s begin",__FILE__,FUN); \
    long long t0 = getCurrentTime();

#define FUN_END_TIME(FUN)\
    long long t1 = getCurrentTime(); \
    LogutilsI("%s:%s func cost time %ldms", __FILE__, FUN, (long)(t1-t0));}


static long getCurrentTime(){
//   struct timeval time;
//    gettimeofday(&time,NULL);
//    long long currentTime = ((long long) (time.tv_sec) ) * 1000 +time.tv_usec/1000 ;
//    return currentTime;

    struct timespec ts;
    clock_gettime(CLOCK_REALTIME,&ts);

    return ts.tv_sec*1000000000+ts.tv_nsec;

}


#define  GO_CHECK_GL_ERROR(...)  LogutilsE("CHECK_GL_ERROR %s glGetError = %d, line = %d, ",  __FUNCTION__, glGetError(), __LINE__)
#endif //OPENGLDEMO_LOGUTILS_H
