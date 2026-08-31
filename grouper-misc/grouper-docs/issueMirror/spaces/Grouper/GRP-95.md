---
key: GRP-95
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-95
type: Task
status: Resolved
resolution: Invalid
priority: Minor
reporter: Chris Hyzer <mchyzer@example.com>
assignee: Chris Hyzer <mchyzer@example.com>
created: 2008-03-06T19:04:05.875+0000
updated: 2008-07-21T05:41:04.608+0000
resolved: 2008-07-21T05:41:04.610+0000
components: [API]
fixVersions: [1.3.1]
labels: []
links: []
---

# GRP-95  hib3 performance is 10-20% slower

Running TestGroup0.runPerfProblem2() after having the db filled with LoadData.loadDukeData() shows that hib3 is 10-20% slower than hib2.  (need to run twice, once for hib2, once for hib3).  I added jamon.jar to measure performance and calculate statistics.  I added criteria queries and support to figure out batching to try a different way to do the cartesian product of groups and attributes.

## Comments

### mchyzer - 2008-07-21T05:41:04.602+0000

I cant compare to hib2 anymore, too much has changed... If there are things now that are slow, lets profile them and see if we can improve the performance.  Also, I did do a lot of comparisons back when this was opened, and didnt see anything obvious.