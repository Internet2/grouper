#!/bin/bash

#exec "$@"
/usr/sbin/slapd -h "ldap:/// ldaps:/// ldapi:///" -u ldap

while ! curl -s ldap://localhost:389 > /dev/null; do sleep 1; done;

while curl -s ldap://localhost:389 > /dev/null; do sleep 5; done