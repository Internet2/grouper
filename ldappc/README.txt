*** REAME.txt File for the Ldappc Installation ***

For those who have either the source or binary distribution, installation
instructions for the program exist in HTML format and can be found by
navigating to the file site/index.html and opening the file in a browser
window.  

This presents you with the main documentation links for Ldappc, plus the
Javadoc.  On this page you can access any of the available documents.  These
documents include architecture, design, LDAP, and deployment guides plus a
user's manual.  You can directly select the "Deployment Guide" link for
detailed documentation about the installation process.

To generate the site documentation, run "mvn site" from the working
directory; this will generate the documentation in the target/site directory.
Then open the target/site/index.html file in a browser window.

To build the program, run "mvn package" from the working directory;
this will compile everything and create a target/ldappc-version.jar file, and
a lib directory containing the required runtime jars. The project can then
be run using the UN*X and Windows runner scripts in the bin directory.

To generate a deployment package, run "mvn package -Pdeploy", which generates
a deploy distribution in the target directory. This contains everything
needed to run ldappc. This is the same as the binary distribution, but does
not contain the documentation.

To generate a distribution that can be run elsewhere, run
"mvn package -Pdistribution", which generates both a binary and a source
distribution in the target directory.  Either can be moved to another
directory and expanded.

The conf directory contains the necessary configuration files, which you must
edit to use Ldappc.  This directory will be included in the distributions,
but the main runner scripts assume you will be running Ldappc in the
development directory.
