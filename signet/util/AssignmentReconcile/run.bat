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
set CLASSPATH=%BASEDIR%\config;%BASEDIR%\lib\ant-1.5.3.jar;%BASEDIR%\lib\ant-optional-1.5.3.jar;%BASEDIR%\lib\antlr.jar;%BASEDIR%\lib\apache.license.txt;%BASEDIR%\lib\c3p0-0.8.4.5.jar;%BASEDIR%\lib\c3p0.license.txt;%BASEDIR%\lib\cglib-2.0.2.jar;%BASEDIR%\lib\cglib-full-2.0.2.jar;%BASEDIR%\lib\commons-beanutils.jar;%BASEDIR%\lib\commons-collections-3.1.jar;%BASEDIR%\lib\commons-dbcp-1.2.1.jar;%BASEDIR%\lib\commons-digester.jar;%BASEDIR%\lib\commons-fileupload.jar;%BASEDIR%\lib\commons-lang-1.0.1.jar;%BASEDIR%\lib\commons-logging-1.0.4.jar;%BASEDIR%\lib\commons-pool-1.2.jar;%BASEDIR%\lib\commons-validator.jar;%BASEDIR%\lib\concurrent-1.3.3.jar;%BASEDIR%\lib\connector.jar;%BASEDIR%\lib\connector.licence.txt;%BASEDIR%\lib\dom4j-1.4.jar;%BASEDIR%\lib\ehcache-0.9.jar;%BASEDIR%\lib\hibernate-tools.jar;%BASEDIR%\lib\hibernate2.jar;%BASEDIR%\lib\hsqldb.jar;%BASEDIR%\lib\jaas.jar;%BASEDIR%\lib\jaas.licence.txt;%BASEDIR%\lib\jakarta-oro.jar;%BASEDIR%\lib\jboss-cache.jar;%BASEDIR%\lib\jboss-common.jar;%BASEDIR%\lib\jboss-jmx.jar;%BASEDIR%\lib\jboss-system.jar;%BASEDIR%\lib\jconn2.jar;%BASEDIR%\lib\jcs-1.0-dev.jar;%BASEDIR%\lib\jdbc2_0-stdext.jar;%BASEDIR%\lib\jdbc2_0-stdext.licence.txt;%BASEDIR%\lib\jdom.jar;%BASEDIR%\lib\jdom.license.txt;%BASEDIR%\lib\jgroups-2.2.3.jar;%BASEDIR%\lib\jta.jar;%BASEDIR%\lib\jTDS2.jar;%BASEDIR%\lib\junit-3.8.1.jar;%BASEDIR%\lib\log4j-1.2.8.jar;%BASEDIR%\lib\odmg-3.0.jar;%BASEDIR%\lib\oscache-2.0.jar;%BASEDIR%\lib\proxool-0.8.3.jar;%BASEDIR%\lib\servlet-api.jar;%BASEDIR%\lib\signet-1.0.jar;%BASEDIR%\lib\signet-ui-1.0.jar;%BASEDIR%\lib\signet-util-1.0.jar;%BASEDIR%\lib\stax-api-1.0.jar;%BASEDIR%\lib\struts.jar;%BASEDIR%\lib\subject-0.1.jar;%BASEDIR%\lib\swarmcache-1.0rc2.jar;%BASEDIR%\lib\velocity-1.3.1.jar;%BASEDIR%\lib\wstx.jar;%BASEDIR%\lib\xalan-2.4.0.jar;%BASEDIR%\lib\xerces-2.4.0.jar;%BASEDIR%\lib\xml-apis.jar

%JAVA_HOME%/bin/java -cp %CLASSPATH% edu.internet2.middleware.signet.util.AssignmentReconcile %1

:end
