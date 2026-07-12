---
title: "Grouper SQL provisioner startWith scaffolding"
space: Grouper
pageId: 28560202
version: 15
lastUpdated: 2026-07-01T05:36:04.225Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560202/Grouper+SQL+provisioner+startWith+scaffolding
---

## Start with: Common SQL pattern

First six fields can show one at a time when the previous is filled in

1. SQL external system ID (required)
2. SQL pattern: drop down with options below (show after external system selected)
  
  1. entityTable
    
    1. init membership structure to notApplicable
    2. init start with entity table to true
    3. init start with entity table name to: from_grouper_entity
    4. init start with entity primary key column: entity_uuid
    5. init start with entity primary key value: entityUuid
    6. init start with entity other columns as: subject_id
    7. init start with entity link false
  2. entityTableWithAttributeTable
    
    1. init membership structure to notApplicable
    2. same as entity table init
    3. entity attribute table name init to from_grouper_entity_attr
    4. foreign key to entity table entity_uuid
    5. column name which is the attribute name: attribute_name
    6. column name which is the attribute value: attribute_value
  3. entityTableWithAttributeTableAndMemberships
    
    1. same as entity table init
    2. same as entitiesTableWithAttributesTable
    3. membership structure: entityAttributes
    4. membership attribute: memberOf
    5. init entityLink to true
  4. entityTableMembershipTable
    
    1. same as entityTable init
    2. same as membershipTable init
    3. init start with membership table entity column name: entity_uuid (not subject_id)
    4. init start with membership table entity column value: entityUuid (not subjectId)
    5. init entityLink to true
  5. groupTable
    
    1. init membership structure to notApplicable
    2. init start with group table to true
    3. init start with group table name to: from_grouper_group
    4. init start with group primary key column: group_id_index
    5. init start with group other columns as: groupIdIndex
    6. init start with group link false
  6. groupTableWithAttributeTable
    
    1. same as groupTable init
    2. group attribute table name init to from_grouper_group_attr
    3. foreign key to entity table entity_id_index
    4. column name which is the attribute name: attribute_name
    5. column name which is the attribute value: attribute_value
  7. groupTableWithAttributeTableAndMemberships
    
    1. same as groupTableWithAttributeTable init
    2. membership structure: groupAttributes
    3. membership attribute: hasMember
    4. init groupLink to true
  8. groupTableMembershipTable
    
    1. same as groupTable init
    2. same as membershipTable init
    3. init start with membership table group column name: group_id_index (not group_name)
    4. init start with membership table group column value: groupIdIndex (not groupName)
    5. init groupLink to true
  9. groupTableEntityTableMembershipTable
    
    1. same as groupTable init
    2. same as entityTable init
    3. same as membershipTable init
    4. membership table group column is: group_id_index
    5. membership table group value is: groupIdIndex
    6. membership table entity column is: entity_uuid
    7. membership table entity value is: entityUuid
    8. init groupLink to true
    9. init entityLink to true
  10. membershipTable
    
    1. membership structure: membershipObjects
    2. init start with membership table to true
    3. init start with membership table name to: from_grouper_mship
    4. init start with group column: group_name
    5. init start with group value: groupName
    6. init start with entity column: subject_id
    7. init start with entity value: subjectId
    8. init groupLink to false
    9. init entityLink to false
  11. other
    
    1. proceed with screen
3. User attributes type: drop down with options, required (show after ldap pattern)
  
  1. Only uses built in core attributes e.g. subjectId, subjectIdentifier0-2, email, name, description or translations of those (value: core)
  2. Needs other subject source attributes (value: subjectSource)
  3. Needs a SQL or LDAP filter, but not other subject attributes. If LDAP this is a different LDAP than the LDAP provisioning to (value: entityResolver)
  4. Needs subject source source attributes and SQL or LDAP filter. If LDAP this is a different LDAP than the LDAP provisioning to (value: subjectSourceAndEntityResolver)
4. Membership structure (default to membershipObjects): entityAttributes, groupAttributes, membershipObjects, notApplicable
5. if (user attribute type is subjectSource or subjectSourceAndEntityResolver)
  
  1. list those attributes and validate against subject source (textfield, comma separated attributes, required)
6. Ask if has group table (default false)
  
  1. If has group table, ask for group table name (required, validate on submit) (suggest: from_grouper_group)
  2. If has group table, ask for group primary key column (suggest group_id_index) (required, validate on submit)
  3. If has group table, ask for group primary key value (suggest groupIdIndex) (drop down required) (other, script, groupExtension, groupIdIndex, groupName, groupPrimaryKey, groupUuid)
  4. If has group table, ask for list of other group columns, suggest names: group_uuid, group_name, group_id_index, group_extension, group_display_name, group_display_extension, group_description
  5. Ask if need group link (optional, grouper will try to deduce this)
  6. If has group table, ask if has group attribute table (to store multi-valued attributes or memberships, not common)
    
    1. If has group attribute table, ask for group attribute table name (required, validate on submit)
    2. If has group attribute table, ask for column name which is foreign key to group table (suggest group_uuid) (required, validate on submit)
    3. If has group attribute table, ask for column name which is the attribute name (suggest attribute_name) (required, validate on submit)
    4. If has group attribute table, ask for column name which is the attribute value (suggest attribute_value) (required, validate on submit)
    5. If groupAttributes membership structure, ask for the membership attribute name (required)
    6. If groupAttributes membership structure, ask for membership value (drop down required) (entityPrimaryKey, other, script, subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2)
    7. If has group attribute table, ask for other attribute names in attribute table (optional)
7. Ask if has entity table (default false)
  
  1. If has entity table, ask for entity table name (required, validate on submit)
  2. If has entity table, ask for entity primary key column (suggest entity_uuid) (required, validate on submit)
  3. If has entity table, ask for entity primary key value (suggest entityUuid) (drop down required) (other, script, email, entity_uuid, entity_description, entity_name, subject_id, subject_identifier0, subject_identifier1, subject_identifier2)
  4. If has entity table, ask for list of other entity columns, suggest names: email, entity_uuid, entity_description, entity_name, subject_id, subject_identifier0, subject_identifier1, subject_identifier2
  5. Ask if need entity link (boolean optional grouper will try to deduce this)
  6. If has entity table, ask if has entity attribute table (to store multi-valued attributes or memberships, not common)
    
    1. If has entity attribute table, ask for entity attribute table name (required, validate on submit)
    2. If has entity attribute table, ask for column name which is foreign key to entity table (suggest entity_uuid) (required, validate on submit)
    3. If has entity attribute table, ask for column name which is the attribute name (suggest attribute_name) (required, validate on submit)
    4. If has entity attribute table, ask for column name which is the attribute value (suggest attribute_value) (required, validate on submit)
    5. If entityAttributes membership structure, ask for the membership attribute name (required)
    6. If entityAttributes membership structure, ask for membership value (drop down required) (other, script, groupExtension, groupIdIndex, groupName, groupPrimaryKey, groupUuid)
    7. If has entity attribute table, ask for other attribute names in attribute table (optional)
8. Ask if has membership table (default false)
  
  1. Ask for membership table name: (suggest: from_grouper_mship)
  2. Ask for group column (required, validate on submit)
  3. Ask for group value (drop down required) (other, script, groupExtension, groupIdIndex, groupName, groupPrimaryKey, groupUuid)
  4. Ask for entity column (required, validate on submit)
  5. Ask for entity value (drop down required) (entityPrimaryKey, other, script, subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2)
9. Add disabled full sync daemon? boolean default to true
10. Add disabled incremental sync daemon? boolean default to true
