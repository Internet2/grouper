---
title: "Grouper rules pattern - Assign attribute to group"
space: Grouper
pageId: 28555190
version: 11
lastUpdated: 2024-02-21T06:12:04.743Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555190/Grouper+rules+pattern+-+Assign+attribute+to+group
---

*This is here for historical purposes.*

If a group is created somewhere in an "etc" or "ref" folder, add the do_not_provision_to attribute with values of "ad" and "ldap"

Note, make sure you are not using "smart quotes or dashes". You can disable these in a Mac in Preferences->keyboard-text.

## Setup

First off you need GrouperSystem to be able to access the API in rules:

grouper.properties:

```
# configure some group
rules.accessToApiInEl.group = etc:hasAccessToApiInEl
```

put GrouperSysAdmin in that group:

## Example1: UI example of a multi-assign single-valued attribute (this IS what PSPNG does)

Create a rule on some ancestor folder:

Text: note: there are two sets of curlies because if you have multiple statements you need to put them in another set of curlies

```
rule
ruleActAsSubjectId       GrouperSystem 
ruleActAsSubjectSourceId g:isa 
ruleCheckStemScope       SUB 
ruleCheckType            groupCreate 
ruleIfConditionEl        ${groupName.contains(":etc:") || groupName.contains(":ref:")} 
ruleThenEl               ${ { group.getAttributeValueDelegate().assignValueString("etc:pspng:do_not_provision_to", "ldap"); group.getAttributeDelegate().addAttributeByName("etc:pspng:do_not_provision_to").getAttributeAssign().getValueDelegate().assignValueString("ad"); }} 
ruleValid                T 
```

If I create a group in that parent folder in an "etc" or a "ref" folder, it will get some attributes

## Example2: UI example of a single-assign multi-valued attribute (this is NOT what PSPNG does)

Create a rule on some ancestor folder:

Text: note: there are two sets of curlies because if you have multiple statements you need to put them in another set of curlies

```
rule
ruleActAsSubjectId       GrouperSystem 
ruleActAsSubjectSourceId g:isa 
ruleCheckStemScope       SUB 
ruleCheckType            groupCreate 
ruleIfConditionEl        ${groupName.contains(":etc:") || groupName.contains(":ref:")} 
ruleThenEl               ${ { group.getAttributeValueDelegate().addValueString("etc:pspng:do_not_provision_to", "ldap"); group.getAttributeValueDelegate().addValueString("etc:pspng:do_not_provision_to", "ad"); } } 
ruleValid                T 
```

If I create a group in that parent folder in an "etc" or a "ref" folder, it will get some attributes
