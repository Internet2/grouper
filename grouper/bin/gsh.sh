#!/usr/bin/env bash

_ERROR_BAD_HOME_SET=80
_ERROR_BAD_HOME_CALC=81
_ERROR_BAD_CONF=82

# Returns true if $1/dist/lib/grouper.jar exists
isApiHome() {
	# undefined is failure
	if [ -z "$1" ]; then return 1; fi
	[ -f "$1/dist/lib/grouper.jar" ]
}

# Returns true if exists in $1/lib: grouper.jar or grouper-a.b.c*.jar
isWebappHome() {
	# undefined is failure
	if [ -z "$1" ]; then return 1; fi

	# lib/grouper.jar is success
	if [ -f "$1/lib/grouper.jar" ]; then return 0; fi

	# (bash command) lib/grouper-a.b.c.jar and lib/grouper-a.b.c-SNAPSHOT.jar are success
	compgen -G "$1/lib/grouper-[0-9].[0-9].[0-9]*.jar" > /dev/null 2>&1
}

# set _grouperHomeType to ["", api, webapp] depending on where grouper.jar is found
checkGrouperHome() {
	_grouperHomeType=
	if isApiHome "$1"; then
		_grouperHomeType=api
	elif isWebappHome "$1"; then
		_grouperHomeType=webapp
	fi

	[ -n "$_grouperHomeType" ]
}


errorBadHome() {
	echo "The GROUPER_HOME environment variable ('$GROUPER_HOME') is"
	echo "    not defined correctly or could not be determined. The"
	echo "    grouper.jar file could not be found in either the dist/lib"
	echo "    or lib directory"
}


# if GROUPER_HOME already set, verify it's a good directory
if [ -n "$GROUPER_HOME" ]; then
	if ! checkGrouperHome "$GROUPER_HOME"; then
		errorBadHome
		return $_ERROR_BAD_HOME_SET 2>/dev/null || exit $_ERROR_BAD_HOME_SET
	fi
else
	# Otherwise, get dirname from parent of gsh path
	dirname="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

	if checkGrouperHome "$dirname"; then
		GROUPER_HOME="$dirname"
	#elif checkGrouperHome "$PWD/.."; then
	#	echo "WARNING-could not determine GROUPER_HOME based on location of this"
	#	echo "  script, so using parent of the current directory instead"
	#	GROUPER_HOME="$PWD/.."
	else
		errorBadHome
		return $_ERROR_BAD_HOME_CALC 2>/dev/null || exit $_ERROR_BAD_HOME_CALC
	fi
fi

if [ -z "$GSH_QUIET" ]; then
	echo "Detected Grouper directory structure '$_grouperHomeType' (valid is api or webapp)"
fi

# Check conf directory
if [ "$GROUPER_CONF" = "" ]; then
	case $_grouperHomeType in
		api) GROUPER_CONF="$GROUPER_HOME/conf";;
		webapp) GROUPER_CONF="$GROUPER_HOME/classes";;
	esac
fi
if [ ! -f "$GROUPER_CONF/grouper.hibernate.properties" ]; then
	echo "The GROUPER_CONF environment variable ('$GROUPER_CONF') is not"
	echo "  defined correctly or could not be determined. This should be a directory"
	echo "  containing property files, but is missing grouper.hibernate.properties"
	return $_ERROR_BAD_CONF 2>/dev/null || exit $_ERROR_BAD_CONF
fi


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

if [ -n "$JAVA_HOME" ]; then
	JAVA="$JAVA_HOME/bin/java"
else
	JAVA=java
fi


# Append Grouper's configuration
GROUPER_CP=${GROUPER_CONF}


if [ $_grouperHomeType = api ]; then
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
elif [ $_grouperHomeType = webapp ]; then
	GROUPER_CP="${GROUPER_CONF}:${GROUPER_HOME}/lib/*"
else
	echo "Could not determine Grouper directory structure (should be api or webapp)"
	return 1 2>/dev/null || exit 1
fi


# Preserve the user's $CLASSPATH
if [ -n "$CLASSPATH" ]; then
	GROUPER_CP=${CLASSPATH}:${GROUPER_CP}
fi

# If using Cygwin under Windows, may want to convert the Grouper paths, and convert classpath (:) to (;)
if [ -n "$GSH_CYGWIN" ]; then
	GROUPER_HOME=$(cygpath --windows "$GROUPER_HOME")
	GROUPER_CONF=$(cygpath --windows "$GROUPER_CONF")
	GROUPER_CP=$(cygpath --path --windows "$GROUPER_CP")
fi

retVal=0


# ----- Execute The Requested Command ---------------------------------------

if [ -z "$GSH_QUIET" ]; then
	echo "Using GROUPER_HOME:           $GROUPER_HOME"
	echo "Using GROUPER_CONF:           $GROUPER_CONF"
	echo "Using JAVA:                   $JAVA"
	echo "Using CLASSPATH:              $GROUPER_CP"
	echo "using MEMORY:                 $MEM_START-$MEM_MAX"
fi

GSH=edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper

"${JAVA}" -Xms$MEM_START -Xmx$MEM_MAX -Dgrouper.home="$GROUPER_HOME/" -Dfile.encoding=utf-8 $GSH_JVMARGS -classpath "${GROUPER_CP}" $GSH "$@"
retVal=$?

# handle return from either execution or bash source
return $retVal 2>/dev/null || exit $retVal

