---
title: "Grouper Custom UI example at Penn MFA and O365"
space: Grouper
pageId: 28555175
version: 10
lastUpdated: 2026-07-01T05:38:46.966Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555175/Grouper+Custom+UI+example+at+Penn+MFA+and+O365
---

> For more general information, see [Grouper Custom UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549064/Grouper+Custom+UI)

## Requirements

- Set up a group that people can enroll or unenroll in to test their MFA with O365
- Only allow people to use this who have a date in the future where they will be required
- Dont allow people already required to be able to unenroll
- Show if the assignment is provisioned into Azure (if the MFA is enabled)
- Show if the assignment is provisioned into LDAP from PSPNG (intermediary destination, then it flows from LDAP/AD into Azure)
- Require the user be in MFA, and if not, give a link to enroll
- Require the user have an O365 mailbox, and if not, notify the user
- Managers (readers and updaters) should be able to check on a user, see why their access is not correct, and enroll or unenroll them
- Emails should be sent to users when they enroll or unenroll (or a manager does it for them) so they have information about unenrolling or re-enrolling later

## User variables

### Configuration

```
{
   "variableToAssign":"cu_o365twoStepTeam",
   "userQueryType":"grouper",
   "groupName":"penn:isc:ait:apps:O365:o365twoStepTeam",
   "label":"On O365 Two-Step team",
   "order":110,
   "forLoggedInUser":true
}
{
   "variableToAssign":"cu_o365twoStepEnrolled",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod",
   "label":"${textContainer.text['penn_o365twoStep_cu_o365twoStepEnrolled']}",
   "order":10
}
{
   "variableToAssign":"cu_o365twoStepSelfEnrolled",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepSelfEnrolled",
   "label":"${textContainer.text['penn_o365twoStep_cu_o365twoStepSelfEnrolled']}",
   "order":20
}
{
   "variableToAssign":"cu_o365twoStepCanEnrollUnenroll",
   "fieldNames":"optins,optouts",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepSelfEnrolled",
   "label":"${textContainer.text['penn_o365twoStep_cu_o365twoStepCanEnrollUnenroll']}",
   "order":30
}
{
   "variableToAssign":"cu_o365twoStepRequiredToEnroll",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod_policy",
   "label":"${textContainer.text['penn_o365twoStep_cu_o365twoStepRequiredToEnroll']}",
   "order":40
}
{
   "variableToAssign":"cu_o365twoStepRequiredDate",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select min(value_string) from authz_o365_twostep_req_date_v where subject_id = ?",
   "label":"${textContainer.text['penn_o365twoStep_cu_o365twoStepRequiredDate']}",
   "order":50
}
{
   "variableToAssign":"cu_o365twoStepInAzure",
   "userQueryType":"azure",
   "configId":"pennAzure",
   "variableToAssignOnError":"cu_o365twoStepInAzureError",
   "groupName":"penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod",
   "label":"${textContainer.text['penn_o365twoStep_cu_o365twoStepInAzure']}",
   "errorLabel":"${textContainer.text['penn_o365twoStep_cu_o365twoStepInAzureError']}",
   "order":60
}
{
   "variableToAssign":"cu_o365hasMailbox",
   "userQueryType":"grouper",
   "groupName":"penn:isc:ait:apps:O365:o365hasMailbox",
   "label":"${textContainer.text['penn_o365twoStep_cu_o365hasMailbox']}",
   "order":80
}
{
   "variableToAssign":"cu_twoStepUsers",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:community:authentication:twoStepUsers",
   "label":"${textContainer.text['penn_o365twoStep_cu_twoStepUsers']}",
   "order":90
}
{
   "variableToAssign":"cu_o365twoStepInLdap",
   "userQueryType":"ldap",
   "variableToAssignOnError":"cu_o365twoStepInLdapError",
   "groupName":"penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod",
   "label":"${textContainer.text['penn_o365twoStep_cu_o365twoStepInLdap']}",
   "errorLabel":"${textContainer.text['penn_o365twoStep_cu_o365twoStepInLdapError']}",
   "order":70,
   "configId":"oneProdAd"
}
{
   "variableToAssign":"cu_o365twoStepAllowedToManage",
   "fieldNames":"updaters,readers",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepSelfEnrolled",
   "label":"${textContainer.text['penn_o365twoStep_cu_o365twoStepAllowedToManage']}",
   "order":100,
   "forLoggedInUser":true
}
{
   "variableToAssign":"default",
   "ldapSearchDn":"DC=one,DC=upenn,DC=edu",
   "ldapFilter":"(&(objectclass=user)(employeeID=${subject.getId()})(memberof=CN=${group.getName()},OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu))",
   "ldapAttributeToRetrieve":"employeeID"
}
```

