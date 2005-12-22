#!/bin/sh
# -----------------------------------------------------------------------------
# Start Script for the Signet demo system
#
# $Id: startup.sh,v 1.3 2005-12-22 13:16:56 acohen Exp $
# -----------------------------------------------------------------------------

TOMCAT_DIR="jakarta-tomcat-5.0.28"
HSQLDB_DIR="hsqldb"

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
 
PRGDIR=`dirname "$PRG"`
PRGDIR="$PRGDIR/$TOMCAT_DIR/bin"
EXECUTABLE=startup.sh

# Check that target executable exists
if [ ! -x "$PRGDIR"/"$EXECUTABLE" ]; then
  echo "Cannot find $PRGDIR/$EXECUTABLE"
  echo "This file is needed to run this program"
  exit 1
fi

exec "$PRGDIR"/"$EXECUTABLE"

PRGDIR=`dirname "$PRG"`
PRGDIR="$PRGDIR/$HSQLDB_DIR/lib"
EXECUTABLE=hsqldb.jar

# Check that target executable exists
if [ ! -x "$PRGDIR"/"$EXECUTABLE" ]; then
  echo "Cannot find $PRGDIR/$EXECUTABLE"
  echo "This file is needed to run this program"
  exit 1
fi

cd "$PRGDIR"
exec "$JAVA_HOME/bin/java -classpath hsqldb.jar org.hsqldb.Server -database.0 mydb -dbname.0 xdb &
