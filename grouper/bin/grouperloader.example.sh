#!/bin/sh

# if not in bin dir, but in grouper_home, then cd to bin
[ -e grouperloader.example.sh ] && cd bin

[ -e grouperloader.sh ] || echo "Cant find grouperloader.sh.  Are you in the grouper_home dir or grouper_home\bin dir?"
[ -e grouperloader.sh ] || exit 1

JAVA=java

GROUPERLOADER=edu.internet2.middleware.grouper.app.loader.GrouperLoader

GROUPER_HOME=..

GROUPER_CONF=../conf

MEM_START=64m

MEM_MAX=256m

${JAVA} -Xms${MEM_START} -Xmx${MEM_MAX} -jar ${GROUPER_HOME}/lib/grouper/invoker.jar -cpdir ${GROUPER_CONF} -cpalljars ${GROUPER_HOME}/lib -cpjar ${GROUPER_HOME}/dist/lib/grouper.jar ${GROUPERLOADER_JVMARGS} ${GROUPERLOADER} "$@"

exit $?
