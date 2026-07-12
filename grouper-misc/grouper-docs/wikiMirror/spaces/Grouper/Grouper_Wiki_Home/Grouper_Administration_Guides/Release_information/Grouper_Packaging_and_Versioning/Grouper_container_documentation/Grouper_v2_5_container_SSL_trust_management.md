---
title: "Grouper v2.5 container SSL trust management"
space: Grouper
pageId: 28554579
version: 16
lastUpdated: 2026-07-01T05:40:08.075Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554579/Grouper+v2.5+container+SSL+trust+management
---

## Introduction

2.6.9+ The Grouper container needs to connect to external systems like HTTPS endpoints, LDAPS, SQL (with SSL), etc. If the endpoints have SSL chains of trust through well-known roots, then things will just work. If there are self-signed certs or untrusted roots, then certificates must be added to the Grouper container.

## Trusted roots

Note, if your container does not run as root, then you need to put files in /etc/pki/ca-trust/source/anchors/ and run this command '/bin/update-ca-trust' in the Dockerfile

If your institution has a trusted root cert that is the trust chain for server certifications, you can put them in the directory. Only *.pem files (with one cert per file) may be in the directory (case sensitive)

```
/opt/grouper/certs/anchors/
```

e.g.

```
/opt/grouper/certs/anchors/myTrustAnchor.pem
```

The container will copy that to /etc/pki/ca-trust/source/anchors/ and load that into the RHEL trust store (you do not need to run this!)

```
/bin/update-ca-trust
```

The container will add this to Java's trusted certs (you do not need to add this!)

```
-e GROUPER_EXTRA_CATALINA_OPTS="-Djavax.net.ssl.trustStore=/etc/pki/java/cacerts"
```

You will see something like this in the container logs for a successful processing

```
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_sslCertsAnchors) There are anchor certs in /opt/grouper/certs/anchors/ to process
'/opt/grouper/certs/anchors/mcommunity.pem' -> '/etc/pki/ca-trust/source/anchors/mcommunity.pem'
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_sslCertsAnchors) /usr/bin/cp -v /opt/grouper/certs/anchors/* /etc/pki/ca-trust/source/anchors , result=0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_sslCertsAnchors) /bin/update-ca-trust , result=0
```

## Trusted client certs

If you are connecting to an endpoint that requires a client cert to be loaded (i.e. there is not a trusted root that can be used, i.e. self signed). Put certs in directory. Only *.pem files (with one cert per file) may be in the directory (case sensitive). Note this will not work if your container is not running as the tomcat user or root (e.g. openshift). You need to make a derived image and put the certs in the trust store in the image.

```
/opt/grouper/certs/client/
```

e.g.

```
/opt/grouper/certs/client/someCert.pem
```

This what you will see in the container logs for successful processing

```
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_sslCertsAnchors) chmod u+w /usr/lib/jvm/java/jre/lib/security/cacerts , result=0
Certificate was added to keystore
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_sslCertsAnchors) /usr/lib/jvm/java/bin/keytool -import -noprompt -keystore /usr/lib/jvm/java/jre/lib/security/cacerts -storepass changeit -alias "twitter" -file "/opt/grouper/certs/client/twitter.pem" , result=0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_sslCertsAnchors) chmod u-w /usr/lib/jvm/java/jre/lib/security/cacerts , result=0

```

## Test connections

Connect to a port with openssl

```
 [tomcat@i2midev6 client]$ openssl s_client -connect hostname:port -showcerts
```

Test an SSL connection with Java

```
[tomcat@i2midev6 certs]$ pwd
/opt/grouper/certs
[tomcat@i2midev6 certs]$ java TestSsl a.b.c.d 443
Successfully connected
[tomcat@i2midev6 certs]$ 
```

## Get certs

