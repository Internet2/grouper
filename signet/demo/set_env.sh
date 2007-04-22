#!/bin/sh

# $Header: /home/hagleyj/i2mi/signet/demo/set_env.sh,v 1.1 2007-04-22 01:17:36 ddonn Exp $

# Setup environment for start_demo.sh and start_hsqldb.sh

if [ "$1" == "HSQLDB" ]; then
	UTIL_DIR=../util
	HSQLDB_DIR=../data
	HSQLDB_FILENAME=SignetQsDb
	HSQLDB_ALIAS=$HSQLDB_FILENAME
	HSQLDB_EXEC=$UTIL_DIR/lib/hsqldb.jar
	HSQLDB_CFG_DIR=$UTIL_DIR/config
	HSQLDB_RC_FILE=$HSQLDB_CFG_DIR/sqltool.rc
	HSQLDB_CP=$HSQLDB_EXEC:$HSQLDB_CFG_DIR
	HSQLDB_URLID=localhost-sa
elif [ "$1" == "DEMO" ]; then
	CATALINA_HOME=../tomcat
	TOMCAT_START=$CATALINA_HOME/bin/startup.sh
	TOMCAT_SHUTDOWN=$CATALINA_HOME/bin/shutdown.sh
else
	echo $0 - Unknown parameter: "$1"
fi
