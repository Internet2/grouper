---
title: "Grouper entity attribute resolver"
space: Grouper
pageId: 28555599
version: 28
lastUpdated: 2026-07-01T05:37:49.872Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555599/Grouper+entity+attribute+resolver
---

There are built in entity attributes (subject id, identifier, name, description), subject attributes (anything you configure in your subject source, e.g. firstName), but there could be other entity data you need to provision or access for reports or other reasons.

In v2.6.6 of Grouper you can resolve entity attributes not available in Grouper or the subject source. Each provisioner could have one SQL resolver, one LDAP resolver, or both. Also you can configure "global" entity attribute resolvers which could be used as well.

On a full provisioning run get all users and full sync the translations.

On an incremental sync, get users with last_updated changes (if applicable, not implemented yet) and process those entities. Any membership changes will bulk select all the users and process them.

When translating, SQL entity data will be available in expressions as: entityAttributeResolverSql__column_name where column name is all lower case. e.g. ${entityAttributeResolverSql__org_department} if the column is "org_department")

When translating, LDAP entity data will be available in expressions as: entityAttributeResolverLdap__attribute_name where attribute name is all lower case. e.g. ${entityAttributeResolverLdap__samaccountname} if the attribute is "samaccountname")

Note that if you want incremental to pick up new entity attributes, LDAP needs to have the modifyTimestamp (not implemented yet), or you could make an LDAP to SQL job (e.g. every 30 minutes) and then a trigger could maintain last_updated

## Global entity attribute resolvers list page

## Global entity attribute resolvers add SQL resolver page

## Configuration options

Configuration section: Do you have entity attributes not in the subject source? True/False (default false)

If true:

| Config item | Value | Show if | Description |
| --- | --- | --- | --- |
| Show entity attribute resolver | true/false |  | Have a separate section just like Membership configuration and it shows up before Membership configuration section |
| Resolve attributes with SQL | true/false | showEntityAttributeResolver == 'true' | If true show the next section |
| Use global SQL resolver | true/false | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql == 'true' | (default false), if true then use a global resolver |
| Global SQL resolver | myPeopleResolver | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql == 'true'    && useGlobalSqlResolver == 'true' | Drop down of global SQL resolvers |
| SQL config id | warehouse | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql == 'true'    && useGlobalSqlResolver == 'false' | Drop down with SQL config ids - Db external system config ids dropdown |
| Table or view name | my_people | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql == 'true'    && useGlobalSqlResolver == 'false' | Table of user data, must have a subject source (optional), and matching/search col (required), and columns with single valued attributes |
| Attribute names | name,description | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql == 'true'    && useGlobalSqlResolver == 'false' | Comma separated list of columns you want to fetch and send to target system as attributes. |
| Subject source id column | subject_source_id | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql = 'true'    && useGlobalSqlResolver == 'false' | The subject source id column (optional) |
| Subject search / matching column | employee_id | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql == 'true'    && useGlobalSqlResolver == 'false' | Column that searches and matches an entity |
| SQL mapping type | entityAttribute / translation | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql == 'true'    && useGlobalSqlResolver == 'false' | Drop down of the mapping type |
| SQL mapping entity attribute | subjectId | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql == 'true'    && useGlobalSqlResolver == 'false'   && sqlMappingType == 'entityAttribute' | If this is an entity attribute mapping type, pick the entity attribute from a drop down |
| SQL mapping expression | ${grouperProvisioningEntity.retrieveAttributeValueString('uid')} | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql == 'true'    && useGlobalSqlResolver == 'false'   && sqlMappingType == 'translation' | If this is a translation write the expression (unescaped) (useGlobalResolver='false') |
| Last updated column | last_updated | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql == 'true'    && useGlobalSqlResolver == 'false' | If this is provided then the incremental provisioner will process people that have been recently updated (useGlobalResolver='false') |
| Last updated type | timestamp | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql == 'true'    && useGlobalSqlResolver == 'false' | Could be timestamp, millisSince1970 (useGlobalResolver='false') |
| Select all SQL on full | true/false | showEntityAttributeResolver == 'true'    && resolveAttributesWithSql == 'true' | (Default true), if select * from the table should occur on full runs. Set to false if only a small subset of the  total entities in the table are provisionable (show for local or global resolver) |
| Resolve attributes with LDAP | true/false | showEntityAttributeResolver == 'true' | If true show the next section |
| Use global LDAP resolver | true/false | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true' | (default false), if true then use a global resolver |
| Global LDAP resolver | myPeopleResolver | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'    && useGlobalLdapResolver == 'true' | Drop down of global LDAP resolvers |
| LDAP | myAd | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'   && useGlobalLdapResolver == 'false' | Drop down with LDAP config ids |
| Base DN | OU=users,DC=school,DC=edu | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'   && useGlobalLdapResolver == 'false' | Base DN for search |
| Search scope | ONELEVEL_SCOPE, or SUBTREE_SCOPE (default) | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'   && useGlobalLdapResolver == 'false' |  |
| Filter part | (objectClass=person) | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'   && useGlobalLdapResolver == 'false' | If provided, this will be part of the full or individual filter |
| Attributes | employeeID, name, org, extensionAttribute11, modifyTimestamp | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'   && useGlobalLdapResolver == 'false' | Attributes to retrieve (multi-valued attributes will be stored in appropriate structure) |
| Multivalued ldap attributes | org,name | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'   && useGlobalLdapResolver == 'false' | Comma separated list of attributes that are multivalued |
| LDAP matching / search attribute | employeeID | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'   && useGlobalLdapResolver == 'false' | LDAP attribute which is used to lookup and match an entity in Grouper |
| LDAP mapping type | entityAttribute / translation | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'   && useGlobalLdapResolver == 'false' | Drop down of the mapping type |
| LDAP mapping entity attribute | subjectId | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'   && useGlobalLdapResolver == 'false' | If this is an entity attribute mapping type, pick the entity attribute from a drop down |
| LDAP matching expression | ${grouperProvisioningEntity.retrieveAttributeValueString('uid')} | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'   && useGlobalLdapResolver == 'false' | The value in Grouper that matches the LDAP data. This is not yet ldap escaped. In this case the filter to get one record would be generated as:  (&(employeeID=${grouperUtil.ldapFilterEscape(grouperProvisioningEntity.retrieveAttributeValueString('uid'))})(objectClass=person)) |
| Filter all LDAP on full | true/false | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true' | (Default true), if full filter should occur on full runs. Set to false if only a small subset of the  total entities in the table are provisionable. In the above example, if this is true, the full filter would be:  (&(employeeID=*)(objectClass=person)) |
| Last updated attribute | modifyTimestamp | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'   && useGlobalLdapResolver == 'false' | If provided the incremental can poll for new records to process. e.g. the filter would be (openldap / edirectory)  (&(employeeID=*)(objectClass=person)(modifyTimestamp>=20211119082103Z))  Active directory  (&(employeeID=*)(objectClass=person)(modifyTimestamp>= 20211119163324.0Z)) |
| LDAP last updated format | default / activeDirectory | showEntityAttributeResolver == 'true'    && resolveAttributesWithLdap == 'true'   && useGlobalLdapResolver == 'false' | This is optional, if not selected it will select default 20211119082103Z for a non AD connection and activeDirectory 20211119163324.0Z for  an active directory connection (which is selected in the external system) |

