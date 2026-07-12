---
title: "20-November-2024"
space: GrIntDev
pageId: 48793199
version: 11
lastUpdated: 2026-07-12T17:27:29.655Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793199/20-November-2024
---

# **Grouper Call of Nov. 20, 2024**

**Attending**

- Chris Hyzer, Penn, Chair
- Vivek Sachdiva, independent
- Shilen Patel, Duke
- Gail Lift, University of Michigan
- Chad Redman, Unicon
- Matt Black, Purdue
- Tushar Walaskar, Weber State U
- Ben Rappleyea, Illinois State U
- Drew Aschenbrener, Internet2
- Chris Hubing, Internet2
- Emily Eisbruch, Independent

## DISCUSSION

Administrivia

- [Internet2 Intellectual Property Policy](https://internet2.edu/community/about-us/policies/internet2-intellectual-property-policy/)
- Review AIs [Grouper Project Action Items (Google Doc)](https://docs.google.com/document/d/1jQCt1nICmVVZsU8iprjbDw0WbmnpUt87NsS7rdKmfMo/edit)
- **Agenda bash**

## ***Call Summary from Zoom AI***

- *The team discussed changes to the Jira project, including the creation of issues and the addition of the 'guest' group to the permission scheme.*
- *They also addressed issues related to database connection problems, the concept of a "substantial change" in their system, and the complexities of parsing and running scripts on a large scale.*
- *Lastly, they discussed the progress of the project, including the resolution of issues with the Adobe provisioner and the content type for the SCIM provisioner*

## ***Call Next Steps from Zoom AI***

1. *Chris Hyzer to update the wiki with documentation on upgrade tasks and DDL handling.*
2. *Chris Hyzer to implement content type option for SCIM provisioner in the next release.*
3. *Chris Hyzer to fix the issue with Google Groups provisioner in the next release.*
4. *Chris Hyzer to resolve the SQL query issue reported by Tushar in the next release.*
5. *Vivek and Chris Hyzer to investigate and implement a solution for creating functions within full install SQL files.*
6. *Shilen to continue work on the SQL cache history feature, including the new full task daemon and incremental updates.*
7. *Chris Hyzer to finalize and release Grouper v5.0.*
8. *Chris Hyzer to review and consider implementing Chad's UI customization changes in Grouper v5.0.*
9. *Chris Hyzer to investigate and implement a solution for handling complex ABAC dependencies and incremental processin*g.

**Change to JIRA Permissions**

- updated privileges on JIRA project
- create issues change
- Posted in slack channel, we changed who is allowed to create Jiras
- We were getting anonymous spam JIRAs
- Right now only devs and admins
- Should be guests also
- Drew will update the permissions so grouper project users (guests) can create issues
- Guests group will map to a Grouper Project Role that Drew is setting up
- Guests can also edit the wiki
- Chris will reply to Graham on this

**Grouper at TechEx******

- **Grouper BOF**  
  Tuesday Dec. 10, 2024 12:10 pm - 1:25 pm
- **Grouper Chronicles: Success with ABAC and Legacy Challenges**  
  Wednesday Dec 11, 2024 9:00 am - 9:50 am   
   (presentation from Univ of Michigan and Univ of Virginia)

## **Current Work**

**Vivek**

- [Grouper upgrade tasks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks) , Work finished, Tested
- Some files were too large
- For every upgrade task there is now a separate file
- Upgrade tasks are now not done via daemons on schedule basis
- Can run via UI
- Disable chron job, so upgrade jobs do not run automatically
- Pending item: for functions within upgrade tasks, looking into why we can’t create function from build sql files
- Shilen looked at this issue
- The full SQL file uses semicolon as a delimiter, there are semicolons in functions,
- This is causing issues
- Views will be in full sql file, then need to add functions
- Run upgrade task before DDL?
- How to programa\tically look at this and know when the end of it is?
- Semicolon on its own line?
- If running SQL thru a prompt, you can specify what the delimiter is, (2 semicolons?)
- That will work
- AI Chris and Vivek will look at upgrade SQL semicolon issue and suggest a solution
- Suggested transient solution
- GCDBX works fine
- But adding to SQL file is the issue due to semicolon
- Nice to not have to set new delimiter
- Wiki [Grouper upgrade tasks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)
- Some people want to manually do DDL, they do not set the auto DDL to be configured for a version they have. If you have an upgrade task, need all the SQL in DDL in the JIRA so people can do that manually
- Some of the upgrades change the timestamp
- If it’s just add a column and do the SQL it’s hard to intermix w JAVA, gets complicated
- Would need SQL script
- Carey: Possible approach: have a stack of DDL files in the image for those who want to do manually , Use a programmatic approach?
- Does not work unless you have Grouper schema, would not work in Oracle for example
- Chris will clarify the approach in the [DDL in Grouper](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548570/DDL+in+Grouper) wiki
- 
- When Grouper starts, if whole DDL was just run, then there is no upgrade
- Looked at all DDL versions that overlap w upgrade tasks and removed them
- Some upgrade tasks assume databases will add certain columns
- Gail: At U-Michigan there are some database connection problems
- Go back and fix up stuff and move on
- Problems require individual solutions
- Seems the suggested approach is the right approach
- Question: how does something qualify as a substantial change
- 15 substantial but what about 16?
- Chris Hyzer: it’s a gut feelin
- Vivek also worked on loader settings
- AI Chris or Vivek: will **add descriptions** to the upgrade tasks table wiki
- Need to tie everything together.

**Shilen**

**S*QL Cache History and Dependencies (from the Zoom AI)***

*Shilen discussed his work on the SQL Cache history, including the addition of new tables for dependency and dependency type tables.*

*Chris Hyzer elaborated on the reasons for caching membership history and the use of attributes for institutions to specify which objects should have history. They also discussed the need for a full sync for the history table and the creation of a new daemon to populate the SQL Cache history table. Chris also mentioned his work on real-time back changes and the need for transactional updates in the change log. The team also discussed the challenges of creating dependencies for incremental changes, particularly when dealing with complex policies.*

- Upgrade task around functions
- SQL cache history
- Some prereqs
- Adding new tables for dependencies
- Got DDL for that added
- Added a marker attribute for every field (about 22)
- Attributes can later be assigned to objects to indicate they would have history
- Created a veto hook
- Added a check config to populate cache dependency type table with three values
- Next step: start with full sync for the history table, create a new daemon to populate SQL cache history table based on dependency table.
- Chris explains:
- It’s about caching membership history
- Grouper wants to cache point in time history for memberships and reasons an institution will want to do that
- We can add an ABAC function for a grace period, or to do something with history
- Chris working on real time ABAC changes
- Making sure all data changes go thru the change log
- Instead of actual values putting IDs of things
- Can add more data later
- Tracks which groups are related to which policies.
- Full sync for history table

- Shilen: next, full task daemon, then will do the incremental
- Full daemon will
- Question: For an object kept in history, do we keep membership history from beginning, or is there a cutoff?
- Chris: don’t need from the beginning probably, but not sure how to configure.
- For data fields, specify how long the history
- ABAC knows how much history to keep, but no column in the database for that
- For the manually assigned, we can have a value (number of days) on the attribute assignment). If not set, then keep history forever.
- Usually don’t need more than 2 years
- Default to 2 years
- Add a feature later

**Chris**

***Parsing Scripts and Change Log Processor (from Zoom AI)***

*Chris Hyzer discussed the complexities of parsing and running scripts on a large scale, particularly in relation to the change log processor and the incremental for groups.*

*He proposed a dependency table to track which groups and policies are affected by changes, and suggested a system where the change log processor could run a script to determine if a policy is applicable to a group.*

*Chris also discussed the potential for over-processing and the need for efficient performance.*

*Gail suggested the possibility of using a test bed for certain scenarios, and Chris agreed to explore this further.*

*The team also discussed the need for inputs in the script, with Gail suggesting that efficiency was more important than the method of evaluation.*

- Did change log for data field changes
- When a data provider changes assignments
- Needs to be batched up
- Collecting the ABAC scripts and running a change thru all at once, then marking which groups need changes as a result?
- This informs which ABAC scripts to look at.
- Makes a better approximation
- We have learned our current database has too many columns and tables
- Need to make dependencies clearer
- Chris will make unit tests
- Shilen: if you have ABAC script that depends on a group versus depends on a data field, those are 2 different types?
- Yes
- Chris will work more on this
- We don’t want to miss things, but we need to pare things down to improve performance
- Gail suggested test bed
- Interesting use case around time of day, start and end time for door access
- University of Michigan use case: on certain dates, everyone’s status changes

Chris worked on several Jiras, inluding

- [GRP-5834  
  add change log events to data field changes](https://todos.internet2.edu/browse/GRP-5834)
- [GRP-5836  
  cannot delete jexl loaded population script setting](https://todos.internet2.edu/browse/GRP-5836)
- [GRP-5831  
  current ABAC only should return non-group](https://todos.internet2.edu/browse/GRP-5831)s
- [GRP-5811  
  adobe throttles with two jsons in the body](https://todos.internet2.edu/browse/GRP-5811)

**Chad**

- Client is waiting for Content Type for SCIM provisioner
- Another customer is waiting for [GRP-5657](https://todos.internet2.edu/browse/GRP-5657)

- AI Chris will work on [GRP-5657](https://todos.internet2.edu/browse/GRP-5657)

Can't delete group due to dependency on grouper_sync_dep_group_group

Chad working on data dictionary issue

**Big Query Issue**

- Tushar Walaskar, Weber State U  
   question on bigger queries and error around order by group name and inverted character, need semi colon, comma causing error.
- Chris suggests not using “Group by”
- AI Chris will look into issue from Tushar Walaskar, Weber State U around long query
- Chris made a jira for this
- [GRP-5838](https://todos.internet2.edu/browse/GRP-5838) long query for tushar with list of groups
- Chris will configure Tushar in COmanage so he can create Jiras

## **Issue Roundup**

**JIRAs**

****

**Wiki updates in past 2 weeks**

- [v4 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)

- [Grouper upgrade tasks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)
- [GrouperShell (gsh)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh)
- [How to use the Grouper wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541532/How+to+use+the+Grouper+wiki)
- [Grouper attribute framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework)
- [API Building & Configuration](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544006/API+Building+Configuration)
- [Community Contributions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541859/Community+Contributions)
- [Portland State University Grouper Page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543104/Portland+State+University+Grouper+Page)
- [Rochester Institute of Technology Grouper Page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543424/Rochester+Institute+of+Technology+Grouper+Page)
- Grouper Training Environment - text to copy and paste - 301.8 - GSH templates
- Grouper Training Environment - text to copy and paste - 301.5 - notificationsNov 14, 2024 • updated by Chad Redman • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544798/Grouper+Training+-+Use+cases+-+Lesson+07+Notifications)
- [Grouper daily email notification](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549403/Grouper+daily+email+notification)
- [Grouper upgrade task](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)s
- [Grouper Training Environmen](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541839/Grouper+Training+Environment)t
- [Understanding Grouper](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543452/Understanding+Grouper)
- [Grouper upgrade task](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)
- [Automating one-time commands with GSH and SQL](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547354/Automating+one-time+commands+with+GSH+and+SQL)
- [Grouper Adobe provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555458/Grouper+Adobe+provisioner)
- Grouper external system - Web service - Oauth credential

**Next Grouper Call**: Wed: December 4, 2024

****
