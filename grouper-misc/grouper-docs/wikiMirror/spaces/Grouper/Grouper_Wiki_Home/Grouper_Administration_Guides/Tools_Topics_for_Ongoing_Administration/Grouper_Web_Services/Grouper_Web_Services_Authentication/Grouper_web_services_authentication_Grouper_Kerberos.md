---
title: "Grouper web services - authentication - Grouper Kerberos"
space: Grouper
pageId: 28555113
version: 4
lastUpdated: 2026-07-01T05:38:54.929Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555113/Grouper+web+services+-+authentication+-+Grouper+Kerberos
---

Grouper has built-in Kerberos authentication. Pass the user/pass in basic auth (over SSL), and grouper can check it via kerberos. The username will be the subject id or identifier

To use this, make the following settings in the grouper-ws.properties (obviously you need to configure the kerberos settings to fit your institution):

## Manage users

Manage users in kerberos with kadmin

## Configure

Note the file locations in the container are listed in the [v2.5 container documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation)

| File | Value | Description |
| --- | --- | --- |
| grouper.hibernate.properties | [grouper.is.ws](http://grouper.is.ws).basicAuthn=false | This is the default provided with container, do not overlay |
| web.xml | Should be an empty element | This is the default provided with container, do not overlay |
| server.xml | ajp 8009 connector element: tomcatAuthentication="false" | This is the default provided with container, do not overlay  Tomcat is not doing authn so that attribute needs to be false |
| grouper-ws.properties | ``` # to provide custom authentication (instead of the default httpServletRequest.getUserPrincipal() # for non-Rampart authentication.  Class must implement the interface: # edu.internet2.middleware.grouper.ws.security.WsCustomAuthentication # class must be fully qualified.  e.g. edu.school.whatever.MyAuthenticator # blank means use default: edu.internet2.middleware.grouper.ws.security.WsGrouperDefaultAuthentication ws.security.non-rampart.authentication.class = edu.internet2.middleware.grouper.ws.security.WsGrouperKerberosAuthentication  ################# KERBEROS settings, only needed if doing kerberos simple auth ################ # realm, whatever your realm is, e.g. SCHOOL.EDU kerberos.realm = SCHOOL.EDU # address of your kdc, e.g. kdc.school.edu kerberos.kdc.address = kdc.school.edu ``` | Overlay the grouper-ws.properties or configure in    the database. Note you might need an external    krb5.cnf file or other config in the container |
| grouper-www.conf | Do not have any authn directives here | This is the default provided with container, do not overlay |

DEBUG

Note, if you want to debug this, put this in the log4j.properties:

```
log4j.logger.edu.internet2.middleware.grouper.ws.security.WsGrouperKerberosAuthentication = DEBUG
```
