mkdir out
javac -d out -cp "./java;./lib/*" java/ru/ifmo/ctddev/solutions/implementor/*.java
cd out
jar -cvfm JarImplementor.jar ../manifest.mf .