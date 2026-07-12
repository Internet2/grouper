---
title: "University of Nebraska - Grace Period in Grouper"
space: Grouper
pageId: 28544138
version: 11
lastUpdated: 2026-07-12T15:26:12.099Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544138/University+of+Nebraska+-+Grace+Period+in+Grouper
---

**This info is from Advance CAMP session at 2019 Technology Exchange in New Orleans**

**[https://docs.google.com/document/d/1iLbTbPe0qop0BW7L5nbEpYJY21vrg6I8nNdQMGU2DDk/edit](https://docs.google.com/document/d/1iLbTbPe0qop0BW7L5nbEpYJY21vrg6I8nNdQMGU2DDk/edit)**

***Also of interest, Grouper automatically managed recent memberships feature added in May 2020: [Grouper recent memberships / grace periods](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545165/Grouper+recent+memberships+grace+periods)***

Documentation from University of Nebraska on how to accommodate a 90 day grace period for employees.

This uses a [rule](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules) through the Grouper UI, which **adds a user to another group when their membership removes**.

- *contact Brett Bieber or Patrick Haugland for more info*

Adding removed member to another group with expiration date (grace period group).

*Note these rule only triggers on users who are direct members of the access policy.

1. Create a grace period group for your access policy.
2. Navigate to the group you wish to add a grace period rule to and click **More | Attribute assignments**
3. Click the **+ Assign Attribute** button and add in the following attribute assignment:

1. Now click on the actions button to the right of the rule attribute that has been added and select "Add metadata assignment. Do this for each of the following attributes

*When you're done it should look something like this
