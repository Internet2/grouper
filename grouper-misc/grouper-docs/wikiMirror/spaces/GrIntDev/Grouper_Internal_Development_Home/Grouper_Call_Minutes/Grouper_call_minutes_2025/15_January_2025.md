---
title: "15-January-2025"
space: GrIntDev
pageId: 48793304
version: 10
lastUpdated: 2026-07-12T17:27:34.075Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793304/15-January-2025
---

# **Grouper Call of Jan. 15, 2025**

**Attending**

- Chris Hyzer, Penn, Chair
- Shilen Patel, Duke
- Bert Bee Lindgren, Georgia Tech
- Dusty Edenfield, Georgia Tech
- Chad Redman, Unicon
- Matt Black, Purdue
- Kellen Murphy, Univ of Virginia
- Michael Gettes, SLAC
- Ben Rappleyea, Illinois State U
- Drew Aschenbrener, Internet2

## **DISCUSSION**

**Administrivia**

- [Internet2 Intellectual Property Policy](https://internet2.edu/community/about-us/policies/internet2-intellectual-property-policy/)
- Review AIs [Grouper Project Action Items (Google Doc)](https://docs.google.com/document/d/1jQCt1nICmVVZsU8iprjbDw0WbmnpUt87NsS7rdKmfMo/edit)
- Agenda bash

**Call Summary from Zoom Ai**

*The team explored the implementation of a new method for database queries, the functionality of the cache history table and dependency table, and the revamping of training. Issues related to the lifecycle and script hooks, the non-prod environment, and the Grouper provisioning job were also addressed, with plans to continue troubleshooting and upgrading to the latest version for logging improvements.*

**Next Steps (from Zoom Ai)**

1. Chris to update the Grouper script hooks documentation to include underscores in function names.
2. Chris to meet with Internet2 team to discuss AD provisioner issues and Posix provisioner problems.
3. Chris to investigate the Google Provisioner issue where users are sometimes getting DNE errors and sometimes ERR errors.
4. Chris to look into the error messages Drew is getting with the Grouper provisioning daemon job in version 4.17. There is a Slack Thread on this.
5. Chad to continue work on creating videos for the online grouper training.
6. Shilen to add unit tests and finalize the recent member of ABAC option.
7. Chris to document the process of upgrading from PSPNG to the provisioning framework.
8. Chris to document Penn's new container design approach in either the contrib section or the main container page.
9. Chris to investigate and potentially implement performance enhancements for ABAC as discussed by Vivek.
10. Chris to review and potentially implement the fresh service provisioner contributed by North Dakota.

**Grouper Releases**

There have been several recent Grouper Releases

- [v5 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes)
- [v4 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)

## **Current Work**

**Vivek**

- working on [Grouper Okta provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554255/Grouper+Okta+provisioner)
- [Grouper external system - Web service - Oauth credential - Okta](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547432/Grouper+external+system+-+Web+service+-+Oauth+credential+-+Okta)
- Trying to leverage web service external system common patterns
- Many targets communicate in two or three basic ways
- Will show on provisioning screen
- You must name configs carefully, such as okta external system
- We added OAUTH to web service external system
- Chris Hyzer:
- OKTA can do multiple authentications
- Some orgs use a shared secret
- You can pick and choose
- Sometimes targets change their authentication
- We can now accommodate that without changing the provisioner
- Swiss army knife external system
- When we refactor other provisioners we will use this swiss army knife approach
- Challenge with EL and OKTA, we refactored that for a better experience.
- Goal is to provision groups and memberships, can also provision users
- North Dakota did their own provisioner for Fresh Service. They will contribute that to the Grouper Project
- Will implement a simple mock Fresh Service.
- U Mich will start creating some provisioners also.
- Vivek also working on performance enhancements
- Database calls are expensive, do multiple at once where possible
- Use batch method instead of one by one in loops
- Bert: suggests that when batch operation fails, should roll back and do them one at at time. Chris Hyzer: there are parameters, to use smaller batch or retry individual.
- Bert: specific error messages are important in the case of batch operation fails

**Improving Database Query Performance (from Zoom AI)**

*Vivek discussed the implementation of a new method for database queries, aiming to improve performance by reducing the number of individual queries. He explained that instead of executing queries one by one, the new method allows for the execution of multiple queries in a batch, up to a limit of 1,000. This approach was demonstrated through examples of select, insert, and delete queries.****Chris and Bert discussed the potential benefits and limitations of this method, including the possibility of encountering database sensitivity issues.*

**Cache History Table and Dependency**

- Shilen demonstrated the new "**recent member of**" ABAC option, which allows for the selection of group members based on their activity within a specified timeframe.
- Shilen demonstrated the functionality of the cache history table and dependency table.
- The system can track recent members and their status, and can handle changes in group membership.
- Chris suggested adding a feature to track when a member was in a group in the last two days.
- Point in time for memberships in ABAC is great. Attributes will be done in the future.
- Bert raised a concern about differentiating between manually removed memberships and those removed due to a reference group.
- Chris agreed to discuss this further on the ABAC Channel.

**Chad**

- Chad did a fix for the Oracle upgrade task, with semicolons, was in V5 but not v4. Fixed that.
- improvements to the provisioning error handling. Changed descriptions to include the error code. Provides visual help.
- Chad has Confusion about the deprovisioning process,
- Chris explained deprovisioning employees is a manual HR task.
- It’s not automatic.
- It’s a major event that happens relatively infrequently.
- Deprovisioned members get added to a blocked group.
- Chad agreed to discuss possible doc improvements further at the next Grouper documentation meeting.

**Grouper Training**

- Chad preparing for online Grouper training.
- The training team plans to make 20 to 40 Grouper Training videos
- Make a wiki to track the work on creating Grouper Training Videos
- Change in person training to focus on use cases
- There will be on-demand training to be available whenever people want (not just as class prework)
- Hope to have an advanced course on the Grouper Loader, with 5 to 10 lessons
- Then advanced courses for Provisioning, Data Fields, ABAC, and other topics

**Grouper Client**

- There are 2 projects in GIT
- Grouper client could do more than talk to web services, could do things w messaging and database. That project is meant to be self contained. One jar no dependencies.
- Now we go towards Grouper v7 and less hibernate because of poor performance.
- Now it’s hard to code in the Grouper Client
- Things in the client are self contained
- Hope to change intent of Grouper client
- Move more things to the Grouper API
- Chad agreed with Chris's ideas and suggested splitting out core functionality from the executable part of the Grouper client.
- Shaded plug ins help.
- Do we need Grouper Client at all?
- Might not need it if we started from scratch, but people are used to it.
- Idea of removing the database access from the Grouper client and refactoring the configuration files.
- The team agreed to further discuss these ideas and consider the implications of these changes.

**Tomcat Upgrade and Performance Improvements**

- Chris discussed the recent updates and issues with their release notes page, particularly a problem with the Tomcat upgrade that may have affected the Ajp port.
- He mentioned a large release in early December and a couple of issues that were minor adjustments.
- performance improvements were made, such as the way errors were handled and the incremental processing of messages.
- He shared their experience with upgrading from PSP to the provisioning framework and their new container design for Penn. Drew confirmed that their setup was similar, using one image for all containers and environments.

**Lifecycle and Script Hook Issues**

- Bert expressed difficulty in understanding the lifecycle and script hooks
- Chris mentioned that they are working on issues with the rules UI and the membership history graph.
- Chad brought up an issue with the Google Provisioner, where sometimes it throws a DNE error and sometimes an err.
- Chris suggested that both should be DNEs and that improvements can be made in the provisioning process.
- Bert and Chad also reported issues with the Posix provisioner not working as expected.
- Chris agreed to look into these issues and suggested a meeting to discuss them further.

- Chad suggested adding certain hooks to the grouper image for everyone to use, particularly for setting up the Shibboleth SP name.
- Chris agreed and provided an example of their health check for GSH.

**Non-Prod Environment Troubleshooting and Upgrades**

- Bert and Chris discussed the challenges they were facing with their non-prod environment, which was not functioning as expected.
- They agreed to continue troubleshooting and possibly upgrade to the latest version, 5.15.4, for logging improvements.
- Chris also agreed to look into the core provisioning changes from 5.13 to 5.15.4.
- Drew brought up an issue with the Grouper provisioning job throwing error messages, which Chris agreed to investigate.
- The team also discussed the possibility of externalizing GSH template configurations for better revision control.

## **Issue Roundup**

**Wiki Updates**

- [v4 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)Jan 09, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)
- [v5 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes)Jan 09, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes)
- [Contact Information](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541872/Contact+Information)Jan 09, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541872/Contact+Information)
- [Grouper Okta provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554255/Grouper+Okta+provisioner)Jan 08, 2025 • updated by Vivek Sachdeva (google.com) • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554255/Grouper+Okta+provisioner)
- 
- [Grouper upgrade tasks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)Jan 06, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)
- [Grouper container documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation)Jan 06, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation)
- v5 Upgrade Instructions from v4  
  Jan 05, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547828/v6+Upgrade+Instructions+from+v4)
