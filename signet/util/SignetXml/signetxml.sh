#!/bin/sh

# -----------------------------------------------------------------------------
# Script/command line utility to create or read SignetXml
#
# signetxml -h for command help
#
# Environment Variable Prequisites
#
#    JAVA_HOME     Must point to your JDK.
# -----------------------------------------------------------------------------

BASEDIR=..
CLASSPATH=$BASEDIR/config

for file in `ls $BASEDIR/lib/*.jar`;
do
if [ "$CLASSPATH" != "" ]; then
   CLASSPATH=${CLASSPATH}:$file
else
   CLASSPATH=$file
fi
done

$JAVA_HOME/bin/java -cp $CLASSPATH edu.internet2.middleware.signet.util.xml.SignetXml $1 $2 $3 $4 $5 $6 $7 $8 $9
