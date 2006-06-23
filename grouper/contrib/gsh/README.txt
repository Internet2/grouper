
# README

*GrouperShell* (gsh) is a shell for administering and interacting
with the [Grouper][0] API.  It can be used in both a batch and interactive
manner.

$Id: README.txt,v 1.1 2006-06-23 17:30:09 blair Exp $

---

## Build

Build *Grouper* per release instructions.

Build *GrouperShell*:

    % cd $GSH_HOME
    % ant

## Test

Test *GrouperShell*'s scripting capabilities:

    % ant test

## Bundle

Build `gsh.jar` file in `dist` directory:

    % ant jar

## Documentation

Build javadoc in `doc/html`:

    % ant html

## Use

Run *GrouperShell* in an interactive manner from a Unix-like
environment:

    % ./bin/gsh.sh

Run *GrouperShell* (crudely) from Ant:

    % ant shell

Read *GrouperShell* commands from STDIN:

    % ./bin/gsh.sh - 

Read *GrouperShell* commands from a script file:

    % ./bin/gsh.sh /path/to/your/script.gsh

---

[0]: <http://middleware.internet2.edu/dir/groups/grouper/>

