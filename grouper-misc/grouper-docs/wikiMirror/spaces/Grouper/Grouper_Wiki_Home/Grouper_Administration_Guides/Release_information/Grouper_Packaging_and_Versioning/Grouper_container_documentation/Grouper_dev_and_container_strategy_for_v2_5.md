---
title: "Grouper dev and container strategy for v2.5"
space: Grouper
pageId: 28554821
version: 15
lastUpdated: 2026-07-12T05:14:34.999Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554821/Grouper+dev+and+container+strategy+for+v2.5
---

See also [Container for Grouper 2.5](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation) for additional details .

## Developer workflow

## Maven central jars

On each commit, snapshots will be committed

On tag, release the jars

## Build container

## Maven tasks

Compile and jar will be done in maven. Jars will be housed in maven central.

Maven is not needed by anyone except the CI/CD or script that maven compiles and stores jars to maven central. devs dont need it. container doesnt need it. plugin devs need to get jars from maven. institutions running grouper dont need maven.

| **Task** | **Description** | **Progress** |
| --- | --- | --- |
| no snapshots |  |  |
| build versions | 1. when certain type of tag, e.g. GROUPER_RELEASE_2.5.8, it would make the real release jars            1. Travis sees tag, pulls down source code   2. Runs mvn versions command to assign version locally (in git in pom the version is 2.4-SNAPSHOT)   3. Compile   4. e.g. grouper-2.5.8.jar   5. Publish to staging branch   6. Manually release it in maven (whoever tags to do this)   7. Automatic ci/cd based on tag format (low priority) 2. publish to maven central automatically (low priority) |  |
| remove jars from git | 1. Remove 3rd party jars from git 2. Add the git dependency command in readme 3. "ant dev" to get jars from dependency jars and copy to webapp/WEB-INF/lib |  |
| build jars | Each project should have    1. normal classfile jar 2. source jar (low priority) 3. test jar (low priority) (if there are test classes) 4. test source jar (low priority) (if there are test classes) 5. javadoc jar? (low priority) |  |
| notification | every commit, compile everything and run tests (low priority)  can there be a notification if build breaks (to core list)? (low priority) |  |
| what needs to change after build? | nothing  Instead of hard coding grouper version we would get that from manifest from jar |  |
| maven grouperParent | 1. should build everything in grouper, using local filesystem builds as dependencies 2. e.g. if something in grouper is dependent on something in grouper client, and that isnt in maven yet, the build should succeed 3. only build from parent (assumption) |  |
| maven grouperClient | get maven working on client jars. ant currently works for this, see build.xml    1. src/ext and src/java should be compiled with no dependencies 2. src/testSource can use all of above and jars 3. low priority, see if shade can easily (not take a lot of time to get working),      manage the "ext" source jars to be put in the same classpath (since used elsewhere) |  |
| maven grouper | get maven making the grouper jars    1. grouper depends on client 2. source dir and test dir, pretty simple |  |
| maven grouper ui | get maven working for ui jars    1. ui depends on grouper (and transitively, client) 2. source dir and test dir, pretty simple |  |
| maven grouper ws | get maven working for ws jars    1. classfile jar has source from: src/grouper-ws, src/grouper-ws_v2_0, ... , src/grouper-ws_v2_4 (not there yet but will be) all the src dirs 2. test jar has source from: src/test, (low priority) grouper-ws-java-generated-client/src?, (low priority) grouper-ws-java-manaual-client/src/java-manual-client?, (low priority) grouper-ws-test/src/test? |  |
| maven grouper scim | this is grouper-ws/grouper-ws-scim, get maven working for jars    1. source and test dir, not too complicated 2. add dependent jars to project |  |
| maven pspng | should be ready to go right? |  |
| maven box connector |  |  |
| maven installer | note, we should just move this to the client, right? |  |
| maven duo connector |  |  |
| maven activemq messaging connector |  |  |
| maven aws messaging connector |  |  |
| maven rabbitmq messaging connector |  |  |
| maven remedy connector |  |  |

## Ant tasks

Ant can be used for various minimal tasks and for packaging up tarballs. Maven is used for compile/jar

| **Ant task** | **Description** | **All projects or just some** |
| --- | --- | --- |
| checkDependencies | see if grouper, grouper client, or whatever dependencies are there | most |
| dev | copy jars to webapp/WEB-INF/lib | webapps |
| makeContainer | make the dir for | grouper api. download jars from maven central with ant-maven plugin (grouper jars and third party jars). make the uber webapp dir that contains everything. should be ready for docker container. assume everything is checked out from git. |
| java2wsdl | take javabeans make ws wsdl | ws |
