#!/bin/sh

#
# Because Java runtime issues still baffle me...
#
# To build `grouperq', run the following from the top-level of the
# Grouper distribution:
#
#   % ant -f contrib/misc/grouperq/build.xml
#
# To run `grouper', run this script from the top-level of the Grouper
# distribution with any desired command line arguments:
#
#   % contrib/misq/grouperq/grouperq.sh -h
#
# $Id: grouperq.sh,v 1.1 2004-11-30 06:55:10 blair Exp $
#

classpath=build:conf
for f in java/lib/*.jar; do
  classpath=${classpath}:${f}
done
java -classpath ${classpath} grouperq "$@"
exit $?

