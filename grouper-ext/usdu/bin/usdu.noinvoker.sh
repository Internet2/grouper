#!/bin/sh

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

# Preserve the user's $CLASSPATH
CP=${CLASSPATH}

# Append Grouper .jar or build classes
# TODO 20070320 this is fragile
grouper_jar=${GROUPER_HOME}/dist/lib/grouper.jar
if [ -f ${grouper_jar} ]; then
  CP=${CP}:${grouper_jar}
else
  CP=${CP}:${GROUPER_HOME}/build/grouper
fi

# Append Grouper's configuration
CP=${CP}:${GROUPER_HOME}/conf

# Append Grouper's 3rd party libraries
for f in ${GROUPER_HOME}/lib/*.jar; do CP=${CP}:${f}; done

# Append usdu .jar  
CP=${CP}:${GROUPER_EXT_LIB}/usdu-${USDU_VERSION}.jar
# Append usdu's 3rd party libs
CP=${CP}:${GROUPER_EXT_LIB}/commons-cli-1.1.jar

${JAVA} -classpath ${CP} -Xms${MEM_START} -Xmx${MEM_MAX} ${USDU_JVMARGS} ${USDU} "$@"
exit $?

