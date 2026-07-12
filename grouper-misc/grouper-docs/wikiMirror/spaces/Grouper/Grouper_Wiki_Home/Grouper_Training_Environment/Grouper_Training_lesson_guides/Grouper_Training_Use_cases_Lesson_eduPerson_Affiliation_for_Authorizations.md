---
title: "Grouper Training - Use cases - Lesson: eduPerson Affiliation for Authorizations"
space: Grouper
pageId: 28545584
version: 23
lastUpdated: 2025-08-06T19:25:01.215Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545584/Grouper+Training+-+Use+cases+-+Lesson+eduPerson+Affiliation+for+Authorizations
---

# eduPerson Affiliation for Authorization

## Learning Objectives

- Understand how to do subject attributes management with policy groups
- Configure provisioning to reflect group membership (aka subject attributes) into OpenLDAP
- Configure Shibboleth to release eduPersonAffiliation for loosely defined authorization use cases

## Hands on

### Create a new application template

- Navigate to the *app* folder
- create a new application (Folder actions -> New template -> Application)
  
  - Key: `eduPersonAffiliation`
  - Description:
    
    `eduPersonAffiliation (defined in eduPerson 1.0); OID: 1.3.6.1.4.1.5923.1.1.1.1 Specifies the person's relationship(s) to the institution in broad categories such as student, faculty, staff, alum, etc.`

### Create app groups for eduPersonAffiliation

- Assign object type “policy” to the *service:policy* folder
- Create the following groups in app:eduPersonAffiliation:service:policy
  
  - student
  - faculty
  - staff

### Add reference groups to policy

The eduPerson specification states: "...each institution will decide the criteria for membership in each affiliation classification. What is desirable is that a reasonable person should find an institution’s definition of affiliation plausible"

- Add *ref:student:students* to the policy for *student*
- Add the *All Staff* reference group to *staff*
- Add the *All Faculty* reference group to *faculty*

The eduPerson specification states: "The 'member' affiliation MUST be asserted for people carrying one or more of the following affiliations: faculty or staff or student or employee"

- Create new group under app:eduPersonAffiliation:service:policy
  
  - name: `member`
- Add faculty, staff, and student to member
- Visualize the new *member* group

### Configure external system for provisioning

Provisioning targets depend on an external system that includes basic connection and configuration. For this lesson, the LDAP system has already been set up with config id "demo".

- Navigate to Miscellaneous > External Systems.
- Location entry for "demo"
- Under Actions, view the details, then test the system

### Configure (review) provisioner

A provisioner has already been set up, “eduPersonAffiliation”. Review its properties

- Go to Miscellaneous -> Provisioning -> eduPersonAffiliation -> Actions -> Edit provisioner

To configure your own:

- Delete existing provisioner
- Add provisioner
- Config id: eduPersonAffiliation
- Provisioning type: LDAP
- Start with
- External system: demo
- Pattern: usersWithEduPersonAffiliation
- user base DN: ou=people,dc=internet2,dc=edu
- Membership attribbute name for entities: affiliation
- Matching search attribute name for entities: uid
- Matching search attribute value for entities: subjectIdentifier0
- object class for entity: eduPerson

