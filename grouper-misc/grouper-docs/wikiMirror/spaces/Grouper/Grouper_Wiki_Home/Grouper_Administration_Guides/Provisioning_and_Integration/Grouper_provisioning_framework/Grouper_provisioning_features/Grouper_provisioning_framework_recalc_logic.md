---
title: "Grouper provisioning framework recalc logic"
space: Grouper
pageId: 28555038
version: 6
lastUpdated: 2026-07-01T05:39:05.999Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555038/Grouper+provisioning+framework+recalc+logic
---

This wiki shows various conditions and the expected behavior of the provisioner to handle situations

See these other wikis for more context:

- [Provisioning glossary](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548708/Grouper+provisioning+glossary)
- [Full sync provisioning workflow](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555100/Grouper+provisioning+full+workflow)
- [Incremental provisioning workflow](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555332/Grouper+provisioning+incremental+workflow)

Definitions

- Grouper data - Groups in Grouper
- Target data - Users / Groups / Memberships in the target that is being provisioned to
- Full sync - The daily (configurable) sync that takes all or most data from Grouper and the Target and compares and makes it right
- Group sync - In an incremental sync, instead of dealing with individual memberships, take all the memberships of a group (generally in a recalc) and fix the whole group
- Incremental sync - The process that runs every minute to take Grouper change log events and messages and process them
- Sync table - Cache data from target and keep state
- Event - CLC and messaging has events for adding/removing data or checking state
- Recalc - If recalc, then check state on Grouper and Target side and treat message as stateless and make the target correct and update sync tables
- Send a message - If the provisioner needs to start over (e.g. action occurred, and error was thrown), then send a message to itself (incremental provisioner that runs a minute later), to check something again

