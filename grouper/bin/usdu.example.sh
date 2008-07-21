#!/bin/sh

# Run Grouper's Unresolvable Subject Deletion Utility

# if not in bin dir, but in grouper_home, then cd to bin
[ -e bin/grouperloader.example.sh ] && cd bin

JAVA=java

USDU=edu.internet2.middleware.grouper.app.usdu.USDU

GROUPER_HOME=..

GROUPER_CONF=../conf

MEM_START=64m

MEM_MAX=256m

${JAVA} -Xms${MEM_START} -Xmx${MEM_MAX} -jar ${GROUPER_HOME}/lib/invoker.jar -cpdir ${GROUPER_CONF} -cpalljars ${GROUPER_HOME}/lib -cpjar ${GROUPER_HOME}/dist/lib/grouper.jar ${USDU_JVMARGS} ${USDU} "$@"

exit $?