## Global entity attribute resolvers

If you have global attribute resolvers (can be shared among provisioners or maybe other things too), configure in grouper.properties

```
######################################
## Global entity attribute resolvers
## These SQL or LDAP attribute resolvers could be used in multiple provisioners or other areas
## entityAttributeResolverId is the unique configId for the attribute resolver
######################################
 
# Entity attribute resolver type
# {valueType: "string", order: 1000, regex: "^entityAttributeResolver\\.([^.]+)\\.resolverType$", required: true, formElement: "dropdown", optionValues: ["sql", "ldap"]}
# entityAttributeResolver.entityAttributeResolverId.resolverType =

# if this resolver is enabled
# {valueType: "boolean", defaultValue: "true", order: 2000 }
# entityAttributeResolver.entityAttributeResolverId.enabled =
 
# SQL configId for database connection, default to grouper database
# {valueType: "string", order: 3000, regex: "^entityAttributeResolver\\.([^.]+)\\.sqlConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem", showEl: "${resolverType == 'sql'}"}
# entityAttributeResolver.entityAttributeResolverId.sqlConfigId =
 
# Table of user data, must have a subject source (optional), and matching/search col (required), and columns with single valued attributes
# {valueType: "string", order: 4000, regex: "^entityAttributeResolver\\.([^.]+)\\.tableOrViewName$", required: true, showEl: "${resolverType == 'sql'}"}
# entityAttributeResolver.entityAttributeResolverId.tableOrViewName =

# Comma separated column names from the entity attributes table that need to be added as attributes in the target system
# {valueType: "string", order: 4500, regex: "^entityAttributeResolver\\.([^.]+)\\.columnNames$", required: true, showEl: "${resolverType == 'sql'}"}
# entityAttributeResolver.entityAttributeResolverId.columnNames =
 
# The subject source id column
# {valueType: "string", order: 5000, regex: "^entityAttributeResolver\\.([^.]+)\\.subjectSourceIdColumn$", showEl: "${resolverType == 'sql'}"}
# entityAttributeResolver.entityAttributeResolverId.subjectSourceIdColumn =
 
# Column that searches and matches an entity
# {valueType: "string", order: 6000, regex: "^entityAttributeResolver\\.([^.]+)\\.subjectSearchMatchingColumn$", required: true, showEl: "${resolverType == 'sql'}"}
# entityAttributeResolver.entityAttributeResolverId.subjectSearchMatchingColumn =
  
# Grouper attribute that matches the row
# {valueType: "string", order: 7000, regex: "^entityAttributeResolver\\.([^.]+)\\.grouperAttributeThatMatchesRow$", required: true, showEl: "${resolverType == 'sql'}", formElement: "dropdown", optionValues: ['subjectId', 'subjectIdentifer0']}
# entityAttributeResolver.entityAttributeResolverId.grouperAttributeThatMatchesRow =
  
# The last updated column, e.g. a timestamp or number field (number of millis since 1970)
# {valueType: "string", order: 8000, regex: "^entityAttributeResolver\\.([^.]+)\\.lastUpdatedColumn$", showEl: "${resolverType == 'sql'}"}
# entityAttributeResolver.entityAttributeResolverId.lastUpdatedColumn =
   
# The last updated column type, e.g. timestamp, millisSince1970.  If this is provided then the incremental provisioner will process people that have been recently updated
# {valueType: "string", order: 9000, regex: "^entityAttributeResolver\\.([^.]+)\\.lastUpdatedColumn$", showEl: "${resolverType == 'sql'}", formElement: "dropdown", optionValues: ['timestamp', 'millisSince1970']}
# entityAttributeResolver.entityAttributeResolverId.lastUpdatedType =
 
# LDAP configId for connection
# {valueType: "string", order: 10000, regex: "^entityAttributeResolver\\.([^.]+)\\.ldapConfigId$", required: true, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.externalSystem.LdapGrouperExternalSystem", showEl: "${resolverType == 'ldap'}"}
#entityAttributeResolver.entityAttributeResolverId.ldapConfigId =
   
# Base DN for search
# {valueType: "string", order: 11000, regex: "^entityAttributeResolver\\.([^.]+)\\.baseDn$", showEl: "${resolverType == 'ldap'}", required: true}
# entityAttributeResolver.entityAttributeResolverId.baseDn =
 
# Search scope, default is SUBTREE_SCOPE
# {valueType: "string", order: 12000, regex: "^entityAttributeResolver\\.([^.]+)\\.searchScope$", showEl: "${resolverType == 'ldap'}", required: true, formElement: "dropdown", optionValues: ['ONELEVEL_SCOPE', ' SUBTREE_SCOPE']}
# entityAttributeResolver.entityAttributeResolverId.searchScope =
  
# If there is more to the filter for all users or selected user than just the search attribute, then put that here.  e.g. if you have a search attribute of employeeID, and you want the
# filter to be (&(employeeID=12345678)(objectClass=person)) then you should fill in this value as: (objectClass=person)
# {valueType: "string", order: 13000, regex: "^entityAttributeResolver\\.([^.]+)\\.filterPart$", showEl: "${resolverType == 'ldap'}"}
# entityAttributeResolver.entityAttributeResolverId.filterPart =
   
# Attributes to retrieve (multi-valued attributes will be stored in appropriate structure)
# {valueType: "string", order: 14000, regex: "^entityAttributeResolver\\.([^.]+)\\.ldapAttributes$", showEl: "${resolverType == 'ldap'}", required: true}
# entityAttributeResolver.entityAttributeResolverId.ldapAttributes =

# Which attributes are multivalued
# {valueType: "string", order: 14500, regex: "^entityAttributeResolver\\.([^.]+)\\.multiValuedLdapAttributes$", showEl: "${resolverType == 'ldap'}"}
# entityAttributeResolver.entityAttributeResolverId.multiValuedLdapAttributes =
    
# LDAP matching / search attribute, this needs to be the same as the subject ID or subject identifier0
# {valueType: "string", order: 15000, regex: "^entityAttributeResolver\\.([^.]+)\\.subjectSearchMatchingAttribute$", showEl: "${resolverType == 'ldap'}", required: true}
# entityAttributeResolver.entityAttributeResolverId.subjectSearchMatchingAttribute =
   
# Grouper attribute that matches the record
# {valueType: "string", order: 16000, regex: "^entityAttributeResolver\\.([^.]+)\\.grouperAttributeThatMatchesRecord$", required: true, showEl: "${resolverType == 'ldap'}", formElement: "dropdown", optionValues: ['subjectId', 'subjectIdentifer0']}
# entityAttributeResolver.entityAttributeResolverId.grouperAttributeThatMatchesRecord =
 
# If provided the incremental can poll for new records to process.  e.g. the filter would be (openldap / edirectory) (modifyTimestamp>=20211119082103Z), or Active Directory: (modifyTimestamp>= 20211119163324.0Z)
# {valueType: "string", order: 17000, regex: "^entityAttributeResolver\\.([^.]+)\\.lastUpdatedAttribute$", showEl: "${resolverType == 'ldap'}"}
# entityAttributeResolver.entityAttributeResolverId.lastUpdatedAttribute = 
  
# This is optional, if not selected it will select default 20211119082103Z for a non AD connection and activeDirectory 20211119163324.0Z for
# an active directory connection (which is selected in the external system)
# {valueType: "string", order: 18000, regex: "^entityAttributeResolver\\.([^.]+)\\.ldapLastUpdatedFormat$", showEl: "${resolverType == 'ldap'}", formElement: "dropdown", optionValues: ['default', 'activeDirectory']}
# entityAttributeResolver.entityAttributeResolverId.ldapLastUpdatedFormat =   
```
