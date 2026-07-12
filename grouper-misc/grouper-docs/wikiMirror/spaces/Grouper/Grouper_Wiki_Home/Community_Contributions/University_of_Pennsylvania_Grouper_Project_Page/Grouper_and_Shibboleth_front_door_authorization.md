---
title: "Grouper and Shibboleth front door authorization"
space: Grouper
pageId: 28545521
version: 23
lastUpdated: 2026-07-01T05:47:06.948Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545521/Grouper+and+Shibboleth+front+door+authorization
---

Penn front door authorization as presented in the [June 2025 InCommon IAM Online](https://youtu.be/og_7vhAzAXE).

## Summary

See this [marketing video to explain front door authorization](https://www.youtube.com/watch?v=MykwtpygkrE).

Shibboleth SAML Service Providers (SP's) can have a front door authorization policy assigned which will ensure that users are eligible for the service.

When a user is not allowed to access an application (SP), they are sent an error page (could be customized or an external link) instead of the application.

There are currently a dozen coarse-grained options for the eligibility population (described below). A policy group could be used instead of or in addition.

Application admins can self-manage their front-door configuration.

This is an extra security control on top of whatever specific authorization a service performs. You can think of it like a security guard at the front of a building. Applications still need to provide the same authorization, but deprovisioning is not as urgent.

## Frequently asked questions

1. If my application is protected by Workforce, and someone is being blocked, how do I remedy that?
  
  1. If the user is working at Penn, they should have an affiliation which reflects that relationship. Assuming they are not paid by Penn, their sponsor should give them a contractor or research affiliation.
  2. If the user is not working at Penn, then perhaps the Workforce coarse grained reference group is not appropriate for this service and the front door for this service should be reconfigured to use something wider (e.g. Member which includes students and related organizations like the children's hospital).
2. Does this affect local log ins?
  
  1. If a user is authenticating and is not using Shibboleth, (i.e. a local password), then the front door is not used
  2. Note: your application should not allow local logins by policy. You might have a local login extraordinary situations (e.g. for super admins who need to reconfigure SSO).
3. How will this affect the user experience?
  
  1. The user will not know the front door is being used, unless they leave Penn or their reference/policy group
4. Do I have to provide a list of at least two admins or owners?
  
  1. No, you can let the IT dept manage the front door if you do not have the technical staff to manage it

### User flow

1. User attempts to log in to an application
2. Shibboleth determines which population is eligible for that service based on how it is configured. The membership of that PennGroup (eligible population) is checked for the user (i.e. are they eligible)
3. If the user is in the population, the user is sent to the service. 
  
  1. The service has more specific authorization checks and the user might be able to use the application or see an application error page
4. If the user is not in the population, the user will be sent to a generic error handler at Grouper
5. If the service does not have any Grouper error handling configured, the user will go to a generic error page
6. The error page could be hosted by the service owner at some location (not on the service that is protected)
7. The error page in Grouper could be customized to provide specific information to the user
  
  1. To tell the user whom to contact for support
  2. To clearly communicate the access requirements

## Coarse-grained eligibility populations

Note: “Requires Two-step” means all users must be enrolled in Duo.

If the application selects a reference group for the eligibility requirement, they can pick: Workforce, Workforce in Two-Step, Member, Member in Two-step, etc.

## Error page - generic

This is a dynamic page that will attempt to reduce tickets based on the state of the user.

- If the service has no eligibility, then users will never see the error page.
- If the service has only a reference group eligibility, then if the user is not in that group then they will see this page. If the reference group requires Two-step then the user must be enrolled (not bypassed) in Two-Step.
- If the service has only a policy group eligibility, then if the user is not in that group then they will see this page. If the policy group requires Two-step (via composite) then the user must be enrolled (not bypassed) in Two-Step. It is easier to add a reference group that requires Two-step.
- If the service has both a reference group and a policy group then the user must be in both groups. If the reference group requires Two-step then the user must be enrolled (not bypassed) in Two-Step.

Basic page:

Two step error:

Timing error:

## Generic error page - custom UI

See examples of screen above

```
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.0.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.0.label = Entity ID
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.0.order = 10
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.0.userQueryType = url
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.0.variableToAssign = cu_var
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.0.variableType = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.1.bindVar0 = cu_var
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.1.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.1.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.1.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.1.label = SP name
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.1.order = 20
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.1.query = select name from sso_prod_entity_front_door where entity_id = ?
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.1.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.1.variableToAssign = cu_spName
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.1.variableType = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.10.bindVar0 = \u0024{subject.id}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.10.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.10.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.10.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.10.label = Has been in workforce
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.10.order = 110
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.10.query = select case when exists (select 1 from penn_custom_ui_mships_v where group_name = 'penn\u003Acommunity\u003AemployeeOrContractorIncludingUphs' and subject_id = ? and subject_source_id in ('pennperson') and has_been_in_group = 'T') then 1 else 0 end as has_been_in_group
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.10.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.10.variableToAssign = cu_hasBeenInWorkforceGroup
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.10.variableType = boolean
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.11.bindVar0 = \u0024{subject.id}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.11.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.11.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.11.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.11.label = Workforce membership ended minutes ago
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.11.order = 120
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.11.query = select coalesce(minutes_since_in_group, 99999999) as min_since_in_group from (select ((extract(epoch from now()) - user_end_secs_since_1970)/60)\u003A\u003Anumeric\u003A\u003Ainteger as minutes_since_in_group from penn_custom_ui_mships_v where group_name = 'penn\u003Acommunity\u003AemployeeOrContractorIncludingUphs' and subject_id = ? and user_end_secs_since_1970 is not null and subject_source_id in ('pennperson') limit 1) as minutes_since_in_group_table
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.11.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.11.variableToAssign = cu_workforceEndedMinutesAgo
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.11.variableType = integer
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.12.bindVar0 = cu_var
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.12.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.12.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.12.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.12.label = Requires two-step
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.12.order = 35
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.12.query = select case when exists (select 1 from sso_prod_entity_front_door where entity_id = ? and requires_twostep = 'T') then 1 else 0 end as requires_twostep_integer
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.12.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.12.variableToAssign = cu_requiresTwostep
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.12.variableType = boolean
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.13.bindVar0 = cu_var
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.13.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.13.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.13.label = Error page type
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.13.order = 140
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.13.query = select error_page_type from sso_prod_entity_front_door where entity_id = ?
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.13.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.13.variableToAssign = cu_errorPageType
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.14.bindVar0 = cu_var
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.14.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.14.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.14.label = Error page URL
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.14.order = 150
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.14.query = select error_page_url from sso_prod_entity_front_door where entity_id = ?
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.14.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.14.variableToAssign = cu_errorPageUrl
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.15.bindVar0 = cu_var
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.15.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.15.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.15.label = Error page text
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.15.order = 160
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.15.query = select error_page_text || '<br /><br />' from sso_prod_entity_front_door where entity_id = ?
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.15.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.15.variableToAssign = cu_errorPageText
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.2.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.2.label = SP found
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.2.order = 30
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.2.script = \u0024{!grouperUtil.isBlank(cu_var) && !grouperUtil.isBlank(cu_spName)}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.2.userQueryType = expressionLanguage
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.2.variableToAssign = cu_spFound
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.2.variableType = boolean
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.3.bindVar0 = cu_var
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.3.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.3.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.3.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.3.label = Eligibility group
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.3.order = 40
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.3.query = select group_name from sso_prod_entity_groups_v where entity_id = ?
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.3.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.3.variableToAssign = cu_frontDoorPolicyGroup
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.3.variableType = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.4.bindVar0 = cu_var
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.4.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.4.bindVar1 = \u0024{subject.id}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.4.bindVar1Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.4.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.4.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.4.label = In eligibility group
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.4.order = 50
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.4.query = select is_in_group from sso_prod_entity_mships2_v where entity_id = ? and subject_id = ?
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.4.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.4.variableToAssign = cu_inEligibilityGroup
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.4.variableType = boolean
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.5.bindVar0 = cu_var
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.5.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.5.bindVar1 = \u0024{subject.id}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.5.bindVar1Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.5.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.5.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.5.label = Has been in eligibility group
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.5.order = 60
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.5.query = select has_been_in_group from sso_prod_entity_mships2_v where entity_id = ? and subject_id = ?
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.5.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.5.variableToAssign = cu_hasBeenInEligibilityGroup
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.5.variableType = boolean
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.6.bindVar0 = cu_var
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.6.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.6.bindVar1 = \u0024{subject.id}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.6.bindVar1Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.6.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.6.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.6.label = Group membership ended minutes ago
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.6.order = 70
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.6.query = select min_since_in_group from sso_prod_entity_mships2_v where entity_id = ? and subject_id = ?
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.6.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.6.variableToAssign = cu_groupMembershipEndedMinutesAgo
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.6.variableType = integer
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.7.bindVar0 = cu_var
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.7.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.7.bindVar1 = \u0024{subject.id}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.7.bindVar1Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.7.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.7.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.7.label = Has been in group for minutes
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.7.order = 80
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.7.query = select min_been_in_group from sso_prod_entity_mships2_v where entity_id = ? and subject_id = ?
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.7.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.7.variableToAssign = cu_beenInGroupForMinutes
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.7.variableType = integer
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.8.bindVar0 = \u0024{subject.id}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.8.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.8.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.8.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.8.label = In two step group
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.8.order = 90
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.8.query = select case when exists (select 1 from penn_custom_ui_mships_v where group_name = 'penn\u003Acommunity\u003Aauthentication\u003AtwoStepUsers' and subject_id = ? and subject_source_id in ('pennperson') and is_user = 'T') then 1 else 0 end as is_user
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.8.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.8.variableToAssign = cu_inTwoStepGroup
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.8.variableType = boolean
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.9.bindVar0 = \u0024{subject.id}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.9.bindVar0Type = string
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.9.configId = grouper
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.9.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.9.label = Has been in two-step group for minutes
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.9.order = 100
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.9.query = select minutes_been_in_group as min_been_in_group from (select ((extract(epoch from now()) - user_start_secs_since_1970)/60)\u003A\u003Anumeric\u003A\u003Ainteger as minutes_been_in_group from penn_custom_ui_mships_v where group_name = 'penn\u003Acommunity\u003Aauthentication\u003AtwoStepUsers' and subject_id = ? and user_start_secs_since_1970 is not null and subject_source_id in ('pennperson') limit 1) as minutes_been_in_group_table
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.9.userQueryType = sql
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.9.variableToAssign = cu_beenInTwoStepGroupForMinutes
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuQuery.9.variableType = integer
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.0.defaultText = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.0.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.0.endIfMatches = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.0.index = 10
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.0.text = <h1>Penn WebLogin - not authorized</h1>
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.0.textType = header
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.1.defaultText = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.1.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.1.endIfMatches = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.1.index = 10
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.1.text =  
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.1.textType = helpLink
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.10.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.10.endIfMatches = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.10.index = -100
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.10.script = \u0024{cu_errorPageType == 'other_url'  }
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.10.text = \u0024{cu_errorPageUrl}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.10.textType = redirectToUrl
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.2.defaultText = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.2.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.2.index = 10
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.2.textBoolean = false
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.2.textType = enrollButtonShow
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.3.endIfMatches = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.3.index = 0
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.3.text = Find a person to see their error page
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.3.textType = managerInstructions
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.4.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.4.endIfMatches = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.4.index = 10
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.4.script = \u0024{grouperUtil.isBlank(cu_var) || grouperUtil.isBlank(cu_spName)}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.4.text = The specifics of the error is unknown.  You are not allowed to access the application.  Discuss your status with your university manager or sponsor.
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.4.textType = enrollmentLabel
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.5.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.5.endIfMatches = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.5.index = 20
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.5.script = \u0024{cu_spFound && cu_inEligibilityGroup && cu_beenInGroupForMinutes > 30 && (!cu_requiresTwostep || (cu_inTwoStepGroup && cu_beenInTwoStepGroupForMinutes > 30)) }
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.5.text = You are allowed to access the application\u003A \u0024{grouperUtil.defaultString(cu_spName)}.  You should not be experiencing an error.  Wait some time and try again or open a ticket to resolve the issue.
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.5.textType = enrollmentLabel
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.6.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.6.endIfMatches = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.6.index = 30
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.6.script = \u0024{cu_spFound && cu_inEligibilityGroup && (!cu_requiresTwostep || (cu_inTwoStepGroup )) }
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.6.text = You are allowed to access the application\u003A \u0024{grouperUtil.defaultString(cu_spName)}.  Since your access has been granted recently, and it takes thirty minutes for the access to be enabled, please try again shortly.
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.6.textType = enrollmentLabel
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.7.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.7.index = 40
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.7.script = \u0024{cu_spFound && cu_requiresTwostep & !cu_inTwoStepGroup }
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.7.text = <font style="color\u003Abrown"><b>Error\u003A</b></font> You must enroll in <a href="https\u003A//www.isc.upenn.edu/how-to/two-step-verification-getting-started">Two-Step Verification</a><br />
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.7.textType = enrollmentLabel
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.8.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.8.index = 50
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.8.script = \u0024{cu_spFound && !cu_inEligibilityGroup}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.8.text = <font style="color\u003Abrown"><b>Error\u003A</b></font> You are not authorized to access the application\u003A \u0024{grouperUtil.defaultString(cu_spName, "")}.  Discuss your status with your university manager or sponsor.<br />
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.8.textType = enrollmentLabel
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.9.enabled = true
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.9.index = -10
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.9.script = \u0024{cu_errorPageType == 'customized'}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.9.text = \u0024{cu_errorPageText}
grouperCustomUI.webLoginFrontDoorErrorPageProd.cuTextConfig.9.textType = enrollmentLabel
grouperCustomUI.webLoginFrontDoorErrorPageProd.groupCanAssignVariables = penn\u003Aisc\u003Ats\u003Aiam\u003Aweblogin\u003Aservice\u003Apolicy\u003AidpFrontDoorProd\u003Asecurity\u003AidpFrontDoorProdCustomUIAdmins
grouperCustomUI.webLoginFrontDoorErrorPageProd.groupCanSeeScreenState = penn\u003Aisc\u003Ats\u003Aiam\u003Aweblogin\u003Aservice\u003Apolicy\u003AidpFrontDoorProd\u003Asecurity\u003AidpFrontDoorProdCustomUIAdmins
grouperCustomUI.webLoginFrontDoorErrorPageProd.groupCanSeeUserEnvironment = penn\u003Aisc\u003Ats\u003Aiam\u003Aweblogin\u003Aservice\u003Apolicy\u003AidpFrontDoorProd\u003Asecurity\u003AidpFrontDoorProdCustomUIAdmins
grouperCustomUI.webLoginFrontDoorErrorPageProd.groupOfManagers = penn\u003Aisc\u003Ats\u003Aiam\u003Aweblogin\u003Aservice\u003Apolicy\u003AidpFrontDoorProd\u003Asecurity\u003AidpFrontDoorProdCustomUIAdmins
grouperCustomUI.webLoginFrontDoorErrorPageProd.groupUUIDOrName = penn\u003Aisc\u003Ats\u003Aiam\u003Aweblogin\u003Aservice\u003Apolicy\u003AidpFrontDoorProd\u003Aservice\u003Aref\u003AidpFrontDoorProdErrorPage
grouperCustomUI.webLoginFrontDoorErrorPageProd.numberOfQueries = 16
grouperCustomUI.webLoginFrontDoorErrorPageProd.numberOfTextConfigs = 11

```

## Error page - custom

This error page has custom HTML embedded. Note all the dynamic features of the generic page exist in the custom page, and it has additional static HTML information as configured.

## Feed from splunk to Grouper about log ins to service providers

There is a table for entity id's

There is a table of login logs from splunk

This is populated by GSH daemon which runs every ten minutes

```
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

//public class Test79splunkSpLogins {

  public static Set<Long> memberIdIndexInGroup(String groupName, Collection<Long> memberIdIndexesCollection) {
    
    int theBatchSize = 1000;
    List<Long> memberIdIndexesList = new ArrayList<>(GrouperUtil.nonNull(memberIdIndexesCollection));
    
    int theNumberOfBatches = GrouperUtil.batchNumberOfBatches(memberIdIndexesList, theBatchSize, false);

    Set<Long> resultMemberIdIndexes = new HashSet<>();
    
    // go through in batches
    for (int i=0;i<theNumberOfBatches;i++) {
      List<Long> memberIdIndexesBatch = GrouperUtil.batchList(memberIdIndexesList, theBatchSize, i);
      List<Long> memberIdIndexesExist = new GcDbAccess().connectionName("awsProdReadonly").sql(
          "select gm.id_index from grouper_memberships_lw_v gmlv, grouper_members gm where " +
          " gm.id = gmlv.member_id and gmlv.list_name = 'members'  " + 
          " and gmlv.group_name = ? and gm.id_index in (" + 
          GrouperClientUtils.appendQuestions(memberIdIndexesBatch.size()) + ")").
          addBindVar(groupName).addBindVars(memberIdIndexesBatch).selectList(Long.class);
      
      resultMemberIdIndexes.addAll(memberIdIndexesExist);
      
    }
    return resultMemberIdIndexes;
  }

//  public static void main(String[] args) {
    
    GrouperStartup.startup();
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Map<String, Object> debugMap = new LinkedHashMap<>();

    String splunkBaseUrl = GrouperUtil.stripLastSlashIfExists(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("splunk.url"));
    String splunkBearerToken = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("splunk.bearerTokenSecret");

    long startedMillis = System.currentTimeMillis();
    
    // https://sls.isc.upenn.edu:8089/servicesNS/-/upenn_isc_attribution/search/jobs/export?search=search%20%60sso_authentications%60&exec_mode=oneshot&output_mode=json
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient().
        assignGrouperHttpMethod(GrouperHttpMethod.get).
        addHeader("Content-Type", "application/json").
        addHeader("Accept", "application/json").
        addHeader("Authorization", "Bearer " + splunkBearerToken).
        assignUrl(splunkBaseUrl + "/servicesNS/-/upenn_isc_attribution/search/jobs/export").
        addUrlParameter("search", "search `sso_authentications`").
        addUrlParameter("output_mode", "json").
        addUrlParameter("exec_mode", "oneshot").
        executeRequest();
    
    int responseCode = grouperHttpClient.getResponseCode();
    String body = grouperHttpClient.getResponseBody();
    if (responseCode != 200) {
      debugMap.put("splunkResponseCode", responseCode);
      throw new RuntimeException(responseCode + "," + body);
    }

    debugMap.put("splunkQueryTookMs", System.currentTimeMillis() - startedMillis);
    
    //  {"preview":false,"offset":37239,"result":{"_time":"2024-01-25 11:08:31.132 EST","user":"aaravrr","dest_app":"courses.upenn.edu/sam_rMReby8Vl3M1BiyVWMAT"}}
    //  {"preview":false,"offset":37252,"result":{"_time":"2024-01-25 11:08:28.546 EST","user":"baronche","dest_app":"https://cluster-prod.apps.upenn.edu/shibboleth"}}
    String[] bodyLines = new String[0];
    if (!StringUtils.isBlank(body)) {
        bodyLines = GrouperUtil.splitTrim(body, "\n");
    }
    
    int linesTotal = bodyLines.length;
    debugMap.put("linesTotal", linesTotal);

    int linesDontMatch = 0;
    int invalidDates = 0;
    int invalidUsers = 0;
    
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
    
    Set<String> pennkeys = new HashSet<String>();
    Set<String> pennids = new HashSet<String>();
    
    Set<String> entityIds = new HashSet<String>();

    Timestamp minTimestamp = null;
    List<Object[]> timeUserSps = new ArrayList<Object[]>();

    Timestamp maxTimestampAlreadyLogged = new GcDbAccess().sql("select max(login_timestamp) from sso_prod_logs_person_sp").select(Timestamp.class);
    if (maxTimestampAlreadyLogged == null) {
      maxTimestampAlreadyLogged = new Timestamp(0L);
    }
    int recordsAlreadyLogged = 0;

    Map<String, Set<String>> entityIdToPennkeyOrPennid = new HashMap<>();
    
    for (String bodyLine : bodyLines) {
      
      if (StringUtils.isBlank(bodyLine)) {
        continue;
      }
      JsonNode mainNode = GrouperUtil.jsonJacksonNode(bodyLine);
      JsonNode resultNode = mainNode == null ? null : GrouperUtil.jsonJacksonGetNode(mainNode, "result");
      String time = resultNode == null ? null : GrouperUtil.jsonJacksonGetString(resultNode, "_time");
      String user = resultNode == null ? null : GrouperUtil.jsonJacksonGetString(resultNode, "user");
      String dest_app = resultNode == null ? null : GrouperUtil.jsonJacksonGetString(resultNode, "dest_app");
      String timeSuffix1 = " EST";
      String timeSuffix2 = " EDT";
      if (StringUtils.isBlank(time) || StringUtils.isBlank(user) || StringUtils.isBlank(dest_app) || (!time.endsWith(timeSuffix1) && !time.endsWith(timeSuffix2))) {
        if (linesDontMatch < 10) {
          debugMap.put("linesDontMatchExample_" + linesDontMatch, bodyLine);
        }
        linesDontMatch++;
        continue;
      }
      time = time.substring(0, time.length() - timeSuffix1.length());
      
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
      df.setTimeZone(cal.getTimeZone());
      Timestamp timestamp = null;
      try {
        Date date = null;
        date = df.parse(time);
        timestamp = new Timestamp(date.getTime());
      } catch (Exception e) {
        if (invalidDates < 10) {
          debugMap.put("invalidDatesExample_" + invalidDates, time);
        }

        invalidDates++;
      }

      if (timestamp.before(maxTimestampAlreadyLogged)) {
        recordsAlreadyLogged++;
        continue;
      }
      
      if (!user.matches("[a-z0-9]{2,8}")) {
        if (invalidUsers < 10) {
          debugMap.put("invalidUser_" + invalidUsers, user);
        }
        if (OtherJobScript.retrieveFromThreadLocal() == null) {
          System.out.println("Invalid user: " + user);
        }
        invalidUsers++;
      }
      
      if (user.matches("[0-9]{2,8}")) {
        pennids.add(user);
      } else {
        pennkeys.add(user);
      }
      entityIds.add(dest_app);
      
      if (minTimestamp == null || timestamp.before(minTimestamp)) {
        minTimestamp = timestamp;
      }

      timeUserSps.add(GrouperUtil.toArrayObject(timestamp, user, dest_app));
      
      Set<String> pennkeyOrPennids = entityIdToPennkeyOrPennid.get(dest_app);
      if (pennkeyOrPennids == null) {
        pennkeyOrPennids = new HashSet<>();
        entityIdToPennkeyOrPennid.put(dest_app, pennkeyOrPennids);
      }
      pennkeyOrPennids.add(user);
      
    }

    if (OtherJobScript.retrieveFromThreadLocal() != null) {
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setTotalCount(bodyLines.length);

    }

    debugMap.put("pennkeysCount", pennkeys.size());
    debugMap.put("pennidsCount", pennids.size());
    debugMap.put("recordsAlreadyLogged", recordsAlreadyLogged);
    debugMap.put("linesDontMatch", linesDontMatch);
    debugMap.put("invalidUsers", invalidUsers);
    debugMap.put("matchedLinesTotal", timeUserSps.size());

    Map<String, Long> loginIdToIdIndex = new HashMap<String, Long>();
    Map<String, String> loginIdToPennid = new HashMap<String, String>();

    // retrieve / create users
    List<String> pennkeyList = new ArrayList<String>(pennkeys);
    int batchSize = 1000;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(pennkeyList.size(), batchSize, false);
    
    int membersCreated = 0;
    int subjectsNotFound = 0;
    
    for (int i=0;i<numberOfBatches;i++) {
      List<String> batchPennkeys = GrouperUtil.batchList(pennkeyList, batchSize, i);
      
      Set<String> batchPennkeysSet = new HashSet<String>(batchPennkeys);

      String sql = """
          select gm.subject_identifier0 AS pennkey, id_index, subject_id from grouper_members gm
          """ + " where subject_identifier0 in (" + GrouperClientUtils.appendQuestions(batchPennkeys.size()) + ")";
      GcDbAccess gcDbAccess = new GcDbAccess().sql(sql).connectionName("awsProdReadonly");
      for (String pennkey : batchPennkeys) {
        gcDbAccess.addBindVar(pennkey);
      }
      List<Object[]> subjectIdentifierIdIndexes = gcDbAccess.selectList(Object[].class);
      for (Object[] subjectIdentifierIdIndex : subjectIdentifierIdIndexes) {
        String subjectIdentifier = (String)subjectIdentifierIdIndex[0];
        Long idIndex = GrouperUtil.longValue(subjectIdentifierIdIndex[1]);
        batchPennkeysSet.remove(subjectIdentifier);
        loginIdToIdIndex.put(subjectIdentifier, idIndex);
        
        String pennId = (String)subjectIdentifierIdIndex[2];
        loginIdToPennid.put(subjectIdentifier, pennId);
        
      }
      
      for (String pennkey : batchPennkeysSet) {
        Subject subject = SubjectFinder.findByIdentifierAndSource(pennkey, "pennperson", false);
        if (subject == null) {
          if (subjectsNotFound < 10) {
            debugMap.put("subjectNotFound_" + subjectsNotFound, pennkey);
          }
          subjectsNotFound++;
          continue;
        }
        loginIdToPennid.put(pennkey, subject.getId());

        Member member = MemberFinder.findBySubject(grouperSession, subject, true);
        if (OtherJobScript.retrieveFromThreadLocal() != null) {
          OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(1);

        }
        loginIdToIdIndex.put(pennkey, member.getIdIndex());
        if (membersCreated < 10) {
          debugMap.put("memberCreated_" + membersCreated, pennkey);
        }
        membersCreated++;
        
      }
    }

    List<String> pennidList = new ArrayList<String>(pennids);
    numberOfBatches = GrouperUtil.batchNumberOfBatches(pennidList.size(), batchSize, false);
    
    int membersPennIdCreated = 0;
    int subjectsPennIdNotFound = 0;

    for (int i=0;i<numberOfBatches;i++) {
      List<String> batchPennids = GrouperUtil.batchList(pennidList, batchSize, i);
      
      Set<String> batchPennidsSet = new HashSet<String>(batchPennids);

      String sql = """
          select gm.subject_id AS subject_id, id_index from grouper_members gm
          """ + " where subject_id in (" + GrouperClientUtils.appendQuestions(batchPennids.size()) + ")";

      GcDbAccess gcDbAccess = new GcDbAccess().sql(sql).connectionName("awsProdReadonly");
      for (String pennid : batchPennids) {
        gcDbAccess.addBindVar(pennid);
        loginIdToPennid.put(pennid, pennid);

      }
      List<Object[]> subjectIdIdIndexes = gcDbAccess.selectList(Object[].class);
      for (Object[] subjectIdIdIndex : subjectIdIdIndexes) {
        String subjectId = (String)subjectIdIdIndex[0];
        Long idIndex = GrouperUtil.longValue(subjectIdIdIndex[1]);
        batchPennidsSet.remove(subjectId);
        loginIdToIdIndex.put(subjectId, idIndex);
        
      }
      
      for (String pennid : batchPennidsSet) {
        Subject subject = SubjectFinder.findByIdAndSource(pennid, "pennperson", false);
        if (subject == null) {
          if (subjectsPennIdNotFound < 10) {
            debugMap.put("subjectNotFound_" + subjectsPennIdNotFound, pennid);
          }
          subjectsPennIdNotFound++;
          continue;
        }
        Member member = MemberFinder.findBySubject(grouperSession, subject, true);
        if (OtherJobScript.retrieveFromThreadLocal() != null) {
          OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(1);

        }
        loginIdToIdIndex.put(pennid, member.getIdIndex());
        if (membersPennIdCreated < 10) {
          debugMap.put("memberCreated_" + membersPennIdCreated, pennid);
        }
        membersPennIdCreated++;
        
      }
    }
    debugMap.put("membersPennIdCreated", membersPennIdCreated);
    debugMap.put("subjectsPennIdNotFound", subjectsPennIdNotFound);

    // retrieve / create sps
    Map<String, Long> entityIdToIdIndex = new HashMap<String, Long>();
    
    List<String> entityIdList = new ArrayList<String>(entityIds);
    numberOfBatches = GrouperUtil.batchNumberOfBatches(entityIdList.size(), batchSize, false);
    
    int entityIdsCreated = 0;

    Long maxEntityIdIndex = new GcDbAccess().sql("select max(id_index) from sso_prod_entity_id").select(long.class);
    if (maxEntityIdIndex == null) {
      maxEntityIdIndex = 0L;
    } else {
      maxEntityIdIndex++;
    }
    
    List<List<Object>> entityIdIdIndexToCreate = new ArrayList<>();
    Map<String, String> entityIdToFrontDoorGroupName = new HashMap<>();
    
    for (int i=0;i<numberOfBatches;i++) {
      List<String> batchEntityIds = GrouperUtil.batchList(entityIdList, batchSize, i);
      
      Set<String> batchEntityIdsSet = new HashSet<String>(batchEntityIds);

      String sql = "select spei.entity_id, spei.id_index, spefd.front_door_policy_group from sso_prod_entity_id spei " +
          " left join sso_prod_entity_front_door spefd on spei.entity_id = spefd.entity_id " +
          " where spei.entity_id in (" + GrouperClientUtils.appendQuestions(batchEntityIds.size()) + ")";
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName("awsProdReadonly").sql(sql);
      for (String sp : batchEntityIds) {
        gcDbAccess.addBindVar(sp);
      }
      List<Object[]> entityIdIdIndexGroupNames = gcDbAccess.selectList(Object[].class);
      for (Object[] entityIdIdIndexGroupName : entityIdIdIndexGroupNames) {
        String entityId = (String)entityIdIdIndexGroupName[0];
        Long idIndex = GrouperUtil.longValue(entityIdIdIndexGroupName[1]);
        String frontDoorGroupName = (String)entityIdIdIndexGroupName[2];
        batchEntityIdsSet.remove(entityId);
        entityIdToIdIndex.put(entityId, idIndex);
        if (!StringUtils.isBlank(frontDoorGroupName)) {
          entityIdToFrontDoorGroupName.put(entityId, frontDoorGroupName);
        }
      }
      
      for (String entityId : batchEntityIdsSet) {
        entityIdToIdIndex.put(entityId, maxEntityIdIndex);
        entityIdIdIndexToCreate.add(GrouperUtil.toListObject(entityId, maxEntityIdIndex));
        if (OtherJobScript.retrieveFromThreadLocal() != null) {
          OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(1);

        }
        if (entityIdsCreated < 10) {
          debugMap.put("entityIdCreated_" + entityIdsCreated, entityId);
        }
        entityIdsCreated++;
        maxEntityIdIndex++;
      }
    }
    
    new GcDbAccess().sql("insert into sso_prod_entity_id (entity_id, id_index) values (?, ?)").batchBindVars(entityIdIdIndexToCreate).executeBatchSql();

    // insert data
    int logRecordsCreated = 0;

    int subjectsIdIndexNotFound = 0;
    
    Long maxLogRecordIdIndex = new GcDbAccess().sql("select max(id_index) from sso_prod_logs_person_sp").select(long.class);
    if (maxLogRecordIdIndex == null) {
      maxLogRecordIdIndex = 0L;
    } else {
      maxLogRecordIdIndex++;
    }
    
    Map<String, Set<Long>> entityIdHasFrontDoorGroupHasMemberIdIndexes = new HashMap(); 

    for (String entityId : entityIdToFrontDoorGroupName.keySet()) {
      String frontDoorGroupName = entityIdToFrontDoorGroupName.get(entityId);
      Set<String> pennkeyOrPennids = entityIdToPennkeyOrPennid.get(entityId);
      List<Long> memberIdIndexes = new ArrayList<>();
      for (String pennkeyOrPennid : pennkeyOrPennids) {
        Long memberIdIndex = loginIdToIdIndex.get(pennkeyOrPennid);
        if (memberIdIndex != null) {
          memberIdIndexes.add(memberIdIndex);
        }
      }
      Set<Long> memberIdsInGroup = memberIdIndexInGroup(frontDoorGroupName, memberIdIndexes);
      entityIdHasFrontDoorGroupHasMemberIdIndexes.put(entityId, memberIdsInGroup);

    }
    Set<Long> memberIdIndexes = new HashSet<>(loginIdToIdIndex.values());
    
    Set<Long> twoStepMemberIdIndexes = memberIdIndexInGroup("penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:enrolledInTwoStep", memberIdIndexes);
    Set<Long> workforceMemberIdIndexes = memberIdIndexInGroup("penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpWorkforce", memberIdIndexes);
    Set<Long> memberMemberIdIndexes = memberIdIndexInGroup("penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:member", memberIdIndexes);
    Set<Long> affiliateMemberIdIndexes = memberIdIndexInGroup("penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpAffiliate", memberIdIndexes);
    Set<Long> recentAffiliateMemberIdIndexes = memberIdIndexInGroup("penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpRecentAffiliate", memberIdIndexes);
    Set<Long> alumOrAffiliateMemberIdIndexes = memberIdIndexInGroup("penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpAffiliateOrAlum", memberIdIndexes);
    Set<Long> uphsOnlyMemberIdIndexes = memberIdIndexInGroup("penn:community:uphsOnly", memberIdIndexes);
    Set<Long> alumOnlyMemberIdIndexes = memberIdIndexInGroup("penn:community:alumni:alumniOnly", memberIdIndexes);
    Set<Long> lockedOutMemberIdIndexes = memberIdIndexInGroup("penn:etc:deprovisioning:usersWhoHaveBeenDeprovisioned_employee", memberIdIndexes);
    Set<Long> uphsNotPennpayNotStudentMemberIdIndexes = memberIdIndexInGroup("penn:community:uphsNotPennpayNotStudent", memberIdIndexes);

    List<List<Object>> logRecordIdIndexUserIndexSpIndexTimestamp = new ArrayList<>();
    
    for (Object[] timeUserSp : timeUserSps) {
      
      Timestamp timestamp = (Timestamp)timeUserSp[0];
      String pennkey = (String)timeUserSp[1];
      Long userIdIndex = loginIdToIdIndex.get(pennkey);
      String pennid = loginIdToPennid.get(pennkey);
      
      String entityId = (String)timeUserSp[2];
      Long entityIdIdIndex = entityIdToIdIndex.get(entityId);
      
      if (userIdIndex == null) {

        if (subjectsIdIndexNotFound < 10) {
          debugMap.put("subjectsIdIndexNotFound_" + subjectsIdIndexNotFound, entityId);
        }
        
        subjectsIdIndexNotFound++;
        continue;
      }
      
      Integer inFrontDoor = null;
      String frontDoorGroupName = entityIdToFrontDoorGroupName.get(entityId);
      if (!StringUtils.isBlank(frontDoorGroupName)) {
        Set<Long> memberIdsInFrontDoorGroup = entityIdHasFrontDoorGroupHasMemberIdIndexes.get(entityId);
        inFrontDoor = memberIdsInFrontDoorGroup != null && memberIdsInFrontDoorGroup.contains(userIdIndex) ? 1 : 0;
      }
      
      Integer nonPersistent = null;
      if (!StringUtils.isBlank(pennid)) {
        nonPersistent = pennid.startsWith("9") ? 1 : 0;
      }
      
      logRecordIdIndexUserIndexSpIndexTimestamp.add(GrouperUtil.toListObject(maxLogRecordIdIndex, userIdIndex, entityIdIdIndex, timestamp,
          twoStepMemberIdIndexes.contains(userIdIndex) ? 1 : 0, workforceMemberIdIndexes.contains(userIdIndex) ? 1 : 0,
          memberMemberIdIndexes.contains(userIdIndex) ? 1 : 0, affiliateMemberIdIndexes.contains(userIdIndex) ? 1 : 0,
          recentAffiliateMemberIdIndexes.contains(userIdIndex) ? 1 : 0, alumOrAffiliateMemberIdIndexes.contains(userIdIndex) ? 1 : 0,
          uphsOnlyMemberIdIndexes.contains(userIdIndex) ? 1 : 0, alumOnlyMemberIdIndexes.contains(userIdIndex) ? 1 : 0,
          inFrontDoor, lockedOutMemberIdIndexes.contains(userIdIndex) ? 1 : 0, nonPersistent, uphsNotPennpayNotStudentMemberIdIndexes.contains(userIdIndex) ? 1 : 0));
      if (OtherJobScript.retrieveFromThreadLocal() != null) {
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(1);

      }
      if (logRecordsCreated < 10) {
        debugMap.put("logRecordsCreated_" + logRecordsCreated, timestamp.toString() + ", " + pennkey + ", " + entityId);
      }
      logRecordsCreated++;
      maxLogRecordIdIndex++;
    }
    
    
    
    new GcDbAccess().sql("insert into sso_prod_logs_person_sp (id_index, user_member_id_index, sp_entity_id_index, login_timestamp, two_step_enrolled, workforce, member, "
        + " affiliate, recent_affiliate, alum_or_affiliate, uphs_only, alum_only, in_front_door, locked_out, "
        + "non_persistent, uphs_not_pennpay_not_student) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)").
      batchBindVars(logRecordIdIndexUserIndexSpIndexTimestamp).executeBatchSql();

    debugMap.put("subjectsIdIndexNotFound", subjectsIdIndexNotFound);
    debugMap.put("logRecordsCreated", logRecordsCreated);
    
    if (OtherJobScript.retrieveFromThreadLocal() != null) {
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.mapToString(debugMap));
    } else {
      System.exit(0);
    }
//  }
//}
```

## Managers and owners in Grouper for each service provider

## Manage front door with GSH template

Config

```
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.defaultRunButtonFolderUuidOrName = penn\u003Aisc\u003Ats\u003Aiam\u003Aweblogin\u003Aservice\u003Atemplates\u003AidpRegistrationProd
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.displayErrorOutput = true
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.folderShowOnDescendants = certainFolders
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.folderShowType = certainFolders
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.folderUuidToShow = penn\u003Aisc\u003Ats\u003Aiam\u003Aweblogin\u003Aservice\u003Atemplates\u003AidpRegistrationProd
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.groupUuidCanRun = penn\u003Aisc\u003Ats\u003Aiam\u003Aweblogin\u003Aservice\u003Asecurity\u003AcanRunIdpTemplate
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.gshTemplate = //
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.0.description = What do you want to do with front door config?  add, edit, delete, enable, disable
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.0.dropdownCsvValue = add, edit, delete, enable, disable
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.0.formElementType = dropdown
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.0.index = 10
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.0.label = Action
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.0.name = gsh_input_action
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.0.required = true
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.1.description = Entity ID of the service provider
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.1.dropdownValueFormat = dynamicFromTemplate
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.1.formElementType = dropdown
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.1.index = 20
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.1.label = Entity ID
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.1.name = gsh_input_entityId
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.1.required = true
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.1.showEl = \u0024{gsh_input_action == 'add' || gsh_input_action == 'edit' || gsh_input_action == 'delete'}
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.10.defaultValue = generic
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.10.description = Generic is the built-in error page.  Customized is the generic page but with customized text.  Other URL will redirect to another URL for the error page.  The default value is 'generic'.
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.10.dropdownCsvValue = generic, customized, other_url
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.10.formElementType = dropdown
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.10.index = 120
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.10.label = Error page type
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.10.name = gsh_input_errorPageType
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.10.showEl = \u0024{ (gsh_input_action == 'add' || gsh_input_action == 'edit') && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_entityId ) }
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.11.description = URL to redirect to for an error for this app
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.11.index = 130
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.11.label = Error page URL
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.11.maxLength = 2000
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.11.name = gsh_input_errorPageUrl
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.11.showEl = \u0024{  (gsh_input_action == 'add' || gsh_input_action == 'edit') && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_entityId )  }
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.11.validationType = none
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.12.description = Enter the HTML to be added to the error page
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.12.formElementType = textarea
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.12.index = 140
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.12.label = Error page text HTML
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.12.maxLength = 4000
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.12.name = gsh_input_errorPageText
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.12.showEl = \u0024{  (gsh_input_action == 'add' || gsh_input_action == 'edit') && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_entityId )  }
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.12.validationType = none
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.13.description = Enter the ID path of the group which is the policy group for this service provider.  This is separate from the front door policy
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.13.index = 80
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.13.label = Policy group name
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.13.maxLength = 1000
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.13.name = gsh_input_policyGroupName
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.13.showEl = \u0024{  (gsh_input_action == 'add' || gsh_input_action == 'edit') && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_entityId )  }
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.13.validationType = none
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.2.description = Write a short but descriptive name for this service provider.  If this is an application for a certain team or organization, include that.  If there are multiple environments (e.g. dev / test / prod).  For example\u003A ISC Jira prod.
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.2.index = 30
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.2.label = App name
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.2.maxLength = 100
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.2.name = gsh_input_name
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.2.required = true
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.2.showEl = \u0024{  (gsh_input_action == 'add' || gsh_input_action == 'edit') && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_entityId ) }
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.2.validationType = none
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.3.description = Describe the application so that it can be easily searched for or differentiated from other service providers.
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.3.formElementType = textarea
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.3.index = 40
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.3.label = Description
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.3.maxLength = 2000
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.3.name = gsh_input_description
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.3.required = true
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.3.showEl = \u0024{  (gsh_input_action == 'add' || gsh_input_action == 'edit') && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_entityId ) }
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.3.trimWhitespace = true
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.3.validationType = none
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.4.description = Admins can manage the settings of this service provider.  Input a comma separated list of pennkeys, pennids, or penngroup ID paths who admin this service provider.
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.4.index = 50
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.4.label = Admins
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.4.maxLength = 2000
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.4.name = gsh_input_adminGroup
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.4.showEl = \u0024{ gsh_input_action == 'add' && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_action ) && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_entityId ) }
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.4.validationType = none
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.5.description = Owners can manage the settings of this service provider and can manage the list of managers and owners.  Input a comma separated list of pennkeys, pennids, or penngroup ID paths who own this service provider.
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.5.index = 60
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.5.label = Owners
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.5.maxLength = 2000
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.5.name = gsh_input_ownerGroup
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.5.showEl = \u0024{ gsh_input_action == 'add' && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_action ) && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_entityId ) }
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.5.validationType = none
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.6.description = Select the coarse grained policy group that users must be in to access the application.  See the PennGroups wiki for information.
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.6.dropdownCsvValue = workforceInTwoStep, workforce, memberInTwoStep, member, affiliateInTwoStep, affiliate, affiliateOrAlumInTwoStep, affiliateOrAlum, inTwoStep, recentAffiliate, recentAffiliateInTwoStep, none
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.6.dropdownValueFormat = csv
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.6.formElementType = dropdown
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.6.index = 70
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.6.label = Front door reference group
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.6.name = gsh_input_frontDoorPolicy
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.6.showEl = \u0024{ (gsh_input_action == 'add' || gsh_input_action == 'edit') && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_entityId ) }
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.7.description = Enter a URL of this application that can be used to test or tell users to log in.  If there is no one URL, then provide a jump page, service page, or one of the URLs.
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.7.index = 90
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.7.label = URL of this application
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.7.maxLength = 2000
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.7.name = gsh_input_url
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.7.required = true
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.7.showEl = \u0024{  (gsh_input_action == 'add' || gsh_input_action == 'edit') && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_entityId ) }
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.7.validationType = none
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.8.description = Provide instructions for logging in to the application especially if it is not obvious.
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.8.formElementType = textarea
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.8.index = 100
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.8.label = URL instructions
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.8.maxLength = 2000
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.8.name = gsh_input_urlInstructions
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.8.required = true
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.8.showEl = \u0024{  (gsh_input_action == 'add' || gsh_input_action == 'edit') && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_entityId ) }
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.8.validationType = none
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.9.defaultValue = false
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.9.description = If the front door group(s) (usually the reference group, but could be policy group) requires Two-step and the error page can help users then set this to true.  This does not require Two-step and only affects the error page.
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.9.index = 110
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.9.label = Policy requires Two-step
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.9.name = gsh_input_requiresTwostep
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.9.showEl = \u0024{  (gsh_input_action == 'add' || gsh_input_action == 'edit') && !org.apache.commons.lang3.StringUtils.isBlank(gsh_input_entityId ) }
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.input.9.type = boolean
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.moreActionsLabel = WebLogin front door authorization config
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.numberOfInputs = 14
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.runAsType = GrouperSystem
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.runButtonGroupOrFolder = folder
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.securityRunType = specifiedGroup
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.showInMoreActions = true
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.showOnFolders = true
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.templateDescription = Configure a Service Provider's settings for front door authorization
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.templateName = WebLogin front door authorization config
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.templateType = gsh
grouperGshTemplate.ssoCoarseGrainedAuthzConfig.templateVersion = V2
```

Source

```
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateDecorateForUiInput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputConfigAndValue;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

public class Test76ssoCoarseGrainedConfig extends GshTemplateV2 {
  
  @Override
  public void decorateTemplateForUiDisplay(
      GshTemplateDecorateForUiInput gshTemplateDecorateForUiInput) {
    
    String stemName = gshTemplateDecorateForUiInput.getOwnerStemName();
    boolean prod = StringUtils.equals(stemName, "penn:isc:ts:iam:weblogin:service:templates:idpRegistrationProd");
    if (!stemName.equals("penn:isc:ts:iam:weblogin:service:templates:idpRegistrationStage")
        && !stemName.equals("penn:isc:ts:iam:weblogin:service:templates:idpRegistrationProd")) {
      throw new RuntimeException("Invalid owner folder, should start with: 'penn:isc:ts:iam:weblogin:service:templates:' and be prod or stage");
    }

    String adminGroupName = "penn:isc:ts:iam:weblogin:service:policy:idpFrontDoorStaging:security:idpFrontDoorStagingUpdaters";

    // ssoFrontDoor_shibdev2_access_admins
    String managerFolderPrefix = "penn:isc:ts:iam:weblogin:service:policy:idpFrontDoorStaging:service:policy:managers:";
    String tableName = "sso_stage_entity_front_door";
    if (prod) {
      tableName = "sso_prod_entity_front_door";
      adminGroupName = "penn:isc:ts:iam:weblogin:service:policy:idpFrontDoorProd:security:idpFrontDoorProdUpdaters";
      managerFolderPrefix = "penn:isc:ts:iam:weblogin:service:policy:idpFrontDoorProd:service:policy:managers:";
    }
    Group adminGroup = GroupFinder.findByName(adminGroupName, true);
    boolean isAdmin = adminGroup.hasMember(gshTemplateDecorateForUiInput.getCurrentSubject());

    
    Map<String, GshTemplateInputConfigAndValue> gshTemplateInputConfigAndValues = gshTemplateDecorateForUiInput.getGshTemplateInputConfigAndValues();
    GshTemplateInputConfigAndValue action = gshTemplateInputConfigAndValues.get("gsh_input_action");
    GshTemplateInputConfigAndValue entityIdInput = gshTemplateInputConfigAndValues.get("gsh_input_entityId");
    
    GshTemplateInputConfigAndValue nameInput = gshTemplateInputConfigAndValues.get("gsh_input_name");
    GshTemplateInputConfigAndValue descriptionInput = gshTemplateInputConfigAndValues.get("gsh_input_description");
    GshTemplateInputConfigAndValue adminsInput = gshTemplateInputConfigAndValues.get("gsh_input_admins");
    GshTemplateInputConfigAndValue ownersInput = gshTemplateInputConfigAndValues.get("gsh_input_owners");
    GshTemplateInputConfigAndValue frontDoorPolicyInput = gshTemplateInputConfigAndValues.get("gsh_input_frontDoorPolicy");
    GshTemplateInputConfigAndValue urlInput = gshTemplateInputConfigAndValues.get("gsh_input_url");
    GshTemplateInputConfigAndValue urlInstructionsInput = gshTemplateInputConfigAndValues.get("gsh_input_urlInstructions");
    GshTemplateInputConfigAndValue requiresTwoStepInput = gshTemplateInputConfigAndValues.get("gsh_input_requiresTwostep");
    GshTemplateInputConfigAndValue errorPageTypeInput = gshTemplateInputConfigAndValues.get("gsh_input_errorPageType");
    GshTemplateInputConfigAndValue errorPageUrlInput = gshTemplateInputConfigAndValues.get("gsh_input_errorPageUrl");
    GshTemplateInputConfigAndValue errorPageTextInput = gshTemplateInputConfigAndValues.get("gsh_input_errorPageText");
    GshTemplateInputConfigAndValue policyGroupNameInput = gshTemplateInputConfigAndValues.get("gsh_input_policyGroupName");
    
    if (StringUtils.equals(action.getValue(), "add")) {

      List<String> entityIds = new GcDbAccess().sql("select entity_id from sso_prod_entity_id spei where not exists (select 1 from " + tableName + " sefd where sefd.entity_id = spei.entity_id) order by 1").selectList(String.class);
      
      List<MultiKey> keysAndValues = new ArrayList<MultiKey>();
      
      keysAndValues.add(new MultiKey("", ""));
      for (String entityId : entityIds) {
        keysAndValues.add(new MultiKey(entityId, entityId));
      }
      
      entityIdInput.getGshTemplateInputConfig().setDropdownKeysAndLabels(keysAndValues);
      
    } else if (StringUtils.equals(action.getValue(), "delete") || StringUtils.equals(action.getValue(), "edit")) {

      List<String> entityIds = null;
      
      if (isAdmin) {
        
        entityIds = new GcDbAccess().sql("select entity_id from " + tableName + " order by 1").selectList(String.class);
        
      } else {
        
        entityIds = new GcDbAccess().sql(String.format("""
            select entity_id from %s stefd
            WHERE EXISTS (SELECT 1 FROM GROUPER_MEMBERSHIPS_LW_V gmlv
            WHERE gmlv.SUBJECT_ID = ?
            AND gmlv.SUBJECT_SOURCE = 'pennperson'
            AND gmlv.LIST_NAME = 'members' 
            AND gmlv.GROUP_NAME = '%sssoFrontDoor_'
            || stefd.name_without_special_characters || '_admins') order by 1
                      """, tableName, managerFolderPrefix)).addBindVar(gshTemplateDecorateForUiInput.getCurrentSubject().getId()).selectList(String.class);
        
      }
      
      List<MultiKey> keysAndValues = new ArrayList<MultiKey>();
      
      keysAndValues.add(new MultiKey("", ""));
      for (String entityId : entityIds) {
        keysAndValues.add(new MultiKey(entityId, entityId));
      }
      
      entityIdInput.getGshTemplateInputConfig().setDropdownKeysAndLabels(keysAndValues);

    }

    //  gsh_input_action
    //  gsh_input_entityId
    //  gsh_input_name
    //  gsh_input_description
    //  gsh_input_admins
    //  gsh_input_owners
    //  gsh_input_frontDoorPolicy
    //  gsh_input_url
    //  gsh_input_urlInstructions
    //  gsh_input_requiresTwostep

    if (StringUtils.equals(gshTemplateDecorateForUiInput.getEventConfigId(), "gsh_input_action")) {
      if (entityIdInput != null) {
        entityIdInput.setValue(null);
      }
    }
    
    if (StringUtils.equals(gshTemplateDecorateForUiInput.getEventConfigId(), "gsh_input_action")
        || (entityIdInput != null && StringUtils.isBlank(entityIdInput.getValue()))
        || (StringUtils.equals(gshTemplateDecorateForUiInput.getEventConfigId(), "gsh_input_entityId")
            && StringUtils.equals(action.getValue(), "add"))) {
      if (nameInput != null) {
        nameInput.setValue(null);
      }
      if (descriptionInput != null) {
        descriptionInput.setValue(null);
      }
      if (adminsInput != null) {
        adminsInput.setValue(null);
      }
      if (ownersInput != null) {
        ownersInput.setValue(null);
      }
      if (frontDoorPolicyInput != null) {
        frontDoorPolicyInput.setValue(null);
      }
      if (urlInput != null) {
        urlInput.setValue(null);
      }
      if (urlInstructionsInput != null) {
        urlInstructionsInput.setValue(null);
      }
      if (requiresTwoStepInput != null) {
        requiresTwoStepInput.setValue(null);
      }
      if (errorPageTypeInput != null) {
        errorPageTypeInput.setValue(null);
      }
      if (errorPageUrlInput != null) {
        errorPageUrlInput.setValue(null);
      }
      if (errorPageTextInput != null) {
        errorPageTextInput.setValue(null);
      }
      if (policyGroupNameInput != null) {
        policyGroupNameInput.setValue(null);
      }

    }
    
    
    if (StringUtils.equals(gshTemplateDecorateForUiInput.getEventConfigId(), "gsh_input_entityId")
        && StringUtils.equals(action.getValue(), "edit") && entityIdInput != null && !StringUtils.isBlank(entityIdInput.getValue())) {
      
      String entityId = entityIdInput.getValue();
      
      Object[] dataRow = new GcDbAccess().sql("select name_without_special_characters, name, description, url, url_instructions, "
          + "coarse_grained_policy, requires_twostep, error_page_type, error_page_url, error_page_text, front_door_policy_group from " + tableName + " where entity_id = ?").addBindVar(entityId).
        select(Object[].class);
      
      String nameWithoutSpecialCharacters = (String)dataRow[0];
      String name = (String)dataRow[1];
      String description = (String)dataRow[2];
      String url = (String)dataRow[3];
      String urlInstructions = (String)dataRow[4];
      String coarseGrainedPolicy = (String)dataRow[5];
      String requiresTwostep = (String)dataRow[6];
      String errorPageType = (String)dataRow[7];
      String errorPageUrl = (String)dataRow[8];
      String errorPageText = (String)dataRow[9];
      String policyFrontDoorGroup = (String)dataRow[10];

      nameInput.setValue(name);
      descriptionInput.setValue(description);
      urlInput.setValue(url);
      urlInstructionsInput.setValue(urlInstructions);
      frontDoorPolicyInput.setValue(coarseGrainedPolicy);
      requiresTwoStepInput.setValue(GrouperUtil.booleanValue(requiresTwostep, false) ? "true" : "false");
      errorPageTypeInput.setValue(errorPageType);
      errorPageUrlInput.setValue(errorPageUrl);
      errorPageTextInput.setValue(errorPageText);
      policyGroupNameInput.setValue(policyFrontDoorGroup);

    }
    
  }

  Map<String, String> bucketLabelToGroupName = GrouperUtil.toMap(
      "workforceInTwoStep", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:employeesInTwoStep",
      "workforce", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpWorkforce",
      "memberInTwoStep", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:memberInTwoStep",
      "member", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:member",
      "affiliateInTwoStep", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpAffiliateInTwoStep",
      "affiliate", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpAffiliate",
      "affiliateOrAlumInTwoStep", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpAffiliateOrAlumInTwoStep",
      "affiliateOrAlum", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpAffiliateOrAlum",
      "inTwoStep", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:enrolledInTwoStep",
      "recentAffiliateInTwoStep", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpRecentAffiliateInTwoStep",
      "recentAffiliate", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpRecentAffiliate",
      "none", "none");
  
  

  public void convertListOfSubjectStringsToSubjects(String inputName, String subjectsString, Set<Subject> subjects, GshTemplateOutput gsh_builtin_gshTemplateOutput) {
    Set<String> subjectStringSet = GrouperUtil.splitTrimToSet(subjectsString, ",");
    for (String subjectString : subjectStringSet) {
      if (subjectString.contains(":")) {
        Group group = GroupFinder.findByName(subjectString, false);
        if (group == null) {
          gsh_builtin_gshTemplateOutput.addValidationLine(inputName, "Cannot find group: '" + subjectString + "'");
        } else {
          subjects.add(group.toSubject());
        }
      } else {
        Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(subjectString, "pennperson", false);
        if (subject == null) {
          gsh_builtin_gshTemplateOutput.addValidationLine(inputName, "Cannot find person: '" + subjectString + "'");
        } else {
          subjects.add(subject);
        }
      }
    }
  }
  
  /**
   * TODO remove after upgrade
   * encrypt a message to SHA
   * @param plaintext
   * @return the hash
   */
  public synchronized String encryptShaHex(String plaintext) {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("SHA"); //step 2
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    try {
      md.update(plaintext.getBytes("UTF-8")); //step 3
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    byte[] raw = md.digest(); //step 4
    String hex = HexFormat.of().formatHex(raw);
    return hex; //step 6
  }

  public void createLdapModificationItem(List<LdapModificationItem> ldapModificationItems, String attributeName, String oldValue, String newValue) {
    
    if (StringUtils.isBlank(oldValue)) {
      ldapModificationItems.add(new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute(attributeName, newValue)));
    } else if (StringUtils.isBlank(newValue)) {
      ldapModificationItems.add(new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute(attributeName)));
    } else {
      ldapModificationItems.add(new LdapModificationItem(LdapModificationType.REPLACE_ATTRIBUTE, new LdapAttribute(attributeName, newValue)));
    }
  }
  
  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    Subject subjectLoggedIn = gshTemplateV2input.getGsh_builtin_gshTemplateRuntime().getCurrentSubject();

    // Analyze user access, Analyze user history, Analyze application
    String gsh_input_action = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_action");
    
    String gsh_input_entityId = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_entityId");
    String gsh_input_name = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_name");
    String gsh_input_description = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_description");
    String gsh_input_admins = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_admins");
    String gsh_input_owners = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_owners");
    String gsh_input_frontDoorPolicy = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_frontDoorPolicy");
    String gsh_input_url = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_url");
    String gsh_input_urlInstructions = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_urlInstructions");
    boolean gsh_input_requiresTwostep = gshTemplateV2input.getGsh_builtin_inputBoolean("gsh_input_requiresTwostep");
    String gsh_input_errorPageType = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_errorPageType");
    String gsh_input_errorPageText = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_errorPageText");
    String gsh_input_errorPageUrl = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_errorPageUrl");
    
    String gsh_input_policyGroupName = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_policyGroupName");
        
    String stemName = gshTemplateV2input.getGsh_builtin_ownerStemName();
    boolean prod = StringUtils.equals(stemName, "penn:isc:ts:iam:weblogin:service:templates:idpRegistrationProd");
    if (!stemName.startsWith("penn:isc:ts:iam:weblogin:service:templates:")) {
      throw new RuntimeException("Invalid owner folder, should start with: 'penn:isc:ts:iam:weblogin:service:templates:'");
    }

    String adminGroupName = "penn:isc:ts:iam:weblogin:service:policy:idpFrontDoorStaging:security:idpFrontDoorStagingUpdaters";
    String tableName = "sso_stage_entity_front_door";
    String ldapConnection = "iamLdapStagingApplications";
    String pennGroupsFolderExtension = "idpFrontDoorStaging";
    String canRunTemplateGroupName = "penn:isc:ts:iam:weblogin:service:security:canRunIdpTemplateStage";
    if (prod) {
      tableName = "sso_prod_entity_front_door";
      ldapConnection = "iamLdapProdApplications";
      pennGroupsFolderExtension = "idpFrontDoorProd";
      canRunTemplateGroupName = "penn:isc:ts:iam:weblogin:service:security:canRunIdpTemplate";
      adminGroupName = "penn:isc:ts:iam:weblogin:service:policy:idpFrontDoorProd:security:idpFrontDoorProdUpdaters";
    }

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");

    Group adminGroup = GroupFinder.findByName(adminGroupName, true);
    boolean isAdmin = adminGroup.hasMember(subjectLoggedIn);
    
    String inputPolicyGroupEntitlement = null;
    
    if (!StringUtils.isBlank(gsh_input_policyGroupName)) {
      Group policyGroup = GroupFinder.findByName(gsh_input_policyGroupName, false);
      if (policyGroup == null) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_policyGroupName", "Policy group '" + gsh_input_policyGroupName + "' not found, make sure to copy/paste the ID path");
        return;
      }
      inputPolicyGroupEntitlement = "urn:mace:upenn.edu:" + gsh_input_policyGroupName;
    }
    
    boolean actionAdd = StringUtils.equals("add", gsh_input_action);
    
    if (actionAdd && !isAdmin) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_action", "You are not allowed to add front door configurations, please use the remedy tile to request a configuration");
      return;
    }
    
    boolean actionEdit = StringUtils.equals("edit", gsh_input_action);
    boolean actionDelete = StringUtils.equals("delete", gsh_input_action);
    if (!actionAdd && !actionDelete && !actionEdit) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_action", "Action not implemented yet, just add for now");
      return;
    }
    
    if (StringUtils.equals(gsh_input_errorPageType, "customized") && StringUtils.isBlank(gsh_input_errorPageText)) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_errorPageText", "Error page text is required");
      return;
    }

    if (StringUtils.equals(gsh_input_errorPageType, "other_url") && StringUtils.isBlank(gsh_input_errorPageUrl)) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_errorPageUrl", "Error page URL is required");
      return;
    }

    if (!StringUtils.equals(gsh_input_errorPageType, "other_url") && !StringUtils.isBlank(gsh_input_errorPageUrl)) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_errorPageUrl", "Error page URL must be blank if the error page type is not 'other_url'");
      return;
    }

    if (!StringUtils.equals(gsh_input_errorPageType, "customized") && !StringUtils.isBlank(gsh_input_errorPageText)) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_errorPageText", "Error page text must be blank if the error page type is not 'custommized'");
      return;
    }

    String entityIdShaHex = encryptShaHex(gsh_input_entityId);

    String nameWithoutSpecialCharacters = gsh_input_name == null ? null : gsh_input_name.replaceAll("[^A-Za-z0-9_-]", "_");

    if (actionEdit || actionDelete) {
      nameWithoutSpecialCharacters = new GcDbAccess().sql("select name_without_special_characters from " + tableName + " where entity_id = ?").
          addBindVar(gsh_input_entityId).select(String.class);
    }    

    //  entity_id
    //  entity_id_sha_hex
    //  name_without_special_characters
    //  name
    //  description
    //  url
    //  url_instructions
    
    int rowsForNameWithoutSpecialChars = new GcDbAccess().sql("select count(1) from " + tableName + " where name_without_special_characters = ?").
      addBindVar(nameWithoutSpecialCharacters).select(int.class);

    if (actionAdd && rowsForNameWithoutSpecialChars > 0) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_name", "Name conflicts with existing SP registration! '" + gsh_input_name + "'");
    }

    if ((actionDelete || actionEdit) && rowsForNameWithoutSpecialChars != 1) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_name", "Name not found with existing SP registration! '" + gsh_input_name + "'");
    }

    int rowsForEntityId = new GcDbAccess().sql("select count(1) from " + tableName + " where entity_id = ?").
      addBindVar(gsh_input_entityId).select(int.class);

    if (actionAdd && rowsForEntityId > 0) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_entityId", "Entity ID conflicts with existing SP registration! '" + gsh_input_entityId + "'");
    }

    if (actionEdit && rowsForEntityId != 1) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_entityId", "Cannot find entity ID! '" + gsh_input_entityId + "'");
    }

    String groupExtensionAdmins = "ssoFrontDoor_" + nameWithoutSpecialCharacters + "_admins";
    String groupExtensionOwners = "ssoFrontDoor_" + nameWithoutSpecialCharacters + "_owners";
    
    String adminsGroupName = "penn:isc:ts:iam:weblogin:service:policy:" + pennGroupsFolderExtension + ":service:policy:managers:" + groupExtensionAdmins;
    
    Group adminsGroup = GroupFinder.findByName(adminsGroupName, false);

    if (!isAdmin) {
      if (adminsGroup == null) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_entityId", "Cannot find admins group! '" + groupExtensionAdmins + "'");
      } else {
        if (!adminsGroup.hasMember(subjectLoggedIn)) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_entityId", "You are not allowed to make changes to this entity! '" + gsh_input_entityId + "'");
        }
      }
    }
    
    Set<Subject> admins = new HashSet<>();
    
    if (!StringUtils.isBlank(gsh_input_admins) && actionAdd) {
      convertListOfSubjectStringsToSubjects("gsh_input_admins", gsh_input_admins, admins, gsh_builtin_gshTemplateOutput);
    }
    
    Set<Subject> owners = new HashSet<>();
    
    if (!StringUtils.isBlank(gsh_input_owners) && actionAdd) {
      convertListOfSubjectStringsToSubjects("gsh_input_owners", gsh_input_owners, owners, gsh_builtin_gshTemplateOutput);
    }
    if (StringUtils.isBlank(gsh_input_frontDoorPolicy)) {
      gsh_input_frontDoorPolicy = "none";
    }
    
    
    if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
      gsh_builtin_gshTemplateOutput.assignIsError(true);
      return;
    }
    
    if (actionAdd) {
      new GcDbAccess().sql("insert into " + tableName + " (entity_id, entity_id_sha_hex, "
          + "name_without_special_characters, name, description, url, url_instructions, coarse_grained_policy, front_door_policy_group, "
          + "requires_twostep, error_page_type, error_page_url, error_page_text) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ").
        addBindVar(gsh_input_entityId).addBindVar(entityIdShaHex).addBindVar(nameWithoutSpecialCharacters).
        addBindVar(gsh_input_name).addBindVar(gsh_input_description).addBindVar(gsh_input_url).
        addBindVar(gsh_input_urlInstructions).addBindVar(gsh_input_frontDoorPolicy).addBindVar(gsh_input_policyGroupName).
        addBindVar(gsh_input_requiresTwostep ? "T" : "F").addBindVar(gsh_input_errorPageType).
        addBindVar(gsh_input_errorPageUrl).addBindVar(gsh_input_errorPageText).executeSql();

      if (adminsGroup == null) {
        adminsGroup = new GroupSave().assignName(adminsGroupName).assignDescription("Admins for " + gsh_input_name + ".  " + gsh_input_description).save();
      }

      for (Subject subject : admins) {
        adminsGroup.addMember(subject, false);
      }
  
      String ownersGroupName = "penn:isc:ts:iam:weblogin:service:policy:" + pennGroupsFolderExtension + ":service:policy:managers:" + groupExtensionOwners;
  
      Group ownersGroup = new GroupSave().assignName(ownersGroupName).assignDescription("Owners for " + gsh_input_name + ".  " + gsh_input_description).save();
      for (Subject subject : owners) {
        ownersGroup.addMember(subject, false);
      }
      
      adminsGroup.addMember(ownersGroup.toSubject());
      
      adminsGroup.grantPriv(ownersGroup.toSubject(), AccessPrivilege.UPDATE, false);
      adminsGroup.grantPriv(ownersGroup.toSubject(), AccessPrivilege.READ, false);
  
      ownersGroup.grantPriv(ownersGroup.toSubject(), AccessPrivilege.UPDATE, false);
      ownersGroup.grantPriv(ownersGroup.toSubject(), AccessPrivilege.READ, false);
      
      Group canRunTemplateGroup = GroupFinder.findByName(canRunTemplateGroupName, true);
      canRunTemplateGroup.addMember(adminsGroup.toSubject(), false);
  
      // cn=c77a2c1fd25902a3e09e313af5cd4d0da29b7405,ou=applications,dc=penncommunity,dc=upenn,dc=edu
      String dn = "cn=" + entityIdShaHex + ",ou=applications,dc=authz,dc=upenn,dc=edu";
      LdapEntry ldapEntry = new LdapEntry(dn);
      ldapEntry.addAttribute(new LdapAttribute("cn", entityIdShaHex));
      ldapEntry.addAttribute(new LdapAttribute("objectClass", "sAMLApp"));
      ldapEntry.addAttribute(new LdapAttribute("appEntityId", gsh_input_entityId));
      ldapEntry.addAttribute(new LdapAttribute("appAdminEntitlement", "urn:mace:upenn.edu:" + adminsGroupName));
      
      ldapEntry.addAttribute(new LdapAttribute("appOwnerEntitlement", "urn:mace:upenn.edu:" + ownersGroupName));
      
      ldapEntry.addAttribute(new LdapAttribute("appDescription", gsh_input_description));
      if (!StringUtils.equals("none", gsh_input_frontDoorPolicy) && !StringUtils.isBlank(gsh_input_frontDoorPolicy)) {
        ldapEntry.addAttribute(new LdapAttribute("appMACPolicy", gsh_input_frontDoorPolicy));
      }
      ldapEntry.addAttribute(new LdapAttribute("appModifiedBy", subjectLoggedIn.getId()));
      ldapEntry.addAttribute(new LdapAttribute("appName", gsh_input_name));
      if (!StringUtils.isBlank(inputPolicyGroupEntitlement)) {
        ldapEntry.addAttribute(new LdapAttribute("appPolicyGroup", inputPolicyGroupEntitlement));
      }

      if (!StringUtils.isBlank(gsh_input_urlInstructions)) {
        ldapEntry.addAttribute(new LdapAttribute("appTestNote", gsh_input_urlInstructions));
      }
      if (!StringUtils.isBlank(gsh_input_url)) {
        ldapEntry.addAttribute(new LdapAttribute("appTestURL", gsh_input_url));
      }
  
      LdapSessionUtils.ldapSession().create(ldapConnection, ldapEntry);
      
      gsh_builtin_gshTemplateOutput.addOutputLine("Success: added a new service provider");
      
    } else if (actionEdit) {
      
      Object[] dataRow = new GcDbAccess().sql("select name, description, url, url_instructions, "
          + "coarse_grained_policy, requires_twostep, error_page_type, error_page_url, error_page_text, front_door_policy_group from " + tableName + " where entity_id = ?").addBindVar(gsh_input_entityId).
        select(Object[].class);
      
      String name = (String)dataRow[0];
      String description = (String)dataRow[1];
      String url = (String)dataRow[2];
      String urlInstructions = (String)dataRow[3];
      String coarseGrainedPolicy = (String)dataRow[4];
      String requiresTwoStep = (String)dataRow[5];
      String errorPageType = (String)dataRow[6];
      String errorPageUrl = (String)dataRow[7];
      String errorPageText = (String)dataRow[8];
      String frontDoorPolicyGroup = (String)dataRow[9];
      String frontDoorPolicyGroupEntitlement = frontDoorPolicyGroup;
      if (!StringUtils.isBlank(frontDoorPolicyGroup)) {
        frontDoorPolicyGroupEntitlement = "urn:mace:upenn.edu:" + frontDoorPolicyGroup;
      }

      if (StringUtils.isBlank(coarseGrainedPolicy)) {
        coarseGrainedPolicy = "none";
      }

      if (!isAdmin && !StringUtils.equals(frontDoorPolicyGroup, gsh_input_policyGroupName)) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_policyGroupName", "Non admins cannot change the policy group, contact ISC to change this");
        return;
      }
      
      boolean hasChange = false;
      List<LdapModificationItem> ldapModificationItems = new ArrayList<LdapModificationItem>();

      if (!StringUtils.equals(StringUtils.trimToNull(name), StringUtils.trimToNull(gsh_input_name))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appName", name, gsh_input_name);
      }
      if (!StringUtils.equals(StringUtils.trimToNull(description), StringUtils.trimToNull(gsh_input_description))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appDescription", description, gsh_input_description);
      }
      if (!StringUtils.equals(StringUtils.trimToNull(url), StringUtils.trimToNull(gsh_input_url))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appTestURL", url, gsh_input_url);
      }
      if (!StringUtils.equals(StringUtils.trimToNull(urlInstructions), StringUtils.trimToNull(gsh_input_urlInstructions))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appTestNote", urlInstructions, gsh_input_urlInstructions);
      }
      if (!StringUtils.equals(StringUtils.trimToNull(coarseGrainedPolicy), StringUtils.trimToNull(gsh_input_frontDoorPolicy))) {
        hasChange = true;
                
        createLdapModificationItem(ldapModificationItems, "appMACPolicy", StringUtils.equals(coarseGrainedPolicy, "none") ? null : coarseGrainedPolicy, 
            StringUtils.equals(gsh_input_frontDoorPolicy, "none") ? null : gsh_input_frontDoorPolicy);

      }
      if (!StringUtils.equals(StringUtils.trimToNull(frontDoorPolicyGroupEntitlement), StringUtils.trimToNull(inputPolicyGroupEntitlement))) {
        hasChange = true;
        
        createLdapModificationItem(ldapModificationItems, "appPolicyGroup", frontDoorPolicyGroupEntitlement, inputPolicyGroupEntitlement);
      }
      
      String inputRequiresTwoStep = gsh_input_requiresTwostep ? "T" : "F";
      if (!StringUtils.equals(StringUtils.trimToNull(requiresTwoStep), StringUtils.trimToNull(inputRequiresTwoStep))) {
        hasChange = true;
      }
      if (!StringUtils.equals(StringUtils.trimToNull(errorPageType), StringUtils.trimToNull(gsh_input_errorPageType))) {
        hasChange = true;
      }
      if (!StringUtils.equals(StringUtils.trimToNull(errorPageUrl), StringUtils.trimToNull(gsh_input_errorPageUrl))) {
        hasChange = true;
      }
      if (!StringUtils.equals(StringUtils.trimToNull(errorPageText), StringUtils.trimToNull(gsh_input_errorPageText))) {
        hasChange = true;
      }
      
      if (!hasChange) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Note: no changes made");
        return;
      }
      
      new GcDbAccess().sql("update " + tableName + " set name=?, description=?, url=?, url_instructions=?, coarse_grained_policy=?, "
          + "front_door_policy_group=?, requires_twostep=?, error_page_type=?, error_page_url=?, error_page_text=? where entity_id = ?").
        addBindVar(gsh_input_name).addBindVar(gsh_input_description).addBindVar(gsh_input_url).
        addBindVar(gsh_input_urlInstructions).addBindVar(gsh_input_frontDoorPolicy).addBindVar(gsh_input_policyGroupName).
        addBindVar(inputRequiresTwoStep).addBindVar(gsh_input_errorPageType).addBindVar(gsh_input_errorPageUrl).
        addBindVar(gsh_input_errorPageText).addBindVar(gsh_input_entityId).executeSql();
      
      if (ldapModificationItems.size() > 0) {

        createLdapModificationItem(ldapModificationItems, "appModifiedBy", "whatever123", subjectLoggedIn.getId());

        // cn=c77a2c1fd25902a3e09e313af5cd4d0da29b7405,ou=applications,dc=penncommunity,dc=upenn,dc=edu
        String dn = "cn=" + entityIdShaHex + ",ou=applications,dc=authz,dc=upenn,dc=edu";
        LdapSessionUtils.ldapSession().internal_modifyHelper(ldapConnection, dn, ldapModificationItems);
      }
      
      gsh_builtin_gshTemplateOutput.addOutputLine("Success: edited new service provider");

    } else if (actionDelete) {

      if (adminsGroup != null) {
        adminsGroup.delete();
        gsh_builtin_gshTemplateOutput.addOutputLine("Success: group deleted: " + adminsGroupName);
      }
  
      String ownersGroupName = "penn:isc:ts:iam:weblogin:service:policy:" + pennGroupsFolderExtension + ":service:policy:managers:" + groupExtensionOwners;
  
      Group ownersGroup = GroupFinder.findByName(ownersGroupName, false);

      if (ownersGroup != null) {
        ownersGroup.delete();
        gsh_builtin_gshTemplateOutput.addOutputLine("Success: group deleted: " + ownersGroupName);
      }

      new GcDbAccess().sql("delete from " + tableName + " where entity_id = ?").addBindVar(gsh_input_entityId).executeSql();
      gsh_builtin_gshTemplateOutput.addOutputLine("Success: delete registration in database for entity id: " + gsh_input_entityId);
      
      String dn = "cn=" + entityIdShaHex + ",ou=applications,dc=authz,dc=upenn,dc=edu";

      LdapSessionUtils.ldapSession().delete(ldapConnection, dn);
      gsh_builtin_gshTemplateOutput.addOutputLine("Success: delete LDAP entry: " + dn);

      gsh_builtin_gshTemplateOutput.addOutputLine("Success: deleted the service provider");
      
    }

//    if (StringUtils.equals("add", gsh_input_action)) {
//      
//    } else {
//      throw new RuntimeException("Invalid action: '" + gsh_input_action + "'");
//    }
    

  }

  public static void main(String[] args) {
    
    GshTemplateV2input gshTemplateV2input = new GshTemplateV2input();

    GshTemplateV2output gshTemplateV2output = new GshTemplateV2output();

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        // user for chris hyzer
        //Subject subject = SubjectFinder.findByIdAndSource("10021368", "pennperson", true);

        // user for sam jenkins: 55308115
        Subject subject = SubjectFinder.findByIdAndSource("55308115", "pennperson", true);
        
        gshTemplateV2input.setGsh_builtin_gshTemplateRuntime(new GshTemplateRuntime());
//        gshTemplateV2input.getGsh_builtin_gshTemplateRuntime().setAuthenticatedSubject(subject);
        gshTemplateV2input.setGsh_builtin_subject(subject);
        gshTemplateV2input.getGsh_builtin_gshTemplateRuntime().setCurrentSubject(subject);
        GshTemplateRuntime gshTemplateRuntime = new GshTemplateRuntime();
        gshTemplateRuntime.setTemplateConfigId("ssoCoarseGrainedAuthzConfig");
        
        Test76ssoCoarseGrainedConfig myGshTemplate = new Test76ssoCoarseGrainedConfig();

        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_action", "add");
        //gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_action", "edit");
        //gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_entityId", "https://fides.isc-seo.upenn.edu/shibboleth");
        //gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_name", "sdfsdf");
        //gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_description", "sdfasdf");
        //gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_admins", "");
        //gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_owners", "");
        //gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_frontDoorPolicy", "workforceInTwoStep");
        //gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_url", "");
        //gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_urlInstructions", "");
        //gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_requiresTwostep", true);
        
        //myGshTemplate.gshRunLogic(gshTemplateV2input, gshTemplateV2output);
        // 
        
        GshTemplateConfig templateConfig = new GshTemplateConfig("ssoCoarseGrainedAuthzConfig");
        templateConfig.populateConfiguration();

        
        GshTemplateDecorateForUiInput gshTemplateDecorateForUiInput = new GshTemplateDecorateForUiInput();
        gshTemplateDecorateForUiInput.setOwnerStemName("penn:isc:ts:iam:weblogin:service:templates:idpRegistrationProd");
        gshTemplateDecorateForUiInput.setCurrentSubject(subject);
        gshTemplateDecorateForUiInput.setEventConfigId("gsh_input_action");
        Map<String, GshTemplateInputConfigAndValue> gshTemplateInputConfigAndValues = new HashMap<String, GshTemplateInputConfigAndValue>();
        GshTemplateInputConfigAndValue gshTemplateConfigAndValue = gshTemplateConfigAndValue();
        gshTemplateConfigAndValue.setGshTemplateInputConfig(templateConfig.getGshTemplateInputConfigs().get(0));
        gshTemplateConfigAndValue.setValueObject("edit");
        gshTemplateInputConfigAndValues.put("gsh_input_action", gshTemplateConfigAndValue);
        gshTemplateDecorateForUiInput.setGshTemplateInputConfigAndValues(gshTemplateInputConfigAndValues);
        myGshTemplate.decorateTemplateForUiDisplay(gshTemplateDecorateForUiInput);
        return null;
      }

      public GshTemplateInputConfigAndValue gshTemplateConfigAndValue() {
        return new GshTemplateInputConfigAndValue();
      }
    });
    System.out.println(GrouperUtil.toStringForLog(gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getValidationLines()));
    System.out.println(GrouperUtil.toStringForLog(gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getOutputLines()));
    System.exit(0);
  }

}
```

## Front door admin template

Used by IAM team for now to run reports

Config

```
grouperGshTemplate.ssoCoarseGrainedAuthz.defaultRunButtonFolderUuidOrName = penn\u003Aisc\u003Ats\u003Aiam\u003Aweblogin\u003Aservice\u003Atemplates\u003AidpRegistrationProd
grouperGshTemplate.ssoCoarseGrainedAuthz.folderShowOnDescendants = certainFolders
grouperGshTemplate.ssoCoarseGrainedAuthz.folderShowType = certainFolders
grouperGshTemplate.ssoCoarseGrainedAuthz.folderUuidToShow = penn\u003Aisc\u003Ats\u003Aiam\u003Aweblogin\u003Aservice\u003Atemplates\u003AidpRegistrationProd
grouperGshTemplate.ssoCoarseGrainedAuthz.groupUuidCanRun = penn\u003Aisc\u003Ats\u003Aiam\u003Aweblogin\u003Aservice\u003Asecurity\u003AcanRunIdpTemplate
grouperGshTemplate.ssoCoarseGrainedAuthz.gshTemplate = //
grouperGshTemplate.ssoCoarseGrainedAuthz.input.0.description = Select an action\u003A Analyze user access, Analyze user history, Analyze application
grouperGshTemplate.ssoCoarseGrainedAuthz.input.0.dropdownCsvValue = Analyze application
grouperGshTemplate.ssoCoarseGrainedAuthz.input.0.formElementType = dropdown
grouperGshTemplate.ssoCoarseGrainedAuthz.input.0.label = Action
grouperGshTemplate.ssoCoarseGrainedAuthz.input.0.name = gsh_input_action
grouperGshTemplate.ssoCoarseGrainedAuthz.input.1.description = Enter in the SAML entity ID (you can get this from Firefox SAML tracer plugin)
grouperGshTemplate.ssoCoarseGrainedAuthz.input.1.dropdownValueFormat = dynamicFromTemplate
grouperGshTemplate.ssoCoarseGrainedAuthz.input.1.formElementType = dropdown
grouperGshTemplate.ssoCoarseGrainedAuthz.input.1.label = SAML entity ID
grouperGshTemplate.ssoCoarseGrainedAuthz.input.1.name = gsh_input_samlEntityId
grouperGshTemplate.ssoCoarseGrainedAuthz.input.1.required = true
grouperGshTemplate.ssoCoarseGrainedAuthz.input.1.showEl = \u0024{gsh_input_action == 'Analyze application' || gsh_input_action == 'Analyze user access'}
grouperGshTemplate.ssoCoarseGrainedAuthz.input.2.description = Enter the PennKey or PennID of the user
grouperGshTemplate.ssoCoarseGrainedAuthz.input.2.label = PennKey or Penn ID
grouperGshTemplate.ssoCoarseGrainedAuthz.input.2.name = gsh_input_pennkey
grouperGshTemplate.ssoCoarseGrainedAuthz.input.2.showEl = \u0024{gsh_input_action == 'Analyze user access' || gsh_input_action == 'Analyze user history'}
grouperGshTemplate.ssoCoarseGrainedAuthz.input.2.validationType = none
grouperGshTemplate.ssoCoarseGrainedAuthz.input.3.description = SAML entity ID (you can get this from Firefox SAML tracer plugin)
grouperGshTemplate.ssoCoarseGrainedAuthz.input.3.dropdownValueFormat = dynamicFromTemplate
grouperGshTemplate.ssoCoarseGrainedAuthz.input.3.formElementType = dropdown
grouperGshTemplate.ssoCoarseGrainedAuthz.input.3.label = SAML entity ID
grouperGshTemplate.ssoCoarseGrainedAuthz.input.3.name = gsh_input_samlEntityIdForUser
grouperGshTemplate.ssoCoarseGrainedAuthz.input.3.showEl = \u0024{false}
grouperGshTemplate.ssoCoarseGrainedAuthz.moreActionsLabel = WebLogin front door authorization
grouperGshTemplate.ssoCoarseGrainedAuthz.numberOfInputs = 4
grouperGshTemplate.ssoCoarseGrainedAuthz.runAsType = GrouperSystem
grouperGshTemplate.ssoCoarseGrainedAuthz.runButtonGroupOrFolder = folder
grouperGshTemplate.ssoCoarseGrainedAuthz.securityRunType = specifiedGroup
grouperGshTemplate.ssoCoarseGrainedAuthz.showInMoreActions = true
grouperGshTemplate.ssoCoarseGrainedAuthz.showOnFolders = true
grouperGshTemplate.ssoCoarseGrainedAuthz.templateDescription = Analyze a service provider (application) or a user in front door authorization in WebLogin
grouperGshTemplate.ssoCoarseGrainedAuthz.templateName = WebLogin front door authorization
grouperGshTemplate.ssoCoarseGrainedAuthz.templateType = gsh
grouperGshTemplate.ssoCoarseGrainedAuthz.templateVersion = V2

```

Code

```
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateDecorateForUiInput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputConfigAndValue;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.reports.GrouperCsvReportJob;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

public class Test76ssoCoarseGrained extends GshTemplateV2 {
  
  public static class TheState {
    Map<String, Map<String, List<Object[]>>> groupNameToPennIdToPointInTime = new HashMap<>();
  
    // get point in time per bucket and per user
    // buckets are:
    // inTwoStep: penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:enrolledInTwoStep
    // affiliate: penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpAffiliate
    // affiliateInTwoStep: penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpAffiliateInTwoStep
    // member: penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:member
    // memberInTwoStep: penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:memberInTwoStep
    // employeeInTwoStep: penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:employeesInTwoStep
    Map<String, String> bucketLabelToGroupName = GrouperUtil.toMap(
        "inTwoStep", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:enrolledInTwoStep",
        "affiliate", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpAffiliate",
        "affiliateInTwoStep", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:idpAffiliateInTwoStep",
        "member", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:member",
        "memberInTwoStep", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:memberInTwoStep",
        "employeeInTwoStep", "penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:employeesInTwoStep");
    
    Map<String, String> groupNameToBucketLabel = new HashMap<>();
      
    String deprovisionedGroupName = "penn:etc:deprovisioning:usersWhoHaveBeenDeprovisioned_employee";
    
    public TheState() {
      for (String bucketLabel : bucketLabelToGroupName.keySet()) {
        groupNameToBucketLabel.put(bucketLabelToGroupName.get(bucketLabel), bucketLabel);
      }
    }
  }

  private static int percent(int count, int total) {
    if (total == 0 || count == 0) {
      return 0;
    }
    int percent = (int)Math.round((100d * (double)count / (double)total));
    if (percent == 100 && count != total) {
      percent = 99;
    }
    return percent;
  }

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    TheState theState = new TheState();

    // Analyze user access, Analyze user history, Analyze application
    String gsh_input_action = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_action");

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();
    
    Subject currentUserSubject = gshTemplateV2input.getGsh_builtin_subject();

    if (StringUtils.equals("Analyze application", gsh_input_action)) {
      String gsh_input_samlEntityId = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_samlEntityId");
      
      String sql = """
          select spefd.name_without_special_characters, spefd."name", spefd.description,
          spefd.url, spefd.url_instructions, spefd.coarse_grained_policy, spefd.front_door_policy_group,
          spefd.error_page_type, spefd.error_page_text, spefd.error_page_url from sso_prod_entity_front_door spefd
          where spefd.entity_id=?
          """;

      Object[] frontDoorConfig = new GcDbAccess().sql(sql).addBindVar(gsh_input_samlEntityId).select(Object[].class);
      
      String nameWithoutSpecialChars = gsh_input_samlEntityId == null ? null : gsh_input_samlEntityId.replaceAll("[^A-Za-z0-9_-]", "_");
      
      if (GrouperUtil.length(frontDoorConfig) == 0) {
        gsh_builtin_gshTemplateOutput.addOutputLine("The service provider does not have a front door config.");
        gsh_builtin_gshTemplateOutput.addOutputLine(" ");
        
      } else {
        
        nameWithoutSpecialChars = (String)frontDoorConfig[0];
        String name = (String)frontDoorConfig[1];
        String description = (String)frontDoorConfig[2];
        String url = (String)frontDoorConfig[3];
        String urlInstructions = (String)frontDoorConfig[4];
        String coarseGrainedPolicy = (String)frontDoorConfig[5];
        String frontDoorPolicyGroup = (String)frontDoorConfig[6];
        String errorPageType = (String)frontDoorConfig[7];
        String errorPageText = (String)frontDoorConfig[8];
        String errorPageUrl = (String)frontDoorConfig[9];

        gsh_builtin_gshTemplateOutput.addOutputLine("The service provider has a front door config.");
        gsh_builtin_gshTemplateOutput.addOutputLine("App name: " + GrouperUtil.escapeHtml(name, true));
        gsh_builtin_gshTemplateOutput.addOutputLine("Description: " + GrouperUtil.escapeHtml(description, true));
        gsh_builtin_gshTemplateOutput.addOutputLine("URL: <a href=\"" + GrouperUtil.escapeHtml(url, true) + "\">" + GrouperUtil.escapeHtml(url, true) + "</a>");
        gsh_builtin_gshTemplateOutput.addOutputLine("Login instructions: " + GrouperUtil.escapeHtml(urlInstructions, true));
        gsh_builtin_gshTemplateOutput.addOutputLine("Coarse grained policy: " + GrouperUtil.escapeHtml(coarseGrainedPolicy, true));
        if (!StringUtils.isBlank(frontDoorPolicyGroup)) {
          gsh_builtin_gshTemplateOutput.addOutputLine("Front door group: " + GrouperUtil.escapeHtml(frontDoorPolicyGroup, true));
        }
        gsh_builtin_gshTemplateOutput.addOutputLine("Error page type: " + GrouperUtil.escapeHtml(errorPageType, true));
        if (!StringUtils.isBlank(errorPageText)) {
          gsh_builtin_gshTemplateOutput.addOutputLine("Error page text: " + GrouperUtil.escapeHtml(errorPageText, true));
        }
        if (!StringUtils.isBlank(errorPageUrl)) {
          gsh_builtin_gshTemplateOutput.addOutputLine("Error page URL: " + GrouperUtil.escapeHtml(errorPageUrl, true));
        }
        gsh_builtin_gshTemplateOutput.addOutputLine(" ");

      }
      
      sql = """
          select penn_id, person_description, login_timestamp, pennkey, two_step_enrolled, 
          workforce, member, affiliate, recent_affiliate, alum_or_affiliate, uphs_only, alum_only, 
          in_front_door, locked_out, non_persistent, uphs_not_pennpay_not_student, email, coarse_grained_group_name,  
          now_two_step, now_workforce, now_member, now_affliate, now_recent_affliate, now_affliate_or_alum, 
          now_locked_out, now_in_front_door_group_current, entity_id, login_timestamp_native
          from sso_prod_logs_v where entity_id = ? order by login_timestamp_native desc
          """;
      
      List<Object[]> ssoProdLogs = new GcDbAccess().sql(sql).addBindVar(gsh_input_samlEntityId).selectList(Object[].class);
      
      List<String> reportHeaders = GrouperUtil.toList(
          "penn_id", "person_description", "login_timestamp", "pennkey", "login_two_step_enrolled", 
          "login_workforce", "login_member", "login_affiliate", "login_recent_affiliate", "login_alum_or_affiliate", "login_uphs_only", "login_alum_only", 
          "login_in_front_door", "login_locked_out", "non_persistent", "uphs_not_pennpay_not_student", "email", "coarse_grained_group_name", 
          "now_two_step", "now_workforce", "now_member", "now_affliate", "now_recent_affliate", "now_affliate_or_alum", 
          "now_locked_out", "now_in_front_door_group_current", "entity_id");

      List<String[]> reportData = new ArrayList<String[]>();
      
      Timestamp minTimestamp = null;
      
      Set<String> pennIds = new HashSet<String>();
      Set<String> pennIdsWorkforce = new HashSet<String>();
      Set<String> pennIdsNotWorkforce = new HashSet<String>();
      Set<String> pennIdsWorkforceOrAreNow = new HashSet<String>();
      int workforceCount = 0;
      int workforceOrIsNowCount = 0;

      Set<String> pennIdsTwostep = new HashSet<String>();
      Set<String> pennIdsNotTwostep = new HashSet<String>();
      Set<String> pennIdsTwostepOrAreNow = new HashSet<String>();
      int twostepCount = 0;
      int twostepOrIsNowCount = 0;

      Set<String> pennIdsMember = new HashSet<String>();
      Set<String> pennIdsNotMember = new HashSet<String>();
      Set<String> pennIdsMemberOrAreNow = new HashSet<String>();
      int memberCount = 0;
      int memberOrIsNowCount = 0;

      Set<String> pennIdsAffiliate = new HashSet<String>();
      Set<String> pennIdsNotAffiliate = new HashSet<String>();
      Set<String> pennIdsAffiliateOrAreNow = new HashSet<String>();
      int affiliateCount = 0;
      int affiliateOrIsNowCount = 0;

      Set<String> pennIdsAffiliateOrAlum = new HashSet<String>();
      Set<String> pennIdsNotAffiliateOrAlum = new HashSet<String>();
      Set<String> pennIdsAffiliateOrAlumOrAreNow = new HashSet<String>();
      int affiliateOrAlumCount = 0;
      int affiliateOrAlumOrIsNowCount = 0;

      Set<String> pennIdsRecentAffiliate = new HashSet<String>();
      Set<String> pennIdsNotRecentAffiliate = new HashSet<String>();
      Set<String> pennIdsRecentAffiliateOrAreNow = new HashSet<String>();
      int recentAffiliateCount = 0;
      int recentAffiliateOrIsNowCount = 0;

      Set<MultiKey> usersLogged = new HashSet<MultiKey>();

      for (Object[] ssoProdLog : ssoProdLogs) {
        
        String pennId = (String)ssoProdLog[0];
        pennIds.add(pennId);
        String personDescription = (String)ssoProdLog[1];
        String loginTimestamp = (String)ssoProdLog[2];
        String pennkey = (String)ssoProdLog[3];
        String twoStepEnrolled = (String)ssoProdLog[4];
        String workforce = (String)ssoProdLog[5];
        String member = (String)ssoProdLog[6];
        String affiliate = (String)ssoProdLog[7];
        String recentAffiliate = (String)ssoProdLog[8];
        String alumOrAffiliate = (String)ssoProdLog[9];
        String uphsOnly = (String)ssoProdLog[10];
        String alumOnly = (String)ssoProdLog[11];
        String inFrontDoor = (String)ssoProdLog[12];
        String lockedOut = (String)ssoProdLog[13];
        String nonPersistent = (String)ssoProdLog[14];
        String uphsNotPennpayNotStudent = (String)ssoProdLog[15];
        String email = (String)ssoProdLog[16];
        String coarseGrainedGroupName = (String)ssoProdLog[17];
        String nowTwoStep = (String)ssoProdLog[18];
        
        if (StringUtils.equals("T", twoStepEnrolled)) {
          pennIdsTwostep.add(pennId);
          twostepCount++;
          pennIdsTwostepOrAreNow.add(pennId);
          twostepOrIsNowCount++;
        } else {
          pennIdsNotTwostep.add(pennId);
          if (StringUtils.equals("T", nowTwoStep)) {
            pennIdsTwostepOrAreNow.add(pennId);
            twostepOrIsNowCount++;
          }
        }

        String nowWorkforce = (String)ssoProdLog[19];
        
        if (StringUtils.equals("T", workforce)) {
          pennIdsWorkforce.add(pennId);
          workforceCount++;
          pennIdsWorkforceOrAreNow.add(pennId);
          workforceOrIsNowCount++;
        } else {
          pennIdsNotWorkforce.add(pennId);
          if (StringUtils.equals("T", nowWorkforce)) {
            pennIdsWorkforceOrAreNow.add(pennId);
            workforceOrIsNowCount++;
          }
        }

        String nowMember = (String)ssoProdLog[20];

        if (StringUtils.equals("T", member)) {
          pennIdsMember.add(pennId);
          memberCount++;
          pennIdsMemberOrAreNow.add(pennId);
          memberOrIsNowCount++;
        } else {
          pennIdsNotMember.add(pennId);
          if (StringUtils.equals("T", nowMember)) {
            pennIdsMemberOrAreNow.add(pennId);
            memberOrIsNowCount++;
          }
        }

        String nowAffiliate = (String)ssoProdLog[21];

        if (StringUtils.equals("T", affiliate)) {
          pennIdsAffiliate.add(pennId);
          affiliateCount++;
          pennIdsAffiliateOrAreNow.add(pennId);
          affiliateOrIsNowCount++;
        } else {
          pennIdsNotAffiliate.add(pennId);
          if (StringUtils.equals("T", nowAffiliate)) {
            pennIdsAffiliateOrAreNow.add(pennId);
            affiliateOrIsNowCount++;
          }
        }

        String nowRecentAffiliate = (String)ssoProdLog[22];

        if (StringUtils.equals("T", recentAffiliate)) {
          pennIdsRecentAffiliate.add(pennId);
          recentAffiliateCount++;
          pennIdsRecentAffiliateOrAreNow.add(pennId);
          recentAffiliateOrIsNowCount++;
        } else {
          pennIdsNotRecentAffiliate.add(pennId);
          if (StringUtils.equals("T", nowRecentAffiliate)) {
            pennIdsRecentAffiliateOrAreNow.add(pennId);
            recentAffiliateOrIsNowCount++;
          }
        }

        String nowAffliateOrAlum = (String)ssoProdLog[23];
        
        if (StringUtils.equals("T", alumOrAffiliate)) {
          pennIdsAffiliateOrAlum.add(pennId);
          affiliateOrAlumCount++;
          pennIdsAffiliateOrAlumOrAreNow.add(pennId);
          affiliateOrAlumOrIsNowCount++;
        } else {
          pennIdsNotAffiliateOrAlum.add(pennId);
          if (StringUtils.equals("T", nowAffliateOrAlum)) {
            pennIdsAffiliateOrAlumOrAreNow.add(pennId);
            affiliateOrAlumOrIsNowCount++;
          }
        }

        String nowLockedOut = (String)ssoProdLog[24];
        String nowInFrontDoorGroupCurrent = (String)ssoProdLog[25];
        String entityId = (String)ssoProdLog[26];
        Timestamp timestamp = GrouperUtil.toTimestamp(ssoProdLog[27]);
        if (minTimestamp == null || timestamp.getTime() < minTimestamp.getTime()) {
          minTimestamp = timestamp;
        }
        
        if (ssoProdLogs.size() >= 30000) {
          MultiKey userLogged = new MultiKey(GrouperUtil.toArrayObject(pennId, twoStepEnrolled, workforce, member, affiliate, alumOrAffiliate, recentAffiliate));
          if (usersLogged.contains(userLogged)) {
            continue;
          }
          usersLogged.add(userLogged);
        }
        
        String[] reportDataRow = GrouperUtil.toArray(GrouperUtil.toList(pennId, personDescription, loginTimestamp, pennkey, twoStepEnrolled,
            workforce, member, affiliate, recentAffiliate, alumOrAffiliate, uphsOnly, alumOnly, inFrontDoor, lockedOut, nonPersistent,
            uphsNotPennpayNotStudent, email, coarseGrainedGroupName, nowTwoStep, nowWorkforce, nowMember, nowAffiliate, nowRecentAffiliate,
            nowAffliateOrAlum, nowLockedOut, nowInFrontDoorGroupCurrent, entityId), String.class);
        reportData.add(reportDataRow);
      }

      if (reportData.size() > 30000) {
        Iterator<String[]> iterator = reportData.iterator();
        int count = 0;
        while (iterator.hasNext()) {
          String[] reportDataRow = iterator.next();
          if (StringUtils.equals("T", reportDataRow[4]) && StringUtils.equals("T", reportDataRow[5])) {
            iterator.remove();
            continue;
          }
          if (count >= 30000) {
            iterator.remove();
            continue;
          }
          count++;
        }
        
      }
      
      
      String timestampToFileString = GrouperUtil.timestampToFileString(new Date());
      String fileName = GrouperUtil.tmpDir(true) + "spLogs_" + nameWithoutSpecialChars + "_" + timestampToFileString + ".csv";

      File file = GrouperCsvReportJob.createCsv(fileName, reportHeaders, reportData);

      new GrouperEmail().addEmailAddressToSendTo(GrouperEmailUtils.getEmail(currentUserSubject)).
        setSubject("SP report for " + gsh_input_samlEntityId + ": " + timestampToFileString).
        setBody("Attached is " + gsh_input_samlEntityId + " report").addAttachment(file).send();
      
      file.delete();

      gsh_builtin_gshTemplateOutput.addOutputLine("The user report was emailed to you" + (reportData.size() != ssoProdLogs.size() ? (", note " + (ssoProdLogs.size() - reportData.size()) + " old or redundant or twostep/workforce rows were removed") : ""));
      gsh_builtin_gshTemplateOutput.addOutputLine(" ");

      gsh_builtin_gshTemplateOutput.addOutputLine("There were " + pennIds.size() + " users and " + ssoProdLogs.size() + " log-ins since " + minTimestamp +  ".");
      
      gsh_builtin_gshTemplateOutput.addOutputLine("For two-step: " + (pennIds.size()-pennIdsNotTwostep.size()) + "/" + pennIds.size()
        + " users (" + percent(pennIds.size()-pennIdsNotTwostep.size(), pennIds.size()) + "%), " + pennIdsTwostepOrAreNow.size() + "/" + pennIds.size()
        + " users or now (" + percent(pennIdsTwostepOrAreNow.size(), pennIds.size()) + "%), "
        + twostepCount + "/" + ssoProdLogs.size() + " log ins (" + percent(twostepCount, ssoProdLogs.size()) + "%), " 
        + twostepOrIsNowCount + "/" + ssoProdLogs.size()
        + " log ins or now (" + percent(twostepOrIsNowCount, ssoProdLogs.size()) + "%)");
      
      gsh_builtin_gshTemplateOutput.addOutputLine("For workforce: " + (pennIds.size()-pennIdsNotWorkforce.size()) + "/" + pennIds.size() 
        + " users (" + percent(pennIds.size()-pennIdsNotWorkforce.size(), pennIds.size()) + "%), " + pennIdsWorkforceOrAreNow.size() + "/" + pennIds.size()
        + " users or now (" + percent(pennIdsWorkforceOrAreNow.size(), pennIds.size()) + "%), "
        + workforceCount + "/" + ssoProdLogs.size() + " log ins (" + percent(workforceCount, ssoProdLogs.size()) + "%), " 
        + workforceOrIsNowCount + "/" + ssoProdLogs.size()
        + " log ins or now (" + percent(workforceOrIsNowCount, ssoProdLogs.size()) + "%)");
      
      gsh_builtin_gshTemplateOutput.addOutputLine("For member: " + (pennIds.size() - pennIdsNotMember.size()) + "/" + pennIds.size() 
        + " users (" + percent(pennIds.size() - pennIdsNotMember.size(), pennIds.size()) + "%), " + pennIdsMemberOrAreNow.size() + "/" + pennIds.size()
        + " users or now (" + percent(pennIdsMemberOrAreNow.size(), pennIds.size()) + "%), "
        + memberCount + "/" + ssoProdLogs.size() + " log ins (" + percent(memberCount, ssoProdLogs.size()) + "%), " 
        + memberOrIsNowCount + "/" + ssoProdLogs.size()
        + " log ins or now (" + percent(memberOrIsNowCount, ssoProdLogs.size()) + "%)");

      gsh_builtin_gshTemplateOutput.addOutputLine("For affiliate: " + (pennIds.size()-pennIdsNotAffiliate.size()) + "/" + pennIds.size() 
        + " users (" + percent(pennIds.size()-pennIdsNotAffiliate.size(), pennIds.size()) + "%), " + pennIdsAffiliateOrAreNow.size() + "/" + pennIds.size()
        + " users or now (" + percent(pennIdsAffiliateOrAreNow.size(), pennIds.size()) + "%), "
        + affiliateCount + "/" + ssoProdLogs.size() + " log ins (" + percent(affiliateCount, ssoProdLogs.size()) + "%), " 
        + affiliateOrIsNowCount + "/" + ssoProdLogs.size()
        + " log ins or now (" + percent(affiliateOrIsNowCount, ssoProdLogs.size()) + "%)");

      gsh_builtin_gshTemplateOutput.addOutputLine("For affiliateOrAlum: " + (pennIds.size()-pennIdsNotAffiliateOrAlum.size()) + "/" + pennIds.size() 
        + " users (" + percent(pennIds.size()-pennIdsNotAffiliateOrAlum.size(), pennIds.size()) + "%), " + pennIdsAffiliateOrAlumOrAreNow.size() + "/" + pennIds.size()
        + " users or now (" + percent(pennIdsAffiliateOrAlumOrAreNow.size(), pennIds.size()) + "%), "
        + affiliateOrAlumCount + "/" + ssoProdLogs.size() + " log ins (" + percent(affiliateOrAlumCount, ssoProdLogs.size()) + "%), " 
        + affiliateOrAlumOrIsNowCount + "/" + ssoProdLogs.size()
        + " log ins or now (" + percent(affiliateOrAlumOrIsNowCount, ssoProdLogs.size()) + "%)");

      gsh_builtin_gshTemplateOutput.addOutputLine("For recentAffiliate: " + (pennIds.size()-pennIdsNotRecentAffiliate.size()) + "/" + pennIds.size() 
        + " users (" + percent(pennIds.size()-pennIdsNotRecentAffiliate.size(), pennIds.size()) + "%), " + pennIdsRecentAffiliateOrAreNow.size() + "/" + pennIds.size()
        + " users or now (" + percent(pennIdsRecentAffiliateOrAreNow.size(), pennIds.size()) + "%), "
        + recentAffiliateCount + "/" + ssoProdLogs.size() + " log ins (" + percent(recentAffiliateCount, ssoProdLogs.size()) + "%), " 
        + recentAffiliateOrIsNowCount + "/" + ssoProdLogs.size()
        + " log ins or now (" + percent(recentAffiliateOrIsNowCount, ssoProdLogs.size()) + "%)");
      
    } else if (StringUtils.equals("Analyze user history", gsh_input_action)) {
      String gsh_input_pennkey = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_pennkey");
      Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(gsh_input_pennkey, "pennperson", false);
      if (subject == null) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_pennkey", "Cannot find person");
        return;
      }
      
      List<Object[]> entityIdAndTimestamps = new GcDbAccess().sql(
          "select entity_id, login_timestamp from sso_prod_logs_v splv where penn_id = ? order by login_timestamp desc").
          addBindVar(subject.getId()).selectList(Object[].class);

      Set<String> entityIds = new LinkedHashSet<>();
      
      for (Object[] entityIdAndTimestamp : entityIdAndTimestamps) {
        
        String entityId = (String)entityIdAndTimestamp[0];
        Timestamp timestamp = (Timestamp)entityIdAndTimestamp[1];
        
        if (entityIds.contains(entityId)) {
          continue;
        }
        entityIds.add(entityId);        
      }
      
      Set<String> groupNames = new HashSet<String>(theState.bucketLabelToGroupName.values());
      groupNames.add(theState.deprovisionedGroupName);
      
      for (String groupName : groupNames) {
        GcDbAccess gcDbAccess = new GcDbAccess();
        gcDbAccess.sql("select gpmglv.subject_id, gpmglv.the_start_time, gpmglv.the_end_time from grouper_pit_mship_group_lw_v gpmglv " +
            " where gpmglv.subject_id = ?  " +
            " and gpmglv.subject_source = 'pennperson' " +
            " and gpmglv.field_name = 'members' " +
            " and gpmglv.group_name = ? ");
        gcDbAccess.addBindVar(subject.getId());
        gcDbAccess.addBindVar(groupName);

        List<Object[]> pennIdStartEnds = gcDbAccess.selectList(Object[].class);
        for (Object[] pennIdStartEnd : pennIdStartEnds) {
          String pennId = (String)pennIdStartEnd[0];
          Long startMillis = GrouperUtil.longValue(pennIdStartEnd[1]);
          if (startMillis != null) {
            startMillis /= 1000;
          }
          Long endMillis = GrouperUtil.longObjectValue(pennIdStartEnd[2], true);
          if (endMillis != null) {
            endMillis /= 1000;
          }
          Map<String, List<Object[]>> pennIdToStartAndEnd = theState.groupNameToPennIdToPointInTime.get(groupName);
          if (pennIdToStartAndEnd == null) {
            pennIdToStartAndEnd = new HashMap<>();
            theState.groupNameToPennIdToPointInTime.put(groupName, pennIdToStartAndEnd);
          }
          
          List<Object[]> startMillisEndMillises = pennIdToStartAndEnd.get(pennId);
          if (startMillisEndMillises == null) {
            startMillisEndMillises = new ArrayList<Object[]>();
            pennIdToStartAndEnd.put(pennId, startMillisEndMillises);
          }
          
          startMillisEndMillises.add(GrouperUtil.toArrayObject(startMillis, endMillis));
        }
      }

      gsh_builtin_gshTemplateOutput.addOutputLine(subject.getDescription());

      String pennId = subject.getId();
      
      // non persistent
      if (pennId.startsWith("9")) {
        gsh_builtin_gshTemplateOutput.addOutputLine("User is non-persistent");
      } else {
        gsh_builtin_gshTemplateOutput.addOutputLine("User not from a non-persistent affiliation");
      }
      
      boolean isLockedOut = isInGroup(theState, theState.deprovisionedGroupName, pennId, System.currentTimeMillis());
      if (isLockedOut) {
        gsh_builtin_gshTemplateOutput.addOutputLine("User is locked out");
      } else {
        gsh_builtin_gshTemplateOutput.addOutputLine("User not locked out");
      }
      
      for (String bucket : theState.bucketLabelToGroupName.keySet()) {
        
        boolean isInBucket = isInGroup(theState, bucket, pennId, System.currentTimeMillis());
        if (isInBucket) {
          gsh_builtin_gshTemplateOutput.addOutputLine("User is in built-in population: " + bucket);
        } else {
          gsh_builtin_gshTemplateOutput.addOutputLine("User is not in built-in population: " + bucket);
        }          
      }

      for (String entityId : entityIds) {

        gsh_builtin_gshTemplateOutput.addOutputLine("Entity id '" + entityId + "':");

        for (Object[] entityIdAndTimestamp : entityIdAndTimestamps) {
          
          String currentEntityId = (String)entityIdAndTimestamp[0];
          Timestamp timestamp = (Timestamp)entityIdAndTimestamp[1];
          
          if (!StringUtils.equals(entityId, currentEntityId)) {
            continue;
          }
          //TODO put in success or not
          gsh_builtin_gshTemplateOutput.addOutputLine("Authenticated " + timestamp.toString());

        }
        
      }
            
    } else if (StringUtils.equals("Analyze user access", gsh_input_action)) {
      String gsh_input_pennkey = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_pennkey");
      String gsh_input_samlEntityIdForUser = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_samlEntityIdForUser");
      gsh_builtin_gshTemplateOutput.addOutputLine("The service provider uses the coarse grained authorization group 'ISC employees in Two-step'.");
      if (StringUtils.equals("mchyzer", gsh_input_pennkey)) {
        gsh_builtin_gshTemplateOutput.addOutputLine("The user is currently in the coarse-grained authz group: penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:iscEmployeesInTwoStep");
        gsh_builtin_gshTemplateOutput.addOutputLine("The user has been in the group since 2024-01-12 1:43 pm");
        gsh_builtin_gshTemplateOutput.addOutputLine("The user has authenticated to the service 9 times in the last week");
        gsh_builtin_gshTemplateOutput.addOutputLine("Recent successful authentication time: 2024-01-22 10:12 am");
        gsh_builtin_gshTemplateOutput.addOutputLine("Recent successful authentication time: 2024-01-22 8:45 am");
        gsh_builtin_gshTemplateOutput.addOutputLine("Recent successful authentication time: 2024-01-21 7:32 pm");
      }
      if (StringUtils.equals("isobel", gsh_input_pennkey)) {
        gsh_builtin_gshTemplateOutput.addOutputLine("The user is currently not in the coarse-grained authz group: penn:isc:ts:iam:weblogin:service:policy:idpCoarseGrained:iscEmployeesInTwoStep");
        gsh_builtin_gshTemplateOutput.addOutputLine("The user was in the group between 2023-05-12 1:43 pm - 2023-12-03 5:51 pm");
        gsh_builtin_gshTemplateOutput.addOutputLine("The user has authenticated to the service 5 times in the last week");
        gsh_builtin_gshTemplateOutput.addOutputLine("Recent successful authentication time: 2024-01-22 8:12 am");
        gsh_builtin_gshTemplateOutput.addOutputLine("Recent unsuccessful authentication time: 2024-01-19 5:45 am");
        gsh_builtin_gshTemplateOutput.addOutputLine("Recent unsuccessful authentication time: 2024-01-15 4:32 pm");
      }
      
    } else {
      throw new RuntimeException("Invalid action: '" + gsh_input_action + "'");
    }
    

    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");

  }

  public static void main(String[] args) {

    GshTemplateV2input gshTemplateV2input = new GshTemplateV2input();

    GshTemplateV2output gshTemplateV2output = new GshTemplateV2output();

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Subject subject = SubjectFinder.findByIdAndSource("10021368", "pennperson", true);

        gshTemplateV2input.setGsh_builtin_subject(subject);
        GshTemplateRuntime gshTemplateRuntime = new GshTemplateRuntime();
        gshTemplateRuntime.setTemplateConfigId("ssoCoarseGrainedAuthz");

        
        Test76ssoCoarseGrained myGshTemplate = new Test76ssoCoarseGrained();

        //gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_action", "Analyze user history");
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_action", "Analyze application");
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_samlEntityId", "https://upenndar-compass.my.salesforce.com");
        
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_pennkey", "mchyzer");
                
        myGshTemplate.gshRunLogic(gshTemplateV2input, gshTemplateV2output);
        return null;
      }
    });
    System.out.println(GrouperUtil.toStringForLog(gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getValidationLines()));
    System.out.println(GrouperUtil.toStringForLog(gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getOutputLines()));
    System.exit(0);
  }

  @Override
  public void decorateTemplateForUiDisplay(GshTemplateDecorateForUiInput gshTemplateDecorateForUiInput) {
    
    String stemName = gshTemplateDecorateForUiInput.getOwnerStemName();

    boolean prod = StringUtils.equals(stemName, "penn:isc:ts:iam:weblogin:service:templates:idpRegistrationProd");
    if (!stemName.startsWith("penn:isc:ts:iam:weblogin:service:templates:")) {
      throw new RuntimeException("Invalid owner folder, should start with: 'penn:isc:ts:iam:weblogin:service:templates:'");
    }

    String tableName = "sso_stage_entity_front_door";
    if (prod) {
      tableName = "sso_prod_entity_front_door";
    }

    GshTemplateInputConfigAndValue dropDownConfigAndValueAction = gshTemplateDecorateForUiInput.getGshTemplateInputConfigAndValues().get("gsh_input_action");
    
    GshTemplateInputConfigAndValue dropDownConfigAndValueEntityId = gshTemplateDecorateForUiInput.getGshTemplateInputConfigAndValues().get("gsh_input_samlEntityId");
    
    if (StringUtils.equalsAny(dropDownConfigAndValueAction.getValue(), "Analyze application", "Analyze user access")) {

      List<String> entityIds = new GcDbAccess().sql("select entity_id from sso_prod_entity_id spei order by 1").selectList(String.class);
      
      List<MultiKey> keysAndValues = new ArrayList<MultiKey>();
      
      keysAndValues.add(new MultiKey("", ""));
      for (String entityId : entityIds) {
        keysAndValues.add(new MultiKey(entityId, entityId));
      }
      
      dropDownConfigAndValueEntityId.getGshTemplateInputConfig().setDropdownKeysAndLabels(keysAndValues);
    }
    
    if (gshTemplateDecorateForUiInput.isSubmit()) {

      String gsh_input_action = dropDownConfigAndValueAction.getValue();
      if (StringUtils.equals("Analyze user history", gsh_input_action) || StringUtils.equals("Analyze user access", gsh_input_action)) {
        
//        GshTemplateInputConfigAndValue dropDownConfigAndValue = gshTemplateDecorateForUiInput.getGshTemplateInputConfigAndValues().get("gsh_input_samlEntityIdForUser");
//        List<MultiKey> localDropdownKeysAndLabels = new ArrayList<MultiKey>();
//        localDropdownKeysAndLabels.add(new MultiKey("flash", "value1"));
        
      }
    }
    
    
//    // dynamically set the options for drop down 1
//    List<MultiKey> localDropdownKeysAndLabels = new ArrayList<MultiKey>();
//    localDropdownKeysAndLabels.add(new MultiKey("", ""));
//    localDropdownKeysAndLabels.add(new MultiKey("key1", "value1"));
//    localDropdownKeysAndLabels.add(new MultiKey("key2", "value2"));
//    dropDownConfigAndValue.getGshTemplateInputConfig().setDropdownKeysAndLabels(localDropdownKeysAndLabels);
//    
//    GshTemplateInputConfigAndValue dropDownConfigAndValue2 = gshTemplateDecorateForUiInput.getGshTemplateInputConfigAndValues().get("gsh_input_dropDown2");
//    
//    // if drop down 1 has somethign set
//    if (!StringUtils.isBlank(dropDownConfigAndValue.getValueOrDefault())) {
//
//      // if the first option is selected, set the drop down 2 options to one set 
//      if (StringUtils.equals("key1", dropDownConfigAndValue.getValueOrDefault())) {
//        List<MultiKey> localDropdownKeysAndLabels2 = new ArrayList<MultiKey>();
//        localDropdownKeysAndLabels2.add(new MultiKey("", ""));
//        localDropdownKeysAndLabels2.add(new MultiKey("key1a", "value1a"));
//        localDropdownKeysAndLabels2.add(new MultiKey("key1b", "value1b"));
//        dropDownConfigAndValue2.getGshTemplateInputConfig().setDropdownKeysAndLabels(localDropdownKeysAndLabels2);
//        
//        // if the second option is selected, set the drop down 2 options to another set
//      } else if (StringUtils.equals("key2", dropDownConfigAndValue.getValueOrDefault())) {
//        List<MultiKey> localDropdownKeysAndLabels2 = new ArrayList<MultiKey>();
//        localDropdownKeysAndLabels2.add(new MultiKey("", ""));
//        localDropdownKeysAndLabels2.add(new MultiKey("key2a", "value2a"));
//        localDropdownKeysAndLabels2.add(new MultiKey("key2b", "value2b"));
//        dropDownConfigAndValue2.getGshTemplateInputConfig().setDropdownKeysAndLabels(localDropdownKeysAndLabels2);
//        
//      }
//      
//    }
//
//    // if drop down 1 is blank, hide the textfield (it is shown by default)
//    if (StringUtils.isBlank(dropDownConfigAndValue.getValue())) {
//      
//      gshTemplateDecorateForUiInput.getGshTemplateInputConfigAndValues().remove("gsh_input_textField");
//
//      // if drop down 1 was changed, then set the text field to its value (overwrite whats there)
//    } else if (StringUtils.equals("gsh_input_dropDown", gshTemplateDecorateForUiInput.getEventConfigId())) {
//      
//      GshTemplateInputConfigAndValue textFieldConfigAndValue = gshTemplateDecorateForUiInput.getGshTemplateInputConfigAndValues().get("gsh_input_textField");
//      
//      // if the first option for drop down 1 is there, then set to one thing
//      if (StringUtils.equals("key1", dropDownConfigAndValue.getValue())) {
//        textFieldConfigAndValue.setValue("<key1 value default>");
//        
//        // if the second option for drop down 1 is there, then set to another thing
//      } else if (StringUtils.equals("key2", dropDownConfigAndValue.getValue())) {
//        textFieldConfigAndValue.setValue("<key2 value default>");
//      }
//      
//      // if drop down 1 is blank, leave the textfield unchanged
    }

  private static boolean isInGroup(TheState theState, String groupNameOrBucketName, String pennId, Long millis) {
    String groupName = groupNameOrBucketName;
    if (theState.bucketLabelToGroupName.containsKey(groupNameOrBucketName)) {
      groupName = theState.bucketLabelToGroupName.get(groupNameOrBucketName);
    }
    Map<String, List<Object[]>> pennIdToPointInTime = theState.groupNameToPennIdToPointInTime.get(groupName);
    if (pennIdToPointInTime == null) {
      return false;
    }
    List<Object[]> startMillisEndMillises = pennIdToPointInTime.get(pennId);
    if (startMillisEndMillises == null) {
      return false;
    }
    for (Object[] startMillisEndMillis : startMillisEndMillises) {
      Long startMillis = (Long)startMillisEndMillis[0];
      Long endMillis = (Long)startMillisEndMillis[1];
      if (startMillis != null && startMillis < millis && endMillis == null) {
        return true;
      }
      if (startMillis != null && startMillis < millis && endMillis != null && endMillis > millis) {
        return true;
      }
    }
    return false;
  }

}
```

## Shibboleth config

**Resolver**

```
<AttributeDefinition id="coarseAuthz" xsi:type="ScriptedAttribute">
  <InputDataConnector ref="userDB" attributeNames="eduPersonEntitlement"/>
  <InputDataConnector ref="ldap-apps" attributeNames="cn appPolicy" />
  <Script>
    <![CDATA[
      logger = Java.type("org.slf4j.LoggerFactory").getLogger("resolver/coarseAuthz")
      coarseMapping = {'policy1':'grouper:path:one','policy2':'grouper:path:two','policy3':'grouper:path:three'}
      appPolicy = ''
      if (typeof appPolicy != "undefined" && appPolicy.getValues().size() > 0) {
        appPolicyValue = appPolicy.getValues().get(0)
        if (typeof eduPersonEntitlement !== 'undefined' && eduPersonEntitlement !== null && !eduPersonEntitlement.getValues().empty) {
          if (!eduPersonEntitlement.getValues().contains(coarseMapping[appPolicyValue])) {
            coarseAuthz.addValue("unauthorized")
          } 
        } else {
          coarseAuthz.addValue("unauthorized")
        }
      }
    ]]>
</Script>
</AttributeDefinition>
```

Relied on a DataConnector for LDAP, and a standard ContextCheck Intercept

**Intercept condition check**

```
<bean id="shibboleth.coarse-authz.Condition" parent="shibboleth.Conditions.NOT">
  <constructor-arg>
    <list>
      <bean class="net.shibboleth.idp.profile.logic.SimpleAttributePredicate" p:useUnfilteredAttributes="true">
        <property name="attributeValueMap">
          <map>
            <entry key="coarseAuthz">
              <list>
                <value>unauthorized</value>
              </list>
            </entry>
          </map>
        </property>
      </bean>
    </list>
  </constructor-arg>
</bean>
```

**velocity template for relying party**

```
#set ($rpContext = $profileRequestContext.getSubcontext('net.shibboleth.profile.context.RelyingPartyContext'))
#set ($spEntityId = $rpContext.getRelyingPartyId())
#set ($spEntityId = $encoder.encodeForHTMLAttribute($spEntityId))
```

**intercept velocity view**

```
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>$title - $titleSuffix</title>
    <meta http-equiv="refresh" content="0;url=#springMessage("coarse-authz.url")$spEntityId">   </head>
</html>
```

## Sync Grouper database of configs to LDAP

Note: Penn will use logical replication in the future, but it started out syncing to LDAP

This is the full sync. Each time a config is saved in the GSH template, it is saved to LDAP

```
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

//public class Test76ssoCoarseGrainedDaemon {
  
  public static String ldapAttributeValue(LdapEntry ldapEntry, String attributeName) {
    
    LdapAttribute ldapAttribute = ldapEntry.getAttribute(attributeName);
    
    if (ldapAttribute == null) {
      return null;
    }
    
    Collection<String> stringValues = ldapAttribute.getStringValues();
    
    if (stringValues == null) {
      return "";
    }

    if (stringValues.size() > 1) {
      throw new RuntimeException(stringValues.size() + " values for attribute " + attributeName + " for entry " + ldapEntry.getDn());
    }
    
    return stringValues.iterator().next();
  }

  public static void createLdapModificationItem(List<LdapModificationItem> ldapModificationItems, String attributeName, String oldValue, String newValue) {
    
    if (oldValue == null) {
      ldapModificationItems.add(new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute(attributeName, newValue)));
    } else if (StringUtils.isBlank(newValue)) {
      ldapModificationItems.add(new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute(attributeName)));
    } else {
      ldapModificationItems.add(new LdapModificationItem(LdapModificationType.REPLACE_ATTRIBUTE, new LdapAttribute(attributeName, newValue)));
    }
  }

//  public static void main(String[] args) {

    Map<String, Object> debugMap = new LinkedHashMap<>();

    boolean isProd = false;
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = OtherJobScript.retrieveHib3GrouperLoaderLogNotNull();
    if(hib3GrouperLoaderLog != null && StringUtils.equals("OTHER_JOB_webloginFrontDoorSyncProd", hib3GrouperLoaderLog.getJobName())) {
      isProd = true;
    }

    String tableName = "sso_stage_entity_front_door";
    String ldapConnection = "iamLdapStagingApplications";
    String pennGroupsFolderExtension = "idpFrontDoorStaging";

    if (isProd) {
      tableName = "sso_prod_entity_front_door";
      ldapConnection = "iamLdapProdApplications";
      pennGroupsFolderExtension = "idpFrontDoorProd";
    }

    String groupPrefix = "urn:mace:upenn.edu:penn:isc:ts:iam:weblogin:service:policy:" + pennGroupsFolderExtension + ":service:policy:managers:ssoFrontDoor_";
    
    List<Object[]> frontDoorConfigs = new GcDbAccess().sql("select entity_id, entity_id_sha_hex, name, description, url, url_instructions, "
        + "case when coarse_grained_policy = 'none' then null else coarse_grained_policy end as coarse_grained_policy, "
        + "front_door_policy_group, '" + groupPrefix + "' || name_without_special_characters || '_admins' as admin_group_name, "
        + "'" + groupPrefix + "' || name_without_special_characters || '_owners' as owner_group_name from " + tableName).
      selectList(Object[].class);
      
    Map<String, Object[]> entityIdHexToDbFrontDoorConfig = new HashMap<>();
    
    for (Object[] frontDoorConfig : frontDoorConfigs) {
      entityIdHexToDbFrontDoorConfig.put((String)frontDoorConfig[1], frontDoorConfig);
      String frontDoorPolicy = (String)frontDoorConfig[7];
      if (!StringUtils.isBlank(frontDoorPolicy)) {
        frontDoorPolicy = "urn:mace:upenn.edu:" + frontDoorPolicy;
        frontDoorConfig[7] = frontDoorPolicy;
      }
    }

    hib3GrouperLoaderLog.setTotalCount(entityIdHexToDbFrontDoorConfig.size());

    Map<String, Object[]> entityIdHexToLdapFrontDoorConfig = new HashMap<>();
    
    List<LdapEntry> list = LdapSessionUtils.ldapSession().list(ldapConnection, "ou=applications,dc=authz,dc=upenn,dc=edu", 
        LdapSearchScope.ONELEVEL_SCOPE, "(cn=*)", null, null);

    for (LdapEntry ldapEntry : list) {
      
      String entityId = ldapAttributeValue(ldapEntry, "appEntityId");
      String entityIdShaHex = ldapAttributeValue(ldapEntry, "cn");
      String name = ldapAttributeValue(ldapEntry, "appName");
      String description = ldapAttributeValue(ldapEntry, "appDescription");
      String url = ldapAttributeValue(ldapEntry, "appTestUrl");
      String urlInstructions = ldapAttributeValue(ldapEntry, "appTestNote");
      String frontDoorPolicy = ldapAttributeValue(ldapEntry, "appMACPolicy");
      String appPolicyGroup = ldapAttributeValue(ldapEntry, "appPolicyGroup");
      String appAdminEntitlement = ldapAttributeValue(ldapEntry, "appAdminEntitlement");
      String appOwnerEntitlement = ldapAttributeValue(ldapEntry, "appOwnerEntitlement");

      Object[] frontDoorConfig = new Object[10];
      frontDoorConfig[0] = entityId;
      frontDoorConfig[1] = entityIdShaHex;
      frontDoorConfig[2] = name;
      frontDoorConfig[3] = description;
      frontDoorConfig[4] = url;
      frontDoorConfig[5] = urlInstructions;
      frontDoorConfig[6] = frontDoorPolicy;
      frontDoorConfig[7] = appPolicyGroup;
      frontDoorConfig[8] = appAdminEntitlement;
      frontDoorConfig[9] = appOwnerEntitlement;
      entityIdHexToLdapFrontDoorConfig.put(entityIdShaHex, frontDoorConfig);
    }
      
    Set<String> entityIdHexsToInsert = new HashSet<>(entityIdHexToDbFrontDoorConfig.keySet());
    entityIdHexsToInsert.removeAll(entityIdHexToLdapFrontDoorConfig.keySet());
    
    for (String entityIdShaHex : entityIdHexsToInsert) {
      Object[] dataRow = entityIdHexToDbFrontDoorConfig.get(entityIdShaHex);
      
      String entityId = (String)dataRow[0];
      
      String name = (String)dataRow[2];
      String description = (String)dataRow[3];
      String url = (String)dataRow[4];
      String urlInstructions = (String)dataRow[5];
      String coarseGrainedPolicy = (String)dataRow[6];
      String policyFrontDoorGroup = (String)dataRow[7];
      
      String appAdminEntitlement = (String)dataRow[8];
      String appOwnerEntitlement = (String)dataRow[9];
            
      // cn=c77a2c1fd25902a3e09e313af5cd4d0da29b7405,ou=applications,dc=penncommunity,dc=upenn,dc=edu
      String dn = "cn=" + entityIdShaHex + ",ou=applications,dc=authz,dc=upenn,dc=edu";
      LdapEntry ldapEntry = new LdapEntry(dn);
      ldapEntry.addAttribute(new LdapAttribute("cn", entityIdShaHex));
      ldapEntry.addAttribute(new LdapAttribute("objectClass", "sAMLApp"));
      ldapEntry.addAttribute(new LdapAttribute("appEntityId", entityId));
      ldapEntry.addAttribute(new LdapAttribute("appAdminEntitlement", appAdminEntitlement));
      
      ldapEntry.addAttribute(new LdapAttribute("appOwnerEntitlement", appOwnerEntitlement));
      
      ldapEntry.addAttribute(new LdapAttribute("appDescription", description));
      if (!StringUtils.isBlank(coarseGrainedPolicy)) {
        ldapEntry.addAttribute(new LdapAttribute("appMACPolicy", coarseGrainedPolicy));
      }
      ldapEntry.addAttribute(new LdapAttribute("appName", name));
      if (!StringUtils.isBlank(policyFrontDoorGroup)) {
        ldapEntry.addAttribute(new LdapAttribute("appPolicyGroup", policyFrontDoorGroup));
      }

      if (!StringUtils.isBlank(urlInstructions)) {
        ldapEntry.addAttribute(new LdapAttribute("appTestNote", urlInstructions));
      }
      if (!StringUtils.isBlank(url)) {
        ldapEntry.addAttribute(new LdapAttribute("appTestURL", url));
      }
  
      LdapSessionUtils.ldapSession().create(ldapConnection, ldapEntry);
      
      hib3GrouperLoaderLog.addInsertCount(1);

      debugMap.put("insert_" + entityIdShaHex, entityIdShaHex);
      
    }
    
    Set<String> entityIdHexsToDelete = new HashSet<>(entityIdHexToLdapFrontDoorConfig.keySet());
    entityIdHexsToDelete.removeAll(entityIdHexToDbFrontDoorConfig.keySet());

    for (String entityIdShaHex : entityIdHexsToDelete) {
      Object[] dataRow = entityIdHexToLdapFrontDoorConfig.get(entityIdShaHex);
     
      String dn = "cn=" + entityIdShaHex + ",ou=applications,dc=authz,dc=upenn,dc=edu";

      LdapSessionUtils.ldapSession().delete(ldapConnection, dn);

      debugMap.put("delete_" + entityIdShaHex, entityIdShaHex);

      hib3GrouperLoaderLog.addDeleteCount(1);

    }
    
    Set<String> entityIdHexsToUpdate = new HashSet<>(entityIdHexToDbFrontDoorConfig.keySet());
    entityIdHexsToUpdate.retainAll(entityIdHexToLdapFrontDoorConfig.keySet());
    
    for (String entityIdShaHex : entityIdHexsToUpdate) {
      Object[] dataRowDb = entityIdHexToDbFrontDoorConfig.get(entityIdShaHex);
      Object[] dataRowLdap = entityIdHexToLdapFrontDoorConfig.get(entityIdShaHex);
      boolean hasChange = false;
      List<LdapModificationItem> ldapModificationItems = new ArrayList<LdapModificationItem>();

      if (!StringUtils.equals(StringUtils.trimToNull((String)dataRowDb[0]), StringUtils.trimToNull((String)dataRowLdap[0]))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appEntityId", (String)dataRowLdap[0], (String)dataRowDb[0]);
      }
      if (!StringUtils.equals(StringUtils.trimToNull((String)dataRowDb[2]), StringUtils.trimToNull((String)dataRowLdap[2]))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appName", (String)dataRowLdap[2], (String)dataRowDb[2]);
      }
      if (!StringUtils.equals(StringUtils.trimToNull((String)dataRowDb[3]), StringUtils.trimToNull((String)dataRowLdap[3]))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appDescription", (String)dataRowLdap[3], (String)dataRowDb[3]);
      }
      if (!StringUtils.equals(StringUtils.trimToNull((String)dataRowDb[4]), StringUtils.trimToNull((String)dataRowLdap[4]))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appTestURL", (String)dataRowLdap[4], (String)dataRowDb[4]);
      }
      if (!StringUtils.equals(StringUtils.trimToNull((String)dataRowDb[5]), StringUtils.trimToNull((String)dataRowLdap[5]))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appappTestNoteTestURL", (String)dataRowLdap[5], (String)dataRowDb[5]);
      }
      if (!StringUtils.equals(StringUtils.trimToNull((String)dataRowDb[6]), StringUtils.trimToNull((String)dataRowLdap[6]))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appMACPolicy", (String)dataRowLdap[6], (String)dataRowDb[6]);
      }
      if (!StringUtils.equals(StringUtils.trimToNull((String)dataRowDb[7]), StringUtils.trimToNull((String)dataRowLdap[7]))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appPolicyGroup", (String)dataRowLdap[7], (String)dataRowDb[7]);
      }
      if (!StringUtils.equals(StringUtils.trimToNull((String) dataRowDb[8]), StringUtils.trimToNull((String) dataRowLdap[8]))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appAdminEntitlement", (String) dataRowLdap[8], (String) dataRowDb[8]);
      }
      if (!StringUtils.equals(StringUtils.trimToNull((String) dataRowDb[9]), StringUtils.trimToNull((String) dataRowLdap[9]))) {
        hasChange = true;
        createLdapModificationItem(ldapModificationItems, "appOwnerEntitlement", (String) dataRowLdap[9], (String) dataRowDb[9]);
      }
      if (!hasChange) {
        continue;
      }
      
      if (ldapModificationItems.size() > 0) {

        hib3GrouperLoaderLog.addUpdateCount(1);
        debugMap.put("update_" + entityIdShaHex, true);

        // cn=c77a2c1fd25902a3e09e313af5cd4d0da29b7405,ou=applications,dc=penncommunity,dc=upenn,dc=edu
        String dn = "cn=" + entityIdShaHex + ",ou=applications,dc=authz,dc=upenn,dc=edu";
        LdapSessionUtils.ldapSession().internal_modifyHelper(ldapConnection, dn, ldapModificationItems);
      }
      

    }
    
    String debugMapForLog = GrouperUtil.toStringForLog(debugMap);
    hib3GrouperLoaderLog.setJobMessage(debugMapForLog);
    if (OtherJobScript.retrieveFromThreadLocal() == null) {
      System.out.println(debugMapForLog);
      System.exit(0);
    }
//  }
//}
```

## Training example

1. Policy group allows users in front door in Shibboleth
2. Manual override will allow admins to add users who have taken training but the data has not yet propagated
  
  1. This group could have a rule which automatically adds an end date on new memberships, e.g. a few days
3. Manual deny group allows removals from the policy (generally for training purposes)
4. Eligibility group has nursing workers or students, in two step, and trained recently
5. Reference group in the training folder for people who have taken HIPAA and REDCap training in the last year
6. People who have taken REDCap training in the last year
7. People who have taken HIPAA training in the last year
8. Nursing reference group for all workers and students

Error page with no training

Custom UI config in grouper.properties

```
grouperCustomUI.nursingTrainedTest.cuQuery.0.bindVar0 = \u0024{subject.id}
grouperCustomUI.nursingTrainedTest.cuQuery.0.bindVar0Type = string
grouperCustomUI.nursingTrainedTest.cuQuery.0.configId = grouper
grouperCustomUI.nursingTrainedTest.cuQuery.0.enabled = true
grouperCustomUI.nursingTrainedTest.cuQuery.0.label = Can log in, and has been able to for the last 30 minutes
grouperCustomUI.nursingTrainedTest.cuQuery.0.order = 0
grouperCustomUI.nursingTrainedTest.cuQuery.0.query = select case when is_user_for_30_min = 'T' then 1 else 0 end as the_result from penn_custom_ui_mships_v where group_name in ('test\u003Anursing\u003Aapps\u003Aredcap\u003Aredcap_frontdoor_test' ) and subject_source_id = 'pennperson' and subject_id = ?
grouperCustomUI.nursingTrainedTest.cuQuery.0.userQueryType = sql
grouperCustomUI.nursingTrainedTest.cuQuery.0.variableToAssign = cu_nursingTrainedCanLogInProvisioned
grouperCustomUI.nursingTrainedTest.cuQuery.0.variableType = boolean
grouperCustomUI.nursingTrainedTest.cuQuery.1.fieldNames = members
grouperCustomUI.nursingTrainedTest.cuQuery.1.groupName = penn\u003Acommunity\u003Aauthentication\u003AtwoStepUsers
grouperCustomUI.nursingTrainedTest.cuQuery.1.label = In two-step
grouperCustomUI.nursingTrainedTest.cuQuery.1.order = 10
grouperCustomUI.nursingTrainedTest.cuQuery.1.userQueryType = grouper
grouperCustomUI.nursingTrainedTest.cuQuery.1.variableToAssign = cu_twoStep
grouperCustomUI.nursingTrainedTest.cuQuery.1.variableType = boolean
grouperCustomUI.nursingTrainedTest.cuQuery.2.fieldNames = members
grouperCustomUI.nursingTrainedTest.cuQuery.2.groupName = test\u003Anursing\u003Aapps\u003Aredcap\u003Aredcap_frontdoor_test
grouperCustomUI.nursingTrainedTest.cuQuery.2.label = In policy group
grouperCustomUI.nursingTrainedTest.cuQuery.2.order = 20
grouperCustomUI.nursingTrainedTest.cuQuery.2.userQueryType = grouper
grouperCustomUI.nursingTrainedTest.cuQuery.2.variableToAssign = cu_nursingTrained
grouperCustomUI.nursingTrainedTest.cuQuery.2.variableType = boolean
grouperCustomUI.nursingTrainedTest.cuQuery.3.fieldNames = members
grouperCustomUI.nursingTrainedTest.cuQuery.3.groupName = penn\u003Aisc\u003Aait\u003Aapps\u003AknowledgeLink\u003AcompletedWhenRequired\u003AHS_30006_ITEM_HIPAA_Y19
grouperCustomUI.nursingTrainedTest.cuQuery.3.label = HIPAA trained
grouperCustomUI.nursingTrainedTest.cuQuery.3.order = 30
grouperCustomUI.nursingTrainedTest.cuQuery.3.userQueryType = grouper
grouperCustomUI.nursingTrainedTest.cuQuery.3.variableToAssign = cu_hipaaTrained
grouperCustomUI.nursingTrainedTest.cuQuery.3.variableType = boolean
grouperCustomUI.nursingTrainedTest.cuQuery.4.fieldNames = members
grouperCustomUI.nursingTrainedTest.cuQuery.4.groupName = penn\u003Anursing\u003Acommunity\u003Anursing_all
grouperCustomUI.nursingTrainedTest.cuQuery.4.label = In nursing_all
grouperCustomUI.nursingTrainedTest.cuQuery.4.order = 100
grouperCustomUI.nursingTrainedTest.cuQuery.4.userQueryType = grouper
grouperCustomUI.nursingTrainedTest.cuQuery.4.variableToAssign = cu_nursingAll
grouperCustomUI.nursingTrainedTest.cuQuery.4.variableType = boolean
grouperCustomUI.nursingTrainedTest.cuQuery.5.bindVar0 = \u0024{subject.id}
grouperCustomUI.nursingTrainedTest.cuQuery.5.bindVar0Type = string
grouperCustomUI.nursingTrainedTest.cuQuery.5.configId = snowflakeProdAuthzadm
grouperCustomUI.nursingTrainedTest.cuQuery.5.label = HIPAA expire date
grouperCustomUI.nursingTrainedTest.cuQuery.5.order = 50
grouperCustomUI.nursingTrainedTest.cuQuery.5.query = select to_char(max(end_date), 'YYYY/MM/DD HH24\u003AMI') as the_end_date from AUTHZ_LMS_COURSEYR_EXPIRE_V where penn_id = ? and item_id = 'HS.30006.ITEM.HIPAA_Y19'
grouperCustomUI.nursingTrainedTest.cuQuery.5.userQueryType = sql
grouperCustomUI.nursingTrainedTest.cuQuery.5.variableToAssign = cu_hipaaEndDate
grouperCustomUI.nursingTrainedTest.cuQuery.5.variableType = string
grouperCustomUI.nursingTrainedTest.cuQuery.6.fieldNames = members
grouperCustomUI.nursingTrainedTest.cuQuery.6.groupName = penn\u003Aisc\u003Aait\u003Aapps\u003AknowledgeLink\u003AcompletedWhenRequired\u003AUP_40009_ITEM_REDCAPPHI
grouperCustomUI.nursingTrainedTest.cuQuery.6.label = REDCap trained
grouperCustomUI.nursingTrainedTest.cuQuery.6.order = 60
grouperCustomUI.nursingTrainedTest.cuQuery.6.userQueryType = grouper
grouperCustomUI.nursingTrainedTest.cuQuery.6.variableToAssign = cu_redcapTrained
grouperCustomUI.nursingTrainedTest.cuQuery.6.variableType = boolean
grouperCustomUI.nursingTrainedTest.cuQuery.7.fieldNames = members
grouperCustomUI.nursingTrainedTest.cuQuery.7.groupName = penn\u003Acommunity\u003Astudent
grouperCustomUI.nursingTrainedTest.cuQuery.7.label = Student
grouperCustomUI.nursingTrainedTest.cuQuery.7.order = 100
grouperCustomUI.nursingTrainedTest.cuQuery.7.userQueryType = grouper
grouperCustomUI.nursingTrainedTest.cuQuery.7.variableToAssign = cu_student
grouperCustomUI.nursingTrainedTest.cuQuery.7.variableType = boolean
grouperCustomUI.nursingTrainedTest.cuQuery.8.bindVar0 = \u0024{subject.id}
grouperCustomUI.nursingTrainedTest.cuQuery.8.bindVar0Type = string
grouperCustomUI.nursingTrainedTest.cuQuery.8.configId = snowflakeProdAuthzadm
grouperCustomUI.nursingTrainedTest.cuQuery.8.label = REDCap expire date
grouperCustomUI.nursingTrainedTest.cuQuery.8.order = 80
grouperCustomUI.nursingTrainedTest.cuQuery.8.query = select to_char(max(end_date), 'YYYY/MM/DD HH24\u003AMI') as the_end_date from AUTHZ_LMS_COURSEYR_EXPIRE_V where penn_id = ? and item_id = 'UP.40009.ITEM.REDCAPPHI'
grouperCustomUI.nursingTrainedTest.cuQuery.8.userQueryType = sql
grouperCustomUI.nursingTrainedTest.cuQuery.8.variableToAssign = cu_redcapEndDate
grouperCustomUI.nursingTrainedTest.cuQuery.8.variableType = string
grouperCustomUI.nursingTrainedTest.cuQuery.9.fieldNames = members
grouperCustomUI.nursingTrainedTest.cuQuery.9.groupName = penn\u003Acommunity\u003AemployeeOrContractorIncludingUphs
grouperCustomUI.nursingTrainedTest.cuQuery.9.label = Workforce
grouperCustomUI.nursingTrainedTest.cuQuery.9.order = 90
grouperCustomUI.nursingTrainedTest.cuQuery.9.userQueryType = grouper
grouperCustomUI.nursingTrainedTest.cuQuery.9.variableToAssign = cu_workforce
grouperCustomUI.nursingTrainedTest.cuQuery.9.variableType = boolean
grouperCustomUI.nursingTrainedTest.cuTextConfig.0.defaultText = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.0.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.0.endIfMatches = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.0.index = 10
grouperCustomUI.nursingTrainedTest.cuTextConfig.0.text = <h1>REDCap Eligibility</h1>
grouperCustomUI.nursingTrainedTest.cuTextConfig.0.textType = header
grouperCustomUI.nursingTrainedTest.cuTextConfig.1.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.1.endIfMatches = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.1.index = 0
grouperCustomUI.nursingTrainedTest.cuTextConfig.1.text =  
grouperCustomUI.nursingTrainedTest.cuTextConfig.1.textType = helpLink
grouperCustomUI.nursingTrainedTest.cuTextConfig.10.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.10.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.10.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.10.index = 10
grouperCustomUI.nursingTrainedTest.cuTextConfig.10.script = \u0024{ cu_nursingTrainedCanLogInProvisioned && cu_nursingTrained }
grouperCustomUI.nursingTrainedTest.cuTextConfig.10.text = <li><b>REDCap access provisioned\u003A</b> yes. You have had REDCap access for more than 30 minutes ago and it is ready to use.</li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.10.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.11.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.11.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.11.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.11.index = 8
grouperCustomUI.nursingTrainedTest.cuTextConfig.11.script = \u0024{ cu_nursingTrained }
grouperCustomUI.nursingTrainedTest.cuTextConfig.11.text = <li><b>REDCap eligibility\u003A</b> yes. You have satisfied the REDCap requirements.</li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.11.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.12.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.12.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.12.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.12.index = 9
grouperCustomUI.nursingTrainedTest.cuTextConfig.12.script = \u0024{ !cu_nursingTrained }
grouperCustomUI.nursingTrainedTest.cuTextConfig.12.text = <li><b>REDCap access\u003A</b> <b style="color\u003A brown">no. You do not have REDCap access.</b></li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.12.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.13.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.13.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.13.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.13.index = 20
grouperCustomUI.nursingTrainedTest.cuTextConfig.13.script = \u0024{ !cu_hipaaTrained }
grouperCustomUI.nursingTrainedTest.cuTextConfig.13.text = <li><b>HIPAA training\u003A</b> <b style="color\u003A brown">You are required to take <a href="https\u003A//tinyurl.com/pennHipaa">HIPAA training</a>.  Access will be restored the next day.</b></li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.13.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.14.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.14.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.14.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.14.index = 21
grouperCustomUI.nursingTrainedTest.cuTextConfig.14.script = \u0024{ cu_hipaaTrained }
grouperCustomUI.nursingTrainedTest.cuTextConfig.14.text = <li><b>HIPAA training\u003A</b> yes. You have taken HIPAA training in the past year.</li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.14.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.15.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.15.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.15.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.15.index = 25
grouperCustomUI.nursingTrainedTest.cuTextConfig.15.script = \u0024{ cu_hipaaTrained && cu_hipaaEndDate != null }
grouperCustomUI.nursingTrainedTest.cuTextConfig.15.text = <li><b>HIPAA end date\u003A</b> \u0024{cu_hipaaEndDate}.  Be sure to <a href="https\u003A//tinyurl.com/pennHipaa" style="text-decoration\u003Aunderline !important">take the HIPAA training again</a> before that date.</li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.15.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.16.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.16.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.16.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.16.index = 2000
grouperCustomUI.nursingTrainedTest.cuTextConfig.16.text = </ul>
grouperCustomUI.nursingTrainedTest.cuTextConfig.16.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.17.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.17.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.17.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.17.index = 30
grouperCustomUI.nursingTrainedTest.cuTextConfig.17.script = \u0024{ !cu_redcapTrained }
grouperCustomUI.nursingTrainedTest.cuTextConfig.17.text = <li><b>REDCap training\u003A</b> <b style="color\u003A brown">You are required to take <a href="https\u003A//tinyurl.com/pennRedcap">REDCap training</a>.  Access will be restored the next day.</b></li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.17.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.18.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.18.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.18.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.18.index = 31
grouperCustomUI.nursingTrainedTest.cuTextConfig.18.script = \u0024{ cu_redcapTrained }
grouperCustomUI.nursingTrainedTest.cuTextConfig.18.text = <li><b>REDCap training\u003A</b> yes. You taken REDCap training in the past year.</li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.18.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.19.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.19.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.19.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.19.index = 35
grouperCustomUI.nursingTrainedTest.cuTextConfig.19.script = \u0024{ cu_redcapTrained && cu_redcapEndDate != null }
grouperCustomUI.nursingTrainedTest.cuTextConfig.19.text = <li><b>REDCap end date\u003A</b> \u0024{cu_hipaaEndDate}.  Be sure to <a href="https\u003A//tinyurl.com/pennRedcap" style="text-decoration\u003Aunderline !important">take the REDCap training again</a> before that date.</li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.19.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.2.defaultText = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.2.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.2.endIfMatches = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.2.index = 0
grouperCustomUI.nursingTrainedTest.cuTextConfig.2.textBoolean = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.2.textIsScript = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.2.textType = unenrollButtonShow
grouperCustomUI.nursingTrainedTest.cuTextConfig.20.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.20.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.20.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.20.index = 40
grouperCustomUI.nursingTrainedTest.cuTextConfig.20.script = \u0024{ !cu_nursingAll && cu_workforce }
grouperCustomUI.nursingTrainedTest.cuTextConfig.20.text = <li><b>Penn Nursing workforce\u003A</b> <b style="color\u003A brown">no. You are in the Workforce but you are not in a Nursing organization.  Please contact your University sponsor.</b></li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.20.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.21.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.21.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.21.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.21.index = 41
grouperCustomUI.nursingTrainedTest.cuTextConfig.21.script = \u0024{ !cu_nursingAll && cu_student }
grouperCustomUI.nursingTrainedTest.cuTextConfig.21.text = <li>Penn Nursing student\u003A <b style="color\u003A brown">no. You are a Penn student but not in the Nursing school.</b></li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.21.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.22.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.22.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.22.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.22.index = 42
grouperCustomUI.nursingTrainedTest.cuTextConfig.22.script = \u0024{ !cu_nursingAll && !cu_student && !cu_workforce }
grouperCustomUI.nursingTrainedTest.cuTextConfig.22.text = <li><b>Penn workforce or student\u003A</b> <b style="color\u003A brown">no.  You do not have a valid Penn affiliation.  Please contact your University sponsor.</b></li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.22.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.23.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.23.endIfMatches = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.23.index = 10
grouperCustomUI.nursingTrainedTest.cuTextConfig.23.script = \u0024{!cu_nursingTrainedCanLogInProvisioned && cu_nursingTrained}
grouperCustomUI.nursingTrainedTest.cuTextConfig.23.text = <font style="color\u003Abrown"><b>Warning\u003A</b></font> You have access but you need to wait 30 minutes for data to propagate to WebLogin.  Please wait and try again.
grouperCustomUI.nursingTrainedTest.cuTextConfig.23.textType = enrollmentLabel
grouperCustomUI.nursingTrainedTest.cuTextConfig.24.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.24.endIfMatches = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.24.index = 20
grouperCustomUI.nursingTrainedTest.cuTextConfig.24.script = \u0024{cu_nursingTrainedCanLogInProvisioned}
grouperCustomUI.nursingTrainedTest.cuTextConfig.24.text = <font style="color\u003Agreen"><b>Success\u003A</b></font> You have access and have had access for more than 30 minutes.  Try the application again.
grouperCustomUI.nursingTrainedTest.cuTextConfig.24.textType = enrollmentLabel
grouperCustomUI.nursingTrainedTest.cuTextConfig.25.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.25.endIfMatches = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.25.index = 30
grouperCustomUI.nursingTrainedTest.cuTextConfig.25.script = \u0024{!cu_nursingTrainedCanLogInProvisioned && !cu_nursingTrained}
grouperCustomUI.nursingTrainedTest.cuTextConfig.25.text = <font style="color\u003Abrown"><b>Error\u003A</b></font> You do not have access.
grouperCustomUI.nursingTrainedTest.cuTextConfig.25.textType = enrollmentLabel
grouperCustomUI.nursingTrainedTest.cuTextConfig.26.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.26.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.26.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.26.index = 43
grouperCustomUI.nursingTrainedTest.cuTextConfig.26.script = \u0024{ cu_nursingAll }
grouperCustomUI.nursingTrainedTest.cuTextConfig.26.text = <li><b>Penn Nursing\u003A</b> yes. You are working or studying in the School of Nursing.</b></li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.26.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.3.defaultText = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.3.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.3.endIfMatches = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.3.index = 0
grouperCustomUI.nursingTrainedTest.cuTextConfig.3.textBoolean = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.3.textIsScript = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.3.textType = enrollButtonShow
grouperCustomUI.nursingTrainedTest.cuTextConfig.4.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.4.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.4.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.4.index = 0
grouperCustomUI.nursingTrainedTest.cuTextConfig.4.text =  
grouperCustomUI.nursingTrainedTest.cuTextConfig.4.textType = enrollmentLabel
grouperCustomUI.nursingTrainedTest.cuTextConfig.5.defaultText = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.5.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.5.endIfMatches = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.5.index = 0
grouperCustomUI.nursingTrainedTest.cuTextConfig.5.textBoolean = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.5.textIsScript = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.5.textType = manageMembership
grouperCustomUI.nursingTrainedTest.cuTextConfig.6.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.6.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.6.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.6.index = 71
grouperCustomUI.nursingTrainedTest.cuTextConfig.6.script = \u0024{ !cu_twoStep }
grouperCustomUI.nursingTrainedTest.cuTextConfig.6.text = <li><b>Two-Step Verification\u003A</b> <b style="color\u003A brown">you are not enrolled, please <a href="https\u003A//isc.upenn.edu/pennkey/two-step-verification-enrollment-instructions" style="text-decoration\u003Aunderline !important">enroll</a> or contact <a href="https\u003A//isc.upenn.edu/pennkey/contact">PennKey support</a> for help.</b></li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.6.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.7.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.7.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.7.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.7.index = 72
grouperCustomUI.nursingTrainedTest.cuTextConfig.7.script = \u0024{ cu_twoStep }
grouperCustomUI.nursingTrainedTest.cuTextConfig.7.text = <li><b>Two-Step Verification\u003A</b> yes. You are enrolled.</li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.7.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.8.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.8.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.8.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.8.index = 11
grouperCustomUI.nursingTrainedTest.cuTextConfig.8.script = \u0024{ !cu_nursingTrainedCanLogInProvisioned && cu_nursingTrained }
grouperCustomUI.nursingTrainedTest.cuTextConfig.8.text = <li><b>REDCap access provisioned\u003A</b> <b style="color\u003A brown">no. You need to wait 30 minutes for data propagation.</b></li>
grouperCustomUI.nursingTrainedTest.cuTextConfig.8.textType = instructions1
grouperCustomUI.nursingTrainedTest.cuTextConfig.9.defaultText = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.9.enabled = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.9.endIfMatches = false
grouperCustomUI.nursingTrainedTest.cuTextConfig.9.index = -20
grouperCustomUI.nursingTrainedTest.cuTextConfig.9.script = true
grouperCustomUI.nursingTrainedTest.cuTextConfig.9.text = This service requires being a Penn Nursing worker or student, enrollment in Two-Step Verification, and recent training in HIPAA and REDCap.  Once you have completed training, you need to wait until the next day to be able to log in.<br /><br /><ul>
grouperCustomUI.nursingTrainedTest.cuTextConfig.9.textType = instructions1
grouperCustomUI.nursingTrainedTest.groupCanAssignVariables = test\u003Anursing\u003Aapps\u003Aredcap\u003Aredcap_test_form_admins_can_assign_url_params
grouperCustomUI.nursingTrainedTest.groupCanSeeScreenState = test\u003Anursing\u003Aapps\u003Aredcap\u003Aredcap_test_form_admins
grouperCustomUI.nursingTrainedTest.groupCanSeeUserEnvironment = test\u003Anursing\u003Aapps\u003Aredcap\u003Aredcap_test_form_admins
grouperCustomUI.nursingTrainedTest.groupOfManagers = test\u003Anursing\u003Aapps\u003Aredcap\u003Aredcap_test_form_admins
grouperCustomUI.nursingTrainedTest.groupUUIDOrName = test\u003Anursing\u003Aapps\u003Aredcap\u003Aredcap_frontdoor_test
grouperCustomUI.nursingTrainedTest.numberOfQueries = 10
grouperCustomUI.nursingTrainedTest.numberOfTextConfigs = 27
```

## On demand licenses

This is up and coming but we would like users to claim licenses as they log in. This is an example for BP Logix our workflow system

1. BP Logix sends an SFTP file of users and if they have assigned tasks. File is CSV and goes to depot.
2. Grouper loads that file to a table: bplogix_active_users, and bplogix_active_users_stage
3. Fed group of current users via depot, loads from the SQL table. Note, users must match a valid eppn
4. Users with tasks in bplogix: these are automatically fed from the nightly CSV SFTP feed from BP Logix to Grouper (there is no web service to retrieve all users)
5. Users with tasks get an automatic license since they need to use BP Logix. Note: if the user is not eligible by reference group (front door) they cannot sign in (affiliate or alum in prod, or workforce with twostep in stage)
6. Some users have an automatic license.
  
  
  ```
  where group_name like 'penn:isc:ait:apps:bplogix:service:policy:userFeedGroups:%' and group_name != 'penn:isc:ait:apps:bplogix:service:policy:userFeedGroups:BannerManaged:ALL.ActiveBannerAcadAdvisors' and group_name != 'penn:isc:ait:apps:bplogix:service:policy:userFeedGroups:BannerManaged:ALL.ActiveBannerAdvisors' and group_name != 'penn:isc:ait:apps:bplogix:service:policy:userFeedGroups:ALL.FAC' and group_name != 'penn:isc:ait:apps:bplogix:service:policy:userFeedGroups:ALL.AccessAdministrators:PennantSecurityGroups:SRFS.SecurityAdminSubmit'
  ```
  
  
  
  
  
  1. Anyone with an automatic license is in the “has a license” group
7. Has self-claimed a license
  
  
  
  1. Anyone who has self-claimed a license is in the has claimed a license group
  2. The “has a license” group is the front door policy group.
8. The “has a license” group is also the provisionable group. When users get added or removed they get provisioned or deprovisioned in bplogix.
9. Shibboleth via the logging service (these are fed from the [recent authentication loader](https://penngroups.isc.upenn.edu/features/recentAuthentications)) 60 days back:
10. The “claim a license” screen is redirected to when a user is not in the front door reference group or policy group.
11. When a user claims a license from the “claim a license” screen it adds them as in “opt in” to the has claimed license group
12. When the user is on the “claim a license” screen, they only see the “get access” button when they are in the “can claim license” group.
13. Bplogix user claimed license recently. If the users claims a license they have 60 days to log in to BP Logix.
14. Group for recently claimed license or recently logged in contains both groups
  
  
  
  1. This are three rules which make this happen
15. Recent authn feeds the group: recent authn or recent claim access
16. If a user has not recently claimed access or recently logged in, and they have a claimed license, they will lose that license (and need to claim it again)
