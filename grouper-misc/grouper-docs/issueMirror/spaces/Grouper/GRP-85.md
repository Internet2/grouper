---
key: GRP-85
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-85
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Chris Hyzer <mchyzer@example.com>
assignee: Chris Hyzer <mchyzer@example.com>
created: 2008-01-24T15:12:38.451+0000
updated: 2008-03-26T04:30:34.494+0000
resolved: 2008-01-24T15:18:10.060+0000
components: [API, UI]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-85  remove typos, and improve exception handling

I get errors on the struts-config due to type, and when exceptions are thrown in using sources, the real exception is swallowed...  unswallowed it

## Comments

### mchyzer - 2008-01-24T15:18:10.047+0000

made these fixes

### mchyzer - 2008-02-10T07:19:08.458+0000

the signature of Set<Privilege> getPrivileges in AccessResolver, and a lot of other classes was wrong.  It was really returning Set<AccessPrivilege>.  This bug actually didnt cause any runtime problems since Java generics are only compile time features... but it needs to be changed.  If anything not in grouper was calling these methods, might need to change the call to these to use the new signature.  Nothing else will need to be changed.  All unit tests pass.  Chris

### mchyzer - 2008-03-26T04:30:34.297+0000

Also gave a descriptive error message for a source search by id