---
title: "Running Grouper in a different container"
space: Grouper
pageId: 28555810
version: 4
lastUpdated: 2026-07-01T05:37:25.678Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555810/Running+Grouper+in+a+different+container
---

In v4, if you want to run Grouper in a different OS, e.g. centos, you need to look at the ship SP Dockerfile that Grouper uses, and the Grouper Dockerfile, and do the OS stuff, and copy the Grouper built stuff

Here is an example that works with Grouper v4.1.2. Going forward you should perdiocially check on changes to the Dockerfiles to make sure you incorporate any applicable changes

This will be in git under Grouper: Dockerfile_centos in 4.1.5+

Use your own random password not 'j7aFqwTmRfrme1CXk1N0'

```
FROM i2incommon/grouper:4.1.2 as grouperContainer

FROM centos:centos7

COPY --from=grouperContainer /opt /opt
COPY --from=grouperContainer /usr/local/bin /usr/local/bin

LABEL author="tier-packaging@example.com <tier-packaging@example.com>" \
      Vendor="TIER" \
      ImageType="Grouper" \
      ImageName=$imagename \
      ImageOS=centos7

ARG GROUPER_CONTAINER_VERSION

ENV GROUPER_VERSION=4.1.2 \
    GROUPER_CONTAINER_VERSION=4.1.2 \
    JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto \
    PATH=$PATH:$JAVA_HOME/bin \
    GROUPER_HOME=/opt/grouper/grouperWebapp/WEB-INF

#  net-tools curl mlocate strace telnet man vim rsyslog cron httpd mod_ssl cronie

RUN rm -fr /var/cache/yum/* && yum clean all && yum -y install --setopt=tsflags=nodocs epel-release \ 
    && yum update -y \
    && yum install -y logrotate python3-pip rsync sudo patch supervisor wget tar unzip dos2unix file net-tools curl mlocate logrotate strace telnet man vim rsyslog cronie httpd mod_ssl findutils \
    && pip3 install --upgrade setuptools \
    && yum clean -y all \
    && groupadd -r tomcat \
    && useradd -r -m -s /sbin/nologin -g tomcat tomcat

# Install Corretto Java JDK
#Corretto download page: https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/downloads-list.html

# Install Corretto Java JDK (newer more arch independent way)
RUN rpm --import https://yum.corretto.aws/corretto.key \
    && curl -L -o /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo \
    && yum install -y java-17-amazon-corretto-devel

RUN /opt/container_files/docker-build-bin/containerDockerfileInstallPermissions.sh tomcat root

# testing container
# docker build -f Dockerfile_centos -t mygrouper
# see output with  
# DOCKER_BUILDKIT=0 docker build --progress=plain -t mygrouper .
# docker run --detach --name mygrouper mygrouper:latest
# docker exec -it mygrouper bash
# docker run --detach -e GROUPER_SELF_SIGNED_CERT=true -e GROUPER_MAX_MEMORY='3g' -e GROUPER_RUN_SHIB_SP=false -e GROUPERSYSTEM_QUICKSTART_PASS=j7aFqwTmRfrme1CXk1N0 -e GROUPER_UI_GROUPER_AUTH=true -e GROUPER_DATABASE_URL=jdbc:postgresql://host.docker.internal:5433/grouper -e GROUPER_DATABASE_USERNAME=grouper -e GROUPER_DATABASE_PASSWORD=j7aFqwTmRfrme1CXk1N0 -e GROUPER_AUTO_DDL_UPTOVERSION='v4.*.*' -e GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES='0.0.0.0/0' -e GROUPER_START_DELAY_SECONDS=10 --publish 8081:8080  -e GROUPER_RUN_APACHE=false --name mygrouper mygrouper:latest ui

WORKDIR /opt/grouper/grouperWebapp/WEB-INF/
EXPOSE 80 443
HEALTHCHECK NONE

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
#ENTRYPOINT ["ping"]
#CMD ["google.com"]

```
