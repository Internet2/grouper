---
title: "Grouper web services - authentication - Tomcat authentication"
space: Grouper
pageId: 28554813
version: 4
lastUpdated: 2026-07-01T05:39:34.841Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554813/Grouper+web+services+-+authentication+-+Tomcat+authentication
---

If you want to use tomcat authentication, you can do that. Generally you should not use the tomcat-users.xml file (use [grouper built-in authn](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555139/Grouper+web+services+-+authentication+-+built-in+Grouper) instead), if you use LDAP or something else it could be useful.

Note that in v2.5+ the container uses tomee which is essentially tomcat (same authn config)

## Manage users

If you are using tomcat ldap authn, manage users in your ldap. This is an example using tomcat-users.xml file which is not convenient in container, but as an example

```
  <user username="THE_PRINCIPAL" password="THE_PASSWORD" roles="grouper,grouper_user"/>
```

## Configure

Note the file locations in the container are listed in the [v2.5 container documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation)

| File | Value | Description |
| --- | --- | --- |
| grouper.hibernate.properties | [grouper.is.ws](http://grouper.is.ws).basicAuthn=false | This is the default provided with container, do not overlay |
| web.xml | Make sure the appropriate security configs are there, this is an example in the v2.5 container   ``` <?xml version="1.0" encoding="UTF-8"?> <web-app xmlns:j2ee="http://java.sun.com/xml/ns/j2ee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"   xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"   version="2.4">    <security-constraint>     <web-resource-collection>       <web-resource-name>Web services</web-resource-name>       <url-pattern>/services/*</url-pattern>     </web-resource-collection>     <auth-constraint>       <role-name>grouper_user</role-name>     </auth-constraint>   </security-constraint>    <security-constraint>     <web-resource-collection>       <web-resource-name>Web services</web-resource-name>       <url-pattern>/servicesRest/*</url-pattern>     </web-resource-collection>     <auth-constraint>       <!-- NOTE:  This role is not present in the default users file -->       <role-name>grouper_user</role-name>     </auth-constraint>   </security-constraint>    <!-- Define the Login Configuration for this Application -->   <login-config>     <auth-method>BASIC</auth-method>     <realm-name>Grouper Application</realm-name>   </login-config>    <!-- Security roles referenced by this web application -->   <security-role>     <description>       The role that is required to log in to web service     </description>     <role-name>grouper_user</role-name>   </security-role>    </web-app> ``` | Overlay this, and do not include any servlet mappings etc |
| server.xml | ajp 8009 connector element: tomcatAuthentication="true" | Tomcat is doing authn so that attribute needs to be true |
| server.xml | add in any other configs, e.g. for ldap authn   ``` <Realm className="org.apache.catalina.realm.JNDIRealm" connectionURL="ldaps://ldap.ad.ufl.edu:636" connectionName="CN=something-grouper,OU=Grouper,OU=Service Accounts,OU=somead,DC=ad,DC=school,DC=edu" connectionPassword="XXXXX" userBase="OU=users,DC=ad,DC=ufl,DC=edu" userSubtree="true" userSearch="(userPrincipalName={0})" adCompat="true" allRolesMode="authOnly" /> </Realm> ``` | See the tomcat documentation for correct location |
| grouper-ws.properties | ws.security.non-rampart.authentication.class = | This should be blank (get remote_user)  This is the default provided with container, do not overlay |
| grouper-www.conf | Has no auth directives | This is the default provided with container, do not overlay for authn reasons |
