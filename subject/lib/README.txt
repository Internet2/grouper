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
with *Subject*.

---

# bcprov-jdk16-1.46.jar

The Bouncy Castle Crypto package is a Java implementation of cryptographic
algorithms. This jar contains JCE provider and lightweight API for the Bouncy
Castle Cryptography APIs for JDK 1.6.

* Version:  1.46
* Home:     <http://www.bouncycastle.org/java.html>
* Source:   <https://github.com/bcgit/bc-java/>
* License:  <http://www.bouncycastle.org/licence.html>
            Please note this should be read in the same way as the MIT license <http://opensource.org/licenses/MIT>.
* Updated:  2011-02-23
* Use:      base64 encoder in edu.internet2.middleware.subject.provider.PKCS1 (still in use?)


# c3p0-0.9.5.2.jar

a JDBC Connection pooling / Statement caching library

* Version:  0.9.5.2
* Home:     <http://www.mchange.com/projects/c3p0/>
* Source:   <https://github.com/swaldman/c3p0>
* License:  <http://www.gnu.org/licenses/lgpl-2.1.html>
            <http://www.eclipse.org/legal/epl-v10.html>
            This software is made available for use, modification, and
	    redistribution, under the terms of the Lesser GNU Public License,
	    v.2.1 (LGPL) or the Eclipse Public License, v.1.0 (EPL), at your
	    option.
* Updated:  2015-12-09
* Use:      edu.internet2.middleware.subject.provider.JdbcConnectionProvider


# commons-collections-3.2.2.jar

Types that extend and augment the Java Collections Framework.

* Version:  3.2.2
* Home:     <http://commons.apache.org/collections/>
* Source:   <https://git-wip-us.apache.org/repos/asf?p=commons-collections.git>
* License:  <http://www.apache.org/licenses/LICENSE-2.0>
            Apache License, Version 2.0
* Updated:  2015-11-12
* Use:      SingletonIterator <- edu.internet2.middleware.subject.provider.JdbcSubjectAttributeSet


# commons-digester3-3.2.jar

The Apache Commons Digester package lets you configure an XML to Java object
mapping module which triggers certain actions called rules whenever a particular
pattern of nested XML elements is recognized.

* Version:  3.2
* Home:     <http://commons.apache.org/digester/>
* Source:   <http://svn.apache.org/viewvc/commons/proper/digester/trunk/>
* License:  <http://www.apache.org/licenses/LICENSE-2.0>
            Apache License, Version 2.0
* Updated:  2011-12-10
* Use:      edu.internet2.middleware.subject.provider.SourceManager#parseConfig


# commons-jexl-2.1.1.jar

The Commons Jexl library is an implementation of the JSTL Expression Language
with extensions.

* Version:  2.1.1
* Home:     <http://commons.apache.org/jexl/>
* Source:   <http://svn.apache.org/viewvc/commons/proper/jexl/tags/COMMONS_JEXL_2_1_1/>
* License:  <http://www.apache.org/licenses/LICENSE-2.0>
            Apache License, Version 2.0
* Updated:  2011-12-19
* Use:      EL used in edu.internet2.middleware.subject.SubjectUtils


# commons-lang-2.6.jar

Commons Lang, a package of Java utility classes for the classes that are in
java.lang's hierarchy, or are considered to be so standard as to justify
existence in java.lang.

* Version:  2.6
* Home:     <http://commons.apache.org/lang/>
* Source:   <https://git-wip-us.apache.org/repos/asf?p=commons-lang.git;a=log;h=refs/heads/LANG_2_X>
* License:  <http://www.apache.org/licenses/LICENSE-2.0>
            Apache License, Version 2.0
* Updated:  2011-01-16
* Use:      StringUtils is used in many places


# commons-logging-1.2.jar

Apache Commons Logging is a thin adapter allowing configurable bridging to other, well known logging systems.

* Version:  1.2
* Home:     <http://commons.apache.org/proper/commons-logging/>
* Source:   <http://svn.apache.org/repos/asf/commons/proper/logging/trunk/>
* License:  <http://www.apache.org/licenses/LICENSE-2.0>
            Apache License, Version 2.0
* Updated:  2014-07-05
* Use:      General logging


# hsqldb-2.3.5.jar

HSQLDB - Lightweight 100% Java SQL Database Engine

* Version:  1.2
* Home:     <http://hsqldb.org/>
* Source:   <https://git.code.sf.net/p/hsqldb/hsqldb>
* License:  <http://hsqldb.org/web/hsqlLicense.html>
            (based on BSD License)
* Updated:  2017-04-16
* Use:      Testing of the jdbc adapter


# junit-4.12.jar

JUnit is a unit testing framework for Java, created by Erich Gamma and Kent Beck.

* Version:  4.12
* Home:     <http://junit.org/>
* Source:   <https://github.com/junit-team/junit4>
* License:  <https://github.com/junit-team/junit4/blob/master/LICENSE-junit.txt>
            Eclipse Public License 1.0
* Updated:  2014-12-04
* Use:      Unit testing


# mchange-commons-java-0.2.14.jar

No description

* Version:  0.2.14
* Home:     <https://github.com/swaldman/mchange-commons-java>
* Source:   <https://github.com/swaldman/mchange-commons-java>
* License:  <https://github.com/swaldman/mchange-commons-java/blob/master/LICENSE>
            GNU Lesser General Public License (LGPL), version 2.1
              OR
            Eclipse Public License (EPL), version 1.0
* Updated:  2017-11-07
* Use:      Runtime dependency of c3p0


# vt-ldap-3.3.9.jar

Library for performing common LDAP operations

* Version:  3.3.9
* Home:     <http://code.google.com/p/vt-middleware/wiki/vtldap>
* Source:   <https://code.google.com/archive/p/vt-middleware/source>
* License:  <http://www.apache.org/licenses/LICENSE-2.0>
            Apache License, Version 2.0
* Updated:  2015-02-04
* Use:      LdapSourceAdapter
