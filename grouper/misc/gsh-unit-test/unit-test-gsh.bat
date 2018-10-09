
echo off

setlocal

set GRP_API_DIR=C:\Users\cer28\Documents\UNC\Git\grouper-dist\grouper\grouper
set GRP_UI_DIR=C:\Users\cer28\Documents\UNC\Git\grouper-dist\grouper\grouper-ui\dist\grouper\WEB-INF
set GRP_STAGE_DIR=C:\cygwin64\home\cer28\tmp\grouper-stage
set CLASSPATH=x y z

MKDIR %GRP_STAGE_DIR%\fake-java-true\bin
MKDIR %GRP_STAGE_DIR%\fake-java-false\bin

COPY C:\cygwin64\bin\true.exe %GRP_STAGE_DIR%\fake-java-true\bin\java.exe
COPY C:\cygwin64\bin\false.exe %GRP_STAGE_DIR%\fake-java-false\bin\java.exe

set JAVA_HOME=%GRP_STAGE_DIR%\fake-java-true
rem set JAVA_HOME=C:\Program Files\Java\jre1.8.0_181


if "%1%" == "" (
	call :test1
	call :test2
	call :test3
	call :test4
	call :test5
	call :test6
	call :test7
	call :test8
	call :test9
) else (
	call %1%
)


exit /b 0


:test1
rem Test 1: Execute gsh from current directory

set GROUPER_HOME=
set GROUPER_CONF=

echo Test 1A Execute ./gsh from api bin directory
cd "%GRP_API_DIR%\bin"
call .\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

echo Test 1B Execute ./gsh from webapp bin directory
cd "%GRP_UI_DIR%\bin"
call .\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

echo Test 1C
call C:\Users\cer28\Documents\UNC\Git\grouper\grouper\unc-grouper-ui\unc-grouper-ui-war\target\grouper\WEB-INF\bin\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

exit /b 0


rem -----------------

:test2
rem Test 2: Execute gsh as absolute path from outside directory

set GROUPER_HOME=
set GROUPER_CONF=

CD C:\Temp

echo Test 2A Execute gsh from api bin directory
call %GRP_API_DIR%\bin\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

echo Test 2B Execute gsh from webapp bin directory
call %GRP_UI_DIR%\bin\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

echo Test 2C Execute gsh from a Maven directory
call C:\Users\cer28\Documents\UNC\Git\grouper\grouper\unc-grouper-ui\unc-grouper-ui-war\target\grouper\WEB-INF\bin\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

exit /b 0

rem -----------------

:test3
rem #3) Set grouper home and call gsh in non-grouper directory

set JAVA_HOME=%GRP_STAGE_DIR%\fake-java-true
set GROUPER_CONF=
set GROUPER_HOME=
cd C:\Temp

MKDIR %GRP_STAGE_DIR%\test3\bin

COPY %GRP_API_DIR%\bin\gsh.bat %GRP_STAGE_DIR%\test3\bin

echo Test 3A set grouper home as api, should not use gsh directory for calcs
set GROUPER_HOME=%GRP_API_DIR%
call %GRP_STAGE_DIR%\test3\bin\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

echo Test 3B set grouper home as webapp, should not use gsh directory for calcs
SET GROUPER_HOME=%GRP_UI_DIR%
call %GRP_STAGE_DIR%\test3\bin\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

exit /b 0


rem -----------------

:test4
rem #4) conf set to external dir (with spaces); execute from bin directory


MKLINK /J "%GRP_STAGE_DIR%\Grouper Conf" %GRP_API_DIR%\conf
MKLINK /J "%GRP_STAGE_DIR%\Grouper Classes" %GRP_UI_DIR%\classes

set JAVA_HOME=%GRP_STAGE_DIR%\fake-java-true
set GROUPER_HOME=
cd C:\Temp


echo Test 4A conf set to external dir (with spaces); execute from api bin directory
set GROUPER_CONF=%GRP_STAGE_DIR%\Grouper Conf
call %GRP_API_DIR%\bin\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

echo Test 4B conf set to external dir (with spaces); execute from webapp bin directory
set GROUPER_CONF=%GRP_STAGE_DIR%\Grouper Classes
call %GRP_UI_DIR%\bin\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

exit /b 0


rem -----------------

:test5
rem #5) define grouper_conf

MKLINK /J "%GRP_STAGE_DIR%\Grouper Conf" %GRP_API_DIR%\conf
MKLINK /J "%GRP_STAGE_DIR%\Grouper Classes" %GRP_UI_DIR%\classes


set JAVA_HOME=%GRP_STAGE_DIR%\fake-java-true
set GROUPER_HOME=
set GROUPER_CONF=
cd C:\Temp

