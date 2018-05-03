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


This module contains source code and configuration files for the legacy "Admin UI" interface
that was removed in Grouper release 2.4.0.


There are a few steps to restoring the previous Admin UI functionality into your web application.

1) Back up your existing grouper webapp folder! While this process works at the point in time of the 2.4.0
release, future changes to the UI module may adversely affect this legacy code

2) In the base of your grouper web application (there should be a WEB-INF subfolder here), unpack the
grouper-legacy-ui.tar.gz archive. This will add in the legacy files, and replace other files with the
older legacy versions of the file.

```
cd $TOMCAT/webapps/grouper
tar xzf $DL/grouper-legacy-ui.tar.gz
```

3) Manually update WEB-INF/web.xml to restore the legacy servlet and Struts filters. Near the end of the file (before </web-app>),
add in the following xml fragments

```
<filter>
    <filter-name>Error Catcher</filter-name>
    <filter-class>edu.internet2.middleware.grouper.ui.ErrorFilter</filter-class>
</filter>
<filter>
    <filter-name>Caller page</filter-name>
    <filter-class>edu.internet2.middleware.grouper.ui.CallerPageFilter</filter-class>
</filter>

<filter-mapping>
    <filter-name>GrouperUi</filter-name>
    <url-pattern>*.do</url-pattern>
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
```
