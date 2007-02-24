REM $Header: /home/hagleyj/i2mi/signet/util/dist-signet_cp.bat,v 1.3 2007-02-24 02:11:32 ddonn Exp $
REM
REM This file is intended to be called from each of the run.bat files in
REM the subdirectories of util. DOS Batch does not provide a dynamic means of
REM generating a classpath as done in each run.sh. 
REM
REM You may need to change SIGNET_LIBS to point to YOUR installation!
REM
set SIGNET_LIBS=..\lib
set CLASSPATH=%CLASSPATH%;..\config
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\antlr-2.7.6.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\asm-attrs.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\asm.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\cglib-2.1.3.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\commons-beanutils-1.7.0.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\commons-collections-3.2.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\commons-dbcp-1.2.1.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\commons-digester-1.7.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\commons-lang-1.0.1.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\commons-logging-1.1.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\commons-pool-1.3.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\dom4j-1.6.1.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\ehcache-1.2.3.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\hibernate3.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\hsqldb.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\jdbc2_0-stdext.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\jdom.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\jta.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\jTDS2.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\junit-4.1.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\log4j-1.2.11.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\postgresql-8.1-404.jdbc2.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\postgresql-8.1-404.jdbc2ee.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\postgresql-8.1-404.jdbc3.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\servlet-api.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\signet-api-1.1_TMP08.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\signet-ui-1.1_TMP08.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\signet-util-1.1_TMP08.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\stax-api-1.0.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\struts.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\subject-0.2.2-cvs.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\wstx.jar
set CLASSPATH=%CLASSPATH%;%SIGNET_LIBS%\xml-apis.jar
