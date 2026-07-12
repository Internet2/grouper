---
title: "Grouper container unit tests"
space: Grouper
pageId: 28555514
version: 37
lastUpdated: 2026-07-01T05:38:04.381Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555514/Grouper+container+unit+tests
---

There are unit tests (bash unix based, basic unix commands and curl required) in the container to test options of the container

The following are tested

- Environment variables
- File placement
- File variable substitution
- Processes running
- Ports listening
- Login to UI/WS/Scim

Anyone can run these tests, but do these with no extra configs, or with a non prod database. Do NOT run these in production or in an environment with valuable data. Note, these tests are not intended to be destructive but it is not guaranteed. Run these in an empty directory, they will assume they have full control of directory

Tests are in /opt/tier-support/test/

Note, if you are on a mac using podman, you need to run this in a dir under /Users (e.g. ~/temp)

Copy those to the host and run:

```
docker pull i2incommon/grouper:2.6.5
docker run --detach --name grouper-test i2incommon/grouper:2.6.5
docker cp grouper-test:/opt/tier-support/test .
docker rm -f grouper-test
mv test/* .
rmdir test
chmod +x *.sh
./grouperContainerUnitTest.sh grouper-test i2incommon/grouper:2.6.5 2.6.5 2.6.5
```

Sample output (returns 0 on success and 1 on failure)

