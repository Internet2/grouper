---
key: GRP-42
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-42
type: Improvement
status: Resolved
resolution: Fixed
priority: Trivial
reporter: James Cramton <jcramton@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-09-20T17:58:10.039+0000
updated: 2007-11-17T17:26:04.659+0000
resolved: 2007-10-31T14:15:41.638+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-42  Allow users and groups to be sorted separately in List Members page

When viewing direct and indirect members of a group, the group members and person members are collectively sorted alphabetically. This produces an unpredictable and less useful display. We would like to be able to display the groups in one bundle, and the person members in another. Perhaps a filter in the Change Selection Scope form would help.

## Comments

### James Cramton - 2007-09-20T17:59:38.628+0000

Gropus and Users currently list alphabetically. In some groups, this can be very unreadable. For most day-to-day use, group member listing is not needed.

### James Cramton - 2007-09-20T17:59:41.806+0000

Gropus and Users currently list alphabetically. In some groups, this can be very unreadable. For most day-to-day use, group member listing is not needed.

### Gary Brown - 2007-09-25T15:01:26.304+0000

I've updated SubjectComparatorHelper (in CVS) to read the following media.properties:

 subject.pre-sort.g\:gsa=a
 subject.pre-sort.<YOUR_SOURCE_ID>=b

The pre-sort string is pre-pended to th eusual sort string so that subjects from the same source will naturally group together.

The UI iterates over all members so it should be possible to determine the list of Subject sources which are present and allow the user to select one or all for display.

### Gary Brown - 2007-10-31T14:15:41.610+0000

You can now define in media.properties, per source id, a String which will be pre-pended to the usual sort String. In this way like Subjects will group together e.g. 

subject.pre-sort.g\:gsa=AAA
subject.pre-sort.qsuob=BBB

Also:


### Gary Brown - 2007-10-31T14:18:59.793+0000

# Allow filtering of membership lists by subject source
members.filter.by-source=true
members.filter.limit=500

The original implementation of this was tweaked to avoid instantiation of subjects if  disabled. In fact a further change in the API would let me get the source id from the Membership object.



## Attachments
- edu.brown.ListGroupsApartFromUsers.jpg (220912 bytes) - by James Cramton on 2007-09-20T17:59:41.729+0000
- edu.brown.ListGroupsApartFromUsers.jpg (220912 bytes) - by James Cramton on 2007-09-20T17:59:38.540+0000