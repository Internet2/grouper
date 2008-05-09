%rem Author: Gary Brown

rem $Id: usdu.bat,v 1.2 2008-05-09 03:00:21 tzeller Exp $


@echo off

rem if "%OS%" == "Windows_NT" setlocal

if not "%1" == "acp" goto execute

goto end

:execute


rem POPULATED AT BUILD

set GROUPER_HOME=@GROUPER_HOME@

set GSH_HOME=@GSH_HOME@

set GROUPER_EXT_LIB=@GROUPER_EXT_LIB@

set GROUPER_EXT_BIN=@GROUPER_EXT_BIN@

set MEM_START=@GSH_MEM_START@

set MEM_MAX=@GSH_MEM_MAX@

rem POPULATED AT BUILD


java -Xms%MEM_START% -Xmx%MEM_MAX% -jar %GROUPER_HOME%/lib/invoker.jar -cpdir %GROUPER_CONF% -cpalljars %GROUPER_HOME%/lib -cpalljars %GROUPER_EXT_LIB% -cpjar %GROUPER_HOME%/dist/lib/grouper.jar %GSH_JVMARGS% com.devclue.grouper.shell.GrouperShell %*

:end