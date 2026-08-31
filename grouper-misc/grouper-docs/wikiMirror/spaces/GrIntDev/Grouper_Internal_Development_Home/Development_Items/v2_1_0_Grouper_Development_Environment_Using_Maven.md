---
title: "v2.1.0 Grouper Development Environment Using Maven"
space: GrIntDev
pageId: 48792882
version: 41
lastUpdated: 2026-07-12T06:45:51.243Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792882/v2.1.0+Grouper+Development+Environment+Using+Maven
---

##### Run this build script

```
[mchyzer@i2mibuild ~]$ buildGrouperPsp.sh 2.3.0 GROUPER_2_3_0 noRelease
```

##### Example PSP build from build server

**NOTE THIS IS REPLACED BY THE ABOVE SCRIPT!!!!!!!!!!!!**

Replace X Y Z's with the build number

make sure java is jdk7 and M2 env vars are set correctly, and make sure you use maven 3.3.3 (or perhaps later?).

```
[mchyzer@i2mibuild mchyzer]$ cd /home/mchyzer/tmp
[mchyzer@i2mibuild tmp]$ mkdir mchyzer_build
[mchyzer@i2mibuild tmp]$ export MAVEN_OPTS=-Xmx1024m
[mchyzer@i2mibuild mchyzer_build]$ cd mchyzer_build/
[mchyzer@i2mibuild mchyzer_build]$ wget https://github.com/Internet2/grouper-psp/archive/2.3.0.zip
[mchyzer@i2mibuild mchyzer_build]$ mv 2.3.0 2.3.0.zip
[mchyzer@i2mibuild mchyzer_build]$ unzip 2.3.0.zip 
[mchyzer@i2mibuild mchyzer_build]$ wget https://github.com/Internet2/grouper/archive/GROUPER_2_3_0.zip
[mchyzer@i2mibuild mchyzer_build]$ mv GROUPER_2_3_0 GROUPER_2_3_0.zip
[mchyzer@i2mibuild mchyzer_build]$ unzip GROUPER_2_3_0.zip 
[mchyzer@i2mibuild mchyzer_build]$ rm *.zip
[mchyzer@i2mibuild mchyzer_build]$ mkdir ldappcng
[mchyzer@i2mibuild mchyzer_build]$ mv grouper-GROUPER_2_3_0/grouper-misc/grouper-shib/ ldappcng/
[mchyzer@i2mibuild mchyzer_build]$ cd grouper-psp-2.3.0
[mchyzer@i2mibuild grouper-psp-2.2.2-rc1]$ mv ../grouper-GROUPER_2_3_0/grouper .
[mchyzer@i2mibuild grouper-psp-2.2.2-rc1]$ mv ../grouper-GROUPER_2_3_0/grouper-parent/ .
[mchyzer@i2mibuild grouper-psp-2.2.2-rc1]$ mv ../grouper-GROUPER_2_3_0/grouper-misc/ .
[mchyzer@i2mibuild grouper-psp-2.2.2-rc1]$ mv ../grouper-GROUPER_2_3_0/subject/ .
[mchyzer@i2mibuild grouper-psp-2.2.2-rc1]$ cp grouper/.classpath.mvn grouper/.classpath
[mchyzer@i2mibuild grouper-psp-2.2.2-rc1]$ cp grouper-misc/grouperClient/.classpath.mvn grouper-misc/grouperClient/.classpath
[mchyzer@i2mibuild grouper-psp-2.2.2-rc1]$ cd grouper 
[mchyzer@i2mibuild grouper]$ ant clean
 
Note: if there are errors about missing jars and maven, download them manually and make the parent dirs and copy the jars
[mchyzer@i2mibuild grouper]$ ant
[mchyzer@i2mibuild grouper]$ cd ../grouper-parent
[mchyzer@i2mibuild grouper-parent]$ /home/mchyzer/software/apache-maven-3.3.3/bin/mvn -Dmaven.wagon.provider.http=httpclient clean deploy -DskipTests -Prelease
[mchyzer@i2mibuild grouper-parent]$ cd ../psp_build/psp-parent/
[mchyzer@i2mibuild psp-parent]$ /home/mchyzer/software/apache-maven-3.3.3/bin/mvn -Dmaven.wagon.provider.http=httpclient clean deploy -DskipTests -Prelease
[mchyzer@i2mibuild psp-parent]$ cd ../psp-distribution-for-grouper/target
[mchyzer@i2mibuild target]$ gzip -d grouper.psp-2.3.0.tar.gz 
[mchyzer@i2mibuild target]$ tar xf grouper.psp-2.3.0.tar 
[mchyzer@i2mibuild target]$ cd grouper.psp-2.3.0/lib/custom/
[mchyzer@i2mibuild custom]$ wget https://github.com/Internet2/grouper-psp/tree/master/psp-parent/lib/openspml2-1.0.jar?raw=true --no-check-certificate
HTTP request sent, awaiting response... 200 OK
Length: 301337 (294K) [application/octet-stream]
Saving to: `openspml2-1.0.jar'
100%[==========================================================================================>] 301,337     1.28M/s   in 0.2s    
2015-09-28 15:54:07 (1.28 MB/s) - `openspml2-1.0.jar' saved [301337/301337]
[mchyzer@i2mibuild grouper.psp-2.2.2]$ cd ..
[mchyzer@i2mibuild target]$ rm grouper.psp-2.3.0.tar
[mchyzer@i2mibuild target]$ tar cf grouper.psp-2.3.0.tar grouper.psp-2.3.0
[mchyzer@i2mibuild target]$ gzip grouper.psp-2.3.0.tar
[mchyzer@i2mibuild target]$ sftp webprod3
sftp> cd /home/htdocs/software.internet2.edu/grouper/release/2.3.0
sftp> put grouper.psp-2.3.0.tar.gz
```

