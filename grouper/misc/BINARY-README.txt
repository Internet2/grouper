Grouper binary distribution:

- There is no build.xml, everything is built
- Follow these instructions to upgrade grouper from v1.6:

https://spaces.internet2.edu/display/Grouper/v2.0+Upgrade+Instructions+from+v1.6.*
https://spaces.internet2.edu/display/Grouper/Grouper+changes+v2.0

- Google for a Grouper quick start document, e.g.

https://spaces.internet2.edu/display/Grouper/Getting+Started+with+Grouper
https://spaces.internet2.edu/display/Grouper/Starting+with+Grouper
https://spaces.internet2.edu/display/Grouper/Grouper+Hosted+on+a+Cloud+Server

- Generally:

1. configure the non-example files in the conf dir
2a. If you are using hsqldb, start your database: 
  [windows]: java -cp lib\jdbcSamples\hsqldb.jar org.hsqldb.Server -database.0 file:grouper -dbname.0 grouper
     [unix]: java -cp lib/jdbcSamples/hsqldb.jar org.hsqldb.Server -database.0 file:grouper -dbname.0 grouper
2b. init your registry: [windows]: bin\gsh -registry -check -runscript
                           [unix]: bin/gsh.sh -registry -check -runscript
3. start gsh: [windows]: bin\gsh
                 [unix]: bin/gsh.sh

https://wiki.internet2.edu/confluence/display/GrouperWG/GrouperShell+(gsh)

4. install the ui quickstart, or web services