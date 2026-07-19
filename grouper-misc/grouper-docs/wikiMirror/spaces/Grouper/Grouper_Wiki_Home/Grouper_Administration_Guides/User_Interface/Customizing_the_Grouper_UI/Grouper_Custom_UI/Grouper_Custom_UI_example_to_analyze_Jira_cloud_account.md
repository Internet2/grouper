---
title: "Grouper Custom UI example to analyze Jira cloud account"
space: Grouper
pageId: 28555220
version: 5
lastUpdated: 2026-07-01T05:38:42.872Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555220/Grouper+Custom+UI+example+to+analyze+Jira+cloud+account
---

## Privileges

- Whoever can claim a license needs OPTIN on the group
- Whoever can run the Custom UI needs view on the group. Maybe this is just GrouperAll

## Configure

Configurations are done in the UI (Miscellaneous → Custom UI) or in grouper.properties

Example of PIT provisioning variable. See if the membership for the user in the app has been provisioned for a certain group in a provisioner more than 5 minutes ago (postgres)

```
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.bindVar0 = \u0024{subject.id}
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.bindVar0Type = string
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.configId = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.label = Has Jira license provisioned to cloud more than 5 minutes ago
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.query = select  case when exists ( select 1 from  grouper_sync_group gsg, grouper_sync_membership gsmem, grouper_sync_member gsm, grouper_sync gs where gsg.group_name = 'penn:isc:ait:apps:atlassian:groupsJira:jira_has_license' and gsm.subject_id = ? and gs.provisioner_name = 'atlassianCloudJira' and gsg.grouper_sync_id = gs.id  and gsm.grouper_sync_id = gs.id  and gsmem.grouper_sync_group_id = gsg.id  and gsmem.grouper_sync_member_id = gsm.id and gsmem.in_target_start < current_timestamp - (5 * interval '1 minute') ) then 1 else 0 end as  has_license_provisioned
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.userQueryType = sql
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.variableToAssign = cu_jiraHasLicenseProvisioned
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.variableType = boolean

```

Query

```
select
  case
    when exists (
    select
      1
    from
      grouper_sync_group gsg,
      grouper_sync_membership gsmem,
      grouper_sync_member gsm,
      grouper_sync gs
    where
      gsg.group_name = 'penn:isc:ait:apps:atlassian:groupsJira:jira_has_license'
      and gsm.subject_id = ?
      and gs.provisioner_name = 'atlassianCloudJira'
      and gsg.grouper_sync_id = gs.id
      and gsm.grouper_sync_id = gs.id
      and gsmem.grouper_sync_group_id = gsg.id
      and gsmem.grouper_sync_member_id = gsm.id
      and gsmem.in_target_start < current_timestamp - (5 * interval '1 minute') ) then 1
    else 0
  end as has_license_provisioned
```

