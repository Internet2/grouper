@echo off
REM $Header: /home/hagleyj/i2mi/signet/demo/shutdown_demo.bat,v 1.1 2007-04-18 00:11:31 ddonn Exp $

REM This script shuts down the Signet QuickStart Demo

IF "%OS%" == "Windows_NT" setlocal

CALL set_env.bat DEMO

:okHome
IF EXIST %TOMCAT_SHUTDOWN% GOTO okExec
ECHO Could not find the Tomcat shutdown script: %TOMCAT_SHUTDOWN%
GOTO errorEnd

:okExec
CALL %TOMCAT_SHUTDOWN%
CALL shutdown_hsqldb.bat
GOTO okEnd

:errorEnd
ECHO Abnormally terminated.
GOTO end

:okEnd
ECHO Tomcat Web Server successfully shutdown.

:end

