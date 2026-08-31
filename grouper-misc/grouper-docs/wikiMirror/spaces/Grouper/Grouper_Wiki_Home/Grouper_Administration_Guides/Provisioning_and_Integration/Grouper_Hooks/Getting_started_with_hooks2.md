---
title: "Getting started with hooks2"
space: Grouper
pageId: 28547391
version: 18
lastUpdated: 2026-07-01T05:46:52.658Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547391/Getting+started+with+hooks2
---

#### Hooks Example - Assign a UNIX ID to Each New Group

[Hooks Introduction](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545347/Grouper+Hooks)  
[Getting started with hooks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547388/Getting+started+with+hooks)  
[Proof of concept](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548997/Hooks+POC+Proof+of+concept) (A veto hook)

- Assign a Unix id to each new group

Here is another hooks example. This will assign a Unix id to each new group, by a database auto-increment. Also, the attribute is not editable (by non wheel / root)

- Download and unzip [Grouper binary api 1.5](http://www.internet2.edu/grouper/release/1.5.3/grouper.apiBinary-1.5.3.tar.gz) (should work in later version)
- This example is for mysql, make a schema
  ```
  CREATE DATABASE grouper_v1_5;
  CREATE USER 'grouper_v1_5'@'localhost' IDENTIFIED BY '*********';
  grant all on grouper_v1_5.* to 'grouper_v1_5'@'localhost';
  
  
  
  ```
- Configure the grouper.hibernate.properties
  ```
  hibernate.dialect                     = org.hibernate.dialect.MySQL5Dialect
  hibernate.cache.use_query_cache       = true
  hibernate.connection.driver_class     = com.mysql.jdbc.Driver
  hibernate.connection.url              = jdbc:mysql://localhost:3306/grouper_v1_5
  hibernate.connection.username         = grouper_v1_5
  hibernate.connection.password         = **********
  
  
  ```

- Initialize the database
  ```
  C:\temp\grouper.apiBinary-1.5.3\bin> gsh -registry -runscript
  
  ```
- Add a type and an attribute and stem
  ```
  C:\temp\grouper.apiBinary-1.5.3\bin> gsh
  gsh 0% grouperSession = GrouperSession.startRootSession();
  gsh 1% addRootStem("aStem", "aStem");
  gsh 2% typeAdd("posixGroup");
  gsh 3% typeAddAttr("posixGroup", "gidNumber", AccessPrivilege.READ, AccessPrivilege.ADMIN, false);
  
  ```
- Create a table to auto-increment an id (note, this is easier with postgres or oracle with sequences)
  ```
   create table `grouper_v1_5`.`posix_groupids`(
     `gid` bigint NOT NULL AUTO_INCREMENT,
     `groupid` varchar(50) NOT NULL,
     PRIMARY KEY (`gid`)
   );
  
  ```
- Secure the type/attribute in grouper.properties:
  ```
  #before Grouper 1.6, you need to set this to true due to jira GRP-451
  grouperIncludeExclude.requireGroups.use = true
  
  #secure this type/attribute so that non admins cannot edit it once in place
  security.types.posixGroup.wheelOnly = true
  
  
  ```
- Write the hook source, and put it in source/test/GroupAddUosHook.java
  ```
  package test;
  
  import java.util.List;
  
  import org.apache.commons.lang3.StringUtils;
  
  import edu.internet2.middleware.grouper.Group;
  import edu.internet2.middleware.grouper.GroupType;
  import edu.internet2.middleware.grouper.GroupTypeFinder;
  import edu.internet2.middleware.grouper.GrouperSession;
  import edu.internet2.middleware.grouper.exception.GrouperSessionException;
  import edu.internet2.middleware.grouper.hibernate.HibUtils;
  import edu.internet2.middleware.grouper.hibernate.HibernateSession;
  import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
  import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
  import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
  import edu.internet2.middleware.grouper.util.GrouperUtil;
  
  /**
   * add a type after a group insert
   */
  public class GroupAddUosHook extends
      edu.internet2.middleware.grouper.hooks.GroupHooks {
  
    /**
     *
     * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void groupPostInsert(HooksContext hooksContext,
        final HooksGroupBean postInsertBean) {
  
      super.groupPostInsert(hooksContext, postInsertBean);
  
      try {
  
        //since we have security on the type/attribute, we need to do this as root
        GrouperSession.callbackGrouperSession(
            GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
  
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
  
            Group group = postInsertBean.getGroup();
            GroupType posixGroup = GroupTypeFinder.find("posixGroup", true);
            group.addType(posixGroup);
  
            //its possible this is already there (e.g. from import or something)
            //select by list since if not by list it must be there
            List<String> gidList = HibernateSession.bySqlStatic().listSelect(String.class,
                "select gid from posix_groupids where groupid = ?", HibUtils.listObject(group.getId()));
  
            if (GrouperUtil.length(gidList) == 0) {
              HibernateSession.bySqlStatic().executeSql("insert into posix_groupids (groupid) values (?)",
                  HibUtils.listObject(group.getId()));
              gidList = HibernateSession.bySqlStatic().listSelect(String.class,
                  "select gid from posix_groupids where groupid = ?", HibUtils.listObject(group.getId()));
            }
  
            String gid = gidList.get(0);
            if (StringUtils.isBlank(gid)) {
              throw new RuntimeException("Why is gid blank??? " + group);
            }
            group.setAttribute("gidNumber", gid);
  
            return null;
          }
        });
  
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  
  }
  
  
  ```
- Put this build.xml in the base dir:
  ```
   <project name="grouperHooks" default="build" basedir=".">
  
    <path id="theClasspath">
      <fileset dir="lib">
        <include name="**/*.jar"/>
      </fileset>
      <fileset dir="dist">
        <include name="**/*.jar"/>
      </fileset>
      <pathelement  location="classes"/>
    </path>
  
    <target name="build">
  
      <mkdir dir="classes"/>
      <delete dir="classes"/>
      <mkdir dir="classes"/>
  
      <mkdir dir="distHooks"/>
      <delete dir="distHooks"/>
      <mkdir dir="distHooks"/>
  
      <javac srcdir="source" destdir="classes" debug="true"
        source="1.5" target="1.5">
        <classpath  refid="theClasspath"/>
        <include name="**/*.java" />
      </javac>
  
      <jar destfile="distHooks/hooksForGrouper.jar">
        <manifest>
          <attribute name="Built-By"                value="${user.name}"/>
        </manifest>
        <fileset dir="classes">
          <include name="**/*.class"/>
        </fileset>
        <fileset dir="source">
          <include name="**/*"/>
        </fileset>
      </jar>
  
      <copy todir="lib/custom" file="distHooks/hooksforGrouper.jar" />
    </target>
  </project>
  
  ```
- Run ant
  ```
  C:\temp\grouper.apiBinary-1.5.3>ant
  Buildfile: build.xml
  
  build:
     [delete] Deleting directory C:\temp\grouper.apiBinary-1.5.3\classes
      [mkdir] Created dir: C:\temp\grouper.apiBinary-1.5.3\classes
     [delete] Deleting directory C:\temp\grouper.apiBinary-1.5.3\distHooks
      [mkdir] Created dir: C:\temp\grouper.apiBinary-1.5.3\distHooks
      [javac] Compiling 1 source file to C:\temp\grouper.apiBinary-1.5.3\classes
        [jar] Building jar: C:\temp\grouper.apiBinary-1.5.3\distHooks\hooksForGrouper.jar
       [copy] Copying 1 file to C:\temp\grouper.apiBinary-1.5.3\lib\custom
  
  BUILD SUCCESSFUL
  Total time: 1 second
  C:\temp\grouper.apiBinary-1.5.3>
  
  ```
- Register the hook in the grouper.properties
  ```
  hooks.group.class=test.GroupAddUosHook
  
  
  ```
- Try it out in gsh (restart gsh)
  ```
  gsh 0% grouperSession = GrouperSession.startRootSession();
  edu.internet2.middleware.grouper.GrouperSession: c54ae34f1fed4e908cf2731691857745,'GrouperSystem','application'
  gsh 1% stem = new edu.internet2.middleware.grouper.StemSave(grouperSession).assignStemNameToEdit("aStem").assignName("aStem").assignCreateParentStemsIfNotExist(true).save();
  stem: name='aStem' displayName='aStem' uuid='576e26d770584119a1903d7095fab9f2'
  gsh 2% subject = SubjectFinder.findById("test.subject.0", true);
  subject: id='test.subject.0' type='person' source='jdbc' name='my name is test.subject.0'
  gsh 3% stem.grantPriv(subject, NamingPrivilege.CREATE, false);
  true
  gsh 4% GrouperSession.stopQuietly(grouperSession);
  gsh 5% exit
  
  
  Start again to get new grouper session (necessary pre Grouper 1.6)
  
  
  gsh 0% subject = SubjectFinder.findById("test.subject.0", true);
  subject: id='test.subject.0' type='person' source='jdbc' name='my name is test.subject.0'
  gsh 1% grouperSession = GrouperSession.start(subject);
  edu.internet2.middleware.grouper.GrouperSession: f81e665ebe6c41818d5d98120fb940a0,'test.subject.0','person'
  gsh 2% addGroup("aStem", "aGroup9", "aGroup9");
  group: name='aStem:aGroup9' displayName='aStem:aGroup9' uuid='bfac07adf0964722a3226355af09c927'
  gsh 3% groupGetTypes("aStem:aGroup9");
  type: 'base'
  type: 'posixGroup'
  gsh 4% getGroupAttr("aStem:aGroup9", "gidNumber");
  8
  gsh 5% setGroupAttr("aStem:aGroup9", "gidNumber", "7");
  gsh 6% // Error: unable to evaluate command: Sourced file: inline evaluation of: ``setGroupAttr("aStem:aGroup9", "gidNumber", "7"); ;'' : Error invoking compiled command: : Error in compiled command: edu.internet2.middleware.grouper.hooks.logic.HookVeto: cantEditTypeNotInGroup: Not allowed to edit type: posixGroup, changing attribute gidNumber since the user Subject id: test.subject.0, sourceId: jdbc is not in group: null
  
  
  
  ```
-
