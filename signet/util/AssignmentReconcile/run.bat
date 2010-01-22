@echo off
if "%OS%" == "Windows_NT" setlocal

rem -----------------------------------------------------------------------------
rem Script for command line utilility to reconcile assignments for status change
rem
rem Environment Variable Prequisites
rem
rem    JAVA_HOME     Must point to your JDK.
rem -----------------------------------------------------------------------------

if exist "%JAVA_HOME%\bin\java.exe" goto okJavaHome
echo This script requires that the JAVA_HOME environment variable be properly
echo set. That means that it must name a directory which contains
echo "bin\java.exe".
goto end

:okJavaHome
set BASEDIR=..\..
set SIGNET_LIBS=%BASEDIR%\lib

set CLASSPATH=%BASEDIR%\config;%SIGNET_LIBS%\ant-1.5.3.jar;%SIGNET_LIBS%\ant-optional-1.5.3.jar;%SIGNET_LIBS%\antlr.jar;%SIGNET_LIBS%\apache.license.txt;%SIGNET_LIBS%\c3p0-0.8.4.5.jar;%SIGNET_LIBS%\c3p0.license.txt;%SIGNET_LIBS%\cglib-2.0.2.jar;%SIGNET_LIBS%\cglib-full-2.0.2.jar;%SIGNET_LIBS%\commons-beanutils.jar;%SIGNET_LIBS%\commons-collections-3.1.jar;%SIGNET_LIBS%\commons-dbcp-1.2.1.jar;%SIGNET_LIBS%\commons-digester.jar;%SIGNET_LIBS%\commons-fileupload.jar;%SIGNET_LIBS%\commons-lang-1.0.1.jar;%SIGNET_LIBS%\commons-logging-1.0.4.jar;%SIGNET_LIBS%\commons-pool-1.2.jar;%SIGNET_LIBS%\commons-validator.jar;%SIGNET_LIBS%\concurrent-1.3.3.jar;%SIGNET_LIBS%\connector.jar;%SIGNET_LIBS%\connector.licence.txt;%SIGNET_LIBS%\dom4j-1.4.jar;%SIGNET_LIBS%\ehcache-0.9.jar;%SIGNET_LIBS%\hibernate-tools.jar;%SIGNET_LIBS%\hibernate2.jar;%SIGNET_LIBS%\hsqldb.jar;%SIGNET_LIBS%\jaas.jar;%SIGNET_LIBS%\jaas.licence.txt;%SIGNET_LIBS%\jakarta-oro.jar;%SIGNET_LIBS%\jboss-cache.jar;%SIGNET_LIBS%\jboss-common.jar;%SIGNET_LIBS%\jboss-jmx.jar;%SIGNET_LIBS%\jboss-system.jar;%SIGNET_LIBS%\jconn2.jar;%SIGNET_LIBS%\jcs-1.0-dev.jar;%SIGNET_LIBS%\jdbc2_0-stdext.jar;%SIGNET_LIBS%\jdbc2_0-stdext.licence.txt;%SIGNET_LIBS%\jdom.jar;%SIGNET_LIBS%\jdom.license.txt;%SIGNET_LIBS%\jgroups-2.2.3.jar;%SIGNET_LIBS%\jta.jar;%SIGNET_LIBS%\jTDS2.jar;%SIGNET_LIBS%\junit-3.8.1.jar;%SIGNET_LIBS%\log4j-1.2.8.jar;%SIGNET_LIBS%\odmg-3.0.jar;%SIGNET_LIBS%\oscache-2.0.jar;%SIGNET_LIBS%\proxool-0.8.3.jar;%SIGNET_LIBS%\servlet-api.jar;%SIGNET_LIBS%\signet-1.0.1.jar;%SIGNET_LIBS%\signet-ui-1.0.1.jar;%SIGNET_LIBS%\signet-util-1.0.1.jar;%SIGNET_LIBS%\stax-api-1.0.jar;%SIGNET_LIBS%\struts.jar;%SIGNET_LIBS%\subject-0.1.jar;%SIGNET_LIBS%\swarmcache-1.0rc2.jar;%SIGNET_LIBS%\velocity-1.3.1.jar;%SIGNET_LIBS%\wstx.jar;%SIGNET_LIBS%\xalan-2.4.0.jar;%SIGNET_LIBS%\xerces-2.4.0.jar;%SIGNET_LIBS%\xml-apis.jar;%CLASSPATH%

%JAVA_HOME%/bin/java -cp %CLASSPATH% edu.internet2.middleware.signet.util.AssignmentReconcile %1

:end
