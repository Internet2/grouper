====
    Copyright 2012 Internet2

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


# Grouper Extension Framework

!!! THIS IS AN ALPHA IMPLEMENTATION AND INTERFACE !!!
!!! IT MAY CHANGE IN IN AN INCOMPATIBLE WAY IN A FUTURE RELEASE !!!


## Summary

The Grouper Extension Framework allows code external to the Grouper API to be
compiled-and-tested at the same time as the Grouper API.  The goal is to ease integration
of code external to the core API by defining a common build process and means of referring
to the Grouper installation.  A secondary goal is to allow some of the code currently in
the core API - that **isn't** core - to be extracted into external extensions.

This framework was loosely inspired by the [Shibboleth Extension Framework][0].


## Using An Extension

To use an extension, place the extension directory into the "GROUPER_HOME/ext/" directory and 
follow the normal instructions for building the Grouper API.  There are hooks in several
targets in the top-level "GROUPER_HOME/build.xml" file that will also act on any
installed extensions.

When extensions are built they should install any relevant files into the
"GROUPER_HOME/ext/bin/", "GROUPER_HOME/ext/doc/" and "GROUPER_HOME/ext/lib/" directories.
For example, the "gsh 0.1.1" extension installs the following files:

    # DOS and Unix scripts for invoking gsh. 
    GROUPER_HOME/ext/bin/gsh.bat 
    GROUPER_HOME/ext/bin/gsh.sh   

    # Directory containing gsh JavaDoc.
    GROUPER_HOME/ext/doc/gsh/

    # gsh extension and its required libraries.
    GROUPER_HOME/ext/lib/bsh-2.0b4.jar
    GROUPER_HOME/ext/lib/gsh-0.1.1.jar


## Creating An Extension

As a goal is to allow extensions that aren't pure Java (e.g JRuby) the extension
framework works by identifying what look like extension directories within
"GROUPER_HOME/ext/" (i.e.  those that contain a "build.xml" file) and invoking several
different Ant targets within the extension's "build.xml" at various points in the
Grouper API build process.  This means that the extension framework itself does not
enforce good conduct by extensions.  It is instead up to the extensions to do as they see
fit during each stage of the build process and to install the appropriate files.

Ideally the framework itself would have a little more intelligence but that probably won't
happen until an actual need arises.

There are several **required** Ant targets within an extension's "build.xml".  If any of
these are not present the build will fail with a cryptic error message.  Below are the
required targets and where they are called from within the top-level Grouper API "build.xml".

    Grouper API Target    Extension Target    Description
    ==================    ================    ===========
    clean                 ext.clean           Clean the extension as appropriate.
    compile               ext.compile         Build the extension as appropriate.
    compile               ext.install         Should install any files appropriate for 
                                              "GROUPER_HOME/ext/bin/" and "GROUPER_HOME/ext/lib".
    javadoc               ext.doc             Should install any documentation in 
                                              "GROUPER_HOME/ext/doc/".  Ideally the documentation 
                                              would be installed into a subdirectory (named after 
                                              the extension itself) within documentation directory.
    test                  ext.test            Run any relevant tests if appropriate.

The required extension target names all begin with the prefix "ext.".  While
this isn't ideal - and could ideally be overridden on a per extension basis -
it has the advantage of a) it works, b) it is really simple and c) it is
unlikely to come into conflict with any preexisting targets within the
extension's "build.xml".  One option for handling these naming conventions is
to follow the "gsh" approach.  "gsh" defines these targets and has each
perform an "antcall" on the appropriate target within its "build.xml".
Another approach would be to have the "ext." prefixed targets for building
code as an extension and non-prefixed targets for building code outside of the
extension framework.

When each of the extension targets is invoked, several properties will be made
available for the extension to use.  The current list of defined properties is
below:

    PROPERTY                  VALUE
    ========                  =====
    GROUPER_CLASSPATH         project.classpath
    GROUPER_CONF              conf.dir
    GROUPER_DOC_CSS           javadoc.css
    GROUPER_EXT_BIN           ext.dir.bin
    GROUPER_EXT_DOC           ext.dir.doc
    GROUPER_EXT_LIB           ext.dir.lib
    GROUPER_HOME              basedir

More properties can be made available if needed.  See "GROUPER_HOME/build.xml"
and "GROUPER_HOME/buildGrouper.properties" for the values of each property.

Finally, if there is more than one extension being built, one **should not** have
any expectations as to the order in which they will be built.


## Miscellany

### Calling extension Ant targets

You can use the "ext.call" Ant target to call an arbitrary Ant target on an individual
extension.

Example:
    
    % ant ext.call -Dext=your_extension -Dtarget=test
  

## Notes

[0]: <http://viewvc.internet2.edu/viewvc.py/shibboleth/java/custom/README.txt?&view=markup>

