#!/bin/sh

#
# Copyright (C) 2005 blair christensen.
# All Rights Reserved.
#
# You may use and distribute under the same terms as Grouper itself.
#

#
# A minimal-and-crude wrapper for running cdg (and presumably
# grouper) command line apps.  
#
# $Id: grouper-cli.sh,v 1.1 2005-12-16 21:47:59 blair Exp $
#

# Set some defaults
CDG_BASE=${CDG_BASE-`pwd`}
GROUPER_BASE=${GROUPER_BASE-`pwd`/../../grouper}

_append_classpath () {
  cp=""
  jars=`ls ${2}/*.jar 2>/dev/null`
  if [ "$jars" ]
  then
    cp=${1}
    for f in ${jars}
    do
      cp=${cp}:${f}
    done
  else
    cp=${1}:${2}
  fi
  echo ${cp}
}

_build_classpath() {
  for d in \
    ${CLASSPATH} \
    ${CDG_JAR} \
    ${GROUPER_JAR} \
    ${GROUPER_LIB_JAR} \
    ${GROUPER_BASE}/build/grouper ${GROUPER_BASE}/lib \
    ${CDG_BASE}/build/cdg ${CDG_BASE}/lib \
    ${GROUPER_BASE}/conf
  do
    if [ -e "$d" ]
    then
      cp=`_append_classpath ${cp} ${d}`
    fi
  done 
  if [ "$cp" ]; then
    cp="-classpath $cp"
  fi
  echo ${cp}
}

if [ "$1" ]
then
  if [ ${1} = "-h" -o ${1} = "-help" -o ${1} = "--help" ]
  then
    cat <<EO_HELP
USAGE: ${0} <class> [arguments]

The following environmental variables are used:

  CDG_BASE: cdg distribution base.  Defaults to the current working 
    directory if not set.  Is used to help find the cdg class files
    and third party libraries.

  CDG_JAR: Path to cdg jar.  Has precedence over CDG_BASE.

  GROUPER_BASE: Grouper distribution base.  Defaults to the current
    working directory if not set.  Is used to help find Grouper
    class files, third party libraries and configuration.

  GROUPER_JAR: Path to Grouper jar.  Has precedence over GROUPER_BASE.

  GROUPER_LIB_JAR: Path to Grouper third party libraries jar.  Has
    precedence over GROUPER_BASE.

EO_HELP
  else
    java `_build_classpath` ${@}
  fi
else
  echo >&2 "USAGE: ${0} <class> [arguments]"
  exit 1
fi

exit $?

