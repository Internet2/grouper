#!/bin/sh
# $Id: generate-schema.sh,v 1.2 2005-04-15 15:53:04 blair Exp $
schema=schema.m4
test=test-data.m4
for type in hsqldb oracle
do
  for f in schema test-data
  do
    m4 -D${type}=true ${f}.m4 > ${f}-${type}.sql
  done
done
exit $?

