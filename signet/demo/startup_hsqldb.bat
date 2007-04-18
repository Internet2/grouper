@echo off
REM $Header: /home/hagleyj/i2mi/signet/demo/startup_hsqldb.bat,v 1.1 2007-04-18 00:11:31 ddonn Exp $

REM This script starts the HypersonicSQL database in Server mode.
REM It is called by the Signet QuickStart demo startup script. Or, it
REM may be invoked directly.

IF "%OS%" == "Windows_NT" setlocal

IF EXIST "%JAVA_HOME%\bin\java.exe" GOTO okJavaHome
ECHO This script requires that the JAVA_HOME environment variable be properly
ECHO set. It must name a directory which contains "bin\java.exe".
GOTO errorEnd

:okJavaHome
CALL set_env.bat HSQLDB

IF EXIST %HSQLDB_EXEC% GOTO okExec
ECHO Could not find the HSQLDB executable: %HSQLDB_EXEC%
GOTO errorEnd

:okExec
start "Signet Demo HSQLDB Server" "%JAVA_HOME%\bin\java.exe" -classpath %HSQLDB_CP% org.hsqldb.Server -database.0 %HSQLDB_DIR%\%HSQLDB_FILENAME% -dbname.0 %HSQLDB_ALIAS%
GOTO okEnd

:errorEnd
ECHO Abnormally terminated.
GOTO end

:okEnd
IF "%1" == "FROM_DEMO_SCRIPT" GOTO end
ECHO HSQLDB successfully started. Use 'shutdown_hsqldb.bat' to shut down.

:end
