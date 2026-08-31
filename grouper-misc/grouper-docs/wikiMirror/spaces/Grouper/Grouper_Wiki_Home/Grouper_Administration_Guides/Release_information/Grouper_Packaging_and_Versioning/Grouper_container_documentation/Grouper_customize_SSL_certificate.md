---
title: "Grouper customize SSL certificate"
space: Grouper
pageId: 28555077
version: 11
lastUpdated: 2026-07-01T05:39:01.879Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555077/Grouper+customize+SSL+certificate
---

## v5+

There is no apache in the container by default, so you need to run tomcat with SSL.

If you are running tomcat behind a web server or load balancer, the SSL will terminate there. You can run Grouper with self signed SSL so the traffic from the load balancer is encrpyted (allow invalid cert from load balancer), or you can run without SSL for that back channel.

Or you can run SSL with a valid cert for your domain name.

This is configured in the /opt/tomcat/conf/server.xml, by setting some environment variables

If you dont want to run with SSL, set GROUPER_TOMCAT_HTTP_PORT=8080 and GROUPER_TOMCAT_HTTPS_PORT=-1

If you want SSL with self signed cert set GROUPER_SELF_SIGNED_CERT=true, then tomcat on 8443 will run in SSL with a self-signed cert

If you want to load a cert for your domain name, you should put your mycert.pem in /opt/grouper/certs/client/ and myprivatekey.pem in /opt/grouper/certs/keys/.   
Note, if you put them in a different dir, thats fine, just make sure the permissions on the private key are 660. The /opt/grouper/certs/keys/ folder will have the right permissions  
Configure the files with GROUPER_SSL_CERT_FILE=/opt/grouper/certs/client/mycert.pem, GROUPER_SSL_KEY_FILE=/opt/grouper/certs/keys/myprivatekey.pem  
If you need a chain file, set GROUPER_SSL_CHAIN_FILE=/opt/grouper/certs/anchors/mychain.pem

Note that the file ownership and permissions needs to be for the tomcat user unlike when Apache was in the container which ran as root

If you want to change the SSL port (default 8443) set: GROUPER_TOMCAT_HTTPS_PORT=443

## v4.10.3+ tomcat only (not apache)

If you want to run tomcat without an apache you can turn on SSL with tomcat

If you dont want to run with SSL, set GROUPER_TOMCAT_HTTP_PORT=8080 and GROUPER_TOMCAT_HTTPS_PORT=-1

If you want SSL with self signed cert set (note, this is different than the apache self signed paths since file ownership and permissions are different)

```
GROUPER_TOMCAT_HTTP_PORT=-1
GROUPER_TOMCAT_AJP_PORT=-1
GROUPER_TOMCAT_HTTPS_PORT=8443       (or to be like apache use 443 if tomcat runs as root which is not recommended)
GROUPER_RUN_APACHE=false

in 4.11.0+:
GROUPER_SSL_CERT_FILE=/opt/container_files/certs/client/localhost.pem
GROUPER_SSL_KEY_FILE=/opt/container_files/certs/keys/localhost.key

in 4.10.3:
GROUPER_SSL_CERT_FILE=/opt/grouper/certs/client/localhost.pem
GROUPER_SSL_KEY_FILE=/opt/grouper/certs/keys/localhost.key
```

## v4 and previous

Start the UI or WS without the self signed cert option (either false or just leave it out). e.g.

```
docker run ... -e SELF_SIGNED_CERT='false' ... i2incommon/grouper:2.5.37.1 ui
8b95e14575ec33942494da222827c69effce6aae6b1e59d01e961b35e9c2e9e5
```

This will cause this file: /etc/httpd/conf.d/ssl-enabled.conf to have these entries:

```
  SSLCertificateChainFile /etc/pki/tls/certs/cachain.pem

  SSLCertificateFile /etc/pki/tls/certs/host-cert.pem

  SSLCertificateKeyFile /etc/pki/tls/private/host-key.pem

```

You can overwrite the files with you cert and key. See the [container variables section](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation)

| **Argument** | **Description** |
| --- | --- |
| -e GROUPER_SSL_CERT_FILE=a/b/c (v2.5.41+) | Location of cert file. If GROUPER_SELF_SIGNED_CERT!=true, this defaults to /etc/pki/tls/certs/host-cert.pem, if GROUPER_SELF_SIGNED_CERT=true, this will be /etc/pki/tls/certs/localhost.crt |
| -e GROUPER_SSL_KEY_FILE=a/b/d  (v2.5.41+) | Location of key file. If GROUPER_SELF_SIGNED_CERT!=true, this defaults to /etc/pki/tls/private/host-key.pem, if GROUPER_SELF_SIGNED_CERT=true, this will be /etc/pki/tls/private/localhost.key  Note: /run/secrets/host-key.pem will be symlinked to /etc/pki/tls/private/host-key.pem |
| -e GROUPER_SSL_USE_CHAIN_FILE=true\|false  (v2.5.41+) | true if should include an SSL chain file in the ssl-enabled.conf, false if should remove it |
| -e GROUPER_SSL_CHAIN_FILE=/a/b/e  (v2.5.41+) | Location of chain file if GROUPER_SSL_USE_CHAIN_FILE=true. If GROUPER_SELF_SIGNED_CERT!=true, this defaults to /etc/pki/tls/certs/cachain.pem (if that file exists), if GROUPER_SELF_SIGNED_CERT=true, this is not used since GROUPER_SSL_USE_CHAIN_FILE=true |

## Legacy docs on removing cachain

Old way to take out cachain.pem file, you can do this:

Mount or overlay or adjust this file: /usr/local/bin/grouperScriptHooks.sh

```
#!/bin/sh
# called after the setupFiles functions is called, almost before the process starts
grouperScriptHooks_setupFilesPost() {
  echo "myContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) starting..."
  sed -i "s|SSLCertificateChainFile /etc/pki/tls/certs/cachain.pem||g" /etc/httpd/conf.d/ssl-enabled.conf
  echo "myContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) sed -i \"s|SSLCertificateChainFile /etc/pki/tls/certs/cachain.pem||g\" /etc/httpd/conf.d/ssl-enabled.conf , result=$?"
}
export -f grouperScriptHooks_setupFilesPost
echo "myContainer; INFO: (grouperScriptHooks.sh-body) export -f grouperScriptHooks_setupFilesPost, result=$?"
```
