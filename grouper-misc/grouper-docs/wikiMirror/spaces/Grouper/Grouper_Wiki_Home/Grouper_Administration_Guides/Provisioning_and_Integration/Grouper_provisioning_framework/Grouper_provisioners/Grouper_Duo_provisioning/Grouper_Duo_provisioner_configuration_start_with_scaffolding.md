---
title: "Grouper Duo provisioner configuration start with scaffolding"
space: Grouper
pageId: 28559975
version: 2
lastUpdated: 2026-07-01T05:36:35.581Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28559975/Grouper+Duo+provisioner+configuration+start+with+scaffolding
---

## Start with: Common Duo pattern

First few fields can show one at a time when the previous is filled in

1. Duo external system ID (required)
2. Duo pattern: drop down with options below (show after external system selected)
  
  1. manageGroupsManageEntities
    
    1. default start with manage entities true
    2. default start with manage groups true
  2. manageGroupsReadonlyEntities
    
    1. default start with manage entities false
    2. default start with manage groups true
  3. manageEntities
    
    1. default start with manage entities true
    2. default start with manage groups false
  4. other
    
    1. proceed with screen
3. User attributes type: drop down with options, required (show after ldap pattern)
  
  1. Only uses built in core attributes e.g. subjectId, subjectIdentifier0-2, email, name, description or translations of those (value: core)
  2. Needs other subject source attributes (value: subjectSource)
  3. Needs a SQL or LDAP filter, but not other subject attributes. (value: entityResolver)
  4. Needs subject source source attributes and SQL or LDAP filter. (value: subjectSourceAndEntityResolver)
4. if (user attribute type is subjectSource or subjectSourceAndEntityResolver)
  
  1. list those attributes and validate against subject source (textfield, comma separated attributes, required)
5. Manage groups? (default false)
  
  1. If manage groups: Duo group info 
    
    1. name attribute value (dropdown required: extension, idIndex, name, other, script, uuid)
    2. use group description? boolean default true
6. Manage entities? (default false)
  
  1. If manage entities: Duo entity info
    
    1. Entity username (drop down required) (other, script, subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2)
    2. Entity name subject attribute or entity resolver name (textfield) (optional)
    3. Entity first name subject attribute or entity resolver name (textfield) (optional)
    4. Entity email subject attribute or entity resolver name (textfield) (optional)
7. add disabled full sync daemon? boolean default to true
8. add disabled incremental sync daemon? boolean default to true
