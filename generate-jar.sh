mkdir out
javac -d out src/main/java/ru/ifmo/ctddev/solutions/implementor/*.java
cd out
jar -cvfm JarImplementor.jar ../manifest.mf ru/ifmo/ctddev/solutions/implementor/*
rm -rf ru