##### Eclipse Development

The Shibboleth development wiki has guidelines for configuring Eclipse [here](https://wiki.shibboleth.net/confluence/display/DEV/Configuring+Eclipse).

These instructions are for Grouper version 2.1.0 or later.

##### Grouper

To build Grouper using Maven and Eclipse :

Check out Grouper projects inside your Eclipse workspace directory. (Only some Grouper projects have been mavenized.)

Trunk :

```
svn co https://svn.internet2.edu/svn/i2mi/trunk/grouper-parent \
 https://svn.internet2.edu/svn/i2mi/trunk/subject \
 https://svn.internet2.edu/svn/i2mi/trunk/grouper-misc/morphString \
 https://svn.internet2.edu/svn/i2mi/trunk/grouper-misc/grouperClient \
 https://svn.internet2.edu/svn/i2mi/trunk/grouper \
 https://svn.internet2.edu/svn/i2mi/trunk/ldappcng/grouper-shib

```

A tag :

```
svn co https://svn.internet2.edu/svn/i2mi/tags/GROUPER_2_1_0/grouper-parent \
 https://svn.internet2.edu/svn/i2mi/tags/GROUPER_2_1_0/subject \
 https://svn.internet2.edu/svn/i2mi/tags/GROUPER_2_1_0/grouper-misc/morphString \
 https://svn.internet2.edu/svn/i2mi/tags/GROUPER_2_1_0/grouper-misc/grouperClient \
 https://svn.internet2.edu/svn/i2mi/tags/GROUPER_2_1_0/grouper \
 https://svn.internet2.edu/svn/i2mi/tags/GROUPER_2_1_0/ldappcng/grouper-shib

```

In the `grouper` and `grouper-client` projects, copy `.classpath.mvn` to `.classpath`.

```
cp grouper/.classpath.mvn grouper/.classpath
cp grouperClient/.classpath.mvn grouperClient/.classpath

```

Create default configuration files by running `ant clean` in the `grouper` project.

```
cd grouper
ant clean

```

In the `grouper` and `grouperClient` projects, make sure that Eclipse targets Java 1.6 in `.settings/org.eclipse.jdt.core.prefs` :

```
org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6
org.eclipse.jdt.core.compiler.compliance=1.6
org.eclipse.jdt.core.compiler.source=1.6

```

In the Eclipse Package Explorer right-click to select `Import` -> `Maven` -> `Existing Maven Projects` and select `grouper-parent` for the root directory.

##### Provisioning Service Provider (PSP)

Check out the psp projects inside your Eclipse workspace directory.

```
svn co https://svn.internet2.edu/svn/i2mi/java-provisioning-provider/trunk/ java-provisioning-provider

```

In the Eclipse Package Explorer right-click to select `Import` -> `Maven` -> `Existing Maven Projects` and select `psp-parent` for the root directory.

##### Maven Central Deployment

To deploy to sonatype repository for release :

```
>export MAVEN_OPTS=-Xmx1024m

>cd grouper-parent
>mvn3 clean deploy -DskipTests -Prelease -Dgpg.passphrase=...

>export MAVEN_OPTS=-Xmx1024m>cd psp-parent
>mvn3 clean deploy -DskipTests -Prelease -Dgpg.passphrase=...

```

Snapshots URL : [https://oss.sonatype.org/content/repositories/snapshots/edu/internet2/middleware](https://oss.sonatype.org/content/repositories/snapshots/edu/internet2/middleware)

Maven itself will upload everything. Once it's uploaded, login to sonatype and follow the instructions here:

[Login to sonatype here](http://oss.sonatype.org)

[https://docs.sonatype.org/display/Repository/Closing+a+Staging+Repository](https://docs.sonatype.org/display/Repository/Closing+a+Staging+Repository)

[https://help.sonatype.com/repomanager2/staging-releases/managing-staging-repositories#ManagingStagingRepositories-ClosinganOpenRepository](https://help.sonatype.com/repomanager2/staging-releases/managing-staging-repositories#ManagingStagingRepositories-ClosinganOpenRepository)

If you get an error about validating signatures, then run this as your user on the build server:

```
[mchyzer@i2mibuild .gnupg]$ gpg --send-keys

```

Then here:

[https://docs.sonatype.org/display/Repository/Releasing+a+Staging+Repository](https://docs.sonatype.org/display/Repository/Releasing+a+Staging+Repository)

[https://help.sonatype.com/repomanager2/staging-releases/managing-staging-repositories#ManagingStagingRepositories-ReleasingaStagingRepository](https://help.sonatype.com/repomanager2/staging-releases/managing-staging-repositories#ManagingStagingRepositories-ReleasingaStagingRepository)

Basically, close the repo, and then after it closed if the report comes back clean, you click "Promote" and that released it.

##### Dependency Management

Dependencies (i.e. third-party jars) should be defined in `grouper-parent/pom.xml`.

If a dependency is used in every child project, then the dependency should be defined under `<dependencies>`.

If a dependency is not used in every child project, then the dependency should be defined under `<dependencyManagement>`.

In child projects, omit the version number, since versions are controlled by the parent project.

##### Setting the Grouper version in the PSP

The Grouper version that the PSP is built against is customizable. Update the `grouper.version` property in `psp-parent/pom.xml`.
