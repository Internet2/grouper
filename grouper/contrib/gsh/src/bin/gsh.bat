rem Author: Gary Brown
rem $Id: gsh.bat,v 1.2 2006-08-04 16:23:05 blair Exp $

@echo off
rem if "%OS%" == "Windows_NT" setlocal
if not "%1" == "acp" goto execute

set GSH_CP=%GSH_CP%;%2

goto end

:execute
rem Populated at build
set GROUPER_HOME=@GROUPER_HOME@
set GSH_HOME=@GSH_HOME@

rem The user's CLASSPATH
set GSH_CP=%CLASSPATH%
rem Append Grouper .jar

rem Append Grouper's build classes
set GSH_CP=%GSH_CP%;%GROUPER_HOME%/build/grouper
rem Append Grouper's configuration
set GSH_CP=%GSH_CP%;%GROUPER_HOME%/conf


rem Append Grouper's 3rd party libs
for %%f in (%GROUPER_HOME%\lib\*.jar) do call %GSH_HOME%\bin\gsh acp %%f
rem Append gsh .jar

rem Append gsh's build classes
set GSH_CP=%GSH_CP%;%GSH_HOME%/build/gsh
rem Append gsh's 3rd party libs
for %%f in (%GSH_HOME%\lib\*.jar) do call %GSH_HOME%\bin\gsh acp %%f

java -classpath %GSH_CP% %GSH_JVMARGS% com.devclue.grouper.shell.GrouperShell %*
set GSH_CP=
:end
