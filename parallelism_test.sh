# Script for check 6rd lab
rm -rf artifacts/concurrent.jar
cd java/
javac -cp "../lib/*:../artifacts/*" ru/ifmo/ctddev/solutions/concurrent/*.java
jar cvf ru/ifmo/ctddev/solutions/concurrent/concurrent.jar ru/ifmo/ctddev/solutions/concurrent/
cp ru/ifmo/ctddev/solutions/concurrent/concurrent.jar ../artifacts/
cd ../
java -cp artifacts/IterativeParallelismTest.jar:artifacts/concurrent.jar:lib/* info.kgeorgiy.java.advanced.concurrent.Tester list ru.ifmo.ctddev.solutions.concurrent.IterativeParallelism