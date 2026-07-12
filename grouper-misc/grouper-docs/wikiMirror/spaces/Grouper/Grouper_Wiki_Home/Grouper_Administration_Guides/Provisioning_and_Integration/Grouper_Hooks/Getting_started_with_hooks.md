---
title: "Getting started with hooks"
space: Grouper
pageId: 28547388
version: 12
lastUpdated: 2026-07-01T05:46:56.302Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547388/Getting+started+with+hooks
---

[Hooks Introduction](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545347/Grouper+Hooks)  
 [Proof of concept](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548997/Hooks+POC+Proof+of+concept) (A veto hook)  
 [Hooks Example](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547391/Getting+started+with+hooks2) - Assign a Unix id to each new group

 Instructions to get started with Grouper and hooks.

 

- Install postgres, add a grouper user with a pass.
- Download the latest grouper 1.4 branch 
  
  - cvs -d:pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi login
  - cvs -d:pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi export -r GROUPER_1_4_BRANCH grouper
  - cvs -d:pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi export -r GROUPER_UI_1_4_BRANCH grouper-ui
- In grouper dir, run: ant dist
- Change these settings in conf/grouper.hibernate.properties 
  
  - hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect
  - hibernate.connection.driver_class =org.postgresql.Driver
  - hibernate.connection.url = jdbc:postgresql://localhost:5433/grouper
  - hibernate.connection.username = grouper
  - hibernate.connection.password = whateverYouSet
- Copy the grouper_home/lib/jdbcSamples/postgresql.jar to grouper_home/lib/custom 
  
  - cp lib/jdbcSamples/postgresql.jar lib/custom
- Change this in conf/grouper.properties so ddl can be done:  
   db.change.allow.user.1=grouper  
   db.change.allow.url.1=jdbc:postgresql://localhost:5432/grouper
- Add tables: bin/gsh.sh -registry -runscript 
  
  - You should now be able to browse the DB with pgAdmin or whatever tool you use to admin the db
- Check tables: bin/gsh.sh -registry -check 
  
  - Should output: NOTE: database table/object structure (ddl) is up to date
- Start gsh and add a subject: bin/gsh.sh  
   gsh 0% addSubject("mchyzer", "person", "Chris Hyzer")  
   gsh 1% exit
- In grouper.properties, I will change/add these settings:  
   groups.wheel.use = true  
   groups.wheel.group = etc:sysadmingroup  
   configuration.autocreate.group.name.0 = etc:sysadmingroup  
   configuration.autocreate.group.description.0 = super users  
   configuration.autocreate.group.subjects.0 = mchyzer
- Start gsh again: bin/gsh.sh see if the user is in the groups. Make a stem. Add a type, and an attribute  
   gsh 0% ***grouperSession = GrouperSession.startRootSession();***  
   edu.internet2.middleware.grouper.GrouperSession: f802d876-b876-4315-b76e-0586bcc561b1,'GrouperSystem','application'  
   gsh 1% ***subject = findSubject("mchyzer");***  
   subject: id='mchyzer' type='person' source='jdbc' name='Chris Hyzer'  
   gsh 2% ***member = MemberFinder.findBySubject(grouperSession, subject);***  
   member: id='mchyzer' type='person' source='jdbc' uuid='1324c75e-9435-4c45-97e9-af40f2b71046'  
   gsh 3% ***member.getGroups();***  
   group: name='etc:sysadmingroup' displayName='etc:sysadmingroup' uuid='e8cf5974-97ea-4865-9ac9-719fe3a13134'  
   gsh 4% **addRootStem("aStem", "aStem");**  
   stem: name='aStem' displayName='aStem' uuid='3e1c5e6e-6dd4-43f0-8b6c-20cb39f01ac8'  
   gsh 5% **typeAdd("fubGroup");**  
   type: 'fubGroup'  
   gsh 5% **typeAddAttr("fubGroup", "gid", AccessPrivilege.READ, AccessPrivilege.ADMIN, false);**  
   attribute: 'gid'
