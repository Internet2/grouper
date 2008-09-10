@echo off

rem if not in bin dir, but in grouper_home, then cd to bin
IF EXIST bin\findbadmemberships.bat cd bin

IF NOT EXIST findbadmemberships.bat echo Cant find file findbadmemberships.bat.  Are you in the grouper_home dir or grouper_home\bin dir?
IF NOT EXIST findbadmemberships.bat exit /b 1

set JAVA=java

set APP=edu.internet2.middleware.grouper.FindBadMemberships

set GROUPER_HOME=..

set GROUPER_CONF=../conf

set MEM_START=256m

set MEM_MAX=768m

%JAVA% -Xms%MEM_START% -Xmx%MEM_MAX% -jar %GROUPER_HOME%/lib/invoker.jar -cpdir %GROUPER_CONF% -cpalljars %GROUPER_HOME%/lib -cpjar %GROUPER_HOME%/dist/lib/grouper.jar %APP% %*
