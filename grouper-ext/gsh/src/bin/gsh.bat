@echo off

set JAVA=java

set GSH=com.devclue.grouper.shell.GrouperShell


rem POPULATED AT BUILD

set GROUPER_HOME=@GROUPER_HOME@

set GROUPER_CONF=@GROUPER_CONF@

set GROUPER_EXT_LIB=@GROUPER_EXT_LIB@

set GROUPER_EXT_BIN=@GROUPER_EXT_BIN@

set MEM_START=@GSH_MEM_START@

set MEM_MAX=@GSH_MEM_MAX@

rem POPULATED AT BUILD


%JAVA% -Xms%MEM_START% -Xmx%MEM_MAX% -jar %GROUPER_HOME%/lib/invoker.jar -cpdir %GROUPER_CONF% -cpalljars %GROUPER_HOME%/lib -cpalljars %GROUPER_EXT_LIB% -cpjar %GROUPER_HOME%/dist/lib/grouper.jar %GSH_JVMARGS% %GSH% %*
