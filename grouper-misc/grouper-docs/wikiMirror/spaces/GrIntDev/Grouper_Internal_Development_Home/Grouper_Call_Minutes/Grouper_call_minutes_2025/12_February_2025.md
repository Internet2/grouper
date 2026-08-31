---
title: "12-February-2025"
space: GrIntDev
pageId: 48793231
version: 17
lastUpdated: 2026-07-19T00:32:57.014Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793231/12-February-2025
---

# **Grouper Call of Feb. 12, 2025**

**Attending**

- Chris Hyzer, Penn, Chair
- Shilen Patel, Duke
- Vivek Sachdeva , Independent
- Chad Redman, Unicon
- Jim Beard, Unicon
- Matt Black, Purdue
- Gail Lift, University of Michigan
- Bert Bee-Lindgren, Georgia Tech
- Chris Hubing, Internet2
- Drew Aschenbrener, Internet2
- Emily EIsbruch, Independent

## **DISCUSSION**

**Administrivia**

- [Internet2 Intellectual Property Policy](https://internet2.edu/community/about-us/policies/internet2-intellectual-property-policy/)
- Review AIs [Grouper Project Action Items (Google Doc)](https://docs.google.com/document/d/1jQCt1nICmVVZsU8iprjbDw0WbmnpUt87NsS7rdKmfMo/edit)
- Agenda bash

**Blog on Grouper Documentation Effort**

- A Grouper blog on the Grouper Doc Improvement effort led by Noelette Stout has been written and should be published in late Feb.

**Call Summary and Next step (partly based on Zoom AI)**

The meeting included a demonstration of AI-assisted script writing,   
There were discussions about data field assignments and history tables.   
The team explored the potential use of AI for script generation and data analysis, while also addressing issues related to rules, AWS integration, and OAuth processes. There were discussions about optimizing group deletions, handling empty strings in the database, and resolving problems with the GSH template and Azure provisioner.

**Next Steps (from Zoom call summary)**

1. Chris Hyzer to review and adjust Vivek's data field assignments screen implementation.

2. Shilen to create a Jira for detecting and handling duplicate column names in SQL queries.

3. Chris Hyzer to implement the fix for shortening expiration dates in overlapping rules.

4. Chris Hyzer to fix the Azure provisioner issue with multiple group matches.

5. Chris Hyzer to implement batching and transactions for data provider operations.

6. Chris Hyzer to update the GSH template to handle source IP address retrieval without relying on the request object.

7. Chris Hyzer and team to resolve the debate on storing null values vs. empty strings in attribute records.

8. Gail to review and update queries that may be inserting unnecessary null values.

9. Chris Hyzer to create upgrade instructions for the OAuth2 body post change.

10. Chris Hyzer to continue work on the custom UI page login functionality.

## DISCUSSION

**Demo of Using AI to create ABAC scripts**

- Chris showed demo on generating ABAC scripts
- proof of concept using Github copilot (using business license)
- SQL generates HTML from data dictionary
- Sample query: ABAC for people in dental school who are temps
- Hit enter, it suggests abacDentTemp
- AI does good job with starting scripts
- We can share a starting script
- Having github copilot know where to look for code will be helpful
- Grouping LOVs can be helpful
- Putting out samples of Jexl for doing queries is helpful
- Using AI for queries is even better
- Making it easier to get to prompts and build ABAC
- This can help with delegation
- Github uses Eclipse
- Chad has used ChatGPT to answer some Grouper user queries
- TechEx presentation on rapid deployment, from William and Mary, tools looking at merge requests, could be of interest
- [https://internet2.app.box.com/v/techex24slides/folder/301831211100](https://internet2.app.box.com/v/techex24slides/folder/301831211100)

[https://grouper.atlassian.net/browse/GRP-5932](https://grouper.atlassian.net/browse/GRP-5932)

**Zoom AI summary on Script Writing and Data**  
Chris Hyzer discussed the potential of using AI for script writing, suggesting that it could be a useful tool for power users.   
He also mentioned the possibility of publishing a single file for others to use.   
Vivek then presented a work-in-progress feature called data field assignment, which allows users to view their assigned attributes and data rows.   
Gail suggested that this feature could be useful for troubleshooting issues with data results.   
Bert and Chris clarified that the top view shows attributes assigned to the user, while the bottom view shows rows assigned to the user.   
Chris also explained that multiple tables could be created for different types of data, such as training data, payroll data, and alumni data.   
The team agreed that this feature has the potential to be a powerful data investigation tool.

## **Current Work**

Vivek

- new option called **data field assignment**
- All data field configs for a user
- Based on this user’s privileges
- Shows as many tables as there are data roles
- Goal is to show what assignments a user has
- Useful if using Jexl and it’s not getting expected results
- This can help explain things that are questioned in the results
- This is a the start of a powerful data investigation tool
- Could do same at row level
- Can see who is a member of a group
- Query tool that provides sample data

Shilen

**History Table Population and Querying** (from Zoom AI summary)  
Shilen discussed the progress on adding code to populate history tables for data fields, rows, and row fields. He identified two bugs: one related to deleting a row and causing foreign key errors, and another related to mapping results from a select query with columns of the same names. Shilen fixed the bugs and planned to create a Jira for the latter issue. Chris Hyzer and Bert discussed the potential for querying history tables, with Chris suggesting the possibility of storing current states in the history table for easier querying. They also discussed the idea of storing snapshots of the whole row every time a row field is deleted or added. The team agreed to proceed with the current approach and adjust it later if necessary.

[https://grouper.atlassian.net/browse/GRP-5932](https://grouper.atlassian.net/browse/GRP-5932)

Chad

- Created 17 jiras, fixed one
- Chad has been creating videos for upcoming Grouper Training
- Much related to rules
- Would be helpful if rules were more self documenting
- Wiki documentation on rules needs some work
- Chad created Demo on integrating Grouper with AWS
- Chris Hubing and Drew noted the Internet2 team is leveraging this

**Rules and AWS Integration Challenges** (from Zoom summary)  
Chris Hyzer and Chad discussed issues with the rules in their system, with Chad noting that many of the problems he encountered were related to the rules. Chris suggested that some of these issues might be due to the system not being updated to the latest version, and Chad confirmed that he was using an older version. They also discussed the need for better documentation and the potential for using Java annotations to improve this. Chad also mentioned his work on integrating AWS with grouper, which Chris expressed excitement about. Chris Hubing and Drew confirmed that they were making progress with this integration, but noted that they were still working on some details.

**Course Registration and AWS Management**(from Zoom summary)  
Chad demonstrated a system that allows course administrators to register students for courses, which then sets up their registration account.   
He also showed how to manage AWS instances, including starting and stopping them, and how the system stores instance details and passwords securely. Chris Hyzer expressed interest in using AI for future GSH templates.   
The team also discussed the integration of AWS with Grouper, which Chad acknowledged took some investigation to get working.   
Shilen asked how AWS was being called from the GSH template, to which Chad responded that it was done using the Java SDK.

- Discussed these Jiras:
- [GRP-5994  
  Add property rules.membershipDisabledDateDoNotExtend](https://grouper.atlassian.net/browse/GRP-5994)
- [GRP-5986  
  Azure with multiple matching groups gives error "Searched for 1 but retrieved 0 maybe a config is off?"](https://grouper.atlassian.net/browse/GRP-5986)

Chris

- Worked on multiple Jiras including:
  
  - [GRP-6006  
    batch up data field assigns (and rows and columns)](https://grouper.atlassian.net/browse/GRP-6006)
  - [GRP-6004  
    gsh templates can access source IP address via api and not request object which could be out of scope in thread](https://grouper.atlassian.net/browse/GRP-6004)

[https://grouper.atlassian.net/browse/GRP-5932](https://grouper.atlassian.net/browse/GRP-5932)

**System Improvements and Issue Discussion (from Zoom Ai)**  
Chris Hyzer discussed several improvements and issues related to their system. He mentioned that they are working on optimizing the deletion of groups and memberships, and are considering moving to GCDP access for better transaction handling. Chris also addressed a problem with the GSH template, which was causing issues with the source IP address. He suggested a solution to this problem, which involves storing the source IP address in the object model. Chris also discussed the issue of empty strings in the database, suggesting that they could be replaced with null values. Lastly, he mentioned a problem with the OAuth process, specifically with Red Hat SSO, which now requires the OAuth information to be in the body of the post instead of the URL.

[https://grouper.atlassian.net/browse/GRP-5932](https://grouper.atlassian.net/browse/GRP-5932)

## **Issue Roundup**

### **Wiki updates**

- [Grouper SQL interface](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544522/Grouper+SQL+interface)about 8 hours ago • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544522/Grouper+SQL+interface)

- [Grouper ABAC with scripted groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups)about 12 hours ago • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups)

- [v4 Upgrade instructions from v4](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549792/v4+Upgrade+instructions+from+v4)Feb 07, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549792/v4+Upgrade+instructions+from+v4)

- [v5 Upgrade instructions from v5](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549165/v5+Upgrade+instructions+from+v5)Feb 07, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549165/v5+Upgrade+instructions+from+v5)

- [Grouper MidPoint provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555467/Grouper+MidPoint+provisioner)Feb 07, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555467/Grouper+MidPoint+provisioner)

- [v5 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes)Feb 07, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes)

- [Grouper provisioning SCIM for AWS](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564269/Grouper+provisioning+SCIM+for+AWS)Feb 02, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564269/Grouper+provisioning+SCIM+for+AWS)

- [Grouper container institutional images](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554290/Grouper+container+institutional+images)Jan 31, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554290/Grouper+container+institutional+images)

### **Jiras******

| ****[**GRP-6017********add total count to data provider jobs (rows)**](https://grouper.atlassian.net/browse/GRP-6017) |
| --- |

[https://grouper.atlassian.net/browse/GRP-5932](https://grouper.atlassian.net/browse/GRP-5932)

| - ****[**GRP-6016********Okta Provisioner should ignore entities or memberships that are missing an entity attribute set as required******](https://grouper.atlassian.net/browse/GRP-6016) - ****[**GRP-6015********On Group Delete Okta Provisioner should delete it instead of memberships first******](https://grouper.atlassian.net/browse/GRP-6015) - ****[**GRP-6014********Allow Okta Incremental Daemon To Process Entities if Required Entity Attribute Is Populated Instead of Wating for the Full Sync******](https://grouper.atlassian.net/browse/GRP-6014) - ****[**GRP-6013********Daemon logs change last updated timestamp when log or counts aren't changing******](https://grouper.atlassian.net/browse/GRP-6013) - ****[**GRP-6012********Document that entityAttributeResolverSql__{attr} attributes need to be lowercase******](https://grouper.atlassian.net/browse/GRP-6012) - ****[**GRP-6011********Filter groups from okta for group type OKTA_GROUP******](https://grouper.atlassian.net/browse/GRP-6011) - ****[**GRP-6010********Fix create Function syntax for Oracle******](https://grouper.atlassian.net/browse/GRP-6010) - ****[**GRP-6009********increase member subject batch size from 80 to 450******](https://grouper.atlassian.net/browse/GRP-6009) - ****[**GRP-6008********batch up abac subject lookups and member object inserts******](https://grouper.atlassian.net/browse/GRP-6008) - ****[**GRP-6007********batch up grouper dictionary retrieves and assigns******](https://grouper.atlassian.net/browse/GRP-6007) - ****[**GRP-6006********batch up data field assigns (and rows and columns)******](https://grouper.atlassian.net/browse/GRP-6006) - [**GRP-6005********error with abac and partial row**](https://grouper.atlassian.net/browse/GRP-6005)       - [**GRP-6004********gsh templates can access source IP address via api and not request object which could be out of scope in thread******](https://grouper.atlassian.net/browse/GRP-6004) - [**GRP-6003********request is not in scope for externalized text while running in thread******](https://grouper.atlassian.net/browse/GRP-6003) - [**GRP-6002********for data field abac sql providers, a null should not store a field value (especially for rows)******](https://grouper.atlassian.net/browse/GRP-6002) - [**GRP-6001********make scope in oauth web service external system optional******](https://grouper.atlassian.net/browse/GRP-6001) - ****[**GRP-6000********null pointer on connectionBean hides underlying exception******](https://grouper.atlassian.net/browse/GRP-6000) - ****[**GRP-5999********Rule pattern "Veto if not eligible due to group" does not run in daemon******](https://grouper.atlassian.net/browse/GRP-5999) - ****[**GRP-5998********grouper loader ws properties regex is incorrect - minor issue******](https://grouper.atlassian.net/browse/GRP-5998) - ****[**GRP-5997********HookVeto reasonKeys don't get replaced with text in the UI******](https://grouper.atlassian.net/browse/GRP-5997) - ****[**GRP-5996********Removing group members shows summary of failure without messages******](https://grouper.atlassian.net/browse/GRP-5996) - ****[**GRP-5995********add option in web service external system to send parameters in body instead of url (default true)******](https://grouper.atlassian.net/browse/GRP-5995) - ****[**GRP-5994********Add property rules.membershipDisabledDateDoNotExtend******](https://grouper.atlassian.net/browse/GRP-5994) - ****[**GRP-5993********Issues deleting data rows******](https://grouper.atlassian.net/browse/GRP-5993) - ****[**GRP-5992********Populate data field history tables******](https://grouper.atlassian.net/browse/GRP-5992) - ****[**GRP-5991********Improvements to documentation for rules******](https://grouper.atlassian.net/browse/GRP-5991) - ****[**GRP-5990********Add RuleUtil method membershipDisabledDateDays()******](https://grouper.atlassian.net/browse/GRP-5990) - ****[**GRP-5989********New installs include the jdbc example in subject.properties******](https://grouper.atlassian.net/browse/GRP-5989) - ****[**GRP-5988********Rule ThenEnum option to run a gsh script******](https://grouper.atlassian.net/browse/GRP-5988) - ****[**GRP-5987********Can't add custom rule to group in UI******](https://grouper.atlassian.net/browse/GRP-5987) - ****[**GRP-5986********Azure with multiple matching groups gives error "Searched for 1 but retrieved 0 maybe a config is off?"******](https://grouper.atlassian.net/browse/GRP-5986) - ****[**GRP-5985********allow custom ui pages to be anonymous******](https://grouper.atlassian.net/browse/GRP-5985) - ****[**GRP-5984********re-order scim settings to be in the appropriate section******](https://grouper.atlassian.net/browse/GRP-5984) - ****[**GRP-5983********add scim provisioning membership strategies******](https://grouper.atlassian.net/browse/GRP-5983) - ****[**GRP-5982********provisioners cannot load data to table (non adobe)******](https://grouper.atlassian.net/browse/GRP-5982) - ****[**GRP-5981********Provisioner UI log should show instanceId (at least once) even with debug objects off******](https://grouper.atlassian.net/browse/GRP-5981) - ****[**GRP-5980********Provisioner debug log shows final debugMap in UI message but not in log file******](https://grouper.atlassian.net/browse/GRP-5980) - ****[**GRP-5979********Google external system add hints to field descriptions and/or wiki******](https://grouper.atlassian.net/browse/GRP-5979) - ****[**GRP-5978********Provisioner "Debug log" option doesn't do much besides log memory usage******](https://grouper.atlassian.net/browse/GRP-5978) - ****[**GRP-5977********scim resources node is missing******](https://grouper.atlassian.net/browse/GRP-5977) |
| --- |

[https://grouper.atlassian.net/browse/GRP-5932](https://grouper.atlassian.net/browse/GRP-5932)

**Next Grouper Call**: Wed. Feb 26, 2025

****
