---
title: "29-January-2025"
space: GrIntDev
pageId: 48793313
version: 11
lastUpdated: 2026-07-19T00:33:03.282Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793313/29-January-2025
---

# **Grouper Call of Jan. 29, 2025**

**Attending**

- Chris Hyzer, Penn, Chair
- Shilen Patel, Duke
- Vivek Sachdeva , Independent
- Chad Redman, Unicon
- Jim Beard, Unicon
- Matt Black, Purdue
- Gail Lift, University of Michigan
- David Hutchins, Univ of Virginia
- Michael Gettes, SLAC
- Ben Rappleyea, Illinois State U
- Drew Aschenbrener, Internet2

## **DISCUSSION**

**Administrivia**

- [Internet2 Intellectual Property Policy](https://internet2.edu/community/about-us/policies/internet2-intellectual-property-policy/)
- Review AIs [Grouper Project Action Items (Google Doc)](https://docs.google.com/document/d/1jQCt1nICmVVZsU8iprjbDw0WbmnpUt87NsS7rdKmfMo/edit)
- Agenda bash

**Call Summary and Next step (partly based on Zoom AI)**

- The group discussed database optimization, new features, and user interface enhancements.
- They addressed issues with existing services, such as SCIM provisioner and GSH templates, and explored potential solutions for better configuration and access management.
- They also discussed documentation updates, troubleshooting efforts, and future development plans to enhance the overall user experience and system performance.

Next Steps

- Chris Hyzer to contact AWS about paging capabilities for SCIM.
- Vivek to add one more option for membership strategy in SCIM provisioner.
- Chris Hyzer to consider adding a copy button or popup window option for demon logs.
- Chad to continue troubleshooting and potentially fix the issue with DNA memberships in provisioning jobs.
- Chris Hyzer to investigate why LDAP command logging is not working when turned on.
- Chris Hyzer to update the UI for daemon logs by moving log ID (renamed to job ID) and host to the right side of the display.
- Michael to look into potential indexing issues for the loader log table if Chris provides the query.
- Chad to continue working on training videos and AWS integration for the template.
- Drew to assist Chad with next steps on the template project when ready.

**CURRENT WORK**

**Vivek**

- **Optimization work**; Reducing database calls
- Previously syncs were often one by one in loops
- Now committed and pushed, in latest release
- Should see significant performance improvement
- **SCIM provisioner**, working on membership strategy
- Different targets in SCIM support different retrieval methods
- Started w AWS and GITHUB, then there were more SCIM based targets
- Still some variability, this option helps
- Michael G: need doc on what can be set for each application environment
- Each endpoint should have its own wiki
- Hope for a generic page with GSH methods, when you run with an external system, name strategy, etc.
- SCIM : looks simple but many complications
- Need better paging Capabilities
- Jan. 29, 2025 - AI Chris reach out to AWS re SCIM paging capabilities .
- AWS appears to be leading an effort to go through IETF to **standardize paging** in SCIM along with some other capabilities. Michael G was at Identiverse last year and tried to connect Chris with them.
- There are different ways to change memberships
- Need to make configurable the max number of numbers in a patch, and add this to testing
- SCIM works well at smaller scale
- At scale, perhaps let go of a full recalc sync with SCIM
- Vivek working on rules, improving description text,
- Effort to improve Rules UI
- [Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)
- Chris asked about Microsoft Entra ID. It was formerly known as Azure Active Directory (Azure AD)

Shilen

- Finished ABAC for membership history work
- New install on all 3 databases
- Oracle and MySQL had minor issues, Shilen fixed
- **Data row fields history**
- Shilen is working on data row fields history

- Goal: grace periods for data fields on ABAC

- There’s concept of point in time, there’s concept of history
- There are nested memberships in group sets
- Same for priv
- Point in time for all those relationships and direct memberships
- Resource intensive to query
- Need to look at timeline graph
- Was the person in the group at this time?
- History has flattened info, simple to query
- Don't have this problem as much for data fields
- Just 3 main tables
- Will have simple history table that is lightweight to store the user, internal ID, start and end time etc.
- Will be lightweight
- Jan 29, 2025 - AI Chris Chris will verify for Data Row Fields work, that there are warnings and confirmation screens before deleting data field changes are implemented
- Dependency table should help
- Flattened must go thru change log temp
- This approach does not require going thru change log temp

**Chris**

Chris working on [GRP-5976](https://grouper.atlassian.net/browse/GRP-5976) Can't list GSH templates if any of them are missing the run folder/group

- Relationship between running a template and needing a folder for it was discussed
- Use a deeplink
- Should be able to favorite a template
- Folders are less locked town in v5?
- Drew will check this out
- If you have run on a template and you send a deeplink…
- This is being changed / corrected in v5
- Moving towards don’t need a folder to run a template
- Better to depend on deeplinks
- If you run a template that does not have a folder
- Folder is mostly for breadcrumbs?
- Jan 29, 2025 AI Chris - think about handling of templates, folders, deeplinks, breadcrumbs, quicklinks
- Discussion of service container based API gateway to act as abstraction layer between SCIM and API provisioning suite. See Zac Adams note on Slack

**Chad**

- Working on Grouper training videos
- Log ID in UI Logs, is not useful, can't search on it, It’s in Daemon logs
- Suggestion to move the Log ID to last or get rid of it
- Chris captured these suggestions in JIRA [GRP-5944](https://grouper.atlassian.net/browse/GRP-5944) Move daemon log column "Log ID" column to the end

**Improving Provisioning Interface and Workflow (from Zoom AI)**

- Chris, Chad, and Gail discussed the need for a more user-friendly interface for managing provisioning.
- They considered the addition of a copy button and the option to open a new window for output, acknowledging that some users might not be familiar with using a text editor.
- They also discussed the potential for a hierarchical list in a dropdown menu for better organization. Chris mentioned that Vivek is working on a solution to improve the process, and Chad is troubleshooting an issue with the membership wrapper.
- They agreed to further discuss the idea of a copy button and the option to open a new window.

**LDAP Command Logging and UI Improvements (from Zoom AI)**

- Chris Hyzer discussed issues with the LDAP command logging and the resolution of a problem with a large-scale provisioner at Georgia Tech.
- He suggested that adding a second search match attribute on the target could help with similar issues.
- Michael mentioned that the Damon logs take a while to load, and Chris suggested looking into the query and indexes.

## **Issue Roundup**

Wiki updates

- [Grouper container documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation)Jan 27, 2025 • updated by jim.beard.2@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation)
- [Grouper upgrade tasks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)Jan 24, 2025 • updated by Shilen Patel (duke.edu) • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)
- [Grouper ABAC with scripted groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups)Jan 23, 2025 • updated by Shilen Patel (duke.edu) • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups)
- [v5 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes)Jan 23, 2025 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes)
- [v4 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)Jan 23, 2025 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)
- [Grouper database - Postgres](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555261/Grouper+database+-+Postgres)Jan 23, 2025 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555261/Grouper+database+-+Postgres)
- [Grouper LDAP provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554930/Grouper+LDAP+provisioner)Jan 16, 2025 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554930/Grouper+LDAP+provisioner)
- Grouper Documents & Presentations  
  Jan 17, 2025 • updated by Emily Eisbruch (internet2.edu) • view change
