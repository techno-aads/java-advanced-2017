# Script for check 3rd lab
cd java/
javac -cp "../lib/*:../artifacts/*" ru/ifmo/ctddev/solutions/implementor/*.java
jar cvf ru/ifmo/ctddev/solutions/implementor/implementor.jar ru/ifmo/ctddev/solutions/implementor/
cp ru/ifmo/ctddev/solutions/implementor/implementor.jar ../artifacts/
cd ../
java -cp artifacts/*:lib/* info.kgeorgiy.java.advanced.implementor.Tester class ru.ifmo.ctddev.solutions.implementor.Implementor