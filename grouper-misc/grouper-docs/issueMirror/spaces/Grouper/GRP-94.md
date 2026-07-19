---
key: GRP-94
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-94
type: Bug
status: Resolved
resolution: Fixed
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2008-03-05T16:45:58.760+0000
updated: 2008-04-02T08:46:43.015+0000
resolved: 2008-04-02T08:46:43.016+0000
components: [UI]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-94  Class cast exception when hiding stem hierarchy and showing groups

Internally a List of groups is returned. Currently Stems are also returned causing the class cast exception. I'm not sure what has changed but it is a straightforward fix

## Comments

### Gary Brown - 2008-04-02T08:46:43.013+0000

I think this broke because the privs used for checking are read from a properties file and a change was made there. I've now added group / stem specific methods so only the correct objects should be returned