- the .project and .classpath is for Eclipse 3.3 with web tools.  This is recommended for development
- checkout i2mi/grouper-ws/grouper-ws
- run an ant quick
- customize the configs in the resources dir

C:\mchyzer\isc\dev\grouper\grouper-ws\resources>ls
CVS                                   grouper.hibernate.properties
ehcache.example.xml                   grouper.properties
ehcache.xml                           log4j.example.properties
grouper.ehcache.example.xml           log4j.properties
grouper.ehcache.xml                   sources.example.xml
grouper.example.properties            sources.xml
grouper.hibernate.example.properties

- customize the build.properties, and customize.  Shouldnt be much to do.

- for tomcat, add something like this in the server.xml in the <Host> tag:

<Context docBase="C:\mchyzer\isc\dev\grouper\grouper-ws\build\dist\grouper-ws" 
path="/grouper-ws" reloadable="false"/>

- manage your db driver.  Some db drivers are included with grouper-ws.  Delete/add to the lib/custom dir as appropriate.
- edit your web.xml appropriately.  If you arent doing container auth (e.g. for apache auth, or rampart, or kerberos), take out the section at bottom:

  <security-constraint>
    <web-resource-collection>
      <web-resource-name>Web services</web-resource-name>
      <url-pattern>/services/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
      <role-name>grouper_user</role-name>
    </auth-constraint>
  </security-constraint>

  <security-constraint>
    <web-resource-collection>
      <web-resource-name>Web services</web-resource-name>
      <url-pattern>/servicesRest/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
      <!-- NOTE:  This role is not present in the default users file -->
      <role-name>grouper_user</role-name>
    </auth-constraint>
  </security-constraint>

  <!-- Define the Login Configuration for this Application -->
  <login-config>
    <auth-method>BASIC</auth-method>
    <realm-name>Grouper Application</realm-name>
  </login-config>

  <!-- Security roles referenced by this web application -->
  <security-role>
    <description>
      The role that is required to log in to web service
    </description>
    <role-name>grouper_user</role-name>
  </security-role>



- do an "ant dist" build, and get started
