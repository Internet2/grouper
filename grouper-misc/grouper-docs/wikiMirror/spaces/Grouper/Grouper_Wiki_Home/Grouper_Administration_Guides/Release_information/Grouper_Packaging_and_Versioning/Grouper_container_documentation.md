---
title: "Grouper container documentation"
space: Grouper
pageId: 28549678
version: 189
lastUpdated: 2026-07-12T05:24:29.596Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation
---

The Grouper project currently supports Grouper v2.5+ running only in the i2incommon container (including of course subcontainers). If you want to take pieces out and run in an unsupported way feel free to do that on your own. If you have questions about how to do something with the container that you think you cannot do and think you need to do surgery, please [discuss it with the Grouper team](https://www.internet2.edu/communities-groups/middleware/grouper-working-group/#group-participate) first, there is likely a way to do it with the existing container.  We are open to changing how the container is structured in v2.6, lets discuss and improve together.

This is how the Grouper container works in v2.5

## Directory structure

alpha order...

| **Path** | **Description** |
| --- | --- |
| /etc/httpd/conf.d/grouper-www.conf (removed in v5) | Apache config for Grouper. Includes Shibboleth directive if using Shibboleth |
| /etc/shibboleth/shibboleth2.xml (removed in v5) | Shibboleth config |
| /opt/grouper/ | Grouper base dir |
| /opt/grouper/grouperWebapp/ | J2EE webapp dir for grouper |
| /opt/grouper/grouperWebapp/WEB-INF/classes/ | Grouper config files |
| /opt/grouper/grouperWebapp/WEB-INF/classes/log4j.properties (before v2.6.5) | Log file |
| /opt/grouper/grouperWebapp/WEB-INF/classes/log4j2.xml (v2.6.7.1+) | Log file, replace if you like |
| /opt/grouper/grouperWebapp/WEB-INF/classes/log4j_additional.properties (before v2.6.5) | If you want to append some configurations to the built-in log4j.properties, put those here (2.5.41+) |
| /opt/grouper/grouperWebapp/WEB-INF/classes/log4j2.additionalLoggers.xml.txt (v2.6.7.1+) | If you want to put loggers in, put in this file, they will be substituted into the stock log4j2.xml e.g.   ```         <Logger name="edu.a.b.c" level="debug" additivity="false">             <AppenderRef ref="grouper_error"/>         </Logger>   ``` |
| /opt/grouper/grouperWebapp/WEB-INF/classes/log4j2.additionalAppenders.xml.txt (v2.6.8+) | If you want to put appenders in, put in this file, they will be substituted into the stock log4j2.xml e.g.   ```         <RollingFile name="some_appender_name" fileName="/opt/grouper/logs/someFile.log"                  filePattern="/opt/grouper/logs/someFile.log.%d{yyyy-MM-dd}" >             <PatternLayout pattern="someFile.log;${ENV};${USERTOKEN};${layout}"/>             <Policies>                 <TimeBasedTriggeringPolicy interval="1"/>             </Policies>             <DefaultRolloverStrategy max="30" />         </RollingFile>     ``` |
| /opt/grouper/grouperWebapp/WEB-INF/classes/grouperText/grouper.text.en.us.properties | Externalized text file (note in 2.5.33+ this can be imported into database config) |
| /opt/grouper/grouperWebapp/WEB-INF/ddlScripts | DDL scripts that are run automatically or not, are written here |
| /opt/grouper/grouperWebapp/WEB-INF/libUiAndDaemon/ | Jars used in UI and daemon only. Place custom change log consumers here |
| /opt/grouper/grouperWebapp/WEB-INF/lib/ | Jars used in all services (UI/WS/daemon/scim). Replace database drivers or add jars for all services |
| /opt/grouper/grouperWebapp/WEB-INF/web.xml | web.xml for grouper might need security settings for authentication (e.g. tomcat LDAP authn for WS) |
| /opt/grouper/logs | If you are externalizing logs to a "mount", this is the suggested standard location |
| /opt/grouper/slashRoot | Any files or folders in here will be overlaid on / (root dir). This is useful for lower maturity levels   so you only need one mount to copy files in and one mount to copy files out.  If you have a dir in your host, lets say "/foo/bar/slashRoot", and you mount that to the container     "/opt/grouper/slashRoot", then if you make the file:    /foobar/slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties,    then it will exist in the container at: /opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties |
| /opt/container_files/certs | built in self signed certs |
| /opt/grouper/certs/anchors/*.pem | [SSL anchor certs](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554579/Grouper+v2.5+container+SSL+trust+management) |
| /opt/grouper/certs/client/*.pem | [SSL certs](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554579/Grouper+v2.5+container+SSL+trust+management) (to connect from Grouper to external systems), not for client authentication necessarily |
| /opt/grouper/certs/keys/*.key | SSL cert private keys (or could go somewhere else) |
| /opt/hsqldb   (removed in 2.6.5+) | HSQLDB directory if using an embedded database (for quick starts or demos) |
| /opt/tomee (v2.5, v2.6)  /opt/tomcat (v4+) | Tomee/Tomcat app server |
| /opt/tomee/bin (v2.5, v2.6)  /opt/tomcat/bin (v4+) | Startup and shutdown scripts for tomee/tomcat |
| /opt/tomee/conf/server.xml (v2.5, v2.6)  /opt/tomcat/conf/server.xml (v4+) | Tomee/Tomcat server.xml might need settings for authentication (e.g. tomcat LDAP authn for WS) |
| /usr/lib/jvm/java-1.8.0-amazon-corretto (v2.5, v2.6)  /usr/lib/jvm/java-17-amazon-corretto (v4) | JAVA_HOME (do not hard code this path, use the ENV var) |
| /usr/lib/jvm/java-1.8.0-amazon-corretto/jre/lib/security/cacerts (v2.5, v2.6)  /usr/lib/jvm/java-17-amazon-corretto/jre/lib/security/cacerts (v4) | cacerts for java |

## Ports and configs

| **Port** | **Service** | **Description** | **Config file** |
| --- | --- | --- | --- |
| 443 (removed in v5) | Apache | Apache listens to requests and reverse proxies to tomcat 8009.   The apache URL path: /grouper → tomcat /grouper   The apache URL path: /grouper-ws → tomcat /grouper   The apache URL path: /grouper -scim → tomcat /grouper  Note, v5 there is no apache and tomcat listens on 8443 by default. | /etc/httpd/conf.d/grouper-www.conf |
| 8009 | AJP | Tomcat listens here to get reverse proxied requests    from apache (or another web server.   Note, if you use an external apache you can link that up with   the internal apache or expose the 8009 (in a secured way!) | /opt/tomee/conf/server.xml (v2.5, v2.6)  /opt/tomcat/conf/server.xml (v4) |
|  |  | maps the /opt/grouper/grouperWebapp directory with    the HTTP path /grouper | /opt/tomee/conf/Catalina/localhost/grouper.xml (v2.5, v2.6)  /opt/tomcat/conf/Catalina/localhost/grouper.xml (v4) |
| 8005/8080/8443 | Tomcat | 8005 is the internal shutdown port for tomcat  8080 is the HTTP port for tomcat  8443 is the HTTPS port for tomcat | /opt/tomee/conf/server.xml (v2.5, v2.6)  /opt/tomcat/conf/server.xml (v4) |
| 9001 | Hsqldb | Embedded database port (for quick starts and demos) | (removed in 2.6.5+) |
| 22 | SSHD | If you are running in a managed environment where there is no  console access, you need to have the option to have SSHD  for troubleshooting reasons (e.g. see env vars, run top, jstack,  look at config files, etc) | You can make this work however you want, here is an example from AWS ECS  Dockerfile   ``` RUN yum install -y openssh-server RUN ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key -N '' RUN ssh-keygen -t dsa -f /etc/ssh/ssh_host_dsa_key -N '' EXPOSE 22 ```  Mount or overlay: /opt/tier-support/supervisord_sshd.conf   ``` [program:sshd] command=/usr/sbin/sshd -D stdout_logfile=/dev/stdout stdout_logfile_maxbytes=0 ```  Mount or overlay or edit: /usr/local/bin/grouperScriptHooks.sh   ``` #!/bin/sh  grouperScriptHooks_setupFilesPost() {   cp -v /opt/tier-support/supervisord.conf /opt/tier-support/supervisord.conf.origGrouper   cat /opt/tier-support/supervisord_sshd.conf >> /opt/tier-support/supervisord.conf   # assumes you have an env var PASS from your password manager   echo "root:${PASS}" \| chpasswd } export -f grouperScriptHooks_setupFilesPost   ``` |

## Grouper Container params

There are a few arguments you can pass to the container, and env vars... Note the command if specified (optional) will set env vars before the env vars. So you could call the container with "ui" but then specify that -e GROUPER_RUN_SHIB_SP='false' (e.g. if you run CAS)

| **Argument** | **Description** |
| --- | --- |
| ui | will set env vars:    GROUPER_UI='true'   GROUPER_RUN_APACHE='true' (removed in v5)   GROUPER_RUN_SHIB_SP='true' (removed in v5)   GROUPER_RUN_TOMEE='true' (v2.5, v2.6)   GROUPER_RUN_TOMCAT='true' (v4) |
| ws | will set env vars:   GROUPER_WS='true'   GROUPER_RUN_APACHE='true' (removed in v5)   GROUPER_RUN_TOMEE='true' (v2.5, v2.6)   GROUPER_RUN_TOMCAT='true' (v4+) |
| scim | will set env vars:   GROUPER_SCIM='true'   GROUPER_RUN_APACHE='true' (removed in v5)   GROUPER_RUN_TOMEE='true' (v2.5, v2.6)   GROUPER_RUN_TOMCAT='true' (v4+) |
| daemon | will set env vars:   GROUPER_DAEMON='true'   GROUPER_RUN_TOMEE='true' (v2.5, v2.6)   GROUPER_RUN_TOMCAT='true' (v4+) |
| bin/gsh <gshScriptFileName> -or-   /opt/grouper/grouperWebapp/WEB-INF/bin/gsh.sh <gshScriptFileName>   gsh | will just run gsh commands from docker command line e.g. (note, you need to put that file in the container e.g. with slashRoot   ``` docker run --detach --mount type=bind,src=/opt/grouperInstaller/logs,dst=/opt/grouper/logs --mount type=bind,src=/opt/grouperInstaller/slashRoot,dst=/opt/grouper/slashRoot --name gsh i2incommon/grouper:2 .5.XX /opt/grouper/grouperWebapp/WEB-INF/bin/gsh.sh /opt/grouper/grouperWebapp/WEB-INF/bin/createGrouperSystemPasswordUi.gsh ```  Note: this will not work with a quickstart container. Shell into the quickstart container and run gsh.sh |
| ui-ws | will set env vars (if not overridden):   GROUPER_UI='true'   GROUPER_WS='true'   GROUPER_RUN_APACHE='true' (removed in v5)   GROUPER_RUN_SHIB_SP='true' (removed in v5)   GROUPER_RUN_TOMEE='true' (v2.5, v2.6)   GROUPER_RUN_TOMCAT='true' (v4+) |
| quickstart (v2.5.27+) | will set env vars (if not overridden):   GROUPER_RUN_HSQLDB=true (removed in 2.6.5+)   GROUPER_RUN_SHIB_SP=false (removed in v5)   GROUPER_SELF_SIGNED_CERT=true   GROUPER_AUTO_DDL_UPTOVERSION='v4.*.*' (or whatever version you are running)   GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES='0.0.0.0/0'   GROUPER_START_DELAY_SECONDS=10   GROUPER_UI_GROUPER_AUTH=true   GROUPER_WS_GROUPER_AUTH=true   GROUPER_MCP=true   GROUPER_SCIM_GROUPER_AUTH=true   (v2.6.4-) If a GROUPERSYSTEM_QUICKSTART_PASS is set and GROUPER_UI_GROUPER_AUTH is true (and GROUPER_QUICKSTART is true), it will configure that in grouper.hibernate.base.properties   (v2.6.5+) If a GROUPERSYSTEM_QUICKSTART_PASS is set and GROUPER_UI_GROUPER_AUTH is true, it will configure that in grouper.hibernate.base.properties   (v2.6.4-) If a GROUPERSYSTEM_QUICKSTART_PASS is set and GROUPER_WS_GROUPER_AUTH is true (and GROUPER_QUICKSTART is true), it will configure that in grouper.hibernate.base.properties   (v2.6.5+) If a GROUPERSYSTEM_QUICKSTART_PASS is set and GROUPER_WS_GROUPER_AUTH is true, it will configure that in grouper.hibernate.base.properties |
| containerPing (v4.9.1+, v5.6.0+) | run ping every 10 minutes. This ensures a process is running so the container does not die. In v4, if you are running supervisor, you can run a container with no tomcat. But in v5, there needs to be a process running. So if you want a GSH container you can use the containerPing command. Or if you just want to start a grouper container that does nothing for testing of other things (file paths, file permissions, certs, etc). |
| -e GROUPER_OPENSHIFT=true | will set env vars (if not overridden):   GROUPER_CHOWN_DIRS=false   GROUPER_SHIB_LOG_USE_PIPE=false   GROUPER_GSH_CHECK_USER=false   GROUPER_RUN_PROCESSES_AS_USERS=false |
| <no command> | do nothing, so GSH can be used in bash in container, or pass in ENV vars to run something not with command above |
| -e GROUPER_UI=true | env var will tell grouper to allow ui calls via grouper.hibernate.base.properties (default false)   [grouper.is](http://grouper.is).ui.elConfig = ${java.lang.System.getenv().get('GROUPER_UI')} |
| -e GROUPER_WS=true | env var will tell grouper to allow ws calls via grouper.hibernate.base.properties (default false)   [grouper.is](http://grouper.is).ws.elConfig = ${java.lang.System.getenv().get('GROUPER_WS')} |
| -e GROUPER_SCIM=true | env var will tell grouper to allow ws calls via grouper.hibernate.base.properties (default false)   [grouper.is](http://grouper.is).scim.elConfig = ${java.lang.System.getenv().get('GROUPER_SCIM')} |
| -e GROUPER_DAEMON=true | env var will tell grouper to kick of daemon thread in tomcat (default false)   [grouper.is](http://grouper.is).daemon.elConfig = ${java.lang.System.getenv().get('GROUPER_DAEMON')} |
| -e GROUPER_MCP=true (v7.0.1+) | env will enable the MCP servlets in Grouper for AI. This is needed in the UI env and WS env.    Note: further configuration is needed to actually use the MCP but this is the first step |
| -e GROUPER_QUICKSTART=true   (v2.5.28+) | env var will setup quickstart components (default false) |
| -e GROUPER_RUN_APACHE=true (removed in v5) | env var will tell supervisor to kick off apache in container. Note, apache is not needed (default false)  for Grouper. You could hook up an external web server to tomcat or run from tomcat itself (not recommended) |
| -e GROUPER_RUN_SHIB_SP=true   (RUN_SHIB_SP up to v2.5.27) (removed in v5) | env var will tell supervisor to kick off shib sp in container. Note if you dont use shib this is not needed. (default false)  Note: you can also run shib outside the grouper container (e.g. in another container or from reverse proxy)  Note: if RUN_SHIB_SP is false, it will take the shib apache directive out of grouper-www.conf |
| -e GROUPER_USE_PIPES (v2.5.46+) (removed in v5) | if logs should go to pipe. Defaults to true if openshift if false |
| -e GROUPER_SHIB_LOG_USE_PIPE (v2.5.30+) (removed in v5) | env var to not setup a pipe for shib. defaults to true.    Set to false if should just log stdout and stderr of shib to /tmp/logshibd   Might want to mount /tmp/logshibd to the external host, or other shib log files |
| -e GROUPER_RUN_TOMEE=true (v2.5, v2.6)   -e GROUPER_RUN_TOMCAT=true (v4+)   (RUN_TOMEE up to v2.5.27) | env var will tell supervisor to kick off tomcat. Note you must have this to true if you are doing anything (default false)  but a GSH env. The WS/UI/scim/daemon must run tomcat in container. |
| -e GROUPER_RUN_HSQLDB=true (v2.5.27+)   (RUN_HSQLDB up to v2.5.27)   (removed in 2.6.5+) | env var will tell supervisor to start hsqldb, storing data files to /opt/hsqldb and listening in container on port 9001 (default false) |
| -e GROUPER_USE_SSL=false (v2.5.28+) | if you do not want apache listening on 443 ssl. If apache is running, default is true  in v5+ this applies to tomcat and not apache |
| -e GROUPER_WEBCLIENT_IS_SSL=false (v2.5.44+) | (default is "true"). if the client to Grouper is not SSL (whether SSL is done in the Grouper container or outside), then set this to false (for browser or WS caller) so Grouper does not setup URLs back to itself with SSL |
| -e GROUPER_SELF_SIGNED_CERT=true   (SELF_SIGNED_CERT up to v2.5.27) | if you dont set GROUPER_SELF_SIGNED_CERT, and you have GROUPER_USE_SSL=true (default), and you dont set GROUPER_SSL_CERT_FILE, and this file doesnt exist: /etc/pki/tls/certs/host-cert.pem, then this will be true.  Means a self signed cert will be configured with apache so it will function though not ideal. Common to use this behind a load balancer which terminates SSL  In v5 this applies to tomcat and not apache |
| -e GROUPER_SSL_USE_STAPLING=false (v2.5.41+) (removed in v5) | set to false to turn stapling off in ssl-enabled.conf (if you don't overlay that file) |
| -e GROUPER_SSL_CERT_FILE=a/b/c (v2.5.41+) | Location of cert file. For self signed built in cert [see this page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555077/Grouper+customize+SSL+certificate) |
| -e GROUPER_SSL_KEY_FILE=a/b/d (v2.5.41+) | Location of key file. For self signed built in cert [see this page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555077/Grouper+customize+SSL+certificate) |
| -e GROUPER_SSL_USE_CHAIN_FILE=true\|false (v2.5.41+) | true if should include an SSL chain file in the ssl-enabled.conf, false if should remove it |
| -e GROUPER_SSL_CHAIN_FILE=/a/b/e (v2.5.41+) | Location of chain file if GROUPER_SSL_USE_CHAIN_FILE=true. If GROUPER_SELF_SIGNED_CERT!=true, this defaults to /etc/pki/tls/certs/cachain.pem (if that file exists), if GROUPER_SELF_SIGNED_CERT=true, this is not used since GROUPER_SSL_USE_CHAIN_FILE=true |
| -e GROUPER_APACHE_SERVER_NAME=[https://a.b.c:443](https://a.b.c:443) (v2.5.28+) (removed in v5) | will set server name in grouper-www.conf and will set UseCanonicalName On. Note that if you are doing SSL offloading to your container which has Shibboleth, you will want to set this to https://your-hostname:443, and modify shibboleth2.xml to set handlerSSL="false" in <Sessions>. |
| -e GROUPER_APACHE_NONSSL_PORT=80 (v2.5.28+) (removed in v5) | will change the non-ssl port of apache. default is 80. |
| -e GROUPER_APACHE_SSL_PORT=443 (v2.5.28+) (removed in v5) | will change the ssl port of apache. default is 443. |
| -e GROUPER_APACHE_AJP_TIMEOUT_SECONDS (v2.5.28+) (removed in v5) | defaults to 3600 (one hour), customize here |
| -e GROUPER_APACHE_DIRECTORY_INDEXES (v2.5.30+) (removed in v5) | defaults to false. set to true to have apache directory indexes on |
| -e GROUPER_APACHE_REMOTE_IP_HEADER (v2.5.41+) (removed in v5) | e.g. X-Forwarded-For Replaces source IP address from reverse proxy. See apache directive RemoteIPHeader (will put in grouper-www.conf) |
| -e GROUPER_APACHE_REMOTE_IP_TRUSTED_PROXY (v2.5.41+) (removed in v5) | e.g. 1.2.3.4/28. Allows proxy from certain networks. See apache directive RemoteIPTrustedProxy (will put in grouper-www.conf) |
| -e GROUPER_APACHE_REMOTE_IP_INTERNAL_PROXY (v2.6.8+) (removed in v5) | e.g. 10.1.2.3/24 Allows proxy from private networks. See apache directive RemoteIPInternalProxy (will put in grouper-www.conf) |
| -e GROUPER_APACHE_STATUS_PATH (v2.5.51+) (removed in v5) (removed in v5) | The status path is good for monitoring (e.g. from nagios). You can restrict the source IP address of status in the grouper.properties: ws.diagnostic.sourceIpAddresses  If nothing specified, there is a status path: /status_grouper/status  If you don't want this, set: -e GROUPER_APACHE_STATUS_PATH=none  If you want it something different, do this: -e GROUPER_APACHE_STATUS_PATH=/status2_grouper/status |
| -e GROUPER_TOMCAT_HTTP_PORT (v2.5.28+) | defaults to -1. Set to -1 to not run the http/8080 connector (v4.10.2+, v5.7.0+). |
| -e GROUPER_TOMCAT_HTTPS_PORT (v4.10.2+) (v5.7.0+) | defaults to 8443. Set to -1 to not run the https/8443 connector (v4.10.2+, v5.7.0+). |
| -e GROUPER_TOMCAT_AJP_PORT (v2.5.28+) | defaults to 8009 in v4.10.2+. defaults to -1 in v5.7.0+. Set to -1 to not run the 8009 apache connector (v4.10.2+, v5.7.0+). |
| -e GROUPER_TOMCAT_SHUTDOWN_PORT (v2.5.28+) | defaults to 8005 |
| -e GROUPER_TOMCAT_LOG_ACCESS=true (v2.5.29+) | defaults to false. if you want tomcat to log access. Apache does this too. |
| -e GROUPER_TOMCAT_LOG_ACCESS_DIRECTORY=/opt/grouper/logs | defaults to /tmp in v4. defaults to /opt/grouper/logs in v5.7.0+  in v7.1.0+ and v6.2.0+ this defaults to /opt/grouper/logs if GROUPER_LOG_TO_HOST=true, and blank if false (log to STDOUT) |
| -e GROUPER_TOMCAT_LOG_ACCESS_PATTERN (v6.2.0+, v7.2.0+) | If GROUPER_TOMCAT_LOG_ACCESS is true, this is the pattern for the Tomcat access logging. The default is:  `$GROUPER_LOG_PREFIX;catalina.out;%h %l %{grouperRemoteUser}r %t \&quot;%r\&quot; %s %b`  Because of how Grouper wraps requests, the normal %u element may not available for Tomcat logging. The %{grouperRemoteUser}r element says to use the request attribute grouperRemoteUser, which Grouper will set for a session for both built-in UI auth and for Pac4j. If you are using an external authentication container and passing the remote user via a header or environment variable, you may need to override the default format and use %u which logs REMOTE_USER. |
| -e GROUPER_TOMCAT_MAX_HEADER_COUNT=200 (v2.5.57+) | due to an error in chrome 93, tomcat throws an exception about too many http headers. The default is now 200 for tomcat, but can be customized |
| -e GROUPER_TOMCAT_SESSION_TIMEOUT_MINUTES=600 (v2.5.36+) | tomcat defaults to <session-timeout>30</session-timeout> in /opt/tomee/conf/web.xml or /opt/tomcat/conf/web.xml, Grouper defaults to 10 hours (600) for UI and 1 minute fo WS. If you use SSO without requiring reauth, then you might as well default to the SSO session length for the UI (ask your IdP operator). Set to -2 to not change the default value in file |
| -e GROUPER_TOMCAT_REMOTE_CIDR_VALVE_ALLOW='1.2.3.4/32'  (v4.3.2+ and v5.1.1+) | inserts a valve into server.xml with RemoteCIDRValve to restrict the source IP address for requests to tomcat (e.g. AJP) |
| -e GROUPER_TOMCAT_REMOTE_IP_VALVE=false (v4.10.2+ and v5.7.0+) | when tomcat is run without apache behind a web server or load balancer, set this to true and configure below. If you set controls over IP addresses allowed to configure (grouperUi.configurationEditor.sourceIpAddresses or GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES), you will want to use this so that you can use the IPs of the originating client and not the IPs for the load balancers |
| -e GROUPER_TOMCAT_REMOTE_IP_INTERNAL_PROXIES="abc" (v4.10.2+ and v5.7.0+) | Sets the internalProxies matching pattern in the Tomcat RemoteIpValve valve. Use along with GROUPER_TOMCAT_REMOTE_IP_VALVE=true.  Regular expression that matches the IP addresses of internal proxies. If they appear in the `remoteIpHeader` value, they will be trusted and will not appear in the `proxiesHeader` value  By default, 10/8, 192.168/16, 169.254/16, 127/8, 100.64/10, 172.16/12, and ::1 are allowed.  Due to how they are processed by the Grouper startup script, the pipe character needs to be escaped once, and all other escapes need to be double escaped.  Example:  GROUPER_TOMCAT_REMOTE_IP_INTERNAL_PROXIES=10\\.10\\.\\d{1,3}\\.\\d{1,3}\\|192\\.168\\.1\\.\\d{1,3}  server.xml: <Valve className="org.apache.catalina.valves.RemoteIpValve" internalProxies="10\.10\.\d{1,3}\.\d{1,3}\|192\.168\.1\.\d{1,3}" /> |
| -e GROUPER_TOMCAT_REMOTE_IP_HEADER=x-forwarded-for (v4.10.2+ and v5.7.0+) | Name of the Http Header read by this valve that holds the list of traversed IP addresses starting from the requesting client. Defaults to: x-forwarded-for |
| -e GROUPER_TOMCAT_REMOTE_IP_PROXIES_HEADER=x-forwarded-by (v4.10.2+ and v5.7.0+) | Name of the http header created by this valve to hold the list of proxies that have been processed in the incoming remoteIpHeader. Defaults to: x-forwarded-by |
| -e GROUPER_TOMCAT_REMOTE_IP_TRUSTED_PROXIES="abc" (v4.10.2+ and v5.7.0+) | Regular expression that matches the IP addresses of trusted proxies. If they appear in the `remoteIpHeader` value, they will be trusted and will appear in the `proxiesHeader` value. |
| -e GROUPER_TOMCAT_REMOTE_IP_PROTOCOL_HEADER=X-Forwarded-Proto (v4.10.2+ and v5.7.0+) | Name of the http header read by this valve that holds the flag that this request. Defaults to: X-Forwarded-Proto |
| -e GROUPER_TOMCAT_REMOTE_IP_PROTOCOL_HEADER_HTTPS_VALUE=https (v4.10.2+ and v5.7.0+) | Value of the `protocolHeader` to indicate that it is an Https request. Defaults to https |
| -e GROUPER_TOMCAT_REMOTE_IP_HTTP_SERVER_PORT=80 (v4.10.2+ and v5.7.0+) | Value returned by [ServletRequest.getServerPort()](https://tomcat.apache.org/tomcat-8.5-doc/servletapi/javax/servlet/ServletRequest.html#getServerPort()) when the `protocolHeader` indicates `http` protocol. Defaults to 80 |
| -e GROUPER_TOMCAT_REMOTE_IP_HTTPS_SERVER_PORT=443 (v4.10.2+ and v5.7.0+) | Value returned by [ServletRequest.getServerPort()](https://tomcat.apache.org/tomcat-8.5-doc/servletapi/javax/servlet/ServletRequest.html#getServerPort()) when the `protocolHeader` indicates `https` protocol. Defaults to 443 |
| -e GROUPER_TOMCAT_UID=996 (v4.9.1+ and v5.6.1+)  -e GROUPER_TOMCAT_GID=994 (v4.9.1+ and v5.6.1+)  -e GROUPER_TOMCAT_UNIX_GROUP=root (v4.9.1+ and v5.6.1+) | set the unix uid of the tomcat process (GROUPER_TOMCAT_UID default 996, GROUPER_TOMCAT_GID default 994, GROUPER_TOMCAT_UNIX_GROUP default root). Note: you can only do this if you are running the container as root, otherwise there will be an error. If you want tomcat user to be root (not recommend), set GROUPER_TOMCAT_UID to 0. If you are not running the container as root, then you cannot run supervisor/apache/shibSP in v4. If you are changing the tomcat unix group, you need GROUPER_CHOWN_DIRS=true (default).  If you are not running the container as root (and want to run the tomcat as non root), then you can do this in your derived image (and do not set any of these env vars):  Have this in your Dockerfile (if uid is 1870). Note containerDockerfileInstallPermissions.sh takes params of the user and group that the container files for tomcat should have   ``` RUN /usr/local/bin/changeUid.sh tomcat 1870 \   && /usr/local/bin/changeGid.sh tomcat 1870 \   && /opt/container_files/docker-build-bin/containerDockerfileInstallPermissions.sh tomcat root ```  In v4 this is related to: GROUPER_RUN_TOMCAT_NOT_SUPERVISOR, and GROUPER_RUN_PROCESSES_AS_USERS  If you are not running the container as root, then when you run, put this in your docker run command (or containerization config), or whatever uid you want tomcat to run as   ``` docker run -u 1870 ... ``` |
| -e GROUPER_MAX_MEMORY='3g' (ui, ws, daemon)  -e MEM_MAX (gsh) | Set heap memory of java; i.e., the java `-Xmx` flag. The default 1.5g.  Recommended values:    \| **Type** \| **Container memory** \| **Heap memory (GROUPER_MAX_MEMORY)** \| \| --- \| --- \| --- \| \| UI/WS/gsh (min) \| 4g \| 2.8g \| \| Daemon (min) \| 16g \| 13g \| \| Daemon \| 32g \| 26g \| \| Daemon \| 64g \| 52g \| \| Daemon \| 96g \| 78g \|  NOTE: make sure you use less memory in the JVM than the container has allocated, and this might not be what "free" or OS calls report. If you are having system out of memory errors or crashes, but the heap size isn't near the limit, you may be running out of OS memory instead of heap space. In this situation, reducing GROUPER_MAX_MEMORY may actually improve stability. These commands should be running on host or in container depending on the maturity level running   ``` 1. (in container) ps -ef \| grep tom  2. (in container) sudo -u tomcat /usr/lib/jvm/java-1.8.0-amazon-corretto/bin/jmap -heap <pid>      (see max heap, should be approx what you expect)  3. (in container and on host if applicable) free -m  4. (in container) set \| grep MEM  5. (in container) top ```  Note: MEM_MAX is still used for GSH   ``` [tomcat@3af22d6c689f4e879e25071bacf99a3c-2291675529 root]$ set \| grep MEM GROUPER_MAX_MEMORY=3000m ```   ``` [root@3af22d6c689f4e879e25071bacf99a3c-2291675529 ~]# free -m               total        used        free      shared  buff/cache   available Mem:           7763        3557         555           0        3650        3949 Swap:             0           0           0 ``` | **Type** | **Container memory** | **Heap memory (GROUPER_MAX_MEMORY)** | UI/WS/gsh (min) | 4g | 2.8g | Daemon (min) | 16g | 13g | Daemon | 32g | 26g | Daemon | 64g | 52g | Daemon | 96g | 78g |
| **Type** | **Container memory** | **Heap memory (GROUPER_MAX_MEMORY)** |
| UI/WS/gsh (min) | 4g | 2.8g |
| Daemon (min) | 16g | 13g |
| Daemon | 32g | 26g |
| Daemon | 64g | 52g |
| Daemon | 96g | 78g |
| -e GROUPER_EXTRA_CATALINA_OPTS='-XX:+PrintGCDetails' | add additional JVM options. default is blank |
| -e CATALINA_OPTS='whatever' | Generally you should not set this, unless you want to override all the default tomcat Grouper customizations   The default Grouper settings as of v2.5.22 are:    -XX:+UseG1GC -XX:+UseStringDeduplication |
| -e GROUPER_AUTO_DDL_UPTOVERSION='v2.5.*'   (v2.5.27+) | If you want Grouper to automatically install and update the database DDL when it starts up, and dont go to another minor version, anything for v2.5.*   You can instead configure this in the grouper.hibernate.properties config file with key: registry.auto.ddl.upToVersion. Default blank |
| -e  GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES=0.0.0.0/0   (v2.5.27+) | Allow the configuration editor in the UI only from this IP address. Put in a cidr, or comma separated cidrs. Or open up and trust your authn/MFA and set to 0.0.0.0/0   You can instead configure this in the grouper-ui.properties config file with key: grouperUi.configurationEditor.sourceIpAddresses   Default is 127.0.0.1/32 |
| -e GROUPER_START_DELAY_SECONDS=10   (v.2.5.27+) | If you want Grouper to delay on startup, e.g. if waiting for the database to start. Default is 0   You can instead configure this in the grouper.hibernate.properties config file with key: grouper.start.delay.seconds |
| -e GROUPER_UI_GROUPER_AUTH=true   (v.2.5.27+) | If you want to use built-in Grouper authentication for the UI before you integrate Grouper with your SSO (default false)   You can instead configure this in the grouper.hibernate.properties config file with key: grouper.is.ui.basicAuthn |
| -e GROUPER_WS_GROUPER_AUTH=true   (v.2.5.27+) | If you want to use built-in Grouper authentication for the WS (default false)   You can instead configure this in the grouper.hibernate.properties config file with key: grouper.is.ws.basicAuthn |
| -e GROUPER_WS_TOMCAT_AUTHN=true  (v.2.5.27+) | Will setup the /opt/grouper/grouperWebapp/WEB-INF/web.xml and /opt/tomcat/conf/server.xml to use tomcat authentication for web services. Note you should consider using Grouper LDAP or built in authentication instead. (default false) |
| -e GROUPER_SCIM_GROUPER_AUTH=true   (v.2.5.27+) | If you want to use built-in Grouper authentication for SCIM (default false)   You can instead configure this in the grouper.hibernate.properties config file with key: grouper.is.scim.basicAuthn |
| -e GROUPER_MORPHSTRING_ENCRYPT_KEY_FILE=/a/b/c   (v.2.5.28+) | Location of morphString encryption key. Note the file just has the value inside (not name=value)   You can instead configure this in the morphString.properties config file with key: encrypt.key |
| -e GROUPER_MORPHSTRING_ENCRYPT_KEY=myUnsecureKey   (v.2.5.27+) | morphString encryption key. Note, passwords in environment variables or Docker commands are security risks   You can instead configure this in the morphString.properties config file with key: encrypt.key |
| -e GROUPER_DATABASE_URL_FILE=/a/b/c   (v.2.5.28+) | Location of the database jdbc url. Note the file just has the value inside (not name=value)   ``` # e.g. postgres (a):        jdbc:postgresql://localhost:5432/database # e.g. postgres (b):        jdbc:postgresql://localhost:5432/database?currentSchema=mySchema # e.g. mysql:           jdbc:mysql://localhost:3306/grouper?useSSL=false # e.g. oracle:          jdbc:oracle:thin:@server.school.edu:1521:sid # e.g. p6spy (log sql): [use the URL that your DB requires] ```  You can instead configure this in the grouper.hibernate.properties config file with key: hibernate.connection.url |
| -e GROUPER_DATABASE_URL='jdbc:[postgresql://localhost:5432/database](#)'   (v.2.5.27+) | Database URL (if not provided from file)   You can instead configure this in the grouper.hibernate.properties config file with key: hibernate.connection.url   (default jdbc:hsqldb:[hsql://localhost:9001/grouper](#) before v2.6.5, blank in v2.6.5+) |
| -e GROUPER_DATABASE_USERNAME_FILE=/a/b/c   (v.2.5.28+) | Database username from file. Note the file just has the value inside (not name=value)   You can instead configure this in the grouper.hibernate.properties config file with key: hibernate.connection.username |
| -e GROUPER_DATABASE_USERNAME=grouperSchema   (v.2.5.27+) | Database username (if not provided by file)   You can instead configure this in the grouper.hibernate.properties config file with key: hibernate.connection.username   (default is: sa before v2.6.5, blank in v2.6.5+) |
| -e GROUPER_DATABASE_PASSWORD_FILE=/a/b/c   (v.2.5.28+) | Database password, should be [encrypted](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549242/Externalize+and+encrypt+grouper+passwords+morphString+morph) in file. Note the file just has the value or encrypted value (recommended) inside (not name=value)   You can instead configure this in the grouper.hibernate.properties config file with key: hibernate.connection.password |
| -e GROUPER_DATABASE_PASSWORD=myUnsecurePass   (v.2.5.27+) | Database password (if not provided by file). Note, passwords in environment variables or Docker commands are security risks   You can instead configure this in the grouper.hibernate.properties config file with key: hibernate.connection.password |
| -e GROUPER_DATABASE_CONNECTION_POOL_SIZE=500   (v4.7+) | If your database can support 500 connections for each node in your env, then you do not need to do anything.   Otherwise, increase the number of connections your database can support, or change the pool size in grouper.hibernate.properties   (used to be 100). Note that file is in the container and cannot be changed dynamically. Divide the max connections to the database by the number of nodes connecting (e.g. 3 uis, 2 ws, 2 daemons) equals 7. Or if you want more in the daemon (recommended) adjust accordingly. You can also set this in the grouper.hibernate.properties (if you set it here you cannot set the env since it overrides): hibernate.c3p0.max_size = 500 |
| -e GROUPERSYSTEM_QUICKSTART_PASS=myUnsecurePass   (v.2.5.27+) | If you are running the quickstart command on the container, and you set this env var, and you are doing grouper built in authentication in the UI and/or WS, then this password will log in GrouperSystem in the UI and/or WS. Note, this is a HUGE security problem if this is available in a production system. GrouperSystem should not be able to log in to Grouper via WS or UI. |
| -e GROUPER_CHOWN_DIRS=false   (v.2.5.27+) | If you do not want the container to chown dirs that it needs owned by certain users. If you are making a subimage, if you can (note change to tomee for pre v4)   ``` RUN chown tomcat:root  /opt/grouper/ /etc/httpd/conf/ /home/tomcat/ /opt/tomcat/ /usr/local/bin /etc/httpd/conf.d/ /opt/tier-support/ /usr/lib/jvm/java/jre/lib/security/cacerts \     && chown -R tomcat:root $(find /opt/grouper/ /etc/httpd/conf/ /home/tomcat/ /opt/tomcat/ /usr/local/bin /etc/httpd/conf.d/ /opt/tier-support/ ! -user tomcat -o ! -group root -print) \     && chmod g+rwx /opt/grouper/ /etc/httpd/conf/ /home/tomcat/ /opt/tomcat/ /usr/local/bin /etc/httpd/conf.d/ /opt/tier-support/ /usr/lib/jvm/java/jre/lib/security/cacerts \     && chmod -R g+rwx $(find /opt/grouper/ /etc/httpd/conf/ /home/tomcat/ /opt/tomcat/ /usr/local/bin /etc/httpd/conf.d/ /opt/tier-support/ ! -perm -g+rwx )  \     && chmod +x /opt/grouper/*.sh   ```  Then the startup of the image will be quicker (whether or not you pass in this variable). Docker subimages can COPY as root which negatively affects Grouper. (default is true) |
| -e GROUPER_LOG_TO_HOST=true   (v2.5.27+) | If you do not want Grouper to log to container pipes (for Maturity level 0) and you want Grouper to log to files and mount the /opt/grouper/logs dir to a host directory (default: false) |
| -e GROUPER_LOG_TO_PIPE=true   (v2.6.16+) (v5 is removed) | If you do not want log4j2.xml to log to /tmp/loggrouper pipe, then set to false.  There are no LOG pipes in v5, Grouper by defaults logs to system out which goes to the container output. |
| -e GROUPER_LOG_TO_STDERR=true   (v4.11.3+, v5.8.6+) | If you want Grouper logging to stderr for its log files and tomcats log files (defaults to true if GROUPER_LOG_TO_HOST=false in v5.8.6+ and false in v4.11.3+) |
| -e GROUPER_USE_GROUPER_CONTEXT=true   (v2.5.27+) | Just use /grouper as the tomcat context no matter what (see "tomcat contexts" below) (default: false) |
| -e GROUPER_TOMCAT_CONTEXT=myGrouper (v2.5.28+) | Will rename the grouper.xml to be myGrouper.xml and use /myGrouper as context as far as tomcat is concerned. If running a single component (UI/WS/SCIM), then this should match the *_URL_CONTEXT (default: grouper unless only running WS or SCIM) |
| -e GROUPER_URL_CONTEXT=myGrouper (v2.5.28+) | The first part of URL for the grouper UI, defaults to "grouper" |
| -e GROUPERWS_URL_CONTEXT=myGrouper (v2.5.28+) | The first part of URL for the grouper WS, defaults to "grouper-ws". Used in swagger substitution too. |
| -e GROUPERSCIM_URL_CONTEXT=myGrouper (v2.5.28+) | The first part of URL for the grouper SCIM, defaults to "grouper-ws-scim" |
| -e GROUPER_REDIRECT_FROM_SLASH_TO_GROUPER=false (v2.5.41+) | By default the container apache will redirect from / to /grouper/ . This can be removed by setting this to false  If tomcat-only (e.g. v5) then tomcat has a rewrite rule to do this. |
| -e GROUPERUI_LOGOUT_REDIRECTTOURL=/some/path   (v2.5.28+) | Set to grouper-ui.properties: grouperUi.logout.redirectToUrl   This will be set to /Shibboleth.sso/Logout if the shib env var is set |
| -e GROUPER_LOG_PREFIX=grouper (v2.5.28+) | Log prefix. By default it is "grouper-ui' for ui-only container. grouper-ws for ws-only. grouper-scim for scim-only. grouper-daemon for daemon-only. Or "grouper" if not set to something else. |
| -e GROUPER_RUN_TOMCAT_NOT_SUPERVISOR=true   (v2.5.28+) | Will run the tomcat process as the only process in the container, not supervisor. Note, this is advanced, and should be run as the tomcat user.   [See this wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555631/Grouper+Container+running+as+non-root) |
| -e GROUPER_RUN_PROCESSES_AS_USERS=false (v2.5.28+) | Set to false if you do not want to run processes as their default users (e.g. httpd runs as apache, tomcat runs as tomcat, shibboleth runs as shibd (this is advanced). (default: true if running container as root or false if not) |
| -e GROUPER_GSH_CHECK_USER=false (v2.5.28+) | If you want other users to be able to run gsh.sh other than "tomcat" (default: true) |
| -e GROUPER_GSH_JVMARGS=-whatever=whatever (v2.6.9+) | JVM args for GSH |
| -e GROUPER_GSH_USER=tomcat | Which unix user GSH should run as when invoked in the container. Default is "tomcat" |
| -e GROUPER_ORIGFILE_GROUPER_XML (v2.5.36+) | Grouper will set this to true if the grouper.xml file was not replaced in subimage or slashRoot |
| -e GROUPER_ORIGFILE_HTTPD_CONF (v2.5.36+) | Grouper will set this to true if the httpd.conf file was not replaced in subimage or slashRoot |
| -e GROUPER_ORIGFILE_HTTPD_SHIB_CONF (v2.5.36+) | Grouper will set this to true if the httpd-shib.conf file was not replaced in subimage or slashRoot |
| -e GROUPER_ORIGFILE_LOG4J_PROPERTIES (v2.5.36+) | Grouper will set this to true if the log4j.properties file was not replaced in subimage or slashRoot |
| -e GROUPER_ORIGFILE_SERVER_XML (v2.5.36+) | Grouper will set this to true if the server.xml file was not replaced in subimage or slashRoot |
| -e GROUPER_ORIGFILE_SHIB_CONF (v2.5.36+) | Grouper will set this to true if the shib.conf file was not replaced in subimage or slashRoot |
| -e GROUPER_ORIGFILE_SSL_ENABLED_CONF (v2.5.36+) | Grouper will set this to true if the ssl-enabled.conf file was not replaced in subimage or slashRoot |
| -e GROUPER_ORIGFILE_WEBAPP_WEB_XML (v2.5.36+) | Grouper will set this to true if the WEB-INF/web.xml file was not replaced in subimage or slashRoot |
| -e GROUPER_UPGRADE_TASKS_FAIL_ON_STARTUP_IF_ERROR (v5.15.1+, v4.17.1+) | If false, Grouper will not fail to set up when there is an upgrade task error. Note this should only be set temporarily, you can go into Grouper and mark upgrade tasks   appropriately to skip or not. |
| -e GROUPER_CHECK_MEMBERSHIP_CACHE_IS_POPULATED (v5.20.1+) | If you want to skip the 95% check on startup, and also skip the upgrade task to check recent group updates, then set this to false.   Same as grouper.hibernate.properties: registry.checkMembershipCacheIsPopulated |
| -e USERTOKEN   -e ENV | This image outputs logs in a manner that is consistent with Docker Logging. Each log entry is prefaced with the submodule name (e.g. shibd, httpd, tomcat, grouper), the logfile name (e.g. access_log, grouper_error.log, catalina.out) and user definable environment name and a user definable token. Content found after the preface will be specific to the application ands its logging configuration.   ``` Note: If customizing a particular component's logging, it is recommended that the file be source from the image (docker container cp) or from the image's source repository. ```  To assign the "environment" string, set the environment variable `ENV` when defining the Docker service. For the "user defined token" string, use the environment variable of `USERTOKEN`.  An example might look like the following, with the env of "dev" and the usertoken of "build-2"   ``` shibd shibd.log dev build-2 2018-03-27 20:42:22 INFO Shibboleth.Listener : listener service starting grouper-api grouper_event.log dev build-2 2018-03-27 21:10:00,046: [DefaultQuartzScheduler_Worker-1] INFO  EventLog.info(156) -  - [fdbb0099fe9e46e5be4371eb11250d39,'GrouperSystem','application'] session: start (0ms) tomcat console dev build-2 Grouper starting up: version: 2.3.0, build date: null, env: <no label configured> ``` |
| -e GROUPER_PLAYWRIGHT_INSTALL_OS_LIBS (v4.18.1+, v5.18.1+) | If the container is run as root, the OS libraries can be installed, set this to true. If the container is not run as root, then the libraries must be installed in a derived image (container maturity 1+), e.g. in the Dockerfile. Note, it is preferable to install these in a subimage so it is more static than installing when the container is run   ``` RUN . /usr/local/bin/library.sh && setupFilesForComponent_playwrightInstallOsLibsHelper ``` |
| -e GROUPERWS_URL_WITH_CONTEXT_NOSLASH (v4.15.3+, 5.12.1+) | Example value: https://myws.inst.edu/grouper-ws   Substitute the WS url in the swagger file so the URLs are correct.    Note, this is only used in the WS container where the docs are.   This must have the http or https, domain, port (optional), and the context (e.g. grouper-ws) and no slash at end   Note the GROUPERWS_URL_CONTEXT is used to substitute in swagger too |
| -e GROUPER_CRSF_HEADER=OWASPCSRFTOKEN (v4.18.3+, v5.18.4+) | Before this version it defaulted to OWASP_CSRFTOKEN but was changed since nginx and some load balancers   not forward headers with underscores in them. This edits the owasp config overlay and the Grouper js file. |

## Grouper startup logs (v2.5.34+)

When the Grouper container starts up, it does a lot of work to manipulate the config files. The logs are printed the container logs. Note, this prints the bash file and function which calls the command, and prints the exit code (0 is usually success). When it gets to "Starting supervisor" then the container should start.

```
grouperContainer; INFO: (library.sh) Start loading library.sh
grouperContainer; INFO: (library.sh) End loading library.sh
grouperContainer; INFO: (libraryPrep.sh-prep_conf) Start setting up initial pipes
grouperContainer; INFO: (librarySetupPipe.sh-setupPipe) Setup pipe: /tmp/logpipe
grouperContainer; INFO: (librarySetupPipe.sh-setupPipe) Setup pipe: /tmp/logsuperd
grouperContainer; INFO: (librarySetupPipe.sh-setupPipe) Setup pipe: /tmp/loggrouper
grouperContainer; INFO: (libraryPrep.sh-prep_conf) End setting up initial pipes
grouperContainer; INFO: (entrypoint.sh) Executing quickstart
grouperContainer; INFO: (libraryPrep.sh-prep_finish) End prep
grouperContainer; INFO: (librarySetupFiles.sh-setupFiles_storeEnvVars) Start store env vars in /opt/grouper/grouperEnv.sh
grouperContainer; INFO: (librarySetupFiles.sh-setupFiles_storeEnvVars) End store env vars in /opt/grouper/grouperEnv.sh
grouperContainer; INFO: (librarySetupFilesForProcess.sh-setupFilesForProcess_supervisor) Clear out supervisor.conf
grouperContainer; INFO: (librarySetupFilesApache.sh-setupFilesApache_supervisor) Appending supervisord-httpd.conf to supervisord.conf
grouperContainer; INFO: (librarySetupFilesApache.sh-setupFilesApache_selfSignedCert) cp /opt/tier-support/ssl-enabled.conf /etc/httpd/conf.d/ , result: 0
grouperContainer; INFO: (librarySetupFilesApache.sh-setupFilesApache_ports) Replace apache ssl port in ssl-enabled.conf', result: 0
patching file /etc/httpd/conf/httpd.conf
grouperContainer; INFO: (librarySetupFilesApache.sh-setupFilesApache_indexes) Patch httpd.conf to turn off indexes 'patch /etc/httpd/conf/httpd.conf /etc/httpd/conf.d/httpd.conf.noindexes.patch' result=0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_loggingSlf4j) rm /opt/tomee/lib/slf4j-api*.jar , result: 0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_loggingSlf4j) rm /opt/tomee/lib/slf4j-jdk*.jar , result: 0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_loggingSlf4j) cp /opt/grouper/grouperWebapp/WEB-INF/lib/slf4j-api-*.jar /opt/tomee/lib , result: 0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_loggingSlf4j) cp /opt/grouper/grouperWebapp/WEB-INF/lib/slf4j-jdk*.jar /opt/tomee/lib , result: 0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_loggingSlf4j) rm /opt/grouper/grouperWebapp/WEB-INF/lib/slf4j-jdk*.jar , result: 0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_turnOnAjp) cp /opt/tomee/conf/server.xml /opt/tomee/conf/server.xml.currentOriginalInContainer , result: 0
patching file /opt/tomee/conf/server.xml
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_turnOnAjp) Patch server.xml to turn on ajp, result: 0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_supervisor) Append supervisord-tomee.conf to supervisord.conf
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_context) Replace context cookies in grouper.xml, result: 0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_context) Replace tomcat context in grouper.xml, result: 0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_context) Set contexts in grouper-www.conf and other files, results: 0 0 0 0 0 0 0 0 0
patching file /opt/tomee/conf/server.xml
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_accessLogs) Patch server.xml to not log access, result: 0
grouperContainer; INFO: (librarySetupFilesForProcess.sh-setupFilesForProcess_shib) mv /etc/httpd/conf.d/shib.conf /etc/httpd/conf.d/shib.conf.dontuse , result: 0
grouperContainer; INFO: (librarySetupFilesForProcess.sh-setupFilesForProcess_supervisorFinal) Running processes as users in supervisord.conf, result: 0
grouperContainer; INFO: (librarySetupFilesForComponent.sh-setupFilesForComponent_ws) cp -r /opt/grouper/grouperWebapp/WEB-INF/libWs/* /opt/grouper/grouperWebapp/WEB-INF/lib/ , result: 0
grouperContainer; INFO: (librarySetupFilesForComponent.sh-setupFilesForComponent_scim) cp -r /opt/grouper/grouperWebapp/WEB-INF/libScim/* /opt/grouper/grouperWebapp/WEB-INF/lib/ , result: 0
grouperContainer; INFO: (librarySetupFilesForComponent.sh-setupFilesForComponent_ui) cp -r /opt/grouper/grouperWebapp/WEB-INF/libUiAndDaemon/* /opt/grouper/grouperWebapp/WEB-INF/lib/ , result: 0
grouperContainer; INFO: (librarySetupFilesForComponent.sh-setupFilesForComponent_quickstart) edit grouper.hibernate.base.properties with UI GrouperSystem password for quick start, result: 0
grouperContainer; INFO: (librarySetupFilesForComponent.sh-setupFilesForComponent_quickstart) edit grouper.hibernate.base.properties with WS GrouperSystem password for quick start, result: 0
grouperContainer; INFO: (librarySetupFiles.sh-setupFiles_loggingPrefix) Changing log prefix to grouper in log4j.properties, result: 0
grouperContainer; INFO: (librarySetupFiles.sh-setupFiles_chownDirs) chown -R tomcat:root /opt/grouper/grouperWebapp, result: 0
grouperContainer; INFO: (librarySetupFiles.sh-setupFiles_chownDirs) chown -R tomcat:root /opt/tomee, result: 0
grouperContainer; INFO: (libraryRunCommand.sh-runCommand) Start setting up remaining pipes
grouperContainer; INFO: (librarySetupPipe.sh-setupPipe) Setup pipe: /tmp/loghttpd
grouperContainer; INFO: (librarySetupPipe.sh-setupPipe) Setup pipe: /tmp/logtomcat
grouperContainer; INFO: (libraryRunCommand.sh-runCommand) End setting up remainder pipes
grouperContainer; INFO: (libraryRunCommand.sh-runCommand) Starting supervisor
```

## Tomcat contexts (v2.5.28+)

There is some complexity in tomcat contexts for a few reasons

1. Only one webapp should be running at a time, do not put multiple context configs in /opt/tomcat/conf/Catalina/localhost
  
  
  
  1. If running multiple modules (e.g. ws/ui/daemon), those run in the same webapp
2. If there is one module running, there should be a specific context config so Grouper knows the context (e.g. for SOAP WSDL), and can customize it (e.g. turn off cookies for WS/Scim)
3. The apache grouper config should dynamically adjust based on which modules are running (in v5 there is no apache)
4. If you dont care about SOAP or cookies and just want everything in the same /grouper tomcat context, just set this env var: GROUPER_USE_GROUPER_CONTEXT=true

To customize the grouper.xml tomcat config:

1. You can overlay the /opt/tomcat/conf/Catalina/localhost/grouper.xml (note, its tomee pre v4)
2. If only WS is running, then grouper.xml will be renamed grouper-ws.xml unless overridden with GROUPERWS_URL_CONTEXT
3. If only SCIM is running, then grouper.xml will be renamed grouper-ws-scim.xml unless overridden with GROUPERSCIM_URL_CONTEXT
4. Otherwise grouper.xml will remain
5. If grouper.xml remains, it will be renamed if overridden with GROUPER_TOMCAT_CONTEXT

To customize the /etc/httpd/conf.d/grouper-[www.conf:](http://www.conf) (removed in v5)

1. This is the base file now, use this as a template for overlays (look in container for most recent version)
  
  
  ```
  Timeout __GROUPER_APACHE_AJP_TIMEOUT_SECONDS__
  ProxyTimeout __GROUPER_APACHE_AJP_TIMEOUT_SECONDS__
  ProxyBadHeader Ignore
  
  # the variable for _ _GROUPER_APACHE_AJP_TIMEOUT_SECONDS_ _ will be replaced to default for one hour on startup env var $GROUPER_APACHE_AJP_TIMEOUT_SECONDS
  # the variable for _ _THE_AJP_URL_ _ (no spaces) will be replaced with something like: ajp://localhost:port/grouper   on startup
  # the variable for _ _GROUPER_PROXY_PASS_ _ (no spaces) will be replaced with comment or blank on startup if running grouper url
  # the variable for _ _GROUPERWS_PROXY_PASS_ _ (no spaces) will be replaced with comment or blank on startup if running grouper-ws url
  # the variable for _ _GROUPERSCIM_PROXY_PASS_ _ (no spaces) will be replaced with comment of blank on startup if running grouper-ws-scim url
  # the variable for _ _GROUPER_TOMCAT_CONTEXT_ _ (no spaces) will be replaced with the env var $GROUPER_TOMCAT_CONTEXT
  # the variable for _ _GROUPER_URL_CONTEXT_ _ (no spaces) will be replaced with the env var $GROUPER_URL_CONTEXT
  # the variable for _ _GROUPERWS_URL_CONTEXT_ _ (no spaces) will be replaced with the env var $GROUPERWS_URL_CONTEXT
  # the variable for _ _GROUPERSCIM_URL_CONTEXT_ _ (no spaces) will be replaced with the env var $GROUPERSCIM_URL_CONTEXT
  __GROUPER_PROXY_PASS__ProxyPass /__GROUPER_URL_CONTEXT__ ajp://localhost:8009/__GROUPER_TOMCAT_CONTEXT__ timeout=__GROUPER_APACHE_AJP_TIMEOUT_SECONDS__
  __GROUPERWS_PROXY_PASS__ProxyPass /__GROUPERWS_URL_CONTEXT__ ajp://localhost:8009/__GROUPER_TOMCAT_CONTEXT__ timeout=__GROUPER_APACHE_AJP_TIMEOUT_SECONDS__
  __GROUPERSCIM_PROXY_PASS__ProxyPass /__GROUPERSCIM_URL_CONTEXT__ ajp://localhost:8009/__GROUPER_TOMCAT_CONTEXT__ timeout=__GROUPER_APACHE_AJP_TIMEOUT_SECONDS__
  
  __GROUPER_PROXY_PASS__RewriteEngine on
  __GROUPER_PROXY_PASS__RewriteRule "^/$" "/__GROUPER_URL_CONTEXT__/" [R]
  
  
  
  
  ```
2. Variables available (removed in v5)
  
  
  
  | Grouper apache config variable | Value | Condition |
  | --- | --- | --- |
  | __THE_AJP_URL__ | ajp://localhost:8009/grouper | If the UI is running, or multiple modules (e.g. SCIM and WS) |
  | __THE_AJP_URL__ | ajp://localhost:8009/grouper-ws | If the WS is running and not UI or SCIM |
  | __THE_AJP_URL__ | ajp://localhost:8009/grouper-ws-scim | If SCIM is running and not WS or SCIM |
  | __GROUPER_PROXY_PASS__ | <blank> | If the UI is running |
  | __GROUPER_PROXY_PASS__ | # (comment) | If the UI is not running |
  | __GROUPERWS_PROXY_PASS__ | <blank> | If the WS is running |
  | __GROUPERWS_PROXY_PASS__ | # (comment) | If the WS is not running |
  | __GROUPERSCIM_PROXY_PASS__ | <blank> | If SCIM is running |
  | __GROUPERSCIM_PROXY_PASS__ | # (comment) | If SCIM is not running |
3. Use those variables in your overlay. You should be able to have one overlay for all your environments, though maybe you want separate subimages for each env

## Dockerfile

If you create dirs or copy things to the webapp, you should set the owner at the end of your Dockerfile (or whatever user/group you run tomcat as)

```
RUN /opt/container_files/docker-build-bin/containerDockerfileInstallPermissions.sh tomcat root
```

See if a file is in the image

```
docker run --rm -it someImage:5.23.3 bash -c 'ls -al /home/tomcat/.bashrc'
```

## Container startup workflow (v2.5.28+)

Custom overridable hooks in green

There are three main phases:

1. Setup logging
2. Setup environment variables. Note: generally these are only set if not already set so they can be customized
3. Setup files
4. Run Processes

| **Step** | **File** | **Description** |
| --- | --- | --- |
| **Setup logging** |
| Start | /usr/local/bin/entrypoint.sh | Kick off the container |
| prepConf() | /usr/local/bin/libraryPrep.sh | Setup logging, needs to be first |
| **Setup environment variables** |
| grouperPrepConfPost() | /usr/local/bin/grouperScriptHooks.sh | Custom hook called at the beginning of the container startup after logging is setup |
| Built-in command | /usr/local/bin/daemon   /usr/local/bin/ui   /usr/local/bin/gsh   /usr/local/bin/ws   etc | Continue the workflow (below) by prepping the component, finish prep, setup files, and start supervisor |
| prep command:   prepUI()   prepWS()   etc | /usr/local/bin/setupPrep.sh | Setup each component, setting env variables |
| prep "only" command:   prepUIonly()   prepDaemonOnly() | /usr/local/bin/setupPrepOnly.sh | Set variables when certain components are the only thing runnings, can optimize   e.g. if in UI only, or WS only   E.g. set logging prefix and webapp context |
| grouperPrepComponentPost() | /usr/local/bin/grouperScriptHooks.sh | Custom hook called after the component has been prepped |
| **Setup files** |
| setupFiles() | /usr/local/bin/librarySetupFiles.sh | Main logic that moves around files, generates files, massages files, substitutes files   Most logic is in functions that could be overridden carefully (check for changes on upgrade)   Note, this will not execute twice since GROUPER_SETUP_FILES_COMPLETE=true at end |
| grouperSetupFilesPost() | /usr/local/bin/grouperScriptHooks.sh | Custom hook called after the setupFiles functions is called, right before the process starts |
| **Run processes** |

## Versions

- Tag in github docker is: 2.5.X where X is an integer that increases for each build
- There is a listing of each version in the [Grouper 2.5 release notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793539/v2.5+Release+Notes), with an indication on if it is stable or not
- Every container will have "yum update" done before the container is released. As needed tomcat will be updated

## Jstack

If you want to know what is hung or what is currently using CPU, you need to run jstack. You need to be able to shell in to your container, and the OS needs to allow jstack (lwp).

Know what node to be on, if it is a daemon node, and you have multiple, look in the daemon logs on the UI, see the running job, and see which node to go to.

Shell into your terminal and be the same user as tomcat/tomee.

PS and get the process ID of tomee/tomcat

```
ps -ef | grep catalina
```

Run jstack:

```
jstack 71
```

## Misc

- If you use Kubernetes, use an ARG for the "ui" or "ws" and not a COMMAND
- All of your containers must have the correct time and consistent timezones
- [HTTP Strict Transport Security (HSTS)](https://en.wikipedia.org/wiki/HTTP_Strict_Transport_Security) is enabled on the Apache HTTP Server. (apache removed in v5)
- morphStrings functionality in Grouper is supported. It is recommended that the various morphString files be associated with the containers as Docker Secrets. Set the configuration file properties to use `/run/secrets/secretname`.
- /run/secrets
  
  
  
  - If you have a secret available in /run/secrets/grouper_fileName.pass, it will be linked from /opt/grouper/grouperWebapp/WEB-INF/classes/fileName.pass (change fileName.pass name as needed)
  - If you have a secret available in /run/secrets/shib_fileName.pass, it will be linked from /etc/shibboleth/fileName.pass (change fileName.pass name as needed)
  - If you have a secret available in /run/secrets/httpd_fileName.pass, it will be linked from /etc/httpd/conf.d/fileName.pass (change fileName.pass name as needed)
  - If you have a secret named /run/secrets/host-key.pem, it will be linked from /etc/pki/tls/private/host-key.pem
- [Configure Grouper UI and WS authentication](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545269/Authentication)
- Take a heap snapshot
  
  
  ```
  [root@43c6b6e000b3 ~]# sudo -u tomcat  /usr/lib/jvm/java-1.8.0-amazon-corretto/bin/jmap -dump:file=/tmp/memory.dump <pid>
  ```
- See the env vars of a process, get the pid fist with ps-ef, then, run as the user (in this case tomcat) and substitute the pid in
  
  
  ```
  [root@73e1e62a5fa1 bin]# sudo -u tomcat cat /proc/45/environ | tr '\0' '\n'
  ```

## Jars

- If you want a jar in all JVMS (ui/ws/daemon/gsh/scim), add it to /opt/grouper/grouperWebapp/WEB-INF/lib
- If you want a jar in ui/daemon/gsh only, add it to /opt/grouper/grouperWebapp/WEB-INF/libUiAndDaemon
- If you are replacing a jar (e.g. an existing driver), you need to remove it first from your dockerfile by wildcard, or overlay a blank file (risky since filenames can change)
  
  
  ```
  RUN rm -rf /opt/grouper/grouperWebapp/WEB-INF/lib/mysql-connector-java*.jar
  ```
- dfs
