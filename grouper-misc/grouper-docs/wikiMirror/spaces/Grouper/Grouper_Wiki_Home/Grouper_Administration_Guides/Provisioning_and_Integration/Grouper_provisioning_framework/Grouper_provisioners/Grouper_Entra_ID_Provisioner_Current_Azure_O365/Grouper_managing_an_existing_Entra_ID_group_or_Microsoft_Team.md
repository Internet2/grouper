---
title: "Grouper managing an existing Entra ID group or Microsoft Team"
space: Grouper
pageId: 243728397
version: 5
lastUpdated: 2026-09-24T07:33:04.601Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/243728397/Grouper+managing+an+existing+Entra+ID+group+or+Microsoft+Team
---

The info on this page applies to Grouper v4+.

The Entra ID provisioner normally creates the groups it provisions. It can also take over the membership of an Entra group that already exists, such as the group behind a Microsoft Team. A Team is not a separate Entra object: it is a Microsoft 365 (Unified) group with a Team on top, so managing a Team's membership means managing that group. This page describes one way to configure that. To have Grouper create the Team itself instead, set `groupTypeUnified` and `resourceProvisioningOptionsTeam` as described in [Grouper Entra ID Provisioner (Current) Azure O365](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555567/Grouper+Entra+ID+Provisioner+Current+Azure+O365).

The approach works with a **least-privilege Entra credential**. The application holds `Group.Create` plus read permissions, not tenant-wide `Group.ReadWrite.All`, and manages a group by being its owner: automatically for groups Grouper creates, and granted per group for groups it adopts. If the application does hold `Group.ReadWrite.All`, the configuration is the same and the ownership step is not needed.

Use this when:

- The group must keep its identity: object id, mail address, SharePoint site, chat history.
- You provision to Active Directory and sync to Entra with AD Connect, which cannot produce a Unified group.

## Overview

By the numbers in the diagram:

1. The source population (a list or policy group) is a member of a mirror group in the application's folder.
2. The mirror group is provisionable to the Entra provisioner (config id `myEntra`), with metadata naming the existing Entra group.
3. The provisioner matches the existing Microsoft 365 group and writes the membership to it.
4. The Microsoft 365 group is the Team's membership.
5. Optionally, a SQL loader puts people the provisioner could not place into a sibling "missing" group.

## How the binding works

The provisioner matches Grouper groups to Entra groups on `id`, which is authoritative once linked, and `mailNickname`, which finds the group the first time. Both are cached from the target.

```text
provisioner.myEntra.groupMatchingAttributeCount = 2
provisioner.myEntra.groupMatchingAttribute0name = id
provisioner.myEntra.groupMatchingAttribute1name = mailNickname

provisioner.myEntra.groupAttributeValueCacheHas = true
provisioner.myEntra.groupAttributeValueCache0type = groupAttribute
provisioner.myEntra.groupAttributeValueCache0source = target
provisioner.myEntra.groupAttributeValueCache0groupAttribute = id
provisioner.myEntra.groupAttributeValueCache1type = groupAttribute
provisioner.myEntra.groupAttributeValueCache1source = target
provisioner.myEntra.groupAttributeValueCache1groupAttribute = mailNickname
```

If the `mailNickname` Grouper computes for a group equals that of an existing Entra group, Grouper adopts that group instead of creating one. The rest of the configuration makes that deliberate rather than accidental.

## Opt-in per group with metadata

Define two provisioning metadata fields and translate the target attributes from them when set, falling back to generated values when blank. The snippets on this page are excerpts; the `targetGroupAttribute` indexes are illustrative.

```text
provisioner.myEntra.configureMetadata = true
provisioner.myEntra.numberOfMetadata = 2
provisioner.myEntra.metadata.0.name = md_entraMailNickname
provisioner.myEntra.metadata.0.showForGroup = true
provisioner.myEntra.metadata.1.name = md_entraDisplayName
provisioner.myEntra.metadata.1.showForGroup = true

# mailNickname: the metadata if set, otherwise a value that cannot collide
provisioner.myEntra.targetGroupAttribute.6.name = mailNickname
provisioner.myEntra.targetGroupAttribute.6.translateExpressionType = translationScript
provisioner.myEntra.targetGroupAttribute.6.translateExpression = \
  ${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_entraMailNickname'), 'myInstEntra_' + grouperProvisioningGroup.getId())}

# displayName: the metadata if set, otherwise the group extension
provisioner.myEntra.targetGroupAttribute.1.name = displayName
provisioner.myEntra.targetGroupAttribute.1.translateExpressionType = translationScript
provisioner.myEntra.targetGroupAttribute.1.translateExpression = \
  ${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_entraDisplayName'), grouperProvisioningGroup.getExtension())}
```

The fallback `mailNickname` is a prefix plus the Grouper group uuid, so an ordinary group can never collide with a human-chosen name. A group is adopted only when an administrator enters the existing group's mail nickname in the metadata. Add externalized text so whoever fills in the fields knows that:

