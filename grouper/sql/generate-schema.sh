#!/bin/sh
# $Id: generate-schema.sh,v 1.1 2005-02-16 20:39:54 blair Exp $
schema=schema.m4
test=test-data.m4
m4                ${schema} > schema-hsqldb.sql
m4 -DORACLE=true  ${schema} > schema-oracle.sql
m4 -DHSQLDB=true  ${test}   > test-data-hsqldb.sql
m4                ${test}   > test-data-oracle.sql
exit $?
