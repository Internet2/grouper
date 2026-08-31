---
key: GRP-7
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-7
type: New Feature
status: Closed
resolution: Completed
priority: Major
reporter: Blair Christensen <blair@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-07-16T16:22:43.886+0000
updated: 2008-06-25T11:26:19.990+0000
resolved: 2008-06-25T11:26:19.992+0000
components: [API]
fixVersions: []
labels: []
links: [depends on GRP-12]
---

# GRP-7  Simplify UI for browsing

From Gary Brown:

These are the signatures that would help simplify the UI for browsing:

GrouperStem:
 public Set getChildGroups(AccessPrivilege[] privileges)
 public Set getChildStems(NamingPrivilege[] namingPrivs, AccessPrivilege[] accessPrivs)
 public Set getChildStems(NamingPrivilege[] namingPrivs)
 public Set getChildStems(AccessPrivilege[] accessPrivs)

Child groups are straightforward. Child stems are an `OR` on the GrouperSession subject having one of the AccessPrivileges on any descendant Group, or NamingPrivilege on any descendant Stem

Group /stem searching would benefit from similar methods, though stem searching would generally return stems where the GrouperSession subject had CREATE or STEM privilege.

Not sure if this would be best done as filters or an additional argument to the createQuery method or getGroups.getStems

When listing memberships the UI shows the number of paths by which a subject is a group member. In order to do this I use a getMemberships call and iterate through the whole membership coming up with a list  - of `unique` subjecta and the count for their occurrence. As I`m using Maps to wrap objects in the UI I set the occurrence count on the Map. It would
simplify the UI if the API were able to do something similar  - a getMembersWithCount. I`m not sure there is a particularly clean way to do this - but there are several kludgy ones e.g. return a Set implementation that can be cast to something with a getCount(Subject subj) method.


## Comments

### blair@example.com - 2007-07-16T16:23:21.117+0000

I should split this into a couple of individual issues.

### blair@example.com - 2007-07-23T14:47:26.915+0000

This is an API issue, not UI issue.

### blair@example.com - 2007-07-23T15:19:50.657+0000

Implementing these methods should be easier once I've implemented the methods required in the parent issue.

### blair@example.com - 2007-08-15T16:36:21.876+0000

I've added two new methods to HEAD that (pending confirmation from Gary) resolve this issue.

* Stem#getChildGroups(Privilege[], Stem.Scope)
* Stem#getChildStems(Privilege[], Stem.Scope)

The "Privilege[]" array can take either Access or Naming privileges.  When passing Access privileges to "Stem#getChildStems(...)" it should return the parent stem of groups where the current subject has that privilege.

### blair@example.com - 2007-08-23T14:58:18.043+0000

From Gary:

"I've had a quick look at the code. getChildGroups looks like it should
work. I'm not sure that getChildStems will, however, all the components are
there for me to adapt that method should the need arise. I'll try and adapt
the UI to use the code tomorrow and so should know more then.

Once a stem / group is added to a result set there is no need to check
other privileges so a 'break' could be used."

The second point is an important - and trivial - optimization to implement.

(UPDATE 20070824 11:29) I've added the "break" statements

### blair@example.com - 2007-08-23T15:00:50.771+0000

From Gary:

"I've attached a modified Stem.java which mostly does what I need it to do - but which won't work quite as you intended - at least with regard to Stems. However, I'm not sure the general approach used will improve performance - see below.

The original code, if called with Scope.ONE, would return any immediate stems matching a supplied naming privilege and would sometimes return the 'parent' stem as well, but it would not take account of descendant groups.

(UPDATE 20070824 13:52 It should no longer return the current stem when an immediate child group matches one of the requested privileges)

Using Scope.SUB would also return stems below the current level taking into account descendant groups, however, I would need to filter out groups not at the current level, and it is possible the parent stem would also be  returned. In addition, if the supplied privileges included naming privileges, stems which had descendant stems where the subject has a supplied naming privilege but no group (with supplied access privilege), would not be returned.

