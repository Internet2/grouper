#!/bin/sh

# The jars in this directory are required by ldappc and its dependents
# (Grouper, Signet, Subject) but are not in the central repository.

# Run the following commands from this directory to install these jars
# to your local Maven repository. Scripts to do this are in this directory
# in the install.bat and install.sh files.

# External jars
mvn install:install-file -Dpackaging=jar -DgroupId=com.jamonapi -DartifactId=jamon -Dversion=2.7 -Dfile=jamon-2.7.jar
mvn install:install-file -Dpackaging=jar -DgroupId=javax.transaction -DartifactId=jta -Dversion=1.0.1B -Dfile=jta-1.0.1B.jar
mvn install:install-file -Dpackaging=jar -DgroupId=javax.mail -DartifactId=mailapi -Dversion=1.3.2 -Dfile=mailapi-1.3.2.jar
mvn install:install-file -Dpackaging=jar -DgroupId=javax.mail -DartifactId=smtp -Dversion=1.3.2 -Dfile=smtp-1.3.2.jar

# Internet2 jars
mvn install:install-file -Dpackaging=jar -DgroupId=edu.internet2.middleware.ldappc -DartifactId=apacheds-ldappc-schema -Dversion=1.0 -Dfile=apacheds-ldappc-schema.jar -DpomFile=apacheds-ldappc-schema.pom
mvn install:install-file -Dpackaging=jar -DgroupId=edu.internet2.middleware.grouper -DartifactId=grouper -Dversion=1.3.0 -Dfile=grouper.jar -DpomFile=grouper.pom
mvn install:install-file -Dpackaging=jar -DgroupId=edu.internet2.middleware.signet -DartifactId=signet-api -Dversion=1.3.0-RC1 -Dfile=signet-api.jar -DpomFile=signet-api.pom
mvn install:install-file -Dpackaging=jar -DgroupId=edu.internet2.middleware.signet -DartifactId=signet-util -Dversion=1.3.0-RC1 -Dfile=signet-util.jar -DpomFile=signet-util.pom
mvn install:install-file -Dpackaging=jar -DgroupId=edu.internet2.middleware.subject -DartifactId=subject -Dversion=0.3.1 -Dfile=subject.jar -DpomFile=subject.pom
