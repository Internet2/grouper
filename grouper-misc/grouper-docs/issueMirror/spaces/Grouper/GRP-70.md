---
key: GRP-70
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-70
type: Improvement
status: Closed
resolution: Fixed
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Shilen Patel <shilen@example.com>
created: 2007-12-06T13:32:00.084+0000
updated: 2008-02-13T16:32:28.249+0000
resolved: 2008-02-13T16:32:12.687+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-70  Sub-optimal index for grouper_composites

The grouper_composites table has a composite_factor_idx which is an index on left_factor and right_factor. The API does the query:

select * from grouper_composites c 
where  c.left_Factor = :left or c.right_Factor = :right; 

when adding a member to a group. During profiling of a loader program the method HibernateCompositeDAO.findAsFactor(GroupDTO) was using 14% of CPU. An Oracle explain plan  showed a full table scan on grouper_composites. By adding an index on right_factor the CPU reduces to 8% and explain plan shows the indexes are being used.

NB: If there were a method to add a collection of subjects presumably the method could be called once, rather than each time a member is added. 

## Comments

### shilen - 2008-02-13T16:32:12.301+0000

I removed the concatenated index on left_factor and right_factor since there aren't any AND queries in the WHERE clause for those two columns.  Instead I added separate indexes for both of those columns.