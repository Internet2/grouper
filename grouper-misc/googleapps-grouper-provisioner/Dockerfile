FROM maven

COPY pom.xml /tmp/provisioner/pom.xml

WORKDIR /tmp/provisioner/

RUN mvn package

COPY /src/ /tmp/provisioner/src/

RUN mvn clean package dependency:copy-dependencies -DskipTests -DincludeScope=runtime


FROM tier/gte:base-201911

COPY docker-test/conf/ /opt/grouper/conf/
COPY docker-test/GoogleProvisioner.p12 /
COPY docker-test/testInit.gsh /

COPY --from=0 /tmp/provisioner/target/lib/ /opt/grouper/grouper.apiBinary/lib/custom/
COPY --from=0 /tmp/provisioner/target/google-*.jar /opt/grouper/grouper.apiBinary/lib/custom/

RUN set -x; \
    (/usr/sbin/slapd -h "ldap:/// ldaps:/// ldapi:///" -u ldap &) \
    && while ! curl -s ldap://localhost:389 > /dev/null; do echo waiting for ldap to start; sleep 1; done; \
    (mysqld_safe & ) \
    && while ! curl -s localhost:3306 > /dev/null; do echo waiting for mysqld to start; sleep 3; done; \
    . /usr/local/bin/library.sh \
    && prepConf \
    bin/gsh /testInit.gsh \
    && bin/gsh -main edu.internet2.middleware.changelogconsumer.googleapps.GoogleAppsFullSync courses \
    && pkill -HUP slapd \
    && while curl -s ldap://localhost:389 > /dev/null; do echo waiting for ldap to stop; sleep 1; done; \
    pkill -u mysql mysqld \
    && while curl -s localhost:3306 > /dev/null; do echo waiting for mysqld to stop; sleep 1; done
