---
title: "Grouper web services - authentication - Apache http password"
space: Grouper
pageId: 28555359
version: 3
lastUpdated: 2026-07-01T05:38:26.375Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555359/Grouper+web+services+-+authentication+-+Apache+http+password
---

If you want to use apache authentication, you can do that. Generally you should not use a password file (use [grouper built-in authn](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555139/Grouper+web+services+-+authentication+-+built-in+Grouper) instead), if you use LDAP or something else it could be useful

## Manage users

If you are using apache ldap authn, manage users in your ldap. This is an example using a password file which is not convenient in container, but as an example

```
[appadmin@i2midev1 bin]$ sudo htpasswd /etc/httpd/conf.d/users.pass username
```

## Configure

Note the file locations in the container are listed in the [v2.5 container documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation)

| File | Value | Description |
| --- | --- | --- |
| grouper.hibernate.properties | [grouper.is.ws](http://grouper.is.ws).basicAuthn=false | This is the default provided with container, do not overlay |
| web.xml | No security-constraints or login-configs | This is the default provided with container, do not overlay |
| server.xml | ajp 8009 connector element: tomcatAuthentication="false" | This is the default provided with container, do not overlay  Tomcat is not doing authn so that attribute needs to be false |
| grouper-ws.properties | ws.security.non-rampart.authentication.class = | This should be blank (get remote_user)  This is the default provided with container, do not overlay |
| grouper-www.conf | Customize this directive with the apache authn config   ``` <LocationMatch ^/grouper-ws/.*>   AuthType Basic   AuthName "By Invitation Only"   AuthUserFile /etc/httpd/conf.d/users.pass    Require valid-user  </LocationMatch>   ``` | Configure apache authn here |
