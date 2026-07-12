---
title: "Authentication to the Grouper UI"
space: Grouper
pageId: 28548676
version: 44
lastUpdated: 2026-07-12T15:26:56.717Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548676/Authentication+to+the+Grouper+UI
---

## Overview

There are a few different ways of authenticating to the Grouper UI. For any method, the mapping of the login username to a Grouper subject works the same.

After authentication, Grouper will attempt to retrieve the username by these methods in order:

- `httpServletRequest.getRemoteUser()`
- a specific http header, as configured in property `grouper.ui.authentication.http.header` in grouper-ui.properties
- servlet attribute `REMOTE_USER`
- `httpServletRequest.getUserPrincipal().getName()`
- httpServletRequest session attribute `authUser`

If none of these return a non-empty string, the authentication will fail with error:

```
"You have an anonymous session since you are not logged in, but this section requires you to be logged in. Maybe No username found. Your identity provider might not be sending your username to this application. Either you need to use a different identity provider, or ask your IT department to send your username to this application."
```

If the username is obtained in the above step, Grouper will attempt to convert this to a subject, trying as either an id or an identifier. By default it will search in all subject sources. If you want to constrain the login to specific subject sources, this can be configured in grouper-ui.properties value `grouper.ui.authentication.sourceIds` . For example, you may want to limit logins to regular people, excluding irrelevant accounts such as GrouperSystem. You can specify multiple sources as a comma-separated list.

Since the resolver will look up the subject by either id or identifier, the SQL or LDAP queries in subject.properties may be adjusted to resolve on alternative fields, in case the login username differs from the normal usage of id or identifier. For example, if a source normally uses `uid` as identifier but the login scheme utilizes `eduPersonPrincipalName` style usernames (e.g. `uid@example.com`), an LDAP source's `searchSubjectByIdentifier` parameter can be modified to use both as identifiers:

```
subjectApi.source.myLdap.search.searchSubjectByIdentifier.param.filter.value = (&(|(uid=%TERM%)(eduPersonPrincipalName=%TERM%))(objectclass=eduPerson))
```

As an alternative, if the web server sets a `uid` HTTP header after login (e.g., if using a Shibboleth SSO identity provider that releases `uid` ), the configuration property `grouper.ui.authentication.http.header` can be set to `uid` . In this case, the resolver will use the uid header value as the %TERM% to look up.

Note: if you use a load balancer or reverse proxy that offloads the SSL then you need to set this in the tomcat server.xml `<Connector>` element for AJP. Something (maybe csrfguard) will redirect from non SSL to SSL and that will send 302's to ajax and cause errors when trying to parse XML and cant.

```
<Connector ... scheme="https" secure="true" />
```

## Grouper built-in authentication (v2.5+)

For a UI/WS that was installed using the Grouper Installer, Tomcat authentication using web.xml security-constraint directives is not used by default since v2.5. It has been replaced by a Grouper authentication module that stores usernames and encrypted passwords in the database. See [this page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549360/Grouper+Built-in+Basic+Authentication+to+UI+and+Web+Services) for further explanation. It is not enabled by default, except for a quickstart container. It is enabled by setting container parameters `GROUPER_UI_GROUPER_AUTH` and `GROUPER_WS_GROUPER_AUTH` , or by `grouper.hibernate.properties parameters grouper.is.ui.basicAuthn` and `grouper.is.ws.basicAuthn` . It is not recommended to use tomcat-users.xml anymore, but it can be added back manually if desired. Besides built-in authentication, external authentication services like Shibboleth and CAS are excellent options.

## Authentication using Shibboleth single sign-on (SSO)

> Thanks to Newcastle University for providing this information on authentication to Grouper using Shibboleth.

This setup assumes an Apache web server with the mod_shib module available, on a server with a Shibboleth service provider (SP) already set up. The servlet container (Tomcat or other) is connected to Apache, typically by proxy_ajp or another mechanism, and the authentication is removed from Tomcat and the Grouper servlet configuration as described below. Apache takes control over the authentication, thus maintaining a login session independent of Grouper. Any users trying to access the UI will need to create a valid session with Shibboleth, before being granted or denied access to the Grouper application.

