@echo off

setlocal enabledelayedexpansion

set ERROR_BAD_HOME_SET=80
set ERROR_BAD_HOME_CALC=81
set ERROR_BAD_CONF=82


if "%1" == "-initEnv" (
	set GROUPER_HOME=
	set GROUPER_CONF=
)

if not "%GROUPER_HOME%" == "" (
	rem if GROUPER_HOME already set, verify it's a good directory
	call :checkGrouperHome "%GROUPER_HOME%"
	IF !ERRORLEVEL! NEQ 0 (
		call :errorBadHome "%GROUPER_HOME%"
		exit /b %ERROR_BAD_HOME_SET%
	)
) else (
	rem Otherwise, get dirname from parent of gsh path
	set GROUPER_CUR_DIR=%~dp0..
	call :checkGrouperHome "!GROUPER_CUR_DIR!"
	if !ERRORLEVEL! EQU 0 (
		set GROUPER_HOME=!GROUPER_CUR_DIR!
	) else (
		call :errorBadHome "!GROUPER_CUR_DIR!"
		exit /b %ERROR_BAD_HOME_CALC%
	)
)

if "%GSH_QUIET%" == "" echo Detected Grouper directory structure '%_grouperHomeType%' (valid is api or webapp)

if "%1" == "-initEnv" (
	if not "%~2" == "" (
		set GROUPER_CONF=%~2
	)	
)

if "%GROUPER_CONF%" == "" (
	if %_grouperHomeType% == api (
		set GROUPER_CONF=%GROUPER_HOME%\conf
	) else if %_grouperHomeType% == webapp (
		set GROUPER_CONF=%GROUPER_HOME%\classes
	)
)


if not exist "%GROUPER_CONF%\grouper.hibernate.properties" (
	echo The GROUPER_CONF environment variable ^(%GROUPER_CONF%^) is not
	echo   defined correctly or could not be determined. This should be a directory
	echo   containing property files, but is missing grouper.hibernate.properties
	exit /b %ERROR_BAD_CONF%
)


rem if using -initEnv, just exit here
if "%1" == "-initEnv" (
	endlocal

	rem path "%GROUPER_HOME%\bin;%PATH%"
	rem echo Added %GROUPER_HOME%\bin to PATH

	echo Setting GROUPER_HOME=%GROUPER_HOME%
	set GROUPER_HOME=%GROUPER_HOME%

	echo Setting GROUPER_CONF=%GROUPER_CONF%
	set GROUPER_CONF=%GROUPER_CONF%

	exit /b 0
)


rem Get standard environment variables
if exist "%GROUPER_HOME%\bin\setenv.bat" call "%GROUPER_HOME%\bin\setenv.bat"

if  "%MEM_START%" == "" set MEM_START=64m

if  "%MEM_MAX%" == "" set MEM_MAX=750m

if not "%JAVA_HOME%" == "" (set JAVA="%JAVA_HOME%\bin\java") else (set JAVA=java)


rem start with Grouper's configuration
set GROUPER_CP=%GROUPER_CONF%

if %_grouperHomeType% == api (
	rem Append Grouper jar
	set GROUPER_CP=%GROUPER_CP%;%GROUPER_HOME%\dist\lib\grouper.jar

	rem Append third party .jars
	set GROUPER_CP=!GROUPER_CP!;%GROUPER_HOME%\lib\grouper\*
	set GROUPER_CP=!GROUPER_CP!;%GROUPER_HOME%\lib\custom\*
	set GROUPER_CP=!GROUPER_CP!;%GROUPER_HOME%\lib\jdbcSamples\*
	set GROUPER_CP=!GROUPER_CP!;%GROUPER_HOME%\lib\ant\*
	set GROUPER_CP=!GROUPER_CP!;%GROUPER_HOME%\lib\test\*
	set GROUPER_CP=!GROUPER_CP!;%GROUPER_HOME%\dist\lib\test\*

	rem Append resources
	set GROUPER_CP=!GROUPER_CP!;%GROUPER_HOME%\src\resources
) else if %_grouperHomeType% == webapp (
	set GROUPER_CP=%GROUPER_CONF%;%GROUPER_HOME%\lib\*
)

rem Preserve the user's $CLASSPATH
if not "%CLASSPATH%" == "" set GROUPER_CP=%CLASSPATH%;%GROUPER_CP%

rem ----- Execute The Requested Command ---------------------------------------

if "%GSH_QUIET%" == "" (
	echo Using GROUPER_HOME:           %GROUPER_HOME%
	echo Using GROUPER_CONF:           %GROUPER_CONF%
	echo Using JAVA:                   %JAVA%
	echo Using CLASSPATH:              %GROUPER_CP%
	echo using MEMORY:                 %MEM_START%-%MEM_MAX%
)

set GSH=edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper

%JAVA% -Xms%MEM_START% -Xmx%MEM_MAX% -Dgrouper.home="%GROUPER_HOME%\\" -Dfile.encoding=utf-8 %GSH_JVMARGS% -classpath "%GROUPER_CP%" %GSH% %*

EXIT /B %ERRORLEVEL%


:checkGrouperHome
	set _grouperHomeType=
	if %1 == "" EXIT /B 1

	if exist %1%\dist\lib\grouper.jar (
		set _grouperHomeType=api
	) else if exist %1%\lib\grouper.jar (
		set _grouperHomeType=webapp
	) else (
		for %%a in (%1%\lib\grouper-*.jar) do set _grouperHomeType=webapp
	)

	if "%_grouperHomeType%" == "" EXIT /B 1
	exit /b 0


:errorBadHome
	echo The GROUPER_HOME environment variable (%1%) is
	echo     not defined correctly or could not be determined. The
	echo     grouper.jar file could not be found in either the dist/lib
	echo     or lib directory
	exit /b 0
