@echo off
javac -d temp info\kgeorgiy\java\advanced\implementor\Impler.java info\kgeorgiy\java\advanced\implementor\JarImpler.java info\kgeorgiy\java\advanced\implementor\ImplerException.java ru\ifmo\ctddev\solutions\implementor\Implementor.java
jar cfm Implementor.jar MANIFEST.MF temp\info\kgeorgiy\java\advanced\implementor\Impler.class temp\info\kgeorgiy\java\advanced\implementor\JarImpler.class temp\info\kgeorgiy\java\advanced\implementor\ImplerException.class temp\ru\ifmo\ctddev\solutions\implementor\Implementor.class
rmdir temp /s /q
@echo on