@echo off
REM $Header: /home/hagleyj/i2mi/signet/demo/shutdown_hsqldb.bat,v 1.1 2007-04-18 00:11:31 ddonn Exp $

REM This script shuts down the HypersonicSQL database in Server mode.
REM It is called by the Signet QuickStart demo shutdown script. Or, it
REM may be invoked directly.

IF "%OS%" == "Windows_NT" setlocal

IF EXIST "%JAVA_HOME%\bin\java.exe" GOTO okJavaHome
ECHO This script requires that the JAVA_HOME environment variable be properly
ECHO set. It must name a directory which contains "bin\java.exe".
GOTO errorEnd

:okJavaHome
CALL set_env.bat HSQLDB

:okHome
IF EXIST %HSQLDB_EXEC% GOTO okExec
ECHO Could not find the HSQLDB executable: %HSQLDB_EXEC%.
GOTO errorEnd

:okExec
"%JAVA_HOME%\bin\java.exe" -jar %HSQLDB_EXEC% --sql "SHUTDOWN" --rcfile %HSQLDB_RC_FILE% %HSQLDB_URLID%
IF NOT ERRORLEVEL 0 GOTO errorEnd
GOTO okEnd

:errorEnd
ECHO Abnormally terminated.
GOTO end

:okEnd
ECHO HSQLDB successfully shutdown.

:end

