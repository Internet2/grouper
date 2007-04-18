@echo off
if "%OS%" == "Windows_NT" setlocal

rem -----------------------------------------------------------------------------
rem Script for command line util to load Tree XML
rem
rem Environment Variable Prequisites
rem
rem   JAVA_HOME     Must point to your JDK.
rem -----------------------------------------------------------------------------

if exist "%JAVA_HOME%\bin\java.exe" goto okJavaHome
echo This script requires that the JAVA_HOME environment variable be properly
echo set. That means that it must name a directory which contains
echo "bin\java.exe".
goto end

:okJavaHome
CALL ..\dist-signet_cp.bat

%JAVA_HOME%/bin/java -cp %CLASSPATH% edu.internet2.middleware.signet.util.TreeXmlLoader %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
