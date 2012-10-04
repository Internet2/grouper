
To build the grouperVoot.jar
- Have grouper and grouper-ws project checked out and built
- Copy the build.example.properties to build.properties
- Configure the build.properties to point to grouper and grouper-ws
- Run the ant target build or distBinary

To run Grouper Voot
- Setup and run the grouper WS
- Copy the grouperVoot.jar to the WEB-INF/lib of grouper WS
- Make sure your sources.xml has an email attribute name in applicable subject sources:

     <!-- If using emails and need email addresses in sources, set which attribute has the email address in this source -->
     <init-param>
       <param-name>emailAttributeName</param-name>
       <param-value>email</param-value>
     </init-param>

- Setup the web.xml

  <filter-mapping>
    <filter-name>Grouper service filter</filter-name>
    <url-pattern>/voot/*</url-pattern>
  </filter-mapping>

  <servlet>
    <servlet-name>VootServlet</servlet-name>
    <display-name>Voot Servlet</display-name>
    <servlet-class>edu.internet2.middleware.grouperVoot.VootServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
  </servlet>

  <servlet-mapping>
    <servlet-name>VootServlet</servlet-name>
    <url-pattern>/voot/*</url-pattern>
  </servlet-mapping>

- If you are using basic auth in the web.xml, make sure the voot servlet is protected:

  <security-constraint>
    <web-resource-collection>
      <web-resource-name>Voot services</web-resource-name>
      <url-pattern>/voot/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
      <role-name>grouper_user</role-name>
    </auth-constraint>
  </security-constraint>

- Voot documentation:

https://github.com/andreassolberg/voot/wiki/Protocol

- Turn on the grouper web services, and try the following URL's:

https://grouper.whatever.com/grouperWs/voot/groups/@me
https://grouper.whatever.com/grouperWs/voot/groups
https://grouper.whatever.com/grouperWs/voot/people/@me
https://grouper.whatever.com/grouperWs/voot/people/@me/aStem:aGroup2  [note: put in valid group name]

You can pass in a param to indent the response:

https://grouper.whatever.com/grouperWs/voot/groups/@me?indentResponse=true
https://grouper.whatever.com/grouperWs/voot/groups?indentResponse=true
https://grouper.whatever.com/grouperWs/voot/people/@me?indentResponse=true
https://grouper.whatever.com/grouperWs/voot/people/@me/aStem:aGroup2?indentResponse=true





