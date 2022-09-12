#!/bin/bash

#docker run  --name ldap my-ldap:latest

docker run -d -p 389:389 --name openldap-dinkel-grouper \
  openldap-dinkel-grouper:latest

# -e SLAPD_PASSWORD=secret -e SLAPD_CONFIG_PASSWORD=secret -e SLAPD_DOMAIN=example.edu 

