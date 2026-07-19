---
title: "4-December-2024"
space: GrIntDev
pageId: 48793192
version: 9
lastUpdated: 2026-07-19T00:32:53.014Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793192/4-December-2024
---

# **Grouper Call of Dec. 4, 2024**

**Attending**

- Chris Hyzer, Penn, Chair
- Vivek Sachdiva, independent
- Shilen Patel, Duke
- Gail Lift, University of Michigan
- Bert Bee Lindgren, Georgia Tech
- Kellen Murphy, Univ of Virginia
- Daniel Fisher, Va Tech
- Chad Redman, Unicon
- Matt Black, Purdue
- Drew Aschenbrener, Internet2
- Chris Hubing, Internet2

## **DISCUSSION**

**Administrivia**

- [Internet2 Intellectual Property Policy](https://internet2.edu/community/about-us/policies/internet2-intellectual-property-policy/)
- Review AIs [Grouper Project Action Items (Google Doc)](https://docs.google.com/document/d/1jQCt1nICmVVZsU8iprjbDw0WbmnpUt87NsS7rdKmfMo/edit)
- Agenda bash

******

**Grouper at TechEx****in Boston**

- **Grouper BOF**  
  Tuesday Dec. 10, 2024 12:10 pm - 1:25 pm
- ***Gro******uper Chronicles: Success with ABAC and Legacy Challenges**   
  Wednesday Dec 11, 2024 9:00 am - 9:50 am   
   (presentation fr****om Univ of Michigan and Univ of Virginia)*********

***Zoom AI summary of this call:***

- *The team discussed enhancements to their system, including the addition of a parser to handle function statements and the removal of certain functionalities.*
- *They also addressed a bug related to group composites and decided on a process for handling disabled or future-enabled memberships.*
- *Lastly, they discussed issues with the SCIM protocol, the functionality of JXL queries, and the process of syncing and updating group dependencies.*

***Next Steps from ZOOM AI***

1. *Vivek to merge two wikis and add information about changing DDL or enabling changing DDL when an upgrade has DDL.*
2. *Chris Hyzer to craft a message with Vivek for deleting future memberships when converting a group to composite.*
3. *Chris Hyzer to add "use NOT EXISTS instead of NOT IN" to coding standards wiki.*
4. *Shilen to search for other instances of "NOT IN" in Grouper code that could be optimized.*
5. *Chris Hyzer to review Gail's Slack messages about data provider issues in version 5.14.*
6. *Drew to work with Chris Hubing on using jstack to diagnose performance issues with the propagate attributes function.*
7. *Chris Hubing to set up a separate AWS account for training VM management and provide necessary credentials.*
8. *Bert to document specific steps for group creation and labeling issues in incremental/full sync for Chris Hyzer to review.*

## **Current Work**

**Vivek**

- Working on [Grouper upgrade tasks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)
- Functions supported in full SQL files
- Limitation was old way using library to run individual statements but could not run functions
- Issue with semicolons in body of fuctions
- Ant library was causing issue w semicolons
- So moved the logic, so not relying on that library for SQL statements
- Using a parser to look at each line
- Can ignore spaces and identify functions
- Will not delimit based on semicolons
- Can add functions in SQL files
- If you move lines around it might fail
- Chris: added code to see if a function is loaded in the database, for postgres and MYSQL
- Not looking at the body of it
- It sees if there is a function
- Took out create or replace, it’s more precise
- Shilen: with the upgrade task, did create or replace in case of changes in the future
- Could be part of a future upgrade task
- Can you replace a function if its being used by view in every database?
- Anyway it won’t do anything if function exists
- Functions now work like everything else
- Hope to keep the code base for v4 and v5
- Will merge back
- We have enums for every upgrade task
- All are something plus right now
- We should denote that in the enum
- Specified versions
- All code can be the same
- If we cherry pick from v5 to v4 and it brings back an enum, there may be issues
- Upgrade tests do not work when you install Grouper from scratch. This needs to be fixed.

- *From Zoom AI:*

- - ***Addressing Group Composite Creation Bug***

- - - *There has been a bug encountered while making a group composite.*

- - - *The issue arose when there were disabled or future-enabled memberships in the group. The team agreed that the UI should inform users about these memberships and prompt them to remove them before proceeding with the composite creation.*

- - - *They also discussed the possibility of deleting these memberships automatically, but decided against it due to potential complications.*

- - - *Instead, they decided to display a message informing users about the deleted memberships.*

- - - *The team agreed to further refine the message and the process for handling these situations.*

**Shilen**

- Worked on performance issue.
- Starting work on the SQL cache history full sync daemon.

**Chad**

- **Chad fixed issues with API functions related to finding groups, query filters, containing certain attributes.

- Now the API functions work with either legacy attributes or new attributes.

- Tested working on what it will take to upgrade bootstrap, must add new CSS, long term project, not urgent, may come up in context of security tests

**Chris**

*From Zoom AI summary:*

**SCIM Protocol Issues and Solutions**

Chris Hyzer discussed the issues with the SCIM protocol, particularly the protocol for memberships and SCIM, and the problem with the ServiceNow full syncs.

He proposed a solution where either a group with users or a user with groups can be used, depending on what the target supports.

Chris also highlighted the SCIM’s three ways to patch emails and suggested that if all these methods are implemented, it should be possible to configure SCIM to work with any target. However, he noted that some targets may not fully support all methods, and it would be beneficial if the SCIM information endpoint could indicate which methods a target supports.

- SCIM work is nuanced around what the target expects
- There are issues with SCIM
- It was never fully baked, some things need work

***JEXL queries and Group******Syncing Issues***

- - [GRP-5855  
    allow abac row subscripts to use = instead of ==](https://grouper.atlassian.net/browse/GRP-5855)

*From Zoom AI summary:*

*Chris Hyzer discussed the functionality of JEXL queries, particularly the use of 'has attribute' and equal sign in queries.*

*He also explained the process of syncing and updating group dependencies, including the use of an incremental job to process membership changes.*

Suggestion for Dependencies on values of attributes

Risk of being too specific, depending on policy

Incremental might do more recalcs than it needs to, but we can readjust

Incremental will process changes, see which group/field or attribute has a scripted group that’s dependent on it and make structure for which scripted groups must be recalculated, then tries to in batches recalc scripted groups for sets of users

Might not be sufficient at first, can be improved

**Other**

- Grouper 5.14: Gail has concern about data providers stopping work. AI Chris agreed to investigate.
- Drew discussed issues with the provisioning piece in the database. 
  
  - Chris Hyzer suggested using jstack to identify the problem. Chris Hubing will help Drew.

**Grouper Training**

- There will be a meeting on Friday Dec 6 on Grouper training.
- Chris discussed the proposed new training system, where users could register for different tracks and complete pre-work at any time, with the option to spin up a VM for the in-person class. With a certain number of hours available to use the VM (EC2 instance) for pre-work.
- Could use a GSH template to manage the hours.
- Could use a background daemon.
- Might need security for this.
- Not an autoscaling group.
- AI Chris Hubing to explore further the Grouper Training using VMs with a certain number of hours available idea and the need for credentials (username and pwd) to spin up EC2s, having it initialize itself, need to delete itself later.
- Also looking at bundles for Grouper Training
  
  - Executive Track
  - Core Implementation Track
- Hope to have this new Grouper Training approach, including on demand training modules, for March 2025
- Need another AWS account to experiment with the new training approach, Chris Hubing will look into that.

## **Issue Roundup**

- [GRP-5860  
  Provisioner configuration uses inline CSS for radio button labels](https://grouper.atlassian.net/browse/GRP-5860)
- [GRP-5859  
  sql cache full sync - replace 'not in' queries with 'not exists'](https://grouper.atlassian.net/browse/GRP-5859)
- [GRP-5858  
  allow functions in full ddl file](https://grouper.atlassian.net/browse/GRP-5858)
- [GRP-5857  
  scim provisioning json pointer config does not exist in scim objects in certain situations](https://grouper.atlassian.net/browse/GRP-5857)
- [GRP-5856  
  scripted group hasAttribute should check for existence of attribute for non booleans](https://grouper.atlassian.net/browse/GRP-5856)
- [GRP-5855  
  allow abac row subscripts to use = instead of ==](https://grouper.atlassian.net/browse/GRP-5855)
- [GRP-5854  
  scim provisioner custom json pointer field updating to null should just remove the attribute](https://grouper.atlassian.net/browse/GRP-5854)
- [GRP-5853  
  upgrade task has extra semicolons](https://grouper.atlassian.net/browse/GRP-5853)
- [GRP-5852  
  scim provisioner does not remove custom json pointer fields correctly](https://grouper.atlassian.net/browse/GRP-5852)
- [GRP-5851  
  data provider delete needs to clean up child data](https://grouper.atlassian.net/browse/GRP-5851)
- [GRP-5850  
  Visualization should also support a "group math equation" option](https://grouper.atlassian.net/browse/GRP-5850)
- [GRP-5849  
  propagateProvisioningAttributes Debugging](https://grouper.atlassian.net/browse/GRP-5849)
- [GRP-5848  
  add ability to have translations in ldap to sql or sql to sql](https://grouper.atlassian.net/browse/GRP-5848)
- [GRP-5847  
  update description under abac script](https://grouper.atlassian.net/browse/GRP-5847)
- [GRP-5846  
  improve the error message for bad data in list of groups job](https://grouper.atlassian.net/browse/GRP-5846)
- [GRP-5845  
  viewing provisioning logs causes memory problems](https://grouper.atlassian.net/browse/GRP-5845)
- [GRP-5844  
  log provisioning actions to provisioning log table](https://grouper.atlassian.net/browse/GRP-5844)
- [GRP-5843  
  delete from sync table when things are consistent in provisioning](https://grouper.atlassian.net/browse/GRP-5843)
- [GRP-5842  
  Incorrect audit for memberships that start with a start date](https://grouper.atlassian.net/browse/GRP-5842)
- [GRP-5841  
  add option for qualified and unqualified name in scim](https://grouper.atlassian.net/browse/GRP-5841)
- [GRP-5840  
  add options for patching users](https://grouper.atlassian.net/browse/GRP-5840)
- [GRP-5839  
  rule does not add end date if membership has enabled date in future and end date after that](https://grouper.atlassian.net/browse/GRP-5839)
- [GRP-5838  
  long query loaders do not work with diagnostics](https://grouper.atlassian.net/browse/GRP-5838)
- [GRP-5837  
  WsRestFindGroupsRequest throws error for composite groups when actAsSubject does not have privs on a component of the composite](https://grouper.atlassian.net/browse/GRP-5837)
- [GRP-5836  
  cannot delete jexl loaded population script setting](https://grouper.atlassian.net/browse/GRP-5836)
- [GRP-5835  
  Ability to add custom menu links](https://grouper.atlassian.net/browse/GRP-5835)
- [GRP-5834  
  add change log events to data field changes](https://grouper.atlassian.net/browse/GRP-5834)
- [GRP-5833  
  adding or remove memberships from abac job should be failsafe and log errors](https://grouper.atlassian.net/browse/GRP-5833)

**Wiki Updates**

- [v5 Upgrade instructions from v5](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549165/v5+Upgrade+instructions+from+v5)
- Nov 27, 2024 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549165/v5+Upgrade+instructions+from+v5)
- [v5 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes)
- Nov 26, 2024 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes)
- [Configuring object name constraints](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544862/Configuring+object+name+constraints)
- Nov 22, 2024 • updated by Chad Redman • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544862/Configuring+object+name+constraints)
- [Grouper - SCIM Provisioner - Examples](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28559948/Grouper+-+SCIM+Provisioner+-+Examples)
- Nov 21, 2024 • created by ben.rappleyea.2@example.com
- [Recovering a deleted group / memberships](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547363/Recovering+a+deleted+group+memberships)
- Nov 20, 2024 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547363/Recovering+a+deleted+group+memberships)
- [Grouper provisioning SCIM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555423/Grouper+provisioning+SCIM)
- Nov 20, 2024 • updated by ben.rappleyea.2@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555423/Grouper+provisioning+SCIM)
- [Grouper provisioning - SCIM - Functions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28559941/Grouper+provisioning+-+SCIM+-+Functions)
- Nov 20, 2024 • updated by ben.rappleyea.2@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28559941/Grouper+provisioning+-+SCIM+-+Functions)
- [Grouper provisioning SCIM ServiceNow](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564299/Grouper+provisioning+SCIM+ServiceNow)
- Nov 20, 2024 • updated by ben.rappleyea.2@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564299/Grouper+provisioning+SCIM+ServiceNow)
- [Grouper upgrade tasks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)
- Nov 20, 2024 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)

***Next Grouper Call: Wed. Dec 18, 2024***
