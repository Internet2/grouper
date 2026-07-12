---
title: "Grouper Entra ID Provisioner (Current) Azure O365"
space: Grouper
pageId: 28555567
version: 57
lastUpdated: 2026-07-01T05:37:54.762Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555567/Grouper+Entra+ID+Provisioner+Current+Azure+O365
---

> The info on this page applies to Grouper v4 and above.   
> For Azure provisioning in Grouper versions before Grouper 2.6, see [this page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555049/Grouper+Entra+ID+Provisioner+Legacy).  
> Grouper versioning information is [here](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544481/Versioning+Support+Policy).

## Links

- [Azure portal](https://portal.azure.com/#home)
- [User documentation](https://docs.microsoft.com/en-us/graph/api/resources/user?view=graph-rest-beta)
- [Azure group types](https://docs.microsoft.com/en-us/graph/api/resources/groups-overview?view=graph-rest-1.0)

## External system

[Entra ID external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549126/Grouper+Entra+ID+Azure+External+System)

## Demo

- Movie: Grouper provisioning framework Azure demo (v2.6.15): [https://youtu.be/abTkJVBMr1M](https://youtu.be/abTkJVBMr1M)
- Config (grouper-loader.properties from version 2.6.15). Note, you should configure this in the provisioning configuration wizard.
  
  
  ```
  grouper.azureConnector.myAzure.clientId = 7e590d54-d6af-4b07-b2aXXXXXXX
  grouper.azureConnector.myAzure.clientSecret = *******
  grouper.azureConnector.myAzure.graphEndpoint = https://graph.microsoft.com
  grouper.azureConnector.myAzure.graphVersion = beta
  grouper.azureConnector.myAzure.loginEndpoint = https://login.microsoftonline.com/
  grouper.azureConnector.myAzure.resource = https://graph.microsoft.com
  grouper.azureConnector.myAzure.resourceEndpoint = https://graph.microsoft.com/beta/
  grouper.azureConnector.myAzure.tenantId = 5e7fa4df-8d24-4c33-bdaXXXXXXXXXXX
  
  changeLog.consumer.provisioner_incremental_azureProvisioner.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
  changeLog.consumer.provisioner_incremental_azureProvisioner.provisionerConfigId = azureProvisioner
  changeLog.consumer.provisioner_incremental_azureProvisioner.publisher.class = edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer
  changeLog.consumer.provisioner_incremental_azureProvisioner.publisher.debug = false
  changeLog.consumer.provisioner_incremental_azureProvisioner.quartzCron = 0 * * * * ?
  
  otherJob.provisioner_full_azureProvisioner.class = edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob
  otherJob.provisioner_full_azureProvisioner.provisionerConfigId = azureProvisioner
  otherJob.provisioner_full_azureProvisioner.quartzCron = 0 36 6 * * ?
  
  provisioner.azureProvisioner.addDisabledFullSyncDaemon = true
  provisioner.azureProvisioner.addDisabledIncrementalSyncDaemon = true
  provisioner.azureProvisioner.azureExternalSystemConfigId = myAzure
  provisioner.azureProvisioner.azureGroupType = true
  provisioner.azureProvisioner.class = edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisioner
  provisioner.azureProvisioner.entityAttributeValueCache0entityAttribute = id
  provisioner.azureProvisioner.entityAttributeValueCache0has = true
  provisioner.azureProvisioner.entityAttributeValueCache0source = target
  provisioner.azureProvisioner.entityAttributeValueCache0type = entityAttribute
  provisioner.azureProvisioner.entityAttributeValueCacheHas = true
  provisioner.azureProvisioner.entityMatchingAttribute0name = userPrincipalName
  provisioner.azureProvisioner.entityMatchingAttributeCount = 1
  provisioner.azureProvisioner.groupAttributeValueCache0groupAttribute = id
  provisioner.azureProvisioner.groupAttributeValueCache0has = true
  provisioner.azureProvisioner.groupAttributeValueCache0source = target
  provisioner.azureProvisioner.groupAttributeValueCache0type = groupAttribute
  provisioner.azureProvisioner.groupAttributeValueCacheHas = true
  provisioner.azureProvisioner.groupMatchingAttribute0name = displayName
  provisioner.azureProvisioner.groupMatchingAttributeCount = 1
  provisioner.azureProvisioner.hasTargetEntityLink = true
  provisioner.azureProvisioner.hasTargetGroupLink = true
  provisioner.azureProvisioner.logAllObjectsVerbose = true
  provisioner.azureProvisioner.logCommandsAlways = true
  provisioner.azureProvisioner.numberOfEntityAttributes = 2
  provisioner.azureProvisioner.numberOfGroupAttributes = 3
  provisioner.azureProvisioner.operateOnGrouperEntities = true
  provisioner.azureProvisioner.operateOnGrouperGroups = true
  provisioner.azureProvisioner.operateOnGrouperMemberships = true
  provisioner.azureProvisioner.provisioningType = membershipObjects
  provisioner.azureProvisioner.selectAllEntities = true
  provisioner.azureProvisioner.showAdvanced = true
  provisioner.azureProvisioner.startWith = this is start with read only
  provisioner.azureProvisioner.subjectSourcesToProvision = jdbc
  provisioner.azureProvisioner.targetEntityAttribute.0.name = id
  provisioner.azureProvisioner.targetEntityAttribute.1.name = userPrincipalName
  provisioner.azureProvisioner.targetEntityAttribute.1.translateExpression = ${ grouperProvisioningEntity.subjectId + '@mchyzergmail.onmicrosoft.com' }
  provisioner.azureProvisioner.targetEntityAttribute.1.translateExpressionType = translationScript
  provisioner.azureProvisioner.targetGroupAttribute.0.insert = false
  provisioner.azureProvisioner.targetGroupAttribute.0.name = id
  provisioner.azureProvisioner.targetGroupAttribute.0.showAdvancedAttribute = true
  provisioner.azureProvisioner.targetGroupAttribute.0.showAttributeCrud = true
  provisioner.azureProvisioner.targetGroupAttribute.0.update = false
  provisioner.azureProvisioner.targetGroupAttribute.1.name = displayName
  provisioner.azureProvisioner.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
  provisioner.azureProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = extension
  provisioner.azureProvisioner.targetGroupAttribute.2.name = mailNickname
  provisioner.azureProvisioner.targetGroupAttribute.2.translateExpressionType = grouperProvisioningGroupField
  provisioner.azureProvisioner.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField = extension
  
  
  ```
- Another config with extension attributes
  
  changeLog.consumer.provisioner_incremental_myAzureProvisioner.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer  
  changeLog.consumer.provisioner_incremental_myAzureProvisioner.provisionerConfigId = myAzureProvisioner  
  changeLog.consumer.provisioner_incremental_myAzureProvisioner.provisionerJobSyncType = incrementalProvisionChangeLog  
  changeLog.consumer.provisioner_incremental_myAzureProvisioner.publisher.class = edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer  
  changeLog.consumer.provisioner_incremental_myAzureProvisioner.publisher.debug = true  
  changeLog.consumer.provisioner_incremental_myAzureProvisioner.quartzCron = 9 59 23 31 12 ? 2099  
  grouper.azureConnector.myAzure.graphEndpoint = https\u003A//[graph.microsoft.com](http://graph.microsoft.com)  
  grouper.azureConnector.myAzure.graphVersion = beta  
  grouper.azureConnector.myAzure.groupLookupAttribute = displayName  
  grouper.azureConnector.myAzure.groupLookupValueFormat = \u0024{group.getName()}  
  grouper.azureConnector.myAzure.loginEndpoint = https\u003A//[login.microsoftonline.com/](http://login.microsoftonline.com/)  
  grouper.azureConnector.myAzure.resource = https\u003A//[graph.microsoft.com](http://graph.microsoft.com)  
  grouper.azureConnector.myAzure.resourceEndpoint = https\u003A//[graph.microsoft.com/beta/](http://graph.microsoft.com/beta/)  
  otherJob.provisioner_full_myAzureProvisioner.class = edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob  
  otherJob.provisioner_full_myAzureProvisioner.provisionerConfigId = myAzureProvisioner  
  otherJob.provisioner_full_myAzureProvisioner.quartzCron = 9 59 23 31 12 ? 2099  
  provisioner.myAzureProvisioner.allowOnlyMembersToPost = true  
  provisioner.myAzureProvisioner.azureExternalSystemConfigId = myAzure  
  provisioner.myAzureProvisioner.class = edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisioner  
  provisioner.myAzureProvisioner.customizeEntityCrud = true  
  provisioner.myAzureProvisioner.customizeGroupCrud = true  
  provisioner.myAzureProvisioner.customizeMembershipCrud = true  
  provisioner.myAzureProvisioner.debugLog = true  
  provisioner.myAzureProvisioner.deleteEntities = false  
  provisioner.myAzureProvisioner.deleteGroups = true  
  provisioner.myAzureProvisioner.deleteGroupsIfNotExistInGrouper = true  
  provisioner.myAzureProvisioner.deleteMemberships = true  
  provisioner.myAzureProvisioner.deleteMembershipsIfNotExistInGrouper = true  
  provisioner.myAzureProvisioner.entityAttributeValueCache2entityAttribute = id  
  provisioner.myAzureProvisioner.entityAttributeValueCache2has = true  
  provisioner.myAzureProvisioner.entityAttributeValueCache2source = target  
  provisioner.myAzureProvisioner.entityAttributeValueCache2type = entityAttribute  
  provisioner.myAzureProvisioner.entityAttributeValueCacheHas = true  
  provisioner.myAzureProvisioner.entityMatchingAttribute0name = displayName  
  provisioner.myAzureProvisioner.entityMatchingAttributeCount = 1  
  provisioner.myAzureProvisioner.groupAttributeValueCache0groupAttribute = displayName  
  provisioner.myAzureProvisioner.groupAttributeValueCache0has = true  
  provisioner.myAzureProvisioner.groupAttributeValueCache0source = target  
  provisioner.myAzureProvisioner.groupAttributeValueCache0type = groupAttribute  
  provisioner.myAzureProvisioner.groupAttributeValueCache2groupAttribute = id  
  provisioner.myAzureProvisioner.groupAttributeValueCache2has = true  
  provisioner.myAzureProvisioner.groupAttributeValueCache2source = target  
  provisioner.myAzureProvisioner.groupAttributeValueCache2type = groupAttribute  
  provisioner.myAzureProvisioner.groupAttributeValueCacheHas = true  
  provisioner.myAzureProvisioner.groupMatchingAttribute0name = displayName  
  provisioner.myAzureProvisioner.groupMatchingAttributeCount = 1  
  provisioner.myAzureProvisioner.hasTargetEntityLink = true  
  provisioner.myAzureProvisioner.hasTargetGroupLink = true  
  provisioner.myAzureProvisioner.hideGroupInOutlook = true  
  provisioner.myAzureProvisioner.insertEntities = true  
  provisioner.myAzureProvisioner.insertGroups = true  
  provisioner.myAzureProvisioner.insertMemberships = true  
  provisioner.myAzureProvisioner.loadEntitiesToGrouperTable = true  
  provisioner.myAzureProvisioner.logAllObjectsVerbose = true  
  provisioner.myAzureProvisioner.logCommandsAlways = true  
  provisioner.myAzureProvisioner.makeChangesToEntities = true  
  provisioner.myAzureProvisioner.numberOfEntityAttributes = 5  
  provisioner.myAzureProvisioner.numberOfGroupAttributes = 6  
  provisioner.myAzureProvisioner.operateOnGrouperEntities = true  
  provisioner.myAzureProvisioner.operateOnGrouperGroups = true  
  provisioner.myAzureProvisioner.operateOnGrouperMemberships = true  
  provisioner.myAzureProvisioner.provisioningType = membershipObjects  
  provisioner.myAzureProvisioner.resourceProvisioningOptionsTeam = true  
  provisioner.myAzureProvisioner.selectAllEntities = true  
  provisioner.myAzureProvisioner.selectAllEntitiesDuringDiagnostics = true  
  provisioner.myAzureProvisioner.selectAllGroupsDuringDiagnostics = true  
  provisioner.myAzureProvisioner.selectAllMembershipsDuringDiagnostics = true  
  provisioner.myAzureProvisioner.selectEntities = true  
  provisioner.myAzureProvisioner.selectGroups = true  
  provisioner.myAzureProvisioner.selectMemberships = true  
  provisioner.myAzureProvisioner.showAdvanced = true  
  provisioner.myAzureProvisioner.showProvisioningDiagnostics = true  
  provisioner.myAzureProvisioner.startWith = this is start with read only  
  provisioner.myAzureProvisioner.subjectSourcesToProvision = jdbc  
  [provisioner.myAzureProvisioner.targetEntityAttribute.0.name](http://provisioner.myAzureProvisioner.targetEntityAttribute.0.name) = id  
  provisioner.myAzureProvisioner.targetEntityAttribute.0.showAdvancedAttribute = true  
  provisioner.myAzureProvisioner.targetEntityAttribute.0.showAttributeCrud = true  
  provisioner.myAzureProvisioner.targetEntityAttribute.0.update = false  
  [provisioner.myAzureProvisioner.targetEntityAttribute.1.name](http://provisioner.myAzureProvisioner.targetEntityAttribute.1.name) = userPrincipalName  
  provisioner.myAzureProvisioner.targetEntityAttribute.1.translateExpression = \u0024{grouperProvisioningEntity.subjectId + '@[erviveksachdevagrouperoutlo.onmicrosoft.com](http://erviveksachdevagrouperoutlo.onmicrosoft.com)'}  
  provisioner.myAzureProvisioner.targetEntityAttribute.1.translateExpressionType = translationScript  
  [provisioner.myAzureProvisioner.targetEntityAttribute.2.name](http://provisioner.myAzureProvisioner.targetEntityAttribute.2.name) = displayName  
  provisioner.myAzureProvisioner.targetEntityAttribute.2.translateExpressionType = grouperProvisioningEntityField  
  provisioner.myAzureProvisioner.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField = name  
  [provisioner.myAzureProvisioner.targetEntityAttribute.3.name](http://provisioner.myAzureProvisioner.targetEntityAttribute.3.name) = mailNickname  
  provisioner.myAzureProvisioner.targetEntityAttribute.3.translateExpressionType = grouperProvisioningEntityField  
  provisioner.myAzureProvisioner.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField = id  
  [provisioner.myAzureProvisioner.targetEntityAttribute.4.name](http://provisioner.myAzureProvisioner.targetEntityAttribute.4.name) = accountEnabled  
  provisioner.myAzureProvisioner.targetEntityAttribute.4.translateExpression = \u0024{'true'}  
  provisioner.myAzureProvisioner.targetEntityAttribute.4.translateExpressionType = translationScript  
  [provisioner.myAzureProvisioner.targetGroupAttribute.0.name](http://provisioner.myAzureProvisioner.targetGroupAttribute.0.name) = id  
  provisioner.myAzureProvisioner.targetGroupAttribute.0.showAdvancedAttribute = true  
  provisioner.myAzureProvisioner.targetGroupAttribute.0.showAttributeCrud = true  
  provisioner.myAzureProvisioner.targetGroupAttribute.0.update = false  
  [provisioner.myAzureProvisioner.targetGroupAttribute.1.name](http://provisioner.myAzureProvisioner.targetGroupAttribute.1.name) = displayName  
  provisioner.myAzureProvisioner.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField  
  provisioner.myAzureProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = name  
  provisioner.myAzureProvisioner.targetGroupAttribute.2.insert = true  
  [provisioner.myAzureProvisioner.targetGroupAttribute.2.name](http://provisioner.myAzureProvisioner.targetGroupAttribute.2.name) = mailNickname  
  provisioner.myAzureProvisioner.targetGroupAttribute.2.select = true  
  provisioner.myAzureProvisioner.targetGroupAttribute.2.showAdvancedAttribute = true  
  provisioner.myAzureProvisioner.targetGroupAttribute.2.showAttributeCrud = true  
  provisioner.myAzureProvisioner.targetGroupAttribute.2.translateExpressionType = grouperProvisioningGroupField  
  provisioner.myAzureProvisioner.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField = extension  
  provisioner.myAzureProvisioner.targetGroupAttribute.2.update = true  
  [provisioner.myAzureProvisioner.targetGroupAttribute.3.name](http://provisioner.myAzureProvisioner.targetGroupAttribute.3.name).elConfig = extension_1ed9f56e96ef441dbf1a8391747e8c7f_altName  
  provisioner.myAzureProvisioner.targetGroupAttribute.3.translateExpressionType = grouperProvisioningGroupField  
  provisioner.myAzureProvisioner.targetGroupAttribute.3.translateFromGrouperProvisioningGroupField = displayExtension  
  provisioner.myAzureProvisioner.targetGroupAttribute.4.multiValued = true  
  [provisioner.myAzureProvisioner.targetGroupAttribute.4.name](http://provisioner.myAzureProvisioner.targetGroupAttribute.4.name).elConfig = extension_1ed9f56e96ef441dbf1a8391747e8c7f_jobGroup  
  provisioner.myAzureProvisioner.targetGroupAttribute.4.showAdvancedAttribute = true  
  provisioner.myAzureProvisioner.targetGroupAttribute.4.showAttributeValueSettings = true  
  provisioner.myAzureProvisioner.targetGroupAttribute.4.translateExpressionType = grouperProvisioningGroupField  
  provisioner.myAzureProvisioner.targetGroupAttribute.4.translateFromGrouperProvisioningGroupField = name  
  [provisioner.myAzureProvisioner.targetGroupAttribute.5.name](http://provisioner.myAzureProvisioner.targetGroupAttribute.5.name) = securityEnabled  
  provisioner.myAzureProvisioner.targetGroupAttribute.5.translateExpressionType = staticValues  
  provisioner.myAzureProvisioner.targetGroupAttribute.5.translateFromStaticValues = true  
  provisioner.myAzureProvisioner.updateEntities = true  
  provisioner.myAzureProvisioner.updateGroups = true

## Provisioning groups

#### [API documentation](https://learn.microsoft.com/en-us/graph/api/resources/groups-overview?view=graph-rest-1.0&tabs=http)

| Grouper name | Type | Required? | Azure/Entra API | Description |
| --- | --- | --- | --- | --- |
| allowOnlyMembersToPost | Boolean | No | resourceBehaviorOptions: ["AllowOnlyMembersToPost"] | When the attribute allowOnlyMembersToPost is true, it would add "AllowOnlyMembersToPost" to the resourceBehaviorOptions array in the API call |
| description | String | No | description | This is the group description on the Azure side |
| displayName | String | Required | displayName | This is the group display name on the Azure side |
| groupOwners | String array | No | [owners@odata.bind](mailto:owners@odata.bind) | This is list of owners. It's sent to Azure only when groupOwnersManage is set to true |
| groupOwnersManage | Boolean | No | N/A | If the value of this attribute is true, then Grouper sends [owners@odata.bind](mailto:owners@odata.bind) property to Azure |
| groupType (***Not being used***) | String | No | N/A | N/A |
| groupTypeUnified | Boolean | No |  | If the value of this attribute is true, then Grouper add "Unified" to the groupTypes array in the API call |
| hideGroupInOutlook | Boolean | No? | resourceBehaviorOptions: ["HideGroupInOutlook"] | When the attribute hideGroupInOutlook is true, it would add "HideGroupInOutlook" to the resourceBehaviorOptions array in the API call |
| id | String | required | id | This is the id read from Azure. Select only. This should not be translated from Grouper, and the target attribute should be cached. |
| isAssignableToRole | Boolean | No | isAssignableToRole | A group with isAssignableToRole property set to true cannot be of dynamic membership type, its securityEnabled must be set to true, and visibility can only be Private. |
| mailEnabled | Boolean | No | mailEnabled | Set to `true` for mail-enabled groups |
| mailNickname | String | Required | mailNickname | Sets the value of mailNickname on the Azure side |
| resourceProvisioningOptionsTeam | Boolean | No | resourceBehaviorOptions: ["Team"] | When the attribute resourceProvisioningOptionsTeam is true, it would add "Team" to the resourceProvisioningOptions array in the API call |
| securityEnabled | Boolean | Yes | securityEnabled | Set to `true` for security-enabled groups, including Microsoft 365 groups. **Note:** Groups created using the Microsoft Entra admin center or the Azure portal always have **securityEnabled** initially set to `true`. |
| subscribeMembersToCalendarEventsDisabled | Boolean | No | resourceBehaviorOptions: ["SubscribeMembersToCalendarEventsDisabled"] | When the attribute subscribeMembersToCalendarEventsDisabled is true, it would add "SubscribeMembersToCalendarEventsDisabled" to the resourceBehaviorOptions array in the API call |
| subscribeNewGroupMembers | Boolean | No | resourceBehaviorOptions: ["SubscribeNewGroupMembers"] | When the attribute subscribeNewGroupMembers is true, it would add "SubscribeNewGroupMembers" to the resourceBehaviorOptions array in the API call |
| visibility | String | No | visibility | Can be  Public \| Private \| HiddenMembership  If you make a metadata for visibility, select show on groups |
| welcomeEmailDisabled | Boolean | No | resourceBehaviorOptions: ["WelcomeEmailDisabled"] | When the attribute welcomeEmailDisabled is true, it would add "WelcomeEmailDisabled" to the resourceBehaviorOptions array in the API call |

## Provisioning users

You can search by email only. You should cache the email and id.

#### [API documentation](https://learn.microsoft.com/en-us/graph/api/resources/users?view=graph-rest-1.0)

| Grouper name | Type | Required? | Azure/Entra API | Description |
| --- | --- | --- | --- | --- |
| accountEnabled | Boolean | Required | accountEnabled | true if the account is enabled; otherwise, false. |
| displayName | String | Required | displayName | The name to display in the address book for the user. |
| id | String | Required | userId | This is the id read from Azure. Select only. This should not be translated from Grouper, and the target attribute should be cached. |
| mailNickname | String | Required | mailNickname | The mail alias for the user. |
| onPremisesImmutableId | String | See description | onPremisesImmutableId | Required only when creating a new user account if you are using a federated domain for the user's **userPrincipalName** (UPN) property. |
| userPrincipalName | String | Required | userPrincipalName | The user principal name (someuser@[contoso.com](http://contoso.com)). It's an Internet-style login name for the user based on the Internet standard RFC 822. By convention, this should map to the user's email name. The general format is alias@domain, where domain must be present in the tenant's collection of verified domains. The verified domains for the tenant can be accessed from the **verifiedDomains** property of [organization](https://learn.microsoft.com/en-us/graph/api/resources/organization?view=graph-rest-1.0).   NOTE: This property cannot contain accent characters. Only the following characters are allowed `A - Z`, `a - z`, `0 - 9`, `' . - _ ! # ^ ~`. |

## Entra Id provisioner configuration

| Name | Config suffix | Value | Description |
| --- | --- | --- | --- |
| Show metadata for 'Assignable to role' | assignableToRole | true\|false | When marking a group as provisionable, allow users to choose if the group is assignable to role. Default value is 'false'. |
| Show metadata for 'azure group type' | azureGroupType | true\|false | When marking a group as provisionable, allow users to choose azure group type. The possible values are: 'security', 'unified', 'unifiedSecurityEnabled'. These are the only three group types the API allows to assign. Grouper cannot manage distribution lists or dynamic distribution groups (legacy types). Default value is 'false'. |
| Show metadata for 'azure group owners' | groupOwners | true\|false | When marking a group as provisionable, allow users to choose azure group owners. Default value is 'false'. |
| Show metadata for 'group owners manage' | groupOwnersManage | true\|false | When marking a group as provisionable, allow users to decide if they want to manage group owners. Default value is 'false'. |
| Show metadata for 'allow only members to post' | allowOnlyMembersToPost | true\|false | When marking a group as provisionable, allow users to choose if only members can post. Default value is 'false'. |
| Show metadata for 'hide group in outlook' | hideGroupInOutlook | true\|false | When marking a group as provisionable, allow users to choose if group can be hidden in outlook. Default value is 'false'. |
| Show metadata for 'subscribe new group members' | subscribeNewGroupMembers | true\|false | When marking a group as provisionable, allow users to choose if new group members can subscribe. Default value is 'false'. |
| Show metadata for 'welcome email disabled' | welcomeEmailDisabled | true\|false | When marking a group as provisionable, allow users to choose if welcome email can be disabled. Default value is 'false'. |
| Show metadata for 'subscribe members to calendar events disabled' | subscribeMembersToCalendarEventsDisabled | true\|false | When marking a group as provisionable, allow users to choose if subscribe members to calendar events can be disabled. Default value is 'false'. |
| Show metadata for 'team' | resourceProvisioningOptionsTeam | true\|false | When marking a group as provisionable, allow users to choose if resource provisioning options can be set as Team. Default value is 'false'. |
| Show metadata for 'visibility' | visibilityMetadata | true\|false | When marking a group as provisionable, allow users to choose two metadata: 1. visibility public/private 2. hidden membership. Default value is 'false'. |

## Configure group owners in Azure

Note: groupOwners is a multivalued attribute. You must set "multi-value attribute" to True. To display the "multi-valued attribute" setting in the UI, first set "advanced options" to True, then set "show value settings" to True.

```
provisioner.myAzureProvisioner.groupOwnersManage = true

provisioner.myAzureProvisioner.targetGroupAttribute.5.multiValued = true
provisioner.myAzureProvisioner.targetGroupAttribute.5.name = groupOwners
provisioner.myAzureProvisioner.targetGroupAttribute.5.showAdvancedAttribute = true
provisioner.myAzureProvisioner.targetGroupAttribute.5.showAttributeValueSettings = true
provisioner.myAzureProvisioner.targetGroupAttribute.5.translateExpression = ${provisioningGroupWrapper.thisGroupPrivilegeHolders('admins', 'entityAttributeValueCache2')}
provisioner.myAzureProvisioner.targetGroupAttribute.5.translateExpressionType = translationScript

provisioner.myAzureProvisioner.entityAttributeValueCache2entityAttribute = id
provisioner.myAzureProvisioner.entityAttributeValueCache2has = true
provisioner.myAzureProvisioner.entityAttributeValueCache2source = target
provisioner.myAzureProvisioner.entityAttributeValueCache2type = entityAttribute
provisioner.myAzureProvisioner.entityAttributeValueCacheHas = true

```

### Setting the Grouper service account as the owner of new groups (to reduce Azure privileges) v4.2.0+

If the service account Grouper uses for Graph API calls can be set as the owner of a managed group, the Azure application no longer needs the privileges to update all groups and memberships. It only needs the Group.Create privilege to create a new group, Group.Read.All to find groups to match with Grouper, and User.Read.All to resolve target entities. While there is an option in the Azure provisioner to set the groupOwner attribute with one or more entities, the ability to add non-users (i.e. service accounts) is only available v4.2.0.

The "groupOwners" attribute is normally expected to be a Grouper subject ID or identifier used for search/match to resolve to an Azure object URL. But this lookup always uses the /users api endpoint, which means the service account can't be added this way. Since v4.2.0, an already-resolved URL can be entered in this field, and this URL can be either for a user or a service account. There are two ways to specify the account.

1) [https://graph.microsoft.com/v1.0/servicePrincipals/{id](https://graph.microsoft.com/v1.0/servicePrincipals/{id)}

2) [https://graph.microsoft.com/v1.0/servicePrincipals(appId='{appId}')](https://graph.microsoft.com/v1.0/servicePrincipals(appId=)

where {id} is the "Object ID" for the object in the Enterprise Applications page (it's not the App registrations page, but you can get to it from there by clicking "Managed application in local directory"). The {appId} is the application ID, also called the client ID, and is easier to find, being on the Enterprise applications page, the App registrations page, and various other pages when you look at the service account details. The appId is also the "Client ID" value in the external system configuration for Azure.

**Note:** As of August 2025, the [MS Graph API call to select the Entra goup's owners](https://learn.microsoft.com/en-us/graph/api/group-list-owners?view=graph-rest-1.0&tabs=http) will not list service principals in the response. By default, Grouper will try to re-add the service principal as a group owner every time it updates the Entra group. The audit log for the Entra group will show a failure to "Add owner to group" with a reason "Microsoft.Online.DirectoryServices.DirectoryValueExistsException", although Grouper does not report any error in its logs. To stop Grouper from attempting to update the Entra group owner, disable the Update operation for the "groupOwners" attribute:

```
provisioner.myAzureProvisioner.targetGroupAttribute.4.name = groupOwners
provisioner.myAzureProvisioner.targetGroupAttribute.4.showAdvancedAttribute = true
provisioner.myAzureProvisioner.targetGroupAttribute.4.showAttributeCrud = true
provisioner.myAzureProvisioner.targetGroupAttribute.4.showAttributeValueSettings = true
provisioner.myAzureProvisioner.targetGroupAttribute.4.translateExpressionType = staticValues
provisioner.myAzureProvisioner.targetGroupAttribute.4.translateFromStaticValues = https\u003A//graph.microsoft.com/v1.0/servicePrincipals/<redacted>
provisioner.myAzureProvisioner.targetGroupAttribute.4.update = false
```

## Group extension attributes

Grouper can manage custom extensions on groups. The attributes must be pre-configured in Entra ID before using them, Grouper only manages existing extension attributes. This is NOT for mail enabled groups or any group if you have one of these attributes configured: `welcomeEmailDisabled` , `resourceProvisioningOptionsTeam.`

- [https://learn.microsoft.com/en-us/graph/extensibility-overview?tabs=http](https://learn.microsoft.com/en-us/graph/extensibility-overview?tabs=http)

- [https://learn.microsoft.com/en-us/graph/api/application-post-extensionproperty?view=graph-rest-1.0&tabs=http](https://learn.microsoft.com/en-us/graph/api/application-post-extensionproperty?view=graph-rest-1.0&tabs=http)

To set up directory extensions in Azure/Entra, follow the steps below:

1. Call POST endpoint like [https://graph.microsoft.com/v1.0/applications/2083897a-1149-43e1-aad5-2bd9be5b97c2/extensionProperties](https://graph.microsoft.com/v1.0/applications/2083897a-1149-43e1-aad5-2bd9be5b97c2/extensionProperties)
2. The ID after the /`applications` is the Object ID. You can retrieve it from the Azure portal in the application tab.
3. Sample Payload for the POST call
  
  
  ```
  {
      "name": "jobGroup",
      "dataType": "String",
      "isMultiValued": true,
      "targetObjects": [
          "User",
          "Group"
      ]
  }
  ```
4. Response should look like
  
  
  ```
  {
      "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#applications('2083897a-1149-43e1-aad5-2bd9be5b97c2')/extensionProperties/$entity",
      "id": "e0918040-c1ac-4724-929e-6c104eb1cbaa",
      "deletedDateTime": null,
      "appDisplayName": "vivek-chris-test-july-2022",
      "dataType": "String",
      "isMultiValued": true,
      "isSyncedFromOnPremises": false,
      "name": "extension_1ed9f56e96ef441dbf1a8391747e8c7f_jobGroup",
      "targetObjects": [
          "User",
          "Group"
      ]
  }
  ```
5. In Azure portal, give Application.ReadWrite.all permissions to the application

To create extension attributes on the Grouper side, follow the steps below:

1. Create an extension attribute in the provisioner config. It must start with "extension_<applicationId>_<extensionProperty>". It's exactly the same name you see in the response body of the POST call [https://graph.microsoft.com/v1.0/applications/2083897a-1149-43e1-aad5-2bd9be5b97c2/extensionProperties](https://graph.microsoft.com/v1.0/applications/2083897a-1149-43e1-aad5-2bd9be5b97c2/extensionProperties)
2. If the extension property is single valued and of type string on the Azure side, then the above settings will work.
3. In case, the extension property is multi-valued, then on the Grouper side, set "show value settings" to true and set "multi-valued attribute" to true for the extension attribute like below:
4. The extension attribute value type on the Azure side must match with the value type on the Grouper side.
