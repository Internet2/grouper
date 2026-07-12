---
title: "Grouper Development Environment Using Maven"
space: GrIntDev
pageId: 48792889
version: 11
lastUpdated: 2016-06-01T15:26:06.881Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792889/Grouper+Development+Environment+Using+Maven
---

##### Eclipse Development

To build grouper as of 2.0.0-SNAPSHOT using maven :

1. Check out projects :

```
svn co https://svn.internet2.edu/svn/i2mi/trunk/grouper-parent \
 https://svn.internet2.edu/svn/i2mi/trunk/subject \
 https://svn.internet2.edu/svn/i2mi/trunk/grouper-misc/morphString \
 https://svn.internet2.edu/svn/i2mi/trunk/grouper-misc/grouperClient \
 https://svn.internet2.edu/svn/i2mi/trunk/grouper \
 https://svn.internet2.edu/svn/i2mi/trunk/ldappcng/grouper-shib \
 https://svn.internet2.edu/svn/i2mi/trunk/ldappcng/ldappcng

```

2. In the `grouper` and `grouper-client` projects, copy `.classpath.mvn` to `.classpath`.

3. Import (right-click in Package Explorer or Navigator view) -> Maven -> Existing Maven Projects.

4. Browse to `grouper-parent` folder.

5. Click finish.

##### Deploy to Central Repository

To deploy to sonatype repository for release :

```
>cd grouper-parent
>mvn3 clean deploy -DskipTests -Prelease -Dgpg.passphrase=...

```

##### Dependency Management

Dependencies (i.e. third-party jars) should be defined in `grouper-parent/pom.xml`.

If a dependency is used in every child project, then the dependency should be defined under `<dependencies>`.

If a dependency is not used in every child project, then the dependency should be defined under `<dependencyManagement>`.

In child projects, omit the version number, since versions are controlled by the parent project.
