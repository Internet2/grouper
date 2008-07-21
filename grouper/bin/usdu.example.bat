@echo off

rem if not in bin dir, but in grouper_home, then cd to bin
IF EXIST bin\grouperloader.example.bat cd bin

set JAVA=java

set USDU=edu.internet2.middleware.grouper.app.usdu.USDU

set GROUPER_HOME=..

set GROUPER_CONF=../conf

set MEM_START=64m

set MEM_MAX=256m

%JAVA% -Xms%MEM_START% -Xmx%MEM_MAX% -jar %GROUPER_HOME%/lib/invoker.jar -cpdir %GROUPER_CONF% -cpalljars %GROUPER_HOME%/lib -cpjar %GROUPER_HOME%/dist/lib/grouper.jar %USDU_JVMARGS% %USDU% %*
