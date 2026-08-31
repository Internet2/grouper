---
title: "Grouper permissions example"
space: Grouper
pageId: 28548152
version: 7
lastUpdated: 2013-04-11T01:53:39.595Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548152/Grouper+permissions+example
---

This is in response to the request to show how to query via web service all the permissions implied by a role.

 Note: this was done in 2.1.4 (candidate) Note also, all this can be done in the UI too...

 Create a session, and two attribute definitions

 
```

gsh 0% grouperSession = GrouperSession.startRootSession();
gsh 1% attributeDef = new AttributeDefSave(grouperSession).assignName("test:app0:permissionDefs:permissionDef0").assignToEffMembership(true).assignToGroup(true).assignAttributeDefType(AttributeDefType.perm).assignCreateParentStemsIfNotExist(true).save();
gsh 2% attributeDef.getAttributeDefActionDelegate().configureActionList("read,write");
gsh 3% attributeDef2 = new AttributeDefSave(grouperSession).assignName("test:app1:permissionDefs:permissionDef1").assignToEffMembership(true).assignToGroup(true).assignAttributeDefType(AttributeDefType.perm).assignCreateParentStemsIfNotExist(true).save();
gsh 4% attributeDef2.getAttributeDefActionDelegate().configureActionList("read,write, admin");

```

 Create a few roles, add some members

 
```

gsh 5% app0role0 = new GroupSave(grouperSession).assignName("test:app0:roles:role0").assignCreateParentStemsIfNotExist(true).assignTypeOfGroup(TypeOfGroup.role).save();
gsh 6% app0role1 = new GroupSave(grouperSession).assignName("test:app0:roles:role1").assignCreateParentStemsIfNotExist(true).assignTypeOfGroup(TypeOfGroup.role).save();
gsh 7% app1role0 = new GroupSave(grouperSession).assignName("test:app1:roles:role0").assignCreateParentStemsIfNotExist(true).assignTypeOfGroup(TypeOfGroup.role).save();
gsh 8% app1role1 = new GroupSave(grouperSession).assignName("test:app1:roles:role1").assignCreateParentStemsIfNotExist(true).assignTypeOfGroup(TypeOfGroup.role).save();
gsh 9% addMember("test:app0:roles:role0", "test.subject.0");
gsh 10% addMember("test:app0:roles:role0", "test.subject.1");
gsh 11% addMember("test:app0:roles:role1", "test.subject.1");
gsh 12% addMember("test:app0:roles:role1", "test.subject.2");
gsh 13% addMember("test:app1:roles:role0", "test.subject.2");
gsh 14% addMember("test:app1:roles:role0", "test.subject.3");
gsh 15% addMember("test:app1:roles:role1", "test.subject.3");
gsh 16% addMember("test:app1:roles:role1", "test.subject.4");

```

 Add some permissionNames

 
```

gsh 17% permissionName0_1 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:app0:permissionNames:permissionName0_1").assignCreateParentStemsIfNotExist(true).save();
gsh 18% permissionName0_2 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:app0:permissionNames:permissionName0_2").assignCreateParentStemsIfNotExist(true).save();
gsh 19% permissionName1_1 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:app0:permissionNames:permissionName1_1").assignCreateParentStemsIfNotExist(true).save();
gsh 20% permissionName1_2 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:app0:permissionNames:permissionName1_2").assignCreateParentStemsIfNotExist(true).save();
gsh 21% permissionName1_0_1 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:app1:permissionNames:permissionName0_1").assignCreateParentStemsIfNotExist(true).save();
gsh 22% permissionName1_0_2 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:app1:permissionNames:permissionName0_2").assignCreateParentStemsIfNotExist(true).save();
gsh 23% permissionName1_1_1 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:app1:permissionNames:permissionName1_1").assignCreateParentStemsIfNotExist(true).save();
gsh 24% permissionName1_1_2 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:app1:permissionNames:permissionName1_2").assignCreateParentStemsIfNotExist(true).save();
gsh 25% permissionName1_1_3 = new AttributeDefNameSave(grouperSession, attributeDef2).assignName("test:app1:permissionNames:permissionName1_3").assignCreateParentStemsIfNotExist(true).save();
gsh 26% permissionName1_1_4 = new AttributeDefNameSave(grouperSession, attributeDef2).assignName("test:app1:permissionNames:permissionName1_4").assignCreateParentStemsIfNotExist(true).save();

```

 Assign some role and individual permissions

 
