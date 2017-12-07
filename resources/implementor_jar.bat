javac -cp artifacts/JarImplementorTest.jar java/ru/ifmo/ctddev/solutions/implementor/Implementor.java
cd java\ru\ifmo\ctddev\solutions\implementor
jar cmf ..\..\..\..\..\..\resources\manifest.txt ..\..\..\..\..\..\Implementor.jar *.class
del *.class
cd ..\..\..\..\..\..\