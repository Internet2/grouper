#!/usr/bin/env bash


checkGrouperHome() {
	# undefined is failure
	if [ -z "$1" ]; then return 1; fi

	# lib/grouper.jar is success
	if [ -f "$1/lib/grouper.jar" ]; then return 0; fi

	# (bash command) lib/grouper-a.b.c.jar and lib/grouper-a.b.c-SNAPSHOT.jar are success
	compgen -G "$1/lib/grouper-[0-9].[0-9].[0-9]*.jar" > /dev/null 2>&1
}


# Guess GROUPER_HOME if not defined
if [ "$GROUPER_HOME" = "" ]; then
	# Get dirname from parent of gsh path
	dirname="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

	# sanity-check
	if checkGrouperHome "$dirname"; then
		GROUPER_HOME="$dirname"
	else
		# Above is unlikely to fail, but if so, get dirname from current parent directory
		dirname2="$PWD/.."
		if checkGrouperHome "$dirname2"; then
			echo "WARNING-could not determine GROUPER_HOME based on location of this"
			echo "  script, so using parent of the current directory instead"
			GROUPER_HOME="$dirname2"
		else
			# revert back to the first attempt so any error messages can report it
			GROUPER_HOME="$dirname"
		fi
	fi
fi


if ! checkGrouperHome "$GROUPER_HOME"; then
	echo "The GROUPER_HOME environment variable ('$GROUPER_HOME') is"
	echo "    not defined correctly or could not be determined. The jar"
	echo "    file <GROUPER_HOME>/lib/grouper.jar must exist"
	return 1 2>/dev/null || exit 1
fi


# Check conf directory
if [ "$GROUPER_CONF" = "" ]; then
	GROUPER_CONF="$GROUPER_HOME/classes"
fi
if [ ! -d "$GROUPER_CONF" ]; then
	echo "The GROUPER_CONF environment variable ('$GROUPER_CONF')"
	echo "  is not defined correctly or could not be determined. This should be"
	echo "  a directory containing property files"
	return 1 2>/dev/null || exit 1
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
GROUPER_CP="${GROUPER_CONF}:${GROUPER_HOME}/lib/*"

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

echo "Using GROUPER_HOME:           $GROUPER_HOME"
echo "Using GROUPER_CONF:           $GROUPER_CONF"
echo "Using JAVA:                   $JAVA"
echo "Using CLASSPATH:              $GROUPER_CP"
echo "using MEMORY:                 $MEM_START-$MEM_MAX"


GSH=edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper

"${JAVA}" -Xms$MEM_START -Xmx$MEM_MAX -Dgrouper.home="$GROUPER_HOME/" -Dfile.encoding=utf-8 $GSH_JVMARGS -classpath "${GROUPER_CP}" $GSH "$@"
retVal=$?

# handle return from either execution or bash source
return $retVal 2>/dev/null || exit $retVal

