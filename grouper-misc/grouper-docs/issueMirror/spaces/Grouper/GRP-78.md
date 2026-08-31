---
key: GRP-78
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-78
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-12-17T15:40:37.179+0000
updated: 2008-01-09T14:10:25.959+0000
resolved: 2008-01-09T14:10:25.960+0000
components: [API, UI]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-78  Improve apparent performance when listing privilegees for stems/groups 

In the UI, listing privilegees is significantly slower than listing members. The main reason for this is that when listing members the UI deals with Membership objects and only instantiates a Subject if it is being displayed (assuming the resultset is larger than the sort limit). When returning privilegees the API internally retrieves Membership oblects  and calls getMember().getSubject().

I propose that we create a new class which implements Subject and takes a Membership object in the constructor. The new class would return available attributes such as subject_id, source_id (available through the Membership object), but other attribute calls would lazily instantiate teh associated Subject and delegate calls to it. In this way we won't unnecessarily instantiate Subjects.

## Comments

### Gary Brown - 2007-12-18T10:38:40.401+0000

Some UI issues are also contributing - determining if a privilege is direct / indirect

### Gary Brown - 2007-12-18T10:47:10.174+0000

Created a LazySubject and modified MembershipFinder to return LazySubjects when listing privilegees. Modified SubjectAsMap so that the constructor does not call methods on the wrapped Subject - deferred to the get method. Also take advantage of the Membership used to create a LazySubject to determine if a privilege is direct or indirect.

All together the changes have made a big improvement. Whereas listing 50 of 1800 privilegees used to take minutes to start rendering the results it now takes 1-2 seconds and is possible to page through the results.

Note that these gains would not occur if we were sorting the results as we would have to instantiate each Subject.

### Gary Brown - 2008-01-09T13:46:38.885+0000

My fixes here introduced a couple of problems:

1) Now that the constructor does not put anything in the Map it appears 'empty' so that the fact the ObjectAsMaps don't fully conform to the Map interface becomes a problem.

2) LazySubject overrode the equals method but not the hashCode method. This means that whenever you have a hash based lookup (HashSet, HashMap) contains / containsKey do not work. This came to light in my loader code which computes differences for actual and desired privilegees to make the minimum API calls necessary.

Unfortunately the hashCode method relies on having a Member object to get the subject id, type and source - so placing the LazySubjects in a HashSet or HashMap would negate any performance gains

### Gary Brown - 2008-01-09T14:10:25.957+0000

1) ObjectAsMap now implements its own keySet, entrySet, size, isEmpty methods so that it more properly fulfills its Map contract

2) The query used to return memberships joins to grouper_members and associates the Member object with the membership so that a separate query is not required to obtain subject id, type and source. There may be some benefit in modifying other queries and methods to behave the same way, however, I'll wait until a need is demonstrated. 