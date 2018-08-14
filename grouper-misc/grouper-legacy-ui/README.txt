====
    Copyright 2018 Internet2

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====


INTRODUCTION
------------

This module contains source code and configuration files for the legacy "Admin UI" and "Lite UI"
interfaces that were removed in Grouper release 2.4.0. While the classes and supporting files have
been removed from the main UI module, they exist in this legacy module in case your installation
still needs the functionality.


INSTALLING
----------

There are a few steps to restoring the previous Admin UI and Lite UI functionality into your web application.

1) Back up your existing grouper webapp folder! While this process works at the point in time of the 2.4.0
release, future changes to the UI module may adversely affect this legacy code


2) In the base of your grouper web application (there should be a WEB-INF subfolder here), unpack the
grouper-legacy-ui.tar.gz archive. This will add in the legacy files, and replace other files with the
older legacy versions of the file.

```
cd $TOMCAT/webapps/grouper
tar xzf $DL/grouper-legacy-ui.tar.gz
```

If done correctly, you should see a newly create file WEB-INF/classes/LEGACY-UI-INSTALLED.txt.


3) Manually update WEB-INF/web.xml to restore the legacy servlet and Struts filters. Near the end of the file (before </web-app>),
add in the following xml fragments. This template uses grouperRole=* for security. If you have a custom setting to allow
specific roles into the application, set the grouperRole to the correct value.


```
<filter>
    <filter-name>Error Catcher</filter-name>
    <filter-class>edu.internet2.middleware.grouper.ui.ErrorFilter</filter-class>
</filter>
<filter>
    <filter-name>Caller page</filter-name>
    <filter-class>edu.internet2.middleware.grouper.ui.CallerPageFilter</filter-class>
</filter>
<filter>
   <filter-name>Login check</filter-name>
   <filter-class>edu.internet2.middleware.grouper.ui.LoginCheckFilter</filter-class>
   <init-param>
           <param-name>failureUrl</param-name>
           <param-value>/index.jsp</param-value>
   </init-param>
           <init-param>
   <param-name>ignore</param-name>
           <param-value>:/populateIndex.do:/callLogin.do:/error.do:/logout.do:/status:</param-value>
   </init-param>
   <init-param>
           <param-name>grouperRole</param-name>
           <param-value>*</param-value>
   </init-param>
</filter>

<filter-mapping>
    <filter-name>GrouperUi</filter-name>
    <url-pattern>*.do</url-pattern>
</filter-mapping>
<filter-mapping>
    <filter-name>GrouperUi</filter-name>
    <url-pattern>/grouperExternal/appHtml/*</url-pattern>
</filter-mapping>
<filter-mapping>
    <filter-name>Error Catcher</filter-name>
    <url-pattern>*.do</url-pattern>
</filter-mapping>
<filter-mapping>
    <filter-name>Error Catcher</filter-name>
    <url-pattern>/gotoCallerPage</url-pattern>
</filter-mapping>
<filter-mapping>
    <filter-name>Caller page</filter-name>
    <url-pattern>/gotoCallerPage</url-pattern>
</filter-mapping>
<filter-mapping>
    <filter-name>Login check</filter-name>
    <url-pattern>*.do</url-pattern>
</filter-mapping>

<servlet>
    <servlet-name>action</servlet-name>
    <servlet-class>org.apache.struts.action.ActionServlet</servlet-class>
    <init-param>
        <param-name>config</param-name>
        <param-value>/WEB-INF/struts-config.xml</param-value>
    </init-param>
    <load-on-startup>2</load-on-startup>
</servlet>

<servlet-mapping>
    <servlet-name>action</servlet-name>
    <url-pattern>*.do</url-pattern>
</servlet-mapping>

<security-constraint>
    <web-resource-collection>
        <web-resource-name>UI</web-resource-name>
        <url-pattern>/grouperExternal/appHtml/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>*</role-name>
    </auth-constraint>
</security-constraint>

<security-constraint>
    <web-resource-collection>
        <web-resource-name>Tomcat login</web-resource-name>
        <url-pattern>/login.do</url-pattern>
    </web-resource-collection>
    <auth-constraint>
         <!-- NOTE:  This role is not present in the default users file -->
         <role-name>*</role-name>
    </auth-constraint>
</security-constraint>

```


REBUILDING THIS LEGACY MODULE FROM SOURCE
-----------------------------------------

The ant settings for this folder are set up to compile the classes and package everything into
the grouper-legacy-ui.tar.gz file. From the command line, simple type

```
ant
```

OR

```
ant distPackage
```

The process will clean out any existing compiled files and staging files, and then rebuild the output
file into dist/grouper-legacy-ui.tar.gz.

