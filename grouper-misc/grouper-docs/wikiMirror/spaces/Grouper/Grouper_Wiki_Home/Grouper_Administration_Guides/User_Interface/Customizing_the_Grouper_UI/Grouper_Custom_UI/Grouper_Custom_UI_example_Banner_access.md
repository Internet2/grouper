---
title: "Grouper Custom UI example Banner access"
space: Grouper
pageId: 28554465
version: 2
lastUpdated: 2020-07-26T23:01:27.547Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554465/Grouper+Custom+UI+example+Banner+access
---

This screen allows admins to analyze an end-user's access to Banner, and allows end users to get a detailed analysis of their issues. The IdP sends users to this screen if they are rejected from coarse grained-authorization.

The access policy is: you can login to Banner (you are eligible) if you

1. Have a reprieve (be default those are end-dated a week after creation)
2. or You are
  
  1. Trained in FERPA in the last year
  2. Enrolled in MFA
  3. Are an employee

Note: we also load a group of people who have Banner accounts and list that on the screen too

Note: it can take 30 minutes for access to be provisioned to the IdP, this is captured too

## User conditions

Ready for Banner

Access pending for IdP provisioning

Needs FERPA training

Missing valid employee records

Not in MFA

No banner account

Not eligible and no account

## What an access manager sees

## What a Grouper Admin sees

## Testing the screen

We have a group of admins

```
penn:isc:ait:apps:ngss:environments:prod:banner:prodBannerAdmin
```

