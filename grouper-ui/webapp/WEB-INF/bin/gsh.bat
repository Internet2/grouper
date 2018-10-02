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

rem In case we are in 'bin' try the parent directory
set GROUPER_HOME=%GROUPER_CUR_DIR%..

:gotHome
if exist "%GROUPER_HOME%\bin\gsh.bat" goto okHome
if not "%1" == "-initEnv" goto badGrouperHome

rem Something isn't right so revert to whatever we started with
set GROUPER_HOME=%GROUPER_HOME_SAFE%

:badGrouperHome
echo The GROUPER_HOME environment variable is not defined correctly
echo or could not be determined
echo This script must be located in "<GROUPER_HOME>" or "<GROUPER_HOME\bin"
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

if "%GROUPER_CONF%" == "" set GROUPER_CONF=%GROUPER_HOME%\classes

rem grouper_conf should be a directory
if not exist "%GROUPER_CONF%\" (
  echo The GROUPER_CONF environment variable '%GROUPER_CONF%'
  echo is not defined correctly or could not be determined. This should be
  echo a directory containing property files"
  goto end
)


set JAVA=java

if not "%JAVA_HOME%" == "" set JAVA="%JAVA_HOME%\bin\java"

rem start with Grouper's configuration
set GROUPER_CP=%GROUPER_CONF%

rem Append third party jars
set GROUPER_CP=%GROUPER_CP%;%GROUPER_HOME%\lib\*

rem Preserve the user's $CLASSPATH
if not "%CLASSPATH%" == "" set GROUPER_CP=%CLASSPATH%;%GROUPER_CP%

rem ----- Execute The Requested Command ---------------------------------------

echo Using GROUPER_HOME:           %GROUPER_HOME%
echo Using GROUPER_CONF:           %GROUPER_CONF%
echo Using JAVA:                   %JAVA%
echo Using CLASSPATH:              %GROUPER_CP%
echo using MEMORY:                 %MEM_START%-%MEM_MAX%

set GSH=edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper

%JAVA% -Xms%MEM_START% -Xmx%MEM_MAX% -Dgrouper.home="%GROUPER_HOME%\\" -Dfile.encoding=utf-8 %GSH_JVMARGS% -classpath "%GROUPER_CP%" %GSH% %*

:end
set GROUPER_HOME=%GROUPER_HOME_SAFE%

:endInitEnv
set GROUPER_CUR_DIR=
set GROUPER_HOME_SAFE=
set GROUPER_CP=
