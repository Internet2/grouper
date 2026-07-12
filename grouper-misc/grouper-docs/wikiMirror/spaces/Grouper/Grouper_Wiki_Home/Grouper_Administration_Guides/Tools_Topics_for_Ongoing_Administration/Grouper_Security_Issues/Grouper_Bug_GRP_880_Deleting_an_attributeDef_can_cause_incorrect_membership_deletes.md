---
title: "Grouper Bug GRP-880 - Deleting an attributeDef can cause incorrect membership deletes"
space: Grouper
pageId: 28673885
version: 8
lastUpdated: 2026-07-01T05:35:04.062Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28673885/Grouper+Bug+GRP-880+-+Deleting+an+attributeDef+can+cause+incorrect+membership+deletes
---

Grouper has a bug where if a group is given privileges to an attributeDef and you delete that attributeDef, then members of the group will be deleted. For example, if you have a group with members X, Y, and Z, and the group is given admin privileges on an attributeDef, then deleting the attributeDef will remove the memberships in the group for X, Y, Z. This will also end up leaving the group in a bad state in the Grouper database. Although this bug is in the Grouper API and can be triggered by any of the Grouper components that interact with the API, it is mainly a Grouper UI security concern. A user of the Grouper UI could create an attributeDef in a folder that the user owns and then grant privileges to a group that the user can only read. Finally, the user can delete the attributeDef and cause the problem.

 This bug affects all versions of Grouper from 1.6 to 2.1.3. The bug was fixed in 2.1.4.

 

## Reproduce the problem

 
```

subject = "test.subject.0";
groupName1 = "test:testAttributeDefDeleteIssue:testGroup1";              // use this to test UI issue
attributeDefName1 = "test:testAttributeDefDeleteIssue:attributeDef1";    // use this to test UI issue
groupName2 = "test:testAttributeDefDeleteIssue:testGroup2";              // use this to confirm the patch
attributeDefName2 = "test:testAttributeDefDeleteIssue:attributeDef2";    // use this to confirm the patch

grouperSession = GrouperSession.startRootSession();

// create groups
group1 = new GroupSave(grouperSession).assignName(groupName1).assignCreateParentStemsIfNotExist(true).save();
group2 = new GroupSave(grouperSession).assignName(groupName2).assignCreateParentStemsIfNotExist(true).save();

// create attributeDefs
attributeDef1 = new AttributeDefSave(grouperSession).assignName(attributeDefName1).assignToGroup(true).assignAttributeDefType(AttributeDefType.attr).save();
attributeDef2 = new AttributeDefSave(grouperSession).assignName(attributeDefName2).assignToGroup(true).assignAttributeDefType(AttributeDefType.attr).save();

// assign privileges
attributeDef1.getPrivilegeDelegate().grantPriv(group1.toSubject(), AttributeDefPrivilege.ATTR_READ, false);
attributeDef2.getPrivilegeDelegate().grantPriv(group2.toSubject(), AttributeDefPrivilege.ATTR_READ, false);

// add memberships
addMember(groupName1, subject);
addMember(groupName2, subject);

// also verify memberships
gsh 66% getMembers(groupName1)
member: id='test.subject.0' type='person' source='jdbc' uuid='5ee3e425b27c4290be477b4323dddc81'
gsh 67% getMembers(groupName2)
member: id='test.subject.0' type='person' source='jdbc' uuid='5ee3e425b27c4290be477b4323dddc81'

```

 Now reproduce the issue using the Grouper UI.

 

