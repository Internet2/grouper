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

# Append gsh .jar  
CP=${CP}:${GROUPER_EXT_LIB}/gsh-${GSH_VERSION}.jar
# Append gsh's 3rd party libs
CP=${CP}:${GROUPER_EXT_LIB}/bsh-2.0b4.jar

${JAVA} -classpath ${CP} -Xms${MEM_START} -Xmx${MEM_MAX} ${GSH_JVMARGS} ${GSH} "$@"
exit $?

