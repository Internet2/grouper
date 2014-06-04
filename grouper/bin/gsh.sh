if [ "$0" = "" ]; then
	arg1=-initEnv
elif [ "$0" = "gsh.sh" ]; then
	arg1=-initEnv
else
	arg1=$1
	if [ "$arg1" = "-initenv" ]; then
		arg1=-initEnv
	fi
	if [ "$arg1" = "-initEnv" ]; then
		echo Cannot run gsh.sh -initEnv. Instead use:
		echo "    source gsh.sh, or"
		echo "    . gsh.sh"
		exit
	fi
fi
# In case something goes wrong
GROUPER_HOME_SAFE=$GROUPER_HOME

# Work out where we are
GROUPER_CUR_DIR=$PWD

# Guess GROUPER_HOME if not defined
if [ "$GROUPER_HOME" = "" ]; then
	if [ "$arg1" = "-initEnv" ]; then
		echo Attempting to reset GROUPER_HOME
	fi
	#:noGrouperHome
	GROUPER_HOME=$GROUPER_CUR_DIR
fi

#:checkGrouperHome
if [ -f "$GROUPER_HOME/bin/gsh.sh" ]; then
	GROUPER_DUMMY=
else
	# In case we are in 'bin' try teh parent directory 
	GROUPER_HOME=$GROUPER_CUR_DIR/..
fi

#:gotHome
if [ -f "$GROUPER_HOME/bin/gsh.sh" ]; then
		GROUPER_DUMMY=
else
	# Bad GROUPER_HOME
	if [ "$arg1" = "-initEnv" ]; then
		# Something isn't right so revert to whatever we started with
		GROUPER_HOME=$GROUPER_HOME_SAFE
	fi
	#:badGrouperHome
	echo The GROUPER_HOME environment variable is not defined correctly
	echo or could not be determined
	echo This script must be located in "<GROUPER_HOME>" or "<GROUPER_HOME>/bin"
	exit
fi

#:okHome

PATH="$GROUPER_HOME/bin:$PATH"
if [ "$arg1" = "-initEnv" ]; then
	echo Added $GROUPER_HOME/bin to PATH
	echo Setting GROUPER_HOME=$GROUPER_HOME
	export GROUPER_HOME
fi

if [ "$2" != "" ]; then
	if [ -f "$2/grouper.hibernate.properties" ]; then
		GROUPER_CONF=$2
		echo Using GROUPER_CONF=$GROUPER_CONF
	fi
fi


#:run
# We aren't initing so handle args

# Get standard environment variables
if [ -f "$GROUPER_HOME/bin/setenv.sh" ]; then
	. "$GROUPER_HOME/bin/setenv.sh"
fi

if [ "$MEM_START" = "" ];then
 MEM_START=64m
fi

if [ "$MEM_MAX" = "" ]; then
 MEM_MAX=750m
fi

if [ "$GROUPER_CONF" = "" ]; then
 GROUPER_CONF=$GROUPER_HOME/conf
fi

JAVA=java

if [ -n "$JAVA_HOME" ]; then
 JAVA="$JAVA_HOME/bin/java"
fi

# Append Grouper's configuration
GROUPER_CP=${GROUPER_HOME}/conf

# Append Grouper .jar
GROUPER_CP=${GROUPER_CP}:${GROUPER_HOME}/dist/lib/grouper.jar

# Append third party .jars
GROUPER_CP=${GROUPER_CP}:${GROUPER_HOME}/lib/grouper/*
GROUPER_CP=${GROUPER_CP}:${GROUPER_HOME}/lib/custom/*
GROUPER_CP=${GROUPER_CP}:${GROUPER_HOME}/lib/jdbcSamples/*
GROUPER_CP=${GROUPER_CP}:${GROUPER_HOME}/lib/ant/*
GROUPER_CP=${GROUPER_CP}:${GROUPER_HOME}/lib/test/*
GROUPER_CP=${GROUPER_CP}:${GROUPER_HOME}/dist/lib/test/*

# Append resources
GROUPER_CP=${GROUPER_CP}:${GROUPER_HOME}/src/resources

# Preserve the user's $CLASSPATH
GROUPER_CP=${GROUPER_CP}:${CLASSPATH}

if [ "$arg1" != "-initEnv" ]; then
	# ----- Execute The Requested Command ---------------------------------------

	echo Using GROUPER_HOME:           $GROUPER_HOME
	echo Using GROUPER_CONF:           $GROUPER_CONF
	echo Using JAVA:                   $JAVA
	echo using MEMORY:                 $MEM_START-$MEM_MAX


	GSH=edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper

	# invoker doesn't appear to properly handle the shibboleth or grouper jars with Spring META-INF resources
	# $JAVA  -Xms$MEM_START -Xmx$MEM_MAX -Dgrouper.home="$GROUPER_HOME/" $GSH_JVMARGS -jar $GROUPER_HOME/lib/grouper/invoker.jar -cpdir $GROUPER_CONF -cpalljars $GROUPER_HOME/lib -cpjar $GROUPER_HOME/dist/lib/grouper.jar  -cpalljars $GROUPER_HOME/dist/lib/test $GSH $*

	${JAVA} -Xms$MEM_START -Xmx$MEM_MAX -Dgrouper.home="$GROUPER_HOME/" $GSH_JVMARGS -classpath "${GROUPER_CP}" $GSH $*
	
fi
#:end
if [ "$arg1" != "-initEnv" ]; then
		GROUPER_HOME=$GROUPER_HOME_SAFE
fi

#:endInitEnv
GROUPER_CUR_DIR=
GROUPER_HOME_SAFE=
GROUPER_CP=