1. Go to the following page: [http://localhost:8090/grouper/grouperUi/appHtml/grouper.html?operation=SimpleAttributeUpdate.createEdit](http://localhost:8090/grouper/grouperUi/appHtml/grouper.html?operation=SimpleAttributeUpdate.createEdit) (fix the host and port to match yours).
2. Login as an admin.
3. Search for: test:testAttributeDefDeleteIssue:attributeDef1
4. Click on "Edit attribute definition".
5. Select Delete.
6. Run getMembers again using GSH and you'll see that the membership is gone. Then delete the group.

 
```

gsh 73% getMembers(groupName1)
gsh 74% delGroup(groupName1)
true

```

 

## Patch Grouper UI

 You need to edit Hib3AttributeDefDAO.java. Note, these instructions are tomcat-specific. Note, anytime you see /PATH_TO_GROUPER_UI_TOMCAT/ substitute that for the path to that tomcat, and anytime you see grouperUiAppName, substitute that for the app name you use for grouper UI, which is generally grouper.

 Unzip the grouper.jar file in a location where you can compile a classfile. These instructions are for linux, though could be used in windows if you change the path separators (colon to semicolon)

 
```

$ mkdir /tmp/grouperUiAttrDefPatch
$ cd /tmp/grouperUiAttrDefPatch/
$ mkdir temp
$ unzip /PATH_TO_GROUPER_UI_TOMCAT/webapps/grouperUiAppName/WEB-INF/lib/grouper.jar -d temp
$ mkdir -p edu/internet2/middleware/grouper/internal/dao/hib3/
$ cp temp/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3AttributeDefDAO.java edu/internet2/middleware/grouper/internal/dao/hib3/
$ rm -rf temp
$ vi edu/internet2/middleware/grouper/internal/dao/hib3/Hib3AttributeDefDAO.java

```

 In the method delete(final AttributeDef attributeDef), replace the following:

 
```

List<Membership> memberships = GrouperDAOFactory.getFactory().getMembership().findAllByAttrDefOwnerAsList(attributeDef.getId(), false);

```

 With this:

 
```

StringBuilder sql = new StringBuilder(
    "select ms, m from MembershipEntry as ms, Member as m where ms.ownerAttrDefId = :owner and ms.type = :type "
      + "and ms.memberUuid = m.uuid");
java.util.List<Object[]> mships = HibernateSession.byHqlStatic()
  .createQuery(sql.toString())
  .setCacheable(false)
  .setCacheRegion(Hib3MembershipDAO.class.getName())
  .setString("owner", attributeDef.getId())
  .setString( "type",   edu.internet2.middleware.grouper.membership.MembershipType.IMMEDIATE.getTypeString()   )
  .list(Object[].class);

java.util.List<edu.internet2.middleware.grouper.Membership> memberships = new java.util.ArrayList<edu.internet2.middleware.grouper.Membership>();

for(Object[] tuple:mships) {
  edu.internet2.middleware.grouper.Membership currMembership = (edu.internet2.middleware.grouper.Membership)tuple[0];
  edu.internet2.middleware.grouper.Member currMember = (edu.internet2.middleware.grouper.Member)tuple[1];
  currMembership.setMember(currMember);
  memberships.add(currMembership);
}

```

 Compile the patched file, note, if you are using Java previous to 1.6, then you need to list out the jar files, instead of the asterisk. Install, and bounce tomcat

 
```

$ javac -classpath "/PATH_TO_GROUPER_UI_TOMCAT/webapps/grouperUiAppName/WEB-INF/lib/*" -sourcepath . edu/internet2/middleware/grouper/internal/dao/hib3/Hib3AttributeDefDAO.java
$
$ find . -name Hib3AttributeDefDAO*
./edu/internet2/middleware/grouper/internal/dao/hib3/Hib3AttributeDefDAO$1.class
./edu/internet2/middleware/grouper/internal/dao/hib3/Hib3AttributeDefDAO$2.class
./edu/internet2/middleware/grouper/internal/dao/hib3/Hib3AttributeDefDAO.class
./edu/internet2/middleware/grouper/internal/dao/hib3/Hib3AttributeDefDAO.java
$
$ cp -R edu /PATH_TO_GROUPER_UI_TOMAT/webapps/grouperUiAppName/WEB-INF/classes/
$ find /PATH_TO_GROUPER_UI_TOMAT/webapps/grouperUiAppName/WEB-INF/classes/edu -name Hib3AttributeDefDAO*
/PATH_TO_GROUPER_UI_TOMAT/webapps/grouperUiAppName/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3AttributeDefDAO$1.class
/PATH_TO_GROUPER_UI_TOMAT/webapps/grouperUiAppName/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3AttributeDefDAO$2.class
/PATH_TO_GROUPER_UI_TOMAT/webapps/grouperUiAppName/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3AttributeDefDAO.class
/PATH_TO_GROUPER_UI_TOMAT/webapps/grouperUiAppName/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3AttributeDefDAO.java

```

 Note, if you run on a cluster, you should zip up the files in the patch, and copy them to all servers that need them. Make sure the path is grouperUiAppName/WEB-INF/classes/edu/internet2... Also, you should probably copy the source file and the classfiles in case there are further tweaks.

 Bounce tomcat.

 Test that the patch worked with the second attributeDef and group.

 

1. Go to the following page: [http://localhost:8090/grouper/grouperUi/appHtml/grouper.html?operation=SimpleAttributeUpdate.createEdit](http://localhost:8090/grouper/grouperUi/appHtml/grouper.html?operation=SimpleAttributeUpdate.createEdit) (fix the host and port to match yours).
2. Login as an admin.
3. Search for: test:testAttributeDefDeleteIssue:attributeDef2
4. Click on "Edit attribute definition".
5. Select Delete.
6. Run getMembers on groupName2 using GSH and you'll see that the membership is still there. Then delete the group.

 
```

gsh 73% getMembers(groupName2)
member: id='test.subject.0' type='person' source='jdbc' uuid='5ee3e425b27c4290be477b4323dddc81'
gsh 74% delGroup(groupName2)
true

```
