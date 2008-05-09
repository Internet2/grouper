@echo off

%rem Author: Gary Brown

rem $Id: usdu.bat,v 1.3 2008-05-09 03:26:41 tzeller Exp $


rem if "%OS%" == "Windows_NT" setlocal

if not "%1" == "acp" goto execute

goto end

:execute


set JAVA=java

set USDU=edu.internet2.middleware.grouper.USDU


rem POPULATED AT BUILD

set GROUPER_HOME=@GROUPER_HOME@

set GROUPER_CONF=@GROUPER_CONF@

set GROUPER_EXT_LIB=@GROUPER_EXT_LIB@

set GROUPER_EXT_BIN=@GROUPER_EXT_BIN@

set MEM_START=@USDU_MEM_START@

set MEM_MAX=@USDU_MEM_MAX@

rem POPULATED AT BUILD


%JAVA% -Xms%MEM_START% -Xmx%MEM_MAX% -jar %GROUPER_HOME%/lib/invoker.jar -cpdir %GROUPER_CONF% -cpalljars %GROUPER_HOME%/lib -cpalljars %GROUPER_EXT_LIB% -cpjar %GROUPER_HOME%/dist/lib/grouper.jar %USDU_JVMARGS% %USDU% %*

:end