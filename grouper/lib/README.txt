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

This file provides information on the third party libraries that ship
with *Grouper*.

---

# antlr-2.7.6.jar

"ANTLR, ANother Tool for Language Recognition, (formerly PCCTS) is a language tool that
provides a framework for constructing recognizers, compilers, and translators from
grammatical descriptions containing Java, C#, C++, or Python actions."

* Version:  2.7.6
* Source:   <http://www.antlr2.org/>
* License:  Public Domain
* Updated:  20070830
* Use:      Runtime requirement of Hibernate3

# commons-discovery-0.4.jar

"The Discovery component is about discovering, or finding, implementations for pluggable
interfaces."  This library is used by FindBugs.

* Version:  0.4
* Source:   <http://jakarta.apache.org/commons/discovery/>
* License:  Apache License, Version 2.0
* Updated:  20070316

# commons-lang-2.0.jar

"The standard Java libraries fail to provide enough methods for
manipulation of its core classes. The  Lang  Component provides
these extra methods."

* Version:  2.0
* Source:   <http://jakarta.apache.org/commons/>
* License:  Apache License, Version 2.0
* Updated:  ?

# commons-math-1.1.jar

" Commons Math is a library of lightweight, self-contained mathematics and
statistics components addressing the most common problems not available in the
Java programming language or Commons Lang."

Grouper uses this for benchmarking.

* Version:  1.1
* Source:   <http://jakarta.apache.org/commons/math/>
* License:  Apache License, Version 2.0
* Updated:  20060907

# ehcache-1.3.0.jar

"EHCache is a pure Java, in-process cache"

* Version:  1.3.0
* Source:   <http://ehcache.sourceforge.net/>
* License:  Apache Software License, Version 1.1
* Updated:  20070807

# hibernate2-2.1.8.jar

"Hibernate is a powerful, ultra-high performance object/relational
persistence and query service for Java."

* Version:  2.1.8
* Source:   <http://www.hibernate.org/>
* License:  LGPL
* Updated:  ?

# hibernate-3.2.5.jar

"Hibernate is a powerful, ultra-high performance object/relational persistence and query service for Java."

* Version:  3.2.5
* Source:   <http://www.hibernate.org/>
* License:  LGPL
* Updated:  20070828

# hsqldb.jar

"Lightweight 100% Java SQL Database Engine"

* Version:  1.8.0.10
* Source:   <http://hsqldb.sourceforge.net/>
* License:  Hypersonic License
* Updated:  2008/08/26

# i2mi-common-0.1.0.jar

"i2mi-common is container for shared resources across Internet2 Middleware
projects."

Contains:
* cglib-nodep-2.1_03.jar
* commons-beanutils-1.7.0.jar
* commons-collections-3.2.jar
* commons-dbcp-1.2.1.jar
* commons-digester-1.7.jar
* commons-logging-1.1.jar
* commons-pool-1.3.jar
* dom4j-1.6.1.jar
* junit-4.1.jar
* odmg-3.0.jar

* Version:  0.1.0
* Source:   <http://viewvc.internet2.edu/viewvc.py/i2mi-common/?root=I2MI>
* License:  Apache License, Version 2.0
* Updated:  20060807

# jta-1.0.1B.jar

"JTA specifies standard Java interfaces between a transaction manager
and the parties involved in a distributed transaction system: the
resource manager, the application server, and the transactional
applications.

* Version:  1.0.1B
* Source:   <http://java.sun.com/products/jta/>
* License:  Sun Microsystems, Inc. Binary License Code Agreement
* Updated:  ?

# jug-1.1.1.jar

"JUG is a pure java UUID generator, that can be used either as a
component in a bigger application, or as a standalone command line
tool (a la 'uuidgen')."

* Version:  1.1.1
* Source:   <http://www.doomdark.org/doomdark/proj/jug/>
* License:  LGPL
* Updated:  ?

# log4j-1.2.8.jar

"The Logging Services project is intended to provide cross-language
logging services for purposes of application debugging and auditing."

* Version:  1.2.8
* Source:   <http://logging.apache.org/>
* License:  Apache License, Version 2.0
* Updated:  ?

# subject.jar

"The I2MI Subject Interface."

* Version:  0.4.4
* Source:   <http://anoncvs.internet2.edu/cgi-bin/viewcvs.cgi/subject/?cvsroot=I2MI>
* License:  Apache License, Version 2.0 (?)
* Updated:  20070315

# jsr107cache-1.0.jar

"Some cache library that ehcache uses sometimes like on solaris"

* Version: 1.0
* Source: http://repo1.maven.org/maven2/net/sf/jsr107cache/jsr107cache/1.0/jsr107cache-1.0.jar
* License: Probably the same as ehcache
* Updated 2008/04/23

# invoker.jar

"reduces the complexity of classpath management."

* Version: 1.0
* Source: http://www.cs.put.poznan.pl/dweiss/xml/projects/invoker/index.xml?lang=en
* License: Public Domain
* Updated 2008/05/08

---

# mailapi.jar

can send mail, e.g. for log4j

* License: Public Domain
* Updated 2008/05/08

---

# activation.jar

can send mail, e.g. for log4j

* License: Public Domain
* Updated 2008/05/08

---

# smtp.jar

can send mail, e.g. for log4j

* License: Public Domain
* Updated 2008/05/08

---

# quartz-1.6.0.jar

can schedule tasks or jobs (e.g. loader)

* License: Public Domain
* Updated 2008/07/21

---

# commons-cli-1.1.jar

processes command line args (e.g. for usdu)

* License: Public Domain
* Updated 2008/07/21

---

# ant-1.7.1.jar

runs the sql from the new ddl strategy

* License: Public Domain
* Updated 2008/07/27

---

# morphString.jar

encrypts strings for passwords, and looks up the values from files

* License: Internet2
* Updated 2008/09/14

---

# c3p0-0.9.1.2.jar

db pooling

* License: LGPL
* Updated 2008/02/17

---
$Id: README.txt,v 1.21 2008-11-24 18:39:44 mchyzer Exp $

