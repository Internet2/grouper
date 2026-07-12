---
title: "2-August-2023"
space: GrIntDev
pageId: 48793665
version: 8
lastUpdated: 2026-07-12T17:03:24.396Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793665/2-August-2023
---

# **Grouper Call of August 2, 2023**

**Attending**

- Chris Hyzer, Penn, Chair
- Vivek Sachdiva, independent
- Shilen Patel, Duke
- Chad Redman, Unicon
- Liam Hoekanga, University of Michigan
- Gail Lift, University of Michigan
- Ian Thomas, U. Toronto
- Chris Hubing, Internet2
- Emily Eisbruch, Independent

Administrivia

- [Internet2 Intellectual Property Policy](https://internet2.edu/community/about-us/policies/internet2-intellectual-property-policy/)
- Review AIs [Grouper Project Action Items (Google Doc)](https://docs.google.com/document/d/1jQCt1nICmVVZsU8iprjbDw0WbmnpUt87NsS7rdKmfMo/edit)
- Agenda Bash

**Mark your Calendar:**

[**Internet2 TechEx is Sept. 18-22, 2023 in Minneapolis**](https://internet2.edu/2023-internet2-technology-exchange/call-for-proposals/)

Note these Grouper related sessions at TechEx:

- **Grouper BOF**
- Tuesday, Sept 19 at lunch  
  12:10pm to 1:40pm
- [https://internet2.edu/2023-internet2-technology-exchange/program/abstracts/#grouperbof](https://internet2.edu/2023-internet2-technology-exchange/program/abstracts/#grouperbof)
- **Wolverine Vs Grouper 2: I’ll be ABAC******
- Tuesday, September 19
- 9am to 9:50am
- [https://internet2.edu/2023-internet2-technology-exchange/program/abstracts/#grouperbof](https://internet2.edu/2023-internet2-technology-exchange/program/abstracts/#grouperbof)

Grouper Training Oct. 17-20, 2023

- [https://incommon.org/academy/grouper-school/](https://incommon.org/academy/grouper-school/)

**Grouper 4.5.0 Released**

- We are proud to announce the release of Grouper 4.5.0.
- There is 1 upgrade step from 4.4.0.
- Note, if you are using a security scanner, the bouncycastle jar was updated in 4.5.0, and Tomcat was updated in 4.3.0, you might get dinged on those before those versions.
- [17 Jiras](https://todos.internet2.edu/issues/?jql=project%20%3D%20GRP%20AND%20status%20in%20(Resolved%2C%20Closed%2C%20%22Ready%20for%20Release%22)%20AND%20fixVersion%20%3D%204.5.0%20ORDER%20BY%20key%20DESC)
- Add progress screen for "[edit stem](https://todos.internet2.edu/browse/GRP-4832)" and "[edit composite](https://todos.internet2.edu/browse/GRP-4829)"
- Add [hook](https://todos.internet2.edu/browse/GRP-4837) and [daemon](https://todos.internet2.edu/browse/GRP-4838) to ensure unique group extension in folder
- [Add edit button on provisionable screen for groups or folders](https://todos.internet2.edu/browse/GRP-4843)
- [Add run button on GSH template screen](https://todos.internet2.edu/browse/GRP-4844)
- [Upgrade bouncycastle for vulnerability](https://todos.internet2.edu/browse/GRP-4855)
- [Add alias1-4 to duo user provisioning](https://todos.internet2.edu/browse/GRP-4827)

**Timeout issue**

- Issue was that there are places in Grouper where timeout is an issue. Add a member of a large group using composites , it can time out.
- In response to timeout issue, there is now a progress bar, needs to be tested
- Most recent release has a few of these fixed
- Edit stem screen
- Edit composites screen
- see wiki
- [Grouper progress UI with time (lightweight) for long running events](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48794057/Grouper+progress+UI+with+time+lightweight+for+long+running+events)****
- Can show percentage done
- For API - large statements, hard to know what percent done
- May get message saying it’s happening and OK to navigate away
- Main entry to UI call , we can have an includes list of paths or an exclude list of paths that don't or both
- Spawn a thread
- UI code - AJAX events
- Try to do a generic approach?
- Any page would have progress?
- Listening for ajax events, if you navigate away, those should cancel
- Q: is this for every request or only after a few seconds?
- A: if you don’t start a task in a thread, you can’t switch it over
- It has a thread, it executes that, then waits for a certain amount of time
- Wait complete seconds
- Times out at 180 seconds
- If it happens for less , which is the usual, you won’t see the progres
- Chris will try to make this more generic
- Use include list of paths in Grouper v4
- Add those centrally or users can tack them in
- In Grouper v5, do it for everything

**Current Work**

Vivek and Chris

- [GRP-4852  
  add provisioning group jexl script example](https://todos.internet2.edu/browse/GRP-4852)
- Chris worked on backend
- Subject cache translation is new type
- A common translation for provisioning if you want to use subject attributes
- This shows how to get translation
- Jexl script - add examples
- The more examples the better
- Question: Should there be a description for the examples?
- Answer: yes, Vivek and Chris will work on this
- This will be in Grouper v4
- Delete values managed by Grouper
- If doing LDAP, and entity attributes, if you want grouper to manage attribute values, and not user or memberships,   
   There is another delete strategy: delete values if managed by Grouper
- UCLA use case

- Setting up a provisioner, can set up security roles,
- Could see full list if only allowed one
- That was fixed

- Grouper ABAC script Analysis

[Grouper ABAC script analysis](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548379/Grouper+ABAC+script+analysis)

- Need to start performance testing this
- To be released with Grouper v5
- Get to this page by editing settings of ABAC script
- Once you have an ABAC script, there can be an Analyze script
- Could be in trace as well

Chad:

- Question on ABAC
- A way to get around large composites
- Switch to data field system
- Loader jobs that look at Grouper tables and get all the groups matching a certain pattern
- If data field or ABAC system could have like strings or expressions, then could do away with logic of the basis group
- Do more query based things, less group based things.
- Member of group, or could apply to data fields also
- Example, If you are in any college this policy applies
- Chris Hyzer: No problem with ??, when it comes to groups, it’s more complex
- Need to be sure user has permission to read all the results
- We should push towards using attributes
- V4 member of groups is available
- V5 moving towards data fields
- Perhaps Won’t need basis group in v5
- Comment:
- If a group you don’t know, you can get a surprise, putting out a long rope, pattern matching can pick up unwanted stuff
- Can limit to power users
- With attribute approach, this is not a problem

Shilen

- Added data providers to Daemon screen
- Uses sync tables
- Chris and Shilen will talk about how to best do incremental

Chad

- Azure issue: wants Azure provisioning,
- Makes people nervous that only correct groups will get touched by Grouper
- Getting Azure permissions
- Group read write all is a big concern
- Can we restrict?
- Permissions within GRAPH
- So Grouper has permission to manipulate its own groups only?
- Chris Hyzer: need to add to wiki: scope this as you want so Grouper can’t delete things it did not create

- 3rd party apps integrating w Azure,
- U Toronto recommends using SCIM
- Azure would push data out to Grouper
- Next Step: Chad will work on free test accounts within Azure
- Find out what permissions is really needed
- Chris Hyzer can add a note and whatever Chad finds out, will tweak it

More about ABAC

- Syntax for data fields, field =
- but no quote around values,
- can’t take integers, expecting variable names ,
- can't use anything with a space
- Chris Hyzer: it’s one quoted string
- Whole thing is a JEXL script
- Agreed, we to support numbers

**Issue Roundup**

JiRAs

[GRP-4868  
add provisioning tests to remove member without making the member unprovisionable. i.e. not managing members (e.g. google but others too)](https://todos.internet2.edu/browse/GRP-4868)

- [GRP-4867  
  v5 data provider prompts for boolean when it shouldnt](https://todos.internet2.edu/browse/GRP-4867)
- [GRP-4866  
  google mock does not delete memberships](https://todos.internet2.edu/browse/GRP-4866)
- [GRP-4865  
  allow grouper loader to specify active dates on groups / memberships](https://todos.internet2.edu/browse/GRP-4865)
- [GRP-4864  
  caches missing](https://todos.internet2.edu/browse/GRP-4864)
- [GRP-4863  
  add friendly description for parts of abac script](https://todos.internet2.edu/browse/GRP-4863)
- [GRP-4862  
  ChangeLogTempToChangelog: Active PITGroup not found](https://todos.internet2.edu/browse/GRP-4862)
- [GRP-4861  
  LDAP loaders need a way to filter non-person source members](https://todos.internet2.edu/browse/GRP-4861)
- [GRP-4860  
  add a strategy in provisioning to be authoritative for value for memberships](https://todos.internet2.edu/browse/GRP-4860)
- [GRP-4859  
  all provisioners were shown in list (on folder or group) when only some should be shown based on security](https://todos.internet2.edu/browse/GRP-4859)
- [GRP-4858  
  sql provisioner added folder provisionable and the incremental provisioned groups but no members](https://todos.internet2.edu/browse/GRP-4858)
- [GRP-4857  
  start with in sql provisioner can add same column twice](https://todos.internet2.edu/browse/GRP-4857)
- [GRP-4856  
  provisioning subject attribute cache translation that returns nothing evaluates to "null" string sometimes and should be null](https://todos.internet2.edu/browse/GRP-4856)
- [GRP-4855  
  upgrade bouncycastle for vulnerability](https://todos.internet2.edu/browse/GRP-4855)
- [GRP-4854  
  some .d folders are not found in v5 container](https://todos.internet2.edu/browse/GRP-4854)
- [GRP-4853  
  UI add daemon jobs for data providers](https://todos.internet2.edu/browse/GRP-4853)
- [GRP-4852  
  add provisioning group jexl script example](https://todos.internet2.edu/browse/GRP-4852)
- [GRP-4851  
  Provisioning Menu/Dashboard impacted by Sync Logs](https://todos.internet2.edu/browse/GRP-4851)
- [GRP-4850  
  jexl loader add methods entity.memberOfLike() and entity.memberOfRegexp()](https://todos.internet2.edu/browse/GRP-4850)
- [GRP-4849  
  Data provider updates](https://todos.internet2.edu/browse/GRP-4849)

**Wiki Updates**

- [Grouper ABAC script analysis](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548379/Grouper+ABAC+script+analysis)****
- Grouper diagnostics****
- [GrouperShell (gsh)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh)****
- [v4 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)****
- [v4 Upgrade instructions from v4](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549792/v4+Upgrade+instructions+from+v4)****
- [Grouper provisioning custom metadata](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555763/Grouper+provisioning+custom+metadata)****
- [Grouper progress UI with time (lightweight) for long running events](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48794057/Grouper+progress+UI+with+time+lightweight+for+long+running+events)****

**Next Grouper Call**: Wed. August 16, 2023

****

****
