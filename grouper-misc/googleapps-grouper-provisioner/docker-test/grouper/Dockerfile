FROM unicon/grouper-demo:2.3.0

COPY temp/lib/ /opt/grouper.apiBinary-2.3.0/lib/custom/
COPY conf/ /opt/grouper.apiBinary-2.3.0/conf/
COPY GoogleProvisioner.p12 /
COPY testInit.gsh /

RUN set -x; \
    service mysql start \
    && service slapd start \
    && cd /opt/grouper.apiBinary-2.3.0/ \
    && bin/gsh /testInit.gsh \
    && rm /testInit.gsh \
    && bin/gsh -main edu.internet2.middleware.changelogconsumer.googleapps.GoogleAppsFullSync courses


    