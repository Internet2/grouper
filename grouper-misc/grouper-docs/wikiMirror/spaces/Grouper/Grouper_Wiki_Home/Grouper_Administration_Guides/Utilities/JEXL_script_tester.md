---
title: "JEXL script tester"
space: Grouper
pageId: 28545070
version: 7
lastUpdated: 2026-07-01T05:47:47.445Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545070/JEXL+script+tester
---

In Grouper v4.0.3+ there is a UI utility to test JEXL scripts.

The point is to test JEXL scripts, and also to show various example in the places where JEXL scripts are used. If you have a good example of a JEXL script please submit it to the Grouper team to add to the screen

## Use

Go to Misc - > JEXL script tester

In order to use the JEXL script tester you need to be a Grouper admin and be in the group: etc:jexlScriptTestingGroup

## Warning

These JEXL scripts via GSH will run in the UI in the environment where the UI is running. Do not code destructive GSH / JEXL or bad things will happen.

## Select the type of script to run

## Select the example based on the type of script

## Customize 'Available beans'

The 'available beans' section of the screen will pre-populate beans that are in scope for the particular JEXL script. You can customize the data based on what you are testing (in this case, edit the group name)

## Edit the 'Null checking script'

The problem with JEXL is in strict mode, a misspelled variable name throws an exception, but a null value also throws an exception. So we are adding a sibling script next to each script in Grouper to check for nulls in non-strict mode. If the null check fails, then null will be used for the script and an error will not be thrown. We used to use the terniary operator for this but you should use a null checking script instead.

Here is an example in provisioning

Here is an example of the null checking script in the jexl script tester

## Edit the example JEXL script (or submit as is)

## See the result

Note the 'type' of data is printed before the string value. In this example the input is:

```
applications:departmentOfArtsAndSciences:commonResources:servicesOfSchool:policyGroups:groupsForActiveDirectory:historyDepartmentProfessors
```

The method called is: stringFormatNameReverseReplaceTruncate with length of 64 and use dots as separators. You see the result is it reversed with dots and length less than 64.

## GSH source

If you want to see the GSH source to run this yourself, it is printed below

## **Jexl advice**

### Jexl javabean property names

It is better to use the getter name than the property name. JEXL will throw an exception if there is a null and referring to it as property name.

For example, this will return null

```
    ProvisioningGroup provisioningGroup = new ProvisioningGroup();
    
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("provisioningGroup", provisioningGroup);
    
    String result = GrouperUtil.substituteExpressionLanguage("${provisioningGroup.getName()}", variableMap, true, false, false);
    
    System.out.println(result);

```

But this will throw an error

```
    ProvisioningGroup provisioningGroup = new ProvisioningGroup();
    
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("provisioningGroup", provisioningGroup);
    
    String result = GrouperUtil.substituteExpressionLanguage("${provisioningGroup.name}", variableMap, true, false, false);
    
    System.out.println(result);

```

## **See Also**

[Grouper ABAC Script Analysis](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548379/Grouper+ABAC+script+analysis)
