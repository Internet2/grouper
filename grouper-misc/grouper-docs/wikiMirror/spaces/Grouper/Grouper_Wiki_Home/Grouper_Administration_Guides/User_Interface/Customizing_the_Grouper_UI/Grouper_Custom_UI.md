---
title: "Grouper Custom UI"
space: Grouper
pageId: 28549064
version: 39
lastUpdated: 2026-07-01T05:42:52.281Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549064/Grouper+Custom+UI
---

The Grouper Custom UI can be applied per group in your Grouper system to provide special capabilities, including:

- Helps end users and administrators view and troubleshoot access state and problems
- (optional) Allows end users to easily opt in or opt out of a group without all the bells and whistles of the Grouper UI
- When the user joins/leaves, or when a manager adds/removes someone, an optional custom email can be sent to the user

Grouper Custom UI is a new feature in API patch 2.4.96+ and included in Grouper v2.5+

> Grouper Custom Templates via GSH are a different feature from Grouper Custom UI.  
> See [this page to learn about Custom Templates via GSH.](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544720/Grouper+custom+template+via+GSH)

To use Grouper Custom UI, configure this in the grouper.properties or on the Miscellaneous → Custom UI screen

Then there is link in the More Actions menu if you have the custom UI attributes assigned on the group

That link goes to the custom ui, here is an example from penn:

If a user is a manager (READ/UPDATE), then they will be able to pull users up, see the screen they see, see the answers to all their variables, and enroll or unenroll those users

The configuration used to be done with JSON from simple javabeans, but now is done in grouper.properties configuration or from the UI. See the [Atlassian Jira Custom UI example for the current way to configure](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555220/Grouper+Custom+UI+example+to+analyze+Jira+cloud+account)

## User query config bean

These configs identify variables that can be used in the screen to conditionally set text, adjust email text, etc. They are set from some operation like checking a membership in a group or an LDAP or SQL call or something

The queries are of type: userQueryType which is from the enum: CustomUiUserQueryType:

- grouper: check a membership or privilege
- ldap: execute an ldap filter
- sql: run a sql query against grouper or another database
- expressionLanguage: some expression (could call java)
- azure: check an azure membership or user object
- zoom: check zoom api and make attributes available (added 2.5.35+)

The queries assign variables which must be prefixed with "cu_" for "Custom UI"

You can configure a default that will fill in values for all config beans (maybe useful if there are a bunch of similar ldap calls)

| Field | Type | Required for type | Optional for type | Description |
| --- | --- | --- | --- | --- |
| allowAssignFromUrl | Boolean |  | all | If a URL param can assign this URL for all users |
| attributeDefId | String |  | expressionLanguage, grouper, sql | uuid of attribute def to look up |
| azureGroupId | String | azure |  | if hardcoding the uuid of group in azure |
| bindVar0 | String |  | sql | bind var for sql |
| bindVar0type | String |  | sql | bind var type in sql: string or integer |
| bindVar1 | String |  | sql | bind var for sql |
| bindVar1type | String |  | sql | bind var type in sql: string or integer |
| bindVar2 | String |  | sql | bind var for sql |
| bindVar2type | String |  | sql | bind var type in sql: string or integer |
| configId | String | azure, duo, ldap, zoom | sql | id in grouper config for azure, duo, ldap, or sql |
| duoGroupName | String | duo |  | if checking a group membership in duo, this is the name of the group in duo |
| enabled | Boolean |  | azure, duo, expressionLanguage, grouper, ldap, sql, zoom | true or false if this var is enabled |
| errorLabel | String | azure, zoom | expressionLanguage, duo, grouper, ldap, sql | label on screen for the error variable |
| fieldNames | String |  | grouper | comma separated privs in grouper, e.g. members, readers, admins, viewers, updaters, optins, optouts, groupAttrReaders, groupAttrUpdaters, creators, stemAdmins, stemAttrReaders, stemAttrUpdaters, attrReaders, attrUpdaters, attrDefAttrReaders, attrDefAttrUpdaters, attrOptins, attrOptouts, attrAdmins |
| forLoggedInUser | Boolean |  | azure, duo, expressionLanguage, grouper, ldap, sql | true if should run this rule for the logged in user (if manager using screen), or by default its the user being acted on (which might be the user logged in) |
| groupId | String |  | azure, expressionLanguage, grouper, ldap, sql | group uuid to look up a group |
| groupName | String |  | azure, expressionLanguage, grouper, ldap, sql | group name to look up a group |
| label | String | azure, duo, expressionLanguage, grouper, ldap, sql, zoom |  | label to see on screen when variables are displayed |
| ldapAttributeToRetrieve | String | ldap |  | which attribute in ldap to retrieve |
| ldapFilter | String | ldap |  | ldap filter to run |
| ldapSearchDn | String |  | ldap | if not using the default dn in connection, search in this dn |
| nameOfAttributeDef | String |  | expressionLanguage, grouper, sql | name of attribute definition to lookup |
| order | Integer |  | azure, duo, expressionLanguage, grouper, ldap, sql, zoom | integer and rules will be ordered by this integer, when displayed on screen |
| query | String | sql |  | sql query to execute |
| script | String | expressionLanguage | azure(2.5.30+), duo, zoom | EL expression to run |
| stemId | String |  | expressionLanguage, grouper, sql | uuid of stem to lookup |
| stemName | String |  | expressionLanguage, grouper, sql | name of stem to lookup |
| userQueryType | String | azure, duo, expressionLanguage, grouper, ldap, sql, zoom |  | identify the type of query, enter either: azure, expressionLanguage, grouper, ldap, sql. dropdown. |
| variableToAssign | String | azure, duo, expressionLanguage, grouper, ldap, sql, zoom |  | name of variable must start with cu_   if the value is "default" then each attribute value will be the default for any bean without that set where its a valid value. dropdown.   you cant have two variables with the same name |
| variableToAssignOnError | String | azure, duo, zoom | expressionLanguage, grouper, ldap, sql | name of variable to assign on error, must start with cu_      you cant have two variables with the same name |
| variableType | String |  | expressionLanguage, grouper, ldap, sql, azure, duo, zoom | type of variable: boolean, integer, string |

