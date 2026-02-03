cmake_minimum_required(VERSION 3.10)

project(documat VERSION 1.0.0 LANGUAGES CXX)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Add the executable
add_executable(documat src/main.cpp)

# Installation rules
install(TARGETS documat DESTINATION bin)