The changes I have made work as I want when Scope.ONE is supplied. I don't  currently have a need for Scope.SUB and am not quite sure how that would best work. The basic logic is:

Iterate child stems.

IF stem matches a naming priv THEN 'add' and 'break', setting wasAdded=true and continuing to next stem ELSE no match so test each descendant stem for this child stem. On the first match 'add' and 'break'.

IF no matches THEN test each descendant group. On the first match 'add' and 'break'

IF no matches THEN discard this child stem and move onto next.

I've separated out the Access and Naming privileges so that we don't check privileges which can't apply.

(UPDATE 20070824 13:34 I now separate out Access and Naming privileges)

Thinking about this approach some more I'm sure the performance will vary greatly depending on the structure of the repository. It might be fairly quick for someone with lots of privileges, but slow for someone with few privileges -  where we end up processing every descendant without finding a match. It would probably be slow near the root stem but quicker when deeper
in the hierarchy. When I suggested the high level methods I envisaged a more query based solution i.e.

select count(1) from grouper_groups gg,
                                grouper_stems gs,
                                grouper_memberships gms,
                                grouper_members gm
                            where
                               gms.owner_id = gg.uuid
                               and gg.parent_stem=gs.uuid
                               and gms.list_name in ('admins','updaters')
                               and gms.member_id = gm.member_uuid
                               and gs.NAME like 'qsuob:%'
                               and gm.subject_id='kebe'

If the actual implementation were part of the privilege interface others could implement as best they can, however, at least the default implementation would hopefully perform OK.

Whilst testing the UI changes I noticed a user was getting 'extra' stems/groups when browsing 'Join groups'. This is because a Subject with ADMIN or UPDATE can also OPTIN. I'm not sure this is necessary or desirable. Anyone with ADMIN or UPDATE could add themselves to a group in any case and someone with ADMIN / UPDATE for many groups would not expect
to see them when clicking 'Join groups'."


### blair@example.com - 2007-08-23T15:01:38.542+0000

This file is associated with Gary's comments in https://bugs.internet2.edu/jira/browse/GRP-7#action_11346

### blair@example.com - 2007-08-24T19:17:51.322+0000

I just checked in some more changes that I *think* capture at least some of what Gary expressed in his last feedback.  I'll check with Gary to confirm if that is actually the case.

### Gary Brown - 2007-09-11T09:52:05.898+0000

The breaks look OK, however 

Stem: public Set<Stem> getChildStems(Privilege[] privileges, Scope scope) doesn't look right.

It correctly checks whether the current user has any provided Naming privilege for immediate child stems, but if it doesn't the following code is executed:

if ( !stems.contains(stem) ) { // no matching naming privileges so checking access privilegees
        // filtering out naming privileges will happen in "#getChildGroups(Privilege[], Scope)"
        for ( Group group : stem.getChildGroups(privileges, scope) ) {
          stems.add( group.getParentStem() );
        }
      }
 
This code does not take account of any descendant stems where the user has one or more of the(possibly) provided naming privileges. In addition, it may return descendant stems:

   stems.add( group.getParentStem() );

should really be:

   stems.add(stem);
   break;




### blair@example.com - 2007-10-04T03:41:28.996+0000

I am working on this issue again.  I'm currently clarifying with Gary whether my new perception of what he requires is accurate.  I fear I've been working on the wrong problem most of this time.

### tbarton - 2007-11-15T03:22:21.357+0000

Reschedule for v1.3.0.

### Gary Brown - 2008-03-20T15:40:49.172+0000

Leave this for now. Shilen can do more profiling on 1.3 and we can look at the best way of improving performance.

### tbarton - 2008-06-25T11:26:19.892+0000

Overtaken by events, no longer seems relevant.


## Attachments
- Stem.java (48582 bytes) - by ? on 2007-08-23T15:01:38.420+0000