#!/bin/sh

# -----------------------------------------------------------------------------
# Script for command line utilility to assign System Administrator proxy
#
# Takes two arguments
#    1 - Action to take -- grant | revoke | list
#    2 - For grant or revoke, a login ID from the subject table
#
# Environment Variable Prequisites
#
#    JAVA_HOME     Must point to your JDK.
# -----------------------------------------------------------------------------

BASEDIR=../..
CLASSPATH=$BASEDIR/config

for file in `ls $BASEDIR/lib/*.jar`;
do
if [ "$CLASSPATH" != "" ]; then
   CLASSPATH=${CLASSPATH}:$file
else
   CLASSPATH=$file
fi
done

echo "Using classpath: "$CLASSPATH

$JAVA_HOME/bin/java -cp $CLASSPATH edu.internet2.middleware.signet.util.SignetProxy $1 $2
