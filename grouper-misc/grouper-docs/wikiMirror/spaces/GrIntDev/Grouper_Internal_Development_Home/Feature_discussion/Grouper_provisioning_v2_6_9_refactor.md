---
title: "Grouper provisioning v2.6.9 refactor"
space: GrIntDev
pageId: 48793085
version: 21
lastUpdated: 2026-07-12T06:46:04.222Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793085/Grouper+provisioning+v2.6.9+refactor
---

## 

## Upgrade provisioning framework provisioners

There is a GSH program to upgrade

1. If you have no provisioning framework provisioners there is nothing to do
2. Turn off daemon server
3. Upgrade
4. GSH in another server
5. Run
  
  
  ```
  XXX fill in...
  ```

## The goal

Make provisioning

1. Easier to configure - take away complexity
2. More difficult to do the wrong thing - organize options so that some types of invalid configs are not possible to configure
3. Simpler - show fewer options on the screen unless they are needed
4. More explicit - when Grouper makes assumptions be explicit about what they are (e.g. show the LDAP search filter that is generated)

## Changes made

After experience from the field and seeing what can be optimized in provisioning, these changes will be made:

1. (IN PROGRESS) Move advanced options to each section (if applicable)
2. (DONE) Make sure advanced is last (currently failsafe is last)
3. (DONE) Defaults for CRUD at object level
  
  1. Insert/Update/Select groups
  2. Delete groups that Grouper created
  3. Insert/Update/Select entities
  4. Delete entities that Grouper created
  5. Insert/Update/Select memberships
  6. Delete memberships that Grouper created
4. (IN PROGRESS) Defaults for CRUD at attribute level
  
  1. If the object selects, the attribute selects
  2. If the object inserts, the attribute inserts
  3. If the object updates, the attribute updates
5. (DONE)There is no concept of field anymore, only attribute
  
  1. (DONE)The notation (e.g.) grouperProvisioningGroup.name will still be supported
6. Multiple matching/search attributes, prioritized
  
  1. Dao can search for all at once or each at a time, the input will specify the search attribute(s)
7. Matching and search attributes will be configured after attributes are configured
  
  1. Pick how many, select each one with drop down
8. Validations will have an opening option as to whether validations are customized (dont show by default)
9. (DONE) LDAP DN is not an attribute: ldap_dn (instead of field name)
10. LDAP automatic settings will show the setting and ask if should be customized (e.g. DN)
  
  1. LDAP DN should be based on RDN
11. (DONE) groupToId2 etc will just be named groupAttributeValueCache0-3
  
  1. Can be configured below attributes in the groupLink section
12. (DONE) Refactor entity attribute resolver config
  
  1. Configs should have a hierarchical prefix on the config suffix
  2. FROM provisioner.genericProvisioner.sqlConfigId to provisioner.genericProvisioner.entityResolver.sqlConfigId
13. Easy way to massage characters
  
  1. How many character substitutions
  2. Substitute a regex (or lack of regex)
  3. For a string
  4. Which objects / attributes to apply to
14. (DONE) Multi-valued attributes are "sets" and not "arrays"
15. (DONE) Rename classes and methods to be consistent (e.g. FROM retrieveGrouperTranslator TO retrieveGrouperProvisioningTranslator)
16. (DONE) Adjust defaults to make sense or rename configs so the default is intended. e.g. retrieve all entities in full sync should default to true
  
  1. Upgrade step for that???
17. Screen should flow from top to bottom. You shouldnt need to scroll up to see an option of configure an option
  
  1. (DONE) Sources to provision should be in the entity section
18. Translate from object or sync object in one fell swoop
19. Add indent level to json metadata and indent on screen
