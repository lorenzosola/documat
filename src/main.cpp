#include <iostream>
#include <string>

int main(int argc, char* argv[]) {
    std::cout << "Documat - Document Management Tool" << std::endl;
    std::cout << "Version 1.0.0" << std::endl;
    std::cout << std::endl;
    
    if (argc > 1) {
        std::cout << "Arguments provided:" << std::endl;
        for (int i = 1; i < argc; ++i) {
            std::cout << "  [" << i << "] " << argv[i] << std::endl;
        }
    } else {
        std::cout << "Usage: documat [options]" << std::endl;
        std::cout << "No arguments provided. Running in default mode." << std::endl;
    }
    
    std::cout << std::endl;
    std::cout << "Documat is ready to use!" << std::endl;
    
    return 0;
}
