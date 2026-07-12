---
title: "Grouper provisioning validation"
space: Grouper
pageId: 28554271
version: 4
lastUpdated: 2026-07-01T05:40:48.387Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554271/Grouper+provisioning+validation
---

> The info on this page applies to Grouper 2.6 and above.

Objects that are synced from Grouper to the target system (e.g. LDAP, Azure, etc), can have validations on the elements.

## Validation configuration example

Here is an example of a config on a group attribute

## Attribute values

(on groups or entities):

- There is a matching id which is in the target format which compares objects on the Grouper side with objects on the target side to see which objects are the same representation. If there is no matching ID, and no other way to match (e.g. a secondary matching ID e.g. a "group link" DN or UUID), then the object is invalid
- Can have a default value where if it is normally blank, it will use the default, and if a value exists in the future that value will replace the default
- Can be required, so that if it does not have a value, then it is invalid
- Can have a max length so that if it will not fit in the target then it is invalid
- Can have an expression that validates the value (e.g. a regex or something complex)
- There is a search attribute to look up the group/entity in the target. There might be other ways to look up the user (e.g. by a "group link" value of DN or UUID or secondary search attribute). If there is no search attribute (even if not marked as required) including taking into consideration the secondary search strategies, then the object is invalid.

## Membership attribute configuration

Membership attribute value (groupAttributes or entityAttributes e.g. LDAP only, not for membershipObject provisioning e.g. web service or SQL)

## Membership attribute values

- If the value of the membership attribute is blank, then the object is invalid
- This applies to the value of the object in the membership. e.g. if you are provisioning groupAttributes, then if the membership attribute in the entity is invalid, then the entity is invalid (not the group)

## Memberships required

- We will add a config to require memberships in a group to make it provisionable
  
  - If a group doesnt have any memberships and this setting is enabled, then the group is not provisionable
  - If a group is in the target and is drained of memberships, then it will be handled in the provisioning delete logic (whatever resolution is configured)
- Entities must be in a provisionable group to be provisionable

## Handling validation problems

Note: full syncs will try all provisionable (by membership) objects, and will see if any invalid objects are now valid and can be provisioned

Important validation issues are the matching ID, search attribute, or membership attribute value.

Unimportant validation issues are the group description or user first/last name.

| Object type | State | Provisioning run type | Issue | Resolution |
| --- | --- | --- | --- | --- |
| Group/entity | Insert | Full | Field/attribute is invalid | Object is not provisionable. No memberships will be added until valid. Do not insert |
| Group/entity | Insert | Incremental | Field/attribute is invalid | Same resolution as full. Also do not retry until attributes change on the object or a full sync |
| Group/entity | Update | Full | Field/attribute is invalid | Do not update the attribute on the object. |
| Group/entity | Update | Incremental | Field/attribute is invalid | Same resolution as full. Also do not retry until attributes change on the object or a full sync |
| Group/entity | Delete / Mark unprovisionable | Full / incremental | Field/attribute is invalid | Delete the object if configured to. Invalid data will not stop deletes |
| Membership | Insert | Full / incremental | Group or entity has unimportant validation issue,    and is not already in target | Do not insert memberships if the group or entity needs to be in the target and is not valid and is not in the target |
| Membership | Insert | Full / incremental | Group or entity has unimportant validation issue,    and is already in target | Insert memberships if the group or entity needs to be in the target and is not valid and is in the target |
| Membership | Insert | Full | Group or entity has important validation issue | Do not insert the membership. The membership is not provisionable |
| Membership | Insert | Incremental | Group or entity has important validation issue | Same resolution as full. Also do not retry until attributes change on the object or a full sync |
| Membership | Delete | Full / incremental | Group or entity has any validation issue | Delete the membership. Invalid data will not stop deletes |

## Reporting on validation errors

Capture the errors and have them available on various UI screens for reporting / troubleshooting

**See also**

[Provisioning configuration validation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555162/Grouper+provisioning+configuration+validation)
