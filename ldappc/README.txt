*** REAME.txt File for the Ldappc Installation ***

For those who have either the source or binary distribution, installation
instructions for the program exist in HTML format and can be found by
navigating to the file doc/index.html and opening the file in a browser
window.  

This presents you with the main documentation links for Ldappc, plus the
Javadoc.  On this page you can access any of the available documents.  These
documents include architecture, design, LDAP, and deployment guides plus a
user's manual.  You can directly select the "Deployment Guide" link for
detailed documentation about the installation process.

For those who are accessing the program using CVS you must first install
the dependencies in the lib directory into your local Maven repository.
Instructions for doing this are in the lib/README.txt file.

To generate the site documentation, run "mvn site" from the working
directory; this will generate the documentation in the doc directory.
Then open the doc/index.html file in a browser window.

To build the program, run "mvn package" from the working directory;
this will generate both a binary and a source distribution in the target
directory.  These can be moved to another directory and expanded.
