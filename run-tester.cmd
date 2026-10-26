@echo off
rem Runs the course tester against the jars in artifacts\.
setlocal
cd /d "%~dp0"

if "%JDK25_HOME%"=="" set JDK25_HOME=C:\Program Files\Java\jdk-25.0.4

"%JDK25_HOME%\bin\java.exe" -cp "tester\workshop-tester.jar;lib\logic-1.0.jar;lib\model-1.0.jar;artifacts\workshop-model-impl.jar;artifacts\workshop-logic-impl.jar" patmal.workshops.main.Main
echo.
echo Tester exit code: %errorlevel%
