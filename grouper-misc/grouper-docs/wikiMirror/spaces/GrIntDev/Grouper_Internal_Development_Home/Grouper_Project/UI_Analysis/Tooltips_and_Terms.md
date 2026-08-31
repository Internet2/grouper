---
title: "Tooltips and Terms"
space: GrIntDev
pageId: 48824700
version: 4
lastUpdated: 2026-07-12T07:02:49.291Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48824700/Tooltips+and+Terms
---

## Tooltips

 

#### Use

 To make a tooltip, just add this onmouseover to any HTML element (if none there, for example, add a span):

 
```

<h1 onmouseover="grouperTooltip('tooltip text');">hey there</h1>

```

 This uses the [Walter Zorn open source LGPL tooltips](http://www.walterzorn.com/tooltip/tooltip_e.htm), though everything goes through a Grouper javascript function, so we could easily swap this out if ever needed. The html title attribute is not as user friendly, it truncates long text, cannot format html, etc. There are a lot of configurations with Walter Zorn tooltips available

 See the [screenshot example](https://wiki.internet2.edu/confluence/download/attachments/20139/tooltips.gif)

 

## Terms Using Tooltips

 

#### Features

 

1. Terms and tooltip text configurable with properties file
2. Terms and tooltip activation occurs automatically in the <grouper:message custom tag
3. Terms can be disabled in the grouper.properties
4. Multiple terms can link to the same tooltip
5. Only the first occurrence of the term is substituted per page so that the page is not inundated with tooltips
6. Terms have a visual cue (current suggestion is a blue dashed underline)
7. You can have a button to enable/disable tooltips in the user's session (we could adjust the timespan since it is cookie based)
8. Tooltips are entered with special syntax in the nav.properties (or local or locale specific)

 

#### Keep in mind

 

- The terms are case-sensitive
- There are no word boundaries, though see "targetted tooltips" below, (e.g. "Group" will find a match in "Grouper" and the "Group" part will be underlined)
- Terms are processed in order, so nothing that is a substring of another should be placed above it
- The syntax of the nav.properties is as follows:

 
```

term.the.hierarchy=the hierarchy
tooltip.the.hierarchy=A hierarchy is where each node has zero or one parents and zero or many children

term.groups=groups
term.groups.1=Groups
term.groups.2=GROUPS
term.groups.3=group
tooltip.groups=Groups are many people or groups

term.showing=Showing
tooltip.showing=To display or show

```

 - Each term must start with "term."  
 - The term variable is the suffix after "term."  
 - Each tooltip must start with "tooltip." and the suffix after that must match the term suffix  
 - If there are multiple terms for one tooltip, they must have unique keys, so suffix the whole thing with ".1", ".2", etc  
 - There can be multiple terms for on tooltip, but there can be only one tooltip per term variable

 

- You can programmatically disable a tooltip like this:

 
```

<grouper:message bundle="${nav}" key="${title}" tooltipDisable="true" />

```

 

- To have an enable/disable button, do something like this:

 
```

<a href="https://wiki.internet2.edu/confluence/pages/editpage.action#"               onclick="toggleTooltips('<grouper:message bundle="${nav}" key="groups.tooltips.disable" tooltipDisable="true"/>',
'<grouper:message bundle="${nav}" key="groups.tooltips.enable" tooltipDisable="true"/>'); return false;" id="tooltipToggleLink"></a>
<script type="text/javascript">
  writeTooltipText('<grouper:message bundle="${nav}" key="groups.tooltips.disable" tooltipDisable="true"/>',
'<grouper:message bundle="${nav}" key="groups.tooltips.enable" tooltipDisable="true"/>');
</script>

```

 

## Targetted tooltips

 If you have a situation where you have a label or a button, and you want the whole thing to be a tooltip, and especially if it is only one word and you dont want it global, you can use targetted tooltips. This is a tooltip that is targetted at the use of another message in the nav.properties. You can also re-use the tooltips with a targettedRef. This is a reference to another message. Here is an example. In the nav.properties, here was the config:

 
```

priv.view=view
priv.read=read
priv.update=update
priv.admin=admin
priv.CREATE=Create Group - entity can create groups in this folder.
priv.STEM=Create Folder - entity can create subfolders in this folder and assign 'Create Folder' and 'Create Group' privileges
priv.MEMBER=member - a member of this group
priv.OPTIN=optin - Entity can choose to join this group
priv.OPTOUT=optout - Entity can choose to leave this group
priv.VIEW=view - Entity can see that this group exists
priv.READ=read - Entity can see the membership list for this group
priv.UPDATE=update - Entity can modify the membership of this group
priv.ADMIN=admin - Entity can modify group attributes, delete this group or assign any privilege to any entity

```

 This rendered this screen

 

 Now we change the text so it is a targetted tooltip. This means that not everywhere that "member" shows up is it substituted, only where these messages are displayed.

 
```

priv.read=read
priv.update=update
priv.admin=admin

tooltipTargettedRef.priv.create=tooltipTargetted.priv.CREATE
tooltipTargettedRef.priv.stem=tooltipTargetted.priv.STEM
tooltipTargettedRef.priv.member=tooltipTargetted.priv.MEMBER
tooltipTargettedRef.priv.optin=tooltipTargetted.priv.OPTIN
tooltipTargettedRef.priv.optout=tooltipTargetted.priv.OPTOUT
tooltipTargettedRef.priv.view=tooltipTargetted.priv.VIEW
tooltipTargettedRef.priv.read=tooltipTargetted.priv.READ
tooltipTargettedRef.priv.update=tooltipTargetted.priv.UPDATE
tooltipTargettedRef.priv.admin=tooltipTargetted.priv.ADMIN

priv.CREATE=Create Group
priv.STEM=Create Folder
priv.MEMBER=member
priv.OPTIN=optin
priv.OPTOUT=optout
priv.VIEW=view
priv.READ=read
priv.UPDATE=update
priv.ADMIN=admin

tooltipTargetted.priv.CREATE=Entity can create groups in this folder (but not subfolders).
tooltipTargetted.priv.STEM=Entity can create subfolders in this folder and assign 'Create Folder' and 'Create Group' privileges
tooltipTargetted.priv.MEMBER=Entity is a member of this group
tooltipTargetted.priv.OPTIN=Entity can choose to join this group

```

 This results in the following result which is less cluttered but still informative for newbies.

 

 

## To do's / issues

 

1. Decide where to use and where not to use tooltips (e.g. in titles?). Dont use generic words. i.e. dont have a non-targetted tooltip which is based on the word "group"
2. If an enclosing link has a title, mark the message to ignore tooltips since they overlap. Or just dont replace with grouper.message
