#!/bin/sh

#
# Because Java runtime issues still baffle me...
#
# $Id: grouperq.sh,v 1.3 2004-12-05 16:31:18 tbarton Exp $
#

pathsep=":"
if [ "$OS" ]; then
  if [ ${OS} = "Windows_NT" ]; then
    pathsep=";"
  fi
fi

cd ../../..
classpath=build${pathsep}conf
for f in java/lib/*.jar; do
  classpath=${classpath}${pathsep}${f}
done
java -classpath ${classpath} grouperq "$@"
exit $?

