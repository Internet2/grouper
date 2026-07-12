---
title: "How to automatically include groups with a name pattern"
space: Grouper
pageId: 28543996
version: 1
lastUpdated: 2018-08-03T18:03:42.298Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543996/How+to+automatically+include+groups+with+a+name+pattern
---

Got a request:

```
I would like to create a composite group on
grouper of all of the groups found at group_school_rrl_* is that possible? 
An appropriate name could simply be group_school_rrl or group_school_rrl_all
```

So this can be done a few different ways.

1. A loader job that loads members of those groups into another group
2. A loader job that loads the groups themselves as members of the overall group

It is much better to do this with the second option since the number of groups being added doesnt change often, but the members will be real time added to the overall group

Start by writing a query against the grouper database... you can use group ID or name, I will use name just so it is obvious which groups are being added... ID will work too

Note, if using group ID, that is the subject source ID, so the column name would be "SUBJECT_ID" instead of "SUBJECT_IDENTIFIER"

```
select name subject_identifier, 'g:gsa' subject_source_id from grouper_groups where name like 'school:apps:box:boxgroups:group_school_rrl_%' and name != 'school:apps:box:boxgroups:group_school_rrl_all'
```

Results

```
SUBJECT_IDENTIFIER										SUBJECT_SOURCE_ID
school:apps:box:boxgroups:group_school_rrl_baxter		g:gsa
school:apps:box:boxgroups:group_school_rrl_biadler	 	g:gsa
school:apps:box:boxgroups:group_school_rrl_bias		 	g:gsa
school:apps:box:boxgroups:group_school_rrl_hernia		g:gsa
school:apps:box:boxgroups:group_school_rrl_leadership	g:gsa
school:apps:box:boxgroups:group_school_rrl_lil_flo	 	g:gsa
school:apps:box:boxgroups:group_school_rrl_panda		g:gsa
school:apps:box:boxgroups:group_school_rrl_rehab_cares 	g:gsa
```

Make a loader job with that query on the following group:

school:apps:box:boxgroups:group_school_rrl_all
