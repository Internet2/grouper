---
title: "Grouper provisioning SCIM ServiceNow"
space: Grouper
pageId: 28564299
version: 6
lastUpdated: 2026-07-01T05:35:21.441Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564299/Grouper+provisioning+SCIM+ServiceNow
---

You can provision to Service Now with SCIM and custom attributes. Note: set the page size to 500 for users and groups in the external system  
  
[https://docs.servicenow.com/bundle/washingtondc-api-reference/page/integrate/inbound-rest/concept/scim-api.html](https://docs.servicenow.com/bundle/washingtondc-api-reference/page/integrate/inbound-rest/concept/scim-api.html)

## Configure provisioner to load data from Service Now

```
provisioner.serviceNowProvisioner.loadEntitiesToGrouperTable = true
```

Note you need to map the attributes you want to load and the uuids you want to dereference

```
provisioner.serviceNowProvisioner.targetEntityAttribute.10.entityAttributeJsonPointer = /urn:ietf:params:scim:schemas:extension:servicenow:2.0:User/department/name
provisioner.serviceNowProvisioner.targetEntityAttribute.10.name.elConfig = ${"department_name"}
provisioner.serviceNowProvisioner.targetEntityAttribute.10.showAdvancedAttribute = true
provisioner.serviceNowProvisioner.targetEntityAttribute.10.showAttributeValueSettings = true

```

```
provisioner.serviceNowProvisioner.acceptHeader = application/scim+json
provisioner.serviceNowProvisioner.bearerTokenExternalSystemConfigId = serviceNowReal
provisioner.serviceNowProvisioner.class = edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner
provisioner.serviceNowProvisioner.debugLog = true
provisioner.serviceNowProvisioner.entity2advanced = true
provisioner.serviceNowProvisioner.entityAttributeValueCache0has = true
provisioner.serviceNowProvisioner.entityAttributeValueCache0nullChecksInScript = true
provisioner.serviceNowProvisioner.entityAttributeValueCache0source = grouper
provisioner.serviceNowProvisioner.entityAttributeValueCache0translationContinueCondition = ${subject != null}
provisioner.serviceNowProvisioner.entityAttributeValueCache0translationScript = ${subject.getAttributeValue('email')}
provisioner.serviceNowProvisioner.entityAttributeValueCache0type = subjectTranslationScript
provisioner.serviceNowProvisioner.entityAttributeValueCache2entityAttribute = id
provisioner.serviceNowProvisioner.entityAttributeValueCache2has = true
provisioner.serviceNowProvisioner.entityAttributeValueCache2source = target
provisioner.serviceNowProvisioner.entityAttributeValueCache2type = entityAttribute
provisioner.serviceNowProvisioner.entityAttributeValueCacheHas = true
provisioner.serviceNowProvisioner.entityMatchingAttribute0name = userName
provisioner.serviceNowProvisioner.entityMatchingAttributeCount = 1
provisioner.serviceNowProvisioner.entityResolver.columnNames = user_name, department_uuid, company_uuid, manager_uuid
provisioner.serviceNowProvisioner.entityResolver.entityAttributesNotInSubjectSource = true
provisioner.serviceNowProvisioner.entityResolver.resolveAttributesWithSQL = true
provisioner.serviceNowProvisioner.entityResolver.sqlConfigId = grouper
provisioner.serviceNowProvisioner.entityResolver.sqlMappingEntityAttribute = subjectId
provisioner.serviceNowProvisioner.entityResolver.sqlMappingType = entityAttribute
provisioner.serviceNowProvisioner.entityResolver.subjectSearchMatchingColumn = user_name
provisioner.serviceNowProvisioner.entityResolver.subjectSourceIdColumn = jdbc
provisioner.serviceNowProvisioner.entityResolver.tableOrViewName = my_payroll_data_v
provisioner.serviceNowProvisioner.groupAttributeValueCache0groupAttribute = id
provisioner.serviceNowProvisioner.groupAttributeValueCache0has = true
provisioner.serviceNowProvisioner.groupAttributeValueCache0source = target
provisioner.serviceNowProvisioner.groupAttributeValueCache0type = groupAttribute
provisioner.serviceNowProvisioner.groupAttributeValueCache1groupAttribute = displayName
provisioner.serviceNowProvisioner.groupAttributeValueCache1has = true
provisioner.serviceNowProvisioner.groupAttributeValueCache1source = target
provisioner.serviceNowProvisioner.groupAttributeValueCache1type = groupAttribute
provisioner.serviceNowProvisioner.groupAttributeValueCacheHas = true
provisioner.serviceNowProvisioner.groupMatchingAttribute0name = displayName
provisioner.serviceNowProvisioner.groupMatchingAttribute1name = id
provisioner.serviceNowProvisioner.groupMatchingAttributeCount = 2
provisioner.serviceNowProvisioner.hasTargetEntityLink = true
provisioner.serviceNowProvisioner.includeActiveOnGroupCreate = false
provisioner.serviceNowProvisioner.loadEntitiesToGrouperTable = true
provisioner.serviceNowProvisioner.logAllObjectsVerbose = true
provisioner.serviceNowProvisioner.logCommandsAlways = false
provisioner.serviceNowProvisioner.makeChangesToEntities = true
provisioner.serviceNowProvisioner.numberOfEntityAttributes = 13
provisioner.serviceNowProvisioner.numberOfGroupAttributes = 2
provisioner.serviceNowProvisioner.operateOnGrouperEntities = true
provisioner.serviceNowProvisioner.operateOnGrouperGroups = true
provisioner.serviceNowProvisioner.operateOnGrouperMemberships = true
provisioner.serviceNowProvisioner.provisioningType = membershipObjects
provisioner.serviceNowProvisioner.scimType = generic
provisioner.serviceNowProvisioner.selectAllEntities = true
provisioner.serviceNowProvisioner.selectAllGroups = true
provisioner.serviceNowProvisioner.showAdvanced = true
provisioner.serviceNowProvisioner.startWith = this is start with read only
provisioner.serviceNowProvisioner.subjectSourcesToProvision = jdbc
provisioner.serviceNowProvisioner.targetEntityAttribute.0.name = id
provisioner.serviceNowProvisioner.targetEntityAttribute.1.name = userName
provisioner.serviceNowProvisioner.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.serviceNowProvisioner.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = subjectId
provisioner.serviceNowProvisioner.targetEntityAttribute.10.entityAttributeJsonPointer = /urn:ietf:params:scim:schemas:extension:servicenow:2.0:User/department/name
provisioner.serviceNowProvisioner.targetEntityAttribute.10.name.elConfig = ${"department_name"}
provisioner.serviceNowProvisioner.targetEntityAttribute.10.showAdvancedAttribute = true
provisioner.serviceNowProvisioner.targetEntityAttribute.10.showAttributeValueSettings = true
provisioner.serviceNowProvisioner.targetEntityAttribute.11.insert = false
provisioner.serviceNowProvisioner.targetEntityAttribute.11.multiValued = true
provisioner.serviceNowProvisioner.targetEntityAttribute.11.name = schemas
provisioner.serviceNowProvisioner.targetEntityAttribute.11.showAdvancedAttribute = true
provisioner.serviceNowProvisioner.targetEntityAttribute.11.showAttributeCrud = true
provisioner.serviceNowProvisioner.targetEntityAttribute.11.showAttributeValueSettings = true
provisioner.serviceNowProvisioner.targetEntityAttribute.11.translateExpressionType = staticValues
provisioner.serviceNowProvisioner.targetEntityAttribute.11.translateFromStaticValues = urn:ietf:params:scim:schemas:extension:servicenow:2.0:User,urn:ietf:params:scim:schemas:core:2.0:User,urn:ietf:params:scim:schemas:extension:enterprise:2.0:User
provisioner.serviceNowProvisioner.targetEntityAttribute.11.update = false
provisioner.serviceNowProvisioner.targetEntityAttribute.12.name = emailType
provisioner.serviceNowProvisioner.targetEntityAttribute.12.translateExpressionType = staticValues
provisioner.serviceNowProvisioner.targetEntityAttribute.12.translateFromStaticValues = work
provisioner.serviceNowProvisioner.targetEntityAttribute.2.name = givenName
provisioner.serviceNowProvisioner.targetEntityAttribute.2.translateExpressionType = grouperProvisioningEntityField
provisioner.serviceNowProvisioner.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField = name
provisioner.serviceNowProvisioner.targetEntityAttribute.3.name = familyName
provisioner.serviceNowProvisioner.targetEntityAttribute.3.translateExpressionType = grouperProvisioningEntityField
provisioner.serviceNowProvisioner.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField = name
provisioner.serviceNowProvisioner.targetEntityAttribute.4.name = emailValue
provisioner.serviceNowProvisioner.targetEntityAttribute.4.translateExpression = ${gcGrouperSyncMember.getEntityAttributeValueCache0()}
provisioner.serviceNowProvisioner.targetEntityAttribute.4.translateExpressionType = translationScript
provisioner.serviceNowProvisioner.targetEntityAttribute.5.entityAttributeJsonPointer = /urn:ietf:params:scim:schemas:extension:servicenow:2.0:User/employeeNumber
provisioner.serviceNowProvisioner.targetEntityAttribute.5.jsonValueType = string
provisioner.serviceNowProvisioner.targetEntityAttribute.5.name.elConfig = ${"service_employeeNumber"}
provisioner.serviceNowProvisioner.targetEntityAttribute.5.showAdvancedAttribute = true
provisioner.serviceNowProvisioner.targetEntityAttribute.5.showAttributeValueSettings = true
provisioner.serviceNowProvisioner.targetEntityAttribute.5.translateExpression = ${'999'+grouperProvisioningEntity.idIndex}
provisioner.serviceNowProvisioner.targetEntityAttribute.5.translateExpressionType = translationScript
provisioner.serviceNowProvisioner.targetEntityAttribute.5.valueType = string
provisioner.serviceNowProvisioner.targetEntityAttribute.6.entityAttributeJsonPointer = /urn:ietf:params:scim:schemas:extension:servicenow:2.0:User/company/value
provisioner.serviceNowProvisioner.targetEntityAttribute.6.jsonValueType = string
provisioner.serviceNowProvisioner.targetEntityAttribute.6.name.elConfig = ${"company_uuid"}
provisioner.serviceNowProvisioner.targetEntityAttribute.6.showAdvancedAttribute = true
provisioner.serviceNowProvisioner.targetEntityAttribute.6.showAttributeValueSettings = true
provisioner.serviceNowProvisioner.targetEntityAttribute.6.translateExpression = ${grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverSql__company_uuid')}
provisioner.serviceNowProvisioner.targetEntityAttribute.6.translateExpressionType = translationScript
provisioner.serviceNowProvisioner.targetEntityAttribute.7.entityAttributeJsonPointer = /urn:ietf:params:scim:schemas:extension:servicenow:2.0:User/department/value
provisioner.serviceNowProvisioner.targetEntityAttribute.7.name.elConfig = ${"department_uuid"}
provisioner.serviceNowProvisioner.targetEntityAttribute.7.showAdvancedAttribute = true
provisioner.serviceNowProvisioner.targetEntityAttribute.7.showAttributeValueSettings = true
provisioner.serviceNowProvisioner.targetEntityAttribute.7.translateExpression = ${grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverSql__department_uuid')}
provisioner.serviceNowProvisioner.targetEntityAttribute.7.translateExpressionType = translationScript
provisioner.serviceNowProvisioner.targetEntityAttribute.8.entityAttributeJsonPointer = /urn:ietf:params:scim:schemas:extension:servicenow:2.0:User/manager/value
provisioner.serviceNowProvisioner.targetEntityAttribute.8.name.elConfig = ${"manager_uuid"}
provisioner.serviceNowProvisioner.targetEntityAttribute.8.showAdvancedAttribute = true
provisioner.serviceNowProvisioner.targetEntityAttribute.8.showAttributeValueSettings = true
provisioner.serviceNowProvisioner.targetEntityAttribute.8.translateExpression = ${grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverSql__manager_uuid')}
provisioner.serviceNowProvisioner.targetEntityAttribute.8.translateExpressionType = translationScript
provisioner.serviceNowProvisioner.targetEntityAttribute.9.entityAttributeJsonPointer = /urn:ietf:params:scim:schemas:extension:servicenow:2.0:User/company/name
provisioner.serviceNowProvisioner.targetEntityAttribute.9.name.elConfig = ${"company_name"}
provisioner.serviceNowProvisioner.targetEntityAttribute.9.showAdvancedAttribute = true
provisioner.serviceNowProvisioner.targetEntityAttribute.9.showAttributeValueSettings = true
provisioner.serviceNowProvisioner.targetGroupAttribute.0.name = id
provisioner.serviceNowProvisioner.targetGroupAttribute.1.name = displayName
provisioner.serviceNowProvisioner.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.serviceNowProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = displayExtension

```

## See data from service now after full sync

Note you can now dereference the uuids of users (from user table), company, and department

## Have your payroll data synced to Grouper

```
CREATE TABLE my_payroll_data (
	user_name varchar(256) NULL,
	manager varchar(256) NULL,
	department varchar(256) NULL,
	company varchar(256) NULL
);
```

Note the user_name needs to match the service now user name, and the department and company need to match the service now department and company

## Make a view of service now UUIDs for manager, department, company

Note this joins to the loaded data from service now to get the UUIDs

```
create view my_payroll_data_v as 
select
  gpsu.user_name,
  (
  select
    distinct gpsua2.attribute_value
  from
    grouper_prov_scim_user_attr gpsua,
    grouper_prov_scim_user_attr gpsua2
  where
    gpsua.id = gpsua2.id
    and gpsua.attribute_name = 'department_name'
    and gpsua.attribute_value = mpd.department
    and gpsua2.attribute_name = 'department_uuid') as department_uuid,
  (
  select
    distinct gpsua2.attribute_value
  from
    grouper_prov_scim_user_attr gpsua,
    grouper_prov_scim_user_attr gpsua2
  where
    gpsua.id = gpsua2.id
    and gpsua.attribute_name = 'company_name'
    and gpsua.attribute_value = mpd.company
    and gpsua2.attribute_name = 'company_uuid') as company_uuid,
  (
  select
    gpsua.attribute_value
  from
    grouper_prov_scim_user_attr gpsua,
    grouper_prov_scim_user gpsu2
  where
    gpsu2.user_name = mpd.manager
    and gpsua.id = gpsu2.id
    and gpsua.attribute_name = 'manager_uuid') as manager_uuid
from
  my_payroll_data mpd,
  grouper_prov_scim_user gpsu
where
  mpd.user_name = gpsu.user_name;
```

## Configure the SQL entity resolver and attribute mappings for manager, company employee

See the config example above for the full config

The entity resolver looks like this:

```
provisioner.serviceNowProvisioner.entityResolver.columnNames = user_name, department_uuid, company_uuid, manager_uuid
provisioner.serviceNowProvisioner.entityResolver.entityAttributesNotInSubjectSource = true
provisioner.serviceNowProvisioner.entityResolver.resolveAttributesWithSQL = true
provisioner.serviceNowProvisioner.entityResolver.sqlConfigId = grouper
provisioner.serviceNowProvisioner.entityResolver.sqlMappingEntityAttribute = subjectId
provisioner.serviceNowProvisioner.entityResolver.sqlMappingType = entityAttribute
provisioner.serviceNowProvisioner.entityResolver.subjectSearchMatchingColumn = user_name
provisioner.serviceNowProvisioner.entityResolver.subjectSourceIdColumn = jdbc
provisioner.serviceNowProvisioner.entityResolver.tableOrViewName = my_payroll_data_v

```

Map a UUID

```
provisioner.serviceNowProvisioner.targetEntityAttribute.8.entityAttributeJsonPointer = /urn:ietf:params:scim:schemas:extension:servicenow:2.0:User/manager/value
provisioner.serviceNowProvisioner.targetEntityAttribute.8.name.elConfig = ${"manager_uuid"}
provisioner.serviceNowProvisioner.targetEntityAttribute.8.showAdvancedAttribute = true
provisioner.serviceNowProvisioner.targetEntityAttribute.8.showAttributeValueSettings = true
provisioner.serviceNowProvisioner.targetEntityAttribute.8.translateExpression = ${grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverSql__manager_uuid')}
provisioner.serviceNowProvisioner.targetEntityAttribute.8.translateExpressionType = translationScript

```
