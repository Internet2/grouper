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

Grouper client binary distribution:

- There is no build.xml, everything is built
- Here is information on grouperClient:

https://wiki.internet2.edu/confluence/display/GrouperWG/Grouper+Client

- Generally:

1. configure the non-example files: grouper.client.properties and grouper.client.usage.txt
2. make sure you have Java 1.6+ (same as Java 6+)
3. run and get the usage: java -jar grouperClient.jar
4. if you are running LDAP or WS, try one of those commands from usage
