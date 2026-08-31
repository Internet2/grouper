---
key: GRP-88
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-88
type: New Feature
status: Resolved
resolution: Fixed
priority: Minor
reporter: Chris Hyzer <mchyzer@example.com>
assignee: Chris Hyzer <mchyzer@example.com>
created: 2008-02-08T07:36:06.199+0000
updated: 2008-02-08T07:36:30.993+0000
resolved: 2008-02-08T07:36:30.994+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-88  add saveGroup method to the Group class, remove warnings, improve exceptions in Stem class

Note: this is unit tested and all unit tests pass.  Gary is concerned about the createStemsIfNotExist flag, perhaps we can discuss in next conference call.  Thanks, Chris

Group edu.internet2.middleware.grouper.Group.saveGroup(GrouperSession grouperSession, String description, String displayExtension, String name, String uuid, boolean retrieveViaNameIfNoUuid, boolean createGroupIfNotExist, boolean createStemsIfNotExist) throws StemNotFoundException, GroupNotFoundException, GroupAddException, InsufficientPrivilegeException, GroupModifyException, StemAddException

 create or update a group.
 
 This is a static method since setters to Group objects persist to the DB
 
 Steps:
 
 1. Find the group
  a. First look by uuid.  if no uuid, or uuid is not found, then its an insert.
  b. If the uuid is not found, and retrieveViaNameIfNoUuid, then lookup the group
  by name.  If exists, update, else insert
 2. Internally set all the fields of the group (no need to reset if already the same)
 3. Store the group (insert or update) if needed
 4. Return the group object
 
 TODO figure out what "types" are and if they should be passed in
 TODO do attributes need to be passed in or are they already taken care of?
 TODO I assume the create and modify should just happen with the insert/update...
 
Parameters:
grouperSession to act as
description if blank, ignore
displayExtension display friendly name for this group only (parent stems are not specified)
name this is required, and is the full name of the group including the names of parent stems. e.g. stem1:stem2:group1 the parent stem must exist
uuid of the group. If a group exists with this uuid, then it will be updated, if not, then it will be created if createIfNotExist is true
retrieveViaNameIfNoUuid
createGroupIfNotExist If not exist, and false, then GroupNotFoundException
createStemsIfNotExist true if the stems should be created if they dont exist, false for StemNotFoundException if not exist. Note, the display extension on created stems will equal the extension
Returns:
the group that was updated or created
Throws:
StemNotFoundException
GroupNotFoundException
GroupAddException
InsufficientPrivilegeException
GroupModifyException
StemAddException

## Comments

### mchyzer - 2008-02-08T07:36:30.990+0000

done