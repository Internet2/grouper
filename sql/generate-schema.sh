#!/bin/sh
# $Id: generate-schema.sh,v 1.3 2005-07-09 20:51:00 blair Exp $
schema=schema.m4
test=test-data.m4
for type in hsqldb oracle postgres
do
  for f in schema test-data
  do
    m4 -D${type}=true ${f}.m4 > ${f}-${type}.sql
  done
done
exit $?

