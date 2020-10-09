#!/bin/bash

dirname="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

docker run -d -p 389:389 --name openldap-dinkel-grouper --mount type=bind,source=$dirname/ldap-seed-data,target=/etc/ldap/prepopulate -e SLAPD_PASSWORD=secret -e SLAPD_CONFIG_PASSWORD=secret -e SLAPD_DOMAIN=example.edu openldap-dinkel-grouper
