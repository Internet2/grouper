---
title: "Grouper GSH new commands"
space: GrIntDev
pageId: 48792711
version: 37
lastUpdated: 2026-07-12T17:02:40.143Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792711/Grouper+GSH+new+commands
---

## Overview

[GSH templates](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544720/Grouper+custom+template+via+GSH) will require people to write [GSH](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh). Currently when considering Grouper's GSH "api", we have "built in" commands, e.g. getGroups(name). These have downsides (they can't get enhanced inputs without overloading the method). Those will be maintained going forward but are deprecated for new scripts.

We have been making more modern "method chained" classes to do Grouper primitives (e.g. create group), but the [GSH documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh) needs to be updated. So we will take this opportunity (as of March 2021) to build out, document, and test the new recommended GSH API operations for Grouper. Here is a first pass at a doc that lists the tasks with priority.

For finders (retrieve) and savers (insert/update/delete):

See also [GrouperShell (gsh) Group insert / update / delete](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547475/GrouperShell+gsh+Group+insert+update+delete+GroupSave)

- Method chaining (allows arbitrary arguments, eliminates method overloading/deprecation)
  
  
  ```
  Group group = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).assign....().save();
  ```
  
  Note the "GroupSave" object could have other results (e.g. if something was actually changed in the database or if it was already correct)
- Secure methods, or option to not be secure (i.e. if secure sees if current Grouper session has ability to make change or query)
  
  - It is not necessary to pass in a GrouperSession
- Replace all settings: keeps track of what methods were called and can only edit what was changed (default true)
  
  
  ```
  // If the group exists and has a description, it will be removed
  Group group = new GroupSave().assignName("a:b:c").save();
  
  // If the group exists and has a description, it will not be changed
  Group group = new GroupSave().assignName("a:b:c").assignReplaceAllSettings(false).save();
  
  
  ```

Example script using method chaining (note this is not a standalone script, some variables are pre-defined)

```
  // Assign privs for admins on folder
  new PrivilegeGroupInheritanceSave().assignStem(workingGroupFolder).addPrivilegeName("admin").assignSubject(adminsGroup.toSubject()).save();
  
  // Assign attestation on working group folder
  new AttestationStemSave().assignStemName(workingGroupFolderName).assignDaysUntilRecertify(90).save();
   
  // sympa folder
  if (gsh_input_isSympa) {
    Stem sympaFolder = new StemSave().assignName(sympaFolderName).assignDisplayExtension(displayExtension + " sympa")
      .assignDescription("Folder email lists for working group.  " + gsh_input_workingGroupDescription).save();
  
    // Create admins group
    Group ownersGroup = new GroupSave().assignName(sympaFolderName + ":owners")
      .assignDisplayExtension(displayExtension + " sympa owners")
      .assignDescription("Owners list manages the email list for the working group.  " + gsh_input_workingGroupDescription).save();
  
    // Create subscribers group
    Group subscribersGroup = new GroupSave().assignName(sympaFolderName + ":subscribers").assignDisplayExtension(displayExtension + " sympa subscribers")
      .assignDescription("Subscribers list receives working group emails.  " + gsh_input_workingGroupDescription).save();
  
    // Add admin group to owners group
    new MembershipSave().assignGroup(ownersGroup).assignSubject(adminsGroup.toSubject()).save();

```

## Tasks

These are tasks in order of priority

Document means

- Make a wiki doc [like this](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547475/GrouperShell+gsh+Group+insert+update+delete+GroupSave) 
  
  - Grouper version
  - legacy calls at bottom
  - each method described
- Remove from [existing doc](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh)

