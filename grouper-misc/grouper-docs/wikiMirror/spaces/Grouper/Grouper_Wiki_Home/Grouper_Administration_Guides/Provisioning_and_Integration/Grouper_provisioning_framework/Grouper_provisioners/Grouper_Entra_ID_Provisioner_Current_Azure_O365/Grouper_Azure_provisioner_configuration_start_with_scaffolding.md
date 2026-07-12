---
title: "Grouper Azure provisioner configuration start with scaffolding"
space: Grouper
pageId: 28560381
version: 4
lastUpdated: 2026-07-01T05:35:39.618Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560381/Grouper+Azure+provisioner+configuration+start+with+scaffolding
---

## Start with: Common Azure pattern

First few fields can show one at a time when the previous is filled in

1. Azure external system ID (required)
2. Azure pattern: drop down with options below (show after external system selected)
  
  1. manageGroupsManageEntities
    
    1. default start with manage entities true
  2. manageGroupsReadonlyEntities
    
    1. default start with manage entities false
  3. other
    
    1. proceed with screen
3. User attributes type: drop down with options, required (show after ldap pattern)
  
  1. Only uses built in core attributes e.g. subjectId, subjectIdentifier0-2, email, name, description or translations of those (value: core)
  2. Needs other subject source attributes (value: subjectSource)
  3. Needs a SQL or LDAP filter, but not other subject attributes. (value: entityResolver)
  4. Needs subject source source attributes and SQL or LDAP filter. (value: subjectSourceAndEntityResolver)
4. if (user attribute type is subjectSource or subjectSourceAndEntityResolver)
  
  1. list those attributes and validate against subject source (textfield, comma separated attributes, required)
5. Azure group info 
  
  1. displayName attribute value (dropdown required: extension, idIndex, name, other, script, uuid)
  2. use group description? boolean default true
  3. mailNickname attribute value (dropdown required: extension, idIndex, name, other, script, uuid)
  4. has metadata for 'group type'? boolean default true
  5. has metadata for allowOnlyMembersToPost? boolean default false
  6. has metadata for hideGroupInOutlook? boolean default false
  7. has metadata for subscribeNewGroupMembers? boolean default false
  8. has metadata for welcomeEmailDisabled? boolean default false
  9. has metadata for resourceProvisioningOptionsTeams? boolean default false
6. Azure entity info
  
  1. Entity user principal name (drop down not required) (other, script, subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2)
  2. Entity mail nickname (drop down not required) (other, script, subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2)
  3. Entity on premises immutable ID (drop down not required) (other, script, subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2) (validate that at least one of user principal name, on prem immutable id, mail nickname is selected)
  4. Manage entities in Azure? (boolean default false)
  5. (if manage entities) Entity display name (drop down required) (name, none, other, script, subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2)
7. add disabled full sync daemon? boolean default to true
8. add disabled incremental sync daemon? boolean default to true