## Text configuration

## Configuration

```
{
   "endIfMatches":true,
   "customUiTextType":"canAssignVariables",
   "index":0,
   "text":"${cu_o365twoStepTeam}"
}
{
   "customUiTextType":"emailToUser",
   "index":0,
   "text":"true"
}
{
   "endIfMatches":true,
   "customUiTextType":"emailSubject",
   "index":0,
   "text":"${cu_grouperEnroll ? textContainer.text['penn_o365twoStep_enroll_emailSubject'] : textContainer.text['penn_o365twoStep_unenroll_emailSubject']}"
}
{
   "endIfMatches":true,
   "customUiTextType":"emailBody",
   "index":10,
   "text":"${textContainer.text['penn_o365twoStep_unenroll_emailBody']}",
   "script":"${!cu_grouperEnroll}"
}
{
   "endIfMatches":true,
   "customUiTextType":"emailBody",
   "index":0,
   "text":"${textContainer.text['penn_o365twoStep_enroll_emailBody']}",
   "script":"${cu_grouperEnroll}"
}
{
   "endIfMatches":true,
   "customUiTextType":"canSeeUserEnvironment",
   "index":0,
   "text":"${cu_o365twoStepAllowedToManage}"
}
{
   "endIfMatches":true,
   "customUiTextType":"canSeeScreenState",
   "index":0,
   "text":"false"
}
{
   "endIfMatches":true,
   "customUiTextType":"emailBccGroupName",
   "index":0,
   "text":"penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepAllowedToAdmin"
}
{
   "endIfMatches":true,
   "customUiTextType":"instructions1",
   "index":60,
   "text":"${textContainer.text['penn_o365twoStep_instructions_notRequiredCannotEnroll']}",
   "script":"${!cu_o365twoStepRequiredToEnroll}"
}
{
   "endIfMatches":true,
   "customUiTextType":"unenrollButtonShow",
   "index":0,
   "text":"${cu_o365twoStepEnrolled && cu_o365twoStepCanEnrollUnenroll && !cu_o365twoStepRequiredToEnroll}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollButtonShow",
   "index":0,
   "text":"${!cu_o365twoStepEnrolled && cu_o365twoStepCanEnrollUnenroll && cu_twoStepUsers && cu_o365hasMailbox}"
}
{
   "endIfMatches":true,
   "customUiTextType":"managerInstructions",
   "index":0,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_instructions_manager']}",
   "script":"${cu_o365twoStepAllowedToManage}"
}
{
   "endIfMatches":true,
   "customUiTextType":"unenrollButtonText",
   "index":0,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_unenrollButtonText']}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollButtonText",
   "index":0,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_enrollButtonText']}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":70,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_enrollLabel_enrolledPendingNotInLdapButInAzure']}",
   "script":"${cu_o365twoStepEnrolled && !cu_o365twoStepInAzure}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":60,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_enrollLabel_enrolledPendingInLdapAndAzure']}",
   "script":"${cu_o365twoStepEnrolled && (cu_o365twoStepInLdapError || !cu_o365twoStepInLdap) && !cu_o365twoStepInAzure}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":50,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_enrollLabel_enrolled']}",
   "script":"${cu_o365twoStepEnrolled && cu_o365twoStepInAzure}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":40,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_enrollLabel_enrolledErrorInAzure']}",
   "script":"${cu_o365twoStepEnrolled && cu_o365twoStepInAzureError}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":30,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_enrollLabel_notEnrolledPendingNotInLdapButInAzure']}",
   "script":"${!cu_o365twoStepEnrolled && cu_o365twoStepInAzure}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":20,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_enrollLabel_notEnrolledPendingInLdapAndAzure']}",
   "script":"${!cu_o365twoStepEnrolled && (cu_o365twoStepInLdapError || cu_o365twoStepInLdap) && cu_o365twoStepInAzure}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":10,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_enrollLabel_notEnrolled']}",
   "script":"${!cu_o365twoStepEnrolled && !cu_o365twoStepInAzure}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":0,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_enrollLabel_notEnrolledErrorInAzure']}",
   "script":"${!cu_o365twoStepEnrolled && cu_o365twoStepInAzureError}"
}
{
   "endIfMatches":true,
   "customUiTextType":"instructions1",
   "index":50,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_instructions_notEnrolledButCanEnroll']}",
   "script":"${!cu_o365twoStepRequiredToEnroll && !cu_o365twoStepEnrolled && cu_o365twoStepCanEnrollUnenroll && cu_twoStepUsers && cu_o365hasMailbox}"
}
{
   "endIfMatches":true,
   "customUiTextType":"instructions1",
   "index":40,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_instructions_needsO365']}",
   "script":"${!cu_o365twoStepRequiredToEnroll && !cu_o365twoStepEnrolled && cu_o365twoStepCanEnrollUnenroll && !cu_o365hasMailbox}"
}
{
   "endIfMatches":true,
   "customUiTextType":"instructions1",
   "index":30,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_instructions_needsTwoStep']}",
   "script":"${!cu_o365twoStepRequiredToEnroll && !cu_o365twoStepEnrolled && cu_o365twoStepCanEnrollUnenroll && !cu_twoStepUsers}"
}
{
   "endIfMatches":true,
   "customUiTextType":"instructions1",
   "index":20,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_instructions_willBeRequiredToEnroll']}",
   "script":"${ !cu_o365twoStepRequiredToEnroll && cu_o365twoStepEnrolled && cu_o365hasMailbox && cu_o365twoStepCanEnrollUnenroll}"
}
{
   "endIfMatches":true,
   "customUiTextType":"instructions1",
   "index":10,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_instructions_requiredToEnroll']}",
   "script":"${cu_o365twoStepRequiredToEnroll}"
}
{
   "customUiTextType":"helpLink",
   "defaultText":true,
   "text":"${textContainer.text['penn_o365twoStep_helplink']}"
}
{
   "customUiTextType":"header",
   "defaultText":true,
   "text":"${textContainer.text['penn_o365twoStep_header']}"
}
```

