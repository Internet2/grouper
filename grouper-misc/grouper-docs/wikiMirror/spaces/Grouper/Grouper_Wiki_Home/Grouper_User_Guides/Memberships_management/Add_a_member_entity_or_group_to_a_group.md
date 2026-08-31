---
title: "Add a member (entity or group) to a group"
space: Grouper
pageId: 28544779
version: 16
lastUpdated: 2024-05-30T13:41:41.904Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544779/Add+a+member+entity+or+group+to+a+group
---

## Summary

You can add a user, service principal, or group to be the member of a group. If a group is a member of another group, then all the members of the member group are effective members of the parent group. The thing that is added to the group is referred to as an entity, subject, or member. Note: a service principal could be a credential that a web service user uses. It could be a local entity in Grouper or something represented in an institutional subject source.

## Privilege requirements

If a UI user has the appropriate privileges on a group, they can add a member to the group.

To add or remove a member from a group, the user needs UPDATE/READ or ADMIN on the group. Note these privileges could be effectively assigned due to the user's role in an application folder or org folder. Sysadmins can manage members in any group.

## Find the group

You can find a Group in Grouper in various ways: searching, navigating, favorites, home screen panels, bookmark, etc

## View group screen

## Click 'Add members'

Click the 'Add members' button to show the add member panel

## Search for a member to add with the search combobox

Note there are icons to distinguish between users, groups,

## Alternatively you can use the search panel

Exact ID match will search by the subject ID or identifier, which is typically your employee ID, net ID, EPPN, etc. The data sources can help filter by group, person, application, etc. Select the user, then click the Add button to add the member to the group

## Select the member from results and optionally add start and end dates

## See the success

Optionally:

1. Click “import a list of members”
2. In the “Import members” page you are presented with a number of options:   
    
  To add a list of people for which you have usernames for click “Copy/paste a list of member IDs” and paste in the list of users you wish to add (clicking “validate entities” will have Grouper check to make sure they are accessible to it)  
    
  Once satisfied click “Submit” at the bottom of the screen  
    
  The next page is a report of how the import process went, clicking OK will allow you to proceed
3. You can validate that the user was successfully added to the group in the next page
