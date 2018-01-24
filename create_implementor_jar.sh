# Script fro generate jar-file for 4th lab
cd java
javac -cp "../lib/*:../artifacts/*" ru/ifmo/ctddev/solutions/implementor/*.java
echo Main-Class: ru.ifmo.ctddev.solutions.implementor.Main > manifest.txt
cp ../artifacts/JarImplementorTest.jar ru/ifmo/ctddev/solutions/implementor
cp ../lib/javapoet-1.9.0.jar ru/ifmo/ctddev/solutions/implementor
echo Class-Path: JarImplementorTest.jar javapoet-1.9.0.jar >> manifest.txt
jar -cvfm ru/ifmo/ctddev/solutions/implementor/Implementor.jar manifest.txt ru/ifmo/ctddev/solutions/implementor/*.class