cmake_minimum_required(VERSION 3.22.1)
project(cacoustic_dsp LANGUAGES CXX)

add_library(
    cacoustic_dsp
    SHARED
    NativeDsp.cpp
)

find_library(log-lib log)

target_compile_features(cacoustic_dsp PRIVATE cxx_std_17)
target_link_libraries(cacoustic_dsp ${log-lib})
