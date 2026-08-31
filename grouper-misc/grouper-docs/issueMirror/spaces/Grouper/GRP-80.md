---
key: GRP-80
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-80
type: Improvement
status: Closed
resolution: Won't Fix
priority: Major
reporter: Shilen Patel <shilen@example.com>
assignee: Shilen Patel <shilen@example.com>
created: 2008-01-14T16:56:09.634+0000
updated: 2008-03-19T11:23:59.678+0000
resolved: 2008-03-19T11:23:59.681+0000
components: [API]
fixVersions: []
labels: []
links: []
---

# GRP-80  Performance of HibernateMembershipDAO.exists()

In Duke's test Grouper environment which has v1.2.1, I've been noticing some performance issues with the method HibernateMembershipDAO.exists().  That method does a query on grouper_memberships in the following way:

select ms.id from HibernateMembershipDAO as ms where 
   ms.ownerUuid  = :owner   and  
   ms.memberUuid = :member  and  
   ms.listName   = :fname    and  
   ms.type  = :type   

The average query time with about 500,000 entries in that table is 355ms.  The reason why this is a problem is because I'm trying to migrate our Grouper 1.1 instance to Grouper 1.2.1 and during this process, HibernateMembershipDAO.exists() is called millions of times.  

If I happen to add an index on that table with the column order [owner_id, member_id, list_name, list_type, mship_type], the average query time drops to 5ms.  This may not necessarily be the solution, but it illustrates the problem.  I think we also need to be careful to not over-index this table.


## Comments

### shilen - 2008-03-19T11:23:59.626+0000

After a few more tests, I discovered that as long as I rebuild the current indexes a few times during the initial data load, I can keep the average query time for each query under 5ms.  Rebuilding the indexes during the first 20% of the load appeared to be most effective.