```
provisioner.eduPersonAffiliation.class = edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync
provisioner.eduPersonAffiliation.customizeGroupCrud = true
provisioner.eduPersonAffiliation.deleteGroups = false
provisioner.eduPersonAffiliation.entityAttributeValueCache0entityAttribute = ldap_dn
provisioner.eduPersonAffiliation.entityAttributeValueCache0has = true
provisioner.eduPersonAffiliation.entityAttributeValueCache0source = target
provisioner.eduPersonAffiliation.entityAttributeValueCache0type = entityAttribute
provisioner.eduPersonAffiliation.entityAttributeValueCacheHas = true
provisioner.eduPersonAffiliation.entityMatchingAttribute0name = uid
provisioner.eduPersonAffiliation.entityMatchingAttributeCount = 1
provisioner.eduPersonAffiliation.entityMembershipAttributeName = eduPersonAffiliation
provisioner.eduPersonAffiliation.entityMembershipAttributeValue = groupAttributeValueCache0
provisioner.eduPersonAffiliation.groupAttributeValueCache0groupAttribute = affiliation
provisioner.eduPersonAffiliation.groupAttributeValueCache0has = true
provisioner.eduPersonAffiliation.groupAttributeValueCache0source = grouper
provisioner.eduPersonAffiliation.groupAttributeValueCache0type = groupAttribute
provisioner.eduPersonAffiliation.groupAttributeValueCacheHas = true
provisioner.eduPersonAffiliation.hasTargetEntityLink = true
provisioner.eduPersonAffiliation.insertGroups = false
provisioner.eduPersonAffiliation.ldapExternalSystemConfigId = demo
provisioner.eduPersonAffiliation.numberOfEntityAttributes = 4
provisioner.eduPersonAffiliation.numberOfGroupAttributes = 1
provisioner.eduPersonAffiliation.operateOnGrouperEntities = true
provisioner.eduPersonAffiliation.operateOnGrouperGroups = true
provisioner.eduPersonAffiliation.operateOnGrouperMemberships = true
provisioner.eduPersonAffiliation.provisioningType = entityAttributes
provisioner.eduPersonAffiliation.selectAllEntities = true
provisioner.eduPersonAffiliation.selectGroups = false
provisioner.eduPersonAffiliation.startWith = this is start with read only
provisioner.eduPersonAffiliation.subjectSourcesToProvision = eduLDAP
provisioner.eduPersonAffiliation.targetEntityAttribute.0.name = ldap_dn
provisioner.eduPersonAffiliation.targetEntityAttribute.1.multiValued = true
provisioner.eduPersonAffiliation.targetEntityAttribute.1.name = eduPersonAffiliation
provisioner.eduPersonAffiliation.targetEntityAttribute.1.showAdvancedAttribute = true
provisioner.eduPersonAffiliation.targetEntityAttribute.1.showAttributeValueSettings = true
provisioner.eduPersonAffiliation.targetEntityAttribute.2.name = uid
provisioner.eduPersonAffiliation.targetEntityAttribute.2.translateExpressionType = grouperProvisioningEntityField
provisioner.eduPersonAffiliation.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField = subjectIdentifier0
provisioner.eduPersonAffiliation.targetEntityAttribute.3.multiValued = true
provisioner.eduPersonAffiliation.targetEntityAttribute.3.name = objectClass
provisioner.eduPersonAffiliation.targetEntityAttribute.3.showAdvancedAttribute = true
provisioner.eduPersonAffiliation.targetEntityAttribute.3.showAttributeValueSettings = true
provisioner.eduPersonAffiliation.targetEntityAttribute.3.translateExpressionType = staticValues
provisioner.eduPersonAffiliation.targetEntityAttribute.3.translateFromStaticValues = eduPerson
provisioner.eduPersonAffiliation.targetGroupAttribute.0.name = affiliation
provisioner.eduPersonAffiliation.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.eduPersonAffiliation.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = extension
provisioner.eduPersonAffiliation.updateGroups = false
provisioner.eduPersonAffiliation.userSearchBaseDn = ou=people,dc=internet2,dc=edu

```

### Create a full sync provisioning job (this has already be set up)

The provisioner exists, but needs to have either a full sync or incremental job to perform the provisioning.

- Go to Miscellaneous -> Daemon jobs
- Search for daemon job *OTHER_JOB_provisioner_full_eduPersonAffiliation*
- Choose Edit daemon

### Configure provisioning on folder

- Navigate back to the *app:eduPersonAffiliation:service:policy* folder.
- Under Folder actions, choose Provisioning
- For provisioner eduPersonAffiliation, choose action "Provision to"
  
  
  
  - Use default settings

### Run provisioner job

- In Miscellaneous -> Daemon jobs, look for job *OTHER_JOB_provisioner_full_eduPersonAffiliation*
- Under job actions, choose *Run job now*
- Click on the hyperlink for the job to go to the job log
- Click on *Apply filter* until the job completes

### **Review job results**

- Back in Miscellaneous->Provisioning->eduPersonAffiliation, select View Activity

### Verify Provisioning results

- From the GTE Jump page (https://localhost:8443/) launch the LDAP manager and login.
- Verify that subjects in the *member* group now have a "member" affiliation in the LDAP record (e.g. uid=aadams)

### Configure Shib to release ePA

- Open a private browser, and log in to https://localhost:8443/app with username *aadams* and password *password*
- *Look at value for eduPersonAffiliation*