| Full sync or Incremental | Event | Group or Entity in sync table | Membership in sync table | Membership type | Supported behavior by target | Error | Expected outcome regarding recalc | Status |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Full sync | N/A | Retrieve all groups and entities regardless of sync table values | Retrieve all memberships regardless of sync table values |  | CRUD supported | N/A | Everything is a recalc. | Done |
| Full sync | N/A | Retrieve all groups but not all entities ( configuration to not retrieve all entities in full sync) if the entity is in the sync table | Retrieve all memberships regardless of sync table values |  | CRUD supported | False | Use the sync entity. groups and memberships are recalcs and entities are not. | To be done |
| Full sync | N/A | Retrieve all groups and entities | Retrieve all memberships regardless of sync table values |  | CRUD supported | True for a membership change | Recalc the error group, error entity, and error memberships. |  |
| Full sync | N/A | Retrieve all groups but not all entities ( configuration to not retrieve all entities in full sync) if the entity is not in the sync table | Retrieve all memberships regardless of sync table values |  | CRUD supported | N/A | Recalc missing entities | To be done |
| Full sync | N/A | Retrieve all groups and entities regardless of sync table values | Retrieve all memberships regardless of sync table values |  | Doesn't select from target | N/A | Throw an exception if no object types select. If an object type does select from the target, sync only the object types that can select. e.g. group is selected, entity and membership are not. Only sync group with target and ignore entity and membership during full sync run. | To be done |
| Incremental sync | Membership add/remove | Group and entity are in the sync table | A membership add corresponds to a missing sync table entry, or a remove corresponds to an existing sync table entry |  | N/A | False | Nothing is a recalc |  |
| Incremental sync | Membership add/remove | Group and entity are in the sync table | A membership add corresponds to an existing sync table entry, or a remove corresponds to a missing sync table entry |  | Does select from target | False | Convert to a recalc membership |  |
| Incremental sync | Membership add/remove | Group and entity are in the sync table | A membership add corresponds to an existing sync table entry, or a remove corresponds to a missing sync table entry |  | Doesn't select from target | False | Don't convert to a recalc membership |  |
| Incremental sync | Membership add/remove | Group and entity are in the sync table | A membership add corresponds to a missing sync table entry, or a remove corresponds to an existing sync table entry |  | Does select from target | True | Send a message to recalc the group only and recalc the entity only (if there are groups and entities in the target), update the sync table with an error message |  |
| Incremental sync | Membership add/remove | Group and entity are in the sync table | A membership add corresponds to a missing sync table entry, or a remove corresponds to an existing sync table entry |  | Doesn't select from target | True | Put the error in the sync row (every error whether it sends a message or not, should still update the sync table).  A subsequent incremental provisioner will retry the actions. |  |
| Incremental sync | Membership add/remove | Group is not in the sync table | N/A |  | Does select from target | False | Group recalc with memberships (group attribute membership type or membership object type) - e.g. ldap has attributes on a group that represent memberships  Group recalc only (if entity attribute membership type ) - only sync group like name, description, etc. No memberships are synced with the target. |  |
| Incremental sync | Membership add/remove | Group is not in the sync table | N/A |  | Doesn't select from target | False | Proceed with the membership add or remove. Can't recalc because can't select from the target. The sync group row will be added to the table. |  |
| Incremental sync | Membership add/remove | Group is not in the sync table | N/A |  |  | True | Put the error in the sync group row (every error whether it sends a message or not, should still update the sync table) |  |
| Incremental sync | Membership add/remove | Entity is not in the sync table | N/A |  |  | False | Entity recalc with memberships (if entity attribute type )  Entity recalc only (if group attribute type ) |  |
| Incremental sync | Membership add/remove | Entity is not in the sync table | N/A |  |  | True | Put the error in the sync member row (every error whether it sends a message or not, should still update the sync table) |  |
| Incremental sync | Group add/remove | N/A | N/A | group attribute membership type or membership objects | Does select groups and memberships for a group from target | False | Group recalc with memberships (if group attribute membership type or membership objects)  Each entity in the group should not be recalc if in the sync table. This is applicable only for group attribute membership type or membership objects |  |
| Incremental sync | Group add/remove | N/A | N/A | entity attribute membership type | Does select groups from target | False | Group recalc only (if entity attribute type ) |  |
| Incremental sync | Group add/remove | N/A | N/A | group attribute membership type or membership objects | If either group or group memberships or both can't be selected from the target, the logic of this row is applied. | False | Proceed with the group add or remove without Recalc. |  |
| Incremental sync | Group add/remove | N/A | N/A | entity attribute membership type | Doesn't select groups from the target | False | Proceed with the group add or remove without Recalc. |  |
| Incremental sync | Group add/remove | N/A | N/A |  | N/A | True | Put the error in the sync group row |  |
| Incremental sync | Entity add/remove | N/A | N/A | entity attribute membership type or membership objects | Does select entities and memberships for an entity from the target | False | Entity recalc with memberships (if entity attribute membership type or membership objects)  Each group associated with the entity should not be recalc if in the sync table. This is applicable only for entity attribute membership type or membership objects |  |
| Incremental sync | Entity add/remove | N/A | N/A | group attribute membership type | Does select entities from the target | False | Entity recalc only |  |
| Incremental sync | Entity add/remove | N/A | N/A | entity attribute membership type or membership objects | If either entity or entity memberships or both can't be selected from the target, the logic of this row is applied. | False | Proceed with the entity add or remove without Recalc. |  |
| Incremental sync | Entity add/remove | N/A | N/A | group attribute membership type | Doesn't select entities from the target | False | Proceed with the entity add or remove without Recalc. |  |
| Incremental sync | Entity add/remove | N/A | N/A |  | N/A | True | Put the error in the sync entity row |  |

- "Not in sync table" means either missing from sync table or in_target = F or null
- Never do a group with memberships recalc if provisioning type is entity attributes
- Never do an entity with memberships recalc if provisioning type is group attributes
- Never do the recalc if the object is not provisioned in the target (might need to do add to behavior)
- The capabilities of the dao can restrict the outcomes, e.g. if you can't select you can't recalc
