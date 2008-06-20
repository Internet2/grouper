*** README File for the testSetU Directory ***

The following describes the purpose of the testSetU directory.

The configuration files in this directory provide the data needed to
execute the built-in set of JUnit test cases.  The data used is 
provided in the testDb directory.  The configuration files in the
testSetU directory allow the user to run the Ant build script to create 
a connection to an LDAP server (that must be provided by the user),
to start the Signet and Grouper databases, and run the built-in 
test case suite. 

This test set is used automatically only when running the Ant
targets "install" or "test".  To use it for other targets,
one would need to add the following argument to the ant command line: 

-Dmaster=conf/testConfigurations/testSetU/antMaster.properties

The Developers Guide in the Javadoc documentation provides additional 
information about each of the files in this directory.  See the 
README.txt file in the top level installation directory for how
to access this information.

The only likely change the user may need to make to these files is
the following.  If the user does not have an environmental variable
set for JAVA_HOME set to JDK1.5, then he may need to set a variable
in the antSystem.properties file to set the JVM version to be used.