echo Test 5A grouper conf is staging/Grouper+Conf; run from api bin
set GROUPER_CONF=%GRP_STAGE_DIR%\Grouper Conf
call %GRP_API_DIR%\bin\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

echo Test 5B grouper conf is staging/Grouper+Classes; run from webapp bin
set GROUPER_CONF=%GRP_STAGE_DIR%\Grouper Classes
call %GRP_UI_DIR%\bin\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

echo Test 5C mix up conf and classes does not matter
set GROUPER_CONF=%GRP_STAGE_DIR%\Grouper Conf
call %GRP_UI_DIR%\bin\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

echo Test 5D mix up conf and classes does not matter
set GROUPER_CONF=%GRP_STAGE_DIR%\Grouper Classes
call %GRP_API_DIR%\bin\gsh
echo exitcode %ERRORLEVEL% should be 0
echo --------

exit /b 0


rem -----------------

:test6
rem #6) define grouper_home

MKLINK /J "%GRP_STAGE_DIR%\Grouper API Home" %GRP_API_DIR%
MKLINK /J "%GRP_STAGE_DIR%\Grouper WEBAPP Home" %GRP_API_DIR%-ui\dist\grouper

set JAVA_HOME=%GRP_STAGE_DIR%\fake-java-true
set GROUPER_HOME=
set GROUPER_CONF=
cd C:\Temp


echo Test 6A gsh location is webapp, home set to api
set GROUPER_HOME=%GRP_STAGE_DIR%\Grouper API Home
call "%GRP_STAGE_DIR%\Grouper WEBAPP Home\WEB-INF\bin\gsh.bat"
echo exitcode %ERRORLEVEL% should be 0
echo --------

echo Test 6B gsh location is api, home set to webapp
set GROUPER_HOME=%GRP_STAGE_DIR%\Grouper WEBAPP Home\WEB-INF
call "%GRP_STAGE_DIR%\Grouper API Home\bin\gsh.bat"
echo exitcode %ERRORLEVEL% should be 0
echo --------

echo Test 6C home and conf are set differently
set GROUPER_HOME=%GRP_STAGE_DIR%\Grouper API Home
set GROUPER_CONF=%GRP_STAGE_DIR%\Grouper Conf
call "%GRP_STAGE_DIR%\Grouper API Home\bin\gsh.bat"
echo exitcode %ERRORLEVEL% should be 0
echo --------

exit /b 0

rem -----------------

:test7
rem #7) test failures
rem    - grouper_home not a good home (no grouper jar)
rem    - parent of gsh not a good home (no grouper jar)
rem    - grouper_conf not good (missing or not a directory)
rem    - grouper_conf not good (no hibernate properties)

MKDIR %GRP_STAGE_DIR%\fake-home\bin
COPY %GRP_API_DIR%\bin\gsh.bat %GRP_STAGE_DIR%\fake-home\bin\gsh.bat
MKDIR %GRP_STAGE_DIR%\fake-home\conf


set JAVA_HOME=%GRP_STAGE_DIR%\fake-java-true

set GROUPER_HOME=
set GROUPER_CONF=
cd C:\Temp


echo Test 7A grouper_home not a good home (no grouper jar)
set GROUPER_HOME=%GRP_STAGE_DIR%\fake-home
call "%GRP_API_DIR%\bin\gsh.bat"
echo exitcode %ERRORLEVEL% should be 80
echo --------

echo Test 7B parent of gsh not a good home (no grouper jar)
set GROUPER_HOME=
call "%GRP_STAGE_DIR%\fake-home\bin\gsh.bat"
echo exitcode %ERRORLEVEL% should be 81
echo --------

echo Test 7C grouper_conf not good (missing directory)
set GROUPER_CONF=C:\Bogus-path
call "%GRP_API_DIR%\bin\gsh.bat"
echo exitcode %ERRORLEVEL% should be 82
echo --------

echo Test 7D grouper_conf not good (missing hibernate properties)
set GROUPER_CONF=%GRP_STAGE_DIR%\fake-home\conf
call "%GRP_API_DIR%\bin\gsh.bat"
echo exitcode %ERRORLEVEL% should be 82
echo --------

exit /b 0

rem -----------------

rem #8) -initEnv tests
rem    - normal usage, grouper_home and conf reset, based on gsh parent
rem    - same, webapp structure
rem    - normal usage, grouper_home and conf reset, based on gsh parent, passing %2 as conf dir
rem    - same, webapp structure
rem    - calculated grouper_conf not good (no hibernate properties)
rem    - %2 parameter grouper_conf not good (no hibernate properties)
rem    - all of above, spaces in name


:test8

set GROUPER_HOME=
set GROUPER_CONF=

