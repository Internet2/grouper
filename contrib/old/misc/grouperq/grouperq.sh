#!/bin/sh

#
# Because Java runtime issues still baffle me...
#
# $Id: grouperq.sh,v 1.2 2004-12-05 04:20:25 blair Exp $
#

pathsep=":"
if [ "$OS" ]; then
  if [ ${OS} = "Windows_NT" ]; then
    pathsep=";"
  fi
fi

classpath=build:conf
for f in java/lib/*.jar; do
  classpath=${classpath}${pathsep}${f}
done
java -classpath ${classpath} grouperq "$@"
exit $?

