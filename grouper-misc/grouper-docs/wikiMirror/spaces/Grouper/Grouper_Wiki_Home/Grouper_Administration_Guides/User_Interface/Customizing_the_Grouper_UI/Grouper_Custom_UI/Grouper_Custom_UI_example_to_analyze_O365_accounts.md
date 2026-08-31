---
title: "Grouper Custom UI example to analyze O365 accounts"
space: Grouper
pageId: 28554652
version: 5
lastUpdated: 2026-07-01T05:39:55.865Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554652/Grouper+Custom+UI+example+to+analyze+O365+accounts
---

Note: The Azure user query is available in 2.5.30+

The requirements for this screen are:

1. Admins will use this screen to look at users and their O365 attributes and state
2. Show PennGroups which affect if and how the account is provisioned
3. Show sync status and state in Azure

## Screenshot

## Configuration

grouper.properties

```
############################################
## Azure connector 
## specify the azure connection with user, pass, endpoint etc
## the string after "db." is the name of the connection, and it should not have
## spaces or other special chars in it
############################################

# login endpoint to get a token
# {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.loginEndpoint$"}
# grouper.azureConnector.myAzure.loginEndpoint = https://login.microsoftonline.com

# azure directory id
# {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.DirectoryID$"}
# grouper.azureConnector.myAzure.DirectoryID = 6c4dxxx0d

# client id
# {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.client_id$"}
# grouper.azureConnector.myAzure.client_id = fd805xxxxdfb

# client secret
# {valueType: "password", sensitive: true, regex: "^db\\.([^.]+)\\.client_secret$"}
#grouper.azureConnector.myAzure.client_secret = ******************

# resource.  generally same as graph endpoint
# {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.resource$"}
# grouper.azureConnector.myAzure.resource = https://graph.microsoft.com

# graph endpoint
# {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.graphEndpoint$"}
# grouper.azureConnector.myAzure.graphEndpoint = https://graph.microsoft.com

# graph version
# {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.graphVersion$"}
# grouper.azureConnector.myAzure.graphVersion = v1.0

# group lookup attribute
# {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.groupLookupAttribute$"}
# grouper.azureConnector.myAzure.groupLookupAttribute = displayName

```

## JSON configs to configure the variables and text

These attributes are on the "custom UI" group and look like this

## Page text "do not show enroll button"

This is a customUiTextConfigBeans attribute value, assigned on the assignment of the marker attribute customUi on the group. This JSON is formatted, but in the attribute value is all in one line

endIfMatches is not important since there are not multiple configurations for "enrollButtonShow". Index doesnt matter either. The value is "false"

```
{
  "endIfMatches":true,
  "customUiTextType":"enrollButtonShow",
  "index":0,
  "text":"false"
}
```

## Page text "header"

This is a customUiTextConfigBeans attribute value, assigned on the assignment of the marker attribute customUi on the group. This JSON is formatted, but in the attribute value is all in one line

endIfMatches, index and defaultText are not important since there are not multiple configurations for "header". The value is externalized from key "penn_o365_header"

```
{
  "endIfMatches":true,
  "customUiTextType":"header",
  "index":10,
  "defaultText":true,
  "text":"${textContainer.text['penn_o365_header']}"
}
```

