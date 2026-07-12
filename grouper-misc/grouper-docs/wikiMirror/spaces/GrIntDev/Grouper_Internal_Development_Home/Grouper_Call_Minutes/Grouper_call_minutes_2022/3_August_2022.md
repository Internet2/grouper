---
title: "3-August-2022"
space: GrIntDev
pageId: 48793933
version: 8
lastUpdated: 2026-07-12T17:03:44.518Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793933/3-August-2022
---

# **Grouper Call of August 3, 2022**

**Attending**

- Chris Hyzer, Penn, Chair
- Shilen Patel, Duke
- JJ, Unicon
- Carey Matt Black, Purdue
- Drew Aschenbrener, Internet2
- Emily Eisbruch, Internet2

## **DISCUSSION**

**Administrivia**

- [Internet2 Intellectual Property Policy](https://internet2.edu/community/about-us/policies/internet2-intellectual-property-policy/)
- Review AIs [Grouper Project Action Items (Google Doc)](https://docs.google.com/document/d/1jQCt1nICmVVZsU8iprjbDw0WbmnpUt87NsS7rdKmfMo/edit)
- Agenda Bash

**MIsc items**

- Thanks to Jason R. and Princeton for this Princeton Grouper contribution [Princeton University Grouper Page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543485/Princeton+University+Grouper+Page)
- Emily submitted Grouper BOF request for TechEx in Denver in December 2022
- Emily working on Grouper doc improvements
- Grouper Training coming up Sept 27-30, 2022 [https://incommon.org/academy/grouper-school/](https://incommon.org/academy/grouper-school/)

**Plan for next Grouper release**

- Users being able to see provisioning
- Hierarchies for groups and entities
- Debug object logs, currently people can’t check
- Chris added another column to grouper loader log

## **Work Items**

Vivek

- Azure and google provisioning
- [Grouper Entra ID Provisioner (Current) Azure O365](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555567/Grouper+Entra+ID+Provisioner+Current+Azure+O365)
- [Grouper Google GCP provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554610/Grouper+Google+GCP+provisioner)
- Leveraged work from NYU
- Concept of priv list on a group for admins or managers
- At first only full sync
- Ideally list column in sync membership table
- Users being able to see provisioning config and make assignments
- Also ability to view provisioning config, but not able to assign
- If a person has View-only priv for a group, that person should not be able to make the group provisionable.
- If you are a provisioning admin, you should only have access to the groups you can read?
- Azure and LDAP are different cases,
- AI Vivek make JIRA for another priv on provisioning, Read, Assign, Admin, Manage
- Right now only sys admins can do re calcs
- Diagnostics, making it simpler

Shilen

- Subject change daemon, real time USDU
- [Subject change daemon](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554199/Subject+change+daemon)
- 
- It issues entity recalcs, not working properly, Chris is looking at that
- JIRA for recalc issue [GRP-4251](https://todos.internet2.edu/browse/GRP-4251) entity recalc by message not working
- Issues found at University of Michigan, primary issue is resolved
- Web services not compiling. CI tests also failing,
- AI Chris will help fix this web services no compiling
- Want to Upgrade hibernate, but some needed functionality was removed
- Would eventually like to remove hibernate altogether
- Could be a task for Shilen
- Go to Hibernate 6? NO, has Java dependency
- Want to remove vulnerabilities in 3rd party jars
- Should we roll back into Grouper 2.5? To be decided
- Some ability to view what groups you are in and respect privacy of other members
- Drew had reasonable proposal
- There are cases where you don’t want someone to know they are a group
- What about trace membership?
- Want to have ability to show to a subject groups they are in without showing then other members of the group
- Shilen will implement Drew’s suggestion
- Keep Drew posted on how this develops

Chris

- See Jiras

JJ

- working with U Wisconsin on provisioning.
- Updating to Grouper 2.6 will solve some of their issues
- For Another client, not U Wisc: 
  
  - AI Chris will look at issue JJ reported : LDAP provisioner , running thru “starts with”, name is flat reverse, name limit 64, the config it fails, because LDAP DN is not being generated. The CN is translated properly, but DN comes back as null.

## **Issue Roundup**

**Jiras in past two weeks**

- [GRP-4250  
  when validating matching attributes and data look for any matching attribute and past or present value](https://todos.internet2.edu/browse/GRP-4250)
- [GRP-4249  
  provisioning: when retrieving groups, and seeing if they are retrieved, compare with String search value](https://todos.internet2.edu/browse/GRP-4249)
- [GRP-4248  
  provisioning: remove from groupOnly list if selecting groupMemberships and type is groupAttributes](https://todos.internet2.edu/browse/GRP-4248)
- [GRP-4247  
  provisioning: do not copy to list of select groupOnly if selecting groupMemberships and type is gruopAttributes](https://todos.internet2.edu/browse/GRP-4247)
- [GRP-4246  
  compute search and matching attributes once in provisioning workflow](https://todos.internet2.edu/browse/GRP-4246)
- [GRP-4245  
  update documentation for group attribute edit](https://todos.internet2.edu/browse/GRP-4245)
- [GRP-4244  
  add deepEquals() method to remove groups/entities/memberships retrieved from dao in case there are dupes](https://todos.internet2.edu/browse/GRP-4244)
- [GRP-4243  
  update provisioning matching](https://todos.internet2.edu/browse/GRP-4243)
- [GRP-4242  
  add a new provisioning validation for multiple matches: MAT](https://todos.internet2.edu/browse/GRP-4242)
- [GRP-4241  
  move provisioning link from administration to miscellaneous](https://todos.internet2.edu/browse/GRP-4241)
- [GRP-4240  
  provisioning: improve documentation for translations](https://todos.internet2.edu/browse/GRP-4240)
- [GRP-4239  
  provisioning: add caches as fields to auto translate from for groups and entities](https://todos.internet2.edu/browse/GRP-4239)
- [GRP-4238  
  document group and entity link for ldap](https://todos.internet2.edu/browse/GRP-4238)
- [GRP-4237  
  document the ldap attribute name and translation type at the field level for groups and entities (normal and create only)](https://todos.internet2.edu/browse/GRP-4237)
- [GRP-4236  
  provisioning: add documentation for entity DN and translation](https://todos.internet2.edu/browse/GRP-4236)
- [GRP-4235  
  provisioning: add documentation for group DN and translation](https://todos.internet2.edu/browse/GRP-4235)
- [GRP-4234  
  have clickable provisioning error messages to go to element with error](https://todos.internet2.edu/browse/GRP-4234)
- [GRP-4233  
  v2.6.13 Unable to turn off provisioning for a provisioner configured as DN Override only](https://todos.internet2.edu/browse/GRP-4233)
- [GRP-4232  
  add clob message column for grouper_loader_log](https://todos.internet2.edu/browse/GRP-4232)
- [GRP-4231  
  add immutable id to search for entities in azure provisioning](https://todos.internet2.edu/browse/GRP-4231)
- [GRP-4230  
  upgrade bouncecastle bcprov-jdk15on for security vulnerability](https://todos.internet2.edu/browse/GRP-4230)
- [GRP-4229  
  upgrade httpclient for security vulnerability](https://todos.internet2.edu/browse/GRP-4229)
- [GRP-4228  
  upgrade wss4j for security vulnerability](https://todos.internet2.edu/browse/GRP-4228)
- [GRP-4227  
  upgrade postgres driver for security vulnerability](https://todos.internet2.edu/browse/GRP-4227)
- [GRP-4226  
  upgrade jackson-databind for security vulnerability](https://todos.internet2.edu/browse/GRP-4226)
- [GRP-4225  
  upgrade xerces library for security vulnerability](https://todos.internet2.edu/browse/GRP-4225)
- [GRP-4224  
  upgrade gson library for security vulnerability](https://todos.internet2.edu/browse/GRP-4224)
- [GRP-4223  
  upgrade to latest csrfguard](https://todos.internet2.edu/browse/GRP-4223)
- [GRP-4222  
  remove struts from parent pom](https://todos.internet2.edu/browse/GRP-4222)
- [GRP-4221  
  upgrade hibernate and migrate to criteria queries](https://todos.internet2.edu/browse/GRP-4221)
- [GRP-4220  
  grouper container only chown files that need chowning](https://todos.internet2.edu/browse/GRP-4220)
- [GRP-4219  
  grouper container should not read files to variables, should sed as file](https://todos.internet2.edu/browse/GRP-4219)
- [GRP-4218  
  RabbitMQ configuring password.elconfig via grouper.messaging doesn't work](https://todos.internet2.edu/browse/GRP-4218)
- [GRP-4217  
  add button to UI to remove sync data and/or cache data](https://todos.internet2.edu/browse/GRP-4217)
- [GRP-4216  
  duo incremental daemon throws errors](https://todos.internet2.edu/browse/GRP-4216)
- [GRP-4215  
  ignore matching id issues with deleted groups](https://todos.internet2.edu/browse/GRP-4215)
- [GRP-4214  
  provisioner type should be readonly after selecting it, since switching doesnt work too well](https://todos.internet2.edu/browse/GRP-4214)
- [GRP-4213  
  remove provisioning config: allowBlankMatchingIds](https://todos.internet2.edu/browse/GRP-4213)
- [GRP-4212  
  installer was deleting a log4j jar since it thought it conflicted](https://todos.internet2.edu/browse/GRP-4212)
- [GRP-4211  
  log4j core is missing from container](https://todos.internet2.edu/browse/GRP-4211)
- [GRP-4210  
  cant view daemon logs](https://todos.internet2.edu/browse/GRP-4210)
- [GRP-4209  
  upgrade jackson json jars](https://todos.internet2.edu/browse/GRP-4209)
- [GRP-4208  
  upgrade bounceycastle jar](https://todos.internet2.edu/browse/GRP-4208)
- [GRP-4207  
  upgrade httpclient jar](https://todos.internet2.edu/browse/GRP-4207)
- [GRP-4206  
  upgrade junit library](https://todos.internet2.edu/browse/GRP-4206)
- [GRP-4205  
  upgrade xerces libary](https://todos.internet2.edu/browse/GRP-4205)
- [GRP-4204  
  upgrade rabbitmq to fix vulnerabilities](https://todos.internet2.edu/browse/GRP-4204)
- [GRP-4203  
  do not rely as much on a single matchingId in the framework (e.g. in wrapper or index)](https://todos.internet2.edu/browse/GRP-4203)
- [GRP-4202  
  provisioning track recalc of groups/entities vs membershipsOfGroups/membershipsOfEntities differently](https://todos.internet2.edu/browse/GRP-4202)
- [GRP-4201  
  in provisioning have separate recalc flag](https://todos.internet2.edu/browse/GRP-4201).
- [GRP-4199  
  azure provisioner edit group name gives error: Parameter securityEnabledOnly must be valid Boolean value.](https://todos.internet2.edu/browse/GRP-4199)
- [GRP-4198  
  clear validation problems at beginning of provisioning run](https://todos.internet2.edu/browse/GRP-4198)
- [GRP-4197  
  "apply filter" on daemon logs screen should refresh the drop down (so can run daemon is was running but not anymore)](https://todos.internet2.edu/browse/GRP-4197)
- [GRP-4196  
  provisioning azure user accountEnabled null pointer](https://todos.internet2.edu/browse/GRP-4196)
- [GRP-4195  
  validate that provisioning memberships have a group and user (if membership objects)](https://todos.internet2.edu/browse/GRP-4195)
- [GRP-4194  
  provisioning, do not require matching ID for objects to be inserted (might be assigned in target)](https://todos.internet2.edu/browse/GRP-4194)
- [GRP-4193  
  remove provisioning config: allowBlankMatchingIds](https://todos.internet2.edu/browse/GRP-4193)
- [GRP-4192  
  provisioning membership translation should throw error if group or user has error](https://todos.internet2.edu/browse/GRP-4192)
- [GRP-4191  
  if cannot create entity, then dont proceed](https://todos.internet2.edu/browse/GRP-4191)
- [GRP-4190  
  azure group types do not translate to group properties](https://todos.internet2.edu/browse/GRP-4190)
- [GRP-4189  
  add http body to debug log](https://todos.internet2.edu/browse/GRP-4189)
- [GRP-4188  
  log http params in debug log for http client](https://todos.internet2.edu/browse/GRP-4188)
- [GRP-4187  
  make sure dont need to be root to assign provisionable](https://todos.internet2.edu/browse/GRP-4187)
- [GRP-4186  
  make ddlutils not a dependency](https://todos.internet2.edu/browse/GRP-4186)
- [GRP-4185  
  implement various search attributes in azure group dao (and other provisioners)](https://todos.internet2.edu/browse/GRP-4185)
- [GRP-4184  
  if cannot create group, then dont proceed](https://todos.internet2.edu/browse/GRP-4184)
- [GRP-4183  
  provisioning azure is not searching correctly](https://todos.internet2.edu/browse/GRP-4183)
- [GRP-4182  
  null pointer in provisioning matching](https://todos.internet2.edu/browse/GRP-4182)
- [GRP-4181  
  if metadata is edited on a group which is not yet provisioned, allow the edit](https://todos.internet2.edu/browse/GRP-4181)
- [GRP-4180  
  azure provisioning error (mail enabled)](https://todos.internet2.edu/browse/GRP-4180)
- [GRP-4179  
  azure provisioning incremental throws exception](https://todos.internet2.edu/browse/GRP-4179)
- [GRP-4178  
  add daemon links from provisioners to full or incremental. and back](https://todos.internet2.edu/browse/GRP-4178)
- [GRP-4177  
  on group provisioning screen should be able to pull drop down next to provisioner and edit](https://todos.internet2.edu/browse/GRP-4177)
- [GRP-4176  
  add unique id to the daemon logs for provisioning](https://todos.internet2.edu/browse/GRP-4176)
- [GRP-4175  
  azure provisioning, when assigning provisionable, and selecting the group type metadata, the drop down for provisionable changes from true to false](https://todos.internet2.edu/browse/GRP-4175)
- [GRP-4174  
  for provisioning like azure, search and matching with uuid, if not exist create](https://todos.internet2.edu/browse/GRP-4174)
- [GRP-4173  
  for azure provisioning, allow a metadata with drop down to pick a group to be the owner list in azure](https://todos.internet2.edu/browse/GRP-4173)
- [GRP-4172  
  change grouper client from xml to json](https://todos.internet2.edu/browse/GRP-4172)
- [GRP-4171  
  Subject change daemon](https://todos.internet2.edu/browse/GRP-4171)
- [GRP-4170  
  Create rule to copy newly added member to another group](https://todos.internet2.edu/browse/GRP-4170)
- [GRP-4169  
  fix vulnerabilities identified in maven central for grouperClient](https://todos.internet2.edu/browse/GRP-4169)
- [GRP-4168  
  clicking entity -> provisioning gives error of no open session](https://todos.internet2.edu/browse/GRP-4168)
- [GRP-4167  
  fix counts on provisioning screen](https://todos.internet2.edu/browse/GRP-4167)
- [GRP-4166  
  Attribute framework should use database constraints to ensure single assign attributes aren't multi assigned](https://todos.internet2.edu/browse/GRP-4166)
- [GRP-4165  
  each wizard section could have "back to top" link](https://todos.internet2.edu/browse/GRP-4165)

**Grouper Emails in past two weeks**

****none

**Grouper wiki updates in past two weeks**

- [Daemon configuration](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547876/Daemon+configuration)
- [Grouper provisioning framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544760/Grouper+provisioning+framework)
- [v2.6 Upgrade Instructions from v2.6](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793415/v2.6+Upgrade+Instructions+from+v2.6)
- [Grouper provisioning matching](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554238/Grouper+provisioning+matching)
- [Princeton University Grouper Page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543485/Princeton+University+Grouper+Page)
- [New employees group](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543989/New+employees+group)
- [Container update process](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544532/Container+update+process)
- [Application performance monitoring](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544682/Application+performance+monitoring)
- [DDL in Grouper v2.5+](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548570/DDL+in+Grouper)
- [Grouper Google GCP provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554610/Grouper+Google+GCP+provisioner)
- [v2.6 Release Notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793558/v2.6+renamed+to+v4+Release+Notes)
- [Grouper Messaging with RabbitMQ](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548652/Grouper+Messaging+with+RabbitMQ)
- [DDL in Grouper v2.5+](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548570/DDL+in+Grouper)
- Grouper container documentation for v2.5
- [Grouper LDAP provisioning with DN override](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28559953/Grouper+LDAP+provisioning+with+DN+override)
- [Grouper Azure provisioner (new provisioning framework)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555567/Grouper+Entra+ID+Provisioner+Current+Azure+O365)
- [Release steps for new container build](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48794029/Release+steps+for+new+container+build)
- [Build new grouper client](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793024/Build+new+grouper+client)
- [Grouper Product Roadmap](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541781/Grouper+Product+Roadmap)
- [Versioning & Support Policy](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544481/Versioning+Support+Policy)
- v2.6 new features
- [Grouper Messaging System](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544824/Grouper+messaging+system)

**Next Grouper Call**: Wed. Aug 17, 2022
