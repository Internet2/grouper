---
title: "Grouper LDAP provisioner startWith scaffolding"
space: Grouper
pageId: 28559969
version: 30
lastUpdated: 2026-07-12T06:45:08.915Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28559969/Grouper+LDAP+provisioner+startWith+scaffolding
---

## Start with: Common LDAP pattern

First six fields can show one at a time when the previous is filled in

1. LDAP external system ID (required)
2. LDAP pattern: drop down with options below (show after external system selected)
  
  1. activeDirectoryGroups
    
    1. make sure the external system is marked as active directory
    2. init start with membership structure to groupAttributes
    3. init start with group object class to: top,group
    4. init start with group membership attribute to: member
    5. init start with membershipDn to true
    6. default start with to bushy type group structure
  2. bushyGroupsWithMembershipDNs
    
    1. init start with membership structure to groupAttributes
    2. init start with group organization to bushy
    3. init start with membershipDn to true
  3. bushyGroupsWithMembershipSubjectIds
    
    1. init user attribute type: core
    2. init start with membership structure to groupAttributes
    3. init start with group organization to bushy
    4. init start with membershipDn to false
    5. init start with membershipValue to subjectId
  4. flatGroupsWithMembershipDNs
    
    1. init start with membership structure to groupAttributes
    2. init start with group organization to flat
    3. init start with membershipDn to true
  5. flatGroupsWithMembershipSubjectIds
    
    1. init user attribute type: core
    2. init start with membership structure to groupAttributes
    3. init start with group organization to flat
    4. init start with membershipDn to false
    5. init start with membershipValue to subjectId
  6. groupOfNames
    
    1. init start with membership structure to groupAttributes
    2. init start with group object class to: top,groupOfNames
    3. init start with group membership attribute to: member
    4. init start with membershipDn to true
  7. other
    
    1. proceed with screen
  8. posixGroupsWithMembershipDNs
    
    1. init start with membership structure to groupAttributes
    2. init start with group object class to: top,posixGroup
    3. init start with group membership attribute to: member
    4. init start with membershipDn to true
    5. init start with idIndex attribute to gidNumber
  9. posixGroupsWithMembershipSubjectIds
    
    1. init start with membership structure to groupAttributes
    2. init start with group object class to: top,posixGroup
    3. init start with group membership attribute to: memberUid
    4. init start with idIndex attribute to gidNumber
    5. init start with membershipValue to subjectId
  10. usersWithEduPersonAffiliations 
    
    1. init start with membership structure to entityAttributes
    2. init start with membershipDn to false
    3. init start with membership attribute name to eduPersonAffiliation
    4. init start with membershipValue to extension
  11. usersWithEduPersonEntitlements 
    
    1. init start with membership structure to entityAttributes
    2. init start with membershipDn to false
    3. init start with membership attribute name to eduPersonEntitlement
    4. init start with membershipValue to name
  12. usersWithMembershipGroupExtensions 
    
    1. init start with membership structure to entityAttributes
    2. init start with membershipDn to false
    3. init start with membershipValue to extension
  13. usersWithMembershipGroupNames 
    
    1. init start with membership structure to entityAttributes
    2. init start with membershipDn to false
    3. init start with membershipValue to name
3. User attributes type: drop down with options, required (show after ldap pattern)
  
  1. Only uses built in core attributes e.g. subjectId, subjectIdentifier0-2, email, name, description or translations of those (value: core)
  2. Needs other subject source attributes (value: subjectSource)
  3. Needs a SQL or LDAP filter, but not other subject attributes. If LDAP this is a different LDAP than the LDAP provisioning to (value: entityResolver)
  4. Needs subject source source attributes and SQL or LDAP filter. If LDAP this is a different LDAP than the LDAP provisioning to (value: subjectSourceAndEntityResolver)
4. Membership structure (required no default, show when user attributes type filled in), groupAttributes or entityAttributes
5. membershipValue is DN? (true/false, required, no default, show after membership structure selected) (show rest of form after this filled in)
6. Group organization: bushy or flat (drop down required no default) (if groupAttributes membershipStructure) (show after membershipValueDn selected)
7. if (user attribute type is subjectSource or subjectSourceAndEntityResolver)
  
  1. list those attributes and validate against subject source (textfield, comma separated attributes, required)
8. if (not groupAttributes and not membershipValueDN), ask if need group link for another reason (boolean default false)
9. if (groupLink or groupAttributes or membershipValueDN): LDAP group info 
  
  1. group base OU, and validate against LDAP (required textfield)
  2. what attribute is RDN for groups? (add to attribute list if not in there) (required textfield)
  3. what is RDN value for groups? drop down required: extension, extensionUnderscoreIdIndex, idIndex, name, nameBackwardsUnderscoreMax64, other, script, uuid
  4. (if groupAttributes) membership attribute name
    
    1. (if not membershipValueDN): membership value (drop down required) (other, script, subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2)
  5. idIndex attribute: (optional textfield, add to attribute list if not in there)
  6. matching search attribute different than RDN or idIndex? boolean default false
  7. if (matching search attribute different than RDN or idIndex) matching search attribute name (textfield required)
  8. if (matching search attribute different than RDN or idIndex) matching search attribute value (dropdown required: extension, idIndex, name, other, script, uuid)
  9. object classes for groups (optional textfield, comma separated)
  10. list other group ldap attributes (not configured yet): (optional textfield comma separated)
  11. ask if allow group DN override (boolean default false)
10. if (not entityAttributes and not membershipValueDN), ask if need entity link for another reason (boolean default false)
11. if membershipValueDN or entityLink or entityAttributes: LDAP entity info
  
  1. entity base OU, and validate against LDAP (required textfield)
  2. change entities in LDAP (besides entity attribute if doing entity attributes)? (boolean default false)
  3. (if change entities) RDN attribute for entities (required textfield)
  4. (if change entities) RDN value for entities (drop down required) (other, script, subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2)
  5. (if entityAttributes) membership attribute name
    
    1. (if not membershipValueDN): membership value (drop down required) (dropdown required: extension, idIndex, name, other, script, uuid)
  6. (if change entities) matching search attribute different than RDN? boolean default false
  7. (if not change entities or matching search attribute different than RDN) matching/search attribute name (required textfield)
  8. (if not change entities or matching search attribute different than RDN) matching/search attribute value (drop down required) (other, script, subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2)
  9. object classes for entities (optional textfield, comma separated)
  10. list other entity ldap attributes (not configured yet): (optional textfield)
  11. if entityAttributes and not membershipValueDN, ask if allow membership value override (boolean default false)
12. add disabled full sync daemon? boolean default to true
13. add disabled incremental sync daemon? boolean default to true
