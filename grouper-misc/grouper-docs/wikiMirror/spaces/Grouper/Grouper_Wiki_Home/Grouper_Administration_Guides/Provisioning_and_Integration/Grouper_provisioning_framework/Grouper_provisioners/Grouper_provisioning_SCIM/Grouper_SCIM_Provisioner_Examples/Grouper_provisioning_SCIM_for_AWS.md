---
title: "Grouper provisioning SCIM for AWS"
space: Grouper
pageId: 28564269
version: 23
lastUpdated: 2026-07-24T16:44:16.006Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564269/Grouper+provisioning+SCIM+for+AWS
---

This is for v4+

[https://www.rfc-editor.org/rfc/rfc7643.html#section-4.1](https://www.rfc-editor.org/rfc/rfc7643.html#section-4.1)

AWS cannot retrieve a group's members, nor the groups a user belongs to, so Grouper cannot read membership state back from AWS (i.e. you cannot select memberships from AWS). Because Grouper cannot diff against the target, you choose how it drives memberships. Either strategy below works with AWS; pick based on the tradeoff.

- **Incremental patches (insert/delete)** - leave `replaceMemberships` unset and drive memberships with `insertMemberships` / `deleteMemberships`. Grouper computes add/remove deltas from its own sync tables and sends only the changes. Lighter weight and scales well for large groups, since it never re-sends a full member list. Tradeoff: it trusts Grouper's sync state, so if AWS drifts from that state (for example someone edits a membership directly in AWS) it will not self-correct until a full sync re-establishes the sync state.
- **Replace memberships** - set `replaceMemberships = true` along with `customizeMembershipCrud = true` and `recalculateAllOperations = true`. Grouper sends the entire desired member list for each group on every sync. Self-correcting, since it re-asserts the full membership even if state has drifted, but heavier, since it pushes every member of every group on each run. Note `recalculateAllOperations = true` is required here, otherwise Grouper computes only the delta against its sync table and the replace payload is incomplete.

Grouper is normally the system of record and memberships are not edited on the AWS side, so incremental patches are usually the better default; the Penn production AWS provisioners run this way. Use replace when you want each sync to re-assert the full membership regardless of drift.

## External System

Grouper uses bearer token authentication to connect with SCIM V2 APIs. Create an external system like below.

We have tested SCIM integration for AWS, Github, Atlassian, Robin, and others. Even though they all follow SCIM, there are still many differences, so when you configure a SCIM provisioner, we ask for SCIM type. Based on the SCIM type, the provisioner framework can run extra validations to make integration more robust.

Note: for AWS (as of 7/30/2024) in the Group section of the config, you must set "include active on group create" to false

## AWS SCIM Provisioning

Group fields and attributes - example request to create a group [https://docs.aws.amazon.com/singlesignon/latest/developerguide/creategroup.html](https://docs.aws.amazon.com/singlesignon/latest/developerguide/creategroup.html)

| Grouper name | Attribute or field | Type | Required? | Description |
| --- | --- | --- | --- | --- |
| id | field | String | required | UUID read from AWS. Select only. |
| displayName | field | String | required | Display Name of the group in AWS. |

Entity fields and attributes - example request to create a user [https://docs.aws.amazon.com/singlesignon/latest/developerguide/createuser.html](https://docs.aws.amazon.com/singlesignon/latest/developerguide/createuser.html)

Make sure patch name strategy is "qualified"

| Grouper name | Attribute or field | Type | Required? | Description |
| --- | --- | --- | --- | --- |
| id | field | String | required | UUID read from AWS. Select only. |
| userName | attribute | String | required | User name |
| displayName | attribute | String | required | Display name of the user |
| familyName | attribute | String | required | Family name (Last name) |
| givenName | attribute | String | required | Given name (First name) |
| externalId | attribute | String | optional | External id |
| formattedName | attribute | String | optional | Formatted name e.g Mr. John Smith, II |
| middleName | attribute | String | optional | Middle name |
| emailValue | attribute | String | optional | Email value e.g. test@example.com |
| emailType | attribute | String | optional | Email type e.g. work |
| userType | attribute | String | optional | User type e.g. Employee |
| employeeNumber | attribute | String | optional | Employee number |
| costCenter | attribute | String | optional | Cost center |

## Configure SCIM settings in AWS for development purposes

1. Go to [AWS Single Sign-On](https://console.aws.amazon.com/singlesignon) in the AWS management console, click IAM Identity Center on left
2. On the left, click on Settings
3. Change Identity source to External Identity Provider
4. Put a random valid URL in IdP Sign-in URL e.g https://grouperdemo.internet2.edu
5. Put a random valid URL in IdP issuer URL e.g https://grouperdemo.internet2.edu
6. Upload the cert for that URL
  
  
  ```
  e.g. -----BEGIN CERTIFICATE-----
  MIIG2zCCBcOgAwIBAgIRALkhWmThH2eXPr1Y3tRnuuQwDQYJKoZIhvcNAQELBQAw
  djELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAk1JMRIwEAYDVQQHEwlBbm4gQXJib3Ix
  EjAQBgNVBAoTCUludGVybmV0MjERMA8GA1UECxMISW5Db21tb24xHzAdBgNVBAMT
  FkluQ29tbW9uIFJTQSBTZXJ2ZXIgQ0EwHhcNMjMwNjI0MDAwMDAwWhcNMjQwNjIz
  MjM1OTU5WjCBhzELMAkGA1UEBhMCVVMxETAPBgNVBAgTCE1pY2hpZ2FuMUEwPwYD
  VQQKEzhVbml2ZXJzaXR5IENvcnBvcmF0aW9uIEZvciBBZHZhbmNlZCBJbnRlcm5l
  dCBEZXZlbG9wbWVudDEiMCAGA1UEAxMZZ3JvdXBlcmRlbW8uaW50ZXJuZXQyLmVk
  dTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANeobOVmhLmw8suXkRLv
  QIl7JySlEmP43APEfWwU1O6niIWZuYU96c0u4rKFLDxEGxVkGtaGCTGljXoLZJ2P
  b2NNVV+JhmySmWacqIn7rawZGdLsiJTWa30Bicb1A1bcpdqGDjElWq2++fzJpyFX
  yRuugI7aQErEY5vZRkpTR2rmiEidEKeYhU3JtwhgwtaHPSa0ZNhFb4D36+L3iqJr
  z2V0MHt3LfxyArdh+gocEDNhqL5xWXUoDA6FfE5+ITHVTwszbANXQ6NC6Of7kwmc
  a0h5WFwD/T7ks4lyxoV8bRvCaduph5Yc0vPEHPo1grYtbXbB0m0ay0WtfPNmxQeF
  dbcCAwEAAaOCA1AwggNMMB8GA1UdIwQYMBaAFB4Fo3ePbJbiW4dLprSGrHEADOc4
  MB0GA1UdDgQWBBS0BoO4lIZQAUry9vLv9X5XEPhnBTAOBgNVHQ8BAf8EBAMCBaAw
  DAYDVR0TAQH/BAIwADAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwZwYD
  VR0gBGAwXjBSBgwrBgEEAa4jAQQDAQEwQjBABggrBgEFBQcCARY0aHR0cHM6Ly93
  d3cuaW5jb21tb24ub3JnL2NlcnQvcmVwb3NpdG9yeS9jcHNfc3NsLnBkZjAIBgZn
  gQwBAgIwRAYDVR0fBD0wOzA5oDegNYYzaHR0cDovL2NybC5pbmNvbW1vbi1yc2Eu
  b3JnL0luQ29tbW9uUlNBU2VydmVyQ0EuY3JsMHUGCCsGAQUFBwEBBGkwZzA+Bggr
  BgEFBQcwAoYyaHR0cDovL2NydC51c2VydHJ1c3QuY29tL0luQ29tbW9uUlNBU2Vy
  dmVyQ0FfMi5jcnQwJQYIKwYBBQUHMAGGGWh0dHA6Ly9vY3NwLnVzZXJ0cnVzdC5j
  b20wJAYDVR0RBB0wG4IZZ3JvdXBlcmRlbW8uaW50ZXJuZXQyLmVkdTCCAX8GCisG
  AQQB1nkCBAIEggFvBIIBawFpAHYAdv+IPwq2+5VRwmHM9Ye6NLSkzbsp3GhCCp/m
  Z0xaOnQAAAGI7ePOKQAABAMARzBFAiBAM0LrKVDsnXhcmrWjOvLrqEEg7K6WfWNG
  kk7xLwYa5QIhAL4kqhhsoczilC9DPNOpG2aLgSh+M3NGc5+jVxnFCUmbAHYA2ra/
  az+1tiKfm8K7XGvocJFxbLtRhIU0vaQ9MEjX+6sAAAGI7ePOhgAABAMARzBFAiAb
  pK4gqQC5TB1CuDXMwwd8eU/Om/61f4tncXVXTunrgQIhAL7nGj6Rx+daR6q9cHxb
  rdN2sSooXi70KhTBvIAx2g7CAHcA7s3QZNXbGs7FXLedtM0TojKHRny87N7DUUhZ
  RnEftZsAAAGI7ePOWAAABAMASDBGAiEAnKTjhIHTgDxYXCm2AfSlY7TaXgrIxVvg
  6vWlmZEVXegCIQCCC0455wCgOoVgtz8pqRuBRFV9orJlaz//HjFK2BwqZjANBgkq
  hkiG9w0BAQsFAAOCAQEAdiGeA/F6tn+5S8b0hIp0Jc+VwPP0Qx1iRg0nmI7YSAiJ
  G6HUIPVsU5V2Kb/yaAWZvmdFKyKqNRfD8w6xADY3AsJcBuvW5KiBjeZgogC7ulPM
  SYhdBtXsPAc++VP13erZdF9aVnLXApQ+lqEAv5jZZI72HxpnmkUuzLkH/+ZryOZT
  1zEzvlMVBQGxDaZ/pk5ArR2Zce9BhbA+NGBVCFCJGWJf7RGGFh59xWppqWr/xsgO
  MeJSODoBflApbraxMD0ruShzOQnhpTe9ScMcYEVyeC/fHb1a8wMzY3zK0BKDRXDz
  wq0Qq7+sNvyYs36SlzPDM1IdPF8jPMyrpjp2GdNpdw==
  -----END CERTIFICATE-----
  ```
7. Enabled automatic provisioning
8. Get the endpoint and token

## Github SCIM Provisioning

[https://docs.github.com/en/enterprise-cloud@latest/rest/scim?apiVersion=2022-11-28#provision-and-invite-a-scim-user](https://docs.github.com/en/enterprise-cloud@latest/rest/scim?apiVersion=2022-11-28#provision-and-invite-a-scim-user)

Github only supports SCIM for user operations. An organization must already exist for which members need to be managed. If you want to manage memberships of multiple organizations, configure a separate external system for each organization.

#### User fields and attributes

| Grouper name | Attribute or field | Type | Required? | Description |
| --- | --- | --- | --- | --- |
| id | field | String | required | UUID read from Github. Select only. |
| userName | attribute | String | required | User name |
| displayName | attribute | String | optional | Display name of the user |
| familyName | attribute | String | required | Family name (Last name) |
| givenName | attribute | String | required | Given name (First name) |
| externalId | attribute | String | optional | External id |
| formattedName | attribute | String | optional | Formatted name e.g Mr. John Smith, II |
| emailValue | attribute | String | required | Email value e.g. [test@example.com](mailto:test@example.com) |
| emailType | attribute | String | optional | Email type e.g. work |

## Configure SCIM settings in Github for development purposes

1. Go to Settings → Develop settings → Personal access tokens.
2. Generate a new token and keep it safe. You will need it when configuring the external system.
3. In your Github organization, you need SAML. For our testing we set up SAML integration between Github and Onelogin. You will need to set up an account on Onelogin. Github and Onelogin both offer trial versions for a few days.
4. The SCIM URL that you need to enter while configuring the external system would look like: [https://api.github.com/scim/v2/organizations/yourOrgName/](https://api.github.com/scim/v2/organizations/yourOrgName/)
5. Here is a video that shows how to integrate Onelogin with the Github organization (though you should integrate with your own saml).

## Example provisioner config

```
provisioner.awsIdentityCenterIscProd.addDisabledFullSyncDaemon = true
provisioner.awsIdentityCenterIscProd.addDisabledIncrementalSyncDaemon = true
provisioner.awsIdentityCenterIscProd.bearerTokenExternalSystemConfigId = awsIdentityCenterIscProd
provisioner.awsIdentityCenterIscProd.class = edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner
provisioner.awsIdentityCenterIscProd.customizeMembershipCrud = true
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache0entityAttribute = id
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache0has = true
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache0source = target
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache0type = entityAttribute
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache1entityAttribute = userName
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache1has = true
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache1source = target
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache1type = entityAttribute
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache2has = true
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache2source = grouper
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache2translationScript = \u0024{subject.getAttributeValue('last_name')}
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache2type = subjectTranslationScript
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache3has = true
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache3source = grouper
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache3translationScript = \u0024{subject.getAttributeValue('first_name')}
provisioner.awsIdentityCenterIscProd.entityAttributeValueCache3type = subjectTranslationScript
provisioner.awsIdentityCenterIscProd.entityAttributeValueCacheHas = true
provisioner.awsIdentityCenterIscProd.entityMatchingAttribute0name = userName
provisioner.awsIdentityCenterIscProd.entityMatchingAttribute1name = id
provisioner.awsIdentityCenterIscProd.entityMatchingAttributeCount = 2
provisioner.awsIdentityCenterIscProd.groupAllowedToAssign = penn:isc:nandt:services:aws:etc:awsIdentityCenterIscProdProvisioningAdmins
provisioner.awsIdentityCenterIscProd.groupAttributeValueCache0groupAttribute = id
provisioner.awsIdentityCenterIscProd.groupAttributeValueCache0has = true
provisioner.awsIdentityCenterIscProd.groupAttributeValueCache0source = target
provisioner.awsIdentityCenterIscProd.groupAttributeValueCache0type = groupAttribute
provisioner.awsIdentityCenterIscProd.groupAttributeValueCache1groupAttribute = displayName
provisioner.awsIdentityCenterIscProd.groupAttributeValueCache1has = true
provisioner.awsIdentityCenterIscProd.groupAttributeValueCache1source = target
provisioner.awsIdentityCenterIscProd.groupAttributeValueCache1type = groupAttribute
provisioner.awsIdentityCenterIscProd.groupAttributeValueCacheHas = true
provisioner.awsIdentityCenterIscProd.groupMatchingAttribute0name = displayName
provisioner.awsIdentityCenterIscProd.groupMatchingAttribute1name = id
provisioner.awsIdentityCenterIscProd.groupMatchingAttributeCount = 2
provisioner.awsIdentityCenterIscProd.hasTargetEntityLink = true
provisioner.awsIdentityCenterIscProd.hasTargetGroupLink = true
provisioner.awsIdentityCenterIscProd.insertMemberships = true
provisioner.awsIdentityCenterIscProd.logAllObjectsVerbose = true
provisioner.awsIdentityCenterIscProd.logAllObjectsVerboseToLogFile = false
provisioner.awsIdentityCenterIscProd.makeChangesToEntities = true
provisioner.awsIdentityCenterIscProd.numberOfEntityAttributes = 6
provisioner.awsIdentityCenterIscProd.numberOfGroupAttributes = 2
provisioner.awsIdentityCenterIscProd.operateOnGrouperEntities = true
provisioner.awsIdentityCenterIscProd.operateOnGrouperGroups = true
provisioner.awsIdentityCenterIscProd.operateOnGrouperMemberships = true
provisioner.awsIdentityCenterIscProd.provisioningType = membershipObjects
provisioner.awsIdentityCenterIscProd.removeAccentedChars = true
provisioner.awsIdentityCenterIscProd.scimNamePatchStrategy = qualified
provisioner.awsIdentityCenterIscProd.scimType = AWS
provisioner.awsIdentityCenterIscProd.selectAllEntities = true
provisioner.awsIdentityCenterIscProd.selectAllGroups = true
provisioner.awsIdentityCenterIscProd.selectMemberships = false
provisioner.awsIdentityCenterIscProd.showAdvanced = true
provisioner.awsIdentityCenterIscProd.showAssigningProvisioning = true
provisioner.awsIdentityCenterIscProd.startWith = this is start with read only
provisioner.awsIdentityCenterIscProd.subjectSourcesToProvision = pennperson
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.0.insert = false
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.0.name = id
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.0.showAdvancedAttribute = true
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.0.showAttributeCrud = true
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.0.update = false
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.1.name = emailValue
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = subjectIdentifier1
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.2.name = familyName
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.2.translateExpressionType = grouperProvisioningEntityField
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField = entityAttributeValueCache2
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.3.name = givenName
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.3.translateExpressionType = grouperProvisioningEntityField
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField = entityAttributeValueCache3
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.4.name = userName
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.4.translateExpressionType = grouperProvisioningEntityField
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.4.translateFromGrouperProvisioningEntityField = subjectIdentifier1
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.5.name = displayName
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.5.translateExpressionType = grouperProvisioningEntityField
provisioner.awsIdentityCenterIscProd.targetEntityAttribute.5.translateFromGrouperProvisioningEntityField = name
provisioner.awsIdentityCenterIscProd.targetGroupAttribute.0.insert = false
provisioner.awsIdentityCenterIscProd.targetGroupAttribute.0.name = id
provisioner.awsIdentityCenterIscProd.targetGroupAttribute.0.showAdvancedAttribute = true
provisioner.awsIdentityCenterIscProd.targetGroupAttribute.0.showAttributeCrud = true
provisioner.awsIdentityCenterIscProd.targetGroupAttribute.0.update = false
provisioner.awsIdentityCenterIscProd.targetGroupAttribute.1.name = displayName
provisioner.awsIdentityCenterIscProd.targetGroupAttribute.1.nullChecksInScript = true
provisioner.awsIdentityCenterIscProd.targetGroupAttribute.1.translateExpressionType = translationScript
provisioner.awsIdentityCenterIscProd.targetGroupAttribute.1.translateExpression = \u0024{var extensions = grouperProvisioningGroup.name.split(":"); extensions.get(size(extensions)-2) + '_' + extensions.get(size(extensions)-1);}
provisioner.awsIdentityCenterIscProd.targetGroupAttribute.1.translationContinueCondition = \u0024{grouperProvisioningGroup.name != null}

```