If you want to get a cert, here is a way (substitute the host, port, and cert name. Note, cert name must be alphanumeric and end in .pem.

```
[tomcat@i2midev6 WEB-INF]$ cd /opt/grouper/certs/client/
[tomcat@i2midev6 client]$ openssl s_client -host a.b.c -port 1234 <<< "Q" 2>&1 | sed -n "/-----BEGIN/,/END\ CERTIFICATE-----/p" > someCert.pem
```

Get a TLS cert

```
[tomcat@i2midev6 certs]$ python3 get_tls_cert.py a.b.c 443
```

## Test anchor cert

Create a cert with file extension pem

```
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -sha256 -days 3650 -nodes -subj "/C=XX/ST=StateName/L=CityName/O=CompanyName/OU=CompanySectionName/CN=CommonNameOrHostname"
```

Put the cert in /opt/grouper/certs/anchors/*.pem (in this case we are mounting but you can put it in the derived image etc)

```
emacs slashRoot/opt/grouper/certs/anchors/selfSignedCert.pem
```

Run container (this is just an example)

```
docker run --name grouper -e GROUPERSYSTEM_QUICKSTART_PASS=pass \
    --mount type=bind,src=/tmp/slashRoot,dst=/opt/grouper/slashRoot \
    -e GROUPER_MORPHSTRING_ENCRYPT_KEY=abc123 \
    -e GROUPER_DATABASE_PASSWORD=pass -e GROUPER_DATABASE_USERNAME=postgres \
    -e GROUPER_RUN_SHIB_SP=false -e GROUPER_SELF_SIGNED_CERT=true \
    -e GROUPER_AUTO_DDL_UPTOVERSION='v4.*.*' \
    -e GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES='0.0.0.0/0' \
    -e GROUPER_START_DELAY_SECONDS=10 -e GROUPER_UI_GROUPER_AUTH=true \
    -e GROUPER_DATABASE_URL=jdbc:postgresql://1.2.3.4:5433/postgres \
    -d -p 8443:443 i2incommon/grouper:4.9.1 ui
```

Container logs

```
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_sslCertsAnchors) There are anchor certs in /opt/grouper/certs/anchors/ to process
'/opt/grouper/certs/anchors/selfSignedCert.pem' -> '/etc/pki/ca-trust/source/anchors/selfSignedCert.pem'
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_sslCertsAnchors) /usr/bin/cp -v /opt/grouper/certs/anchors/* /etc/pki/ca-trust/source/anchors , result=0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_sslCertsAnchors) /bin/update-ca-trust , result=0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_sslCertsAnchors) chmod u+w /usr/lib/jvm/java-17-amazon-corretto/lib/security/cacerts , result=0
Warning: use -cacerts option to access cacerts keystore
Certificate was added to keystore
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_sslCertsAnchors) /usr/lib/jvm/java/bin/keytool -import -trustcacerts -noprompt -keystore /usr/lib/jvm/java-17-amazon-corretto/lib/security/cacerts -storepass changeit -alias "selfSignedCert" -file "/opt/grouper/certs/anchors/selfSignedCert.pem" , result=0
grouperContainer; INFO: (librarySetupFilesTomcat.sh-setupFilesTomcat_sslCertsAnchors) chmod u-w /usr/lib/jvm/java-17-amazon-corretto/lib/security/cacerts , result=0
```

Check cert

```
keytool -v -list -keystore /usr/lib/jvm/java-17-amazon-corretto/lib/security/cacerts -storepass changeit > /tmp/certs.txt

*******************************************
*******************************************

Alias name: selfsignedcert
Creation date: Nov 26, 2023
Entry type: trustedCertEntry

Owner: CN=CommonNameOrHostname, OU=CompanySectionName, O=CompanyName, L=CityName, ST=StateName, C=XX
Issuer: CN=CommonNameOrHostname, OU=CompanySectionName, O=CompanyName, L=CityName, ST=StateName, C=XX
Serial number: a0fa38a64f440ac2
Valid from: Sun Nov 26 06:12:09 UTC 2023 until: Wed Nov 23 06:12:09 UTC 2033
Certificate fingerprints:
         SHA1: 7D:53:B4:B5:9C:24:69:A5:BB:48:63:65:85:D3:8B:60:4B:10:FF:5B
         SHA256: 26:3B:D5:37:08:20:C0:63:72:7F:8A:A3:6E:92:37:57:E3:85:A8:A9:6F:36:1D:07:33:9F:F0:5A:3C:5B:0A:5C
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 4096-bit RSA key
Version: 1

*******************************************
*******************************************

```
