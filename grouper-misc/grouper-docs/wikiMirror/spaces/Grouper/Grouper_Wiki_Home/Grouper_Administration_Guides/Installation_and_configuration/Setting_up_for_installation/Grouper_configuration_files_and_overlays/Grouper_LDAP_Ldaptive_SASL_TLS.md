---
title: "Grouper LDAP Ldaptive SASL TLS"
space: Grouper
pageId: 28554792
version: 7
lastUpdated: 2026-07-01T05:39:36.862Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554792/Grouper+LDAP+Ldaptive+SASL+TLS
---

## Overview

 This page shows how to point a Grouper LDAP connection (such as an LDAP subject source or the LDAP loader) at an OpenLDAP server using a **SASL EXTERNAL** bind authenticated by a client certificate held in a keystore, over StartTLS. Instead of binding with a DN and password, Grouper presents a client certificate from a JKS or PKCS12 keystore and the directory maps that certificate to an identity.

 > Grouper's LDAP connections are configured with [ldaptive](https://www.ldaptive.org/) properties (`org.ldaptive.*`). Ldaptive replaced vt-ldap in **v2.4** (the `configFileFromClasspath` mechanism and these property names), and this approach works in the currently supported releases (confirmed in the supported releases). The SASL and keystore settings are read by ldaptive's own property handlers (`BindConnectionInitializerPropertySource` for the SASL bind, `SslConfigPropertySource` for the keystore credential), per `LdaptiveConfiguration` in the Grouper source.

 > **Required access:** this is a server-side deployment task, not a Grouper UI privilege. You need filesystem/container access to the Grouper webapp to place the keystore and the ldaptive config file, the ability to edit the `grouper-loader.properties` overlay, and the ability to restart Grouper. Protect the keystore file and its password.

 

## Configure SASL EXTERNAL bind with a keystore

 

1. Generate a JKS or PKCS12 keystore with valid keys and certificates. This example uses a `.p12` (PKCS12) keystore file.
2. Put the keystore where the Grouper webapp can read it, for example `/opt/grouperContainer/opt/grouper/grouperWebapp/WEB-INF/classes/`, or anywhere reachable by other means (in this example a separate mount was created and the keystore placed under `/opt/grouper`).
3. Add this to the `grouper-loader.properties` overlay:
  
   
  ```text
  ldap.personLdap.url = ldap://<yourldaphostname>.edu
  
  ldap.personLdap.configFileFromClasspath = ldap.personLdap.properties
  ```
  
   This assumes you are using `personLdap` as the `ldapServerId.value` in `subject.properties`.
4. Put this into `ldap.personLdap.properties` (the ldaptive config file referenced above):
  
   
  ```text
  org.ldaptive.ldapUrl=ldap://<yourldaphostname>.edu
  
  org.ldaptive.useStartTLS=true
  
  org.ldaptive.bindSaslConfig={mechanism=EXTERNAL}
  
  org.ldaptive.credentialConfig=org.ldaptive.ssl.KeyStoreCredentialConfig{{keyStore=file:/<path_to_file>/grouper.p12}{keyStoreType=pkcs12}{keyStorePassword=<secret>}}
  ```
5. The rest is the normal Grouper LDAP integration.
