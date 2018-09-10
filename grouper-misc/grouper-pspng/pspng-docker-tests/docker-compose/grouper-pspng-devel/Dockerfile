From tier/grouper-multi-purpose:2.3.0-a97-u41-w11-p16

RUN yum makecache

ENV TZ=America/New_York
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV PATH=/opt/grouper/grouper.apiBinary/bin:$PATH

RUN sed -i '1s:^:export GROUPER_HOME=/opt/grouper/grouper.apiBinary\n:' /opt/grouper/grouper.apiBinary/bin/gsh
RUN sed -i '1s:^:export GROUPER_HOME=/opt/grouper/grouper.apiBinary\n:' /opt/grouper/grouper.apiBinary/bin/gsh.sh

RUN sed -i '1i#!/bin/bash' /opt/grouper/grouper.apiBinary/bin/gsh
RUN sed -i '1i#!/bin/bash' /opt/grouper/grouper.apiBinary/bin/gsh.sh

RUN yum install -y openldap-clients

# Opensource mysql (for mysql client)
RUN yum install -y mariadb

COPY henplus-install /henplus

COPY pspng-classes/ /opt/grouper/grouper.apiBinary/conf/
COPY dependent-jars/ /opt/grouper/grouper.apiBinary/lib/custom/
COPY /grouper-config-50.d/ /grouper-config-50.d

ENV PATH=/scripts:$PATH

RUN cd / && ln -s /opt/grouper/grouper.apiBinary/bin/gsh /bin

COPY container-files/install-grouper-configs /
CMD /install-grouper-configs && /scripts/run-grouper-loader

# Remove unknown version of slf4j, classloading conflict with 1.6.1 version??
RUN rm /opt/grouper/grouper.apiBinary/lib/grouper/slf4j-log4j12.jar
RUN rm /opt/grouper/grouper.apiBinary/lib/grouper/activemq-all-5.15.0.jar
RUN rm /opt/grouper/grouper.apiBinary/lib/grouper/mchange-commons-java-0.2.11.jar

# Patch mysql jdbc version for latest version of mysqld
RUN rm /opt/grouper/grouper.apiBinary/lib/custom/mysql-connector*
COPY mysql-connector-java-5.1.25-bin.jar /opt/grouper/grouper.apiBinary/lib/custom
