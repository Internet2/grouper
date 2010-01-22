@echo off
rem Run ldappc on Windows systems.
rem Set APP_HOME to the base directory for your ldappc distribution
rem and you're ready to go.
rem
rem $Id: ldappc.bat,v 1.1 2008-04-11 06:51:47 khuxtable Exp $

set APP_HOME=.

rem Include main configuration files
rem Append libs
set LDAPPC_CP=%CLASSPATH%
set LDAPPC_CP=%LDAPPC_CP%;%APP_HOME%/conf
for f in ${APP_HOME}/lib/*.jar; do CP=${CP}:${f}; done

rem Append Grouper's 3rd party libs
for %%f in (%APP_HOME%\lib\*.jar) do set LDAPPC_CP=%LDAPPC_CP%;%f

java -classpath %LDAPPC_CP% -Xmx256m edu.internet2.middleware.ldappc.Ldappc %*
set LDAPPC_CP=
:end
