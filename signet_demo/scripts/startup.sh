#!/bin/sh

# Make sure we're not using some other, pre-existing Tomcat installation
# on this machine.
unset CATALINA_HOME

if [ ! -x "$JAVA_HOME"/bin/java ]; then
  echo This script requires that the JAVA_HOME environment variable be
properly
  echo set. That means that it must name a directory which contains
  echo "bin/java".
  exit 1
fi

TOMCAT_DIR=jakarta-tomcat-5.0.28

if [ ! -x "$TOMCAT_DIR" ]; then
  echo This script must be run from the signet_demo home directory. That\'s
the
  echo "directory that contains the $TOMCAT_DIR and $HSQLDB_DIR
directories."
  exit 1
fi

HSQLDB_DIR=hsqldb

if [ ! -x "$HSQLDB_DIR" ]; then
  echo This script must be run from the signet_demo home directory. That\'s
the
  echo "directory that contains the $TOMCAT_DIR and $HSQLDB_DIR
directories."
  exit 1
fi

TOMCAT_EXECUTABLE_DIR="$TOMCAT_DIR"/bin
TOMCAT_EXECUTABLE=startup.sh

if [ ! -x "$TOMCAT_EXECUTABLE_DIR"/"$TOMCAT_EXECUTABLE" ]; then
  echo Cannot find "$TOMCAT_EXECUTABLE_DIR"/"$TOMCAT_EXECUTABLE"
  echo This file is needed to run this program.
  exit 1
fi

HSQLDB_EXECUTABLE_DIR="$HSQLDB_DIR"/lib
HSQLDB_EXECUTABLE=hsqldb.jar

if [ ! -x "$HSQLDB_EXECUTABLE_DIR"/"$HSQLDB_EXECUTABLE" ]; then
  echo Cannot find "$HSQLDB_EXECUTABLE_DIR"/"$HSQLDB_EXECUTABLE"
  echo This file is needed to run this program.
  exit 1
fi

pushd $TOMCAT_EXECUTABLE_DIR
./"$TOMCAT_EXECUTABLE"
popd

cd $HSQLDB_EXECUTABLE_DIR
"$JAVA_HOME"/bin/java -classpath hsqldb.jar org.hsqldb.Server -database.0
mydb -dbname.0 xdb >! hsqldb.out &

