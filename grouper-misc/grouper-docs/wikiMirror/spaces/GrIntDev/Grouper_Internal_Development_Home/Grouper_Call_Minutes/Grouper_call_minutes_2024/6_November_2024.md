---
title: "6-November-2024"
space: GrIntDev
pageId: 48793185
version: 20
lastUpdated: 2026-07-19T00:32:52.494Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793185/6-November-2024
---

# **Grouper Call of Nov. 6, 2024**

**Attending**

- Chris Hyzer, Penn, Chair
- Vivek Sachdiva, independent
- Shilen Patel, Duke
- Gail Lift, University of Michigan
- Chad Redman, Unicon
- Bert Bee Lindgren, GA Tech
- Ben Rappleyea, Illinois State U
- Drew Aschenbrener, Internet2
- Chris Hubing, Internet2
- Emily Eisbruch, Independent

## DISCUSSION

Administrivia

- [Internet2 Intellectual Property Policy](https://internet2.edu/community/about-us/policies/internet2-intellectual-property-policy/)
- Review AIs [Grouper Project Action Items (Google Doc)](https://docs.google.com/document/d/1jQCt1nICmVVZsU8iprjbDw0WbmnpUt87NsS7rdKmfMo/edit)
- Agenda bash

**Grouper at TechEx** [https://events.internet2.edu/website/69276/](https://events.internet2.edu/website/69276/)

- Grouper BOF   
  Tuesday Dec. 10, 2024 12:10 pm - 1:25 pm
- Grouper Chronicles: Success with ABAC and Legacy Challenges  
  Wednesday Dec 11, 2024 9:00 am - 9:50 am   
   (presentation from Univ of Michigan and Univ of Virginia)

**Grouper v4.16.0 Release**

- Nov 4, 2024
- We are proud to announce the release of Grouper v4.16.0. There is one upgrade instruction from 4.15.8.
- See the release notes: [v4 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)

**Next Steps from this call (from Zoom AI call summary)**

1. Chris to update the DDL Coding Wiki with new decisions regarding upgrade tasks.

2. Chris to check after startup that the daemon runs for upgrade tasks.

3. Vivek to populate the wiki with upgrade task versions and their applicability to Grouper versions.

4. Chris to focus on implementing incremental ABAC functionality.

5. Chris to send Gail information on how SQL syncs work for incremental data providers.

6. Chris to investigate and implement improvements for handling DNE (Does Not Exist) errors in provisioners.

7. Chris to add retry settings for different error states in provisioners.

8. Chris to reach out to the ABAC mailing list for examples of use cases for managing data rows in Grouper.

**Call Summary from Zoom AI**

The team discussed the progress on various tasks, including the upgrade task, startup task, and the addition of a web service OAuth external system. They also discussed the management of data in Grouper, the challenges of auditing group ads and deletes, and the potential for a custom loader to pick up important departmental data. Lastly, they discussed issues related to provisioning, error handling, and the functionality of their system, with a focus on improving performance and efficiency.

## **Current Projects**

Vivek

- Adobe provisioner work is done. [Grouper Adobe provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555458/Grouper+Adobe+provisioner)
- Chris will test with real environment
- VIvek tested in mock environment
- Also working on **upgrade tasks**
- Used to have only one assignment value for an upgrade task
- Making it a multi -valued attribute
- Goes thru enums in order
- Shows history
- Increasing order, can skip things
- V4 and v5 will be mutually exclusive
- Can go from 29 to 31
- Will populate a wiki on upgrade tasks and what versions
- V4+ or v4 only
- DDL, never incremented
- Will make coding Grouper easier
- AI, Chris update the wiki [DDL in Grouper](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548570/DDL+in+Grouper) to reflect upgrade tasks work
- Vivek will work on task reordering
- Vivek and Chris will discuss
- Chris worked on: [GRP-5796 improve upgrade tasks in grouper](https://grouper.atlassian.net/browse/GRP-5796)
- Chris working on Adobe full cycle config

Shilen

- Converted flattened time stamp to micros from epoc
- [Grouper developers coding standards](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792568/Grouper+developers+coding+standards)
- Tested in 3 databases
- Conversion works well
- Takes about 3 hours in test data from Duke
- Updated DDL for cache membership history table
- There is an upgrade task
- Multiple things changing
- Do we only want to populate objects with a certain marker flag?
- Need to decide
- Want to keep track of why something is
- Either marker flag, keep history
- Or simple table, 2 columns, reason
- Don’t want automatically adding attributes to things, too heavyweight?
- Thanks Shilen for this work around timestamps
- Function library… not ready
- In database, micros from epoch, if we want to convert to a timestamp
- Based on whatever is set up on the client
- Can also have UTC version
- If you haven’t added time zone to image, it will be UTC
- Shilen will look more at this
- Epoch value to timestamp object
- Create version of the function where you pass in the timezone
- Must be supported by the database
- Are we overthinking this?
- Convert micros to timestamp object
- It works out
- Two versions of the funciton, one to UTC and one to
- Session timezone has one function and UTC has another
- Need an offset?
- Shilen will look into this more
- If you have a timestamp object, client is going to display time based on what session says timestamp is
- If you want to display a different value, then change how it’s displayed

Chris

- ABAC will have data rows that do not come from a system of record
- Global data and attributes assigned to users
- Want to imagine a data field editor in Grouper for these situations
- Rich security framework on top of existing security
- On top of attribute level
- You can say who can view read and update
- Depending on the data in the row
- Security based on that
- Tracking manual affiliations that user has with the institution (contractor, spouse)
- Different people can manage different affiliations
- Use case: Need to track things but don’t have a system to do it
- Could ask on ABAC Slack chat
- Good use case for custom loaders?
- Carey: managing tabular data in Grouper could be a complication in use of Grouper
- Take group structures and return to ABAC data table structures
- Feedback to tabular data for ABAC
- Chris: worried that is too fancy
- Chris should perhaps share functional examples of what people need to do at Penn

- Chris is working on provisioning in general at Penn.
- Switched from SAML to OIDC
- Converted various provisioners
- Want to get to v5
- Gail: hoping for incremental ABAC
- Chris: Will focus on that
- 
- Each data field must go through the change log
- Data providers have incrementals
- No need for SQL cache attribute in easy to query repository
- Dependency table
- Where Grouper calculates groups where attributes are dependent on one and other
- Chris working on [GRP-5805](https://grouper.atlassian.net/browse/GRP-5805)

- Adobe provisioner errors when provisionable users do not exist in target
- Extra retry switch for each condition
- In provisioning
- If not an error, don’t retry
- Error split into 2, for full and incremental?

Chad

- Grouper training is next week
- Audit graph work
- Going through audit log is slow
- Going though point in time should be faster
- Idea is to provide more info than is seen in audit log

## Issue Roundup

**Jiras**

- [GRP-5807  
  membership with user not exist is listed as ERR in provisioner](https://grouper.atlassian.net/browse/GRP-5807)
- [GRP-5806  
  provisioning changes can log or have dupes](https://grouper.atlassian.net/browse/GRP-5806)
- [GRP-5805  
  adobe provisioner errors when provisionable users do not exist in target](https://grouper.atlassian.net/browse/GRP-5805)
- [GRP-5804  
  do not show anything that is encrypted in database on screen](https://grouper.atlassian.net/browse/GRP-5804)
- [GRP-5803  
  change text from WS (bearer token or basic authn) to Web service](https://grouper.atlassian.net/browse/GRP-5803)
- [GRP-5802  
  if there are disabled memberships, those should be removed when a composite is added](https://grouper.atlassian.net/browse/GRP-5802)
- [GRP-5801  
  do a rule example that requires an expiration date](https://grouper.atlassian.net/browse/GRP-5801)
- [GRP-5800  
  adjust content type and accept for scim provisioner](https://grouper.atlassian.net/browse/GRP-5800)
- [GRP-5799  
  incremental object log can throw errors for object being null](https://grouper.atlassian.net/browse/GRP-5799)
- [GRP-5798  
  invalid provisioner throws error on group provisioning screen](https://grouper.atlassian.net/browse/GRP-5798)
- [GRP-5797  
  allow testing for oauth external system](https://grouper.atlassian.net/browse/GRP-5797)
- [GRP-5796  
  improve upgrade tasks in grouper](https://grouper.atlassian.net/browse/GRP-5796)
- [GRP-5795  
  GrouperHttpClient should handle UTF bodies](https://grouper.atlassian.net/browse/GRP-5795)
- [GRP-5794  
  adobe provisioner](https://grouper.atlassian.net/browse/GRP-5794)
- [GRP-5793  
  Visualization show rules relationships](https://grouper.atlassian.net/browse/GRP-5793)
- [GRP-5792  
  Recreate grouper_sql_cache_mship_hst table](https://grouper.atlassian.net/browse/GRP-5792)
- [GRP-5791  
  1password in scim does not send itemsPerPage in the last page of users](https://grouper.atlassian.net/browse/GRP-5791)
- [GRP-5790  
  WS find groups with FIND_BY_EXACT_ATTRIBUTE only works with legacy attributes](https://grouper.atlassian.net/browse/GRP-5790)
- [GRP-5789  
  Javadoc and swagger reference FIND_BY_ATTRIBUTE, should be FIND_BY_EXACT_ATTRIBUTE](https://grouper.atlassian.net/browse/GRP-5789)
- [GRP-5788  
  Swagger URLS contain zero-width characters and can't be used directly](https://grouper.atlassian.net/browse/GRP-5788)
- [GRP-5787  
  Notification job fail](https://grouper.atlassian.net/browse/GRP-5787)
- [GRP-5786  
  Sample config for Helm charts](https://grouper.atlassian.net/browse/GRP-5786)
- [GRP-5785  
  Visualization should include all the "cannot VIEW group rectangles" but only show a label as "no access to this group"](https://grouper.atlassian.net/browse/GRP-5785)
- [GRP-5784  
  add test case for azure group display name with multiple caches and matching attributes](https://grouper.atlassian.net/browse/GRP-5784)
- [GRP-5783  
  add select group by id to azure mock](https://grouper.atlassian.net/browse/GRP-5783)
- [GRP-5782  
  duo update user name is not working](https://grouper.atlassian.net/browse/GRP-5782)
- [GRP-5781  
  Convert grouper_sql_cache_mship.flattened_add_timestamp to microseconds from epoch](https://grouper.atlassian.net/browse/GRP-5781)
- [GRP-5778  
  only query policy groups in provisioning if needed and directly, performance improvement](https://grouper.atlassian.net/browse/GRP-5778)
- [GRP-5777  
  Group UI should hide (or support ) Import a list of Privileges when the group UI is in Privileges mode](https://grouper.atlassian.net/browse/GRP-5777)
- [GRP-5776  
  incremental user not in provisionable group in entity attribute](https://grouper.atlassian.net/browse/GRP-5776)

**Wiki pages updated in past two weeks**

- [Grouper Adobe provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555458/Grouper+Adobe+provisioner)
- Grouper external system - Web service - Oauth credential
- [v5 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes)
- [v4 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)
- [DDL in Grouper](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548570/DDL+in+Grouper)
- [v4 Upgrade instructions from v4](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549792/v4+Upgrade+instructions+from+v4)
- [Grouper provisioning SCIM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555423/Grouper+provisioning+SCIM)
- Recovering a deleted group
- [Grouper deprovisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544732/Grouper+deprovisioning)
- [Grouper provisioning caching](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555196/Grouper+provisioning+caching)
- Grouper Training Environment - text to copy and paste - 201.4
- [Grouper provisioning entity attribute testing](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554183/Grouper+provisioning+entity+attribute+testing)
- [Grouper provisioning caching](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555196/Grouper+provisioning+caching)Oct 29, 2024 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555196/Grouper+provisioning+caching)
- [Grouper rules pattern - Veto if new membership is not a group or in certain subject sources](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554971/Grouper+rules+pattern+-+Veto+if+new+membership+is+not+a+group+or+in+certain+subject+sources)
- [Grouper rules pattern - Veto delete membership if immediate membership has attribute value](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554213/Grouper+rules+pattern+-+Veto+delete+membership+if+immediate+membership+has+attribute+value)

**Next Grouper Call:** Wed. Nov 20, 2024
