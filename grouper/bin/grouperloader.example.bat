@echo off

rem if not in bin dir, but in grouper_home, then cd to bin
IF EXIST bin\grouperloader.bat cd bin

IF NOT EXIST grouperloader.bat echo Cant find file grouperloader.bat.  Are you in the grouper_home dir or grouper_home\bin dir?
IF NOT EXIST grouperloader.bat exit /b 1

set JAVA=java

set GROUPERLOADER=edu.internet2.middleware.grouper.app.loader.GrouperLoader

set GROUPER_HOME=..

set GROUPER_CONF=../conf

set MEM_START=64m

set MEM_MAX=256m

%JAVA% -Xms%MEM_START% -Xmx%MEM_MAX% -jar %GROUPER_HOME%/lib/invoker.jar -cpdir %GROUPER_CONF% -cpalljars %GROUPER_HOME%/lib -cpjar %GROUPER_HOME%/dist/lib/grouper.jar %GROUPERLOADER_JVMARGS% %GROUPERLOADER% %*
