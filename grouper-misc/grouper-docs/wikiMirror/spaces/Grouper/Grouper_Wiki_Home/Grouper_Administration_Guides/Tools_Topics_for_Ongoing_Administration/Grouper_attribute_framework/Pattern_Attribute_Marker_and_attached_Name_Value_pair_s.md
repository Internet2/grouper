---
title: "Pattern: Attribute Marker and attached Name-Value pair(s)"
space: Grouper
pageId: 28548217
version: 4
lastUpdated: 2026-07-12T15:26:52.266Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548217/Pattern+Attribute+Marker+and+attached+Name-Value+pair+s
---

## Pattern: Attribute Marker and attached Name-Value Pair(s)

This is intended to introduce a pattern and how you can use it in the Grouper attribute framework to attache "meta data" to a Grouper object.

### Introduction

In this pattern a Grouper object ( Stem/Folder, Grouper, or Attribute Definition ) has an attributeName assigned to the object that marks the object. This attribute assignment to the object is the "Marker Attribute". Typically the Marker Attribute does not support having a value assigned to it, However that is not strictly required to fulfill this pattern. The name of the marker should reflect what the marker means. However a single name often can not fully convey the function of the marker. So to capture the rest of the metadata ( both name(s) and value(s) that are needed to finish the definition ) other attribute are assigned to the Marker Attribute. These attribute generally will have values ( one or more) assigned to each of them to convey the configuration/details of this specific Marker Attribute. It is allowable that some values may be optional. And in an even more advanced case, some attributeNames may be optional too.

#### A practical example.

Often as you design your own Attributes Definitions (and Attribute Names) you will need a set of values to complete your development goals. The [Grouper auto assign relevant attributes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547690/Grouper+auto+assign+relevant+attributes) hook uses a Marker Attribute ( "grouperAttributeAutoCreateMarker" ) assigned to Attribute Definitions to find configurations for how it should do it's work. The hook then uses the values in two name-value pair attributes assigned to the marker called "grouperAttributeAutoCreateIfName" and "grouperAttributeAutoCreateThenNamesOnAssign" to instruct it about how to assign attributes in the "attribute marker" when a user assigns the Marker to another object. Basically the hook helps the users by auto assigning the Name-Value pairs anytime the user assigns an Attribute Marker in Grouper.

The Hook watches for any attribute assignment in the system. It checks to see if the attribute that was just assigned is identified as a Marker Attributes in a configuration ( AKA: a "grouperAttributeAutoCreateIfName" value) on an Attribute Definition. If the attribute is a Marker Attribute then the hook uses the matching "grouperAttributeAutoCreateThenNamesOnAssign" value as a comma separated value list of attributeNames that should be auto assigned to the marker attribute that the user just assigned to the Grouper object..  
  
In short, the user assigns the "Marker Attribute" and the [Grouper auto assign relevant attributes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547690/Grouper+auto+assign+relevant+attributes) hook adds a configured set of attributes to that Marker for the user. Effectively saving: time, mouse clicks and freeing the user from needing to understand what other attributes need to be assigned to the Marker Attribute.  
  
A more advanced example is [Grouper Rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548037/Grouper+rules+patterns). All the rules follow this pattern. In this case the Marker attribute is "rule" ( etc:attribute:rules:rule ) and it has a set of other attributeNames assigned to it to implement what the rule should do in that implementation.  
  
  
It should be noted that the [Grouper auto assign relevant attributes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547690/Grouper+auto+assign+relevant+attributes) hook still requires the user to supply the needed value(s) to the Name-Value pair(s). And the base UI for attribute assignment is still a bit primitive. Some attribute values assignments are being moved to specialized UI tools to help the user better understand what is expected of them. So using this is still expected to be helpful for advanced Grouper users.
