#!/bin/sh

#
# Because Java runtime issues still baffle me...
#
# $Id: csv2subject.sh,v 1.2 2004-12-05 04:20:24 blair Exp $
#

pathsep=":"
if [ "$OS" ]; then
  if [ ${OS} = "Windows_NT" ]; then
    pathsep=";"
  fi
fi

classpath=.
for f in ../../../java/lib/*.jar; do
  classpath=${classpath}${pathsep}${f}
done
java -classpath ${classpath} csv2subject "$@"
exit $?