echo Test 8A -initEnv normal usage, grouper_home and conf reset, based on gsh parent
call "%GRP_API_DIR%\bin\gsh.bat" -initEnv
echo exitcode %ERRORLEVEL% should be 0
echo Value of GROUPER_HOME: "%GROUPER_HOME%" (should be api dir)
echo Value of GROUPER_CONF: "%GROUPER_CONF%" (should be api conf dir)
echo --------

set GROUPER_HOME=
set GROUPER_CONF=

echo Test 8B -initEnv same, webapp structure
call "%GRP_UI_DIR%\bin\gsh.bat" -initEnv
echo exitcode %ERRORLEVEL% should be 0
echo Value of GROUPER_HOME: "%GROUPER_HOME%" (should be ui dir)
echo Value of GROUPER_CONF: "%GROUPER_CONF%" (should be ui classes dir)
echo --------

set GROUPER_HOME=
set GROUPER_CONF=

echo Test 8C -initEnv normal usage, grouper_home and conf reset, based on gsh parent, passing %2 as conf dir
call "%GRP_API_DIR%\bin\gsh.bat" -initEnv %GRP_UI_DIR%\classes
echo exitcode %ERRORLEVEL% should be 0
echo Value of GROUPER_HOME: "%GROUPER_HOME%" (should be api dir)
echo Value of GROUPER_CONF: "%GROUPER_CONF%" (should be ui classes dir)
echo --------

set GROUPER_HOME=
set GROUPER_CONF=

echo Test 8D same, webapp structure
call "%GRP_UI_DIR%\bin\gsh.bat" -initEnv %GRP_API_DIR%\conf
echo exitcode %ERRORLEVEL% should be 0
echo Value of GROUPER_HOME: "%GROUPER_HOME%" (should be ui dir)
echo Value of GROUPER_CONF: "%GROUPER_CONF%" (should be api conf dir)
echo --------

set GROUPER_HOME=
set GROUPER_CONF=

echo Test 8E -initEnv plus parent of gsh not a good home (no grouper jar)
call "%GRP_STAGE_DIR%\fake-home\bin\gsh.bat" -initEnv
echo exitcode %ERRORLEVEL% should be 81
echo Value of GROUPER_HOME: "%GROUPER_HOME%" (should be blank)
echo Value of GROUPER_CONF: "%GROUPER_CONF%" (should be blank)
echo --------

set GROUPER_HOME=
set GROUPER_CONF=

echo Test 8F -initEnv plus %2 parameter grouper_conf not good (no hibernate properties)
call "%GRP_API_DIR%\bin\gsh.bat" -initEnv %GRP_STAGE_DIR%\fake-home\conf
echo exitcode %ERRORLEVEL% should be 82
echo Value of GROUPER_HOME: "%GROUPER_HOME%" (should be blank)
echo Value of GROUPER_CONF: "%GROUPER_CONF%" (should be blank)
echo --------

set GROUPER_HOME=
set GROUPER_CONF=

echo Test 8G -initEnv plus spaces in name
call "%GRP_STAGE_DIR%\Grouper API Home\bin\gsh.bat" -initEnv
echo exitcode %ERRORLEVEL% should be 0
echo Value of GROUPER_HOME: "%GROUPER_HOME%" (should be Grouper API Home)
echo Value of GROUPER_CONF: "%GROUPER_CONF%" (should be Grouper API Home/conf)
echo --------

set GROUPER_HOME=
set GROUPER_CONF=

echo Test 8H -initEnv plus spaces in name, passing %2
call "%GRP_STAGE_DIR%\Grouper API Home\bin\gsh.bat" -initEnv "%GRP_STAGE_DIR%\Grouper Conf"
echo exitcode %ERRORLEVEL% should be 0
echo Value of GROUPER_HOME: "%GROUPER_HOME%" (should be 'Grouper API Home')
echo Value of GROUPER_CONF: "%GROUPER_CONF%" (should be 'Grouper Conf')
echo --------

set GROUPER_HOME=
set GROUPER_CONF=

exit /b 0

rem -----------------

rem #9) gsh exit codes
rem    - Java returns false
rem    - Java invalid executable

:test9

set GROUPER_HOME=
set GROUPER_CONF=
cd C:\Temp

echo Test 9A Java returns false
set JAVA_HOME=%GRP_STAGE_DIR%\fake-java-false
call "%GRP_API_DIR%\bin\gsh.bat"
echo exitcode %ERRORLEVEL% should be 1
echo --------

echo Test 9B invalid Java
set JAVA_HOME=%GRP_STAGE_DIR%\fake-java-BOGUS
call "%GRP_API_DIR%\bin\gsh.bat"
echo exitcode %ERRORLEVEL% should be 3
echo --------


exit /b 0
