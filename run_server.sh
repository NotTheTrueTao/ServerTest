#!/bin/sh
# javac -encoding UTF-8 testServer/SocketServer1.java
# sudo java testServer.SocketServer1

# javac -encoding UTF-8 testServer/SocketServerMultiThread.java
# sudo java testServer.SocketServerMultiThread


name=$1

javac -encoding UTF-8 testServer/${name}.java
sudo java testServer.${name}
