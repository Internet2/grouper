---
title: "Grouper rules pattern - Assign attribute to group"
space: Grouper
pageId: 28555190
version: 13
lastUpdated: 2026-11-23T07:26:56.718Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555190/Grouper+rules+pattern+-+Assign+attribute+to+group
---

*This is here for historical purposes.*

If a group is created somewhere in an "etc" or "ref" folder, add the do_not_provision_to attribute with values of "ad" and "ldap"

Note, make sure you are not using "smart quotes or dashes". You can disable these in a Mac in Preferences->keyboard-text.

## Setup

v7.10.0+, v6.6.0+ and v4.27.0+: set `rules.allowUnrestrictedEl = true` in grouper.properties instead of the group below, which is not used (see [Grouper rules EL variables](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549057/Grouper+rules+EL+variables)). In v7.9.1, v6.5.1 and v4.26.1 this rule does not work, since rule EL runs in a restricted sandbox.

Earlier versions: you need GrouperSystem to be able to access the API in rules:

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
