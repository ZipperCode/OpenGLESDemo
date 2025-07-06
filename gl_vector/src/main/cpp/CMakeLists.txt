cmake_minimum_required(VERSION 3.22.1)
PROJECT(nativelib)

add_library(
        nativelib
        SHARED
        opengles.cpp
        image_process.cpp
        opengles_blur.cpp
        nativelib.cpp
)

find_library(
        log-lib
        log
)

find_library(
        android-lib
        android
)
find_library(
        GLES3_LIB
        GLESv3)
target_link_libraries(
        nativelib
        jnigraphics
        EGL
        ${GLES3_LIB}
        ${log-lib}
        ${android-lib}
)