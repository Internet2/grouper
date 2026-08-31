---
title: "Grouper automatically fix attribute corruption"
space: Grouper
pageId: 28548525
version: 6
lastUpdated: 2021-09-03T01:25:00.320Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548525/Grouper+automatically+fix+attribute+corruption
---

This is fixed in 2.3.0 api patch 98: [https://bugs.internet2.edu/jira/browse/GRP-1725](https://bugs.internet2.edu/jira/browse/GRP-1725)

The maintenance daemon will automatically fix registry corruption. This runs by default nightly. It is automatic since this is a race condition and the new value should overwrite the current value. So the previous value should be removed. Note this currently defaults to readonly until we get more experience with it, then we will default to on.

### Configuration

grouper-loader.properties

```
# if daemon should remove old values which are multi-assigned if the attribute is single valued
loader.removeMultiAttributeValuesIfSingleValuedAttribute = true

# if daemon should remove old values which are multi-assigned if the attribute is single valued
loader.removeMultiAttributeValuesIfSingleValuedAttributeLogOnly = true

# if daemon should remove old assignments which are multi-assigned if the attribute is single assign
loader.removeMultiAttributeAssignIfSingleAssignAttribute = true

# if daemon should remove old assignments which are multi-assigned if the attribute is single assign
loader.removeMultiAttributeAssignIfSingleAssignAttributeLogOnly = true
```

### Kick off by GSH

fix values which have multiple assignments but are single valued attribute definition

```
GrouperSesssion.startRootSession();

//Fix multiple attribute values where the attributeDef is not multi-valued
edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteMultipleCorruption.fixValues(logOnly=false);

//Fix multiple attribute assignments where the attributeDef is not multi-assignable
edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteMultipleCorruption.fixAssigns(logOnly=false);
```

### Logging

Values fix

```
2018-04-02 00:45:50,486: [main] ERROR GrouperDaemonDeleteMultipleCorruption.fixValues(168) -  - Redundant grouper_attribute_assign_value is deleted: [delete from grouper_attribute_assign_value where id = '257d9de235184449aa4dd4be0642417b'], note most recent attribute assign value is: b66fc8bab44842138f4b9fa527c41448 , created date (of one to delete): 2018-04-02 00:45:50.354, most recent created date: 2018-04-02 00:45:50.38, attributeDefName: test:attributeDefName0, value: 'test1', attributeAssignId: 4136de0be0b642c4ad576e86d35b8d21, attributeAssignOwner: Group[name=test:group0,uuid=a2ef6246f6aa42b5ba6e1325485501d9]

```

Assigns fix

```
2018-04-02 03:37:30,785: [main] ERROR GrouperDaemonDeleteMultipleCorruption.logAttributeAssignValues(401) -  - Is deleted value: [delete from grouper_attribute_assign_value where id = '288a0bbe975945cd85a9c8bd8fcc6a91'], value: 'value1b'
2018-04-02 03:37:30,786: [main] ERROR GrouperDaemonDeleteMultipleCorruption.logAttributeAssignValues(401) -  - Is deleted value: [delete from grouper_attribute_assign_value where id = '62d5e7c1c24e41d29f38d46664582b18'], value: 'value1a'
2018-04-02 03:37:30,786: [main] ERROR GrouperDaemonDeleteMultipleCorruption.logAttributeAssign(448) -  - Redundant grouper_attribute_assign is deleted: [delete from grouper_attribute_assign where id = 'a33e9785609547d99889796e3a737557'], note most recent attribute assign is: 84a7572ddee04d6c88745edb896d178a, created date (of one to delete): 2018-04-02 03:37:30.331, most recent created date: 2018-04-02 03:37:30.464, attributeDefName: test:attributeDefNameMeta0, attributeAssignOwner: AttributeAssign[id=4cf2f0db31944f528114d06ada02480d,action=assign,attributeDefName=test:attributeDefName0,
  group=Group[name=test:group0,uuid=ce7177b39dd2485987f6acec872b6218]]
2018-04-02 03:37:30,832: [main] ERROR GrouperDaemonDeleteMultipleCorruption.logAttributeAssign(448) -  - Redundant grouper_attribute_assign is deleted: [delete from grouper_attribute_assign where id = '4cf2f0db31944f528114d06ada02480d'], note most recent attribute assign is: 6a5e458b696947379ce5d6485f67993f, created date (of one to delete): 2018-04-02 03:37:30.175, most recent created date: 2018-04-02 03:37:30.206, attributeDefName: test:attributeDefName0, attributeAssignOwner: Group[name=test:group0,uuid=ce7177b39dd2485987f6acec872b6218]
2018-04-02 03:37:30,856: [main] ERROR GrouperDaemonDeleteMultipleCorruption.logAttributeAssignValues(401) -  - Is deleted value: [delete from grouper_attribute_assign_value where id = '0afb720a05c94666bb19a8afc231f41b'], value: 'value1'
2018-04-02 03:37:30,858: [main] ERROR GrouperDaemonDeleteMultipleCorruption.logAttributeAssign(448) -  - Redundant grouper_attribute_assign is deleted: [delete from grouper_attribute_assign where id = '10fc72b6fb7e40da8239eae207e4ecc5'], note most recent attribute assign is: 09dfd0fd2ae34dc4861fdcd9ff94138c, created date (of one to delete): 2018-04-02 03:37:30.54, most recent created date: 2018-04-02 03:37:30.588, attributeDefName: test:attributeDefNameMeta0, attributeAssignOwner: AttributeAssign[id=c059ffb168cc46298b4a14fe5fb9ec16,action=assign,attributeDefName=test:attributeDefName0,
  group=Group[name=test:group2,uuid=2387fe8025d74f5b8f7d952d98f232ea]]

```
