@echo off
REM $Header: /home/hagleyj/i2mi/signet/demo/start_demo.bat,v 1.2 2007-04-22 01:17:36 ddonn Exp $

REM This script starts the Signet QuickStart Demo.

IF "%OS%" == "Windows_NT" setlocal

IF EXIST "%JAVA_HOME%\bin\java.exe" GOTO okJavaHome
ECHO This script requires that the JAVA_HOME environment variable be properly
ECHO set. It must name a directory which contains "bin\java.exe".
GOTO errorEnd

:okJavaHome
CALL set_env.bat DEMO

REM IF EXIST %CATALINA_HOME% GOTO okHome
REM ECHO Could not find the Tomcat home directory: %CATALINA_HOME%
REM GOTO errorEnd

:okHome
IF EXIST %TOMCAT_START% GOTO okExec
ECHO Could not find the Tomcat startup script: %TOMCAT_START%
GOTO errorEnd

:okExec
CALL start_hsqldb.bat FROM_DEMO_SCRIPT
CALL %TOMCAT_START%
START http://localhost:8080/signet
GOTO okEnd

:errorEnd
ECHO Abnormally terminated.
GOTO end

:okEnd
ECHO Tomcat Web Server successfully started. Use 'shutdown_demo.bat' to shut down.

:end

