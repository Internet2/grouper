#!/bin/sh
#
# Initialize and populate HSQLDB database 
# $Id: hsqldb-init.sh,v 1.1 2004-07-26 17:10:51 blair Exp $
#

class=org.hsqldb.util.DatabaseManager
classpath=../java/lib/hsqldb.jar
url=jdbc:hsqldb:grouper

if [ -d build ]; then
  (
    cd build
    # Clear out any old cruft that might be lingering.
    for f in grouper.*
    do
      [ -f ${f} ] && rm ${f}
    done
    # Load database schema
    java -classpath ${classpath} ${class} -url ${url} -script ../sql/hsqldb.sql 
    # Load base database data
    java -classpath ${classpath} ${class} -url ${url} -script ../sql/base.sql 
    cd ..
  )
else
  echo >&2 "Directory 'build' does not exist.  Run 'ant build'."
  exit 73
fi

exit $?

