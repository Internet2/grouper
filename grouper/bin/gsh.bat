@echo off
if  "%1" == "-initEnv" goto afterSetLocal

rem Don't want side effects unless initing the environment
setlocal

:afterSetLocal

rem In case something goes wrong
set GROUPER_HOME_SAFE=%GROUPER_HOME%

rem Work out where we are
set GROUPER_CUR_DIR=%~dp0

rem Guess GROUPER_HOME if not defined
if "%GROUPER_HOME%" == "" goto noGrouperHome

rem If we are not initing figure out if we are in a valid directory
if not "%1" == "-initEnv" goto checkGrouperHome

rem Initing so force GROUPER_HOME to be where we are now
echo Attempting to reset GROUPER_HOME

:noGrouperHome
set GROUPER_HOME=%GROUPER_CUR_DIR%

:checkGrouperHome
if exist "%GROUPER_HOME%\bin\gsh.bat" goto okHome

rem In case we are in 'bin' try teh parent directory 
set GROUPER_HOME=%GROUPER_CUR_DIR%..

:gotHome
if exist "%GROUPER_HOME%\bin\gsh.bat" goto okHome
if not "%1" == "-initEnv" goto badGrouperHome

rem Something isn't right so revert to whatever we started with
set GROUPER_HOME=%GROUPER_HOME_SAFE%

:badGrouperHome
echo The GROUPER_HOME environment variable is not defined correctly
echo or could not be determined
echo This script must be located in "<GROUPER_HOME>" or "<GROUPER_HOME/bin"
goto end
:okHome

path %GROUPER_HOME%\bin;%PATH%;
if not "%1" == "-initEnv" goto run

echo Added %GROUPER_HOME%\bin to PATH
set GROUPER_HOME=%GROUPER_HOME%
echo Setting GROUPER_HOME=%GROUPER_HOME%
if "%2" == "" goto endInitEnv

if exist "%2%\grouper.hibernate.properties" set GROUPER_CONF=%2
if "%GROUPER_CONF%" == "" goto run
echo Using GROUPER_CONF=%GROUPER_CONF%
goto endInitEnv

:run
rem We aren't initing so handle args

rem Get standard environment variables
if exist "%GROUPER_HOME%\bin\setenv.bat" call "%GROUPER_HOME%\bin\setenv.bat"

if  "%MEM_START%" == "" set MEM_START=64m

if  "%MEM_MAX%" == "" set MEM_MAX=750m

if "%GROUPER_CONF%" == "" set GROUPER_CONF=%GROUPER_HOME%/conf

set JAVA=java

if not "%JAVA_HOME%" == "" set JAVA="%JAVA_HOME%/bin/java"

rem Append Grouper's configuration
set GROUPER_CP=%GROUPER_HOME%/conf

rem Append Grouper .jar
set GROUPER_CP=%GROUPER_CP%;%GROUPER_HOME%/dist/lib/grouper.jar

rem Append third party .jars
set GROUPER_CP=%GROUPER_CP%;%GROUPER_HOME%/lib/grouper/*
set GROUPER_CP=%GROUPER_CP%;%GROUPER_HOME%/lib/custom/*
set GROUPER_CP=%GROUPER_CP%;%GROUPER_HOME%/lib/jdbcSamples/*
set GROUPER_CP=%GROUPER_CP%;%GROUPER_HOME%/lib/ant/*
set GROUPER_CP=%GROUPER_CP%;%GROUPER_HOME%/lib/test/*
set GROUPER_CP=%GROUPER_CP%;%GROUPER_HOME%/dist/lib/test/*

rem Preserve the user's $CLASSPATH
set GROUPER_CP=%GROUPER_CP%;%CLASSPATH%

rem ----- Execute The Requested Command ---------------------------------------

echo Using GROUPER_HOME:           %GROUPER_HOME%
echo Using GROUPER_CONF:           %GROUPER_CONF%
echo Using JAVA:                   %JAVA%
echo using MEMORY:                 %MEM_START%-%MEM_MAX%

set GSH=edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper

rem %JAVA%  -Xms%MEM_START% -Xmx%MEM_MAX% -Dgrouper.home="%GROUPER_HOME%\\" %GSH_JVMARGS% -jar %GROUPER_HOME%/lib/grouper/invoker.jar -cpdir %GROUPER_CONF% -cpalljars %GROUPER_HOME%/lib -cpjar %GROUPER_HOME%/dist/lib/grouper.jar  -cpjar %GROUPER_HOME%/dist/lib/test/grouper-test.jar %GSH% %*

%JAVA% -Xms%MEM_START% -Xmx%MEM_MAX% -Dgrouper.home="%GROUPER_HOME%\\" -Dfile.encoding=utf-8 %GSH_JVMARGS% -classpath "%GROUPER_CP%" %GSH% %*

:end
set GROUPER_HOME=%GROUPER_HOME_SAFE%

:endInitEnv
set GROUPER_CUR_DIR=
set GROUPER_HOME_SAFE=
set GROUPER_CP=
