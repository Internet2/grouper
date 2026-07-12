---
title: "Grouper web services - authentication - rampart"
space: Grouper
pageId: 28555374
version: 4
lastUpdated: 2026-07-01T05:38:20.898Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555374/Grouper+web+services+-+authentication+-+rampart
---

Example

Rampart is Jakarta's WS-Security implementation. We have vanilla Rampart authentication working with grouper-ws (thanks to Sanjay Vivek). Unfortunately it doesnt work out of the box since it seems Rampart and basic auth cannot work together in the web app. If you want to run basic auth and rampart at the same time, you should deploy two separate web apps.

Note the URL for rampart in grouper-ws is the same, it will look like this: /grouper-ws/services/GrouperService

Also, for Rampart, you need custom logic to authenticate users. To use rampart, configure the grouper-ws.properties entry: ws.security.rampart.authentication.class. An example is: [edu.internet2.middleware.grouper.ws](http://edu.internet2.middleware.grouper.ws).security.GrouperWssecSample. Until you configure that, clients will get a 404 http status code. This assumes you are using WSPasswordCallback, if not, just provide your own class directly to the services.xml file (and grouper-ws requires you have an implementation of the interface anyway which wont be executed).

Then you need to enable the correct .aar file.

- Rename the following two files
  
  - /WEB-INF/services/GrouperService.aar to /WEB-INF/services/GrouperService.aar.ondeck
  - /WEB-INF/services/GrouperServiceWssec.aar.ondeck to /WEB-INF/services/GrouperServiceWssec.aar

## Manage users

Manage users in the implemented system

## Configure

Note the file locations in the container are listed in the [v2.5 container documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation)

| File | Value | Description |
| --- | --- | --- |
| grouper.hibernate.properties | [grouper.is.ws](http://grouper.is.ws).basicAuthn=false | This is the default provided with container, do not overlay |
| web.xml | Should be an empty element | This is the default provided with container, do not overlay |
| server.xml | ajp 8009 connector element: tomcatAuthentication="false" | This is the default provided with container, do not overlay  Tomcat is not doing authn so that attribute needs to be false |
| grouper.properties | ``` grouperWsAxisWssec = true ``` | Overlay the grouper.properties or configure in    the database. This will set the servlet param internally (dont put this anywhere)   ``` <init-param>   <param-name>wssec</param-name>   <param-value>true</param-value> </init-param>  ``` |
| grouper-ws.properties | ``` # to provide custom authentication (instead of the default httpServletRequest.getUserPrincipal() # for non-Rampart authentication.  Class must implement the interface: # edu.internet2.middleware.grouper.ws.security.WsCustomAuthentication # class must be fully qualified.  e.g. edu.school.whatever.MyAuthenticator # blank means use default: edu.internet2.middleware.grouper.ws.security.WsGrouperDefaultAuthentication ws.security.non-rampart.authentication.class =    ``` | Overlay the grouper-ws.properties or configure in    the database. Set the rampart authentication |
| grouper-www.conf | Do not have any authn directives here | This is the default provided with container, do not overlay |
