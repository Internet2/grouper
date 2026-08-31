---
key: GRP-21
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-21
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Blair Christensen <blair@example.com>
assignee: Shilen Patel <shilen@example.com>
created: 2007-08-07T15:41:07.961+0000
updated: 2007-10-16T15:18:27.942+0000
resolved: 2007-08-28T14:08:13.251+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-21  Add Group attribute caching

Add some level of caching to group attributes.  The API is generating too much work for the retrieval of attributes.

## Comments

### blair@example.com - 2007-08-24T15:19:01.287+0000

"Group#getAttribute(String)" currently runs through a series of validations upon every call that are not necessary.  While the body of the method is fairly ugly at the moment I think it can be replaced with something much simpler:

* Call "Group#getAttributes()" internally
* If attribute exists, return value.  We might also need to check if the value is null.  If so, return an empty string.
* Otherwise throw an "AttributeNotFoundException"

### shilen - 2007-08-28T14:08:13.248+0000

To address the performance concerns with getAttribute(String), I modified the code to only determine if an attribute is valid for a group if the attribute value is not available from getAttributes().  