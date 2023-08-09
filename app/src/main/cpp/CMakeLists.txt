# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

#cmake_minimum_required(VERSION 3.10.2)
cmake_minimum_required(VERSION 3.22.1)


# Declares and names the project.

project("opengldemo")

#设置源文件层级结构
include_directories(
        egl
        utils
        render
        glm
        parent
        shadersample
)

#设置源代码文件
file(GLOB src-files
        ${CMAKE_SOURCE_DIR}/egl/*.cpp
        ${CMAKE_SOURCE_DIR}/utils/*.cpp
        ${CMAKE_SOURCE_DIR}/render/*.cpp
        ${CMAKE_SOURCE_DIR}/utils/*.h
        ${CMAKE_SOURCE_DIR}/parent/*.h
        ${CMAKE_SOURCE_DIR}/parent/*.cpp
        ${CMAKE_SOURCE_DIR}/shadersample/*.cpp
        ${CMAKE_SOURCE_DIR}/shadertoy/*.cpp
)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.S
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
            ${src-files}
            native-lib.cpp
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

#增加 EGL GLESv3 库函数
target_link_libraries( # Specifies the target library.
                       native-lib
                       EGL
                       GLESv3
                       android
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )