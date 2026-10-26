@echo off
rem Compiles both modules with plain javac and packages them into artifacts\.
setlocal
cd /d "%~dp0"

if "%JDK25_HOME%"=="" set JDK25_HOME=C:\Program Files\Java\jdk-25.0.4
set JAVAC="%JDK25_HOME%\bin\javac.exe"
set JAR="%JDK25_HOME%\bin\jar.exe"
set API=lib\model-1.0.jar;lib\logic-1.0.jar

if exist out\build rmdir /s /q out\build
mkdir out\build\model
mkdir out\build\logic

dir /s /b model\src\*.java > out\model-sources.txt 2>nul
if errorlevel 1 (echo No sources under model\src yet. & exit /b 1)
%JAVAC% -d out\build\model -cp "%API%" @out\model-sources.txt
if errorlevel 1 exit /b 1

dir /s /b logic\src\*.java > out\logic-sources.txt 2>nul
if errorlevel 1 (echo No sources under logic\src yet. & exit /b 1)
%JAVAC% -d out\build\logic -cp "%API%;out\build\model" @out\logic-sources.txt
if errorlevel 1 exit /b 1

%JAR% --create --file artifacts\workshop-model-impl.jar -C out\build\model .
if errorlevel 1 exit /b 1
%JAR% --create --file artifacts\workshop-logic-impl.jar -C out\build\logic .
if errorlevel 1 exit /b 1

echo.
echo Built artifacts\workshop-model-impl.jar and artifacts\workshop-logic-impl.jar