```
mchyzer@ISC20-0637-WL:~/containerTest$ ./grouperContainerUnitTest.sh grouper-test i2incommon/grouper:2.6.0 2.6.0 2.6.0

################
Running container as ui
docker run --detach --name grouper-test --publish 443:443 i2incommon/grouper:2.6.0 ui
################

82842f2a811c416911451c846eee68d80e4b1bebe5bdbebc594f4cc13a6a41c4
SUCCESS: file /opt/tomee/conf/server.xml should contain at least one 'address="0.0.0.0"': '0' < '1'
SUCCESS: file /opt/tomee/conf/server.xml should contain at least one 'allowedRequestAttributesPattern=".*"': '0' < '1'
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libWs/axis2-kernel-1.6.4.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/axis2-kernel-1.6.4.jar should not exist: 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libScim/stax-api-1.0-2.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/stax-api-1.0-2.jar should not exist: 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/grouper-messaging-activemq-2.5.40.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libUiAndDaemon/grouper-messaging-activemq-2.5.40.jar should exist: 1
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should contain at least one 'Listen 443 https': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf/httpd.conf should contain at least one 'Listen 80': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:shibbolethsp': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:tomee': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:httpd': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'user=shibd': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should not contain 'program:hsqldb': 0
SUCCESS: file /opt/tier-support/supervisord.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should contain at least one 'cachain.pem': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain '/etc/pki/tls/certs/localhost.crt': 0
SUCCESS: file /opt/tomee/conf/Catalina/localhost/grouper.xml should contain at least one 'cookies="true"': '0' < '1'
SUCCESS: file /etc/httpd/conf/httpd.conf should not contain 'Options Indexes': 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/log4j.properties should contain at least one '/tmp/logpipe': '0' < '4'
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/log4j.properties should contain at least one 'grouper-ui;': '0' < '4'
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties should not contain 'grouperPasswordConfigOverride_UI_GrouperSystem_pass.elConfig': 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties should not contain 'thisPassIsCopyrightedDontUse': 0
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one '3600': '0' < '5'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should not contain 'ServerName': 0
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should not contain 'UseCanonicalName On': 0
SUCCESS: file /opt/tomee/conf/server.xml should not contain 'AccessLogValve': 0
SUCCESS: file /opt/tomee/conf/server.xml should contain at least one 'tomcatAuthentication': '0' < '1'
SUCCESS: env var GROUPER_APACHE_SERVER_NAME: not equal to: 'https://a.b.c:443', is: ''
SUCCESS: env var GROUPER_TOMCAT_LOG_ACCESS: false
SUCCESS: env var GROUPERSCIM_PROXY_PASS: #
SUCCESS: env var GROUPERSCIM_URL_CONTEXT: grouper-ws-scim
SUCCESS: env var GROUPERWS_PROXY_PASS: #
SUCCESS: env var GROUPERWS_URL_CONTEXT: grouper-ws
SUCCESS: env var GROUPER_APACHE_AJP_TIMEOUT_SECONDS: 3600
SUCCESS: env var GROUPER_APACHE_NONSSL_PORT: 80
SUCCESS: env var GROUPER_APACHE_SSL_PORT: 443
SUCCESS: env var GROUPER_CHOWN_DIRS: true
SUCCESS: env var GROUPER_CONTAINER_VERSION: 2.6.0
SUCCESS: env var GROUPER_DAEMON: false
SUCCESS: env var GROUPER_GSH_CHECK_USER: true
SUCCESS: env var GROUPER_GSH_USER: tomcat
SUCCESS: env var GROUPER_HOME: /opt/grouper/grouperWebapp/WEB-INF
SUCCESS: env var GROUPER_LOG_PREFIX: grouper-ui
SUCCESS: env var GROUPER_MAX_MEMORY: 1500m
SUCCESS: env var GROUPER_PROXY_PASS:
SUCCESS: env var GROUPER_RUN_APACHE: true
SUCCESS: env var GROUPER_RUN_PROCESSES_AS_USERS: true
SUCCESS: env var GROUPER_RUN_SHIB_SP: true
SUCCESS: env var GROUPER_RUN_TOMEE: true
SUCCESS: env var GROUPER_SCIM: false
SUCCESS: env var GROUPER_SCIM_GROUPER_AUTH: false
SUCCESS: env var GROUPER_TOMCAT_CONTEXT: grouper
SUCCESS: env var GROUPER_UI: true
SUCCESS: env var GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES: 127.0.0.1/32
SUCCESS: env var GROUPER_UI_GROUPER_AUTH: false
SUCCESS: env var GROUPER_UI_ONLY: true
SUCCESS: env var GROUPER_URL_CONTEXT: grouper
SUCCESS: env var GROUPER_USE_SSL: true
SUCCESS: env var GROUPER_WS: false
SUCCESS: env var GROUPER_WS_GROUPER_AUTH: false
SUCCESS: tomcat process count: 1
SUCCESS: apache process count: 0
SUCCESS: shib process count: 1
SUCCESS: not listening on port 443: 0
SUCCESS: not listening on port 80: 0
SUCCESS: listening on port 8009: 1
SUCCESS: not listening on port 9001: 0
SUCCESS: listening on port 8080: 1
grouper-test

################
Running container as ui without SSL
docker run --detach --name grouper-test --publish 443:443 -e GROUPER_USE_SSL=false -e GROUPER_TOMCAT_LOG_ACCESS=true -e GROUPER_APACHE_DIRECTORY_INDEXES=true i2incommon/grouper:2.6.0 ui
################

c5c8869d42dd5bed8f01ab18abbe56798a3a74a02bb39fd762970ea21e0d5630
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf.dontuse should exist: 1
SUCCESS: file /etc/httpd/conf.d/ssl.conf.dontuse should exist: 1
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not exist: 0
SUCCESS: file /etc/httpd/conf.d/ssl.conf should not exist: 0
SUCCESS: file /etc/httpd/conf/httpd.conf should contain at least one 'Options Indexes': '0' < '1'
SUCCESS: file /etc/httpd/conf/httpd.conf should contain at least one 'Listen 80': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:shibbolethsp': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:tomee': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:httpd': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'user=shibd': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should not contain '__': 0
SUCCESS: file /opt/tomee/conf/server.xml should contain at least one 'AccessLogValve': '0' < '1'
SUCCESS: env var GROUPER_TOMCAT_LOG_ACCESS: true
SUCCESS: env var GROUPERSCIM_PROXY_PASS: #
SUCCESS: env var GROUPERSCIM_URL_CONTEXT: grouper-ws-scim
SUCCESS: env var GROUPERWS_PROXY_PASS: #
SUCCESS: env var GROUPERWS_URL_CONTEXT: grouper-ws
SUCCESS: env var GROUPER_APACHE_NONSSL_PORT: 80
SUCCESS: env var GROUPER_APACHE_SSL_PORT: 443
SUCCESS: env var GROUPER_CHOWN_DIRS: true
SUCCESS: env var GROUPER_CONTAINER_VERSION: 2.6.0
SUCCESS: env var GROUPER_DAEMON: false
SUCCESS: env var GROUPER_GSH_CHECK_USER: true
SUCCESS: env var GROUPER_GSH_USER: tomcat
SUCCESS: env var GROUPER_HOME: /opt/grouper/grouperWebapp/WEB-INF
SUCCESS: env var GROUPER_LOG_PREFIX: grouper-ui
SUCCESS: env var GROUPER_MAX_MEMORY: 1500m
SUCCESS: env var GROUPER_PROXY_PASS:
SUCCESS: env var GROUPER_RUN_APACHE: true
SUCCESS: env var GROUPER_RUN_PROCESSES_AS_USERS: true
SUCCESS: env var GROUPER_RUN_SHIB_SP: true
SUCCESS: env var GROUPER_RUN_TOMEE: true
SUCCESS: env var GROUPER_SCIM: false
SUCCESS: env var GROUPER_SCIM_GROUPER_AUTH: false
SUCCESS: env var GROUPER_TOMCAT_CONTEXT: grouper
SUCCESS: env var GROUPER_UI: true
SUCCESS: env var GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES: 127.0.0.1/32
SUCCESS: env var GROUPER_UI_GROUPER_AUTH: false
SUCCESS: env var GROUPER_UI_ONLY: true
SUCCESS: env var GROUPER_URL_CONTEXT: grouper
SUCCESS: env var GROUPER_USE_SSL: false
SUCCESS: env var GROUPER_WS: false
SUCCESS: env var GROUPER_WS_GROUPER_AUTH: false
SUCCESS: tomcat process count: 1
SUCCESS: apache process count: 5
SUCCESS: shib process count: 1
SUCCESS: not listening on port 443: 0
SUCCESS: listening on port 80: 1
SUCCESS: listening on port 8009: 1
SUCCESS: not listening on port 9001: 0
grouper-test

################
Running container as ui with slashRoot mounted
docker run --detach --name grouper-test --mount type=bind,src=,dst=/opt/grouper/slashRoot --publish 443:443 i2incommon/grouper:2.6.0 ui
################

07a727c258dcdfe24aba0504de884f8159f85272fda656f3d10402e3a417806b
SUCCESS: file /tmp/temp.txt should exist: 1
grouper-test

################
Running container as ui with self signed cert
docker run --detach --name grouper-test --publish 443:443 -e GROUPER_SELF_SIGNED_CERT=true -e GROUPER_LOG_TO_HOST=true i2incommon/grouper:2.6.0 ui
################

3c2ee7a4f2d966042fa8e11ac7c2cd7b04ee6e3d222b3ddc4d393e9ddeb2c59d
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain 'cachain.pem': 0
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should contain at least one '/etc/pki/tls/certs/localhost.crt': '0' < '2'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one 'ProxyPass /grouper ajp://localhost:8009/grouper timeout=3600': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one '#ProxyPass /grouper-ws ajp://localhost:8009/grouper timeout=3600': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one '#ProxyPass /grouper-ws-scim ajp://localhost:8009/grouper timeout=3600': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one '"/grouper/"': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should not contain '__': 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/log4j.properties should not contain '/tmp/logpipe': 0
SUCCESS: env var GROUPERSCIM_PROXY_PASS: #
SUCCESS: env var GROUPERSCIM_URL_CONTEXT: grouper-ws-scim
SUCCESS: env var GROUPERWS_PROXY_PASS: #
SUCCESS: env var GROUPERWS_URL_CONTEXT: grouper-ws
SUCCESS: env var GROUPER_APACHE_NONSSL_PORT: 80
SUCCESS: env var GROUPER_APACHE_SSL_PORT: 443
SUCCESS: env var GROUPER_CHOWN_DIRS: true
SUCCESS: env var GROUPER_CONTAINER_VERSION: 2.6.0
SUCCESS: env var GROUPER_DAEMON: false
SUCCESS: env var GROUPER_GSH_CHECK_USER: true
SUCCESS: env var GROUPER_GSH_USER: tomcat
SUCCESS: env var GROUPER_HOME: /opt/grouper/grouperWebapp/WEB-INF
SUCCESS: env var GROUPER_LOG_PREFIX: grouper-ui
SUCCESS: env var GROUPER_MAX_MEMORY: 1500m
SUCCESS: env var GROUPER_PROXY_PASS:
SUCCESS: env var GROUPER_RUN_APACHE: true
SUCCESS: env var GROUPER_RUN_PROCESSES_AS_USERS: true
SUCCESS: env var GROUPER_RUN_SHIB_SP: true
SUCCESS: env var GROUPER_RUN_TOMEE: true
SUCCESS: env var GROUPER_SCIM: false
SUCCESS: env var GROUPER_SCIM_GROUPER_AUTH: false
SUCCESS: env var GROUPER_SELF_SIGNED_CERT: true
SUCCESS: env var GROUPER_TOMCAT_CONTEXT: grouper
SUCCESS: env var GROUPER_UI: true
SUCCESS: env var GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES: 127.0.0.1/32
SUCCESS: env var GROUPER_UI_GROUPER_AUTH: false
SUCCESS: env var GROUPER_UI_ONLY: true
SUCCESS: env var GROUPER_URL_CONTEXT: grouper
SUCCESS: env var GROUPER_USE_SSL: true
SUCCESS: env var GROUPER_WS: false
SUCCESS: env var GROUPER_WS_GROUPER_AUTH: false
SUCCESS: tomcat process count: 1
SUCCESS: apache process count: 5
SUCCESS: shib process count: 1
grouper-test

################
Running container as ui with self signed cert with different ports
docker run --detach --name grouper-test --publish 443:443 -e GROUPER_APACHE_AJP_TIMEOUT_SECONDS=2999 -e GROUPER_SELF_SIGNED_CERT=true -e GROUPER_APACHE_SSL_PORT=444 -e GROUPER_APACHE_NONSSL_PORT=81 -e GROUPER_TOMCAT_HTTP_PORT=8600 -e GROUPER_TOMCAT_AJP_PORT=8601 -e GROUPER_TOMCAT_SHUTDOWN_PORT=8602 i2incommon/grouper:2.6.0 ui
################

7bde963dcba852105a2d726d96810187c44b8abd656e97308c5d40aa4b4891b8
SUCCESS: env var GROUPER_APACHE_NONSSL_PORT: 81
SUCCESS: env var GROUPER_APACHE_SSL_PORT: 444
SUCCESS: env var GROUPER_APACHE_AJP_TIMEOUT_SECONDS: 2999
SUCCESS: env var GROUPER_TOMCAT_HTTP_PORT: 8600
SUCCESS: env var GROUPER_TOMCAT_AJP_PORT: 8601
SUCCESS: env var GROUPER_TOMCAT_SHUTDOWN_PORT: 8602
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one '2999': '0' < '5'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should not contain '3600': 0
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should not contain '2400': 0
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain 'Listen 443 https': 0
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should contain at least one 'Listen 444 https': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf/httpd.conf should not contain 'Listen 80': 0
SUCCESS: file /etc/httpd/conf/httpd.conf should contain at least one 'Listen 81': '0' < '1'
SUCCESS: tomcat process count: 1
SUCCESS: apache process count: 5
SUCCESS: shib process count: 1
SUCCESS: listening on port 444: 1
SUCCESS: listening on port 81: 1
SUCCESS: not listening on port 443: 0
SUCCESS: not listening on port 80: 0
SUCCESS: listening on port 8600: 1
SUCCESS: listening on port 8601: 1
SUCCESS: not listening on port 9001: 0
grouper-test

################
Running container as scim
docker run --detach --name grouper-test --publish 443:443 -e GROUPER_SELF_SIGNED_CERT=true i2incommon/grouper:2.6.0 scim
################

7f02e0cdee107ee8e246b41db974233526a8acd8c569e6946646678b0a791457
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libWs/axis2-kernel-1.6.4.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/axis2-kernel-1.6.4.jar should not exist: 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libScim/stax-api-1.0-2.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/stax-api-1.0-2.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/grouper-messaging-activemq-2.5.40.jar should not exist: 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libUiAndDaemon/grouper-messaging-activemq-2.5.40.jar should exist: 1
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should contain at least one 'Listen 443 https': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf/httpd.conf should contain at least one 'Listen 80': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should not contain 'program:shibbolethsp': 0
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:tomee': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:httpd': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should not contain 'user=shibd': 0
SUCCESS: file /opt/tier-support/supervisord.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain 'cachain.pem': 0
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should contain at least one '/etc/pki/tls/certs/localhost.crt': '0' < '2'
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/log4j.properties should contain at least one 'grouper-scim;': '0' < '4'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one '3600': '0' < '5'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should not contain '__': 0
SUCCESS: env var GROUPERSCIM_PROXY_PASS:
SUCCESS: env var GROUPERSCIM_URL_CONTEXT: grouper-ws-scim
SUCCESS: env var GROUPERWS_PROXY_PASS: #
SUCCESS: env var GROUPERWS_URL_CONTEXT: grouper-ws
SUCCESS: env var GROUPER_APACHE_AJP_TIMEOUT_SECONDS: 3600
SUCCESS: env var GROUPER_APACHE_NONSSL_PORT: 80
SUCCESS: env var GROUPER_APACHE_SSL_PORT: 443
SUCCESS: env var GROUPER_CHOWN_DIRS: true
SUCCESS: env var GROUPER_CONTAINER_VERSION: 2.6.0
SUCCESS: env var GROUPER_DAEMON: false
SUCCESS: env var GROUPER_GSH_CHECK_USER: true
SUCCESS: env var GROUPER_GSH_USER: tomcat
SUCCESS: env var GROUPER_HOME: /opt/grouper/grouperWebapp/WEB-INF
SUCCESS: env var GROUPER_LOG_PREFIX: grouper-scim
SUCCESS: env var GROUPER_MAX_MEMORY: 1500m
SUCCESS: env var GROUPER_PROXY_PASS: #
SUCCESS: env var GROUPER_RUN_APACHE: true
SUCCESS: env var GROUPER_RUN_PROCESSES_AS_USERS: true
SUCCESS: env var GROUPER_RUN_SHIB_SP: not equal to: 'true', is: ''
SUCCESS: env var GROUPER_RUN_TOMEE: true
SUCCESS: env var GROUPER_SCIM: true
SUCCESS: env var GROUPER_SCIM_GROUPER_AUTH: false
SUCCESS: env var GROUPER_TOMCAT_CONTEXT: grouper-ws-scim
SUCCESS: env var GROUPER_UI: false
SUCCESS: env var GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES: 127.0.0.1/32
SUCCESS: env var GROUPER_UI_GROUPER_AUTH: false
SUCCESS: env var GROUPER_URL_CONTEXT: grouper
SUCCESS: env var GROUPER_USE_SSL: true
SUCCESS: env var GROUPER_WS: false
SUCCESS: env var GROUPER_WS_GROUPER_AUTH: false
SUCCESS: env var GROUPER_WS_ONLY: not equal to: 'true', is: ''
SUCCESS: tomcat process count: 1
SUCCESS: apache process count: 5
SUCCESS: shib process count: 0
SUCCESS: listening on port 443: 1
SUCCESS: listening on port 80: 1
SUCCESS: listening on port 8009: 1
SUCCESS: not listening on port 9001: 0
grouper-test

################
Running container as ws
docker run --detach --name grouper-test --publish 443:443 -e GROUPER_SELF_SIGNED_CERT=true -e GROUPER_APACHE_SERVER_NAME=https://a.b.c:443 i2incommon/grouper:2.6.0 ws
################

2c16674270f699410aab13482612c28958c49f0a84884b26f12ed56b70907d64
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libWs/axis2-kernel-1.6.4.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/axis2-kernel-1.6.4.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libScim/stax-api-1.0-2.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/stax-api-1.0-2.jar should not exist: 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/grouper-messaging-activemq-2.5.40.jar should not exist: 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libUiAndDaemon/grouper-messaging-activemq-2.5.40.jar should exist: 1
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should contain at least one 'Listen 443 https': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf/httpd.conf should contain at least one 'Listen 80': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should not contain 'program:shibbolethsp': 0
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:tomee': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:httpd': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should not contain 'user=shibd': 0
SUCCESS: file /opt/tier-support/supervisord.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain 'cachain.pem': 0
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should contain at least one '/etc/pki/tls/certs/localhost.crt': '0' < '2'
SUCCESS: file /opt/tomee/conf/Catalina/localhost/grouper-ws.xml should contain at least one 'cookies="false"': '0' < '1'
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/log4j.properties should contain at least one 'grouper-ws;': '0' < '4'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one '3600': '0' < '5'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one 'ServerName https://a.b.c:443': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one 'UseCanonicalName On': '0' < '1'
SUCCESS: env var GROUPER_APACHE_SERVER_NAME: https://a.b.c:443
SUCCESS: env var GROUPERSCIM_PROXY_PASS: #
SUCCESS: env var GROUPERSCIM_URL_CONTEXT: grouper-ws-scim
SUCCESS: env var GROUPERWS_PROXY_PASS:
SUCCESS: env var GROUPERWS_URL_CONTEXT: grouper-ws
SUCCESS: env var GROUPER_APACHE_AJP_TIMEOUT_SECONDS: 3600
SUCCESS: env var GROUPER_APACHE_NONSSL_PORT: 80
SUCCESS: env var GROUPER_APACHE_SSL_PORT: 443
SUCCESS: env var GROUPER_CHOWN_DIRS: true
SUCCESS: env var GROUPER_CONTAINER_VERSION: 2.6.0
SUCCESS: env var GROUPER_DAEMON: false
SUCCESS: env var GROUPER_GSH_CHECK_USER: true
SUCCESS: env var GROUPER_GSH_USER: tomcat
SUCCESS: env var GROUPER_HOME: /opt/grouper/grouperWebapp/WEB-INF
SUCCESS: env var GROUPER_LOG_PREFIX: grouper-ws
SUCCESS: env var GROUPER_MAX_MEMORY: 1500m
SUCCESS: env var GROUPER_PROXY_PASS: #
SUCCESS: env var GROUPER_RUN_APACHE: true
SUCCESS: env var GROUPER_RUN_PROCESSES_AS_USERS: true
SUCCESS: env var GROUPER_RUN_SHIB_SP: not equal to: 'true', is: ''
SUCCESS: env var GROUPER_RUN_TOMEE: true
SUCCESS: env var GROUPER_SCIM: false
SUCCESS: env var GROUPER_SCIM_GROUPER_AUTH: false
SUCCESS: env var GROUPER_TOMCAT_CONTEXT: grouper-ws
SUCCESS: env var GROUPER_UI: false
SUCCESS: env var GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES: 127.0.0.1/32
SUCCESS: env var GROUPER_UI_GROUPER_AUTH: false
SUCCESS: env var GROUPER_UI_ONLY: not equal to: 'true', is: ''
SUCCESS: env var GROUPER_URL_CONTEXT: grouper
SUCCESS: env var GROUPER_USE_SSL: true
SUCCESS: env var GROUPER_WS: true
SUCCESS: env var GROUPER_WS_GROUPER_AUTH: false
SUCCESS: env var GROUPER_WS_ONLY: true
SUCCESS: tomcat process count: 1
SUCCESS: apache process count: 5
SUCCESS: shib process count: 0
SUCCESS: listening on port 443: 1
SUCCESS: listening on port 80: 1
SUCCESS: listening on port 8009: 1
SUCCESS: not listening on port 9001: 0
grouper-test

################
Running container as quickstart
docker run --detach --name grouper-test --publish 443:443 -e GROUPER_MORPHSTRING_ENCRYPT_KEY=abcdefg12345dontUseThis \
-e GROUPERSYSTEM_QUICKSTART_PASS=thisPassIsCopyrightedDontUse i2incommon/grouper:2.6.0 quickstart
################

b75d3a2df22b07c20a2cb177a8470f8d9d31e413a97c6507b4d1e7dfdf138a7b
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libWs/axis2-kernel-1.6.4.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/axis2-kernel-1.6.4.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libScim/stax-api-1.0-2.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/stax-api-1.0-2.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/grouper-messaging-activemq-2.5.40.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libUiAndDaemon/grouper-messaging-activemq-2.5.40.jar should exist: 1
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should contain at least one 'Listen 443 https': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf/httpd.conf should contain at least one 'Listen 80': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should not contain 'program:shibbolethsp': 0
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:tomee': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:httpd': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:hsqldb': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should not contain 'user=shibd': 0
SUCCESS: file /opt/tier-support/supervisord.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain 'cachain.pem': 0
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should contain at least one '/etc/pki/tls/certs/localhost.crt': '0' < '2'
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties should contain at least one 'grouperPasswordConfigOverride_UI_GrouperSystem_pass.elConfig': '0' < '1'
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties should contain at least one 'GROUPERSYSTEM_QUICKSTART_PASS': '0' < '2'
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/log4j.properties should contain at least one 'grouper;': '0' < '4'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one '3600': '0' < '5'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should not contain '__': 0
SUCCESS: env var GROUPERSCIM_PROXY_PASS:
SUCCESS: env var GROUPERSCIM_URL_CONTEXT: grouper-ws-scim
SUCCESS: env var GROUPERWS_PROXY_PASS:
SUCCESS: env var GROUPERWS_URL_CONTEXT: grouper-ws
SUCCESS: env var GROUPER_APACHE_AJP_TIMEOUT_SECONDS: 3600
SUCCESS: env var GROUPER_APACHE_NONSSL_PORT: 80
SUCCESS: env var GROUPER_APACHE_SSL_PORT: 443
SUCCESS: env var GROUPER_CHOWN_DIRS: true
SUCCESS: env var GROUPER_CONTAINER_VERSION: 2.6.0
SUCCESS: env var GROUPER_DAEMON: true
SUCCESS: env var GROUPER_GSH_CHECK_USER: true
SUCCESS: env var GROUPER_GSH_USER: tomcat
SUCCESS: env var GROUPER_HOME: /opt/grouper/grouperWebapp/WEB-INF
SUCCESS: env var GROUPER_LOG_PREFIX: grouper
SUCCESS: env var GROUPER_MAX_MEMORY: 1500m
SUCCESS: env var GROUPER_PROXY_PASS:
SUCCESS: env var GROUPER_RUN_APACHE: true
SUCCESS: env var GROUPER_RUN_PROCESSES_AS_USERS: true
SUCCESS: env var GROUPER_RUN_SHIB_SP: false
SUCCESS: env var GROUPER_RUN_TOMEE: true
SUCCESS: env var GROUPER_SCIM: true
SUCCESS: env var GROUPER_SCIM_GROUPER_AUTH: true
SUCCESS: env var GROUPER_TOMCAT_CONTEXT: grouper
SUCCESS: env var GROUPER_UI: true
SUCCESS: env var GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES: 0.0.0.0/0
SUCCESS: env var GROUPER_UI_GROUPER_AUTH: true
SUCCESS: env var GROUPER_UI_ONLY: not equal to: 'true', is: ''
SUCCESS: env var GROUPER_URL_CONTEXT: grouper
SUCCESS: env var GROUPER_USE_SSL: true
SUCCESS: env var GROUPER_WS: true
SUCCESS: env var GROUPER_WS_GROUPER_AUTH: true
SUCCESS: tomcat process count: 2
SUCCESS: apache process count: 5
SUCCESS: shib process count: 0
SUCCESS: listening on port 443: 1
SUCCESS: listening on port 80: 1
SUCCESS: listening on port 8009: 1
SUCCESS: listening on port 9001: 1
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   210  100   210    0     0   8750      0 --:--:-- --:--:-- --:--:--  9130
100   243  100   243    0     0      8      0  0:00:30  0:00:28  0:00:02    72
SUCCESS: file index.html should contain at least one 'document.location.href': '0' < '1'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   719  100   719    0     0  39944      0 --:--:-- --:--:-- --:--:-- 39944
SUCCESS: file index.html should contain at least one 'HTTP Status 401': '0' < '1'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   719  100   719    0     0  39944      0 --:--:-- --:--:-- --:--:-- 39944
SUCCESS: file index.html should contain at least one 'HTTP Status 401': '0' < '1'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 11571    0 11571    0     0  24991      0 --:--:-- --:--:-- --:--:-- 24991
SUCCESS: file index.html should contain at least one 'end index.jsp': '0' < '1'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   719  100   719    0     0  35950      0 --:--:-- --:--:-- --:--:-- 35950
SUCCESS: file index.html should contain at least one 'HTTP Status 401': '0' < '1'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   719  100   719    0     0  32681      0 --:--:-- --:--:-- --:--:-- 34238
SUCCESS: file index.html should contain at least one 'HTTP Status 401': '0' < '1'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   298  100   298    0     0   2759      0 --:--:-- --:--:-- --:--:--  2733
SUCCESS: file index.html should contain at least one '"resultCode":"SUCCESS"': '0' < '1'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   719  100   719    0     0  47933      0 --:--:-- --:--:-- --:--:-- 47933
SUCCESS: file index.html should contain at least one 'HTTP Status 401': '0' < '1'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   719  100   719    0     0  39944      0 --:--:-- --:--:-- --:--:-- 39944
SUCCESS: file index.html should contain at least one 'HTTP Status 401': '0' < '1'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  4897    0  4897    0     0  17303      0 --:--:-- --:--:-- --:--:-- 17303
SUCCESS: file index.html should contain at least one 'etc:workflowEditors': '0' < '1'
grouper-test
grouper-test
SUCCESS: tomcat process count: 2
SUCCESS: apache process count: 5
SUCCESS: shib process count: 0
SUCCESS: listening on port 443: 1
SUCCESS: listening on port 80: 1
SUCCESS: listening on port 8009: 1
SUCCESS: listening on port 9001: 1
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   210  100   210    0     0  19090      0 --:--:-- --:--:-- --:--:-- 19090
100   243  100   243    0     0     11      0  0:00:22  0:00:20  0:00:02    59
SUCCESS: file index.html should contain at least one 'document.location.href': '0' < '1'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 11571    0 11571    0     0  70554      0 --:--:-- --:--:-- --:--:-- 70554
SUCCESS: file index.html should contain at least one 'end index.jsp': '0' < '1'
SUCCESS: ps -ef | grep root | grep cat | grep -v grep | wc -l: 6
SUCCESS: ps -ef | grep root | grep awk | grep supervisord | wc -l: 1
SUCCESS: ps -ef | grep root | grep awk | grep grouper | wc -l: 1
SUCCESS: ps -ef | grep root | grep awk | grep httpd | wc -l: 1
SUCCESS: ps -ef | grep root | grep awk | grep tomee | wc -l: 1
grouper-test

################
Running container as daemon
docker run --detach --name grouper-test --publish 443:443 i2incommon/grouper:2.6.0 daemon
################

a9ef0f2794d2333fd96a5fe4ec715c47aeae34566fa3b4b0361ea2160376ced5
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libWs/axis2-kernel-1.6.4.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/axis2-kernel-1.6.4.jar should not exist: 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libScim/stax-api-1.0-2.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/stax-api-1.0-2.jar should not exist: 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/grouper-messaging-activemq-2.5.40.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libUiAndDaemon/grouper-messaging-activemq-2.5.40.jar should exist: 1
SUCCESS: file /opt/tier-support/supervisord.conf should not contain 'program:shibbolethsp': 0
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:tomee': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should not contain 'program:httpd': 0
SUCCESS: file /opt/tier-support/supervisord.conf should not contain 'program:hsqldb': 0
SUCCESS: file /opt/tier-support/supervisord.conf should not contain 'user=shibd': 0
SUCCESS: file /opt/tier-support/supervisord.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one '3600': '0' < '5'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should not contain '__': 0
SUCCESS: env var GROUPERSCIM_PROXY_PASS: #
SUCCESS: env var GROUPERSCIM_URL_CONTEXT: grouper-ws-scim
SUCCESS: env var GROUPERWS_PROXY_PASS: #
SUCCESS: env var GROUPERWS_URL_CONTEXT: grouper-ws
SUCCESS: env var GROUPER_APACHE_AJP_TIMEOUT_SECONDS: 3600
SUCCESS: env var GROUPER_APACHE_NONSSL_PORT: 80
SUCCESS: env var GROUPER_APACHE_SSL_PORT: 443
SUCCESS: env var GROUPER_CHOWN_DIRS: true
SUCCESS: env var GROUPER_CONTAINER_VERSION: 2.6.0
SUCCESS: env var GROUPER_DAEMON: true
SUCCESS: env var GROUPER_GSH_CHECK_USER: true
SUCCESS: env var GROUPER_GSH_USER: tomcat
SUCCESS: env var GROUPER_HOME: /opt/grouper/grouperWebapp/WEB-INF
SUCCESS: env var GROUPER_LOG_PREFIX: grouper-daemon
SUCCESS: env var GROUPER_MAX_MEMORY: 1500m
SUCCESS: env var GROUPER_PROXY_PASS: #
SUCCESS: env var GROUPER_RUN_APACHE: not equal to: 'true', is: ''
SUCCESS: env var GROUPER_RUN_PROCESSES_AS_USERS: true
SUCCESS: env var GROUPER_RUN_SHIB_SP: not equal to: 'true', is: ''
SUCCESS: env var GROUPER_RUN_TOMEE: true
SUCCESS: env var GROUPER_SCIM: false
SUCCESS: env var GROUPER_SCIM_GROUPER_AUTH: false
SUCCESS: env var GROUPER_TOMCAT_CONTEXT: grouper
SUCCESS: env var GROUPER_UI: false
SUCCESS: env var GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES: 127.0.0.1/32
SUCCESS: env var GROUPER_UI_GROUPER_AUTH: false
SUCCESS: env var GROUPER_UI_ONLY: not equal to: 'true', is: ''
SUCCESS: env var GROUPER_URL_CONTEXT: grouper
SUCCESS: env var GROUPER_USE_SSL: true
SUCCESS: env var GROUPER_WS: false
SUCCESS: env var GROUPER_WS_GROUPER_AUTH: false
SUCCESS: tomcat process count: 1
SUCCESS: apache process count: 0
SUCCESS: shib process count: 0
SUCCESS: not listening on port 443: 0
SUCCESS: not listening on port 80: 0
SUCCESS: listening on port 8009: 1
SUCCESS: not listening on port 9001: 0
grouper-test

################
Running container with subimage as ui
cat DockerFile

FROM i2incommon/grouper:2.6.0
ENV GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES 1.1.1.1/32

docker build -t my_grouper-test .
docker run --detach --name grouper-test --publish 443:443 my_grouper-test ui
################

Sending build context to Docker daemon   80.9kB
Step 1/2 : FROM i2incommon/grouper:2.6.0
 ---> c7a1b90f68d9
Step 2/2 : ENV GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES 1.1.1.1/32
 ---> Running in 312ea3326e2a
Removing intermediate container 312ea3326e2a
 ---> 97ca578e56d6
Successfully built 97ca578e56d6
Successfully tagged my_grouper-test:latest
5db436cbe03d0e5e1507eaa8fda886f3f737f1a0fd82c8fdf9883a61e8b276d4
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libWs/axis2-kernel-1.6.4.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/axis2-kernel-1.6.4.jar should not exist: 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libScim/stax-api-1.0-2.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/stax-api-1.0-2.jar should not exist: 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/grouper-messaging-activemq-2.5.40.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libUiAndDaemon/grouper-messaging-activemq-2.5.40.jar should exist: 1
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should contain at least one 'Listen 443 https': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf/httpd.conf should contain at least one 'Listen 80': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:shibbolethsp': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:tomee': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'program:httpd': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should contain at least one 'user=shibd': '0' < '1'
SUCCESS: file /opt/tier-support/supervisord.conf should not contain 'program:hsqldb': 0
SUCCESS: file /opt/tier-support/supervisord.conf should not contain '__': 0
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should contain at least one 'cachain.pem': '0' < '1'
SUCCESS: file /etc/httpd/conf.d/ssl-enabled.conf should not contain '/etc/pki/tls/certs/localhost.crt': 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/log4j.properties should contain at least one '/tmp/logpipe': '0' < '4'
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/log4j.properties should contain at least one 'grouper-ui;': '0' < '4'
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties should not contain 'grouperPasswordConfigOverride_UI_GrouperSystem_pass.elConfig': 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties should not contain 'thisPassIsCopyrightedDontUse': 0
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should contain at least one '3600': '0' < '5'
SUCCESS: file /etc/httpd/conf.d/grouper-www.conf should not contain '__': 0
SUCCESS: env var GROUPERSCIM_PROXY_PASS: #
SUCCESS: env var GROUPERSCIM_URL_CONTEXT: grouper-ws-scim
SUCCESS: env var GROUPERWS_PROXY_PASS: #
SUCCESS: env var GROUPERWS_URL_CONTEXT: grouper-ws
SUCCESS: env var GROUPER_APACHE_AJP_TIMEOUT_SECONDS: 3600
SUCCESS: env var GROUPER_APACHE_NONSSL_PORT: 80
SUCCESS: env var GROUPER_APACHE_SSL_PORT: 443
SUCCESS: env var GROUPER_CHOWN_DIRS: true
SUCCESS: env var GROUPER_CONTAINER_VERSION: 2.6.0
SUCCESS: env var GROUPER_DAEMON: false
SUCCESS: env var GROUPER_GSH_CHECK_USER: true
SUCCESS: env var GROUPER_GSH_USER: tomcat
SUCCESS: env var GROUPER_HOME: /opt/grouper/grouperWebapp/WEB-INF
SUCCESS: env var GROUPER_LOG_PREFIX: grouper-ui
SUCCESS: env var GROUPER_MAX_MEMORY: 1500m
SUCCESS: env var GROUPER_PROXY_PASS:
SUCCESS: env var GROUPER_RUN_APACHE: true
SUCCESS: env var GROUPER_RUN_PROCESSES_AS_USERS: true
SUCCESS: env var GROUPER_RUN_SHIB_SP: true
SUCCESS: env var GROUPER_RUN_TOMEE: true
SUCCESS: env var GROUPER_SCIM: false
SUCCESS: env var GROUPER_SCIM_GROUPER_AUTH: false
SUCCESS: env var GROUPER_TOMCAT_CONTEXT: grouper
SUCCESS: env var GROUPER_UI: true
SUCCESS: env var GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES: 1.1.1.1/32
SUCCESS: env var GROUPER_UI_GROUPER_AUTH: false
SUCCESS: env var GROUPER_UI_ONLY: true
SUCCESS: env var GROUPER_URL_CONTEXT: grouper
SUCCESS: env var GROUPER_USE_SSL: true
SUCCESS: env var GROUPER_WS: false
SUCCESS: env var GROUPER_WS_GROUPER_AUTH: false
SUCCESS: tomcat process count: 1
SUCCESS: apache process count: 0
SUCCESS: shib process count: 1
SUCCESS: not listening on port 443: 0
SUCCESS: not listening on port 80: 0
SUCCESS: listening on port 8009: 1
SUCCESS: not listening on port 9001: 0
grouper-test
Untagged: my_grouper-test:latest
Deleted: sha256:97ca578e56d6daf0bc6a7b2b3f1d1ff9e2e74f9e1f77e0c36f864a7a3527ccbb

################
Running container with subimage as ui without root
cat DockerFile

FROM i2incommon/grouper:2.6.0
RUN /usr/local/bin/changeUid.sh tomcat 1000

docker build -t my_grouper-test .
docker run --detach --name grouper-test -u 1000 -e GROUPER_RUN_TOMCAT_NOT_SUPERVISOR=true --publish 8080:8080 my_grouper-test ui
################

Sending build context to Docker daemon   80.9kB
Step 1/2 : FROM i2incommon/grouper:2.6.0
 ---> c7a1b90f68d9
Step 2/2 : RUN /usr/local/bin/changeUid.sh tomcat 1000
 ---> Running in f2fa86e25618
grouperContainer; INFO: (changeUid.sh) usermod -u 1000 tomcat , result: 0
grouperContainer; INFO: (changeUid.sh) find / -xdev -type d -user 998 -exec chown -h tomcat {} \; , result: 0
Removing intermediate container f2fa86e25618
 ---> fba191c5bea7
Successfully built fba191c5bea7
Successfully tagged my_grouper-test:latest
0289dd35ae64ad36946bba48e0eb8c175885c7a8231a8b046e0330d61fae9010
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libWs/axis2-kernel-1.6.4.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/axis2-kernel-1.6.4.jar should not exist: 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libScim/stax-api-1.0-2.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/stax-api-1.0-2.jar should not exist: 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/lib/grouper-messaging-activemq-2.5.40.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/libUiAndDaemon/grouper-messaging-activemq-2.5.40.jar should exist: 1
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/log4j.properties should contain at least one '/tmp/logpipe': '0' < '4'
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/log4j.properties should contain at least one 'grouper-ui;': '0' < '4'
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties should not contain 'grouperPasswordConfigOverride_UI_GrouperSystem_pass.elConfig': 0
SUCCESS: file /opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties should not contain 'thisPassIsCopyrightedDontUse': 0
SUCCESS: env var GROUPERSCIM_PROXY_PASS: #
SUCCESS: env var GROUPERSCIM_URL_CONTEXT: grouper-ws-scim
SUCCESS: env var GROUPERWS_PROXY_PASS: #
SUCCESS: env var GROUPERWS_URL_CONTEXT: grouper-ws
SUCCESS: env var GROUPER_APACHE_AJP_TIMEOUT_SECONDS: 3600
SUCCESS: env var GROUPER_APACHE_NONSSL_PORT: 80
SUCCESS: env var GROUPER_APACHE_SSL_PORT: 443
SUCCESS: env var GROUPER_CHOWN_DIRS: true
SUCCESS: env var GROUPER_CONTAINER_VERSION: 2.6.0
SUCCESS: env var GROUPER_DAEMON: false
SUCCESS: env var GROUPER_GSH_CHECK_USER: true
SUCCESS: env var GROUPER_GSH_USER: tomcat
SUCCESS: env var GROUPER_HOME: /opt/grouper/grouperWebapp/WEB-INF
SUCCESS: env var GROUPER_LOG_PREFIX: grouper-ui
SUCCESS: env var GROUPER_MAX_MEMORY: 1500m
SUCCESS: env var GROUPER_PROXY_PASS:
SUCCESS: env var GROUPER_RUN_APACHE: not equal to: 'true', is: ''
SUCCESS: env var GROUPER_RUN_PROCESSES_AS_USERS: true
SUCCESS: env var GROUPER_RUN_SHIB_SP: not equal to: 'true', is: ''
SUCCESS: env var GROUPER_RUN_TOMEE: true
SUCCESS: env var GROUPER_SCIM: false
SUCCESS: env var GROUPER_SCIM_GROUPER_AUTH: false
SUCCESS: env var GROUPER_TOMCAT_CONTEXT: grouper
SUCCESS: env var GROUPER_UI: true
SUCCESS: env var GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES: 127.0.0.1/32
SUCCESS: env var GROUPER_UI_GROUPER_AUTH: false
SUCCESS: env var GROUPER_UI_ONLY: true
SUCCESS: env var GROUPER_URL_CONTEXT: grouper
SUCCESS: env var GROUPER_USE_SSL: true
SUCCESS: env var GROUPER_WS: false
SUCCESS: env var GROUPER_WS_GROUPER_AUTH: false
SUCCESS: tomcat process count: 13
SUCCESS: apache process count: 0
SUCCESS: shib process count: 0
SUCCESS: not listening on port 443: 0
SUCCESS: not listening on port 80: 0
SUCCESS: listening on port 8009: 1
SUCCESS: not listening on port 9001: 0
grouper-test
Untagged: my_grouper-test:latest
Deleted: sha256:fba191c5bea76dc7d5d39fbb9eb205e76301368ff5b4d0dc4609713e5bab26f4
Deleted: sha256:7c6292b2131ce0111bb3c4be8ef34ecd646591a74a8c3f6f72fb3c343131b96b

554 successes, 0 failures
SUCCESS!

mchyzer@ISC20-0637-WL:~/containerTest$
```

## Develop and test

To develop and test while developing (before container is released), you can use this script:

```
$ ./rebuildTestContainer.sh 2.6.0 /mnt/c/mchyzer/git/grouper_container
```

This will copy all the scripts and files from slashRoot and make a subimage. You can test the subimage just like testing the real grouper image