| Task or GSH operation | Implement | Document | Tested | TO DO | Replace all settings? | Developer |
| --- | --- | --- | --- | --- | --- | --- |
| GrouperSession | Done | Done | Done |  | N | Chris |
| GroupFinder | Done | Done | Done | Allow null GrouperSession (if null just get from static) | N | Vivek |
| StemFinder | Done | Done | Done | Allow null GrouperSession (if null just get from static) | N |  |
| StemSave | Done | Done | Done |  | Y | Vivek |
| AttributeAssignToStemSave | Done | Done | Done | Add method to take into account if adding attribute assignment (default to no) | Maybe later | Vivek |
| AttributeAssignToGroupSave | Done | Done | Done | Also move "group type" GSH documentation to this new wiki as legacy | Maybe later | Vivek |
| AttributeAssignToAssignmentSave | Done | Done | Done |  | Maybe later | Vivek |
| AttributeAssignValueSave | Done | Done | Done | Manage adding or assigning values to an AttributeAssignment | Maybe later | Vivek |
| AttestationStemSave | Done | Done | Done | Write tests and documentation | Y | Vivek |
| AttestationGroupSave | Done | Done | Done |  | Y | Vivek |
| GrouperPasswordSave | Done | Done | Done |  | N |  |
| AttributeDefSave | Done | Done | Done |  | Y | Vivek |
| AttributeDefNameSave | Done | Done | Done |  | Y | Vivek |
| PrivilegeGroupInheritanceSave | Done | Done | Done |  | N | Vivek |
| PrivilegeStemInheritanceSave | Done | Done | Done |  | N | Vivek |
| PrivilegeAttributeDefInheritanceSave | Done | Done | Done |  | N | Vivek |
| GdgTypeGroupSave | Done | Done | Done | Change UI to call this (similar to attestation stem save?) Done | Maybe later | Vivek |
| GdgTypeStemSave | Done | Done | Done | Change UI to call this (similar to attestation stem save?) Done | Maybe later | Vivek |
| GdgTypeGroupFinder | Done | Done | Done | Change UI to call this Done | N | Vivek |
| GdgTypeStemFinder | Done | Done | Done | Change UI to call this Done | N | Vivek |
| CompositeSave | Done | Done | TODO |  | N |  |
| CompositeFinder | Done | Done | TODO | add a way to find one specific composite | N |  |
| MembershipSave | Done | Done | TODO |  | Maybe later |  |
| MembershipFinder | Done | Done | TODO |  | N |  |
| MemberFinder | Done | Done | TODO |  | N |  |
| SubjectFinder | Done | Done | TODO |  | N |  |
| StemCopy | Done | Done | TODO |  | N |  |
| GroupCopy | Done | Done | TODO |  | N |  |
| GrouperEmail | Done | Done | Done |  | N | Chris |
| GcDbAccess | Done | Done | Done | Arbitrary SQL calls with bind vars | N |  |
| LdapSessionUtils | Done | Done | Done | Arbitrary LDAP calls | N |  |
| ProvisionableStemSave | Done | Done | Done | Also use this from UI? Coordinate with Shilen? | Maybe later | Vivek |
| ProvisionableGroupSave | Done | Done | Done | Also use this from UI? Coordinate with Shilen? | Maybe later | Vivek |
| ProvisionableStemFinder | Done | Done | Done | Also use this from UI? Coordinate with Shilen? | Maybe later | Vivek |
| ProvisionableGroupFinder | Done | Done | Done | Also use this from UI? Coordinate with Shilen? | Maybe later | Vivek |
| GshTemplateExec | Done | Done | Done | Add easier ways to add input values (without needing to instantiate GshTemplateInput object?) | N |  |
| Create script from SQL | NA | TODO | NA | Just move docs from GSH page to new page | N |  |
| Messaging | NA | TODO | NA | Just move docs from GSH page to new page(s) | N |  |
| JEXL testing | NA | TODO | NA | Just move docs from GSH page to new page | N |  |
| Registry | NA | TODO | NA | Just move docs from GSH page to new page | N |  |
| Grouper config files | Done | TODO | Done | How to read properties from various property files, various data types | N |  |
| GrouperHttpClient | Done | Done | NA |  | NA | Chris |
| Longer term |
| PrivilegeGroupFinder | TODO | TODO | TODO |  |  |  |
| PrivilegeAttributeDefFinder | TODO | TODO | TODO |  |  |  |
| PrivilegeStemFinder | TODO | TODO | TODO |  |  |  |
| AttributeAssignToEffectiveMembershipSave | TODO | TODO | TODO |  |  |  |
| AttributeAssignToImmediateMembershipSave | TODO | TODO | TODO |  |  |  |
| AttributeAssignToMemberSave | TODO | TODO | TODO |  |  |  |
| MemberChangeSubjectSave | TODO | TODO | TODO |  |  |  |
| PrivilegeGroupInheritanceFinder | TODO | TODO | TODO |  |  |  |
| PrivilegeStemInheritanceFinder | TODO | TODO | TODO |  |  |  |
| PrivilegeAttributeDefInheritanceFinder | TODO | TODO | TODO |  |  |  |
| ConfigInDbSave | TODO | TODO | TODO | See GrouperEmail.addAllowEmailToGroup() |  |  |
| GroupSave | Done | Done | Done | Add replaceAllSettings like AttestationStemSave |  |  |
| StemSave | NA | NA | NA | Add replaceAllSettings like AttestationStemSave |  |  |
| AttestationGroupSave | NA | NA | NA | Change UI logic so it calls the new logic |  |  |
| AttestationStemSave | NA | NA | NA | Change UI logic so it calls the new logic |  |  |
| AttributeDefSave | NA | NA | NA | Add replaceAllSettings like AttestationStemSave |  |  |
| PrivilegeGroupInheritanceSave | NA | NA | NA | Change UI logic so it calls the new logic |  |  |
| PrivilegeStemInheritanceSave | NA | NA | NA | Change UI logic so it calls the new logic |  |  |
| PrivilegeAttributeDefInheritanceSave | NA | NA | NA | Change UI logic so it calls the new logic |  |  |
| Interacting with daemon jobs | ? | NA | NA |  |  |  |
| USDU | TODO | TODO | TODO | See what we can do for builder classes here |  |  |
| Import / export | ? | TODO | TODO |  |  |  |
| Transactions | ? | TODO | TODO | Decide on recommended strategy and document |  |  |
