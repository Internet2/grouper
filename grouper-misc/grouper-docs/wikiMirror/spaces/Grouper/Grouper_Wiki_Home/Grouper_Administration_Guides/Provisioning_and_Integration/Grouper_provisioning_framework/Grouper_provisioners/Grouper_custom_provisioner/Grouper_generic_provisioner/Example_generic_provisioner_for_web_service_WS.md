---
title: "Example generic provisioner for web service (WS)"
space: Grouper
pageId: 28564240
version: 6
lastUpdated: 2026-07-01T05:35:27.679Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564240/Example+generic+provisioner+for+web+service+WS
---

Valid for Grouper v2.6.19+

[This is the non-lite wiki with more features](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564251/Example+custom+provisioner+for+web+service)

This is an example of how an institution would make a provisioner that is not included in the Grouper Provisioning Framework. If you have something that is generic that others can leverage, maybe we should add it to the Grouper product. If your needs are specific to your institution, this is what you need to do.

This example is based on requirements posted to the slack channel from University of Minnesota.

All the code is included in Grouper, so you can see the source code. Though this provisioner is not enabled, it is intended as an example.

[Example generic WS source code](https://github.com/Internet2/grouper/tree/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisionerGeneric)

**Table of contents**

****

## WS spec

The first step is to identify and document the WS spec. Proof of concepts of calling the WS could be done.

In this case there is one operation, a REPLACE of members for a group

It is assumed that authentication is basic auth.

HTTP method

```
PUT /path/endpoint/<SOURCE>/<ROLE>
```

HTTP body

```
<?xml version="1.0"?>
<ExternalRoleRequest>
 <Users>
    <netID>USER1234</netID>
    <netID>USER5678</netID>
    <netID>USER9012</netID>
    <netID>USER3456</netID>
  </Users>
</ExternalRoleRequest>
```

## Design the target representation of objects

Plan out which operations of the WS will be used for Grouper and how.

There is one operation, so we will use that

The <SOURCE> will be configured for the provisioner instance. If you want to provision to multiple sources, make another provisioner. This is an assumption and could be metadata or based on a parent folder or whatever.

The <ROLE> will be the sole attribute of the target representation of the group. We will translate this from the extension of the group. Again this is an assumption. Figure out how you want to translate based on your requirements.

| **Target group representation** |
| --- |
| Attribute | Translation | Notes |
| role | group extension | The provisioner will put this attribute in the role spot in the URL |

The provisioning type will be membershipObjects. We could have probably used groupAttributes, but this is what we did. The membershipObject will have two attributes.

| **Target membership representation** |
| --- |
| Attribute | Translation | Notes |
| role | group extension | The provisioner needs this to differentiate memberships from one group to another. Needs a tuple |
| netID | entity subjectIdentifier | The main subject identifier defaults to the subject source subjectIdentifier0. Will make the XML based on these |

We are not selecting or changing entities and there is a straight translation from grouper provisioning entities (subjectIdentifier0) so we don't need to define a target representation of entities.

## Configure

provisioner.myGenericWs.addDisabledFullSyncDaemon = true  
provisioner.myGenericWs.addDisabledIncrementalSyncDaemon = true  
provisioner.myGenericWs.class = edu.internet2.middleware.grouper.app.genericProvisioner.GrouperGenericProvisioner  
provisioner.myGenericWs.customizeGroupCrud = true  
provisioner.myGenericWs.customizeMembershipCrud = true  
provisioner.myGenericWs.deleteGroups = false  
provisioner.myGenericWs.deleteMemberships = false  
provisioner.myGenericWs.genericProvisionerDaoClassName = edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisionerGeneric.GrouperExampleWsGenericTargetDao  
provisioner.myGenericWs.insertGroups = false  
provisioner.myGenericWs.insertMemberships = false  
provisioner.myGenericWs.logAllObjectsVerbose = true  
provisioner.myGenericWs.membership2AdvancedOptions = true  
provisioner.myGenericWs.membershipMatchingIdExpression = ${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('role'), targetMembership.retrieveAttributeValueString('netID'))}  
provisioner.myGenericWs.numberOfGroupAttributes = 1  
provisioner.myGenericWs.numberOfMembershipAttributes = 2  
provisioner.myGenericWs.operateOnGrouperGroups = true  
provisioner.myGenericWs.operateOnGrouperMemberships = true  
provisioner.myGenericWs.provisioningType = membershipObjects  
provisioner.myGenericWs.replaceMemberships = true  
provisioner.myGenericWs.selectGroups = false  
provisioner.myGenericWs.selectMemberships = false  
provisioner.myGenericWs.showAdvanced = true  
provisioner.myGenericWs.startWith = this is start with read only  
provisioner.myGenericWs.subjectSourcesToProvision = jdbc  
provisioner.myGenericWs.targetGroupAttribute.0.name = role  
provisioner.myGenericWs.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField  
provisioner.myGenericWs.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = extension  
provisioner.myGenericWs.targetMembershipAttribute.0.name = role  
provisioner.myGenericWs.targetMembershipAttribute.0.translateExpressionType = grouperProvisioningGroupField  
provisioner.myGenericWs.targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField = extension  
provisioner.myGenericWs.targetMembershipAttribute.1.name = netID  
provisioner.myGenericWs.targetMembershipAttribute.1.translateExpressionType = grouperProvisioningEntityField  
provisioner.myGenericWs.targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField = subjectIdentifier  
provisioner.myGenericWs.updateGroups = false

There is specific configuration here that needs to be set that is referenced from the DAO. We arbitrarily put this in grouper.properties, and used keys that will not be confused with other things. Note in this case the configId has to match

```
exampleWs.myGenericWs.endpointPrefix = http://localhost:8080/grouper/mockServices/exampleWs
exampleWs.myGenericWs.userName = testUserName
exampleWs.myGenericWs.password = 123456
exampleWs.myGenericWs.source = testSource
```

## Write the DAO

[Here is the DAO](https://github.com/Internet2/grouper/blob/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisionerGeneric/GrouperExampleWsGenericTargetDao.java)

This is the code that connects the target to the provisioning framework in a minimal way. The code to select/insert/update/delete/replace, whatever the target supports. In this case it is only a replace

## Assign provisionable

## Run the daemon
