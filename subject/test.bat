@echo off

rem -----------------------------------------------------------------------------
rem Script for command line util to test sources.xml config file.
rem
rem Environment Variable Prequisites
rem
rem   JAVA_HOME     Must point to your JDK.
rem -----------------------------------------------------------------------------

set JAVA_HOME=\j2sdk1.4.2_05

set CLASSPATH=build
set CLASSPATH=conf;%CLASSPATH%

for %%f in (lib\*.jar) do call cpappend.bat %%f

echo "Using classpath: %CLASSPATH%"

%JAVA_HOME%\bin\java -cp %CLASSPATH% edu.internet2.middleware.subject.provider.SourceManager %1
