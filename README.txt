
# Grouper README

Welcome to _Grouper_.  

This file is intended to give you a brief overview of how this
directory tree is structured and how to get started with compiling,
testing and running Grouper.

The [Grouper Wiki](https://wiki.internet2.edu/confluence/display/GrouperWG/Home)
has additional documentation.

---

## Reporting Problems

Please send email to [grouper-dev](mailto:grouper-dev@internet2.edu).

---

## Project Layout

See `PROJECT_LAYOUT` for more information.

---

## Building

    % ant build

Build Grouper from source.

---

## Initializing Database

    % ant schemaexport

This creates a DDL appropriate for the database you are using and
applies it to the database

    % ant db-init

This initializes the Groups Registry with Grouper's default schema and
installs the root stem.

---

## Testing

    % ant test

This adds some test subjects to the Groups Registry and then runs the
Grouper test suite.  This is a *destructive* action and will destroy
any data within the configured database.

A *JDBCSourceAdapter* for subjects must be configured for the test suite to
complete successfully.

---

## Installing

These are optional tasks that may make runtime invocation easier.

  % ant dist 

Builds a `grouper.jar` file in the `dist/lib` directory.

  % ant dist-lib

Builds a `grouper-lib.jar` file in the `dist/lib` directory.  This is a
rollup of all the third party .jar files that Grouper relies upon that
are located in `java/lib`.

--

## Using

You may now try using any of the sample contributed programs (located
in `contrib/`) or your own Grouper code.  You will need to add the
following to your _$CLASSPATH_ to use Grouper:

* Full pathname of `grouper.jar` OR the the full pathanme of the 
  `build/grouper` directory
* Full pathname of `grouper-lib.jar` OR the full pathname of all of 
  the .jar files in `java/lib`
* Full pathname of the Grouper configuration directory

---

## Building Documentation

To build the javadoc(1) documentation:

  % ant html

Please see the [Grouper Wiki](https://wiki.internet2.edu/confluence/display/GrouperWG/Home)
for additional documentation.

---

## Getting Grouper

### Releases

Grouper releases are available from Grouper's Internet2 Middleware page:
  <http://middleware.internet2.edu/dir/groups/grouper/#software>

### Anonymous CVS

Read-only anonymous access to the Grouper CVS repository is now
available.  To perform a CVS checkout, run the following commands.
When prompted for a password, hit enter.

  % cvs -z3 -d :pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi login
  % cvs -z3 -d :pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi co grouper

---

$Id: README.txt,v 1.2 2006-07-20 15:02:06 blair Exp $