```text
md_entraMailNickname_myEntra_label = Entra mail nickname
md_entraMailNickname_myEntra_description = Leave blank unless attaching to an Entra group that \
  already exists. This is the local part of that group's email address, before the @.
md_entraDisplayName_myEntra_label = Entra display name
md_entraDisplayName_myEntra_description = Leave blank unless attaching to an existing Entra group. \
  Must match the display name in Entra exactly.
```

## Do not reshape the adopted group

An adopted Team is a Unified group; a group Grouper creates is typically a security group. If group type can be updated, the first sync tries to convert one into the other. Make the structural attributes insert-only:

```text
provisioner.myEntra.customizeGroupCrud = true

provisioner.myEntra.targetGroupAttribute.4.name = groupType
provisioner.myEntra.targetGroupAttribute.4.translateExpressionType = staticValues
provisioner.myEntra.targetGroupAttribute.4.translateFromStaticValues = security
provisioner.myEntra.targetGroupAttribute.4.update = false

provisioner.myEntra.targetGroupAttribute.5.name = securityEnabled
provisioner.myEntra.targetGroupAttribute.5.translateExpressionType = staticValues
provisioner.myEntra.targetGroupAttribute.5.translateFromStaticValues = true
provisioner.myEntra.targetGroupAttribute.5.update = false

provisioner.myEntra.targetGroupAttribute.0.name = id
provisioner.myEntra.targetGroupAttribute.0.update = false

# never delete a group Grouper did not create, and do not delete on unmark
provisioner.myEntra.deleteGroupsIfGrouperCreated = true
provisioner.myEntra.deleteGroupsIfUnmarkedProvisionable = false
```

`description` is not in that list: it is translated from the Grouper group description and pushed on update, so it becomes the description Team members see. Write it accordingly, or set `update = false` on it too.

## Make the service principal an owner

With the least-privilege credential, **Grouper has no rights over a group it did not create until its service principal is an owner of that group**. Without that, the sync fails with `403 Authorization_RequestDenied` on `POST /groups/<id>/members/$ref`. Skip this section if the application holds `Group.ReadWrite.All`.

Neither the Entra owners picker nor the Teams member picker lists service principals, so use the command line. Only an existing owner can add an owner:

1. The Team owner adds the Grouper administrator as an owner.
2. The Grouper administrator adds the service principal:
  
  
  ```bash
  az ad group owner add --group <groupId> --owner-object-id <servicePrincipalObjectId>
  ```
  
  and verifies it (a plain owners listing hides service principals):
  
  
  ```bash
  az rest --method get --url \
    'https://graph.microsoft.com/v1.0/groups/<groupId>/owners/microsoft.graph.servicePrincipal?$select=id,displayName'
  ```
3. After the first sync, the administrator demotes themselves from owner to member. Since they are not in the Grouper group, the next sync removes them. The service principal must remain an owner.

If `groupOwners` is a static value so Grouper owns the groups it creates, set `update = false` and `select = false` on it. As of August 2025 the Graph owners listing omits service principals, so Grouper cannot read back the owner and would retry the add on every update.

## Attaching a group

1. **Use a mirror group** in the application's folder containing the source population, rather than provisioning a group owned by another service. This keeps the target's lifecycle independent of the source.
2. **Check the target in Entra:** type **Microsoft 365** and membership type **Assigned**. A security group is not a Team, and Graph rejects member writes on dynamic-membership groups.
3. **Make the service principal an owner**, as above.
4. **Mark the mirror group provisionable** and enter the existing group's mail nickname and display name in the metadata.
5. **Dry run** a full sync in read-only mode. Expect no group inserts, no group updates, no membership deletes, and membership inserts roughly equal to the population. Anything else means the metadata is wrong and a real run would create or rename an object. Do not skip this.
6. **Run it** and verify in Entra, not Teams; Teams clients can lag by a few hours.

## People who will not be added

Grouper matches subjects to Entra accounts by user principal name and does not create accounts. Subjects with no account in the tenant are skipped and logged as "Entity does not exist in target or cannot be found, and not creating entities". This includes guests, whose user principal name has the `#EXT#` form and does not match. Set `errorHandlingTargetObjectDoesNotExistIsAnError = false` so these do not fail the job.

So the Team will be smaller than the source population. Tell the owners before the first sync. To make the gap visible, a SQL loader over the provisioner sync tables can populate a "missing" group, which is easier to share than a log.

## Caveats

- Entra allows duplicate display names, and AD-synced groups cannot be managed through Graph. If the same population is provisioned to both AD and Entra, Grouper can end up managing the wrong same-named group. Match on `mailNickname`, not display name, and confirm the object id after the first sync.
- Set `sleepBeforeSelectAfterInsertMillis` to around 30000; Entra does not make a new group immediately visible to a following select.
- Unmarking the group provisionable does not delete the Entra group (with the settings above) and does not remove the service principal's ownership. Remove that by hand.
