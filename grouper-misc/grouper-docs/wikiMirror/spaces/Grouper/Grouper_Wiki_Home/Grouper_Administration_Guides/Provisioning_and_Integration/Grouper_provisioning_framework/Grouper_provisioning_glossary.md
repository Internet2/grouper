---
title: "Grouper provisioning glossary"
space: Grouper
pageId: 28548708
version: 21
lastUpdated: 2026-07-01T05:43:45.678Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548708/Grouper+provisioning+glossary
---

See also [Grouper Provisioning Framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544760/Grouper+provisioning+framework) for full documentation.

| Term | Definition | Example |
| --- | --- | --- |
| Provisioning | Taking Grouper data and sending to target |  |
| Provisioning framework | The Grouper system that manages provisioning | UI, logic, compare, logging, etc |
| Target | An external service that Grouper can sync with | LDAP, SQL, Box, etc |
| Group | Collection of subjects/entities that can be managed in target. Could be a group or role etc |  |
| Entity | Subject / entity / user in the target that can be managed |  |
| Membership | Relationship between a target group and entity |  |
| Full sync | Nightly (configurable) job that takes everything from Grouper    and everything from Target and syncs them up by changing the target |  |
| Incremental sync | Job that runs every minute (configurable) and takes change log events   and messages and manipulates the target |  |
| Group sync | Sync only one group or a collection of groups from Grouper to the target using recalc | e.g. if a certain configurable threshold number of memberships changed at once, or if a user clicks a button on UI to sync a group or a folder |
| Sync tables | Grouper SQL tables that store information for provisioning:    - Groups, users, memberships, jobs - State in the target (is data provisioned, when, did Grouper put it there) - Cache target data (if there is a "link" then keep the uuid or dn or whatever on the grouper side) - Track when data starts or ends being provisionable |  |
| Synchronous vs asynchronous sync | A full sync or group sync could be synchronous where it is the only provisioning action happening across all daemons and will update the target and update the grouper sync tables.  A full sync or group sync could be asynchronous where it looks at Grouper and the target and sees what is out of sync and sends messages to the Incremental sync to recalc certain objects (groups / entities / memberships / full sync) |  |
| Recalc stateless action | An action on a membership, group, or entity that will retrieve data   from Grouper and the target and compare and adjust the target   to make it consistent with Grouper. Recalcs happen:    - if a user clicks a button - if an error occurs and it gets requeued - if too many memberships appear on queue, just recalc the group - inconsistent events get converted to recalc (e.g. if its an add member but the sync object disagrees) - etc | User clicks "provision group" button,    it sends a message to the incremental   job |
| Non-recalc stateful action | An action that takes a Grouper change log event, and sends a command to the target. | Subject is added to provisionable provisioned   group and the addMember command is sent to    the target |
| Sync error | Error while trying to provision a group / entity / membership |  |
| Sync error: ERR | Error code: error, exception while provisioning | If an exception occurs for some reason, e.g. network error, then this error code happens |
| Sync error: INV | Error code: invalid data, based on script | If you have a validation for email address, and some user has an invalid email, then this error occurs |
| Sync error: LEN | Error code: attribute value is more than maxlength | If the max length of an attribute is set to 50, but the length of the data on one item is 68, then it will have this error and not me provisioned |
| Sync error: REQ | Error code: required field is missing | If you configure email to be a required attribute and a user doesnt have one, they cannot be provisioned |
| Sync error: DNE | Error code: object is missing in the target and not able to be inserted | If a membership in a group is for a user which has an entity link, and the entity does not exist in the target, and the provisioner is not configured to insert entities, then it is a DNE on the membership |
| Sync error: MAT | Error code: grouper object matches multiple target objects | If there is a data problem and multiple groups/entities/memberships in the target or in Grouper have the same matching data value, then the Grouper group/entity/membership will have a MAT error. Or if all matching attributes are null. |
| Sync error: MEM | Error code: group has no members, and configured to be not provisionable if not members | If a group is configured to no provision if no members, and it is provisionable and has no members, then this error occurs |
| Sync object | Grouper stores state about provisioning (target data, provisioning state, sync errors, logs, etc) in the database | Sync objects include: provisioner, jobs, groups,    entities, memberships, logs |
| Provisioning attributes   and metadata | Information that is internally stored in Grouper using the attribute framework that is used in provisioning. | If provisionable, ID/name to use in target,   flags used in target |
| Provisionable | If grouper data should be in the target | If a group is flagged as provisionable directly or from ancestor folder, if entities are in the right subject source or group or exist in target, etc |
| In target | If the grouper data is in the target | Generally this means Grouper provisioned the data but it could mean that the data already existed in the target |
| Target DAO | Data Access Object implementation of a Java contract that allows the provisioning framework   to select, insert, update, delete data from target | Run SQL queries in the SQL DAO. Run web    service calls in Azure DAO |
| Target DAO capabilities | List of DAO actions supported by this target DAO implementation | Maybe a target DAO for box cannot bulk delete   groups and needs to delete them individually |
| Provisioner configuration | Configuration the Grouper admin does when setting up a provisioner | e.g. specify the OU to look for groups in LDAP |
| Provisioner behaviors | Intersection of target DAO capabilities and provisioner configuration so the framework knows what to do | e.g. if a provisioner is not supposed to or cannot delete entities in the target |
| Membership sync vs object only | A group membership sync will sync the group and its members, whereas a group only sync will sync   the name and description etc of a group and not worry about the memberships. Entity membership sync applies here too |  |
| Inconsistent event | If the grouper or change log state does not match the sync object state. i.e. if you add and remove a member a few times right after one another, the change log state might not match the sync state. This will trigger a recalc on that membership |  |
| Provisioning object bean | There are ProvisioningGroup, ProvisioningEntity, ProvisioningMembership simple javabeans that hold   data in attributes |  |
| Provisioning object wrapper | For a group, entity, membership, there is an uber bean that holds references to the grouper state, translated state, target state, etc |  |
| Membership sync type | Memberships in target can be represented as objects, as group attributes, as entity attributes | e.g. in box memberships are objects, but in LDAP memberships are group attributes or entity attributes |
| Group target link | Maybe some data (e.g. DN or UUID) needs to be retrieved from a group object in the target. This data is "linked" to the membership generally by caching in a sync row in the database |  |
| Entity target link | Maybe some data (e.g. DN or UUID) needs to be retrieved from an entity object in the target. This data is "linked" to the membership generally by caching in a sync row in the database |  |
| Subject link | The target might refer to subjects as something other than the subject ID. So the subject needs to be resolved and that attribute needs to be used in provisioning | e.g. provision by EPPN but subject ID is a numeric employee ID |
| Sync object cache | ID's (or data) in the group target link, entity target link, or subject link, can be cached in the grouper sync objects in the database to help with deprovisioning when objects are deleted or for performance in non-recalc actions | cache the entity DN in the database |
| Translation | Grouper data is translated to target format in order to be able to retrieve data from target or to compare data | e.g. put the group name in the CN of the target group object |
| Attribute manipulation | Data attributes from grouper or the target originate in a certain format. These are manipulated based on provisioner configuration to change the type or assign a default value | e.g. change LDAP gidNumber from a string to an integer |
| Matching attribute(s) | An attribute (or composite key) in the group/entity/membership objects in the target format that can be used to match grouper objects with target objects | e.g. the gidNumber for posix groups |
| Search attribute(s) | An attribute used to search for an object in the target | e.g. gidNumber for posix groups, or the DN |
| Comparison | Target data and grouper data translated in target format are compared to see what actions need to occur in target to sync state |  |
| Membership replace | If the target does not support membership insert/delete, maybe it supports "replace". This is when you have to send the full membership list for a group or entity and it will be replaced with this new list. This is not the most efficient way to operate but some targets do that. |  |

**See also**

[Grouper Glossary](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541893/Grouper+glossary)
