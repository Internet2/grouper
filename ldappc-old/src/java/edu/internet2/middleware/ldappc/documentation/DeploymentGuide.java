/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package edu.internet2.middleware.ldappc.documentation;


/**
 * <h1> Deployment Guide </h1>
 * <p>
 * This document describes how to deploy the Ldappc program.
 * </p>
 *
 * <h2>Installation Quickstart</h2>
 * <p>
 * The installation distribution file is available as a compressed tar file.
 * </p>
 * <h3> 
 * Prerequisites
 * </h3> 
 * <p>
 * An LDAP server must be installed by the deployer prior to
 * installation of the Ldappc distribution package.  Actually, the 
 * installation can be performed without an LDAP server, but most of the 
 * test cases will fail.  The ldappc properties file must be modified to
 * contain the appropriate values in the "ldap" section of the ldappc.xml 
 * file in the conf directory.  In addition, the ldappc.schema file in the
 * conf directory must be added to the LDAP configuration before starting it.  
 * For OpenLDAP, this is done by adding a line similar to the following one 
 * to the slapd.conf file:
 * </p>
 * <pre>
 *      include yourInstallationDirectory/conf/ldappc.schema
 * </pre>
 * <p>
 * For test sets that require additional schema, additional lines pointing
 * to the other schema will be needed.  To run the built-in test case suite,
 * you will need to add the following line.
 * </p>
 * <pre>
 *      include yourInstallationDirectory/conf/testConfigurations/testSetU/kitneduperson.schema 
 * </pre>
 * </p>
 * <p>
 * For efficient use of the LDAP directory, it is recommed that at least the following
 * entries are indexed when setting up the ldap server.
 * </p>
 * <p>
 * <pre>
 *     index   objectClass           eq
 *     index   uid                   pres,eq
 *     index   cn                    pres,eq,sub
 *     index   sn                    pres,eq,sub
 *     index   kitnEduPersonRegId    eq
 *     index   hasMember             eq
 *     index   member                eq
 *     index   isMemberOf            eq
 *     index   eduPersonEntitlement  eq 
 * </pre>
 * </p>
 * If one uses "uniqueMember" instead of "member" in the ldappc.xml file, then one would
 * want to replace "member" above with "uniqueMember". 
 * <h3> 
 * Procedure
 * </h3> 
 * <p>
 * The first step in the installation is to untar the installation archive
 * file to an installation directory of your choice. 
 * This will create a subdirectory of ldappc-theVersionNumber, which will 
 * be referred to as the installation directory. 
 * </p>
 * <p>
 * For those who prefer jumping right in, first read the section below that 
 * concerns configuration file settings that are required if the built-in test 
 * cases are going run.  Then you can navigate to the installation directory 
 * in a command window, and type "ant".  This will run the ant build.xml script 
 * found in that directory in the interactive mode that will prompt you for common 
 * Ant targets.  The first time you use the script, and after you have modified the
 * ldapppc.xml and testLdappc.properties files as instructed herein, select the "install" target.  
 * Alternatively, use the "help" target to gain a better understanding of the Ant 
 * script first.  The install target will compile the source code and create 
 * class files under the build/classes directory.  It will also automatically 
 * run one set of the built-in test cases.
 * </p>
 * <h3> 
 * Essential Configuration File Settings.
 * </h3> 
 * <p>
 * The Ant build script can be used in a simple manner for installation
 * and testing.  It also has the ability to allow management of multiple
 * configuration of configuration and testing files.  This 
 * section describes only the use of the Ant build script for installation
 * and initial testing.
 * </p>
 * <p>
 * There are several configuration files that control the operations of
 * of the Ant build script.  Only one of these configuration files,
 * the ldappc.xml file, requires changes in order to run anything useful 
 * with Ldappc.  The section of the ldappc.xml that requires
 * settings is the under the "ldap" tag.  These settings describe how to
 * connect to the the LDAP server and how to authenticate to the server.
 * For production use, the entire contents of the ldappc.xml file will need
 * to taylored to the users requirements. 
 * </p>
 * <p>
 * See the ldappcTemplate.xml file, which is under the conf directory, for 
 * instructions on what values are needed.  The conf directory contains a
 * copy of the ldappc.xml file used in the conf/testConfigurations/testSetU
 * directory that is used for executing the built-in test cases.
 * You can change the conf/ldappc.xml file for your own use, as you could
 * always restore it from the testSetU copy.  Other files in this
 * directory have copies that include "Template" in the name. 
 * The ldappcTemplate.xml file is different from the ldappc.xml file
 * in that the former contains detailed descriptions of all of the
 * allowed parameters, while the later contains actual values used
 * without most of the descriptive text. 
 * </p>
 * <p>
 * Make sure that you have the JAVA_HOME environment variable set
 * to point to the JDK1.5.  The code has been tested with JDK1.5 only
 * If you do not wish to set JAVA_HOME or wish to use JDK1.4,  
 * a change will be needed to one other configuration file, the
 * antSystem.properties file, to set the "jvmbase" property.
 * </p>
 * <h4> 
 * Setting up the LDAP Directory Test Data Content  
 * </h4> 
 * <p>
 * To set up the LDAP directory with the data needed for testing the
 * distribution, change to the test data directory and execute a
 * command to add the data to the LDAP directory as follows.
 * <p>
 * <pre> 
 *     cd yourInstallationDirectory\conf\testConfigurations\testSetU
 *     ldapadd -x -D "yourSecurityPrincipal" -w "yourRootPW" -f ldappcSubject.ldif 
 * </pre> 
 * </p>
 * <p>
 * In the above command, "yourSecurityPrincipal" represents the value used in the 
 * security principal setting in the ldapp.xml file. 
 * </p>
 * <p>
 * In the above command, "yourRootPW" represents the password used in the 
 * security credential setting in the ldapp.xml file.  Make sure that the 
 * same value is used in the ldapp.xml file, in the above command, and in 
 * the configuration file for your LDAP server.
 * </p>
 * <p>
 * The user may wish to make additional changes to the ldappcSubject.ldif and
 * the ldappc.xml files; however, until the initial set up of the distribution
 * has been verified, such changes should be kept to a minimum. 
 * </p>
 * <h4> 
 * Optional Advanced User Testing Configurations
 * </h4> 
 * <p>
 * The configuration files are set up for versatile usage and allow
 * overriding a variety of properties in the Ant build script.
 * The later section, "The Build File" describes these configuration files.
 * </p> 
 * <h3> 
 * Expected Results
 * </h3> 
 * 
 * All of the more than 40 test cases should run successfully if the installation is 
 * successful.  There should be no error messages sent to the ErrorLog file.
 * <h3> 
 * Common Problems
 * </h3> 
 * <p> 
 * The following is a list of possible causes why the test cases may fail
 * due incorrect setup.
 * </p> 
 * <p> 
 * If one sees error messages concerning a credential 
 * problem, this may indicate an error in the set up of the LDAP 
 * server or may simply indicate that you have incorrect values for  
 * the "security_principal" and "security_credentials" parameters in the 
 * ldappc.xml file.  
 * </p> 
 * <p> 
 * A failure will occur if the ldappc.schema file and other schema
 * files mentioned above are not present either in the LDAP schemas provided
 * in the LDAP server's directory or referenced by the server configuration.
 * </p>
 * <p> 
 * Make sure the values of source attribute on the source-subject-identifier match 
 * the values of the id tag in the source tag in the sources.xml file.  Since no
 * changes to either of these is required for use of the built-in test cases,
 * this should not be a problem for installation testing.  However, this may
 * be easy error to make when implement one's own databases.
 * </p>
 * <p> 
 * If one is used to using two separate sources.xml files for Grouper and
 * Signet, realize that this will cause a problem if attempted with Ldappc.
 * Ldappc needs a sources.xml file in its classpath; if two appear, only the
 * first would be used.  Therefore, if you are using both Grouper and Signet, 
 * you must combine the two sources.xml files into a single file. 
 * </p>
 * <p> 
 * If you get the message "Class file has wrong version 49.0, should be 48.",
 * this indicates that one is trying to use some classes that compiled under JDK 1.4 
 * with others that compiled under JDK 1.5. make sure that your value of JAVA_HOME
 * is JDK1.5 or that you set the jvmBase value to that for JDK1.5 in the antSystem.xml 
 * file.
 * </p>
 * <p> 
 * Another source of problems during development occurred because of the
 * existence of two different versions of the hsqldb.jar file.  The Signet
 * Quickstart database used version 1.8.0 and the Grouper Quickstart
 * database used 1.7.2.  It has reported that Grouper will not work properly 
 * with version 1.8.0. Nevertheless, the Ldappc built-in test cases run using
 * version 1.8.0 for both Signet and Grouper.  The build file places the latest 
 * version of the hsqldb.jar file (1.8.0) before all other jar
 * files in the classpath to assure that it is used rather than another version.
 * For production use, the user will want to use whatever versions of hsqldb.jar
 * have been working for them for Grouper and Signet.  
 * </p>
 * <p> 
 * If one is not using the same version of hsqldb.jar for testing and for
 * production, then remember to replace the the hsqldb.jar file in the installation
 * directory's lib directory whenever one switches between testing and production.  
 * If one is not using the built-in capability to start the Grouper and Signet
 * databases and is starting one's own databases, then for production one can remove 
 * the provided hsqldb.jar file from the lib directory as it is only needed for testing.
 * </p>
 * <h2>Directory Structure</h2>
 * <p> 
 * The following describes the directory structure of the program after the 
 * initial installation of the distribution tar file.  Prior to installation, 
 * the build directory does not exist. 
 * </p> 
 * <p> 
 * Table key: "..." means that lower level directories are not shown. 
 * </p> 
 * <p> 
 * The installation directory, ldappc-${versionNumber}, is not shown to avoid extra indentation. 
 * </p> 
 * <table>
 * <blockquote>
 * <table border=1 padding=5>
 *     <tr>
 *         <th>Directory</th>
 *         <th>Discussion</th>
 *     </tr>
 *     <tr>
 *         <td><code>build</code></td>
 *         <td>Top level directory build contents</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;ldappc</code></td>
 *         <td>Container for compiled code, copies of jar and config files</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;classes</code></td>
 *         <td>Compiled code</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;conf</code></td>
 *         <td>Configuration files</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;logs</code></td>
 *         <td>Log files</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;lib</code></td>
 *         <td>Library files for the selected system</td>
 *     </tr>
 *     <tr>
 *         <td><code>conf</code></td>
 *         <td>Sample config files: ldappc.xml file template</td>
 *     </tr>
 *     <tr>
 *         <td><code>doc</code></td>
 *         <td>Documentation files</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;javadoc</code></td>
 *         <td>Javadoc files</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;javadocSupplement</code></td>
 *         <td>Doc file integrated with Javadoc files</td>
 *     </tr>
 *     <tr>
 *         <td><code>lib</code></td>
 *         <td>Jar files</code></td>
 *     </tr>
 *     <tr>
 *         <td><code>logs</code></td>
 *         <td>Log files</td>
 *     </tr>
 *     <tr>
 *         <td><code>src</code></td>
 *         <td>Container for source code</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;java</code></td>
 *         <td>Container for Java source code</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;edu</code></td>
 *         <td>package path</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;internet2</code></td>
 *         <td>package path</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;middleware</code></td>
 *         <td>package path</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ldappc</code></td>
 *         <td>Primary directory for ldappc source code</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;documentation</code></td>
 *         <td>Directory containing architecture, design, and usage docs</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ldap</code></td>
 *         <td>LDAP-related code</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;logging</code></td>
 *         <td>Log handling code</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;schema</code></td>
 *         <td>Schema file</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;synchronize</code></td>
 *         <td>Synchronization code</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;util</code></td>
 *         <td>Utility files</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ldappcTest ...</code></td>
 *         <td>Directory for ldappc test source code</td>
 *     </tr>
 *     <tr>
 *         <td><code>testDb</code></td>
 *         <td>Container for test database and configuration</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;grouper</code></td>
 *         <td>Container for Grouper test database and configuration</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;conf</code></td>
 *         <td>Container for Grouper test configuration</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;hsqldb</code></td>
 *         <td>Container for Grouper test database</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;signet</code></td>
 *         <td>Container for Signet test database and configuration</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;conf</code></td>
 *         <td>Container for Signet test configuration</td>
 *     </tr>
 *     <tr>
 *         <td><code>&nbsp;&nbsp;&nbsp;&nbsp;hsqldb</code></td>
 *         <td>Container for Signet test database</td>
 *     </tr>
 * </table>
 * </blockquote>
 * </table>
 * <p> 
 * Note that there is a top level conf directory and one under the build directory.
 * The higher level conf directory may contain multiple subdirectories that contain
 * sets of configurations for different test cases or operating systems.
 * There are two such sets in the distribution, testSetU and testSetX.  TestSetU
 * contain the standard set of test cases.  TestSetX is for advanced users only. 
 * </p> 
 * <p> 
 * During the installation process, the testSetU directory will be copied to the conf
 * directory under the build file.  This is because the Ant "install" and "test"
 * targets automatically use the testSetU directory.  For all other targets, 
 * unless you do use the antMaster.properties file to change the default location 
 * of the conf files to be used, then the configuration files that are copied to 
 * the build/conf directory are the ones immediately under the top level conf directory.
 * </p>
 * <p>
 * At the top level of level of the above structure there are several files.  The one of most interest 
 * is the build.xml file used by Ant.  The README.txt file simply refers the reader to this Deployment
 * Guide.  The antMaster.properties file is not required for installation or testing, but may be 
 * useful for anyone adding his or her own set of test case configurations and databases.
 * </p>
 * <h2>The Build File</h2>
 * <p>
 * An Ant build file is provided along with the source code.  It can be used to compile the code and
 * execute either a set of JUnit test cases or to run the command line interface to the application.
 * When running the JUnit test cases it will automatically start the built-in Grouper 
 * and Signet databases, run the test cases, and stop the databases.  When running for normal (non-testing)
 * execution, it is the responsibility of the user to assure that his Grouper and/or Signet
 * databases are running before executing Ldappc.
 * The build file was created primarily for use in development and testing 
 * and is not set up for convenient general use by deployers for purposes
 * other than installation and testing.  However, it's full capabilities may 
 * prove useful to developers who wish to extend the current product.
 * </p>
 * <p>
 * The Ant build file is set up to allow the user to specify configuration settings in 
 * configuration files and to be able to easily switch between different computing 
 * environments and different configurations of Ldappc.  However, for installation
 * and initial testing, most readers can savely skip reading the rest of this section.
 * </p>
 *
 * <h3>Ant configuration Files</h3>
 *
 * <p>
 * Two properties files control what the ant scripts do.  These are
 * referred to as the master ant properties file, which is optional, 
 * and the ant system properties file, which is required for running this 
 * Ant build script for installation and testing only if the user does 
 * not have an environmental variable JAVA_HOME set to the directory 
 * for JDK1.5.  A third properties file, testLdappc.properties, is used for 
 * providing data to the Ldappc testing code.  Most users will never
 * need to use this file.
 * </p>
 * <p>
 * 1)  The master ant properties file. (Optional)
 * </p>
 * <p>
 * You only need to read about the master ant properties file
 * if you are setting up multiple configurations.
 * </p>
 * <p>
 * The default name and location is: antmaster.properties
 * under the installation directory.
 * This may be changed by using -Dmaster=yourMasterPropertiesFile
 * on the ant command line.
 * </p>
 * <p>
 * The master file determines the location of the other configuration files.
 * This allows the master file to easily switch between using an antSystem.properties
 * file that uses different databases or configurations.
 * The antmaster.properties file requires the key: applicationConfigDir.
 * If a single set of data and configuration files is to be used, then
 * the normal setting is: applicationConfigDir=conf.
 * If multiple sets of data and configuration files are to be used, then
 * the normal setting is: applicationConfigDir=conf/yourSubDirectory.
 * While most other properties should be set in the ant system properties,
 * described below, the master ant file can be used to set any properties
 * of the properties that are common to all of the ant system properties files
 * in the directories pointed to by the applicationConfigDir property.
 * </p>
 * <p>
 * 2)  The ant system properties file. (Optional)
 * </p>
 * <p>
 * The ant system properties file contains properties needed by the build
 * script to account for differences is operating systems, test cases, and user
 * preferences.  The default name and location is: antSystem.properties
 * under the conf directory under the top level directory.
 * This may be changed by setting the property applicationConfigDir
 * in the master ant properties file.
 * </p>
 * <p>
 * 3)  The test ldappc properties file. (Optional)
 * </p>
 * <p>
 * The default name and location is testLdappc.properties
 * under the conf directory under the top level directory.
 * The testLdappc.properties file is used for providing data to the Ldappc
 * testing code.  It is also provides an alternative location for the
 * security credentials for the LDAP server.  This would allow one to set
 * different permissions on the file that contains the password and the
 * ldappc.xml file, to which more users may need access.
 * </p>
 * <h3>Build File Details</h3>
 * <p>
 * If one has done an Ant install and then wishes to make changes to update from CVS, where
 * the CVS changes involve only changes to Java class files, then one could recompile the 
 * code using the Ant "compile" target.  However, unless one knows that no changes have been
 * made to the configuration files or certain other non-Java source files, then one should 
 * instead run the Ant script with the "build" target.  It recommended that the "build" or
 * "test" targets usually be used instead of the "compile" target.  Use of the "build" target
 * will assure that the selected configuration files, the ldappcConfig.xsd file, the sources.xml
 * file, and additional test data files are copied to the build directory.
 * </p>
 * <p>
 * To save copying time, the lib directory is not copied to the build directory by 
 * the Ant build script.  There is commented out code in the script to perform 
 * the copy that could be uncommented and other modifications could be made to allow
 * doing the copying of selected jar files based on some new property value.
 * The user may wish to do this if he wishes to maintain different configurations that
 * use different library versions. 
 * </p> 
 * <h2>The Schema Files</h2>
 * </p>
 * <p>
 * Most of the schema files needed by Ldappc will be provided by the LDAP server that the
 * user deploys.  In addition, the user will need to set up the LDAP server to include the 
 * ldappc.schema that is located in the conf directory, as discussed in the 
 * "Prerequisites" section. 
 * </p>
 * <p> 
 * <h2>The sources.xml File</h2>
 * </p>
 * <p>
 * To use Ldappc, a single sources.xml file is required.
 * Therefore, instead of using the separate sources.xml files for
 * Grouper and Signet for the test cases, these had to be combined into
 * a single sources.xml file, such as the one in the test database directory.
 * Production users must also create a single sources.xml file to define the
 * location and SQL commands for all data sources.
 * </p>
 * <p>
 * The Ant build script determines where to get the sources.xml file in the following
 * manner.  If the user does not do anything special, then the sources.xml file is copied 
 * to the build directory from the "applicationConfigDir" directory; this is 
 * the "conf" directory unless set otherwise in the antMaster.xml file.
 * If user wants the script to find the sources.xml file elsewhere, he can set
 * the "sourcesXmlDir" property in the antSystem.properties file to the
 * directory desired.  The file name must be sources.xml, unless the
 * user sets the sourcesXml property to a different file name.
 * For testing, e.g. when using the "ant install" or "ant test" commands,
 * the antSystem file used is in the conf/testConfiguration/testSetU
 * directory and it sets sourcesXmlDir=testDb.
 * </p>
 * <h2>Javadoc Documentation</h2>
 * <p>
 * Unless you are reading a printed copy of this document, you must already
 * have navigated to the Javadoc and do not need read to further in this section. 
 * </p>
 * <p>
 * The distribution file comes with a directory, docs, that contains the generated
 * Javadoc directories.  The Ant docs target can be used to recreate the Javadoc 
 * documentation from the src directory.
 * </p>
 * <p>
 * Within this documentation there are some Ldappc documentation files.
 * These documentation files can be located by placing the
 * appropriate URL in a browser window.  This URL is dependent on
 * on the base location where Ldappc is deployed plus the following 
 * relative path: 
 * doc/javadoc/index.html
 * Once the Javadoc is visible in your browser, navigate to the  
 * edu.internet2.middleware.ldappc.documentation package.
 * Then select the DocumentationRoadmap or any of the other links.
 * The other available topics are: 
 * Archicture, DeploymentGuide, Design, LdapGuide, and UserManual.
 * </p>
 * <h2>Testing</h2>
 * <p>
 * The distribution includes a set of JUnit test cases.  These test cases
 * are dependent on the use of databases built into the deployment package. 
 * The JUnit test cases are set up for use only with the HSQLDB 
 * database management program, whose jar file is included
 * in the distribution.
 * The test data for the built-in Grouper and Signet databases are in located 
 * in the testDb directory. 
 * </p>
 * <p>
 * When running the built-in test cases, the build script
 * will automatically start and stop the databases for Grouper 
 * and Signet.
 * If a complete set of configuration and database files is placed
 * in a directory parallel to the directory that contains the
 * built-in cases, then it is also possible to use the build script
 * to start and stop a different database.  In this case, the directory location
 * must be specified using the gsDataDir parameter in the ant system file.
 * </p>
 * <p>
 * Setting the noDbStop parameter to true in the ant system file will allow
 * the database to remain running after the test cases are run.
 * </p>
 * <p>
 * The following figure shows the structure of the built-in test data, where
 * gsDataDir is parameter in the Ant system properties file whose default
 * value is "testDb".  This directory format is referred as the gsData standard 
 * directory format.
 * </p>
 * <p>
 * <pre> 
 * gsDataDir
 *     grouper
 *         conf
 *             ehcache.xml 
 *             grouper.hibernate.properties
 *             grouper.properties.
 *         hsqldb
 *             grouper.properties.
 *             grouper.script
 *     signet
 *         conf
 *             hibernate.cfg.xml
 *         hsqldb
 *             signet.properties.
 *             signet.script
 * </pre> 
 * </p>
 * <h3>Using the Build File for more than Installation and Testing</h3>
 * <p>
 * It is not necessary to read this section for installation and initial testing.
 * </p>
 * <p>
 * The primary purpose of the build file is for installation and running 
 * the built-in test cases.  However, the build file was used extensively for
 * development may be useful to recipients for some purposes  
 * in addition to installation and running the test
 * cases provided.  In particular, if one wishes to create one's own
 * test cases, one can use the above standard data structure and the
 * build file will start and shutdown the database for the user.
 * On the other hand, the build script is set up only for use with
 * the HSQLDB database.  Oracle users, for instance, would need to make
 * significant changes to the build file that are not discussed here. 
 * </p>
 * <p>
 * To run using one's own databases, some changes are required.
 * The parameter gsDataDir in the ant system properties file defines
 * the directory that contains the Grouper and Signet configuration and
 * database data used for testing.  The default directory is "testDb".
 * This directory is used for the built-in test data, but the gsDataDir
 * value can be set to another directory with different data.
 * It could even be used for production data if the user chooses to place all
 * Grouper and Signet data under this directory in a standard data format.  
 * However, it is anticipated that most users will use their existing Grouper and
 * Signet directory structure to hold their data (except for the sources.xml file).   
 * The gsData standard directory format is required for the built-in test database;
 * but is not required otherwise; however, several
 * additional properties must be set if it is not used.  The configuration 
 * file conf/testSetX/antSystem.properties file shows and example of setting
 * these properties.  If the gsDataDir is used, then
 * this data must be in the standard format for this type data, as shown above.
 * </p>
 * <p>
 * The following describes files that will need to be modified in order to use the 
 * build file on a different port than is built-in.  It also has value as list of
 * items that will require setting if the user develops his own Ldappc environment.
 * </p>
 * <p>
 * The minimum changes that will be required to the files corresponding the files below are listed below.
 * The port 51515 will need to be changed in all the lines below, as will the database names.
 * </p>
 * <p>
 * In ldappc/testDb/grouper/conf/grouper.hibernate.properties:
 * </p>
 * <pre>
 *    hibernate.connection.url = jdbc:hsqldb:hsql://localhost:51515/grouperdb/grouper.hibernate.properties
 * </pre>
 * <p>
 * In the conf directory in file testLdappc.properties:
 * </p>
 * <pre>
 *    dbUrl=jdbc:hsqldb:hsql://localhost:51515/signetdb
 * </pre>
 * <p>
 * In the ldappc/testDb/sources.xml file:
 * </p>
 * <pre>
 *    jdbc:hsqldb:hsql://localhost:51515/grouperdb
 *    jdbc:hsqldb:hsql://localhost:51515/signetdb
 * </pre>
 * <p>
 * Other possible changes to consider are listed below.
 * </p>
 * <p>
 * In ldappc/testDb/grouper/conf/grouper.hibernate.properties:
 * </p>
 * <pre>
 *    hibernate.connection.username = sa
 *    hibernate.connection.password =
 * </pre>
 * <p> 
 * If you want to run the JUnit test cases using OpenLDAP and the built-in database but using
 * a different port, in addition to the instances of port 51515 above needing changed,
 * you will you will need to change the port number in the sqltool.rc file of the 
 * ldappc/testDb directory, and possibly the username and password.
 * </p>
 */

public class DeploymentGuide
{
}
