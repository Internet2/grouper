rem The jars in this directory are required by ldappc and its dependents
rem (Grouper, Signet, Subject) but are not in the central repository.

rem Run the following commands from this directory to install these jars
rem to your local Maven repository. Scripts to do this are in this directory
rem in the install.bat and install.sh files.

rem External jars
mvn install:install-file -Dfile=invoker.jar -DpomFile=invoker.pom
mvn install:install-file -Dfile=jamon-2.7.jar -DpomFile=jamon-2.7.pom
mvn install:install-file -Dfile=jta-1.0.1B.jar -DpomFile=jta-1.0.1B.pom
mvn install:install-file -Dfile=mailapi-1.3.2.jar -DpomFile=mailapi-1.3.2.pom
mvn install:install-file -Dfile=smtp-1.3.2.jar -DpomFile=smtp-1.3.2.pom

rem Internet2 jars
mvn install:install-file -Dfile=apacheds-ldappc-schema.jar -DpomFile=apacheds-ldappc-schema.pom
mvn install:install-file -Dfile=grouper.jar -DpomFile=grouper.pom
mvn install:install-file -Dfile=signet-api.jar -DpomFile=signet-api.pom
mvn install:install-file -Dfile=signet-util.jar -DpomFile=signet-util.pom
mvn install:install-file -Dfile=subject.jar -DpomFile=subject.pom
