#!/bin/sh
# -----------------------------------------------------------------------------
# Start Script for the Signet demo system
#
# $Id: startup.sh,v 1.2 2005-12-21 02:02:07 acohen Exp $
# -----------------------------------------------------------------------------

TOMCAT_BIN_DIR="jakarta-tomcat-5.0.28/bin"
HSQLDB_BIN_DIR="hsqldb/bin"

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
PRGDIR="$PRGDIR/$TOMCAT_BIN_DIR"
EXECUTABLE=startup.sh

# Check that target executable exists
if [ ! -x "$PRGDIR"/"$EXECUTABLE" ]; then
  echo "Cannot find $PRGDIR/$EXECUTABLE"
  echo "This file is needed to run this program"
  exit 1
fi

exec "$PRGDIR"/"$EXECUTABLE"
