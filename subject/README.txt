
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
