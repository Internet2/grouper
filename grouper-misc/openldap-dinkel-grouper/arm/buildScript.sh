#!/bin/bash

cp /usr/share/openldap-servers/DB_CONFIG.example /var/lib/ldap/DB_CONFIG

mkdir -p /var/ldap/example

chown -R ldap:ldap /var/lib/ldap /etc/openldap/slapd.d /var/ldap

(/usr/sbin/slapd -h "ldap:/// ldaps:/// ldapi:///" -u ldap &) 

while ! curl -s ldap://localhost:389 > /dev/null; do echo waiting for ldap to start; sleep 1; done;

ldapmodify -Y EXTERNAL -H ldapi:/// -f /seed-data/domain.ldif

ldapadd -H ldapi:/// -f /etc/openldap/schema/cosine.ldif

ldapadd -H ldapi:/// -f /etc/openldap/schema/inetorgperson.ldif

ldapadd -H ldapi:/// -f /etc/openldap/schema/nis.ldif

ldapadd -H ldapi:/// -f /seed-data/memberOf.ldif

ldapadd -H ldapi:/// -f /seed-data/eduPerson.ldif

ldapadd -x -D cn=admin,dc=example,dc=edu -w secret -f /seed-data/domain2.ldif

ldapadd -x -D cn=admin,dc=example,dc=edu -w secret -f /seed-data/02-users.ldif

ldapadd -x -D cn=admin,dc=example,dc=edu -w secret -f /seed-data/03-more-users.ldif

ldapadd -x -D cn=admin,dc=example,dc=edu -w secret -f /seed-data/03-redirect.ldif

pkill -HUP slapd

while curl -s ldap://localhost:389 > /dev/null; do echo waiting for ldap to stop; sleep 1; done

chown -R ldap:ldap /var/lib/ldap /etc/openldap/slapd.d /var/ldap
        