Those admins are in READ/UPDATE on the custom UI group (so they can pull up a user and see their state

This variable assigns that fact to cu_bannerProdAllowedToManage

```
{"variableToAssign":"cu_bannerProdAllowedToManage",
 "fieldNames":"members",
 "userQueryType":"grouper",
 "variableType":"boolean",
 "groupName":"penn:isc:ait:apps:ngss:environments:prod:banner:prodBannerAdmin",
 "label":"${textContainer.text['penn_bannerProd_cu_bannerProdAllowedToManage']}",
 "order":120,
 "forLoggedInUser":true} 
```

This config means people who are admins can assign variables to the URL to test

```
{"endIfMatches":true,
 "customUiTextType":"canAssignVariables",
 "index":0,
 "text":"${cu_bannerProdAllowedToManage}"}
```

This is an example URL to test with

```
https://grouper.url.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=abc123abc123abc123&cu_o365twoStepAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerProdEligible30min=false&cu_bannerProdEligible=false&cu_bannerProdReprieved=false&cu_bannerProdHasAccount=true&cu_twoStepUsers=true&cu_bannerProdActiveEmployee=true&cu_bannerProdFerpa=false&cu_bannerProdFerpaEndDate=2020/05/04%2007:45
```

## Helper views

There are some views needed. Penn is on Grouper 2.4. There is a Jira to add these to Grouper in a database agnostic way. Until then you could add these in your database if you want to use them sooner

[JIRA GRP-2902]

## Membership analysis

Before this new feature, each group membership was just shown as a boolean. Now using a SQL query and one of the new views, you can see if the user is in the group and when that started, if the user is not in the group and when it ended, or if the user was never in the group. This is generally for the admins to use for troubleshooting unless the "user state" table is viewable by end users. Note if you have some provisioning information (e.g. 30 minutes until IDP provisioning), you could have a custom view for that

## Immediate membership end date

If there is a reprieve, that is an immediate membership with an end date. That end date is useful for users and admins to know

## Last audit

For ad hoc groups it might be useful to display the last audit for that record.

## Point in time for provisioning

If the provisioning delay is known or estimated, then a variable can be assigned with a good estimate for if provisioning has occurred (i.e. if the user has been in the group for a minimum of X amount of time).

## User query config beans

These are stored in attribute values on the custom UI group

```
{
  "variableToAssign":"cu_bannerProdPersonDescription",
  "bindVar0":"${subject.id}",
  "userQueryType":"sql",
  "variableType":"string",
  "configId":"pennCommunity",
  "bindVar0type":"string",
  "query":"select description from PCDADMIN.computed_person cp where cp.char_PENN_ID = ?",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdPersonDescription']}",
  "order":10
}

{
  "variableToAssign":"cu_bannerProdEligible30min",
  "bindVar0":"${subject.id}",
  "userQueryType":"sql",
  "variableType":"boolean",
  "configId":"grouper",
  "bindVar0type":"string",
  "query":"select case when is_user_for_30_min = 'T' then 1 else 0 end as the_result from penn_custom_ui_mships_v where group_name in ('penn:isc:ait:apps:ngss:environments:prod:banner:population:ngssBannerProdCanLogin' ) and subject_source_id = 'pennperson' and subject_id = ?",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdEligible30min']}",
  "order":20
}

{
  "variableToAssign":"cu_bannerProdEligible",
  "fieldNames":"members",
  "userQueryType":"grouper",
  "variableType":"boolean",
  "groupName":"penn:isc:ait:apps:ngss:environments:prod:banner:population:ngssBannerProdCanLogin",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdEligible']}",
  "order":30
}

{
  "variableToAssign":"cu_bannerProdEligibleAnalysis",
  "bindVar0":"${subject.id}",
  "userQueryType":"sql",
  "variableType":"string",
  "configId":"grouper",
  "bindVar0type":"string",
  "query":"select mship_shib_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:environments:prod:banner:population:ngssBannerProdCanLogin' and subject_source_id = 'pennperson' and subject_id = ?",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdEligibleAnalysis']}",
  "order":35
}

{
  "variableToAssign":"cu_bannerProdReprieved",
  "fieldNames":"members",
  "userQueryType":"grouper",
  "variableType":"boolean",
  "groupName":"penn:isc:ait:apps:ngss:environments:prod:banner:population:ngssProdPopulationReprieve",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdReprieved']}",
  "order":40
}

{
  "variableToAssign":"cu_bannerProdReprieveEnd",
  "bindVar0":"${subject.id}",
  "userQueryType":"sql",
  "variableType":"String",
  "configId":"grouper",
  "bindVar0type":"string",
  "query":"select disabled_time from penn_imm_mship_disabled_date_v where group_name = 'penn:isc:ait:apps:ngss:environments:prod:banner:population:ngssProdPopulationReprieve' and subject_source_id = 'pennperson' and subject_id = ?",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdReprieveEnd']}",
  "order":50
}

{
  "variableToAssign":"cu_bannerProdReprieveAnalysis",
  "bindVar0":"${subject.id}",
  "userQueryType":"sql",
  "variableType":"string",
  "configId":"grouper",
  "bindVar0type":"string",
  "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:environments:prod:banner:population:ngssProdPopulationReprieve' and subject_source_id = 'pennperson' and subject_id = ?",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdReprieveAnalysis']}",
  "order":55
}

{
  "variableToAssign":"cu_bannerProdReprieveLastAudit",
  "bindVar0":"${subject.id}",
  "userQueryType":"sql",
  "variableType":"string",
  "configId":"grouper",
  "bindVar0type":"string",
  "query":"select 'The last reprieve was assigned by ' || user_using_grouper_name || ' on ' || to_char(the_timestamp, 'YYYY/MM/DD HH24:MI') as reprieve_audit from penn_audit_add_membership_help_v paamv1 where group_name = 'penn:isc:ait:apps:ngss:environments:prod:banner:population:ngssProdPopulationReprieve' and user_acted_on_subject_id = ? and user_acted_on_subject_source_id = 'pennperson' and paamv1.the_timestamp = (select max(paamv2.the_timestamp) from penn_audit_add_membership_help_v paamv2 where paamv1.group_name = paamv2.group_name and paamv1.user_acted_on_member_id = paamv2.user_acted_on_member_id) limit 1",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdReprieveLastAudit']}",
  "order":57
}

{
  "variableToAssign":"cu_bannerProdHasAccount",
  "fieldNames":"members",
  "userQueryType":"grouper",
  "variableType":"boolean",
  "groupName":"penn:isc:ait:apps:ngss:environments:prod:banner:users:ngssProdHasBannerAccount",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdHasAccount']}",
  "order":60
}

{
  "variableToAssign":"cu_bannerProdHasAccountAnalysis",
  "bindVar0":"${subject.id}",
  "userQueryType":"sql",
  "variableType":"string",
  "configId":"grouper",
  "bindVar0type":"string",
  "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:environments:prod:banner:users:ngssProdHasBannerAccount' and subject_source_id = 'pennperson' and subject_id = ?",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdHasAccountAnalysis']}",
  "order":65
}

{
  "variableToAssign":"cu_twoStepUsers",
  "fieldNames":"members",
  "userQueryType":"grouper",
  "variableType":"boolean",
  "groupName":"penn:community:authentication:twoStepUsers",
  "label":"${textContainer.text['penn_o365twoStep_cu_twoStepUsers']}",
  "order":70
}

{
  "variableToAssign":"cu_bannerProdActiveEmployee",
  "fieldNames":"members",
  "userQueryType":"grouper",
  "variableType":"boolean",
  "groupName":"penn:isc:ait:apps:ngss:environments:prod:banner:population:ngssProdPopulation",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdActiveEmployee']}",
  "order":80
}

{
  "variableToAssign":"cu_bannerProdActiveEmployeeAnalysis",
  "bindVar0":"${subject.id}",
  "userQueryType":"sql",
  "variableType":"string",
  "configId":"grouper",
  "bindVar0type":"string",
  "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:environments:prod:banner:population:ngssProdPopulation' and subject_source_id = 'pennperson' and subject_id = ?",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdActiveEmployeeAnalysis']}",
  "order":85
}

{
  "variableToAssign":"cu_bannerProdActiveEmployeeEndDate",
  "bindVar0":"${subject.id}",
  "userQueryType":"sql",
  "variableType":"string",
  "configId":"pennCommunity",
  "bindVar0type":"string",
  "query":"select expire_message from employee_affil_expire_date_v where penn_id = ?",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdActiveEmployeeEndDate']}",
  "order":87
}

{
  "variableToAssign":"cu_bannerProdFerpa",
  "fieldNames":"members",
  "userQueryType":"grouper",
  "variableType":"boolean",
  "groupName":"penn:isc:ait:apps:knowledgeLink:completedWhenRequired:UP_87024_ITEM_FERPA001",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdFerpa']}",
  "order":90
}

{
  "variableToAssign":"cu_bannerProdFerpaAnalysis",
  "bindVar0":"${subject.id}",
  "userQueryType":"sql",
  "variableType":"string",
  "configId":"grouper",
  "bindVar0type":"string",
  "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:knowledgeLink:completedWhenRequired:UP_87024_ITEM_FERPA001' and subject_source_id = 'pennperson' and subject_id = ?",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdFerpaAnalysis']}",
  "order":100
}

{
  "variableToAssign":"cu_bannerProdFerpaEndDate",
  "bindVar0":"${subject.id}",
  "userQueryType":"sql",
  "variableType":"string",
  "configId":"warehouse",
  "bindVar0type":"string",
  "query":"select to_char(max(end_date), 'YYYY/MM/DD HH24:MI') as the_end_date from AUTHZ_KL_COURSEYR_EXPIRE_V where penn_id = ? and item_id = 'UP.87024.ITEM.FERPA001'",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdFerpaEndDate']}",
  "order":110
}

{
  "variableToAssign":"cu_bannerProdAllowedToManage",
  "fieldNames":"members",
  "userQueryType":"grouper",
  "variableType":"boolean",
  "groupName":"penn:isc:ait:apps:ngss:environments:prod:banner:prodBannerAdmin",
  "label":"${textContainer.text['penn_bannerProd_cu_bannerProdAllowedToManage']}",
  "order":120,
  "forLoggedInUser":true
}
```

## Text config beans

These are stored in attribute values on the custom UI group

```
{
  "endIfMatches":true,
  "customUiTextType":"header",
  "index":10,
  "defaultText":true,
  "text":"${textContainer.text['penn_bannerprod_header']}"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":0,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_eligible']}",
  "script":"${ cu_bannerProdEligible }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":10,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_ineligible']}",
  "script":"${ ! cu_bannerProdEligible }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":20,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_eligible30']}",
  "script":"${ cu_bannerProdEligible && cu_bannerProdEligible30min }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":30,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_eligibleNot30']}",
  "script":"${ cu_bannerProdEligible && !cu_bannerProdEligible30min }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":40,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_reprievedNoEnd']}",
  "script":"${ cu_bannerProdReprieved && cu_bannerProdReprieveEnd == null }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":50,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_reprievedEnd']}",
  "script":"${ cu_bannerProdReprieved && cu_bannerProdReprieveEnd != null }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":60,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_bannerAccount']}",
  "script":"${ cu_bannerProdHasAccount }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":70,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_bannerNoAccount']}",
  "script":"${ ! cu_bannerProdHasAccount }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":80,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_twoStep']}",
  "script":"${ cu_twoStepUsers }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":90,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_noTwoStep']}",
  "script":"${ ! cu_twoStepUsers }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":100,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_activeEmployee']}",
  "script":"${ cu_bannerProdActiveEmployee }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":110,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_notActiveEmployee']}",
  "script":"${ ! cu_bannerProdActiveEmployee }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":120,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_activeEmployeeEndDate']}",
  "script":"${ cu_bannerProdActiveEmployeeEndDate != null }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":130,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_ferpa']}",
  "script":"${ cu_bannerProdFerpa }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":140,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_noFerpa']}",
  "script":"${ ! cu_bannerProdFerpa }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":150,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_instructions_ferpaEndDate']}",
  "script":"${ cu_bannerProdFerpaEndDate != null }"
}

{
  "endIfMatches":false,
  "customUiTextType":"instructions1",
  "index":160,
  "defaultText":false,
  "text":"<br />",
  "script":"true"
}

{
  "endIfMatches":true,
  "customUiTextType":"enrollmentLabel",
  "index":0,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_enrollLabel_canUserBanner']}",
  "script":"${cu_bannerProdEligible && cu_bannerProdEligible30min && cu_bannerProdHasAccount}"
}

{
  "endIfMatches":true,
  "customUiTextType":"enrollmentLabel",
  "index":10,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_enrollLabel_pendingToUseBanner']}",
  "script":"${cu_bannerProdEligible && !cu_bannerProdEligible30min && cu_bannerProdHasAccount}"
}

{
  "endIfMatches":true,
  "customUiTextType":"enrollmentLabel",
  "index":20,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_enrollLabel_noAccountInBanner']}",
  "script":"${cu_bannerProdEligible && !cu_bannerProdHasAccount}"
}

{
  "endIfMatches":true,
  "customUiTextType":"enrollmentLabel",
  "index":30,
  "defaultText":false,
  "text":"${textContainer.text['penn_bannerProd_enrollLabel_cannotUseBanner']}",
  "script":"${!cu_bannerProdEligible30min && cu_bannerProdHasAccount}"
}

{
  "endIfMatches":true,
  "customUiTextType":"enrollmentLabel",
  "index":40,
  "defaultText":true,
  "text":"${textContainer.text['penn_bannerProd_enrollLabel_cantAndNoAccount']}"
}

{
  "endIfMatches":true,
  "customUiTextType":"canSeeScreenState",
  "index":0,
  "text":"false"
}

{
  "endIfMatches":true,
  "customUiTextType":"canSeeUserEnvironment",
  "index":0,
  "text":"${cu_bannerProdAllowedToManage}"
}

{
  "endIfMatches":true,
  "customUiTextType":"managerInstructions",
  "index":10,
  "defaultText":true,
  "text":"${textContainer.text['penn_bannerprod_managerInstructions']}"
}

{
  "endIfMatches":true,
  "customUiTextType":"enrollButtonShow",
  "index":0,
  "text":"false"
}

{
  "endIfMatches":true,
  "customUiTextType":"unenrollButtonShow",
  "index":0,
  "text":"false"
}

{
  "endIfMatches":true,
  "customUiTextType":"canAssignVariables",
  "index":0,
  "text":"${cu_bannerProdAllowedToManage}"
}
```

## Externalized text

grouper.text.en.us.properties

```
# ngss prod analysis

penn_bannerprod_header = <h1>Prod Banner account analysis</h1>
penn_bannerprod_helpText = Banner account eligibility is analyzed and described to help troubleshoot issues.
penn_bannerprod_managerInstructions = Search for a person to analyze their Prod Banner status

penn_bannerProd_cu_bannerProdPersonDescription = Person
penn_bannerProd_cu_bannerProdEligible30min = Eligible for 30 min (provisioned to weblogin)
penn_bannerProd_cu_bannerProdEligible = Eligible (active employee, trained, two-step, or reprieved)
penn_bannerProd_cu_bannerProdEligibleAnalysis = Eligibility analysis
penn_bannerProd_cu_bannerProdReprieved = Reprieved (exempt from eligibility requirements)
penn_bannerProd_cu_bannerProdReprieveEnd = Reprieve end date
penn_bannerProd_cu_bannerProdReprieveAnalysis = Reprieve analysis
penn_bannerProd_cu_bannerProdReprieveLastAudit = Latest reprieve audit
penn_bannerProd_cu_bannerProdHasAccount = Has Banner account
penn_bannerProd_cu_bannerProdHasAccountAnalysis = Banner account analysis
penn_bannerProd_cu_bannerProdActiveEmployee = Active employee
penn_bannerProd_cu_bannerProdActiveEmployeeAnalysis = Active employee analysis
penn_bannerProd_cu_bannerProdActiveEmployeeEndDate = Employee end date
penn_bannerProd_cu_bannerProdFerpa = FERPA trained
penn_bannerProd_cu_bannerProdFerpaAnalysis = FERPA analysis
penn_bannerProd_cu_bannerProdFerpaEndDate = FERPA expire date
penn_bannerProd_cu_bannerProdAllowedToManage = Allowed to view other users

penn_bannerProd_enrollLabel_canUserBanner = <b>Banner status:</b> <b style="color: green">Can use Banner in production</b>

penn_bannerProd_enrollLabel_pendingToUseBanner = <b>Banner status:</b> <b style="color: brown">Pending: access should work in less than 30 minutes, try again soon</b>

penn_bannerProd_enrollLabel_noAccountInBanner = <b>Banner status:</b> <b style="color: brown">You are eligible to use Banner but you do not have a Banner account</b>

# not enrolled
penn_bannerProd_enrollLabel_cannotUseBanner = <b>Banner status:</b> <b style="color: brown">You have a banner account but you are not eligible</b>

penn_bannerProd_enrollLabel_cantAndNoAccount = <b>Banner status:</b> <b style="color: brown">You are not eligible to use Bannner and you do not have an account</b>

penn_bannerProd_instructions_eligible = You are eligible to use production Banner.$space$
penn_bannerProd_instructions_ineligible = <b style="color: brown">You are not eligible to use production Banner.</b>$space$ 
penn_bannerProd_instructions_eligible30 = You have been eligible for more than 30 minutes, so Weblogin should allow you through to production Banner.$space$
penn_bannerProd_instructions_eligibleNot30 = <b style="color: brown">You have been eligible for fewer than 30 minutes so you will have to wait for your Weblogin Banner access.  Try again soon.</b>$space$

penn_bannerProd_instructions_reprievedEnd = <b style="color: brown">You have an eligibility reprieve that ends on ${cu_bannerProdReprieveEnd}.</b>$space$
penn_bannerProd_instructions_reprievedNoEnd = You have an eligibility reprieve with no end date.$space$

penn_bannerProd_instructions_bannerAccount = You have a Banner account.$space$
penn_bannerProd_instructions_bannerNoAccount = <b style="color: brown">You do not have a Banner account.</b>$space$

penn_bannerProd_instructions_twoStep = You are enrolled in Two-Step Verification.$space$
penn_bannerProd_instructions_noTwoStep = <b style="color: brown">You are not enrolled in Two-Step Verification, please <a href="https://twostep.apps.upenn.edu" style="text-decoration:underline !important">enroll</a>.</b>$space$

penn_bannerProd_instructions_activeEmployee = You are an active employee.$space$
penn_bannerProd_instructions_notActiveEmployee = <b style="color: brown">Your data does not reflect that you are an active employee.</b>$space$
penn_bannerProd_instructions_activeEmployeeEndDate = <b style="color: brown">Your employment record ends on ${cu_bannerProdActiveEmployeeEndDate}.  If you need Banner access after that date be sure to coordinate with your Business Administrator.</b>.$space$

penn_bannerProd_instructions_ferpa = You are trained in FERPA.$space$
penn_bannerProd_instructions_noFerpa = <b style="color: brown">You are required to take <a href="https://tinyurl.com/ngssFerpa" style="text-decoration:underline !important">FERPA training</a>.</b>.$space$

penn_bannerProd_instructions_ferpaEndDate = Your FERPA end date is ${cu_bannerProdFerpaEndDate}.  Be sure to <a href="https://tinyurl.com/ngssFerpa" style="text-decoration:underline !important">take the FERPA training again</a> before that date.$space$

```