```
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.bindVar0 = \u0024{subject.id}
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.bindVar0Type = string
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.configId = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.label = Has Jira license provisioned to cloud more than 5 minutes ago
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.query = select  case when exists ( select 1 from  grouper_sync_group gsg, grouper_sync_membership gsmem, grouper_sync_member gsm, grouper_sync gs where gsg.group_name = 'penn\u003Aisc\u003Aait\u003Aapps\u003Aatlassian\u003AgroupsJira\u003Ajira_has_license' and gsm.subject_id = ? and gs.provisioner_name = 'atlassianCloudJira' and gsg.grouper_sync_id = gs.id  and gsm.grouper_sync_id = gs.id  and gsmem.grouper_sync_group_id = gsg.id  and gsmem.grouper_sync_member_id = gsm.id and gsmem.in_target_start < current_timestamp - (5 * interval '1 minute') ) then 1 else 0 end as  has_license_provisioned
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.userQueryType = sql
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.variableToAssign = cu_jiraHasLicenseProvisioned
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.0.variableType = boolean
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.1.bindVar0 = \u0024{subject.id}
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.1.bindVar0Type = string
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.1.configId = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.1.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.1.label = Has Jira license
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.1.query = select case when exists ( select 1 from  grouper_sync_group gsg, grouper_sync_membership gsmem, grouper_sync_member gsm, grouper_sync gs where gsg.group_name = 'penn\u003Aisc\u003Aait\u003Aapps\u003Aatlassian\u003AgroupsJira\u003Ajira_has_license' and gsm.subject_id = ? and gs.provisioner_name = 'atlassianCloudJira' and gsg.grouper_sync_id = gs.id  and gsm.grouper_sync_id = gs.id  and gsmem.grouper_sync_group_id = gsg.id  and gsmem.grouper_sync_member_id = gsm.id ) then 1 else 0 end as has_license
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.1.userQueryType = sql
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.1.variableToAssign = cu_jiraHasLicense
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.1.variableType = boolean
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.10.fieldNames = members
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.10.groupName = penn\u003Aisc\u003Aait\u003Aapps\u003Aatlassian\u003Aadmin\u003AatlassianCanAssignVariablesCustomUi
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.10.label = Atlassian admin
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.10.userQueryType = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.10.variableToAssign = cu_atlassianAdminCanAssignVariables
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.10.variableType = boolean
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.11.fieldNames = members
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.11.groupName = penn\u003Acommunity\u003Aauthentication\u003AtwoStepUsers
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.11.label = In two-step
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.11.userQueryType = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.11.variableToAssign = cu_inTwoStep
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.11.variableType = boolean
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.2.fieldNames = members
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.2.groupName = penn\u003Aisc\u003Aait\u003Aapps\u003Aatlassian\u003AhelperGroups\u003AjiraAutomaticLicense
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.2.label = Has automatic license
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.2.order = 10
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.2.userQueryType = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.2.variableToAssign = cu_inGroupForAutomaticLicense
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.2.variableType = boolean
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.3.fieldNames = members
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.3.groupName = penn\u003Acommunity\u003AactiveNonAlumniWithPennname
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.3.label = Active Penn affiliate
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.3.order = 20
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.3.userQueryType = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.3.variableToAssign = cu_activePennAffiliate
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.3.variableType = boolean
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.4.fieldNames = members
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.4.groupName = penn\u003Aisc\u003Aait\u003Aapps\u003Aatlassian\u003AhelperGroups\u003AconfluenceHaveSelfClaimedLicense
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.4.label = Claimed license
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.4.order = 30
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.4.userQueryType = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.4.variableToAssign = cu_inGroupForClaimedLicense
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.4.variableType = boolean
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.5.fieldNames = members
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.5.groupName = penn\u003Aisc\u003Aait\u003Aapps\u003Aatlassian\u003AhelperGroups\u003AjiraAllowedToClaimLicense
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.5.label = Can claim license
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.5.order = 40
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.5.userQueryType = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.5.variableToAssign = cu_inGroupForCanClaimLicense
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.5.variableType = boolean
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.6.bindVar0 = \u0024{subject.id}
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.6.bindVar0Type = string
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.6.configId = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.6.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.6.label = Jira groups
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.6.query = select string_agg(gg.extension, ', '  order by gg.extension) as group_names from grouper_memberships_lw_v gmlv2, grouper_groups gg where gmlv2.group_id = gg.id and gmlv2.list_name = 'members' and gmlv2.subject_source = 'pennperson' and gmlv2.group_name like 'penn\u003Aisc\u003Aait\u003Aapps\u003Aatlassian\u003AgroupsJira\u003A%' and gmlv2.subject_id = ?
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.6.userQueryType = sql
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.6.variableToAssign = cu_jiraGroups
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.6.variableType = string
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.7.bindVar0 = \u0024{subject.id}
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.7.bindVar0Type = string
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.7.configId = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.7.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.7.label = Has at least one Jira group
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.7.query = select case when exists (select 1 from grouper_memberships_lw_v gmlv2, grouper_groups gg where gmlv2.group_id = gg.id and gmlv2.list_name = 'members' and gmlv2.subject_source = 'pennperson' and gmlv2.group_name like 'penn\u003Aisc\u003Aait\u003Aapps\u003Aatlassian\u003AgroupsJira\u003A%' and gmlv2.subject_id = ? and gmlv2.group_name != 'penn\u003Aisc\u003Aait\u003Aapps\u003Aatlassian\u003AgroupsJira\u003Ajira_has_license') then 1 else 0 end as has_group
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.7.userQueryType = sql
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.7.variableToAssign = cu_jiraHasGroup
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.7.variableType = boolean
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.8.fieldNames = members
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.8.groupName = penn\u003Aisc\u003Aait\u003Aapps\u003Aatlassian\u003AhelperGroups\u003ArecentAtlassianCloudUsers
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.8.label = Recent Atlassian user
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.8.order = 40
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.8.userQueryType = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.8.variableToAssign = cu_recentAtlassianUser
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.8.variableType = boolean
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.9.fieldNames = members
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.9.groupName = penn\u003Aisc\u003Aait\u003Aapps\u003Aatlassian\u003Aadmin\u003Aadmins
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.9.label = Atlassian admin
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.9.userQueryType = grouper
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.9.variableToAssign = cu_atlassianAdmin
grouperCustomUI.atlassianJiraClaimLicense.cuQuery.9.variableType = boolean
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.0.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.0.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.0.index = 0
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.0.textBoolean = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.0.textIsScript = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.0.textType = canSeeScreenState
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.1.defaultText = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.1.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.1.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.1.index = 10
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.1.text = <h1>ISC Jira account analysis</h1>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.1.textType = header
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.defaultText = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.index = 0
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.textBoolean = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.textIsScript = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.textType = manageMembership
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.index = -20
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.script = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.text = This page displays your account status for ISC cloud Jira.  If you do not have a license and are eligible to claim one, you can do that here.<br /><br /><ul>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.12.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.12.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.12.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.12.index = 10
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.12.script = \u0024{ cu_jiraHasLicenseProvisioned }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.12.text = <li><b>Jira license provisioned\u003A</b> yes. Your Jira license was provisioned more than 5 minutes ago and is ready to use</li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.12.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.13.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.13.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.13.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.13.index = 11
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.13.script = \u0024{ !cu_jiraHasLicenseProvisioned && cu_jiraHasLicense }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.13.text = <li><b>Jira license provisioned\u003A</b> <b style="color\u003A brown">no. Your Jira license was not provisioned more than 5 minutes ago and is not ready to use</b></li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.13.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.14.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.14.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.14.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.14.index = 32
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.14.script = \u0024{!cu_inGroupForAutomaticLicense && !cu_inGroupForClaimedLicense && !cu_inGroupForCanClaimLicense}
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.14.text = <font style="color\u003Abrown"><b>Error\u003A</b></font> You do not have a license and you are not allowed to self-claim a license, open a ticket with help@example.com with your needs for Jira
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.14.textType = enrollmentLabel
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.15.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.15.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.15.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.15.index = 21
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.15.script = \u0024{ !cu_jiraHasLicense }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.15.text = <li><b>Has provisioned Jira license\u003A</b> <b style="color\u003A brown">no. You do not have a provisioned license</b></li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.15.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.16.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.16.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.16.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.16.index = 2000
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.16.text = </ul>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.16.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.17.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.17.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.17.index = 30
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.17.script = \u0024{!cu_activePennAffiliate}
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.17.text = <font style="color\u003Abrown"><b>Error\u003A</b></font> You are not an active Penn affiliate and need to ask your BA to check employment records
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.17.textType = enrollmentLabel
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.18.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.18.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.18.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.18.index = 30
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.18.script = \u0024{ cu_activePennAffiliate }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.18.text = <li><b>Active Penn affiliate\u003A</b> yes. You have a valid active Penn affiliation</li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.18.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.19.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.19.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.19.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.19.index = 31
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.19.script = \u0024{ !cu_activePennAffiliate }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.19.text = <li><b>Active Penn affiliate\u003A</b> <b style="color\u003A brown">no. You do not have an active Penn affiliation</b></li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.19.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.2.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.2.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.2.index = 0
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.2.textBoolean = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.2.textIsScript = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.2.textType = canSeeUserEnvironment
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.20.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.20.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.20.index = 40
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.20.script = \u0024{(cu_inGroupForAutomaticLicense || cu_inGroupForClaimedLicense) && !cu_jiraHasLicense}
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.20.text = <font style="color\u003Abrown"><b>Error\u003A</b></font> You are in the PennGroup indicating you have a license, but it has not been provisioned.  If it is been a few minutes and still not provisioned, open a ticket with help@example.com and let them know provisioning is not working for Atlassian.
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.20.textType = enrollmentLabel
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.21.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.21.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.21.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.21.index = 40
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.21.script = \u0024{ cu_inGroupForAutomaticLicense }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.21.text = <li><b>Has automatic license\u003A</b> yes. You have an automatic license so you do not need to claim one</li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.21.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.22.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.22.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.22.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.22.index = 41
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.22.script = \u0024{ !cu_inGroupForAutomaticLicense &&  cu_inGroupForClaimedLicense }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.22.text = <li><b>Claimed license\u003A</b> yes. You have claimed a license for Jira.  If you stop using Jira for two months, you will need to claim your license again</b></li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.22.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.23.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.23.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.23.index = 50
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.23.script = \u0024{!cu_inGroupForAutomaticLicense && !cu_inGroupForClaimedLicense && cu_inGroupForCanClaimLicense}
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.23.text = <font style="color\u003Abrown"><b>Error\u003A</b></font> You do not have a Jira license but you can claim one.  Click the 'Claim Jira license' button below, wait 5 minutes, and try Jira again.
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.23.textType = enrollmentLabel
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.24.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.24.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.24.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.24.index = 50
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.24.script = \u0024{ !cu_inGroupForAutomaticLicense &&  cu_inGroupForCanClaimLicense }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.24.text = <li><b>Can claim license\u003A</b> yes. You are allowed to claim a Jira license</li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.24.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.25.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.25.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.25.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.25.index = 51
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.25.script = \u0024{ !cu_inGroupForAutomaticLicense &&  !cu_inGroupForCanClaimLicense }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.25.text = <li><b>Can claim license\u003A</b> <b style="color\u003A brown">no. You are not allowed to claim a Jira license</b></li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.25.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.26.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.26.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.26.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.26.index = 60
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.26.script = \u0024{ cu_jiraHasGroup }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.26.text = <li><b>Groups\u003A</b> \u0024{cu_jiraGroups}</li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.26.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.27.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.27.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.27.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.27.index = 59
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.27.script = \u0024{ !cu_jiraHasGroup }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.27.text = <li><b>Has at least one group\u003A</b> <b style="color\u003A brown">no. You are not in any Jira groups, so do you not have any access.  Open a ticket with help@example.com</b></li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.27.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.28.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.28.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.28.index = 5
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.28.script = \u0024{ !cu_jiraHasGroup }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.28.text = <font style="color\u003Abrown"><b>Error\u003A</b></font> You are not in any Jira policy groups and have no access to any projects.  Open a ticket with help@example.com if this should not be the case</b>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.28.textType = enrollmentLabel
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.29.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.29.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.29.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.29.index = 70
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.29.script = \u0024{ cu_recentAtlassianUser }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.29.text = <li><b>Recent Atlassian user\u003A</b> yes. You have used Atlassian in the cloud in the last 60 days</li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.29.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.3.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.3.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.3.index = 0
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.3.textBooleanScript = \u0024{cu_atlassianAdminCanAssignVariables}
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.3.textIsScript = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.3.textType = canAssignVariables
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.30.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.30.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.30.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.30.index = 71
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.30.script = \u0024{ !cu_recentAtlassianUser }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.30.text = <li><b>Recent Atlassian user\u003A</b> no. You have not used Atlassian in the cloud in the last 60 days</li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.30.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.31.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.31.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.31.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.31.index = 10
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.31.text = Claim Jira license
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.31.textType = enrollButtonText
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.32.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.32.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.32.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.32.index = 80
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.32.script = \u0024{ cu_inTwoStep }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.32.text = <li><b>In two-step\u003A</b> yes. You are enrolled in Two-step</li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.32.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.33.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.33.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.33.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.33.index = 81
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.33.script = \u0024{ !cu_inTwoStep }
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.33.text = <li><b>In two-step\u003A</b> <b style="color\u003A brown">no. You are not enrolled in Two-step</b></li>
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.33.textType = instructions1
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.34.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.34.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.34.index = 31
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.34.script = \u0024{!cu_inTwoStep}
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.34.text = <font style="color\u003Abrown"><b>Error\u003A</b></font> You are not enrolled in <a href="https\u003A//twostep.apps.upenn.edu">Two-step</a>.  You need to enroll to use ISC Jira.
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.34.textType = enrollmentLabel
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.4.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.4.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.4.index = 0
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.4.text =  
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.4.textType = helpLink
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.index = 10
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.script = \u0024{(cu_jiraHasLicense && !cu_jiraHasLicenseProvisioned)}
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.text = <font style="color\u003Abrown"><b>Warning\u003A</b></font> You need to wait a few minutes for data to propagate inside Atlassian.  You have a recently successfully provisioned license though.  Try again in a few minutes.
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.textType = enrollmentLabel
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.index = 11
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.script = \u0024{cu_jiraHasLicenseProvisioned}
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.text = <font style="color\u003Agreen"><b>Success\u003A</b></font> License provisioned more than 5 minutes ago.  <a href="https\u003A//isc-penn-jira.atlassian.net/jira">Jira</a> is ready for you to use.
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.textType = enrollmentLabel
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.defaultText = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.index = 0
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.textBoolean = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.textIsScript = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.textType = unenrollButtonShow
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.defaultText = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.endIfMatches = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.index = 0
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.text =  
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.textType = enrollmentLabel
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.defaultText = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.enabled = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.endIfMatches = true
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.index = 0
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.textBoolean = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.textBooleanScript = \u0024{(!cu_inGroupForAutomaticLicense && cu_activePennAffiliate && !cu_inGroupForClaimedLicense && cu_inGroupForCanClaimLicense && cu_jiraHasGroup)}
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.textIsScript = false
grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.textType = enrollButtonShow
grouperCustomUI.atlassianJiraClaimLicense.groupUUIDOrName = penn\u003Aisc\u003Aait\u003Aapps\u003Aatlassian\u003AhelperGroups\u003AjiraHaveSelfClaimedLicense
grouperCustomUI.atlassianJiraClaimLicense.numberOfQueries = 12
grouperCustomUI.atlassianJiraClaimLicense.numberOfTextConfigs = 35

```

