#!/bin/sh
#
# Copyright 2012 Internet2
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


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
