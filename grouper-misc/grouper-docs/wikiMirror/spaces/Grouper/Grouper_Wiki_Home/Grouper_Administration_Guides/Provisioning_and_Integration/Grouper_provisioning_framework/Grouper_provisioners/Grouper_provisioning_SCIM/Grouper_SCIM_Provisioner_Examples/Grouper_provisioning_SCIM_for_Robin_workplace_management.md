---
title: "Grouper provisioning SCIM for Robin workplace management"
space: Grouper
pageId: 28564194
version: 4
lastUpdated: 2026-07-01T05:35:32.597Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564194/Grouper+provisioning+SCIM+for+Robin+workplace+management
---

This applies to Grouper 4.5+

## Assign a group as provisionable

Note any group needs to let users in the front door too, e.g. add the school/center robin group which is in the overall robin group, e.g. robinHotelingGse for GSE

## External system

Create a SCIM key in robin. It seems that you can only get one, so if you need multiple, you might need to share.

Go to Integrations → SCIM and get your token

## Daemon to normalize userName

- In this example we are using the SCIM userName to search and match. But users created via CSV or SSO (SAML) get a userName assigned to them. In this case we will use netId which is subjectIdentifier0
- The email needs to be SSO ID which is EPPN which is subjectIdentifier1
- We will have a GSH daemon which gets all the users from Robin, and sees which do not have the right userName, and fix it
- Note, it might be possible to use externalId with the employee ID to make this not necessary and solely use the provisioning framework
- There is throttling in Robin so run this daily

Script

```
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
import edu.internet2.middleware.grouper.util.GrouperUtil;

//public class Test53robinDaemon {
//
//  public static void main(String[] args) {
    List<GrouperScim2User> allScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("robinProd", null);
    GrouperSession grouperSession = GrouperSession.startRootSession();
    int usersChanged = 0;
    for (GrouperScim2User grouperScim2User: allScimUsers) {
      String userName = grouperScim2User.getUserName();
      String email = grouperScim2User.getEmailValue().toLowerCase();
      String pennkeyShouldBe = GrouperUtil.prefixOrSuffix(email, "@", true);
      if (!StringUtils.equals(userName, pennkeyShouldBe)) {
        grouperScim2User.setUserName(pennkeyShouldBe);
        usersChanged++;
        Map<String, ProvisioningObjectChangeAction> fieldToUpdate = new HashMap<>();
        fieldToUpdate.put("userName", ProvisioningObjectChangeAction.update);
        GrouperScim2ApiCommands.patchScimUser("robinProd", null, grouperScim2User, fieldToUpdate);
      }
    }
    OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setUpdateCount(usersChanged);
    OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setTotalCount(GrouperUtil.length(allScimUsers));
//  }
//}
```

## Configuration

- Given/Family name is required and cannot contain accented characters, so use the netId if there is no first name and remove accented chars
- Do not have accented characters in display name
- In this case we are not created/deleting/updating groups, we are only assigning memberships to groups
- We will be creating users who are in assigned groups, but not deleting users or updating users
- Map the group name from metadata so the groups can contain spacial characters
- Change the description of the group in Robin so users know that the system of record is Grouper, and do not change the group name
- There is throttling in Robin so run the full daily and the incremental every minute

Configuration from the UI

Configuration file

