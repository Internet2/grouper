---
key: GRP-47
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-47
type: Bug
status: Closed
resolution: Fixed
priority: Critical
reporter: James Cramton <jcramton@example.com>
assignee: Shilen Patel <shilen@example.com>
created: 2007-09-20T20:46:02.031+0000
updated: 2009-10-20T17:21:13.223+0000
resolved: 2009-10-20T17:21:13.243+0000
components: [API]
fixVersions: [1.5.0]
labels: []
links: [duplicates GRP-79]
---

# GRP-47  Do not permit creation of multiple identical membership paths 

One of our Grouper provisioning runs was accidentally started twice, a couple minutes apart. Both runs ran to completion, and this created multiple, identical paths to membership in the same groups. Although not a fatal data error, having multiple identical paths to membership is never correct, and it raises the prospect that this may reveal a larger problem in the API's design. Multiple simultaneous users of the API should not create duplicate records.  To solve the issue, we tried using the API to delete one of the paths, but the API returned an error because it could not uniquely identify a single membership pathway to delete. There are 2 possible solutions to this issue. I believe the correct solution is to properly handle a multi user environment, so simultaneous (or near-simultaneous) transactions do not duplicate data. The alternative, minimally acceptable solution is to allow the deletion of one of the identical paths of membership.  

Brown was forced to choose the 3rd alternative, restoring the Oracle DB from backup and reapplying provisioned groups and manual changes since the error occurred.  We wrote scripts to export and re-import the manual changes to the Grouper DB that we plan to contribute to the project. The scripts are useful for snapshotting the manual changes to the Grouper DB.

## Comments

### Gary Brown - 2007-09-25T11:02:48.539+0000

At one point blair was working on more of a server model where API calls would effectively be queued. This approach was tried so that issues such as yours would not occur and also so that long running processes would not tie up the API caller e.g. UI. There are all sorts of implications for clients in an asynchronous environment., however, we never had to deal with them because the approach was abandoned when it proved difficult to debug problems and it was interfering with the release of new functionality.

I would think that the solution is to have an internal group/stem locking mechanism for API calls that change membership (and privilege) data; pure reads should be fine and needn't be blocked. Having a locking mechanism would create its own problems i.e. blocking, or rogue locks, but, other than queuing I don't see how you can get around the problem. In your case the same process kicked off. If it had been two different operations leading to different results it looks like you might have ended up with something in between 



### Steve Olshansky - 2008-09-25T15:12:01.326+0000

SteveO test comment - please ignore

### shilen - 2009-10-20T17:21:13.198+0000

This is no longer possible in 1.5 due to database constraints.