FROM tier/shib-idp:4.3.1_20231218

WORKDIR /opt/shibboleth-idp

COPY plugin-truststore/net.shibboleth.oidc.common/truststore.asc /opt/shibboleth-idp/credentials/net.shibboleth.oidc.common/
COPY plugin-truststore/net.shibboleth.idp.plugin.oidc.config/truststore.asc /opt/shibboleth-idp/credentials/net.shibboleth.idp.plugin.oidc.config/
COPY plugin-truststore/net.shibboleth.idp.plugin.oidc.op/truststore.asc /opt/shibboleth-idp/credentials/net.shibboleth.idp.plugin.oidc.op/

RUN bin/plugin.sh --noPrompt -I net.shibboleth.oidc.common \
    && bin/plugin.sh --noPrompt -I net.shibboleth.idp.plugin.oidc.config \
    && bin/plugin.sh --noPrompt -I net.shibboleth.idp.plugin.oidc.op

COPY tomcat/server.xml /usr/local/tomcat/conf/

COPY opt-shibboleth-idp/ /opt/shibboleth-idp/

EXPOSE 8080


#FROM amazoncorretto:17 as install
#
#ARG TOMCAT_VERSION=10.1.15
#
#COPY keys/* /tmp/keys/
#
#RUN yum install -y tar gzip
#
#RUN gpg --import /tmp/keys/TOMCAT_PGP_KEYS
#
#RUN curl -L https://archive.apache.org/dist/tomcat/tomcat-10/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz -o apache-tomcat-$TOMCAT_VERSION.tar.gz \
#    && curl https://archive.apache.org/dist/tomcat/tomcat-10/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz.asc -o apache-tomcat-$TOMCAT_VERSION.tar.gz.asc \
#    && gpg --verify apache-tomcat-$TOMCAT_VERSION.tar.gz.asc apache-tomcat-$TOMCAT_VERSION.tar.gz \
#    && tar -zxvf apache-tomcat-$TOMCAT_VERSION.tar.gz -C /opt \
#    && rm apache-tomcat-$TOMCAT_VERSION.tar.gz \
#    && mv /opt/apache-tomcat-$TOMCAT_VERSION/ /opt/tomcat \
#    && rm -rf /opt/tomcat/webapps/*
#
#FROM amazoncorretto:17 as shib-setup
#
#RUN yum install -y dos2unix
#
#COPY opt-shibboleth-idp /opt/shibboleth-idp/
#
#RUN dos2unix /opt/shibboleth-idp/bin/*.sh \
#    && chmod +x /opt/shibboleth-idp/bin/*.sh
#
#FROM amazoncorretto:17 as prod
#
#COPY --from=install /opt/tomcat /opt/tomcat/
#
#COPY --from=shib-setup /opt/shibboleth-idp /opt/shibboleth-idp/
#
#COPY tomcat/idp.xml /opt/tomcat/conf/Catalina/localhost/
#COPY tomcat/server.xml /opt/tomcat/conf/
#COPY tomcat/setenv.sh /opt/tomcat/bin/
#
#ENTRYPOINT /opt/shibboleth-idp/bin/build.sh && /opt/tomcat/bin/catalina.sh run
#
#FROM prod as dev
#
#FROM prod