---
title: "Grouper attribute framework attributes editable in group edit screen"
space: Grouper
pageId: 28548837
version: 9
lastUpdated: 2026-07-01T05:43:23.471Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548837/Grouper+attribute+framework+attributes+editable+in+group+edit+screen
---

This is a feature in v2.6.8+ of the Grouper attribute framework that allows select attributes to be editable in the group edit screen (similar to the legacy Grouper "Admin UI"). The attributes are also viewable on the group screen under "Details". Attributes to show are configured in `grouper.properties` with the `groupScreen.attribute.*` keys.

## When to use this

- If the use case is at all provisioning related, consider using "[provisioning metadata](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555763/Grouper+provisioning+custom+metadata)" which is a very similar concept and experience
- If you need to use this for provisioning please contact the Grouper team about the use case

## Demo edit

## Demo view

## Applicable attributes

- Single assign marker attributes (no value) will be a checkbox
- Single assign single valued string attributes will be a textfield

> - The `attributeName` must be the correct full path of the attribute, or it will be quietly ignored (a warning is logged and the attribute is skipped — there is no error on the screen).
> - For a marker attribute that is not assigned to a group, the attribute does not show on the group "Details" view. It only shows on the group edit screen, as an unchecked checkbox.

## Security

This uses stock group / attributeDef security.

- If the user
  
  
  
  - can attributeRead on the group
  - can read on the attribute definition
  
  then they will see the settings in the group view "Details" section.
- If the user
  
  
  
  - can attributeRead and attributeUpdate on the group (and they need admin on the group to edit it)
  - can read and update on the attributeDef
  
  then they can edit the attributes in the group edit screen.

## Configure

Configure the attributes to show in `grouper.properties`:

```
######################################
## Group types edit view
## Identify marker attributes or single valued string attributes to be viewed or edited on group screen
## "theConfigId" is the config ID of the attribute
######################################

# show custom attributes defined below (v6+; defaults to true)
# {valueType: "boolean"}
# groupScreen.attribute.enabled = true

# attribute name that should be able to be seen on screen, e.g. a:b:c
# {valueType: "string"}
# groupScreen.attribute.theConfigId.attributeName =

# label on the left side of screen for attribute
# {valueType: "string"}
# groupScreen.attribute.theConfigId.label =

# description on the right side of screen for attribute
# {valueType: "string"}
# groupScreen.attribute.theConfigId.description =

# numeric index of the order of the attribute on the screen
# {valueType: "integer"}
# groupScreen.attribute.theConfigId.index = 
```

The `groupScreen.attribute.enabled` toggle (default `true`) is a master switch for whether the configured custom attributes are shown at all; set it to `false` to hide them without removing the per-attribute config. It is listed in the base properties file as of v6+. Setting it to `false` when the feature is unused may slightly improve group-screen performance by skipping the attribute lookups.

## Sample configuration

Below is a sample configuration to show attributes on the group screen, with `etc:attribute:myMfaAzure` as a marker attribute and `etc:attribute:myMfaAzureAttributeDate` as a string attribute. The attribute definitions and names must already exist; if you are not familiar with creating attributes, see the Grouper attribute framework documentation first.

```
# attribute name that should be able to be seen on screen, e.g. a:b:c
# {valueType: "string"}
groupScreen.attribute.azureMarker.attributeName = etc:attribute:myMfaAzure

# label on the left side of screen for attribute
# {valueType: "string"}
groupScreen.attribute.azureMarker.label = Azure require MFA:

# description on the right side of screen for attribute
# {valueType: "string"}
groupScreen.attribute.azureMarker.description = Check this box to require users in the group to have MFA required in Azure.  This rollout is based on org.  Users should have time to migrate and ensure their clients support MFA and do not get locked out.

# numeric index of the order of the attribute on the screen
# {valueType: "integer"}
groupScreen.attribute.azureMarker.index = 1

# attribute name that should be able to be seen on screen, e.g. a:b:c
# {valueType: "string"}
groupScreen.attribute.azureDate.attributeName = etc:attribute:myMfaAzureAttributeDate

# label on the left side of screen for attribute
# {valueType: "string"}
groupScreen.attribute.azureDate.label = Azure MFA date:

# description on the right side of screen for attribute
# {valueType: "string"}
groupScreen.attribute.azureDate.description = yyyy/mm/dd date of when users in this group will be required to use MFA in Azure.  The date format is required.

# numeric index of the order of the attribute on the screen
# {valueType: "integer"}
groupScreen.attribute.azureDate.index = 2

```

Now anyone with proper privileges can view the attributes on the group "Details" screen or edit them in the group edit screen.