## States of screen

Note: to test the screen states, the user must be in the Atlassian admin can assign custom ui variables group

### Success

[https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=true&cu_jiraHasLicense=true&cu_inGroupForAutomaticLicense=true&cu_activePennAffiliate=true&cu_inGroupForClaimedLicense=true&cu_inGroupForCanClaimLicense=true&cu_jiraGroups=isc_ais,+jira-administrators&cu_jiraHasGroup=true&cu_recentAtlassianUser=true](https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=true&cu_jiraHasLicense=true&cu_inGroupForAutomaticLicense=true&cu_activePennAffiliate=true&cu_inGroupForClaimedLicense=true&cu_inGroupForCanClaimLicense=true&cu_jiraGroups=isc_ais,+jira-administrators&cu_jiraHasGroup=true&cu_recentAtlassianUser=true)

### Not provisioned yet

Including 5 minutes to propagate inside Atlassian cloud itself

[https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=false&cu_jiraHasLicense=true&cu_inGroupForAutomaticLicense=true&cu_activePennAffiliate=true&cu_inGroupForClaimedLicense=true&cu_inGroupForCanClaimLicense=true&cu_jiraGroups=isc_ais,+jira-administrators&cu_jiraHasGroup=true&cu_recentAtlassianUser=true](https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=false&cu_jiraHasLicense=true&cu_inGroupForAutomaticLicense=true&cu_activePennAffiliate=true&cu_inGroupForClaimedLicense=true&cu_inGroupForCanClaimLicense=true&cu_jiraGroups=isc_ais,+jira-administrators&cu_jiraHasGroup=true&cu_recentAtlassianUser=true)

