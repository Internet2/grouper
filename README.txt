
# Grouper README

This file is intended to give you a brief overview of how this directory tree
is structured so that you may get started with compiling, testing and using
Grouper.  Please see the [Grouper Wiki][0] for more detailed documentation.

## Contents

1.  Reporting Problems
2.  Project Layout
3.  Building Grouper
4.  Initializing The Database
5.  Testing Grouper
6.  Installing Grouper
7.  Using Grouper
8.  Building API Documentation
9.  Getting Grouper


## Reporting Problems

Please send email to the [grouper-dev][1] list.


## Project Layout

* "GROUPER_HOME"
  Top-level of this Grouper directory tree.
  * "build"
    Destination for all compiled Java classes and generated DDL.
    * "conf"
      Grouper configuration files.
      * "ehcache.xml"
        Hibernate ehcache configuration.
      * "grouper.hibernate.properties"
        Grouper's Hibernate configuration file.  
        NOTE: This file is generated during the build process.
      * "grouper.properties"
        Grouper configuration.
      * "log4j.properties"
        Logging configuration.  
        NOTE: This file is generated during the build process.
      * "sources.xml"
        Subject API resolver configuration.
        NOTE: This file is generated during the build process.
  * "dist"
    * "api"
      Destination for generated JavaDoc.
    * "lib"
      Destination for compiled jar files.
    * "run"
      HSQLDB data directory.
  * "doc"
    * "API.txt"
      API class and method summary.
    * "KNOWN_ISSUES.txt"
      Known issues with the Grouper API and Groups Registry.
    * "NEWS.txt"    
      Release notes.
    * "ROADMAP.txt"
      Development roadmap for future Grouper releases.
    * "TODO.txt"
      A more detailed list of work that needs to be done.
  * "lib"
    Third-party jar files included with Grouper.
    * "README.txt"
      Information on all of the jar files in the "GROUPER_HOME/lib" directory.    
  * "src"
    * "conf"
      Contains configuration files that are to be filtered by Ant during the
      build process.
    * "grouper"
      Grouper API source.
    * "test"
      Grouper API test source.


## Building Grouper

    % ant build

This will compile the Grouper API source and generate several configuration
files.


## Initializing The Database

    % ant schemaexport

This generates a DDL appropriate for the database configured in
"GROUPER_HOME/conf/grouper.hibernate.properties" and applies it to the
database.

    % ant db-init

This initializes the Groups Registry with Grouper's default schema and
installs the root stem


## Testing Grouper

    % ant test

This adds some test subjects to the Groups Registry and then runs the
Grouper test suite.  This is a *destructive* action and will destroy
any data within the configured database.

A "JDBCSourceAdapter" for subjects must be configured in
"GROUPER_HOME/conf/sources.xml" for the test suite to complete successfully.


## Installing Grouper

These are optional tasks that may make runtime invocation easier.

  % ant dist 

Builds "GROUPER_HOME/dist/lib/grouper.jar" from the compiled Grouper source.

  % ant dist.lib

Builds "GROUPER_HOME/dist/lib/grouper-lib.jar".  This jar will contain all of
the files included within the jar files in the "GROUPER_HOME/lib" directory.


## Using Grouper

You need to have the following items in your CLASSPATH to use Grouper:
* "GROUPER_HOME/dist/lib/grouper.jar" or the "GROUPER_HOME/build/grouper"
  directory.
* "GROUPER_HOME/dist/lib/grouper-lib.jar" or all of the jar files in the
  "GROUPER_HOME/lib" directory.
* The "GROUPER_HOME/conf" directory.


## Building API Documentation

  % ant javadoc

This will build the javadoc(1) document.


## Getting Grouper

Grouper releases are available from Grouper's Internet2 Middleware page:
  <http://middleware.internet2.edu/dir/groups/grouper/#software>

Read-only anonymous access to the Grouper CVS repository is now
available.  To perform a CVS checkout, run the following commands.
When prompted for a password, hit enter.

    % cvs -z3 -d :pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi login
    % cvs -z3 -d :pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi co grouper


## Notes

[0]: <https://wiki.internet2.edu/confluence/display/GrouperWG/Home>
[1]: <ailto:grouper-dev@internet2.edu>