- Lets add the hook and try in gsh. Just add in grouper source tree for simplicity sake

 
```
package test;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;

/**
 * add a type after a group insert
 */
public class GroupAddFubHook extends
    edu.internet2.middleware.grouper.hooks.GroupHooks {

  /**
   *
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void groupPostInsert(HooksContext hooksContext,
      HooksGroupBean postInsertBean) {

    super.groupPostInsert(hooksContext, postInsertBean);

    try {
      Group group = postInsertBean.getGroup();
      GroupType fubGroup = GroupTypeFinder.find("fubGroup");
      group.addType(fubGroup);
      group.setAttribute("gid", "2");
      group.store();

    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}

```

 [mchyzer@flash2 grouper]$ ant dist

 

- Add this line to the hooks section in conf/grouper.properties  
   hooks.group.class=test.GroupAddFubHook
- See hook work, see new type and new attribute: bin\gsh.sh

 
```
gsh 0% group = addGroup("aStem", "aGroup5", "aGroup5");
group: name='aStem:aGroup5' displayName='aStem:aGroup5' uuid='b5552545-2ad2-462c-b5df-67586c987992'
gsh 1% group.getTypes();
type: 'base'
type: 'fubGroup'
gsh 2% group.getAttributes();
java.util.HashMap: {extension=aGroup5, displayExtension=aGroup5, gid=2, name=aStem:aGroup5, displayName=aStem:aGroup5}
gsh 3%

```

 *

 

#### Grouper UI

 

- Download or unzip grouper-ui
- Run ant - exit
- Edit the build.properties, 
  
  - set the grouper.folder if not ../grouper
- Run ant - dist
- Edit your tomcat_home/conf/server.xml, add a context for the UI  
   <Engine defaultHost="localhost" name="Catalina">  
   <Realm className="org.apache.catalina.realm.UserDatabaseRealm" resourceName="UserDatabase"/>  
   <Host appBase="webapps" autoDeploy="true" name="localhost" unpackWARs="true" xmlNamespaceAware="false" xmlValidation="false">  
   ***<Context docBase="/home/mchyzer/grouper_v1_4/grouper-ui/dist/grouper" path="/grouper" reloadable="false"/>***  
   </Host>  
   </Engine>
- Add a user to your tomcat-users.xml file:

 
```
<tomcat-users>
  <role rolename="grouper_user"/>
  <user username="mchyzer" password="whateveryouwant" roles="grouper_user"/>
</tomcat-users>

```

 

- 
- If you are using mod_jk, hook up the url with the tomcat:  
   JkMount /grouper/* tomcat_mchyzer
- Stop and start apache and tomcat:  
   [mchyzer@flash2 grouper]$ /home/mchyzer/apache2_0/bin/apachectl stop  
   [mchyzer@flash2 grouper]$ /home/mchyzer/apache2_0/bin/apachectl start  
   [mchyzer@flash2 grouper]$ /home/mchyzer/tomcat/bin/shutdown.sh  
   Using CATALINA_BASE: /home/mchyzer/tomcat  
   Using CATALINA_HOME: /home/mchyzer/tomcat  
   Using CATALINA_TMPDIR: /home/mchyzer/tomcat/temp  
   Using JRE_HOME: /opt/appserv/java6  
   [mchyzer@flash2 grouper]$ /home/mchyzer/tomcat/bin/startup.sh  
   Using CATALINA_BASE: /home/mchyzer/tomcat  
   Using CATALINA_HOME: /home/mchyzer/tomcat  
   Using CATALINA_TMPDIR: /home/mchyzer/tomcat/temp  
   Using JRE_HOME: /opt/appserv/java6  
   [mchyzer@flash2 grouper]$
- Add a group with or without the fubGroup type, and see the type and attribute when done
- 
- 
-
