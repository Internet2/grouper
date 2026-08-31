---
key: GRP-54
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-54
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Shilen Patel <shilen@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-10-31T17:41:06.287+0000
updated: 2007-11-16T15:24:48.033+0000
resolved: 2007-11-16T15:24:48.035+0000
components: []
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-54  Filtering by Member Searches

When in the "My Membership" section of the UI, if you perform a search, the API will return to the UI a list of potentially matches that need to be filtered based on membership.  This filtering should be done in the API.

## Comments

### shilen - 2007-11-13T13:48:14.899+0000

Gary, I've added a GroupMemberFilter that I've been experimenting with that seems to help with this issue.  What are your thoughts on having a UI option to use an intersect filter when searching in the "My Membership" section.  This will avoid the Group.hasMember checks in the UI.  For instance:

 new IntersectionFilter(new GroupAttributeFilter("name", "search criteria", stem), new GroupMemberFilter(subj, stem))

### Gary Brown - 2007-11-13T13:58:12.697+0000

Seems reasonable - I'll have a go at profiling what currently happens, switch to what you suggest and profile again afterwards.

### Gary Brown - 2007-11-16T15:24:47.980+0000

I haven't actually used the new filter as it would require quite a few changes to the UI to enable it - the code which runs the searches isn't necessarily aware of which 'mode' it is running in. Filtering generally happens in a calling method.

That said, I have changed MyMembershipsRepositoryBrowser so that rather than doing Group.hasMember checks it does one Member.getGroups (and caches the results) , and checks for search results there.

This actually doubled the time spent doing 'isValidSearchResult' calls - in my repository, however, the amount of time spent doing this check was <= 3% of the total. I did the search as a subject with 36 group memberships. The search yielded 54 results. So it was quicker to do 54 hasMember checks than to do Member.getGroups (which internally does privilege checking  - and group instantiation?).

Now the search actually gave over 1900 results, but my subject could only view 54 results. If the subject had had view privilege for the majority of groups then there would have been many more hasMember checks and so the Member.getGroups would be more efficient once you are checking >100 groups.

So, in my repository, doing privilege checking on the GrouperQuery results rather than for every QueryFilter, would have been 7+ times faster. In a repository where the default is to give VIEW privilege to GrouperAll, it wouldn't make much difference.