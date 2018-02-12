@echo off
if not exist "temp" mkdir temp
javac -d temp info\kgeorgiy\java\advanced\implementor\Impler.java info\kgeorgiy\java\advanced\implementor\JarImpler.java info\kgeorgiy\java\advanced\implementor\ImplerException.java ru\ifmo\ctddev\solutions\implementor\Implementor.java
cd temp
jar cfm "..\Implementor.jar" "..\manifest.txt" info\kgeorgiy\java\advanced\implementor\Impler.class info\kgeorgiy\java\advanced\implementor\JarImpler.class info\kgeorgiy\java\advanced\implementor\ImplerException.class ru\ifmo\ctddev\solutions\implementor\Implementor.class
cd ..
rmdir temp /s /q
@echo on