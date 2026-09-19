====
    Copyright 2014 Internet2.

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

Grouper binary distribution

- There is no build.xml, everything is built
- Follow these instructions to upgrade grouper from v1.6:

https://docs.grouper.internet2.edu/wiki/spaces/Grouper/overview
https://docs.grouper.internet2.edu/wiki/spaces/Grouper/overview

- Google for a Grouper quick start document, e.g.

https://docs.grouper.internet2.edu/wiki/spaces/Grouper/overview
https://docs.grouper.internet2.edu/wiki/spaces/Grouper/overview
https://docs.grouper.internet2.edu/wiki/spaces/Grouper/overview

- Generally:

1. configure the non-example files in the conf dir
2a. Start your database: 
2b. init your registry: [windows]: bin\gsh -registry -check -runscript
                           [unix]: bin/gsh -registry -check -runscript
3. start gsh: [windows]: bin\gsh
                 [unix]: bin/gsh

https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh

4. install the ui quickstart, or web services
