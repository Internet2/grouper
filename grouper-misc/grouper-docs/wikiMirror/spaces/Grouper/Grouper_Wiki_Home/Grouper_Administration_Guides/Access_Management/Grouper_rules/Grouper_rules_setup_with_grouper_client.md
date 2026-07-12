---
title: "Grouper rules setup with grouper client"
space: Grouper
pageId: 28549383
version: 9
lastUpdated: 2026-07-01T05:42:09.996Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549383/Grouper+rules+setup+with+grouper+client
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

 Here is an example of adding and testing a rule with the grouper client, which is a command line client that you can use if you have WS access. This is the [group intersection rule](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554980/Grouper+rules+pattern+-+Remove+invalid+membership+due+to+group) where you have to be in one group (e.g. employees) to be in another group (e.g. app users). If you fall out of employee, you will fall out of users. A nightly daemon cleans up inconsistencies.

 Note, the names of the attribute depends on where your grouper admin put them in your folder structure.

 
```

//create the groups
c:\temp\gc>java -jar grouperClient.jar --operation=groupSaveWs --name=stem:a --createParentStemsIfNotExist=T
Success: T: code: SUCCESS_INSERTED: stem:a
c:\temp\gc>java -jar grouperClient.jar --operation=groupSaveWs --name=stem:b --createParentStemsIfNotExist=T
Success: T: code: SUCCESS_INSERTED: stem:b

//add a rule to one group
c:\temp\gc>java -jar grouperClient.jar --operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=add_attr --attributeDefNameNames=etc:attribute:rules:rule --ownerGroupNames=stem:a
Index: 0: attributeAssignType: group, owner: stem:a, attributeDefNameName: etc:attribute:rules:rule, action: assign, values: none, enabled: T, id: ac0da4c4802b43589fbcc0a888ba0d33, changed: T, deleted: F, valuesChanged: F

//assign the rule values to the rule assignment
c:\temp\gc>java -jar grouperClient.jar --operation=assignAttributesWs --attributeAssignType=group_asgn --attributeAssignOperation=assign_attr --attributeDefNameNames=etc:attribute:rules:ruleActAsSubjectSourceId --ownerAttributeAssignUuids=ac0da4c4802b43589fbcc0a888ba0d33 --attributeAssignValueOperation=assign_value --values0System=g:isa
Index: 0: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleActAsSubjectSourceId, action: assign, values: g:isa, enabled: T, id: cbd0b7bcdc7e4fb8be6fe51286285164, changed: T, deleted: F, valuesChanged: T

c:\temp\gc>java -jar grouperClient.jar --operation=assignAttributesWs --attributeAssignType=group_asgn --attributeAssignOperation=assign_attr --attributeDefNameNames=etc:attribute:rules:ruleActAsSubjectId --ownerAttributeAssignUuids=ac0da4c4802b43589fbcc0a888ba0d33 --attributeAssignValueOperation=assign_value --values0System=GrouperSystem
Index: 0: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleActAsSubjectId, action: assign, values: GrouperSystem, enabled: T, id: 07566868e528408e977db37fb9e3bd0b, changed: T, deleted: F, valuesChanged: T

c:\temp\gc>java -jar grouperClient.jar --operation=assignAttributesWs --attributeAssignType=group_asgn --attributeAssignOperation=assign_attr --attributeDefNameNames=etc:attribute:rules:ruleCheckOwnerName --ownerAttributeAssignUuids=ac0da4c4802b43589fbcc0a888ba0d33 --attributeAssignValueOperation=assign_value --values0System=stem:b
Index: 0: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleCheckOwnerName, action: assign, values: stem:b, enabled: T, id: 1333885cbbf7453aa312d8472fd93268, changed: T,deleted: F, valuesChanged: T

c:\temp\gc>java -jar grouperClient.jar --operation=assignAttributesWs --attributeAssignType=group_asgn --attributeAssignOperation=assign_attr --attributeDefNameNames=etc:attribute:rules:ruleCheckType --ownerAttributeAssignUuids=ac0da4c4802b43589fbcc0a888ba0d33 --attributeAssignValueOperation=assign_value --values0System=membershipRemove
Index: 0: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleCheckType, action: assign, values: membershipRemove, enabled: T, id: 0ecf19c9b08c4554a72224eeadb60279, changed: T, deleted: F, valuesChanged: T

c:\temp\gc>java -jar grouperClient.jar --operation=assignAttributesWs --attributeAssignType=group_asgn --attributeAssignOperation=assign_attr --attributeDefNameNames=etc:attribute:rules:ruleIfConditionEnum --ownerAttributeAssignUuids=ac0da4c4802b43589fbcc0a888ba0d33 --attributeAssignValueOperation=assign_value --values0System=thisGroupHasImmediateEnabledMembership
Index: 0: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleIfConditionEnum, action: assign, values: thisGroupHasImmediateEnabledMembership, enabled: T, id: 816c21a0ada040798f4ad53799effb23, changed: T, deleted: F, valuesChanged: T

c:\temp\gc>java -jar grouperClient.jar --operation=assignAttributesWs --attributeAssignType=group_asgn --attributeAssignOperation=assign_attr --attributeDefNameNames=etc:attribute:rules:ruleThenEnum --ownerAttributeAssignUuids=ac0da4c4802b43589fbcc0a888ba0d33 --attributeAssignValueOperation=assign_value --values0System=removeMemberFromOwnerGroup
Index: 0: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleThenEnum, action: assign, values: removeMemberFromOwnerGroup, enabled: T, id: 63331ddaddd54f22b7fbe33338473f8c, changed: T, deleted: F, valuesChanged: T

//check everything, make sure rule is valid
c:\temp\gc>java -jar grouperClient.jar --operation=getAttributeAssignmentsWs --attributeAssignType=group --attributeDefNameNames=etc:attribute:rules:rule --ownerGroupNames=stem:a --includeAssignmentsOnAssignments=T
Index: 0: attributeAssignType: group, owner: stem:a, attributeDefNameName: etc:attribute:rules:rule, action: assign, values: none, enabled: T, id:ac0da4c4802b43589fbcc0a888ba0d33
Index: 1: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleActAsSubjectId, action: assign, values: GrouperSystem, enabled: T, id: 07566868e528408e977db37fb9e3bd0b
Index: 2: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleActAsSubjectSourceId, action: assign, values: g:isa, enabled: T, id: cbd0b7bcdc7e4fb8be6fe51286285164
Index: 3: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleCheckOwnerName, action: assign, values: stem:b, enabled: T, id: 1333885cbbf7453aa312d8472fd93268
Index: 4: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleCheckType, action: assign, values: membershipRemove, enabled: T, id: 0ecf19c9b08c4554a72224eeadb60279
Index: 5: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleIfConditionEnum, action: assign, values: thisGroupHasImmediateEnabledMembership, enabled: T, id: 816c21a0ada040798f4ad53799effb23
Index: 6: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleThenEnum, action: assign, values: removeMemberFromOwnerGroup, enabled: T, id: 63331ddaddd54f22b7fbe33338473f8c
Index: 7: attributeAssignType: group_asgn, owner: ac0da4c4802b43589fbcc0a888ba0d33, attributeDefNameName: etc:attribute:rules:ruleValid, action: assign, values: T, enabled: T, id: 1137697e77b649789f80ec1c806cf0c3

//add a membership to both groups
c:\temp\gc>java -jar grouperClient.jar --operation=addMemberWs --groupName=stem:a --subjectIds=test.subject.0
Index 0: success: T: code: SUCCESS: test.subject.0
c:\temp\gc>java -jar grouperClient.jar --operation=addMemberWs --groupName=stem:b --subjectIds=test.subject.0
Index 0: success: T: code: SUCCESS: test.subject.0

//remove from the one that will trigger the rule
c:\temp\gc>java -jar grouperClient.jar --operation=deleteMemberWs --groupName=stem:b --subjectIds=test.subject.0
Index 0: success: T: code: SUCCESS: test.subject.0

//see that it is removed from other group
c:\temp\gc>java -jar grouperClient.jar --operation=hasMemberWs --groupName=stem:a --subjectIds=test.subject.0
Index 0: success: T: code: IS_NOT_MEMBER: test.subject.0: false

```
