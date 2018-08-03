====
    Copyright 2014 Internet2

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

# Grouper README

This file is intended to give you a brief overview of how this directory tree
is structured so that you may get started with compiling, testing and using
Grouper. Please see the [Grouper Wiki][0] for more detailed documentation.

## Contents

1.  Reporting Problems
2.  Project Layout
3.  Building Grouper*
4.  Initializing The Database
5.  Testing Grouper
6.  Installing Grouper*
7.  Using Grouper
8.  Building API Documentation*
9.  Getting Grouper

*From v1.4.0 Grouper also has a pre-built binary release. If you are working with 
the binary release the sections above marked * are redundant.


## 1. Reporting Problems

Please send email to the [grouper-dev][1] list or report them in the [issue tracker][2].


## 2. Project Layout

* "GROUPER_HOME"
  Top-level of this Grouper directory tree.
  * "api"
     Contains Grouper Javadoc (binary distribution only)
  * "bin"
    * "gsh.bat / gsh"
	   Command line and interactive shell interface to Grouper. Run with -h
	   option for help and see Wiki[0] for detailed information
    * "setenv.example.bat / setenv.example.sh"
	   Copy and remove '.example' from file name and uncomment and modify
	   environment variables to control how Java is invoked
  * "build"
    Destination for all compiled Java classes
    * "conf"
      Grouper configuration files. These are provided as example files
	  and are copied to the file names below by the build process.
      * "ehcache.xml"
        Hibernate ehcache configuration.
      * "grouper.hibernate.properties"
        Grouper's Hibernate configuration file.  
      * "grouper.properties"
        Grouper configuration.
      * "grouper-loader.properties.properties"
        Loader configuration.  
     * "log4j.properties"
        Logging configuration.  
	  * "morphString.properties"
	     Controls encryption of passwords in config files
      * "sources.xml"
        Subject API resolver configuration.
  * "dist"
    * "api"
      Destination for generated JavaDoc.
    * "lib"
      Destination for compiled jar files.
    * "run"
      HSQLDB data directory.
  * "ext"
    Top-level directory for Grouper API extensions.
	Currently no extensions are provided. Earlier extensions, such as gsh,
	are now provided as a core part of the Grouper API.
  * "lib"
    Third-party jar files included with Grouper are partitioned as described
	below.
    * "README.txt"
      Information on all of the jar files in the "GROUPER_HOME/lib" directory.
    * "ant"
       * "ant-contrib.jar"
          Used by the Grouper Ant build script (build.xml)
    * "custom"
       Place jar files for your JDBC driver, custom Source adapters etc here
	   to make them available to Grouper
    * "grouper"
       Third-party jar files included with Grouper and required at run time
   * "jdbcSamples"
       Third-party JDBC drivers for databases known to work with Grouper.
	   Copy the appropriate jar file for your database to the "custom" 
	   directory
    * "test"
       * "junit.jar"
	      Required to run the Grouper unit tests
  * "src" (not provided with the binary release)
    * "grouper"
      Grouper API source.
    * "test"
      Grouper API test source.
	  
When you use Grouper, it will create a "ddlScripts" directory, where any 
schema and data SQL manipulation scripts are generated.

By default Grouper will also create a "logs" directory, however, you can
configure, by editing log4j.properties, where you want log files to be
written.


## 3. Building Grouper (not necessary if using the binary release)

    % ant dist

This will compile the Grouper API source, generate several configuration
files, and create "GROUPER_HOME/dist/lib/grouper.jar"

## 4.a Starting The Database

If you are using the built-in hsqldb database to get started, you need to
start the hsqldb server (since hsqldb in file mode does not work well).

Run from the GROUPER_HOME directory:

    % java -cp lib/jdbcSamples/* org.hsqldb.Server -port 9001 -database.0 file:grouperHSQL -dbname.0 grouper

Note, you either need to run that command in the background, or use a terminal window that will wait for the server to exit.

If you want to start the hsql database manager, run this command in a new terminal:
 
    % java -cp lib/jdbcSamples/* org.hsqldb.util.DatabaseManager -url jdbc:hsqldb:hsql://localhost:9001/grouper
    

## 4.b Initializing The Database

    % bin/gsh.sh -registry -runscript

This generates a DDL appropriate for the database configured in
"GROUPER_HOME/conf/grouper.hibernate.properties" and applies it to the
database.  It also initializes default data in some tables (e.g. the root stem in grouper_stems)

To make sure it ran completely, run:

    % bin/gsh.sh -registry -check

You will see output: NOTE: database table/object structure (ddl) is up to date

## 5. Testing Grouper

    % bin/gsh.sh -test -all

This adds some test subjects to the Groups Registry and then runs the
Grouper test suite.  This is a *destructive* action and will destroy
any data within the configured database.

A "JDBCSourceAdapter" (id=jdbc) for subjects must be configured in
"GROUPER_HOME/conf/sources.xml" for the test suite to complete successfully.


## 6. Using Grouper

Review configuration files first.

Starting with v1.4.0 a number of Grouper utilities/extensions are now invoked
using bin/gsh.sh (or bin\gsh.bat) rather than Ant. Type:
  bin/gsh.sh -h
for more information, including how to add "gsh" to your path.

You can bootstrap arbitrary code which uses the Grouper API
by placing your code in a jar file (along with any other jar files 
not provided with Grouper)  in the "GROUPER_HOME/lib/custom" 
directory and invoking gsh i.e
  bin/gsh.sh -main <full class name> [args...]

otherwise you need to have the following items in your CLASSPATH to use Grouper:
* "GROUPER_HOME/dist/lib/grouper.jar" or the "GROUPER_HOME/build/grouper"
  directory.
* all of the jar files in the
  "GROUPER_HOME/lib/grouper" directory.
* any jar files (including your JDBC database driver) in the
  "GROUPER_HOME/lib/custom" directory.
* The "GROUPER_HOME/conf" directory.


## 7. Building API Documentation (not necessary if using the binary release)

  % ant javadoc

This will build the javadoc(1) document.


## 8. Getting Grouper

Grouper releases are available from Grouper's Internet2 Wiki:
  <https://spaces.internet2.edu/display/Grouper/Software+Download>

Grouper source code repository is hosted at Github: 
  <https://github.com/Internet2/grouper>

Grouper documentation is hosted at the Internet2 wiki:
  <https://spaces.internet2.edu/display/Grouper/Grouper+Wiki+Home>
 

## Notes

[0]: <https://spaces.internet2.edu/display/Grouper>
[1]: <mailto:grouper-dev@internet2.edu>
[2]: <https://bugs.internet2.edu/jira/browse/GRP>