### Needs to claim a license

[https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=false&cu_jiraHasLicense=false&cu_inGroupForAutomaticLicense=false&cu_activePennAffiliate=true&cu_inGroupForClaimedLicense=false&cu_inGroupForCanClaimLicense=true&cu_jiraGroups=isc_ais,+jira-administrators&cu_jiraHasGroup=true&cu_recentAtlassianUser=true&cu_inTwoStep=true](https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=false&cu_jiraHasLicense=false&cu_inGroupForAutomaticLicense=false&cu_activePennAffiliate=true&cu_inGroupForClaimedLicense=false&cu_inGroupForCanClaimLicense=true&cu_jiraGroups=isc_ais,+jira-administrators&cu_jiraHasGroup=true&cu_recentAtlassianUser=true&cu_inTwoStep=true)

### Not allowed to claim a license

[https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=false&cu_jiraHasLicense=false&cu_inGroupForAutomaticLicense=false&cu_activePennAffiliate=true&cu_inGroupForClaimedLicense=false&cu_inGroupForCanClaimLicense=false&cu_jiraGroups=isc_ais,+jira-administrators&cu_jiraHasGroup=true&cu_recentAtlassianUser=true&cu_inTwoStep=true](https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=false&cu_jiraHasLicense=false&cu_inGroupForAutomaticLicense=false&cu_activePennAffiliate=true&cu_inGroupForClaimedLicense=false&cu_inGroupForCanClaimLicense=false&cu_jiraGroups=isc_ais,+jira-administrators&cu_jiraHasGroup=true&cu_recentAtlassianUser=true&cu_inTwoStep=true)

