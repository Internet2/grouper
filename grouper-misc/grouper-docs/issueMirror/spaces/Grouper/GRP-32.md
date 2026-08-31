---
key: GRP-32
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-32
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Blair Christensen <blair@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-08-28T16:51:02.966+0000
updated: 2007-08-30T15:54:56.100+0000
resolved: 2007-08-30T15:54:56.104+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-32  Add Hibernate3 support

To begin with:

* We won't care about taking advantage of new Hib3 features. Just copy-and-paste + global search-and-replace enough to get it compiling and tests passing
* Add docs on what configuration params need to be changed to get it to work.
* Include it with the API but don't make it default.
* After more testing it can become the default.





## Comments

### blair@example.com - 2007-08-30T15:54:56.085+0000

Just checked into HEAD.

A few more details can be found at:
  http://groupertoolkit.blogspot.com/2007/08/grp-32-experimental-hibernate3-support.html

And a reminder: this is all experimental at this point.