### Examples

Default user query bean to set some LDAP settings

```
{
   "variableToAssign":"default",
   "ldapSearchDn":"DC=one,DC=upenn,DC=edu",
   "ldapFilter":"(&(objectclass=user)(employeeID=${subject.getId()})(memberof=CN=${group.getName()},OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu))",
   "ldapAttributeToRetrieve":"employeeID"
}
```

Check if a user is in a group

```
{
   "variableToAssign":"cu_o365twoStepSelfEnrolled",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepSelfEnrolled",
   "label":"${textContainer.text['penn_o365twoStep_cu_o365twoStepSelfEnrolled']}",
   "order":20
}
```

Note the label in the [grouper.text.en.us](http://grouper.text.en.us).properties

```
penn_o365twoStep_cu_o365twoStepSelfEnrolled = Self-enrolled in PennO365 Two-Step Verification
```

If the person using the app (logged in user) is a manager (reader and updater on the group)

```
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
```

Note the label in the [grouper.text.en.us](http://grouper.text.en.us).properties

```
penn_o365twoStep_cu_o365twoStepAllowedToManage = Manager of PennO365 Two-Step self enrollments
```

Find the min required date (custom attribute on group) that the operated on user is a member of

```
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
```

The view is:

```
CREATE OR REPLACE VIEW penngrouper.authz_o365_twostep_req_date_v AS
 SELECT gaagv.value_string,
    gmlv.subject_id
   FROM grouper_aval_asn_group_v gaagv,
    grouper_memberships_lw_v gmlv
  WHERE gaagv.group_name = gmlv.group_name 
AND gmlv.list_name = 'members' 
AND gmlv.subject_source = 'pennperson' 
AND gaagv.enabled = 'T' 
AND gaagv.group_name like 'penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepGroupsToBeRequired:%' 
AND gaagv.attribute_def_name_name = 'penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepRequiredDate';

```

Ldap query (note: depends on default bean above)

```
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
```

# Built-in variables

These variables are there for you to key off. Note: you should not start your variable names with "cu_grouper" since that namespace is for built in variables

| Variable name | Description |
| --- | --- |
| cu_grouperEnroll | true if the user clicked the enroll button, false if the user clicked the unenroll button (e.g. for email templates) |
| cu_grouperTurnOffManager | url variable if you do not want to see the manager panel |

# Text config beans

These are a little misnamed, they are for text or for other decisions about the screen. Could be a boolean result. It is strongly encouraged to externalize text in the UI externalized text config

These generally take the variables defined above, and use boolean logic to decide which text to show or who is allowed to do certain things

The type of text is the decision or text to compute. The engine will run through the config and append the ones that match, unless the one that matches says to stop processing. You can also have defaults

Every text bean returns a string, but if its "true" or "false" it will be interpreted as a boolean.

| Text type | Type | Description |
| --- | --- | --- |
| canAssignVariables | boolean | if the screen allows variables to be assigned in the URL for testing. e.g. to simulate various users and see how the screen responds   note: only allow trusted users to be able to do this. Only Grouper admins can do this by default |
| canSeeScreenState | boolean | if the screen state analysis should be displayed on the screen to help the user understand why access exists or not.    By default only Grouper admins can see screen state. Note that more columns of the user environment will also display |
| canSeeUserEnvironment | boolean | if the user variables and results should display. By default group readers and updaters can see this. Note that these are   abbreviated if the user cannoy also see screen state |
| emailBccGroupName | String | if there are emails and a group should be bcc'ed then return the group name here |
| emailBody | String | if there are emails then this is the body. Note you can have a template that is dynamic, or different templates in different scenarios |
| emailSubject | String | if there are emails then this is the subject. Note you can have a template that is dynamic, or different templates in different scenarios |
| emailToUser | boolean | true if an email should be sent to user. Note you can send under certain circumstances if you like |
| enrollButtonShow | boolean | true if the enroll button should show. Note that the user cant enroll if they dont have optin on the group |
| enrollButtonText | String | Button text of enroll button. Defaults to: Enroll |
| enrollmentLabel | String | Text above the enrollment button that shows the state of the enrollment or whatever else |
| header | String | The H1 of the page |
| helpLink | String | Link where the help button goes |
| instructions1 | String | Instructions at the top of the page |
| logo | String | Link for logo |
| managerInstructions | String | Instructions to appear for readers/updaters who are managing users in this group |
| unenrollButtonShow | boolean | true if the unenroll button should show. Note that the user cant enroll if they dont have optout on the group. Note that   the enroll and unenroll button will not show at once |
| unenrollButtonText | String | Button text of unenroll button. Defaults to: Unenroll |
| manageMembership | boolean | (v2.5.38+) true or false, if the button will add the user to the group or remove them |
| redirectToUrl | String | (v2.5.38+) URL that the user should be redirected to after clicking button |
| gshScript | String | (v2.5.38+) GSH script that should be run. Note this is a stripped down version of GSH, so only use Java (no GSH functions), and    fully qualify anything not in the base grouper or util java package. This is for performance reasons. |

### GSH script to generate a text bean

```
    customUiTextConfigBean = new edu.internet2.middleware.grouper.ui.customUi.CustomUiTextConfigBean();

    customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.instructions1.name());
    customUiTextConfigBean.setIndex(10);
    customUiTextConfigBean.setScript("${cu_o365twoStepRequiredToEnroll}");
    customUiTextConfigBean.setText("${textContainer.text['penn_o365twoStep_instructions_requiredToEnroll']}");
    customUiTextConfigBean.setEndIfMatches(true);

    System.out.println(GrouperUtil.jsonConvertTo(customUiTextConfigBean, false));
```

### JSON which is generated

```
{
   "variableToAssign":"cu_o365twoStepRequiredToEnroll",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod_policy",
   "label":"${textContainer.text['penn_o365twoStep_cu_o365twoStepRequiredToEnroll']}",
   "order":40
}
```

### Examples

Always show header, note, this is the default for "header"

```
{
   "customUiTextType":"header",
   "defaultText":true,
   "text":"${textContainer.text['penn_o365twoStep_header']}"
}
```

In [grouper.text.en.us](http://grouper.text.en.us).properties

```
penn_o365twoStep_header = <h1>PennO365 Two-Step Verification</h1>
```

For instructions1, if the operated on user is not required to enroll, if the are enrolled in o365/mfa, if they have a mailbox, and the are allowed to enroll/unenroll

```
{
   "endIfMatches":true,
   "customUiTextType":"instructions1",
   "index":20,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_instructions_willBeRequiredToEnroll']}",
   "script":"${ !cu_o365twoStepRequiredToEnroll && cu_o365twoStepEnrolled && cu_o365hasMailbox && cu_o365twoStepCanEnrollUnenroll}"
}

```

In [grouper.text.en.us](http://grouper.text.en.us).properties. Note the required date, calculated in a variable, is displayed on the screen

```
penn_o365twoStep_instructions_willBeRequiredToEnroll = To improve Penn's data security, you will be required to enroll in Two-Step Verification for O365 on <b>${cu_o365twoStepRequiredDate}</b>.<br /><br />
```

For instructions1, if the user operated on is not required to enroll, and not enrolled, and can enroll and unenroll, and is not enrolled in mfa

```
{
   "endIfMatches":true,
   "customUiTextType":"instructions1",
   "index":30,
   "defaultText":false,
   "text":"${textContainer.text['penn_o365twoStep_instructions_needsTwoStep']}",
   "script":"${!cu_o365twoStepRequiredToEnroll && !cu_o365twoStepEnrolled && cu_o365twoStepCanEnrollUnenroll && !cu_twoStepUsers}"
}
```

In [grouper.text.en.us](http://grouper.text.en.us).properties.

```
penn_o365twoStep_instructions_needsTwoStep = You are not enrolled in Two-Step Verification.  <a href="https://twostep.apps.upenn.edu/twoFactor/twoFactorUi/app/UiMain.index">Enroll now</a>.<br /><br />
```

Show the enroll button if not enrolled in o365/mfa, if can enroll/unenroll, if in mfa, if has a mailbox, show the button

```
{
   "endIfMatches":true,
   "customUiTextType":"enrollButtonShow",
   "index":0,
   "text":"${!cu_o365twoStepEnrolled && cu_o365twoStepCanEnrollUnenroll && cu_twoStepUsers && cu_o365hasMailbox}"
}
```

# User environment

You can show the variables, and results, and links to the grouper admin, the group manager (default), an arbitrary group, or the end user. This explains the access (if the user needs something, if the access is provisioned, etc)

Note, you can control who has access to the user environment table with the customUiTextType: canSeeUserEnvironment

# Text state

You can show another level of debugging by showing the text state. By default only grouper admins can see this but you can show it to an arbitrary group with the customUiTextType: canSeeScreenState. This explains why the decisions and text were made and shown the way they were.

This shows three things:

1. More columns in the user environment
2. The decisions for the current user
3. All text rules and how they are evaluated for the current user

# Assign variables

If you are a Grouper admin (by default), or are allowed since customUiTextType: canAssignVariables. You can send variables in the URL to simulate how the screen would look, to test all the rules

Someone to enroll  
https://grouper.server.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=61bcaad67d57438ab1fea11c426c2f64&cu_o365twoStepAllowedToManage=false&cu_grouperTurnOffManager=true&cu_o365twoStepInAzure=false&cu_o365twoStepInAzureError=false&cu_o365twoStepCanEnrollUnenroll=true&cu_o365twoStepSelfEnrolled=false&cu_o365twoStepEnrolled=false&cu_o365twoStepRequiredToEnroll=false&cu_twoStepUsers=true&cu_o365hasMailbox=true&cu_o365twoStepInLdap=false&cu_o365twoStepInLdapError=false&cu_o365twoStepRequiredDate=2020/05/01

Someone enrolled  
https://grouper.server.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=61bcaad67d57438ab1fea11c426c2f64&cu_o365twoStepAllowedToManage=false&cu_grouperTurnOffManager=true&cu_o365twoStepInAzure=true&cu_o365twoStepInAzureError=false&cu_o365twoStepCanEnrollUnenroll=true&cu_o365twoStepSelfEnrolled=true&cu_o365twoStepEnrolled=true&cu_o365twoStepRequiredToEnroll=false&cu_twoStepUsers=true&cu_o365hasMailbox=true&cu_o365twoStepInLdap=true&cu_o365twoStepInLdapError=false&cu_o365twoStepRequiredDate=2020/05/01

# Azure membership

Configure an azure connection in grouper.properties

```
grouper.azureConnector.myAzure.loginEndpoint = https://login.microsoftonline.com
grouper.azureConnector.myAzure.DirectoryID = 6c4dxxx0d
grouper.azureConnector.myAzure.client_id = fd805xxxxdfb
grouper.azureConnector.myAzure.client_secret = ******************
grouper.azureConnector.myAzure.resource = https://graph.microsoft.com
grouper.azureConnector.myAzure.graphEndpoint = https://graph.microsoft.com
grouper.azureConnector.myAzure.graphVersion = v1.0
grouper.azureConnector.myAzure.groupLookupAttribute = displayName
grouper.azureConnector.myAzure.groupLookupValueFormat = ${group.getName()}
grouper.azureConnector.myAzure.requireSubjectAttribute = PENNNAME
grouper.azureConnector.myAzure.subjectIdValueFormat = ${subject.getAttributeValue("PENNNAME")}@upenn.edu

```

Run a membership check

**See Also**  
[Customizing UI (security headers)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554533/Grouper+v2.5+customize+UI+security+headers)

[Grouper Custom Template via GSH](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544720/Grouper+custom+template+via+GSH)
