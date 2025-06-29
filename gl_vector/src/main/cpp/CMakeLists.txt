cmake_minimum_required(VERSION 3.22.1)
PROJECT(nativelib)

add_library(
        nativelib
        SHARED
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

target_link_libraries(
        nativelib
        jnigraphics
        ${log-lib}
        ${android-lib}
)