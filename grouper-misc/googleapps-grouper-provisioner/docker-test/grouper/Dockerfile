FROM unicon/grouper-demo:2.3.0

COPY temp/lib/ /opt/grouper.apiBinary-2.3.0/lib/custom/
COPY conf/ /opt/grouper.apiBinary-2.3.0/conf/
COPY GoogleProvisioner.p12 /
COPY testInit.gsh /

RUN set -x; \
    (/usr/sbin/ns-slapd -D /etc/dirsrv/slapd-dir &); \
    (/usr/bin/mysqld_safe &); \
    while ! curl -s localhost:3306 > /dev/null; do echo waiting for mysql to start; sleep 3; done; \
    while ! curl -s ldap://localhost:389 > /dev/null; do echo waiting for ldap to start; sleep 3; done; \
    cd /opt/grouper.apiBinary-2.3.0/ \
    && bin/gsh /testInit.gsh \
    && rm /testInit.gsh \
    && bin/gsh -main edu.internet2.middleware.changelogconsumer.googleapps.GoogleAppsFullSync courses


    