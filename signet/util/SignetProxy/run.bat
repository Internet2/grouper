@echo off
if "%OS%" == "Windows_NT" setlocal

rem -----------------------------------------------------------------------------
rem Script for command line utilility to assign System Administrator proxy
rem
rem Takes two arguments
rem    1 - Action to take -- grant | revoke | list
rem    2 - For grant or revoke, a login ID from the subject table
rem
rem Environment Variable Prequisites
rem
rem    JAVA_HOME     Must point to your JDK.
rem -----------------------------------------------------------------------------

if exist "%JAVA_HOME%\bin\java.exe" goto okJavaHome
echo This script requires that the JAVA_HOME environment variable be properly
echo set. That means that it must name a directory which contains
echo "bin\java.exe".
goto end

:okJavaHome
CALL ..\dist-signet_cp.bat

%JAVA_HOME%/bin/java -cp %CLASSPATH% edu.internet2.middleware.signet.util.SignetProxy %1 %2 %3

:end
