#!/bin/sh

#
# Because Java runtime issues still baffle me...
#
# $Id: csv2subject.sh,v 1.1 2004-12-03 16:26:10 blair Exp $
#

classpath=.
for f in ../../../java/lib/*.jar; do
  classpath=${classpath}:${f}
done
java -classpath ${classpath} csv2subject "$@"
exit $?