[grouper.text.en.us](http://grouper.text.en.us).properties

```
####################################
## Custom UI for O365 two step
####################################

# header for o365 two step custom ui
penn_o365twoStep_header = <h1>PennO365 Two-Step Verification</h1>

penn_o365twoStep_helplink = <a href="https://www.isc.upenn.edu/how-to/penno365-office-365-proplus">${textContainer.text['grouper.help'] }</a>

# top line for managers
penn_o365twoStep_instructions_manager = View status of users in Two-Step Verification with O365.  Enroll or unenroll users if applicable.<br /><br />You will see what the user sees below when you pull up a user<br /><br />

# if required to enroll, cannot opt in or opt out
penn_o365twoStep_instructions_requiredToEnroll = To improve Penn's data security, you are required to use Two-Step Verification with O365.<br /><br />

# not required, will be required, and in two step and has o365 mailbox
penn_o365twoStep_instructions_willBeRequiredToEnroll = To improve Penn's data security, you will be required to enroll in Two-Step Verification for O365 on <b>${cu_o365twoStepRequiredDate}</b>.<br /><br />

# not require, not allowed
penn_o365twoStep_instructions_notRequiredCannotEnroll = You are not required to enroll, and you are not allowed to enroll.<br /><br />

# not in two step
penn_o365twoStep_instructions_needsTwoStep = You are not enrolled in Two-Step Verification.  <a href="https://twostep.apps.upenn.edu/twoFactor/twoFactorUi/app/UiMain.index">Enroll now</a>.<br /><br />

# not in o365
penn_o365twoStep_instructions_needsO365 = You need an O365 account.<br /><br />

# can enroll, not enrolled, not required
penn_o365twoStep_instructions_notEnrolledButCanEnroll = To improve Penn's data security, you will be required to enroll in Two-Step Verification for O365 by <b>${cu_o365twoStepRequiredDate}</b>.  Enroll now to ensure that you're not locked out of email and other O365 services. <br /><br />You must enroll for O365 even if you are already enrolled in Two-Step Verification for Penn WebLogin (PennKey).<br /><br />

# not enrolled, cant check azure
penn_o365twoStep_enrollLabel_notEnrolledErrorInAzure = <b>Enrollment status:</b> <b style="color: red; font-size: 120%">Not enrolled in this system but an error occurred checking your enrollment in O365</b>

# not enrolled
penn_o365twoStep_enrollLabel_notEnrolled = <b>Enrollment status:</b> <b style="color: red; font-size: 120%">Not enrolled</b>

# not enrolled but yes in ldap and azure
penn_o365twoStep_enrollLabel_notEnrolledPendingInLdapAndAzure = <b>Enrollment status:</b> <b style="color: brown; font-size: 120%">Pending to be unenrolled.  Generally takes less than 1 hour.</b>

# not enrolled but still in azure
penn_o365twoStep_enrollLabel_notEnrolledPendingNotInLdapButInAzure = <b>Enrollment status:</b> <b style=\"color: brown; font-size: 120%\">Pending to be unenrolled.  Generally takes less than 15 minutes.</b>

# enrolled, cant check azure
penn_o365twoStep_enrollLabel_enrolledErrorInAzure = <b>Enrollment status:</b> <b style=\"color: brown; font-size: 120%\">Enrolled in this system but an error occurred checking your enrollment in O365</b>

# enrolled
penn_o365twoStep_enrollLabel_enrolled = <b>Enrollment status:</b> <b style=\"color: green; font-size: 120%\">Enrolled</b>

# enrolled but not in ldap or azure
penn_o365twoStep_enrollLabel_enrolledPendingInLdapAndAzure = <b>Enrollment status:</b> <b style=\"color: brown; font-size: 120%\">Pending.  Generally takes less than 1 hour.</b>

# enrolled but still not in azure
penn_o365twoStep_enrollLabel_enrolledPendingNotInLdapButInAzure = <b>Enrollment status:</b> <b style=\"color: brown; font-size: 120%\">Pending.  Generally takes less than 15 minutes.</b>

penn_o365twoStep_enrollButtonText = Enroll

penn_o365twoStep_unenrollButtonText = Unenroll

penn_o365twoStep_enroll_emailBody = Dear ${subject.getName()},$newline$$newline$Thank you for enrolling in Two-Step Verification for PennO365.$newline$$newline$When you log in to PennO365 using your Microsoft account, you will be routinely prompted for a single-use verification code.$newline$$newline$You can accept push notifications or generate codes on your phone using Duo Mobile. Make sure you also print out single-use verification codes in case you don't have access to that device (on the "Manage settings" page, click "Generate codes")$newline$$newline$Remember to keep your profile updated with backup phone numbers (to which single-use codes can be sent) and friends you can authorize to retrieve a code if all else fails.$newline$$newline$For more information about Two-Step Verification, see: http://upenn.edu/twostep$newline$$newline$The Penn Two-Step Support team$newline$$newline$For technical assistance with Two-Step Verification, contact the IT support staff of your school or center.  If you are unsure whom to contact, visit the Get IT Help directory: https://www.isc.upenn.edu/get-it-help$newline$$newline$Manage your enrollment: https://grouper.server.school.edu/grouper/grouperUi/app/UiV2Main.indexLite?operation=UiV2GroupLite.liteGroup&groupId=61bcaad67d57438ab1fea11c426c2f64

penn_o365twoStep_enroll_emailSubject = Penn Two-Step Verification - you have enrolled in Two-Step Verification for PennO365

penn_o365twoStep_unenroll_emailBody = Dear ${subject.getName()},$newline$$newline$You have unenrolled from Two-Step Verification for PennO365.$newline$$newline$When you log in to PennO365 using your Microsoft account you will no longer be routinely prompted for a single-use verification code.$newline$$newline$Manage your enrollment: https://grouper.server.school.edu/grouper/grouperUi/app/UiV2Main.indexLite?operation=UiV2GroupLite.liteGroup&groupId=61bcaad67d57438ab1fea11c426c2f64$newline$$newline$For more information about Two-Step Verification, see: http://upenn.edu/twostep$newline$$newline$The Penn Two-Step Support team$newline$$newline$For technical assistance with Two-Step Verification, contact the IT support staff of your school or center.  If you are unsure whom to contact, visit the Get IT Help directory: https://www.isc.upenn.edu/get-it-help$newline$$newline$Manager your enrollment: https://grouper.server.school.edu/grouper/grouperUi/app/UiV2Main.indexLite?operation=UiV2GroupLite.liteGroup&groupId=61bcaad67d57438ab1fea11c426c2f64

penn_o365twoStep_unenroll_emailSubject = Penn Two-Step Verification - you have unenrolled from Two-Step Verification for PennO365

penn_o365twoStep_cu_o365twoStepAllowedToManage = Manager of PennO365 Two-Step self enrollments
penn_o365twoStep_cu_o365twoStepCanEnrollUnenroll = Allowed to enroll or unenroll since has a future deadline
penn_o365twoStep_cu_o365twoStepSelfEnrolled = Self-enrolled in PennO365 Two-Step Verification
penn_o365twoStep_cu_o365twoStepRequiredToEnroll = Required to enroll because org was required in past
penn_o365twoStep_cu_twoStepUsers = Two-Step Verification
penn_o365twoStep_cu_o365hasMailbox = O365 mailbox
penn_o365twoStep_cu_o365twoStepInLdap = In One AD LDAP (intermediary data flow destination)
penn_o365twoStep_cu_o365twoStepInLdapError = Is there an error checking One AD?
penn_o365twoStep_cu_o365twoStepInAzure = In O365 Azure (final data flow destination) means Two-Step on for O365
penn_o365twoStep_cu_o365twoStepInAzureError = Is there an error checking Azure O365?
penn_o365twoStep_cu_o365twoStepRequiredDate = Date required to enroll
penn_o365twoStep_cu_o365twoStepEnrolled = In PennGroup for Two-Step Verification in PennO365 (required or self-enrolled)

```

## Screen examples

Someone who is allowed to enroll and is not enrolled

Someone who is allowed to unenroll and is enrolled

Manager

Someone not enrolled, but who needs MFA

Someone not enrolled who needs an O365 account

Someone enrolled, and provisioned to LDAP/AD, but not yet provisioned to Azure

Someone enrolled, but not yet provisioned to LDAP/AD or Azure

Someone enrolled but error checking to see if in Azure

Someone not enrolled in Grouper, but still in LDAP/AD and Azure

Someone not enrolled in Grouper, and LDAP/AD is also not enrolled, but still enrolled in Azure

Someone not enrolled in Grouper, but there was an error checking Azure