[grouper.text.en.us](http://grouper.text.en.us).properties

```
penn_o365_header = <h1>PennO365 enrollment analysis</h1>
```

## Page text "manager instructions"

This is a customUiTextConfigBeans attribute value, assigned on the assignment of the marker attribute customUi on the group. This JSON is formatted, but in the attribute value is all in one line

endIfMatches, index and defaultText are not important since there are not multiple configurations for "managerInstructions". The value is externalized from key "penn_o365_managerInstructions"

```
{
  "endIfMatches":true,
  "customUiTextType":"managerInstructions",
  "index":10,
  "defaultText":true,
  "text":"${textContainer.text['penn_o365_managerInstructions']}"
}
```

[grouper.text.en.us](http://grouper.text.en.us).properties

```
penn_o365_managerInstructions = Search for a person to analyze their O365 status
```

## Page text "enrollment label"

This is a customUiTextConfigBeans attribute value, assigned on the assignment of the marker attribute customUi on the group. This JSON is formatted, but in the attribute value is all in one line

endIfMatches, index and defaultText are not important since there are not multiple configurations for "managerInstructions". The value is externalized from key "penn_o365_helpText"

```
{
  "endIfMatches":true,
  "customUiTextType":"enrollmentLabel",
  "index":10,
  "defaultText":true,
  "text":"${textContainer.text['penn_o365_helpText']}"
}
```

[grouper.text.en.us](http://grouper.text.en.us).properties

```
penn_o365_helpText = The table below shows the state of the user with regards to PennO365.
```

## Variable for "Person" (SQL example)

This is a customUiUserQueryConfigBeans attribute value, assigned on the assignment of the marker attribute customUi on the group. Since we are not using the variables to show dynamic text, the variableToAssign is not important. This JSON is formatted below, but in the attribute value is all in one line

The order of 5 is less than other query orders so this is first.

This is a sql query, using the subject id, to look up a person description. The database config id in grouper-loader.properties is "pennCommunity".

The label is externalized text

```
{ 
  "variableToAssign":"cu_o365personDescription", 
  "bindVar0":"${subject.id}", 
  "userQueryType":"sql", 
  "variableType":"string", 
  "configId":"pennCommunity", 
  "bindVar0type":"string", 
  "query":"select description from PCDADMIN.computed_person cp where cp.char_PENN_ID = ?", 
  "label":"${textContainer.text['penn_o365_cu_o365personDescription']}", 
  "order":5
}
```

[grouper.text.en.us](http://grouper.text.en.us).properties

```
penn_o365_cu_o365personDescription = Person
```

Note the other SQL queries are similar

## Variable for "Person" (Azure web service example)

This is a customUiUserQueryConfigBeans attribute value, assigned on the assignment of the marker attribute customUi on the group. Since we are not using the variables to show dynamic text, the variableToAssign and variableToAssignOnError are not important. This JSON is formatted below, but in the attribute value is all in one line

The order of 20 is the order on the screen which is third (two have a lower order number)

This will find the "pennAzure" configuration in the grouper.properties file and run a query using the subject which is being analyzed. The Azure query sets a bunch of variables that can be used in the "script" as shown below. This expression sees if the user was found in Azure, if the O365 account is enabled, if exchange is in the assigned and enabled plans, and if exchange is in the provisioned plans. If all those are true then the user has exchange.

The error handling is just to see if Grouper can connect to Azure and run the query successfully.

The object model for the Azure user object is as follows (note, [here are some microsoft docs](https://docs.microsoft.com/en-us/graph/api/resources/user?view=graph-rest-1.0) about these variables, and not all are returned, just these)

| Variable | Example | Description |
| --- | --- | --- |
| userFound | true | If user found in Azure |
| accountEnabled | true |  |
| assignedPlans | AADPremiumService, Adallom, Deskless, EduAnalytics, Homeroom, MicrosoftCommunicationsOnline, MicrosoftKaizala, MicrosoftOffice, MicrosoftStream, OfficeForms, PowerAppsService, ProcessSimple, ProjectWorkManagement, RMSOnline, SharePoint, Sway, TeamspaceAPI, To-Do, WhiteboardServices, YammerEnterprise, exchange | This is a "Set" object that can do simple operations |
| assignedPlansString | AADPremiumService, Adallom, Deskless, EduAnalytics, Homeroom, MicrosoftCommunicationsOnline, MicrosoftKaizala, MicrosoftOffice, MicrosoftStream, OfficeForms, PowerAppsService, ProcessSimple, ProjectWorkManagement, RMSOnline, SharePoint, Sway, TeamspaceAPI, To-Do, WhiteboardServices, YammerEnterprise, exchange | This is a string that is easy for printing |
| mail | [mchyzer@isc.upenn.edu](mailto:mchyzer@isc.upenn.edu) |  |
| onPremisesImmutableId | 10021368 | Maybe this is your subject ID |
| onPremisesLastSyncDateTime | 2020-04-03T15:52:41Z |  |
| onPremisesSamAccountName | mchyzer |  |
| proxyAddresses | mchyzer@[PennO365.mail.onmicrosoft.com](http://PennO365.mail.onmicrosoft.com), [mchyzer@PennO365.onmicrosoft.com](mailto:mchyzer@PennO365.onmicrosoft.com), [mchyzer@isc.upenn.edu](mailto:mchyzer@isc.upenn.edu), [mchyzer@nursing.upenn.edu](mailto:mchyzer@nursing.upenn.edu), [mchyzer@pobox.upenn.edu](mailto:mchyzer@pobox.upenn.edu), [mchyzer@upenn.edu](mailto:mchyzer@upenn.edu), [mchyzer@wharton.upenn.edu](mailto:mchyzer@wharton.upenn.edu) | This is a "Set" object that can do simple operations |
| proxyAddressesString | [mchyzer@PennO365.mail.onmicrosoft.com](mailto:mchyzer@PennO365.mail.onmicrosoft.com), [mchyzer@PennO365.onmicrosoft.com](mailto:mchyzer@PennO365.onmicrosoft.com), [mchyzer@isc.upenn.edu](mailto:mchyzer@isc.upenn.edu), [mchyzer@nursing.upenn.edu](mailto:mchyzer@nursing.upenn.edu), [mchyzer@pobox.upenn.edu](mailto:mchyzer@pobox.upenn.edu), [mchyzer@upenn.edu](mailto:mchyzer@upenn.edu), [mchyzer@wharton.upenn.edu](mailto:mchyzer@wharton.upenn.edu) | This is a string that is easy for printing |
| showInAddressList | true |  |
| userPrincipalName | [mchyzer@upenn.edu](mailto:mchyzer@upenn.edu) |  |
| userType | Member |  |
| provisionedPlans | EXCHANGE, MicrosoftCommunicationsOnline, MicrosoftOffice, SharePoint | This is a "Set" object that can do simple operations |
| provisionedPlansString | EXCHANGE, MicrosoftCommunicationsOnline, MicrosoftOffice, SharePoint | This is a string that is easy for printing |
| summary | accountEnabled: true, mail: mchyzer@isc.upenn.edu, onPremisesImmutableId: 10021368, onPremisesLastSyncDateTime: 2019-12-23T22:46:09Z, onPremisesSamAccountName: mchyzer, ... | Print out all Azure variables in one string |

The label is externalized text

```
{ 
  "variableToAssign":"cu_azureExchangeAccount", 
  "userQueryType":"azure", 
  "variableType":"boolean", 
  "configId":"pennAzure", 
  "script":"${userFound && accountEnabled && grouperUtil.collectionContains(assignedPlans, 'exchange') && grouperUtil.collectionContains(provisionedPlans, 'EXCHANGE')}",
  "label":"${textContainer.text['penn_o365_cu_azureExchangeAccount']}", 
  "errorLabel":"${textContainer.text['penn_o365_cu_azureExchangeAccountError']}", 
  "order":20, 
  "variableToAssignOnError":"cu_azureExchangeAccountError"
}
```

[grouper.text.en.us](http://grouper.text.en.us).properties

```
penn_o365_cu_azureExchangeAccount = Has exchange account
penn_o365_cu_azureExchangeAccountError = Error finding exchange account
```

Note the "summary" Azure query is similar. Also note that only one Azure call is made per page view no matter how many variables are configured to use Azure.

## Variable for "Two-step in O365" (Grouper group example)

This is a customUiUserQueryConfigBeans attribute value, assigned on the assignment of the marker attribute customUi on the group. Since we are not using the variables to show dynamic text, the variableToAssign is not important. This JSON is formatted below, but in the attribute value is all in one line

The order of 77 brings this query toward the bottom of the table

This is a sql query, using the subject id, to look up a person description. The database config id in grouper-loader.properties is "pennCommunity".

The label is externalized text

```
{
  "variableToAssign":"cu_o365twoStepEnrolled",
  "fieldNames":"members",
  "userQueryType":"grouper",
  "variableType":"boolean",
  "groupName":"penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod",
  "label":"${textContainer.text['penn_o365twoStep_cu_o365twoStepEnrolled']}",
  "order":77
}
```

[grouper.text.en.us](http://grouper.text.en.us).properties

```
penn_o365twoStep_cu_o365twoStepEnrolled = In PennGroup for Two-Step Verification for PennO365 (required or self-enrolled)
```

Note the other Grouper queries are similar
