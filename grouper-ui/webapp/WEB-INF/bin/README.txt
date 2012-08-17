Note, if you are on unix, you need to convert the script to unix and make it executable.  e.g.

[appadmin@flash2 bin]$ dos2unix gsh
dos2unix: converting file gsh to UNIX format ...
[appadmin@flash2 bin]$ chmod +x gsh
[appadmin@flash2 bin]$ ./gsh 
Using GROUPER_HOME: /opt/appserv/tomcat_2v/webapps/grouper/WEB-INF/bin/..
Using GROUPER_CONF: /opt/appserv/tomcat_2v/webapps/grouper/WEB-INF/bin/../classes
Using JAVA: /opt/appserv/java6/bin/java
using MEMORY: 64m-512m
Grouper starting up: version: 1.5.1, build date: 2010/01/22 09:58:41, env: DEV
grouper.properties read from: /opt/appserv/tomcat_2v/webapps/grouper/WEB-INF/classes/grouper.properties
Grouper current directory is: /opt/appserv/tomcat_2v/webapps/grouper/WEB-INF/bin
log4j.properties read from:   /opt/appserv/tomcat_2v/webapps/grouper/WEB-INF/classes/log4j.properties
Grouper warning, it is detected that you are logging edu.internet2.middleware.grouper as ERROR and not WARN level.  It is recommended to log at at least WARN level in log4j.properties
Grouper is logging to file:   appender type: SMTPAppender, /opt/appserv/tomcat_2v/logs/grouper/grouper_error.log, at min level ERROR for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /opt/appserv/tomcat_2v/webapps/grouper/WEB-INF/classes/grouper.hibernate.properties
grouper.hibernate.properties: user@jdbc:oracle:thin:@server:1521:sid
sources.xml read from:        /opt/appserv/tomcat_2v/webapps/grouper/WEB-INF/classes/sources.xml
sources.xml jdbc source id:   servPrinc: GrouperJdbcConnectionProvider
sources.xml groupersource id: g:gsa
sources.xml jdbc source id:   pennperson: GrouperJdbcConnectionProvider
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Type help() for instructions
gsh 0% 