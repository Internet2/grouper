#!/bin/sh
# $Id: gsh.sh,v 1.1 2006-06-23 17:30:09 blair Exp $

#
# TODO
# * I should make it configurable whether one wants to use .jar or
#   .class files for both gsh and Grouper
#

# Populated at build
GROUPER_HOME=@GROUPER_HOME@
GSH_HOME=@GSH_HOME@

# The user's $CLASSPATH
CP=${CLASSPATH}
# Append Grouper .jar
#CP=${CP}:${GROUPER_HOME}/dist/lib/grouper.jar
# Append Grouper's build classes
CP=${CP}:${GROUPER_HOME}/build/grouper
# Append Grouper's configuration
CP=${CP}:${GROUPER_HOME}/conf
# Append Grouper's 3rd party library .jar
#CP=${CP}:${GROUPER_HOME}/dist/lib/grouper-lib.jar
# Append Grouper's 3rd party libs
for f in ${GROUPER_HOME}/lib/*.jar; do CP=${CP}:${f}; done
# Append gsh .jar
#CP=${CP}:${GSH_HOME}/dist/gsh.jar
# Append gsh's build classes
CP=${CP}:${GSH_HOME}/build/gsh
# Append gsh's 3rd party libs
for f in ${GSH_HOME}/lib/*.jar; do CP=${CP}:${f}; done

java -classpath ${CP} com.devclue.grouper.shell.GrouperShell "$@"
exit $?

