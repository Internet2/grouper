---
title: "Grouper Training Environment - text to copy and paste - 401.3"
space: Grouper
pageId: 28547747
version: 5
lastUpdated: 2021-06-25T07:49:38.751Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547747/Grouper+Training+Environment+-+text+to+copy+and+paste+-+401.3
---

#### grouper-loader.properties

> `provisioner.eduPersonEntitlement.class = edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync  
> provisioner.eduPersonEntitlement.configureMetadata = true  
> provisioner.eduPersonEntitlement.deleteMemberships = true  
> provisioner.eduPersonEntitlement.deleteMembershipsIfNotExistInGrouper = true  
> provisioner.eduPersonEntitlement.insertMemberships = true  
> provisioner.eduPersonEntitlement.ldapExternalSystemConfigId = demo  
> provisioner.eduPersonEntitlement.metadata.0.formElementType = text  
> provisioner.eduPersonEntitlement.metadata.0.name = md_entitlementValue  
> provisioner.eduPersonEntitlement.metadata.0.showForGroup = true  
> provisioner.eduPersonEntitlement.metadata.0.valueType = string  
> provisioner.eduPersonEntitlement.numberOfEntityAttributes = 3  
> provisioner.eduPersonEntitlement.numberOfGroupAttributes = 1  
> provisioner.eduPersonEntitlement.numberOfMetadata = 1  
> provisioner.eduPersonEntitlement.operateOnGrouperEntities = true  
> provisioner.eduPersonEntitlement.operateOnGrouperGroups = true  
> provisioner.eduPersonEntitlement.operateOnGrouperMemberships = true  
> provisioner.eduPersonEntitlement.provisioningType = entityAttributes  
> provisioner.eduPersonEntitlement.selectEntities = true  
> provisioner.eduPersonEntitlement.selectMemberships = true  
> provisioner.eduPersonEntitlement.showAdvanced = true  
> provisioner.eduPersonEntitlement.subjectSourcesToProvision = ldap  
> provisioner.eduPersonEntitlement.targetEntityAttribute.0.fieldName = name  
> provisioner.eduPersonEntitlement.targetEntityAttribute.0.isFieldElseAttribute = true  
> provisioner.eduPersonEntitlement.targetEntityAttribute.0.select = true  
> provisioner.eduPersonEntitlement.targetEntityAttribute.0.translateExpression = ${'uid=' + grouperProvisioningEntity.subjectId + ',ou=people,dc=internet2,dc=edu'}  
> provisioner.eduPersonEntitlement.targetEntityAttribute.0.translateExpressionType = translationScript  
> provisioner.eduPersonEntitlement.targetEntityAttribute.0.valueType = string  
> provisioner.eduPersonEntitlement.targetEntityAttribute.1.isFieldElseAttribute = false  
> provisioner.eduPersonEntitlement.targetEntityAttribute.1.matchingId = true  
> provisioner.eduPersonEntitlement.targetEntityAttribute.1.name = uid  
> provisioner.eduPersonEntitlement.targetEntityAttribute.1.searchAttribute = true  
> provisioner.eduPersonEntitlement.targetEntityAttribute.1.select = true  
> provisioner.eduPersonEntitlement.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField  
> provisioner.eduPersonEntitlement.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = subjectId  
> provisioner.eduPersonEntitlement.targetEntityAttribute.1.valueType = string  
> provisioner.eduPersonEntitlement.targetEntityAttribute.2.isFieldElseAttribute = false  
> provisioner.eduPersonEntitlement.targetEntityAttribute.2.membershipAttribute = true  
> provisioner.eduPersonEntitlement.targetEntityAttribute.2.multiValued = true  
> provisioner.eduPersonEntitlement.targetEntityAttribute.2.name = eduPersonEntitlement  
> provisioner.eduPersonEntitlement.targetEntityAttribute.2.translateFromGroupSyncField = groupFromId2  
> provisioner.eduPersonEntitlement.targetEntityAttribute.2.valueType = string  
> provisioner.eduPersonEntitlement.targetGroupAttribute.0.isFieldElseAttribute = false  
> provisioner.eduPersonEntitlement.targetGroupAttribute.0.name = entitlement  
> provisioner.eduPersonEntitlement.targetGroupAttribute.0.translateExpression = ${grouperUtil.defaultIfBlank(grouperProvisioningGroup.retrieveAttributeValueString('md_entitlementValue') , grouperProvisioningGroup.extension )}  
> provisioner.eduPersonEntitlement.targetGroupAttribute.0.translateExpressionType = translationScript  
> provisioner.eduPersonEntitlement.targetGroupAttribute.0.translateGrouperToGroupSyncField = groupFromId2  
> provisioner.eduPersonEntitlement.targetGroupAttribute.0.valueType = string  
> provisioner.eduPersonEntitlement.userSearchAllFilter = (uid=*)  
> provisioner.eduPersonEntitlement.userSearchBaseDn = ou=people,dc=internet2,dc=edu`

#### md_entitlementValue_eduPersonEntitlement_label

[http://tier.internet2.edu/mfa/enabled](http://tier.internet2.edu/mfa/enabled)

#### Members of app:mfa:service:ref:mfa_athletics:

```
ahenderson36
amorrison42
bsmith65
cthompson28
janderson13
jdavis4
jlangenberg100
jprice108
jvales117
ldavis5
mgrady137
mmartinez133
nscott103
pthompson61
rdavis16
```

Reference group Banner minus faculty: *app:mfa:service:ref:BannerUsersMinusFaculty*

#### Members of app:mfa:service:ref:NonFacultyBannerINB:

```
jprice108
mnielson143
mvales154
wclark159
kthompson169
athompson183
sanderson191
jlangenberg194
jwhite222
rwilliams230
pwilliams242
lprice328
dgrady331
edoe348
```

#### Members of app:mfa:ref:mfa_required:

```
app:mfa:service:ref:BannerUsersMinusFaculty
ref:dept:Information Technology
app:mfa:service:ref:mfa_athletics
app:mfa:service:ref:mfa_pilot
```
