#!/bin/sh
# $Id: gsh.sh,v 1.1 2006-06-23 17:30:09 blair Exp $

#
# TODO
# * I should make it configurable whether one wants to use .jar or
#   .class files for both cdg and Grouper
#

# Populated at build
GROUPER_HOME=@GROUPER_HOME@
CDG_HOME=@CDG_HOME@

# The user's $CLASSPATH
CP=${CLASSPATH}
# Append Grouper's build classes
CP=${CP}:${GROUPER_HOME}/build/grouper
# Append Grouper's configuration
CP=${CP}:${GROUPER_HOME}/conf
# Append Grouper's 3rd party libs
for f in ${GROUPER_HOME}/lib/*.jar; do CP=${CP}:${f}; done
# Append cdg's build classes
CP=${CP}:${CDG_HOME}/build/cdg
# Append cdg's 3rd party libs
for f in ${CDG_HOME}/lib/*.jar; do CP=${CP}:${f}; done

java -classpath ${CP} com.devclue.grouper.shell.GrouperShell "$@"
exit $?