```
provisioner.robinProd.addDisabledFullSyncDaemon = true
provisioner.robinProd.addDisabledIncrementalSyncDaemon = true
provisioner.robinProd.bearerTokenExternalSystemConfigId = robinProd
provisioner.robinProd.class = edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner
provisioner.robinProd.configureMetadata = true
provisioner.robinProd.customizeEntityCrud = true
provisioner.robinProd.customizeGroupCrud = true
provisioner.robinProd.customizeMembershipCrud = true
provisioner.robinProd.deleteEntities = false
provisioner.robinProd.deleteGroups = false
provisioner.robinProd.deleteMembershipsIfGrouperDeleted = true
provisioner.robinProd.entityAttributeValueCache0entityAttribute = id
provisioner.robinProd.entityAttributeValueCache0has = true
provisioner.robinProd.entityAttributeValueCache0source = target
provisioner.robinProd.entityAttributeValueCache0type = entityAttribute
provisioner.robinProd.entityAttributeValueCache1has = true
provisioner.robinProd.entityAttributeValueCache1source = grouper
provisioner.robinProd.entityAttributeValueCache1translationScript = \u0024{subject.getAttributeValue('firstName')}
provisioner.robinProd.entityAttributeValueCache1type = subjectTranslationScript
provisioner.robinProd.entityAttributeValueCache2has = true
provisioner.robinProd.entityAttributeValueCache2source = grouper
provisioner.robinProd.entityAttributeValueCache2translationScript = \u0024{subject.getAttributeValue('lastName')}
provisioner.robinProd.entityAttributeValueCache2type = subjectTranslationScript
provisioner.robinProd.entityAttributeValueCacheHas = true
provisioner.robinProd.entityMatchingAttribute0name = userName
provisioner.robinProd.entityMatchingAttributeCount = 1
provisioner.robinProd.errorHandlingRequiredValidationIsAnError = false
provisioner.robinProd.errorHandlingShow = true
provisioner.robinProd.groupAttributeValueCache0groupAttribute = id
provisioner.robinProd.groupAttributeValueCache0has = true
provisioner.robinProd.groupAttributeValueCache0source = target
provisioner.robinProd.groupAttributeValueCache0type = groupAttribute
provisioner.robinProd.groupAttributeValueCacheHas = true
provisioner.robinProd.groupMatchingAttribute0name = displayName
provisioner.robinProd.groupMatchingAttributeCount = 1
provisioner.robinProd.hasTargetEntityLink = true
provisioner.robinProd.hasTargetGroupLink = true
provisioner.robinProd.insertGroups = false
provisioner.robinProd.logAllObjectsVerbose = true
provisioner.robinProd.logAllObjectsVerboseToLogFile = false
provisioner.robinProd.logCommandsOnError = true
provisioner.robinProd.makeChangesToEntities = true
provisioner.robinProd.metadata.0.name = md_grouper_robinGroupName
provisioner.robinProd.metadata.0.required = true
provisioner.robinProd.metadata.0.showForGroup = true
provisioner.robinProd.numberOfEntityAttributes = 7
provisioner.robinProd.numberOfGroupAttributes = 2
provisioner.robinProd.numberOfMetadata = 1
provisioner.robinProd.operateOnGrouperEntities = true
provisioner.robinProd.operateOnGrouperGroups = true
provisioner.robinProd.operateOnGrouperMemberships = true
provisioner.robinProd.provisioningType = membershipObjects
provisioner.robinProd.replaceMemberships = true
provisioner.robinProd.scimType = generic
provisioner.robinProd.selectAllEntities = true
provisioner.robinProd.selectAllGroups = false
provisioner.robinProd.showAdvanced = true
provisioner.robinProd.startWith = this is start with read only
provisioner.robinProd.subjectSourcesToProvision = pennperson
provisioner.robinProd.targetEntityAttribute.0.name = id
provisioner.robinProd.targetEntityAttribute.1.name = userName
provisioner.robinProd.targetEntityAttribute.1.required = true
provisioner.robinProd.targetEntityAttribute.1.showAdvancedAttribute = true
provisioner.robinProd.targetEntityAttribute.1.showAttributeValidation = true
provisioner.robinProd.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.robinProd.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = subjectIdentifier0
provisioner.robinProd.targetEntityAttribute.2.name = givenName
provisioner.robinProd.targetEntityAttribute.2.translateExpression = \u0024{edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapElUtils.normalize("NFKD", edu.internet2.middleware.grouper.util.GrouperUtil.defaultString(gcGrouperSyncMember.getEntityAttributeValueCache1(), grouperProvisioningEntity.subjectIdentifier0)).replaceAll("\u005C\u005Cp{M}", "")}
provisioner.robinProd.targetEntityAttribute.2.translateExpressionType = translationScript
provisioner.robinProd.targetEntityAttribute.3.name = familyName
provisioner.robinProd.targetEntityAttribute.3.translateExpression = \u0024{edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapElUtils.normalize("NFKD", edu.internet2.middleware.grouper.util.GrouperUtil.defaultString(gcGrouperSyncMember.getEntityAttributeValueCache2(), grouperProvisioningEntity.subjectIdentifier0)).replaceAll("\u005C\u005Cp{M}", "")}
provisioner.robinProd.targetEntityAttribute.3.translateExpressionType = translationScript
provisioner.robinProd.targetEntityAttribute.4.name = emailValue
provisioner.robinProd.targetEntityAttribute.4.translateExpressionType = grouperProvisioningEntityField
provisioner.robinProd.targetEntityAttribute.4.translateFromGrouperProvisioningEntityField = subjectIdentifier1
provisioner.robinProd.targetEntityAttribute.5.name = emailType
provisioner.robinProd.targetEntityAttribute.5.translateExpressionType = staticValues
provisioner.robinProd.targetEntityAttribute.5.translateFromStaticValues = work
provisioner.robinProd.targetEntityAttribute.6.name = displayName
provisioner.robinProd.targetEntityAttribute.6.translateExpression = \u0024{edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapElUtils.normalize("NFKD", edu.internet2.middleware.grouper.util.GrouperUtil.defaultString(grouperProvisioningEntity.name, grouperProvisioningEntity.subjectIdentifier0)).replaceAll("\u005C\u005Cp{M}", "")}
provisioner.robinProd.targetEntityAttribute.6.translateExpressionType = translationScript
provisioner.robinProd.targetGroupAttribute.0.name = id
provisioner.robinProd.targetGroupAttribute.1.name = displayName
provisioner.robinProd.targetGroupAttribute.1.translateExpression = \u0024{grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_robinGroupName')}
provisioner.robinProd.targetGroupAttribute.1.translateExpressionType = translationScript
provisioner.robinProd.updateEntities = false
provisioner.robinProd.updateGroups = false

```
