---
title: "Composite group - example"
space: Grouper
pageId: 28548883
version: 8
lastUpdated: 2026-07-01T05:43:20.445Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548883/Composite+group+-+example
---

> A **composite** group has its membership defined by combining two other groups, called **factor** groups, in one of three ways:
> 
> 
> 
> - **Union** (or) — members of either factor group.
> - **Intersection** (and) — only members in both factor groups.
> - **Complement** (not) — members of the "left" factor group who are not in the "right" factor group (left minus right).
> 
> A group can have a single composite member *or* ordinary direct members, but not both. A composite group can only combine *two* factors, so to require three or more conditions you "chain" composites: combine two groups into an intermediate composite, then combine that result with the next group.
> 
> This page is a worked example in the Grouper UI. Composite groups are a core, long-standing Grouper feature; the screenshots are from the Grouper UI.

 > Creating groups and composites requires the `CREATE` privilege (or greater) in the folder where the group is created.

 In this example an application needs users who have email accounts **and** are employees, so the application group is built as the intersection of an "employees" group and a "has email" group.

 

## Make a folder for this example

 Create a folder to hold the example groups.

 

 

## Make three groups

 Create the factor groups for the example (for instance, an "employees" group and a "has email" group) plus the application group.

 

 

 

 

## Require application members to have emails

 On the application group, make it a composite (intersection) so its membership is limited to members who are also in the "has email" group.

 

 

 

 

## Chain a second composite to add the next requirement

 Because a composite combines only two factors, add the next requirement (employees) by chaining a second composite on top of the first result.

 

 

 

## Check the memberships

 Confirm the resulting membership reflects users who are both employees and have email accounts.
