#!/bin/sh

#
# Copyright (C) 2005 blair christensen.
# All Rights Reserved.
#
# You may use and distribute under the same terms as Grouper itself.
#

#
# Run cdg CLI tests.  It wasn't behaving properly within Ant and
# JUnit and I don't feel debugging that at this moment.
#
# $Id: test-cli.sh,v 1.1 2005-12-16 21:47:59 blair Exp $
#

#
# TODO 
# * CSV2JDBC
#

GROUPER="../../"

cp=build/cdg:build/tests
cp=${cp}:${GROUPER}/build/grouper:${GROUPER}/conf
for f in lib/*.jar ${GROUPER}/lib/*.jar
do
  cp=${cp}:${f}
done

_fail () {
  out=`_run ${@}`
  if [ $? -eq 0 ]
  then
    echo >&2 "Fail: ${@} - ${out}" 
    exit 1
  else
    echo "Pass: ${@} - ${out}"
  fi
}

_pass () {
  out=`_run ${@}`
  if [ $? -ne 0 ]
  then
    echo >&2 "Fail: ${@} - ${out}"
    exit 1
  else
    echo "Pass: ${@} - ${out}"
  fi
}

_run () {
  java -classpath ${cp} com.devclue.grouper.${@} 2>&1
}

# make sure we are up-to-date
ant build

# Reset the registry
_run registry.Reset

# Now run a series of tests 
_fail stem.StemQ          com
_pass stem.StemAdd        com
_pass stem.StemQ          com
_fail stem.StemQ          net
_fail stem.StemQ          org
_pass stem.StemAdd        org
_pass stem.StemQ          org
_fail stem.StemQ          com:devclue
_pass stem.StemAdd        com                   devclue
_pass stem.StemQ          com:devclue
_fail stem.StemQ          net:devclue
_fail stem.StemQ          org:devclue
_pass stem.StemAdd        org                   devclue
_pass stem.StemQ          org:devclue
_fail group.GroupQ        com
_fail group.GroupQ        com:devclue
_pass group.GroupAdd      com                   devclue
_pass group.GroupQ        com:devclue
_fail group.GroupQ        com:devclue:grouper
_pass group.GroupAdd      com:devclue           grouper
_pass group.GroupQ        com:devclue:grouper
_fail group.GroupQ        net:devclue
_fail group.GroupAdd      net                   devclue
_pass subject.SubjectQ    GrouperSystem
_pass subject.SubjectQ    GrouperSystem         application
_fail subject.SubjectQ    com
_fail subject.SubjectQ    com                   group
_pass subject.SubjectQ    com:devclue
_pass subject.SubjectQ    com:devclue           group
_pass subject.SubjectQ    com:devclue:grouper
_pass subject.SubjectQ    com:devclue:grouper   group
_fail subject.SubjectQ    id0
_pass subject.SubjectAdd  id0
_pass subject.SubjectQ    id0
_fail subject.SubjectQ    id1
_pass subject.SubjectAdd  id1
_pass subject.SubjectQ    id1
_pass member.MemberAdd    com:devclue           id0
_pass member.MemberAdd    com:devclue:grouper   com:devclue   group
_pass member.MemberQ      id0
_pass member.MemberQ      id0                   person
_fail member.MemberQ      id1 
_fail member.MemberQ      id1                   person
_fail member.MemberQ      com:devclue           
_pass member.MemberQ      com:devclue           group
_fail member.MemberQ      com:devclue:grouper   
_fail member.MemberQ      com:devclue:grouper   group
cat <<EO_CSV_PASS | _pass subject.CSV2JDBC 
id10,person
id11,person,this is id11
id12,person,this is id12,uid12
EO_CSV_PASS
_pass subject.SubjectQ      id10
_pass subject.SubjectQ      id10                person
_pass subject.SubjectQ      id11
_pass subject.SubjectQ      id11                person
_pass subject.SubjectQ      id12
_pass subject.SubjectQ      id12                person
cat <<EO_FAIL_SHORT | _fail subject.CSV2JDBC
id13
EO_FAIL_SHORT
_fail subject.SubjectQ      id13
_fail subject.SubjectQ      id13                person
cat <<EO_FAIL_LONG | _fail subject.CSV2JDBC
id14,person,this is id14,uid14,chaff
EO_FAIL_LONG
_fail subject.SubjectQ      id14
_fail subject.SubjectQ      id14                person
cat <<EO_FAIL_TYPE | _fail subject.CSV2JDBC
id15,type
EO_FAIL_TYPE
_fail subject.SubjectQ      id15
_fail subject.SubjectQ      id15                person

exit $?

