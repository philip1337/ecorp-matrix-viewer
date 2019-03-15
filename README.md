# ecorp-matrix-viewer
Matrix Viewer - Displays "something" on a RGB-LED-Matrix.

## Source Tree
* [src/common](src/common) - contains the utility functions
* [src/master](src/master) - contains master process (includes a web server to serve our interface)
* [src/master-web-ui](src/master-web-ui) - contains the web interface (html & assets)
* [src/node](src/node) - contains the node that process the delegations from the master server
* [src/simple](src/simple) - contains the simple command line application to communicate with the matrix
* [src/veloxio](src/veloxio) - contains the [veloxio virtual file system](https://github.com/philip1337/velox-io-java)
* [src/veloxio-archiver](src/veloxio-archiver) - contains the veloxio-archiver to create the archives used by our applications

## Build requirements
To build this application you need the [gradle build system](https://gradle.org/).

## Libraries
* [velox-io-java](https://github.com/philip1337/velox-io-java)
* [picoli](https://github.com/remkop/picocli)
* [jSerialCom](https://github.com/Fazecast/jSerialComm)
* [jTwig](https://github.com/jtwig/jtwig-core)
* [netty-io](https://github.com/netty/netty)
* [imgscalr](https://github.com/rkalla/imgscalr)

## Members
* Patrick (Project Manager)
* Leroy (Co-Worker)
* Philip (Co-Worker & Lead Developer)
