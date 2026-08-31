---
title: "Grouper CCure provisioner"
space: Grouper
pageId: 131956770
version: 1
lastUpdated: 2026-08-15T18:34:34.746Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/131956770/Grouper+CCure+provisioner
---

## What it does

 The CCure provisioner grants and revokes physical access in C·CURE 9000 based on Grouper group membership. A Grouper group maps to a CCure **Clearance**, a member maps to a CCure **Personnel** record, and the membership Grouper writes is a **PersonnelClearancePair** - the object that gives a person a clearance.

 This is a **membership-only** provisioner. Personnel and Clearances belong to CCure and are managed there; Grouper never creates, updates, or deletes them. It only pairs objects that already exist on both sides. Both the person and the clearance must already be in CCure before Grouper can grant anything.

 

- Provisioner class: `edu.internet2.middleware.grouper.app.ccure.CCureProvisioner`
- Provisioning type: `membershipObjects`
- Group CRUD: select only (insert, update, and delete groups are all off)
- Entity CRUD: select only (make changes to entities is off)
- Membership CRUD: insert and delete

 

## Configuration

 

| Property | Default | Description |
| --- | --- | --- |
| `externalSystemConfigId` | (required) | The CCure external system to provision to. |

 Everything else is standard provisioning framework configuration. The settings that matter for CCure are covered below.

 

## Error handling: set this before you go live

 By default the framework treats "the object is missing from the target and cannot be created" as an error, and an object error fails the daemon run. For most provisioners that is right, because they can create the missing object. CCure never can.

 So on stock settings, a single group member without a CCure Personnel record - anyone without a badge - fails the entire run. In most deployments that is guaranteed to happen. Set:

 
```
provisioner.<configId>.errorHandlingShow = true
provisioner.<configId>.errorHandlingTargetObjectDoesNotExistIsAnError = false
```

 With that set, a member with no Personnel record and a group with no matching Clearance are both skipped, the rest of the group provisions normally, and the run succeeds. Leave the default on only if every provisionable group is guaranteed to map completely onto CCure and you want a missing object to stop the run.

 

## Attributes

 Group attributes - a Clearance:

 

| Grouper attribute name | CCure field | Description |
| --- | --- | --- |
| `ObjectID` | ObjectID | CCure-assigned clearance id. Read-only. Cache it in a group attribute value cache so memberships can translate from it. |
| `Name` | Name | Clearance name. Typically translated from the group display extension, and used to match the Grouper group to an existing Clearance. |
| `GUID` | GUID | CCure object GUID. Read-only. |
| `PartitionID` | PartitionID | CCure partition the clearance belongs to. Read-only. |

 Entity attributes - a Personnel record:

 

| Grouper attribute name | CCure field | Description |
| --- | --- | --- |
| `PersonnelID` | ObjectID / PersonnelID | CCure-assigned personnel id. Read-only. Cache it in an entity attribute value cache so memberships can translate from it. |
| `Int1` | Int1 | The field holding the campus identifier. Translated from the subject id, and used to match the Grouper subject to an existing Personnel record. |
| `Name` | Name | Person name. Read-only. |
| `GUID` | GUID | CCure object GUID. Read-only. |

 Membership attributes - a PersonnelClearancePair:

 

| Grouper attribute name | Description |
| --- | --- |
| `PersonnelID` | Translated from the entity attribute value cache holding PersonnelID. |
| `ClearanceID` | Translated from the group attribute value cache holding ObjectID. |
| `ObjectID` | The pair's own id, assigned by CCure on insert. A delete is keyed on this, not on the clearance id. |

 **Matching.** Match groups on `ObjectID` and `Name`, and entities on `PersonnelID` and `Int1`. A clearance pair has no single natural key, so match memberships on the pair of ids:

 
```
${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('ClearanceID'), targetMembership.retrieveAttributeValueString('PersonnelID'))}
```

 **The Int1 convention.** The provisioner matches people by the campus identifier stored in the CCure `Int1` field. If your CCure system keeps that identifier somewhere else, the entity attribute and matching attribute need to change to that field.

 

## Sync back

 Sync back is supported on all three axes. Clearances, Personnel, and clearance pairs read from CCure are captured into the generic `grouper_prov_group`, `grouper_prov_user`, and `grouper_prov_mship` reporting tables.

 Because the CCure query endpoint returns only the fields the request asks for, the provisioner adds any operator-configured native attributes to the outbound field list. So capturing an extra CCure field via `nativeAttributesGroups` or `nativeAttributesEntities` works without any other change.

 

## External system

 Configure the connection first:

 [Grouper CCure external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/132022298/Grouper+CCure+external+system)

 

## Developer notes

 API endpoints, request and response examples, mock tables, and behaviors:

 [Grouper CCure provisioner developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/131956793/Grouper+CCure+provisioner+developer+notes)

 

## How to use it

 

1. Configure the CCure external system and use its "Test" button to confirm the credentials authenticate.
2. Create a provisioner in the Grouper UI, choose **CCure**, and point it at that external system.
3. Set the group, entity, and membership attributes from the tables above, including the two attribute value caches - memberships cannot be built without them.
4. Set the membership matching id expression to the MultiKey shown above.
5. Turn off the does-not-exist error as described in "Error handling" above.
6. In CCure, make sure a Clearance exists whose name matches the group display extension, and that the people you intend to provision have Personnel records carrying their campus id in `Int1`.
7. Mark the folder or group as provisionable to this provisioner and run a full sync.

 On each run the provisioner reads the Clearances, Personnel, and existing clearance pairs, then inserts a pair for each group member who does not have one and deletes pairs that no longer correspond to a Grouper membership. Deleting the Grouper group removes its pairs and leaves the Clearance itself untouched.

 CCure accepts only one clearance pair per call, so a run makes one web service call per membership added or removed.
