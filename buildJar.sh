#!/bin/bash

javac -d build java/info/kgeorgiy/java/advanced/implementor/Impler.java java/info/kgeorgiy/java/advanced/implementor/JarImpler.java java/info/kgeorgiy/java/advanced/implementor/ImplerException.java java/ru/ifmo/ctddev/solutions/implementor/Implementor.java

cd build

jar cmvf MANIFEST.MF Implementor.jar ru/ifmo/ctddev/solutions/implementor/Implementor.class info/kgeorgiy/java/advanced/implementor/*.class