### Not a Penn affiliate

[https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=false&cu_jiraHasLicense=false&cu_inGroupForAutomaticLicense=true&cu_activePennAffiliate=false&cu_inGroupForClaimedLicense=true&cu_inGroupForCanClaimLicense=true&cu_jiraHasGroup=true&cu_recentAtlassianUser=true](https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=false&cu_jiraHasLicense=false&cu_inGroupForAutomaticLicense=true&cu_activePennAffiliate=false&cu_inGroupForClaimedLicense=true&cu_inGroupForCanClaimLicense=true&cu_jiraHasGroup=true&cu_recentAtlassianUser=true)

### Not in any groups

[https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=false&cu_jiraHasLicense=false&cu_inGroupForAutomaticLicense=true&cu_activePennAffiliate=true&cu_inGroupForClaimedLicense=true&cu_inGroupForCanClaimLicense=true&cu_jiraHasGroup=false&cu_recentAtlassianUser=true](https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=false&cu_jiraHasLicense=false&cu_inGroupForAutomaticLicense=true&cu_activePennAffiliate=true&cu_inGroupForClaimedLicense=true&cu_inGroupForCanClaimLicense=true&cu_jiraHasGroup=false&cu_recentAtlassianUser=true)

### Not in MFA

[https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=false&cu_jiraHasLicense=false&cu_inGroupForAutomaticLicense=true&cu_activePennAffiliate=true&cu_inGroupForClaimedLicense=true&cu_inGroupForCanClaimLicense=true&cu_jiraGroups=isc_ais,+jira-administrators&cu_jiraHasGroup=false&cu_recentAtlassianUser=true&cu_inTwoStep=false](https://grouper.institution.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=5fb80749a64e43308faa7c93622a3b88&cu_jiraHasLicenseProvisioned=false&cu_jiraHasLicense=false&cu_inGroupForAutomaticLicense=true&cu_activePennAffiliate=true&cu_inGroupForClaimedLicense=true&cu_inGroupForCanClaimLicense=true&cu_jiraGroups=isc_ais,+jira-administrators&cu_jiraHasGroup=false&cu_recentAtlassianUser=true&cu_inTwoStep=false)

### Admin screen to look up other users
