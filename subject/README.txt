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


The *Subject* API is a technology developed by the Grouper
Project to integrate a java application with a site's existing Identity
Management operations. It enables any type of object whose identity is being
managed - person, group, application, computer, etc - to be presented to that
application without requiring the application to be specifically designed for
particular object types or with knowledge of how those objects are stored and
represented. Those details form the configuration of the Subject API.

The Grouper Wiki has additional information on *Subject*:

    <https://wiki.internet2.edu/confluence/display/GrouperWG/Subject+API>

---

# License

*Subject* is released under *Apache License, Version 2.0*.  See
`LICENSE.txt` for the full text of the license.

---

# Build

    % ant dist

This will create .jar, .tar.gz and .zip files in `dist/`.

---

# Test
  
    % ant test

This will run *Subject*'s unit tests.  Unfortunately they are not very
comprehensive at this time.

---

# API Documentation

    % api javaodc

Generate API javadoc in `doc/api/`.

---

# Clean

    % ant clean

Removes generated build, test, distribution and javadoc files.

---

# Reporting Problems

https://bugs.internet2.edu/jira/browse/GRP

---

# Getting Subject

Grouper download site
