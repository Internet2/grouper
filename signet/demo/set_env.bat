REM $Header: /home/hagleyj/i2mi/signet/demo/set_env.bat,v 1.1 2007-04-18 00:11:31 ddonn Exp $

REM Setup environment for Signet Demo

SET UTIL_DIR=..\util

IF "%1" == "HSQLDB" GOTO setDB
IF "%1" == "DEMO" GOTO setDemo
GOTO errorEnd

:setDB
REM   HSQLDB settings
SET HSQLDB_DIR=..\data
SET HSQLDB_FILENAME=SignetQsDb
SET HSQLDB_ALIAS=%HSQLDB_FILENAME%
SET HSQLDB_EXEC=%UTIL_DIR%\lib\hsqldb.jar
SET HSQLDB_CFG_DIR=%UTIL_DIR%\config
SET HSQLDB_RC_FILE=%HSQLDB_CFG_DIR%\sqltool.rc
SET HSQLDB_CP=%HSQLDB_EXEC%;%HSQLDB_CFG_DIR%
SET HSQLDB_URLID=localhost-sa
GOTO end

:setDemo
REM   Tomcat settings
SET CATALINA_HOME=..\tomcat
SET TOMCAT_START=%CATALINA_HOME%\bin\startup.bat
SET TOMCAT_SHUTDOWN=%CATALINA_HOME%\bin\shutdown.bat
GOTO end

:errorEnd
ECHO set_env.bat - Unknown parameter: "%1"

:end
