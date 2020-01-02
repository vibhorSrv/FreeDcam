//https://stackoverflow.com/questions/28027937/cross-platform-sockets
#undef UNICODE


/* Assume that any non-Windows platform uses POSIX-style sockets instead. */
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>  /* Needed for getaddrinfo() and freeaddrinfo() */
#include <unistd.h> /* Needed for close() */

#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>

#include <stdlib.h>
#include <stdio.h>
#include <string>
#include "DngProfile.h"
#include "CustomMatrix.h"
#include "DngWriter.h"


using namespace std;




DngProfile * getDngP9(int CROP_SIZE)
{
    DngProfile * prof = new DngProfile();
    prof->blacklevel = new float[4] {0,0,0,0};
    prof->whitelevel = 1023;
    prof->rawheight = CROP_SIZE;
    prof->rawwidht = CROP_SIZE;
    prof->bayerformat = "rggb";
    prof->rawType = 5;
    prof->rowSize = 0;

    return prof;
}


CustomMatrix * getP9Matrix()
{
    CustomMatrix * matrix = new CustomMatrix();
    matrix->colorMatrix1 = new float[9]{1.068359018f ,-0.2988280055f, -0.1425780054f, -0.4316410122f, 1.355468989f, 0.05078099996f, -0.1015620004f, 0.2441409972f, 0.5859379766f};
    matrix->colorMatrix2 = new float[9]{2.209960938f, -1.332031012f, -0.1416019942f, -0.1894530054f, 1.227538943f, -0.05468700037f, -0.02343700036f, 0.1826169934f, 0.6035159824f};
    matrix->neutralColorMatrix = new float[3]{1, 1, 1};
    return matrix;
}



int __cdecl main(void)
{
    const int CROP_SIZE = 200;
    const int DEFAULT_BUFLEN = CROP_SIZE*CROP_SIZE*2;//20000;
    const int DEFAULT_PORT = 3333;

    // Setup server for unix
    // taken from: https://www.geeksforgeeks.org/socket-programming-cc/
    int server_fd, new_socket, valread;
    struct sockaddr_in address;
    int opt = 1;
    int addrlen = sizeof(address);
    char recvbuf[DEFAULT_BUFLEN];
    int recvbuflen = DEFAULT_BUFLEN;

    printf("get dng profile \n");
    DngProfile * profile = getDngP9(CROP_SIZE);

    printf("Listening on Port: %i\n", DEFAULT_PORT);
    printf("Setting a crop_size of: %i\n", CROP_SIZE);

    int IMAGEDATALENGTH = CROP_SIZE*CROP_SIZE * 2;
    unsigned char imagedata[CROP_SIZE*CROP_SIZE * 2] = {0};

    int byteCount = 0;
    int imagesRecieved = 0;
    long totalbytes = 0;

    printf("Get DNG matrix \n");
    CustomMatrix * customMatrix = getP9Matrix();

    printf("Init DNG writer\n");
    DngWriter *dngw = new DngWriter();

    printf("Done Initializing DNG writer\n");
    dngw->dngProfile = profile;
    dngw->customMatrix = customMatrix;

    // Creating socket file descriptor
    printf("Initializing Server Socket\n");
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0)
    {
        perror("socket failed");
        exit(EXIT_FAILURE);
    }

    int optlen = sizeof(opt);
    // Forcefully attaching socket to the port 8080
    if (setsockopt(server_fd, SOL_SOCKET, SO_KEEPALIVE, &opt, sizeof(opt)) < 0)// (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, optlen));
    {
        perror("setsockopt");
        exit(EXIT_FAILURE);
    }
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(DEFAULT_PORT);


    // Forcefully attaching socket to the port
    if ((::bind(server_fd, (struct sockaddr *)&address, sizeof(address))) == -1) {
        printf("Error: unable to bind\n");
        printf("Error code: %d\n", errno);
        exit(1);
    }
    if (listen(server_fd, 3) < 0)
    {
        perror("listen");
        exit(EXIT_FAILURE);
    }
    if ((new_socket = accept(server_fd, (struct sockaddr *)&address,(socklen_t*)&addrlen))<0)
    {
        perror("accept");
        exit(EXIT_FAILURE);
    }
    printf("Binding to cellphone successfull!\n");


    int iter = 0;
    // Receive until the peer shuts down the connection
    do {
        //printf("%i", 1);
        valread = read(new_socket, recvbuf, recvbuflen); //recv(ClientSocket, recvbuf, recvbuflen, 0);
        //printf("%i", 2);
        iter++;
        if (valread > 0) {
            //printf("%i - Bytes received: %d\n", iter, valread);
            totalbytes += recvbuf;

            for (int i = 0; i < valread; i++) {
                imagedata[byteCount++] = recvbuf[i];
            }

            if(byteCount == IMAGEDATALENGTH){
                //printf("totalbytes: %i bytes per img %i \n", totalbytes, (totalbytes/IMAGEDATALENGTH));
                string fname = string("img_") + to_string(imagesRecieved++) + string(".dng");
                dngw->bayerBytes = imagedata;
                dngw->rawSize = IMAGEDATALENGTH;
                dngw->fileSavePath = new char[fname.size()];
                strcpy(dngw->fileSavePath, fname.c_str());
                fname.clear();
                //printf("File path: %s Size %i\n", dngw->fileSavePath, byteCount);
                dngw->_make = "dngwriter";
                dngw->_model = "model";
                dngw->WriteDNG();
                //printf("done write dng\n");
                byteCount = 0;
                //printf("saved file "+fname);

                iter = 0;
            }

        }
        else if (valread == 0)
        {
            printf("Connection closing...\n");
            return 0;
        }
        else  {
            printf("recv failed with error");//: %d\n", WSAGetLastError());
            return 1;
        }


    } while (valread > 0);
    // shutdown the connection since we're done

    return 0;
}