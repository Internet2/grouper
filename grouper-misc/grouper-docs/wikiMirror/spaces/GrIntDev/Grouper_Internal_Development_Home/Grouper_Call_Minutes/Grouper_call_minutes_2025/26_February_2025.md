---
title: "26-February-2025"
space: GrIntDev
pageId: 48793268
version: 12
lastUpdated: 2026-07-19T00:33:00.416Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793268/26-February-2025
---

# **Grouper Call of Feb. 26, 2025**

**Attending**

- Chris Hyzer, Penn, Chair
- Shilen Patel, Duke
- Chad Redman, Unicon
- Jim Beard, Unicon
- Matt Black, Purdue
- Bert Bee-Lindgren, Georgia Tech
- Dusty Edenfield, Georgia Tech
- Chris Hubing, Internet2
- Emily EIsbruch, Independent

## **DISCUSSION**

**Administrivia**

- [Internet2 Intellectual Property Policy](https://internet2.edu/community/about-us/policies/internet2-intellectual-property-policy/)
- Review AIs [Grouper Project Action Items (Google Doc)](https://docs.google.com/document/d/1jQCt1nICmVVZsU8iprjbDw0WbmnpUt87NsS7rdKmfMo/edit)
- Agenda bash

****

**Grouper Documentation Blog**

Blog on the Grouper Documentation Improvement effort led by Noelette Stout is published here: [https://incommon.org/news/community-collaborates-to-enhance-grouper-documentation/](https://incommon.org/news/community-collaborates-to-enhance-grouper-documentation/)

**Call Summary - from Zoom AI**

The team discussed the implementation of changes to the data field and row assign history tables, including the need for a UI configuration for rows on whether history should be kept or not. They also discussed configuration for deletion after a specified number of days, and the implications of stopping an instance. Additionally, they discussed the upcoming Grouper training and VMs, proposed a new data field subject source system, and considered the potential switch from an LDAP subject source to an SQL one.

**Next Steps - from Zoom AI**

1. Chris Hyzer to develop a proposal for migrating to data field subject sources and share it with the team for review.
2. Shilen to implement populating the data row assign history table and configure UI options for keeping history on rows.
3. Chad to abstract the user data for Grouper Training VMs from the launch template and put it into the Git repository.
4. Chris Hubing to push updates to the repo for the user data with changes to region and account hardcoding.
5. Chris Hyzer to propose changes to the privilege visibility model on the mailing list for team feedback.
6. Chris Hyzer to investigate adding wildcard functionality for group selection in ABAC scripts.
7. Bert and Dusty to begin planning migration of their loader jobs to use ABAC for delegation purposes.
8. Chris Hyzer to look into improving performance of Jexl script loading for Georgia Tech's use case.
9. Chris Hyzer to work on implementing an incremental update feature for Jexl scripts.

**Current Work**

Shilen

- Fixed issue with queries returning multiple of same column name
- Fixed issues causing data field provider from working
- Chris and Shilen discussed **changing DDL for data row history**
- Shilen made changes as decided with Chris
- Tested on all 3 databases
- Added an upgrade step so it will delete unneeded info from history
- Shilen had a question re UI options to configure whether history will be kept for data fields.
- Currently for data fields, you can specify number of days to keep history
- Can’t do that for rows
- What if you want to keep history for a row but not for certain columns
- Add UI config for rows on whether history should be kept or not
- Don’t look at config for fields, just for row
- Select if it’s a row column or just an attribute
- History input should not be shown if it’s a row column
- For a field, pick if it’s a row column
- If you pick it’s a row column then you should not be able to configure history settings
- This is part of the metadata wizard, Chris can help Shilen with this
- Shilen will get history populated
- WIll need a full sync daemon to delete after certain number of days
- 
- Shilen did a few additional fixes in the provider

****

Chad

- Working on videos for Grouper Training
- Admin section of Grouper Training now has much new content: GSH templates, web services, etc
- Changes to built-in GTE?
- Steps in wiki are to create VMs 
  
  - That page can go away
  - We are no longer creating VMs
- For On Demand VM , which version of Grouper gets created?
- When it launches it will have current version
- Chris Hubing suggests re new user data: Use the word Terminate (not Pause)
- Have another option to Pause?
- Had decided not to have pause option, could still add pause if we want
- AI for Chad: reword GSH template for creating VM: to say Create and Terminate, or Create and Delete
- Some people in training might not like the AWS terminology
- Postgress is within Docker container
- When you delete a running container the data is gone
- Deleting the AWS instance gets rid of the data
- What number of hours for a course?
- Chris Hyzer: each lesson is stand alone
- Hope students spin up and start clean for each lesson

****

Chris Hyzer

- **How to handle migration / switch to new Subject Source ?**

- - Have a different subject source ID?
  - Shilen : allow it to be renamed in the database?
  - Worry about so much tying into subject source ID
  - How to configure both, and then flip the switch?
  - Disable SQL (PennPersonOld) and enable new?
  - Could there be mapping of subject source ID?
  - Shilen: I would do migration in my test environment
  - New subject source would be named same as the old
  - No two subject sources in production
  - Chris Hyzer: should be a 2 step process
  - Carey: Data Load first, recache data in new data source
  - Then switch subject source to point to internal data cache
  - Bert: Bring the attribute resolvers into Grouper, then start creating new PennPerson, then map everything to a subject source that looks like PennPerson, then Grouper can diagnostically compare new PennPerson to oldPerson, then switch to new if happy with diagnostics. It’s essentially renaming.
  - Two upgrade paths?
  - Is it OK to insist new subject source be defined in the database?
  - Is there a config file version of that?
  - Do you have to get subject source into database first?
  - Bert suggests: All subject config to be in database.
  - Disabling can be a file edit
  - Carey: needs to be hierarchical , database usually wins
  - Chris Hyzer suggests: assume you are in database
  - After migration put everything into file
  - Export the UI built configuration and put it back in a file
  - Most people use database configs
  - AI Chris will develop a proposal on new subject source and migration
- Shilen: not related to migration…but … when we create identities in IAM we immediately add them to groups, will there be implications? Need real time
- Chris hoping there are fewer queries and can be more real time
- Hoping for incremental that will put it in there very quickly
- Shilen: Needs to be immediate
- Invoke a Grouper Reader.. Or use web service call
- To get it into the state it needs to be
- Subject source design should support idea of knowing what data providers feed it
- But then subject source does external calls in rare cases
- Worry: some subjects might never be resolved
- Incremental loading is triggered externally,
- Change log table
- Could make it check change log table every 5 seconds
- Change log rows bookmark gets kept, timestamp based
- Way to inject identity into the data store
- Inserting into change log could accomplish that, but it’s 3 layers from where Shilen wants it to be
- Shilen: as long as there is a way of knowing that when this call finishes it will be made available,
- Chris Hyzer: incrementals have daemon checking the tables
- Web service using same logic

**Georgia Tech Issues**

- **Jexl script loading is taking a long time**

- 3600 groups using Jexl loading
- Taking 12 hours to run through those
- If someone adds a member to a component of a JEXL group in middle of JEXL full sync it must wait for another full run of JEXL to get loaded
- Solution: **Use incremental**
- Chris Hyzer: there are plans to address this : a dependency table will be used
- Issue around **admin privileges**

**Advanced User Provisioning for Groups (from Zoom AI)**

Bert discussed the need for advanced users to be able to roll up groups quickly, as part of a backloading effort to delegate loading to non-Sys Admins. Chris Hyzer and Shilen clarified that the focus was on provisioning group names, not memberships.   
Chris suggested that attributes could be made for each department to control access to policy groups, and that multiple groups could be included in the format.

Bert: will focus on ABAC solution.

Privileges:

- - - Chris Hyzer asks: Is it controversial to allow readers of a group see the privileges of a group?
    - They can already see the memberships.
    - Thoughts: this would be a significant change…
    - Could create a priv reading group

****

## **Issue Roundup**

**Jiras**

- [GRP-6035  
  Swagger for GSH template missing /gshTemplateExec from URL](https://grouper.atlassian.net/browse/GRP-6035)
- [GRP-6034  
  Upgrade from 4.14.1 to 4.16.0 caused mass deletions in Active Directory](https://grouper.atlassian.net/browse/GRP-6034)
- [GRP-6033  
  For limited users, folder menu shows Reports under the Templates category](https://grouper.atlassian.net/browse/GRP-6033)
- [GRP-6032  
  Update data row ddl and temporarily stop history](https://grouper.atlassian.net/browse/GRP-6032)
- [GRP-6031  
  non admins should be able to edit loaders for abac](https://grouper.atlassian.net/browse/GRP-6031)
- [GRP-6030  
  grouperClient add option to use EasySslSocketFactory](https://grouper.atlassian.net/browse/GRP-6030)
- [GRP-6029  
  oauth http credentials can get cached indefinitely](https://grouper.atlassian.net/browse/GRP-6029)
- [GRP-6028  
  scripted groups should be labeled as such](https://grouper.atlassian.net/browse/GRP-6028)
- [GRP-6027  
  Problems with rule 'Veto if new membership is not a group or in certain subject sources'](https://grouper.atlassian.net/browse/GRP-6027)
- [GRP-6026  
  Move external system property patterns out of methods so classes can be extended](https://grouper.atlassian.net/browse/GRP-6026)
- [GRP-6025  
  TeamDynamix needs throttling](https://grouper.atlassian.net/browse/GRP-6025)
- [GRP-6024  
  Make the Miscellanous link on the left expand a submenu for misc veatures](https://grouper.atlassian.net/browse/GRP-6024)
- [GRP-6023  
  grouper_data_row_field_asgn_v has incorrect value for data_row_internal_id](https://grouper.atlassian.net/browse/GRP-6023)
- [GRP-6022  
  Add sftp capability to group reports, so it doesn't needs a separate daemon job just for that](https://grouper.atlassian.net/browse/GRP-6022)
- [GRP-6021  
  Combo picker sometimes appears far from the associated field](https://grouper.atlassian.net/browse/GRP-6021)
- [GRP-6020  
  Report actions dropdown should allow to edit](https://grouper.atlassian.net/browse/GRP-6020)
- [GRP-6019  
  Add miscellaneous UI item for reports](https://grouper.atlassian.net/browse/GRP-6019)
- [GRP-6018  
  Group reports description says "SQL reports" when can also do GSH reports](https://grouper.atlassian.net/browse/GRP-6018)

****

**Wiki updates**

- [Grouper upgrade tasks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)Feb 21, 2025 • updated by Shilen Patel (duke.edu) • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)
- [Grouper Packaging and Versioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544489/Grouper+Packaging+and+Versioning)Feb 17, 2025 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544489/Grouper+Packaging+and+Versioning)
- [v4 Upgrade instructions from v4](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549792/v4+Upgrade+instructions+from+v4)Feb 16, 2025 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549792/v4+Upgrade+instructions+from+v4)
- [Pac4j Plugin for Built-in Single Sign-on (SSO)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549858/Pac4j+Plugin+for+Built-in+Single+Sign-on+SSO)Feb 14, 2025 • updated by Chad Redman • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549858/Pac4j+Plugin+for+Built-in+Single+Sign-on+SSO)
- [Grouper deprovisioning report](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548487/Grouper+deprovisioning+report)Feb 14, 2025 • updated by Gail Lift (umich.edu) • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548487/Grouper+deprovisioning+report)
- [Grouper overall summary administrative report](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545058/Grouper+overall+summary+administrative+report)Feb 13, 2025 • updated by Chad Redman • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545058/Grouper+overall+summary+administrative+report)
- [Grouper SQL interface](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544522/Grouper+SQL+interface)Feb 12, 2025 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544522/Grouper+SQL+interface)
- [Grouper ABAC with scripted groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups)Feb 12, 2025 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups)
- Grouper Documents & Presentations7 minutes ago • updated by Emily Eisbruch (internet2.edu) • view change
- [Grouper documentation pages to update](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792957/Grouper+documentation+pages+to+update)10 minutes ago • updated by Emily Eisbruch (internet2.edu) • [view change](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792957/Grouper+documentation+pages+to+update)
- [Grouper Training Environment developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793119/Grouper+Training+Environment+developer+notes)Feb 13, 2025 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793119/Grouper+Training+Environment+developer+notes)
- [Grouper developers coding standards](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792568/Grouper+developers+coding+standards)Feb 12, 2025 • updated by Emily Eisbruch (internet2.edu) • [view change](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792568/Grouper+developers+coding+standards)
- [Grouper developers coding standards](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792568/Grouper+developers+coding+standards)Feb 12, 2025 • updated by chris.hyzer.3@example.com • [view change](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792568/Grouper+developers+coding+standards)
- [Grouper style guide](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792966/Grouper+style+guide)Feb 07, 2025 • updated by Chad Redman • [view chang](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792966/Grouper+style+guide)

**Next Grouper Call: Wed. March 11, 2025**

****
