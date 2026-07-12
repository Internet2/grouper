---
title: "Grouper web services - authentication - built-in Grouper"
space: Grouper
pageId: 28555139
version: 9
lastUpdated: 2026-07-01T05:38:51.979Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555139/Grouper+web+services+-+authentication+-+built-in+Grouper
---

Out of the box, grouper-ws uses [Grouper built-in basic authentication](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549360/Grouper+Built-in+Basic+Authentication+to+UI+and+Web+Services) with usernames and passwords hashed and stored in the grouper database (after enabling it).

This authentication is built-in to Grouper and does not use tomcat or apache authentication

You need to configure Grouper to use the build-in basic authentication and you need to create/manage the username(s) and password(s) for the UI or for WS in the data base.

After you have completed the steps in the Configuresection and the Manage users section on this page then the basic auth features should work for you.

NOTE: You can "Manage users" before and after you enable the built-in authentication feature. The account data lives in the DB so you need to have a stable DB data set too.

## Manage users

Enter your own values for:

- ***PRINCIPAL***
- ***PASSWORD***

Until there is a UI you can remove accounts in the database in the grouper_password table (or we can add more GSH methods)

```
cd /opt/grouper/grouperWebapp/WEB-INF/bin
./gsh.sh    (as tomcat... e.g. sudo -u tomcat ./gsh.sh)

v2.5.29+
new GrouperPasswordSave().assignApplication(GrouperPassword.Application.UI).assignUsername("GrouperSystem").assignPassword("password").save();// and for WebService accounts you can do this:
new GrouperPasswordSave().assignApplication(GrouperPassword.Application.WS).assignUsername("GrouperSystem").assignPassword("WSpassword_can_be_different_for_the_same_username").save();
```

## Configure

This is on by default if you start a Grouper container v2.5 with "ws". But here are some details. Note the file locations in the container are listed in the [v2.5 container documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation)

| File | Value | Description |
| --- | --- | --- |
| grouper.hibernate.properties | [grouper.is.ws](http://grouper.is.ws).basicAuthn=true | This enables the built-in Grouper authentication   with passwords in the database |
| web.xml | No security-constraints or login-configs | This is the default provided with container, do not overlay |
| server.xml | ajp 8009 connector element: tomcatAuthentication="false" | This is the default provided with container, do not overlay  Tomcat is not doing authn so that attribute needs to be false |
| grouper-ws.properties | ws.security.non-rampart.authentication.class = | This should be blank (get remote_user)  This is the default provided with container, do not overlay |
| grouper-www.conf | no AuthType directives | This is the default provided with container, do not overlay |
