---
title: "v2.4 new features"
space: GrIntDev
pageId: 48793978
version: 9
lastUpdated: 2026-07-12T07:02:19.742Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793978/v2.4+new+features
---

## New Features in Grouper 2.4.0

Most features in Grouper 2.4.0 are also in 2.3.0 patches. The upgrade from 2.3.0 to 2.4.0 is a minor upgrade.

| [Migrate to new UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543095/User+Interface) | Migrate all screens in Admin and Lite UI to the "New UI" and remove the admin and lite UI. Note, you can add the legacy UI if needed |
| --- | --- |
| [Deprovisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544732/Grouper+deprovisioning) | Deprovision access from someone to loses an affiliation or changes jobs |
| [Attestation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545015/Grouper+attestation) | Groups and folders can be marked to require periodic membership review. Reminders will be emailed to group owners |
| Grouper deployment guide | Version 1 of the Grouper deployment guide is an introduction to Grouper and best practices for using it |
| [New messaging strategies](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544824/Grouper+messaging+system) | Add new messaging strategies for ActiveMQ, AMQP (e.g. RabbitMQ), AWS |
| [Grouper loader in UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554452/Grouper+loader+on+UI) | User interface to show loader configuration, diagnostics, logs, wizard editor |
| [Subject API diagnostics](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545428/Subject+API+diagnostics) | User interface to analyze, diagnose, and recommend improvements for subject source configuration |
| [Real time SQL loader](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555503/Grouper+loader+real+time+updates) | Allow a change log table (SQL triggers) or messages to trigger loader updates for a partial population or single user |
| [Instrumentation](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792462/Grouper+instrumentation) | Improve and standardize Grouper logging to provide centralized metrics at an institution and the ability to upload stats to a central Internet2 server |
| Packaging | Docker containers that hold Grouper components for each deployment |
| [GSH next generation](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792861/Improve+GSH) | Improve gsh by adding readline like capabilities (line editing, tab completions, history, etc) |
| [Inbound messages](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548229/Grouper+messaging+to+web+service+API) | Allow Grouper to read a message queue and act on messages (e.g. membership changes etc) |
| [vt-ldap to Ldaptive](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792839/vt-ldap+to+ldaptive+migration+for+LDAP+access) | Upgrade from vt-ldap to Ldaptive |
| properties config | Convert  [sources.xml](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555326/Grouper+sources.xml+conversion+to+subject.properties)  and  [ehcache.xml](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554172/Grouper+ehcache.xml+conversion+to+grouper.cache.properties)  to be cascaded properties files |
| [Update 3rd party libraries](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792845/Identifying+and+Updating+Grouper+Libraries+2017-2018) | Update 3rd party libraries to the latest version that is feasible |
