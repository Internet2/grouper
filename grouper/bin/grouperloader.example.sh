#!/bin/sh

# if not in bin dir, but in grouper_home, then cd to bin
[ -e bin/grouperloader.example.sh ] && cd bin

JAVA=java

GROUPERLOADER=edu.internet2.middleware.grouper.app.loader.GrouperLoader

GROUPER_HOME=..

GROUPER_CONF=../conf

MEM_START=64m

MEM_MAX=256m

${JAVA} -Xms${MEM_START} -Xmx${MEM_MAX} -jar ${GROUPER_HOME}/lib/invoker.jar -cpdir ${GROUPER_CONF} -cpalljars ${GROUPER_HOME}/lib -cpjar ${GROUPER_HOME}/dist/lib/grouper.jar ${GROUPERLOADER_JVMARGS} ${GROUPERLOADER} "$@"

exit $?