- [Grouper documentation pages to update](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792957/Grouper+documentation+pages+to+update)Jan 17, 2025 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792957/Grouper+documentation+pages+to+update)

**JIRAs**

[GRP-5976  
Can't list GSH templates if any of them are missing the run folder/group](https://grouper.atlassian.net/browse/GRP-5976)

- [GRP-5975  
  clarify restriction of ability to add every entity to memberships and privileges](https://grouper.atlassian.net/browse/GRP-5975)
- [GRP-5974  
  oracle install fails](https://grouper.atlassian.net/browse/GRP-5974)
- [GRP-5973  
  SCIM PATCH Error for ServiceNow](https://grouper.atlassian.net/browse/GRP-5973)
- [GRP-5972  
  DDL for data field history](https://grouper.atlassian.net/browse/GRP-5972)
- [GRP-5971  
  cannot run scripted group without GSH template type](https://grouper.atlassian.net/browse/GRP-5971)
- [GRP-5970  
  take out sys out print of upgrade tasks for unit tests](https://grouper.atlassian.net/browse/GRP-5970)
- [GRP-5969  
  provisioner can select users who are provisionable in another provisioner](https://grouper.atlassian.net/browse/GRP-5969)
- [GRP-5968  
  in scim search for users/groups based on what framework asks for](https://grouper.atlassian.net/browse/GRP-5968)
- [GRP-5967  
  rename scim start with "aws" to "generic"](https://grouper.atlassian.net/browse/GRP-5967)
- [GRP-5966  
  mysql install fails](https://grouper.atlassian.net/browse/GRP-5966)
- [GRP-5965  
  show data field/row assignments on subject screen](https://grouper.atlassian.net/browse/GRP-5965)
- [GRP-5964  
  disabled date membership rule not working in ui](https://grouper.atlassian.net/browse/GRP-5964)
- [GRP-5963  
  batch scim insert memberships and retry each if batch fails](https://grouper.atlassian.net/browse/GRP-5963)
- [GRP-5962  
  this should not be re-used: grouperUi.composite.useThread](https://grouper.atlassian.net/browse/GRP-5962)
- [GRP-5961  
  visualization with user gives error](https://grouper.atlassian.net/browse/GRP-5961)
- [GRP-5960  
  scim qualified user setting should work for formatted name](https://grouper.atlassian.net/browse/GRP-5960)
- [GRP-5959  
  text properties reuse common words instead of creating new properties](https://grouper.atlassian.net/browse/GRP-5959)
- [GRP-5958  
  support subheaders in template v2 dropdowns](https://grouper.atlassian.net/browse/GRP-5958)
- [GRP-5957  
  Upgrade 2.4.0 => 4.17.3 : missing grouper_sync_dep_group_group relationship](https://grouper.atlassian.net/browse/GRP-5957)
- [GRP-5956  
  scim provisioner cannot select all memberships](https://grouper.atlassian.net/browse/GRP-5956)
- [GRP-5955  
  Google provisioner reports some memberships as ERR, but no logging to troubleshoot](https://grouper.atlassian.net/browse/GRP-5955)
- [GRP-5954  
  recursion error with getAttributeDelegate](https://grouper.atlassian.net/browse/GRP-5954)
- [GRP-5953  
  provisioner with canRetrieveAllData query returns duplicate entities from other provisioners](https://grouper.atlassian.net/browse/GRP-5953)
- [GRP-5952  
  Optimize data sync - Use batch inserts/queries/deletes](https://grouper.atlassian.net/browse/GRP-5952)
- [GRP-5951  
  if there are multiple inherited privileges, rules UI should print all in table](https://grouper.atlassian.net/browse/GRP-5951)
- [GRP-5950  
  for inherited privileges the then part in the table should show the subject assigned to](https://grouper.atlassian.net/browse/GRP-5950)
- [GRP-5949  
  add foreign key on grouper data alias to rows like fields](https://grouper.atlassian.net/browse/GRP-5949)
- [GRP-5948  
  Open daemon log output in popup window](https://grouper.atlassian.net/browse/GRP-5948)
- [GRP-5947  
  recent member of abac](https://grouper.atlassian.net/browse/GRP-5947)
- [GRP-5946  
  ldap command logging does not turn on](https://grouper.atlassian.net/browse/GRP-5946)
- [GRP-5945  
  if log verbose count is 50, the provisioning log file gets truncated](https://grouper.atlassian.net/browse/GRP-5945)
- [GRP-5944  
  Move daemon log column "Log ID" column to the end](https://grouper.atlassian.net/browse/GRP-5944)
- [GRP-5943  
  add provisioning option for command logging for changes only](https://grouper.atlassian.net/browse/GRP-5943)
- [GRP-5942  
  cannot delete group after deleting rule](https://grouper.atlassian.net/browse/GRP-5942)
- [GRP-5941  
  add rule pattern for send email when membership invalid due to group](https://grouper.atlassian.net/browse/GRP-5941)
- [GRP-5940  
  condition of "veto if not group" rule should display the sources](https://grouper.atlassian.net/browse/GRP-5940)
- [GRP-5939  
  if you select g:gsa in veto if not group rule, and edit it again, then it selects other sources](https://grouper.atlassian.net/browse/GRP-5939)
- [GRP-5938  
  veto if not group rule does not check g:gsa by default](https://grouper.atlassian.net/browse/GRP-5938)
- [GRP-5936  
  clarify that the rule source ids are what is allowed](https://grouper.atlassian.net/browse/GRP-5936)
- [GRP-5935  
  rename "PIT history chart" to some less jargon-y](https://grouper.atlassian.net/browse/GRP-5935)
- [GRP-5934  
  Google missing users should be DNE not ERR](https://grouper.atlassian.net/browse/GRP-5934)
- [GRP-5933  
  Provisioning error handling types should show code on label](https://grouper.atlassian.net/browse/GRP-5933)
- [GRP-5932  
  add error options to keep provisioning errors but not mark job as error](https://grouper.atlassian.net/browse/GRP-5932)

**Next Grouper Call**: Wed. Feb 12, 2025

****
