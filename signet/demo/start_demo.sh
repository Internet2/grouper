#!/bin/sh
# $Header: /home/hagleyj/i2mi/signet/demo/start_demo.sh,v 1.1 2007-04-22 01:17:36 ddonn Exp $

# This script starts the Signet QuickStart Demo.

# Check if -h or --help
if [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
  echo Command shell to run the Signet QuickStart Demo
  echo Usage: $0 [browser]
  echo $'\t'browser: The web browser to use \(default = firefox\)
  exit
fi

# Check if Java is runnable
if [ ! -x $JAVA_HOME/bin/java ]; then
  echo This script requires that the JAVA_HOME environment variable be properly
  echo set. It must name a directory which contains "bin/java".
  echo Currently, JAVA_HOME=$JAVA_HOME
  exit
fi

# Define my variables
. set_env.sh DEMO

# Check if Tomcat is runnable
if [ ! -x $TOMCAT_START ]; then
  echo Could not find the Tomcat startup script: $TOMCAT_START
  exit
fi

# Start HypersonicSQL
#. start_hsqldb.sh
. start_hsqldb.sh

# Start Tomcat
$TOMCAT_START
sleep 5s
echo Tomcat Web Server successfully started. Use 'shutdown_demo.sh' to shut down.

# Start a web browser
if [ "$1" != "" ]; then
  BROWSER=$1
else
  BROWSER=firefox
fi
if [ ! -x `which $BROWSER` ]; then
  echo Could not find web browser: `which $BROWSER`
  exit
fi
$BROWSER http://localhost:8080/signet
# This blocks until user exits browser

# Shutdown Tomcat
#$TOMCAT_SHUTDOWN

# Shutdown HSQLDB
#/bin/sh shutdown_hsqldb.sh



