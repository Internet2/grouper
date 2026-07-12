---
title: "Grouper v2.4 Release Announcement"
space: GrIntDev
pageId: 48793283
version: 9
lastUpdated: 2026-07-12T17:27:33.607Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793283/Grouper+v2.4+Release+Announcement
---

**August 31, 2018**

Hello,  
  
Internet2 is pleased to announce the release of Grouper 2.4, which includes many important enhancements. Grouper is an enterprise access management system designed for the highly distributed management and diverse information technology environment common to universities. Grouper is one of the key components in the TIER Program.  
  
Grouper 2.4 is largely a collection of Grouper 2.3 patches, repackaged, updated, and polished. In addition there are new features too. Upgrading from 2.3 to 2.4 is a minor upgrade and should be planned for soon. The Grouper team will now focus on 2.4 patches and 2.5 development. The timeline for 2.5 will be shorter than the 2.4 timeline, planned for in Spring 2019. We have a lot of exciting plans for 2.4 patches and 2.5 features; we will be reaching out the community shortly to verify our prioritization.  
  
New Features in Grouper 2.4 since the 2.3 initial release include:

| Migrate to new UI | Migrate all screens in Admin and Lite UI to the "New UI" and remove the admin and lite UI. Note, you can add the legacy UI if needed |
| --- | --- |
| [Deprovisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544732/Grouper+deprovisioning) | Deprovision access from someone to loses an affiliation or changes jobs |
| [Attestation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545015/Grouper+attestation) | Groups and folders can be marked to require periodic membership review. Reminders will be emailed to group owners |
| Grouper deployment guide |  |
| [New messaging strategies](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544824/Grouper+messaging+system) | Add new messaging strategies for ActiveMQ, AMQP (e.g. RabbitMQ), AWS |
| [Grouper loader in UI](http://grouper%20loader%20in%20ui/) | User interface to show loader configuration, diagnostics, logs, wizard editor |
| [Subject API diagnostics](http://subject%20api%20diagnostics/) | User interface to analyze, diagnose, and recommend improvements for subject source configuration |
| [Real time SQL loader](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555503/Grouper+loader+real+time+updates) | Allow a change log table (SQL triggers) or messages to trigger loader updates for a partial population or single user |
| [Instrumentation](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792462/Grouper+instrumentation) | Improve and standardize Grouper logging to provide centralized metrics at an institution and the ability to upload stats to a central Internet2 server |
| [Packaging](https://spaces.at.internet2.edu/display/TPWG/TIER+Grouper+-+Docker+Reference+Implementation) | Docker containers that hold Grouper components for each deployment |
| [GSH next generation](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792861/Improve+GSH) | Improve gsh by adding readline like capabilities (line editing, tab completions, history, etc) |
| [Inbound messages](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548229/Grouper+messaging+to+web+service+API) | Allow Grouper to read a message queue and act on messages (e.g. membership changes etc) |
| [vt-ldap to Ldaptive](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792839/vt-ldap+to+ldaptive+migration+for+LDAP+access) | Upgrade from vt-ldap to Ldaptive |
| [properties config](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549156/Grouper+configuration+files+and+overlays) | Convert [sources.xml](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555326/Grouper+sources.xml+conversion+to+subject.properties) and [ehcache.xml](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554172/Grouper+ehcache.xml+conversion+to+grouper.cache.properties) to be cascaded properties files |
| [Update 3rd party libraries](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792845/Identifying+and+Updating+Grouper+Libraries+2017-2018) | Update 3rd party libraries to the latest version that is feasible |

To learn more about the Grouper 2.4 release, to download the software and release notes, for upgrade instructions, and a link to a Grouper demo server, please visit: [https://spaces.internet2.edu/display/Grouper/Grouper+Downloads](https://spaces.internet2.edu/display/Grouper/Grouper+Downloads)  
  
For an introduction to Grouper, please see the Grouper Deployment Guide.

Grouper features a comprehensive suite of free [online training videos](https://spaces.at.internet2.edu/display/groupertrain/Grouper+Training) and an active and supportive user community.

You are invited to visit the Grouper [website](https://www.internet2.edu/products-services/trust-identity/grouper/), [wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/overview), and join the [email lists](https://www.internet2.edu/communities-groups/middleware/grouper-working-group/?edit-off#group-participate).

Thanks,

Chris Hyzer on behalf of the Grouper Team

****