```

gsh 27% app0role0.getPermissionRoleDelegate().assignRolePermission("read", permissionName0_1);
gsh 28% app0role0.getPermissionRoleDelegate().assignRolePermission("write", permissionName1_1_1);
gsh 29% app1role1.getPermissionRoleDelegate().assignRolePermission("read", permissionName1_1_2);
gsh 30% app1role1.getPermissionRoleDelegate().assignRolePermission("read", permissionName1_0_2);
gsh 31% app0role0.getPermissionRoleDelegate().assignSubjectRolePermission("admin", permissionName1_1_4, SubjectFinder.findById("test.subject.0", true));
gsh 32% app0role0.getPermissionRoleDelegate().assignSubjectRolePermission("write", permissionName1_0_1, SubjectFinder.findById("test.subject.0", true));
gsh 33% app0role1.getPermissionRoleDelegate().assignSubjectRolePermission("read", permissionName0_2, SubjectFinder.findById("test.subject.1", true));
gsh 34% app1role1.getPermissionRoleDelegate().assignSubjectRolePermission("write", permissionName1_0_1, SubjectFinder.findById("test.subject.4", true));

```

 Now, lets query via web service to find permission assignments assigned to a role

 
```

java -jar grouperClient.jar --operation=getPermissionAssignmentsWs --permissionType=role --roleNames=test:app0:roles:role0

```

 XML to server:

 
```

<WsRestGetPermissionAssignmentsRequest>
  <permissionType>role</permissionType>
  <roleLookups>
    <WsGroupLookup>
      <groupName>test:app0:roles:role0</groupName>
    </WsGroupLookup>
  </roleLookups>
</WsRestGetPermissionAssignmentsRequest>

```

 XML from server:

 
```

<WsGetPermissionAssignmentsResults>
  <wsAttributeDefs>
    <WsAttributeDef>
      <extension>permissionDef0</extension>
      <name>test:app0:permissionDefs:permissionDef0</name>
      <uuid>33c0b3263a1c42ae861990012192310b</uuid>
      <attributeDefType>perm</attributeDefType>
      <multiAssignable>F</multiAssignable>
      <multiValued>F</multiValued>
      <valueType>marker</valueType>
    </WsAttributeDef>
  </wsAttributeDefs>
  <wsPermissionAssigns>
    <WsPermissionAssign>
      <action>read</action>
      <permissionType>role</permissionType>
      <attributeDefNameId>4d81aebc539148c1908ac2f1485ad530</attributeDefNameId>
      <attributeDefNameName>test:app0:permissionNames:permissionName0_1
      </attributeDefNameName>
      <attributeDefId>33c0b3263a1c42ae861990012192310b</attributeDefId>
      <attributeDefName>test:app0:permissionDefs:permissionDef0</attributeDefName>
      <enabled>T</enabled>
      <attributeAssignId>c474dabd2d01482ba287910ea6929407</attributeAssignId>
      <roleId>b6979607f4be43038df280208521d021</roleId>
      <roleName>test:app0:roles:role0</roleName>
      <allowedOverall>T</allowedOverall>
      <disallowed>F</disallowed>
    </WsPermissionAssign>
    <WsPermissionAssign>
      <action>write</action>
      <permissionType>role</permissionType>
      <attributeDefNameId>7051d0aac8894e2cb05f4f00a14b1bb0</attributeDefNameId>
      <attributeDefNameName>test:app1:permissionNames:permissionName1_1
      </attributeDefNameName>
      <attributeDefId>33c0b3263a1c42ae861990012192310b</attributeDefId>
      <attributeDefName>test:app0:permissionDefs:permissionDef0</attributeDefName>
      <enabled>T</enabled>
      <attributeAssignId>9a183f124e8d4c1492931ebfa77165cc</attributeAssignId>
      <roleId>b6979607f4be43038df280208521d021</roleId>
      <roleName>test:app0:roles:role0</roleName>
      <allowedOverall>T</allowedOverall>
      <disallowed>F</disallowed>
    </WsPermissionAssign>
  </wsPermissionAssigns>
  <resultMetadata>
    <resultCode>SUCCESS</resultCode>
    <resultMessage>, Found 2 results.  </resultMessage>
    <success>T</success>
  </resultMetadata>
  <responseMetadata>
    <resultWarnings></resultWarnings>
    <millis>28208</millis>
    <serverVersion>2.1.4</serverVersion>
  </responseMetadata>
  <wsGroups>
    <WsGroup>
      <extension>role0</extension>
      <typeOfGroup>role</typeOfGroup>
      <displayExtension>role0</displayExtension>
      <displayName>test:app0:roles:role0</displayName>
      <name>test:app0:roles:role0</name>
      <uuid>b6979607f4be43038df280208521d021</uuid>
    </WsGroup>
  </wsGroups>
  <wsSubjects />
</WsGetPermissionAssignmentsResults>

```

 sdf