The following page will explain how we have achieved this, there maybe some difference in configurations required depending on server set up.

1. Configure Shibboleth so that it will protect any content that lives under the main Grouper install directory. To do this, enable mod_shib (your location and version may vary), and require Shibboleth for the /grouper/* URI;
  
  
  ```
  LoadModule mod_shib /usr/lib64/shibboleth/mod_shib_22.so
  
  <Location /grouper>
  Authtype shibboleth
  ShibRequireSession On
  require shibboleth
  ShibUseHeaders On</Location>
  ```
2. Then add an AJP proxy to the Apache configuration to forward requests for /grouper to the Tomcat install
  
  
  ```
  LoadModule proxy_ajp_module modules/mod_proxy_ajp.so
  
  ProxyPass /grouper ajp://localhost:8009/grouper
  ProxyPassReverse /grouper ajp://localhost:8009/grouper
  ```
3. Add an AJP connector to Tomcat. The Tomcat conf/server.xml configuration may already have an AJP connector set up. If so, make sure the property `tomcatAuthentication="false"` is included.
  
  
  ```
  <Connector port="8009" protocol= "AJP/1.3" tomcatAuthentication="false" redirectPort="8443"/>
  ```
4. (< v2.5) Remove authentication from the servlet application. From the grouper WEB-INF/web.xml file, remove all security-constraint, login-config, and security-role sections:
  
  
  ```
    <security-constraint>
      <web-resource-collection>
        <web-resource-name>UI</web-resource-name>
        <url-pattern>/grouperUi/app/*</url-pattern>
      </web-resource-collection>
      <auth-constraint>
        <role-name>grouper_user</role-name>
      </auth-constraint>
    </security-constraint>
    <security-constraint>
      <web-resource-collection>
        <web-resource-name>UI</web-resource-name>
        <url-pattern>/grouperUi/appHtml/*</url-pattern>
      </web-resource-collection>
      <auth-constraint>
        <role-name>grouper_user</role-name>
      </auth-constraint>
    </security-constraint>
    <security-constraint>
      <web-resource-collection>
        <web-resource-name>UI</web-resource-name>
        <url-pattern>/grouperExternal/app/*</url-pattern>
      </web-resource-collection>
      <auth-constraint>
        <role-name>grouper_user</role-name>
      </auth-constraint>
    </security-constraint>
    <login-config>
      <auth-method>BASIC</auth-method>
      <realm-name>Grouper Application</realm-name>
    </login-config>
    <security-role>
      <description>
        The role that is required to log in to the Grouper UI
      </description>
      <role-name>grouper_user</role-name>
    </security-role>
  ```

The configurations shown above maybe slightly different for other environments, though hopefully it will be of some help.

## CAS authentication to the Grouper UI

> Thanks to [California Polytechnic State University](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543636/California+Polytechnic+State+University+San+Luis+Obispo) for providing information on authentication to Grouper using CAS.

- Method 1: mod_auth_cas
- Method 2: Tomcat <Realm> authentication

### CAS authentication to Grouper (v2.5–v4.x) TAP container using mod_auth_cas

Due to changes in packaging and instrumenting of the web application (i.e., web.xml is not the primary configuration method any more), previously working configuration methods require more work, or are no longer functional. Integration with CAS can still be accomplished by the use of mod_auth_cas within the Apache httpd process and some selective configuration file updates.

This will assume that you have disabled other environment variable enabled methods of authentication and that you are building your own local docker image for deployment as per [Install the Grouper container with maturity level 1](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554549/Install+the+Grouper+container+with+maturity+level+1).

1. Create a directory for your docker overlay and create the directory `slashRoot` inside it.
2. Create a `Dockerfile` with at least the following contents.
  
  
  ```
  ARG GROUPER_VERSION=2.5.XX
  FROM i2incommon/grouper:${GROUPER_VERSION}
  
  # Need to install CAS so it can be used at the Apache server level
  RUN yum -y install mod_auth_cas
  
  # this will overlay all the files from /opt/grouperContainer/slashRoot on to /
  COPY slashRoot /
  
  RUN chown -R tomcat:tomcat /opt/grouper \
  && chown -R tomcat:tomcat /opt/tomee
  ```
3. Add the files below under `slashRoot`:  
  
  
  1. /etc/httpd/conf.d/auth_cas.conf.cas
    
    
    ```xml
    CASCookiePath /var/cache/httpd/mod_auth_cas/
    CASLoginURL https://__CAS_HOST_NAME__/cas/login
    CASValidateURL https://__CAS_HOST_NAME__/cas/serviceValidate
    CASRootProxiedAs https://__GROUPER_UI_HOST_NAME__
    
    #CASDebug On
    CASVersion 2
    
    <Location /grouper>
      Authtype CAS
      require valid-user
    </Location>
    ```
  2. /usr/local/bin/grouperScriptHooks.sh
    
    1. This hook script only installs the CAS filter in the apache layer when running the UI. It copies the file above over the default installed by yum, and then replaces the placeholder text with environment variables.
  3. ```bash
    #!/bin/sh
    
    # called at the beginning of the container startup
    # after logging is setup
    # grouperScriptHooks_prepConfPost() {
    #	return
    # }
    
    # called after the component command has been prepped
    # grouperScriptHooks_prepComponentPost() {
    # 	return
    # }
    
    # called after the finishPrep is called before the setupFiles
    # grouperScriptHooks_finishPrepPost() {
    # 	return
    # }
    
    # called after the setupFiles functions is called, almost before the process starts
    grouperScriptHooks_setupFilesPost() {
      echo "RUNNING CUSTOM grouperScriptHooks_setupFilesPost: GROUPER_UI=$GROUPER_UI"
      if [ "$GROUPER_UI" = "true" ]; then
        # Install needed CAS configuration
        cp -v /etc/httpd/conf.d/auth_cas.conf.cas /etc/httpd/conf.d/auth_cas.conf
        # Populate with this instance's hostnames
        sed -i "s|__CAS_HOST_NAME__|$CUSTOM_CAS_HOST_NAME|g"               /etc/httpd/conf.d/auth_cas.conf
        sed -i "s|__GROUPER_UI_HOST_NAME__|$GROUPER_APACHE_SERVER_NAME|g"  /etc/httpd/conf.d/auth_cas.conf
        echo "Enabled CAS Authentication Using CAS_HOST_NAME=$CUSTOM_CAS_HOST_NAME"
      fi
    
    	return
    }
    
    # called after the chown at end of setupFiles, right before the process starts
    # grouperScriptHooks_setupFilesPostChown() {
    # 	return
    # }
    
    # export everything
    export -f grouperScriptHooks_setupFilesPost
    ```
4. Either in your dockerfile with ENV commands or upon startup of your container, set the following environment variables:
  
  
  
  1. `GROUPER_APACHE_SERVER_NAME` : host name (no scheme or path) of the server. Will be used in the Apache ServerName directive and to build the service URL used for redirects back from CAS.
  2. `CUSTOM_CAS_HOST_NAME` : host name (no scheme or path) of the CAS server.
5. You will also likely want to set `GROUPERUI_LOGOUT_REDIRECTTOURL` to `http://${CUSTOM_CAS_HOST_NAME}/cas/logout`
6. Build and tag your docker image and then run as per the install instruction page linked above. As with the other authentication methods, you should be able to see that the user ID from CAS has been proxied through by turning on logging by adding the below to your log4j.properties file.
  
  
  ```
  log4j.logger.edu.internet2.middleware.grouper.ui.GrouperUiFilter = DEBUG
  ```

### CAS authentication method 2: Tomcat <Realm> authentication

See also:  [https://github.com/apereo/java-cas-client](https://github.com/apereo/java-cas-client)

Tomcat authentication using realms works both before and after v2.5.0, although some of the file locations differ in containers.

The context definition in server.xml for Tomcat (in the container, this is /opt/tomee/conf/Catalina/localhost/grouper.xml) looks like this:

```xml
<Context docBase="/ucd/opt/grouper-ui/dist/grouper" path="/grouper"
    reloadable="false" mapperContextRootRedirectEnabled="true" mapperDirectoryRedirectEnabled="true" >

  <Realm className="org.jasig.cas.client.tomcat.v85.PropertiesCasRealm"
     propertiesFilePath="/etc/tomcat/grouper-users.properties"
   />

    <!--
       If you do not need to map users to roles via a grouper-users.properties file use this instead.
       <Realm className="org.jasig.cas.client.tomcat.v85.AssertionCasRealm" />
    -->

  <Valve className="org.jasig.cas.client.tomcat.v85.Cas20CasAuthenticator"
     encoding="UTF-8"
     casServerLoginUrl="https://CAS_SERVER/cas/login"
     casServerUrlPrefix="https://CAS_SERVER/cas/"
     serverName="https://server-name.edu"
   />

  <!-- Single sign-out support -->
  <Valve className="org.jasig.cas.client.tomcat.v85.SingleSignOutValve"
    artifactParameterName="SAMLart"
  />
</Context>
```

The following jar files will need to go into the Tomcat lib directory (with current versions as of September 2020). The container equivalent folder is /opt/tomee/lib. For other versions of Tomcat (TomEE in the container is compatible with Tomcat 8.5), change v85 to v8, v7, or v6 as appropriate.

- org.jasig.cas.client : cas-client-core (v3.6.1) [[Download](https://search.maven.org/search?q=g:org.jasig.cas.client%20AND%20a:cas-client-core)]
- org.jasig.cas.client : cas-client-integration-tomcat-common (v3.6.1) [[Download](https://search.maven.org/search?q=g:org.jasig.cas.client%20AND%20a:cas-client-integration-tomcat-common)]
- org.jasig.cas.client : cas-client-integration-tomcat-v85 (v3.6.1) [[Download](https://search.maven.org/search?q=g:org.jasig.cas.client%20AND%20a:cas-client-integration-tomcat-v85)]
- org.slf4j : slf4j-api (v1.7.26) [[Download](https://search.maven.org/search?q=g:org.slf4j%20AND%20a:slf4j-api)] (in versions < v2.5.0 only)

(< v2.5.0) In Grouper's WEB-INF/web.xml, comment out the login-config and security-role sections. The security-constraint sections should remain so that authentication is triggered. The role-name can be changed to "*" (or "**" if that doesn't work) to allow all validated users to log in.

(v2.5.0+ container) In Grouper's WEB-INF/web.xml, add:

```
    <security-constraint>
        <display-name>Web Login Service</display-name>
        <web-resource-collection>
            <web-resource-name>user authentication</web-resource-name>
            <url-pattern>/*</url-pattern>
        </web-resource-collection>
        <auth-constraint>
            <role-name>**</role-name>
        </auth-constraint>
        <!-- uncomment in production? Or with SSL?
        <user-data-constraint>
            <transport-guarantee>CONFIDENTIAL</transport-guarantee> 
        </user-data-constraint>
        -->
    </security-constraint>
```

To sum up, the files that would go into a slashRoot directory of a Maturity 0 container are:

- ./opt/grouper/grouperWebapp/WEB-INF/web.xml
- ./opt/tomee/lib/cas-client-core-3.6.1.jar
- ./opt/tomee/lib/cas-client-integration-tomcat-v85-3.6.1.jar
- ./opt/tomee/lib/cas-client-integration-tomcat-common-3.6.1.jar
- ./opt/tomee/conf/Catalina/localhost/grouper.xml

In addition to these files, if there are issues with Tomcat communicating with CAS (i.e., for back-channel ticket validation), the server certificate will need to be added to ./usr/lib/jvm/java-1.8.0-amazon-corretto/jre/lib/security/cacerts.

## See also

[Grouper Built-in Basic Authentication to UI and Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549360/Grouper+Built-in+Basic+Authentication+to+UI+and+Web+Services)

[Grouper Web Services Authentication](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548787/Grouper+Web+Services+Authentication)

[University of Pennsylvania Example](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545158/Grouper+UI+custom+authentication+example)

For an overview of authenticating to Grouper using Shib, see also the  [Grouper UI Training Video, around minute 7.30](http://www.youtube.com/watch?v=fhl8FUauRM0&feature=plcp).
