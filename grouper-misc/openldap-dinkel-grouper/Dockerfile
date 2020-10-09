FROM dinkel/openldap

RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y procps

# slapd get's updated by ldap-utils install, and this avoids its configuration
RUN echo slapd slapd/no_configuration boolean true | debconf-set-selections
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y ldap-utils

COPY schemas/ /etc/ldap.dist/schema/

ENV SLAPD_ADDITIONAL_SCHEMAS=eduperson

RUN sed -i '1i#!/bin/bash -x' /entrypoint.sh
