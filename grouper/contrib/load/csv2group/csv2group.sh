#!/bin/sh

#
# Because Java runtime issues still baffle me...
#
# $Id: csv2group.sh,v 1.1 2004-12-04 02:12:13 blair Exp $
#

classpath=build:conf
for f in java/lib/*.jar; do
  classpath=${classpath}:${f}
done
java -classpath ${classpath} csv2group "$@"
exit $?

