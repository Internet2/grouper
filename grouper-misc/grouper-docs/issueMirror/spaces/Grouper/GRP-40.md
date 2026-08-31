---
key: GRP-40
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-40
type: Bug
status: Resolved
resolution: Fixed
priority: Trivial
reporter: James Cramton <jcramton@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-09-20T17:34:39.931+0000
updated: 2007-10-18T11:22:37.865+0000
resolved: 2007-10-18T11:22:37.983+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-40  Subject Summary Page does not display modify or create time or subject values

The create/modify date/time and create/modify subject values are not displayed on the Subject Summary page for a group that does have the values defined. Using Brown's group info script, we verified that direct method calls to the Grouper API return values for the create/modify attributes, but they are not displayed in the Subject Summary page.

Here's the output from our group info script; compare with the attached screenshot

Group: COURSE:TEST:0001:2007-Fall:S01:All
  No person members
  Group members: (3)
    COURSE:TEST:0001:2007-Fall:S01:Administrator
    COURSE:TEST:0001:2007-Fall:S01:Contributor
    COURSE:TEST:0001:2007-Fall:S01:Learner
  Group types: (1)
    base
  ACLs:
    admin:
      GrouperSystem
    read:
      ADMIN:COURSE
      COURSE:TEST:0001:2007-Fall:S01:Administrator
      SERVICE:BULK_MAIL
      SERVICE:WEBAUTH
  Creation and modification:
    createSource = ""
    createSubjectName = "GrouperSystem"
    createTime = "Thu Sep 06 11:12:51 EDT 2007"
    modifySource = ""
    modifySubjectName = "GrouperSystem"
    modifyTime = "Thu Sep 06 11:14:15 EDT 2007"
  Attributes:
    description = "All members for TEST0001 S01 2007-Fall"
    displayExtension = "  All  "
    displayName = "COURSE:TEST:0001:2007-Fall:S01:  All  "
    extension = "All"
    name = "COURSE:TEST:0001:2007-Fall:S01:All"


## Comments

### James Cramton - 2007-09-20T17:35:35.419+0000

The subject summary page for a group does not show values for the create/modify date/time or create/modify subject.

### Gary Brown - 2007-09-25T15:38:19.074+0000

This is a bug in GrouperSubject:


### Gary Brown - 2007-09-25T15:41:19.809+0000

Sorry, somehow submitted the last comment prematurely:

  public Set getAttributeValues(String name) {
    return ATTR_VALUES;
  }

Groups only have single-valued attributes so the above method should create a new Set and add the single value.

This manifested itself with 1.2.0 because I modified the UI to handle multi-valued subject attributes

### Gary Brown - 2007-10-18T11:21:24.250+0000

A straightforward fix and would be nice to have for 1.2.1

### Gary Brown - 2007-10-18T11:22:37.811+0000

getAttributeValues now calls getAttributeValue - and puts any non 'null' value into a Set, otherwise returns an empty Set

## Attachments
- edu.brown.SubjectSummaryNoCreateModifyAttribs.jpg (162424 bytes) - by James Cramton on 2007-09-20T17:35:35.350+0000