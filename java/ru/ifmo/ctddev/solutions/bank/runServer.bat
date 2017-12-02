@echo off
set classpath=../..

start %java_home%\bin\rmiregistry
start %java_home%\bin\java examples.rmi.Server


REM !! not actual