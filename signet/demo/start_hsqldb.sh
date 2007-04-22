#!/bin/sh

# $Header: /home/hagleyj/i2mi/signet/demo/start_hsqldb.sh,v 1.1 2007-04-22 01:17:36 ddonn Exp $

# This script starts the HypersonicSQL database in Server mode.
# It is called by the Signet QuickStart demo startup script. Or, it
# may be invoked directly.

# Check if Java is runnable
if [ ! -x $JAVA_HOME/bin/java ]; then
  echo This script requires that the JAVA_HOME environment variable be properly
  echo set. It must name a directory which contains "bin/java".
  echo Currently, JAVA_HOME=$JAVA_HOME
  exit
fi

# Define my variables
. set_env.sh HSQLDB

# Check if hsqldb.jar exists
if [ ! -e $HSQLDB_EXEC ]; then
  echo Could not find the HSQLDB executable: $HSQLDB_EXEC
  exit
fi

# Start HypersonicSQL
$JAVA_HOME/bin/java -classpath $HSQLDB_CP org.hsqldb.Server -database.0 $HSQLDB_DIR/$HSQLDB_FILENAME -dbname.0 $HSQLDB_ALIAS >&1 &
sleep 3s
echo HSQLDB has been started. Use 'shutdown_hsqldb.sh' to shut down.
