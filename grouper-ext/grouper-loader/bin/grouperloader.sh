#!/bin/sh

# Run Grouper's Unresolvable Subject Deletion Utility
#
# based on gsh.sh
#

JAVA=java
GROUPERLOADER=edu.internet2.middleware.grouper.loader.GrouperLoader

# POPULATED AT BUILD
GROUPER_CONF=@GROUPER_CONF@
GROUPER_EXT_LIB=@GROUPER_EXT_LIB@
GROUPER_EXT_CONF=@GROUPER_EXT_CONF@
GROUPER_HOME=@GROUPER_HOME@
MEM_START=@GROUPERLOADER_MEM_START@
MEM_MAX=@GROUPERLOADER_MEM_MAX@
# POPULATED AT BUILD

${JAVA} -Xms${MEM_START} -Xmx${MEM_MAX} -jar ${GROUPER_HOME}/lib/invoker.jar -cpdir ${GROUPER_CONF} -cpdir ${GROUPER_EXT_CONF} -cpalljars ${GROUPER_HOME}/lib -cpalljars ${GROUPER_EXT_LIB} -cpjar ${GROUPER_HOME}/dist/lib/grouper.jar ${GROUPERLOADER_JVMARGS} ${GROUPERLOADER} "$@"

exit $?