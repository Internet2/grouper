---
title: "23-October-2024"
space: GrIntDev
pageId: 48793216
version: 7
lastUpdated: 2026-07-12T17:02:49.961Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793216/23-October-2024
---

# **Grouper Call of Oct 23, 2024**

**Attending**

- Chris Hyzer, Penn, Chair
- Vivek Sachdiva, independent
- Shilen Patel, Duke
- Gail Lift, University of Michigan
- Chad Redman, Unicon
- Jim Beard, Unicon
- Bert Bee Lindgren, GA Tech
- Jordan Dunn, Univ of Virginia
- Drew Aschenbrener, Internet2
- Chris Hubing, Internet2
- Emily Eisbruch, Independent

**DISCUSSION**

Administrivia

- [Internet2 Intellectual Property Policy](https://internet2.edu/community/about-us/policies/internet2-intellectual-property-policy/)
- Review AIs [Grouper Project Action Items (Google Doc)](https://docs.google.com/document/d/1jQCt1nICmVVZsU8iprjbDw0WbmnpUt87NsS7rdKmfMo/edit)
- **Agenda bash**

Grouper Deprovisioning blog now published: [https://internet2.edu/grouper-deprovisioning/](https://internet2.edu/grouper-deprovisioning/)

Congrats Chris for this blog that includes entertaining video

**Grouper at TechEx****[https://events.internet2.edu/website/69276/](https://events.internet2.edu/website/69276/)**

- **Grouper BOF**  
  Tuesday Dec 10, 2024 12:10 pm - 1:25 pm
- **Grouper Chronicles: Success with ABAC and Legacy Challenges**  
  Wednesday Dec 11, 2024 9:00 am - 9:50 am   
   (presentation from Univ of Michigan and Univ of Virginia)

- Several Grouper Team members will attend TechEx

## **Current Projects**

Vivek

- Working on **Adobe provisioner** [Grouper Adobe provisioner developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792592/Grouper+Adobe+provisioner+developer+notes)
- Made good progress, getting close to finishing
- Enhancement to WS External system
- Authentication type
- Includes auth client credentials
- will look at what’s required
- Mock tables on backend
- Simulating how Adobe Auth will work
- Testing end to end behavior
- Does loading work for Grouper prov tables, such as Grouper prov Adobe?
- Not yet tested
- These are tables for storing Adobe users and groups and memberships
- If managing licences in Adobe, you can get a picture of how many licenses your are using
- Useful to be able to load groups in different states in Adobe
- Not closely related to Grouper Sync
- Loading user info from targets
- Loading groups and memberships
- Moving in direction of provisioning to a target and keeping track of what’s in target for reporting or loading
- Can use an entity resolver for provisioning mapping
- Can see local accounts
- Hope to wrap up the Adobe provisioner work soon

Shilen

- Added upgrade task to remove SQL cache attribute that’s no longer being used
- Shilen worked on SQL cache , full sync, takes a long time first time it runs
- Shilen changed it to check other stuff to be sure still in sync
- After that it should be quicker
- [GRP-5749  
  SqlCacheFullSyncDaemon should verify flattened add timestamps](https://todos.internet2.edu/browse/GRP-5749)
- Got into rat hole because with the way using dates, using date type in database, does not keep track of timezone.
- Noticed every time he ran it, it wanted to update 1000 memberships from 1st Sunday of Nov. There is an hour every day for folks on ET, 1:30am , when daylight savings time ends.
- So Shilen made temporary adjustments.
- We should move to milliseconds again.
- Not all databases store time stamps the same way.
- We should standardize on an 8 byte integer. Right now we have tables, views, for each database we should have a package for Grouper w some utility functions. To convert from dates and timestamps to milis and micros since 1970. Also would have timestamp type for browsing easily.
- Need a function that is database specific and works correctly
- This is a forward looking issue for Grouper v7
- TImestamp fields may be more recent
- Date field is recent
- We don’t have time zone component
- Need to think about this carefully
- Chad: We should away from Micros, just use millseconds
- Chris Hyzer: Do that with the functions
- Standardize on micros?
- Yes
- No sequence number for changelog temp
- Checkout values?
- Do we need micros?
- They are in point in time
- Because they are in changelog
- Chris Hyzer happy to do everything in micros
- Path of least resistance, make the function work with both, to avoid migrations of data
- But in Grouper v7 there will be migration of data
- Bert: we should to expose raw number so indexes work
- Put 2 columns in for the view: GMT and local time
- This is for queries in the database
- UI would be same as it is now
- In Grouper v5 we will make some changes, it will be more complete in v7
- Grouper v7 will be a major upgrade
- **Decision**: use Micros since 1970
- Check if this will cause another byte to be used?
- Consistent storage format
- Shilen will convert SQL cache
- With oracle, use 38 or something smaller?
- Using 38 for everything else
- ChrisHyzer: if you declare for 38 it only uses what it needs
- Shilen confirm and suggest around this
- Upgrade task to migrate the data
- Regarding comment about the view having the micros and the timestamp and GMT and local, what do we mean by timestamp?
- We mean timestamp object
- Shilen thinking some databases, are there different timestamp types?
- Could do timestamp with timezone?
- Shilen will do upgrade to convert timestamps to micros
- Then function library that works in all 3 databases
- Does postgres have packages?
- Must do something all databases have, so do Functions
- all functions should start grouper_
- 

Carey

- Carey was asked the other day if the Grouper UI has a way for end users to like request, features or things.
- Chris Hyzer: we can check into that

Chris

- Worked on JIRAS, including

- - SCIM and paths [GRP-5771 scim is not updating name properties correctly](https://todos.internet2.edu/browse/GRP-5771)
  - [GRP-5770 add a way to see what the provisioner will do exactly in readonly mode](https://todos.internet2.edu/browse/GRP-5770)
  - [GRP-5767 add setting and global default to not run logic in full or incremental daemon](https://todos.internet2.edu/browse/GRP-5767)
  - [GRP-5765  
    allow provisioning objects logs count to be configurable](https://todos.internet2.edu/browse/GRP-5765)

Chad

- Worked on [Grouper documentation map](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792975/Grouper+documentation+map). Looks great, thanks
- Idea from a customer, you look at the audit log
- They wanted a graph of members added
- Looks like splunk or AWS, can do number of days, start, end
- Divisions are weird, 30 divisions
- Can see adds and deletes on day to day basis
- It’s for sanity checking
- Shows more than flat list of audit log
- Has direct and indirect, adds and delete
- Right now it’s just for groups, could be for folders too
- What if someone is added to a group in multiple ways?
- Shilen and Chad will discuss that
- Just need read on the group to access this graph
- For audit logs you need admin
- Chad also worked on
- [https://todos.internet2.edu/browse/GRP-5657](https://todos.internet2.edu/browse/GRP-5657)
- Can't delete group due to dependency on grouper_sync_dep_group_group
- 
- Chris Hyzer: Foreign keys with cascade delete makes sense

Bert

- Entity Attributes: there are 5 to 6 booleans describing when to delete data
- About 5 valid combinations
- Issue of rogue values
- Suggestion to have a warning message for a combination that is not one of the five
- Chris made wiki page [Grouper provisioning entity attribute testing](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554183/Grouper+provisioning+entity+attribute+testing)
- Bert: suggestion to break up info on the wiki into cases
- Too much flexibility?
- Chris will think about this and get back to Bert on this

**Next Steps, from Zoom AI summary of the Grouper Call**

1. Chris to work on an issue with upgrade pass and startup tests.

2. Vivek to complete development of the Adobe provisioner, including testing end-to-end behavior.

3. Chris to follow up with Vivek on the Adobe provisioner development and wrap it up.

4. Chad to add export as CSV or view as table functionality to the new audit log graph feature.

5. Chad to sync up with Shilen to ensure the audit log graph is correctly representing membership data.

6. Chris to review and potentially adjust the entity attribute provisioning logic based on Bert's feedback.

7. Bert to provide detailed feedback on the Wiki page describing valid combinations for entity attribute provisioning.

8. Chris to iterate on the entity attribute provisioning logic and release a new version by the end of the weekend.

## **Issue Roundup**

**Jiras in past 2 weeks**

- [GRP-5776  
  incremental user not in provisionable group in entity attributes](https://todos.internet2.edu/browse/GRP-5776)
- 
- [GRP-5773  
  add primary key to grouper_prov_scim_user_attr for mysql](https://todos.internet2.edu/browse/GRP-5773)
- [GRP-5772  
  if there is a provisioning sub option set, and the parent is hid, then errors are displayed](https://todos.internet2.edu/browse/GRP-5772)
- [GRP-5771  
  scim is not updating name properties correctly](https://todos.internet2.edu/browse/GRP-5771)
- [GRP-5770  
  add a way to see what the provisioner will do exactly in readonly mode](https://todos.internet2.edu/browse/GRP-5770)
- [GRP-5769  
  Provisioning: SQL Provisioning and IdIndex](https://todos.internet2.edu/browse/GRP-5769)
- [GRP-5768  
  Provisioning: LoaderLog Summary: Error without details nor jobid](https://todos.internet2.edu/browse/GRP-5768)
- [GRP-5767  
  add setting and global default to not run logic in full or incremental daemon](https://todos.internet2.edu/browse/GRP-5767)
- [GRP-5766  
  global provisioning readonly setting to default all provisioners to readonly](https://todos.internet2.edu/browse/GRP-5766)
- [GRP-5765  
  allow provisioning objects logs count to be configurable](https://todos.internet2.edu/browse/GRP-5765)
- [GRP-5764  
  provisioning messages should add to daemon screen total count (in addition to change log count)](https://todos.internet2.edu/browse/GRP-5764)
- [GRP-5763  
  provisioning debug logs should filter memberships based on group/entity filters](https://todos.internet2.edu/browse/GRP-5763)
- [GRP-5762  
  provisioning debug logs should filter on sync objects](https://todos.internet2.edu/browse/GRP-5762)
- [GRP-5761  
  match provisioning debug log filter based on type of attribute](https://todos.internet2.edu/browse/GRP-5761)
- [GRP-5760  
  allow provisioning debug log to input attributes to focus on (in addition to value)](https://todos.internet2.edu/browse/GRP-5760)
- [GRP-5759  
  log inserts of groups and entities also at bottom of object logs](https://todos.internet2.edu/browse/GRP-5759)
- [GRP-5758  
  add report numbers for crud operations in compare section of object logs](https://todos.internet2.edu/browse/GRP-5758)
- [GRP-5757  
  rename provisioning debug label provisioningMshipsToDelete to provisioningMshipsInTargetNotGrouper](https://todos.internet2.edu/browse/GRP-5757)
- [GRP-5756  
  provisioner setting "Remove accented characters" should also massage smart quotes to normal single or double quote](https://todos.internet2.edu/browse/GRP-5756)
- [GRP-5755  
  azure provisioning error when dealing with owners](https://todos.internet2.edu/browse/GRP-5755)
- [GRP-5754  
  provisioning metadata of type "set" does not translate correctly](https://todos.internet2.edu/browse/GRP-5754)
- [GRP-5753  
  entity attributes provisioning is authoritative when it should not be](https://todos.internet2.edu/browse/GRP-5753)
- [GRP-5752  
  refactor entity attributes provisioning](https://todos.internet2.edu/browse/GRP-5752)
- [GRP-5751  
  entity attribute provisioner full run will delete and add memberships flapping back and forth](https://todos.internet2.edu/browse/GRP-5751)
- [GRP-5749  
  SqlCacheFullSyncDaemon should verify flattened add timestamps](https://todos.internet2.edu/browse/GRP-5749)
- [GRP-5748  
  allow un-attested groups to be disabled](https://todos.internet2.edu/browse/GRP-5748)
- [GRP-5747  
  upgrade libraries for security](https://todos.internet2.edu/browse/GRP-5747)
- [GRP-5746  
  upgrade tomcat](https://todos.internet2.edu/browse/GRP-5746)
- [GRP-5745  
  edit incremental sql ddl upgrades and files and remove things that are in upgrade steps](https://todos.internet2.edu/browse/GRP-5745)
- [GRP-5744  
  add "days before to email" for attestation. default to a global config which defaults to 0](https://todos.internet2.edu/browse/GRP-5744)
- [GRP-5743  
  allow jexl abac to use assignment instead of comparison (one equals sign)](https://todos.internet2.edu/browse/GRP-5743)
- [GRP-5742  
  data provider should resolve subjects in bulk](https://todos.internet2.edu/browse/GRP-5742)
- [GRP-5741  
  Can't unset scripted jexl - only option is Analyze and Cancel](https://todos.internet2.edu/browse/GRP-5741)
- [GRP-5740  
  Run SqlCacheFullSyncDaemon for an extra hour to process groups without recent changes](https://todos.internet2.edu/browse/GRP-5740)
- [GRP-5739  
  scripted group not filtering when set to only include "institution defined subject sources"](https://todos.internet2.edu/browse/GRP-5739)
- [GRP-5738  
  Online Betting: A Deep Dive into the Digital Gambling Revolution](https://todos.internet2.edu/browse/GRP-5738)
- [GRP-5737  
  Remove old sqlCacheGroup attributes (sqlCacheableGroupMarkerDef and sqlCacheableGroupDef)](https://todos.internet2.edu/browse/GRP-5737)
- [GRP-5736  
  do not allow duplicate object changes in provisioning](https://todos.internet2.edu/browse/GRP-5736)
- [GRP-5735  
  update bouncecastle and remove duplicates](https://todos.internet2.edu/browse/GRP-5735)
- [GRP-5734  
  entity attribute provisioner with new provisionable group does not create member and membership sync objects in incremental run for existing memberships](https://todos.internet2.edu/browse/GRP-5734)
- [GRP-5733  
  entity attributes deleteValueIfManagedByGrouper does not delete if attribute not assigned to anything in grouper (empty group)](https://todos.internet2.edu/browse/GRP-5733)
- [GRP-5732  
  Scripted loader syntax error sometimes redirects to "Click here to start over." page](https://todos.internet2.edu/browse/GRP-5732)
- [GRP-5731  
  Date picker format is mm/dd/yyyy which is rejecte](https://todos.internet2.edu/browse/GRP-5731)

**Wiki Updates**

- [Grouper Built-in Basic Authentication to UI and Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549360/Grouper+Built-in+Basic+Authentication+to+UI+and+Web+Services)
- Subject API
- [Build new grouper client](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793024/Build+new+grouper+client)
- [Grouper provisioning framework logging](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555398/Grouper+provisioning+framework+logging)
- [Grouper provisioning entity attribute testing](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554183/Grouper+provisioning+entity+attribute+testing)
- [Example Zabbix monitoring](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555675/Example+Zabbix+monitoring)
- [Install docker postgres database](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555530/Install+docker+postgres+database)
- [Change log consumers](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545225/Change+log+consumers)
- [How to Setup a lite Grouper Development Environment for Grouper](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/48792900/How+to+Setup+a+lite+Grouper+Development+Environment+for+Grouper)
- [Grouper documentation map](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792975/Grouper+documentation+map)
- [Grouper container postgres database auto install example](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560446/Grouper+container+postgres+database+auto+install+example)
