# Documat

A document management tool.

## Prerequisites

To build and run Documat on Linux from scratch, you need:

- **CMake** (version 3.10 or higher)
- **C++ Compiler** with C++17 support (e.g., g++, clang++)
- **Make** or another CMake-compatible build tool

### Installing Prerequisites on Linux

#### Ubuntu/Debian
```bash
sudo apt-get update
sudo apt-get install -y cmake g++ make
```

#### Fedora/RHEL/CentOS
```bash
sudo dnf install -y cmake gcc-c++ make
```

#### Arch Linux
```bash
sudo pacman -S cmake gcc make
```

## Building from Scratch

1. **Clone the repository** (if you haven't already):
   ```bash
   git clone https://github.com/lorenzosola/documat.git
   cd documat
   ```

2. **Create a build directory**:
   ```bash
   mkdir build
   cd build
   ```

3. **Configure the project with CMake**:
   ```bash
   cmake ..
   ```

4. **Build the project**:
   ```bash
   make
   ```

   Or, for faster builds with multiple CPU cores:
   ```bash
   make -j$(nproc)
   ```

## Running Documat

After building, run the executable:

```bash
./documat
```

You can also pass arguments:

```bash
./documat arg1 arg2 arg3
```

## Installation (Optional)

To install Documat system-wide:

```bash
sudo make install
```

After installation, you can run `documat` from anywhere:

```bash
documat
```

## Quick Start (All-in-One)

```bash
# Install dependencies (Ubuntu/Debian example)
sudo apt-get update && sudo apt-get install -y cmake g++ make

# Clone, build, and run
git clone https://github.com/lorenzosola/documat.git
cd documat
mkdir build && cd build
cmake ..
make -j$(nproc)
./documat
```

## Cleaning Build Files

To clean the build directory:

```bash
cd build
make clean
```

To completely remove the build directory:

```bash
cd ..
rm -rf build
```

## License

This project is licensed under the GPLv3 License - see the [LICENSE](LICENSE) file for details.