- [v4 Upgrade instructions from v4](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549792/v4+Upgrade+instructions+from+v4)Jan 04, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549792/v4+Upgrade+instructions+from+v4)
- [Grouper Okta provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554255/Grouper+Okta+provisioner)Jan 04, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554255/Grouper+Okta+provisioner)
- [v5 Upgrade instructions from v5](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549165/v5+Upgrade+instructions+from+v5)Jan 04, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549165/v5+Upgrade+instructions+from+v5)
- [DDL in Grouper](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548570/DDL+in+Grouper)Jan 03, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548570/DDL+in+Grouper)
- 
- [Grouper external system - Web service - Oauth credential - Okta](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547432/Grouper+external+system+-+Web+service+-+Oauth+credential+-+Okta)Jan 02, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547432/Grouper+external+system+-+Web+service+-+Oauth+credential+-+Okta)
- 
- [Grouper Adobe provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555458/Grouper+Adobe+provisioner)Jan 02, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555458/Grouper+Adobe+provisioner)
- [Grouper external system - Web service - Oauth credential - Adobe](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547372/Grouper+external+system+-+Web+service+-+Oauth+credential+-+Adobe)Jan 02, 2025 • updated by chris.hyzer.3@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547372/Grouper+external+system+-+Web+service+-+Oauth+credential+-+Adobe)
- 
- [GrouperShell (gsh) Email smtp (GrouperEmail)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548181/GrouperShell+gsh+Email+smtp+GrouperEmail)Dec 23, 2024 • updated by Graham Ballantyne • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548181/GrouperShell+gsh+Email+smtp+GrouperEmail)
- [Specsheet](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549107/Specsheet)Dec 17, 2024 • updated by ben.rappleyea.2@at.internet2.edu • [view change](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549107/Specsheet)

**Next Grouper Call**: Wednesday, January 29, 2025

****
