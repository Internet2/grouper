- the .project and .classpath is for Eclipse 3.3 with web tools.  This is recommended for development
- checkout i2mi/grouper-ws/grouper-ws
- take all the resources/*.example.* and take ".example" out, and customize for your env.  Here is an example of the resulting resources dir

C:\mchyzer\isc\dev\grouper\grouper-ws\resources>ls
CVS                                   grouper.hibernate.properties
ehcache.example.xml                   grouper.properties
ehcache.xml                           log4j.example.properties
grouper.ehcache.example.xml           log4j.properties
grouper.ehcache.xml                   sources.example.xml
grouper.example.properties            sources.xml
grouper.hibernate.example.properties

- copy the build.template.properties to build.properties, and customize.  Shouldnt be much to do.

- for tomcat, add something like this in the server.xml in the <Host> tag:

<Context docBase="C:\mchyzer\isc\dev\grouper\grouper-ws\build\dist\grouper-ws" 
path="/grouper-ws" reloadable="false"/>

- do a "dist" build, and get started
