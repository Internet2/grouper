#!/bin/sh

#
# TODO
# * I should make it configurable whether one wants to use .jar or
#   .class files for both gsh and Grouper
# * Add cli option support
#

JAVA=java
GSH=com.devclue.grouper.shell.GrouperShell

# POPULATED AT BUILD
GROUPER_CONF=@GROUPER_CONF@
GROUPER_EXT_LIB=@GROUPER_EXT_LIB@
GROUPER_HOME=@GROUPER_HOME@
GSH_HOME=@GSH_HOME@
GSH_VERSION=@GSH_VERSION@
MEM_START=@GSH_MEM_START@
MEM_MAX=@GSH_MEM_MAX@
# POPULATED AT BUILD

${JAVA} -Xms${MEM_START} -Xmx${MEM_MAX} -jar ${GROUPER_HOME}/lib/invoker.jar -cpdir ${GROUPER_CONF} -cpalljars ${GROUPER_HOME}/lib -cpalljars ${GROUPER_EXT_LIB} -cpjar ${GROUPER_HOME}/dist/lib/grouper.jar ${GSH_JVMARGS} ${GSH} "$@"

exit $?

