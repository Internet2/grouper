################### After downloading the grouper.ui*tar.gz file

Edit the build.properties (might need to copy from template first), set the location of the grouper API, 
and set other properties.  See a quick start document, google for this, or try these:

https://spaces.internet2.edu/display/Grouper/Grouper+Installer
https://spaces.internet2.edu/display/Grouper/Starting+with+Grouper
https://spaces.internet2.edu/display/Grouper/Grouper+Hosted+on+a+Cloud+Server

Run an "ant dist", and run tomcat based on the build/dist directory.


################### For developers who checked out the SVN grouper-ws project

- the .project and .classpath is for Eclipse 3.3 with web tools.  This is recommended for development
- checkout i2mi/grouper
- checkout i2mi/grouper-ws/grouper-ws
- run an ant quick
- customize the configs in the conf dir

C:\mchyzer\isc\dev\grouper\grouper-ws\conf>ls
grouper.ws.properties

- customize the build.properties.  Shouldnt be much to do, but make sure the grouper.dir is set correctly (and grouper jar, conf, lib)

- for tomcat, add something like this in the server.xml in the <Host> tag:

<Context docBase="C:\mchyzer\isc\dev\grouper\grouper-ws\build\dist\grouper-ws" 
path="/grouper-ws" reloadable="false"/>

- manage your db driver.  By default they will be added from grouper home.  If you dont want that, tell build.properties to not
copy drivers, and delete/add to the lib/custom dir as appropriate.
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
- if you want to run tests, or restrict access, set this in grouper-ws.properties:

ws.client.user.group.name = etc:webServiceClientUsers

- probably want to set that in grouper.properties to auto-create:

configuration.autocreate.group.name.0 = etc:webServiceClientUsers
configuration.autocreate.group.description.0 = users allowed to log in to the WS
configuration.autocreate.group.subjects.0 = GrouperSystem

- probably want a login in your tomcat-users.xml if not doing authentication some other way

  <role rolename="grouper_user"/>
  <user password="********" roles="grouper_user" username="GrouperSystem"/>
