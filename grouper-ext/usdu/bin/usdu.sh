#!/bin/sh

# Run Grouper's Unresolvable Subject Deletion Utility
#
# based on gsh.sh
#

JAVA=java
USDU=edu.internet2.middleware.grouper.USDU

# POPULATED AT BUILD
GROUPER_CONF=@GROUPER_CONF@
GROUPER_EXT_LIB=@GROUPER_EXT_LIB@
GROUPER_HOME=@GROUPER_HOME@
USDU_VERSION=@USDU_VERSION@
MEM_START=@USDU_MEM_START@
MEM_MAX=@USDU_MEM_MAX@
# POPULATED AT BUILD

${JAVA} -Xms${MEM_START} -Xmx${MEM_MAX} -jar ${GROUPER_HOME}/lib/invoker.jar -cpdir ${GROUPER_CONF} -cpalljars ${GROUPER_HOME}/lib -cpalljars ${GROUPER_EXT_LIB} -cpjar ${GROUPER_HOME}/dist/lib/grouper.jar ${USDU_JVMARGS} ${USDU} "$@"

exit $?