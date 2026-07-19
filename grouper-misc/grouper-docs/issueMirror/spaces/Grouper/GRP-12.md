---
key: GRP-12
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-12
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Blair Christensen <blair@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-07-23T15:02:24.953+0000
updated: 2007-10-16T15:18:28.337+0000
resolved: 2007-08-03T16:13:51.962+0000
components: [API]
fixVersions: [HEAD]
labels: []
links: [has dependent GRP-7, has dependent GRP-15]
---

# GRP-12  Poor integration with Ldappc: subordinate-stem-query generates full table scans

A <subordinate-stem-query> does a "GroupNameFilter" with a scope of the specified stem and using "%" as the search value.  That in turns
calls an API method which does a LIKE "%TERM%" query against the "grouper_attributes" table looking for "name", "displayName",
"extension" or "displayExtension" attributes with that value.  So, yes, it certainly looks like a full table scan.

We don't currently provide an easy or efficient way in the API to find all child groups or stems.  We could (and probably) should add that to
the API.  After that we could either modify Ldappc to make use of those new methods or, perhaps, make the API smarter and have it use
the more efficient version if someone is doing a wildcard search.

## Comments

### blair@example.com - 2007-08-02T19:29:42.894+0000

I've added to HEAD:
* ChildGroupFilter - Query filter that retrieves all child groups beneath specified parent stem
* ChildStemFilter - Query filter that retrieves all child stems beneath specified parent stem
* Stem.getChildGroups(scope) - Can retrieve *all* child groups, not just immediate children
* Stem.getChildStems(scope) - Can retrieve *all* child stems, not just immediate children

### blair@example.com - 2007-08-02T19:33:52.682+0000

I accidentally wiped out my large test Postgres groups registry.  Once I've restored that I'll run some comparisons between using "GroupNameFilter" and "ChildGroupFilter".  Given the speed of the machines involved it will probably be tomorrow at the earliest when I can generate any numbers.

### blair@example.com - 2007-08-03T16:13:51.957+0000

Testing was done using my Postgres installation.  This was using an old PowerBook G4 laptop as the client and Postgres running on a slow Linux VM as the registry database.  There are 728 stems and 2178 groups in the registry.

Initial performance tests indicated searching for child groups with a scope was ~10x faster *but* searching for child groups from the root stem was ~2x slower.  I then added a significant optimization hack (revert to full table scan if searching from root stem) and a couple of smaller tweaks.  Searching with scope is still ~10x faster while searching from the root stem is now ~8x faster.  

While this fixes the API it doesn fix Ldappc.  That will be done once this API code has been released.