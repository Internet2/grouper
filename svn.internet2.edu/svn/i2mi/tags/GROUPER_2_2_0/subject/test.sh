#!/bin/sh

# -----------------------------------------------------------------------------
# Script for command line util to test sources.xml config file.
#
# Environment Variable Prequisites
#
#   JAVA_HOME     Must point to your JDK.
# -----------------------------------------------------------------------------

JAVA_HOME=/usr/java

CLASSPATH=build
CLASSPATH=conf:${CLASSPATH}

for file in `ls lib/*.jar`;
do
if [ "$CLASSPATH" != "" ]; then
   CLASSPATH=${CLASSPATH}:$file
else
   CLASSPATH=$file
fi
done

echo "Using classpath: "$CLASSPATH

$JAVA_HOME/bin/java -cp $CLASSPATH edu.internet2.middleware.subject.provider.SourceManager $1
