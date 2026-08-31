---
title: "9-October-2024"
space: GrIntDev
pageId: 48793178
version: 13
lastUpdated: 2026-07-19T00:32:51.885Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793178/9-October-2024
---

# **Grouper Call of Oct 9, 2024**

**Attending**

- Chris Hyzer, Penn, Chair
- Vivek Sachdiva, independent
- Shilen Patel, Duke
- Gail Lift, University of Michigan
- Chad Redman, Unicon
- Jim Beard, Unicon
- Kellen Murphy, Univ of Virginia
- Gabor Eszes, Univ of Virginia
- Drew Aschenbrener, Internet2
- Chris Hubing, Internet2
- Emily Eisbruch, Independent

**DISCUSSION**

Chris is working on a [blog on Grouper Deprovisioning](https://docs.google.com/document/d/1HlxnscVhdUgyp5OYg11LCk-8rYRo1ZAq62S34GQisQ0/edit)

****

| **Grouper Releases in past week** |
| --- |
| We are proud to announce the release of Grouper **v4.15.4.** There are no upgrade instructions. There is a serious provisioning issue in 4.15.0 and 4.15.3, if you are on those and use provisioning you should upgrade.  See the release notes: [v4 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)  We are proud to announce the release of Grouper**v5.13.0.** There are two upgrade instructions from v5.12.0. Note, you do not need to use the attribute sqlCacheableGroup for scripted groups anymore.  See the release notes: [v5 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes) |

**AI Chad -**create doc on how to make a confluence heat map

**Grouper at TechEx****[https://events.internet2.edu/website/69276/home/](https://events.internet2.edu/website/69276/home/)**

- Grouper BOF   
  Tuesday 12/10/2024 12:10 pm - 1:25 pm
- Grouper Chronicles: Success with ABAC and Legacy Challenges   
  Wednesday 12/11/2024 9:00 am - 9:50 am   
   (presentation from Univ of Michigan and Univ of Virginia)

************

**Grouper Training [https://incommon.org/academy/grouper-school/](https://incommon.org/academy/grouper-school/)**

- Goal is to have Grouper Training revamped by March 2025
- Leverage materials we have
- Organized for our user population
- Sys Admin, Power user, end user, etc
- Different training paths
- Prework on demand
- In class instructor led
- Essentials, advanced, admin training, sys admin training
- Different modules for each
- **Advanced** training on loader, provisioning, ABAC in**on demand** lessons
- WIll have office hours to ask questions
- Talking to Chris Hubing about efficient way to have VMs available to students
- WIll develop a licensing model

## **Current Work**

**Vivek**

- Setting up provisioner for Adobe
- [Grouper Adobe provisioner developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792592/Grouper+Adobe+provisioner+developer+notes)
- OAuth client credentials
- We are creating separate tables for groups and memberships
- Mapping with columns in table
- Values are stored in table for reference
- There are settings around deleting account and deleting user
- It's an Adobe thing
- There is a recycle bin in Adobe

**Shilen**

- Finalizing the sync for SQL cache group and membership table
- Shilen talked with Chris re performance issues
- Did testing, saw reduction in time
- After first full sync, will only do full sync for groups with membership changes
- This will save time
- 3 indexes were being added, one was not needed, so now only 2 indexes
- Can sort by last time synced
- As things run nothing will get too stale
- Upgrade task to remove attributes
- Leave attribute but remove the assignments
- If we use attribute again in future?
- Think about point in time
- We know when point in time is used from ABAC perspective
- Need to decide do we let people also assign if they want ? not used in ABAC
- Don’t want to maintain flattened for everything for all time
- If done by attribute, might have different metadata
- Some differentiator
- We may be overusing the attribute framework too much
- Reasons to use flattened history other than ABAC
- Need a “will be in group”?
- Shilen and Chris will discuss
- Shilen will make upgrade task
- Upgrade tasks:
- Take recent DDL changes that are in upgrade tasks
- Remove from DDL version
- If you get DDL task before upgrade version
- Does version 5 initialize the upgrade task
- Whatever latest version for DDL also includes upgrade tasks
- Should not be in DDL
- Nice to have as a reference
- For what is the database supposed to look like
- Full DDL will always be there…
- We should have an upgrade task wiki listing what version and what it does
- In release notes refer to this
- Add to the release notes page, in extra column
- Patch levels
- Why does upgrade task daemon run every 30 minutes?
- No reason
- Upgrade task is an important part of upgrading your version
- You need to run this
- Changelog temp cant run until upgrade task runs
- Why not have it run just only at startup?
- Shilen will look to see if overwrite or append?
- Need to make it append
- Running it as a daemon and at startup
- Do we want to make it not a daemon?
- Each task can say if its a startup thing or a daemon thing
- Looking at all the attributes is a startup thing
- Some of the upgrade tasks could be long
- Could take 30 minutes
- Could delay startup
- More confusing ?
- List that in the upgrade steps
- Make this an unscheduled daemon you shouldn’t run, but you can see the logs
- If there is a way to kick off daemon jobs for GSH
- Startup version of upgrade tasks works differently from upgrade version
- Logic is the same
- This does not need to be a daemon anymore
- But keep it as an unscheduled daemon?
- Suggestions: one time scheduled
- Daemon has to start to run a daemon job
- Try to do changelog temps
- Leads to failures
- Should do this before a JVM starts
- Record first 4k of logs in an attribute?
- Easiest thing is to run it thru GSH
- Kick off a command
- But not everybody has access to GSH
- Sometimes you have to bring up a container
- Just need subject source in order to start GSH
- A lot of people want this to be automatic
- No Daemon job
- It will run on startup
- For each upgrade task you can have a log message in an attribute
- Concern: too many JVM messages
- Not a problem
- **AI Chris will work on issue around upgrade task and startup task**
- Chad: documented in v4 to v5 upgrade notes that need to set to zero

**Chris**

- Worked on [GRP-5731  
  Date picker format is mm/dd/yyyy which is rejected](https://grouper.atlassian.net/browse/GRP-5731)

Chad : no way to put an end date on someone

Chris will look at this

- [GRP-5724  
  custom ui redirects do not escape uri's correctly](https://grouper.atlassian.net/browse/GRP-5724)
- Want to redirect to a different page
- You can check the state in Duo
- [GRP-5712  
  honor the oidc external system config for source and subject identifier type (if grouper.ui.authentication.sourceIds sources not otherwise specified)](https://grouper.atlassian.net/browse/GRP-5712)

- [GRP-5711  
  if no data is found from oidc claim in ui authn, log which is trying to be retrieved, and the claim attribute names****](https://grouper.atlassian.net/browse/GRP-5711)
- [GRP-5706  
  delete old provisioning sync data if it is more than 1 week old and no provisioner configured](https://grouper.atlassian.net/browse/GRP-5706)

Chris and Drew will meet later, discuss deprovisioning issue   
Attributes can be assigned to configure things, if we need indirect calculations, use a different table?

**Chad**

- Working on Grouper Training Environment for Nov.
- [GRP-5707  
  Index member_sort_string0_idx slowing down Group.getMembers()](https://grouper.atlassian.net/browse/GRP-5707)
- Chad: Issue with postgress, had to get every membership of a group, getting nested loops
- Postgres was doing full table scan
- Question: But don’t we need index to sort?
- Looking at this as a search field..
- Database will spend more time figuring out the sorting if there is no index
- Need to think about this

## **Issue Roundup**

**Jiras**

- - [GRP-5731  
    Date picker format is mm/dd/yyyy which is rejected](https://grouper.atlassian.net/browse/GRP-5731)
  - [GRP-5730  
    remove entries from changlog_consumer if not a job anymore](https://grouper.atlassian.net/browse/GRP-5730)
  - [GRP-5729  
    Upgrade from V46 to V47 fails, grouper_prov_azure_user already exists](https://grouper.atlassian.net/browse/GRP-5729)
  - [GRP-5728  
    "no longer managed by loader" is appended too much (and maybe unnecessarily?)](https://grouper.atlassian.net/browse/GRP-5728)
  - [GRP-5727  
    loader should truncate group description if too long](https://grouper.atlassian.net/browse/GRP-5727)
  - [GRP-5726  
    GrouperProvisioningJob shows config error and needs to be removed from config](https://grouper.atlassian.net/browse/GRP-5726)
  - [GRP-5725  
    subject provisioning list should be in alpha order](https://grouper.atlassian.net/browse/GRP-5725)
  - [GRP-5724  
    custom ui redirects do not escape uri's correctly](https://grouper.atlassian.net/browse/GRP-5724)
  - [GRP-5723  
    put favicon.ico in container](https://grouper.atlassian.net/browse/GRP-5723)
  - [GRP-5722  
    throw descriptive error if redirect url is not entered for ui oidc](https://grouper.atlassian.net/browse/GRP-5722)
  - [GRP-5721  
    add ability to manage owners with azure](https://grouper.atlassian.net/browse/GRP-5721)
  - [GRP-5720  
    add ability to add custom attributes for scim](https://grouper.atlassian.net/browse/GRP-5720)
  - [GRP-5719  
    full provisioner will insert recent memberships which shouldnt be there, then delete them](https://grouper.atlassian.net/browse/GRP-5719)
  - [GRP-5718  
    error while inserting group in ldap](https://grouper.atlassian.net/browse/GRP-5718)
  - [GRP-5717  
    Populate sql cache group and membership tables for all groups, stems, and attributeDefs](https://grouper.atlassian.net/browse/GRP-5717)
  - [GRP-5716  
    Group internal_id not populated after upgrade, causes sql cache to fail](https://grouper.atlassian.net/browse/GRP-5716)
  - [GRP-5715  
    CHANGE_LOG_consumer_sqlCacheGroup fails when deleting a list name](https://grouper.atlassian.net/browse/GRP-5715)
  - [GRP-5714  
    Old maintenance jobs still scheduled after v5 upgrade](https://grouper.atlassian.net/browse/GRP-5714)
  - [GRP-5713  
    MembershipSave.save() says that it will throw a GroupNotFoundException, but it does not.](https://grouper.atlassian.net/browse/GRP-5713)
  - [GRP-5712  
    honor the oidc external system config for source and subject identifier type (if grouper.ui.authentication.sourceIds sources not otherwise specified)](https://grouper.atlassian.net/browse/GRP-5712)
  - [GRP-5711  
    if no data is found from oidc claim in ui authn, log which is trying to be retrieved, and the claim attribute names](https://grouper.atlassian.net/browse/GRP-5711)
  - [GRP-5710  
    Rules text improvements](https://grouper.atlassian.net/browse/GRP-5710)
  - [GRP-5709  
    Provisioner failsafe does not send notification when tripped](https://grouper.atlassian.net/browse/GRP-5709)
  - [GRP-5708  
    add duo as option for custom UI](https://grouper.atlassian.net/browse/GRP-5708)
  - [GRP-5707  
    Index member_sort_string0_idx slowing down Group.getMembers()](https://grouper.atlassian.net/browse/GRP-5707)
  - [GRP-5706  
    delete old provisioning sync data if it is more than 1 week old and no provisioner configured](https://grouper.atlassian.net/browse/GRP-5706)
  - [GRP-5705  
    consolidate grouperProvisioningDaemon into cleanLogs](https://grouper.atlassian.net/browse/GRP-5705)
  - [GRP-5704  
    add daemon log counts to cleanLogs job](https://grouper.atlassian.net/browse/GRP-5704)
  - [GRP-5703  
    Retry and batching for GcDbAccess](https://grouper.atlassian.net/browse/GRP-5703)
  - [GRP-5702  
    allow groups in abac to be specified by the quoted group name and does not have to be entity.memberOf('a:b:c')](https://grouper.atlassian.net/browse/GRP-5702)
  - [GRP-5701  
    in an abac script if you have ! and whitespace after, it was getting stripped](https://grouper.atlassian.net/browse/GRP-5701)
  - [GRP-5700  
    Code review/refactor to reduce NoSuchElementException and NullPointerException errors](https://grouper.atlassian.net/browse/GRP-5700)
  - [GRP-5699  
    Jexl loader fails if a referenced group doesn't exist](https://grouper.atlassian.net/browse/GRP-5699)
  - [GRP-5698  
    allow custom ui to redirect to a url, without clicking a button](https://grouper.atlassian.net/browse/GRP-5698)
  - [GRP-5697  
    null pointer on deleting reports with no status](https://grouper.atlassian.net/browse/GRP-5697)
  - [GRP-5696  
    provisionable regex should take effect in incremental](https://grouper.atlassian.net/browse/GRP-5696)
  - [GRP-5695  
    loader diagnostics for list of groups cant find group_name col in oracle](https://grouper.atlassian.net/browse/GRP-5695)
  - [GRP-5694  
    veto if group name is same as data field name, or vise versa](https://grouper.atlassian.net/browse/GRP-5694)
  - [GRP-5693  
    ldaptive does not return more than 1500 attribute values in AD](https://grouper.atlassian.net/browse/GRP-5693)
  - [GRP-5692  
    remove unecessary provisioning errors](https://grouper.atlassian.net/browse/GRP-5692)
  - [GRP-5691  
    default provisioning metadata on groups/folder/mships should apply even if there is no non default metadata](https://grouper.atlassian.net/browse/GRP-5691)
  - [GRP-5690  
    default provisioning metadata on entities should apply even if there is no non default metadata](https://grouper.atlassian.net/browse/GRP-5690)
  - [GRP-5689  
    Entity attribute provisioner needs option to not attempt unresolvable subjects](https://grouper.atlassian.net/browse/GRP-5689)
  - [GRP-5688  
    search results for folders should be a long link](https://grouper.atlassian.net/browse/GRP-5688)
  - [GRP-5687  
    show full path of group when listing groups for a user](https://grouper.atlassian.net/browse/GRP-5687)

- - [GRP-5686  
    in_target column is null****](https://grouper.atlassian.net/browse/GRP-5686)

**Wiki Updates**

- [Deprovisioning song and video](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545608/Deprovisioning+song+and+video)
- [Grouper Custom UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549064/Grouper+Custom+UI)
- [Grouper ABAC Crashplan deprovisioning example](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548902/Grouper+ABAC+Crashplan+deprovisioning+example)
- [DDL in Grouper](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548570/DDL+in+Grouper)
- [Grouper Product Roadmap](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541781/Grouper+Product+Roadmap)
- [Grouper custom template via GSH departmental Grouper onboarding](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549899/Grouper+custom+template+via+GSH+departmental+Grouper+onboarding)
- [OIDC authentication to Grouper UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548296/OIDC+authentication+to+Grouper+UI)

**Next Grouper Call: Wed. Oct. 23, 2024**
