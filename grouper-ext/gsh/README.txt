
# README

GrouperShell (gsh) is a Grouper API extension.  It provides a shell that may
be used for administering and interacting with the Grouper API.  gsh can be
used in both a batch and interactive manner.

---

gsh has moved from <http://code.google.com/p/blair/> to the Internet2 CVS repository - <http://viewvc.internet2.edu/viewvc.py/grouper-ext/gsh/?root=I2MI>

    *  Access the GrouperShell source code via the web:
          o Connection type: pserver
          o User: anoncvs
          o Passwd: <your email address>
          o Host: anoncvs.internet2.edu
          o Repository Path: /home/cvs/i2mi
          o Use default port: yes
    * Access the complete Grouper source code via the command line:

      cvs -z3 -d :pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi login
      cvs -z3 -d :pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi co grouper-ext/gsh

Please report bugs / feature requests to grouper-users@internet2.edu.



---

## BUILD

See "GROUPER_HOME/ext/README.txt" for information on building, testing and
packaging gsh for use with the Grouper API.

## USE

Run gsh in an interactive manner from a Unix-like
environment:

    % GROUPER_HOME/ext/bin/gsh.sh

Read gsh commands from STDIN:

    % GROUPER_HOME/ext/bin/gsh.sh -

Read gsh commands from a script file:

    % GROUPER_HOME/ext/bin/gsh.sh /path/to/your/script.gsh

---

$Id: README.txt,v 1.2 2008-04-29 15:15:41 isgwb Exp $

