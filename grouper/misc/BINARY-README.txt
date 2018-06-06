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
  [windows]: java -cp "lib\jdbcSamples\*" org.hsqldb.Server -database.0 file:grouper -dbname.0 grouper -port 9001
     [unix]: java -cp "lib/jdbcSamples/*" org.hsqldb.Server -database.0 file:grouper -dbname.0 grouper -port 9001
2b. init your registry: [windows]: bin\gsh -registry -check -runscript
                           [unix]: bin/gsh -registry -check -runscript
3. start gsh: [windows]: bin\gsh
                 [unix]: bin/gsh

https://spaces.internet2.edu/pages/viewpage.action?pageId=14517859

4. install the ui quickstart, or web services
