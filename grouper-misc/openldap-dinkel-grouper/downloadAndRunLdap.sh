#!/bin/bash

mkdir ldapExample
cd ldapExample
mkdir ldap-seed-data
cd ldap-seed-data
wget https://raw.githubusercontent.com/Internet2/grouper/GROUPER_2_5_BRANCH/grouper-misc/openldap-dinkel-grouper/ldap-seed-data/02-users.ldif
wget https://raw.githubusercontent.com/Internet2/grouper/GROUPER_2_5_BRANCH/grouper-misc/openldap-dinkel-grouper/ldap-seed-data/03-more-users.ldif
wget https://raw.githubusercontent.com/Internet2/grouper/GROUPER_2_5_BRANCH/grouper-misc/openldap-dinkel-grouper/ldap-seed-data/03-redirect.ldif
cd ..
mkdir schemas
cd schemas
wget https://raw.githubusercontent.com/Internet2/grouper/GROUPER_2_5_BRANCH/grouper-misc/openldap-dinkel-grouper/schemas/eduperson.ldif
cd ..
wget https://raw.githubusercontent.com/Internet2/grouper/GROUPER_2_5_BRANCH/grouper-misc/openldap-dinkel-grouper/Dockerfile
docker build -t openldap-dinkel-grouper .
docker run -d -p 389:389 --name openldap-dinkel-grouper --mount type=bind,source="$PWD/ldap-seed-data",target=/etc/ldap/prepopulate \
   -e SLAPD_PASSWORD=secret -e SLAPD_CONFIG_PASSWORD=secret -e SLAPD_DOMAIN=example.edu openldap-dinkel-grouper
