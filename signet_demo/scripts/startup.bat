@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Start script for the Signet demo system
rem
rem $Id: startup.bat,v 1.1 2005-12-20 21:46:32 acohen Exp $
rem ---------------------------------------------------------------------------

set TOMCAT_DIR=jakarta-tomcat-5.0.28
set HSQLDB_DIR=hsqldb_1_8_0_1

rem Guess SIGNET_DEMO_HOME if not defined
set INITIAL_DIR=%cd%

if not "%SIGNET_DEMO_HOME%" == "" goto gotHome

set SIGNET_DEMO_HOME=%INITIAL_DIR%

if exist "%SIGNET_DEMO_HOME%\%TOMCAT_DIR%" goto okHome

cd ..
set SIGNET_DEMO_HOME=%cd%
cd %INITIAL_DIR%

:gotHome
if exist "%SIGNET_DEMO_HOME%\%TOMCAT_DIR%" goto okHome
echo The SIGNET_DEMO_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end

:okHome
set TOMCAT_HOME=%SIGNET_DEMO_HOME%\%TOMCAT_DIR%
set TOMCAT_EXECUTABLE_DIR=%TOMCAT_HOME%\bin
set TOMCAT_EXECUTABLE=%TOMCAT_EXECUTABLE_DIR%\startup.bat

rem Check that target executable exists
if exist "%TOMCAT_EXECUTABLE%" goto okExec
echo Cannot find %TOMCAT_EXECUTABLE%
echo This file is needed to run this program
goto end

:okExec

cd %TOMCAT_EXECUTABLE_DIR%
call "%TOMCAT_EXECUTABLE%"

:end
