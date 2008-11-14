Grouper binary distribution:

- There is no build.xml, everything is built
- Follow these instructions to upgrade grouper from v1.3:

https://wiki.internet2.edu/confluence/display/GrouperWG/Grouper+change+log+v1.3

- Follow these instructions to get started with 1.4 from scratch:

https://wiki.internet2.edu/confluence/display/GrouperWG/v1.4.0+Getting+Started

- Generally:

1. configure the non-example files in the conf dir
2. init your registry: bin/gsh -registry -check -runscript
3. start gsh: bin/gsh

https://wiki.internet2.edu/confluence/display/GrouperWG/GrouperShell+(gsh)

4. install the ui quickstart, or web services