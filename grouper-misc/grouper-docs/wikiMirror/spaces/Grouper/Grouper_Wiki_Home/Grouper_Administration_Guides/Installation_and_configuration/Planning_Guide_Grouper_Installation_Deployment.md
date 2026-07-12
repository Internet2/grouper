---
title: "Planning Guide - Grouper Installation & Deployment"
space: Grouper
pageId: 28544646
version: 92
lastUpdated: 2026-07-12T15:26:25.632Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544646/Planning+Guide+-+Grouper+Installation+Deployment
---

This is a sample planning guide for Grouper Installation and Deployment, with content contributed by New York University. It is intended to provide a framework as you are getting started implementing Grouper at your site. There are three primary stages:

- Planning
- Install/Test/Roll-out
- Developing Integration Materials

> Grouper 2.5+ installation requires using a container. See the InCommon Trusted Access Platform (ITAP) Docker Containers.

### I - Planning Stage

**Gain a basic understanding of Grouper**

- Review Grouper introductory/overview [documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541827/Grouper+Administration+Guides), including the [glossary](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541893/Grouper+glossary).
  
  - Take [Grouper School](https://incommon.org/academy/grouper-school/) training
- Review the [Grouper Deployment Guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541813/Grouper+Deployment+Guide)
- Imagine how you expect Grouper to fit into your identity and application architecture
- Check out the [Grouper Demo](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541850/Grouper+Demo)
- Install Grouper in a test environment to familiarize yourself with Grouper.

**Set initial goals**

Establish a set of specific goals for your initial project.

- Are you planning an exploratory investigation of Grouper for possible future use, or have you settled on implementing Grouper in production for, at least, an initial set of purposes?
- What applications or application uses will be integrated with Grouper?
- What [Grouper software components](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543703/Grouper+components+overview) need to be installed for initial use?
- Will Grouper manage ALL your groups, or will some group data be managed by other means?
- Do you have existing groups data and groups management software from which you need to migrate?
- Can you install, and begin to use, Grouper in phases?

**Plan hardware and software environments**

- Review InCommon Trusted Access Platform Docker Containers. **This is the suggested installation as of 2019 and required as of Grouper 2.5 and above**.
- What Grouper environments will you initially install? A development instance? A test (Q/A) instance? A production instance? All three or just one or two?
- For your software environments, what host machines will you run on? What ports will be used, what firewall settings might need to be made?
- Will you run Grouper software "as root" or as another user?
- How will you handle [authentication to the UI and to Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545269/Authentication)?

**Plan groups data hierarchy and naming**

- What basic categories of groups do you wish to manage? (e.g. classes, committees, workgroups, groups that share an entitlement, major subsets of your community, such as students/freshman/faculty/IT staff, etc. etc. etc.)
- Determine a basic stem / folder structure that supports two or more initial categories of groups.
- Determine your groups naming scheme. [Example here](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544556/UW-Madison+Stem+and+Group+Naming+Standards).
- Flat or bushy?
- Will you use the [template wizard for creating folders and groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545142/Template+wizard)?

**Determine application and data components to use**

The Grouper software consists of a number of major application and data components, not all of which you may wish to install and run from the beginning....

- What database (existing or new) will form your Grouper database repository?
- What database (existing) will provide you with subject data
- Do you plan to replicate groups data out to LDAP or some other database?
- Do you plan to automate groups management (for some or all groups) based on one or more data sources (and using the Grouper Loader)?
- Should you use just an application server or an application server + web server to enable web access?
- Which interfaces to groups data do plan to initially implement and support? Web browser access? [Web services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services) access? [Grouper shell](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh) access?
- How do you expect end-users and applications to interface for read-only and for read-write purposes to groups data?
- Security considerations (e.g. [wheel group](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545234/Initializing+Administration+of+Privileges), [externalizing and encrypting database/ldap passwords](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549242/Externalize+and+encrypt+grouper+passwords+morphString+morph))
- How will you [structure configuration files](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549156/Grouper+configuration+files+and+overlays)?
- Will you use
  
  - [Reporting](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554409/Grouper+reporting)
  - [Attestation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545015/Grouper+attestation)
  - [Visualization](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548433/Visualization+UI)

### II - Installation, Testing, Rollout

**Sketch out your actual installation, testing, and rollout process, including:**

- Confirm access to hardware/software environments, data sources and destinations
- Outline steps for installation and configuration of Grouper software elements. Consider using [this quickstart method](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555721/Install+the+Grouper+container+maturity+level+-1+quick+start+v2.6.5+quickstart)
- Finalize initial stems/folders to create, authentication approach, initial groups to create and populate
  
  - consider using the [template wizard](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545142/Template+wizard)
- Plan basic testing of functionality
- Plan for ongoing operations, considering your desired approach to such duties as
- Monitoring / Management / Maintenance (see the section in [Tools & Topics for Ongoing Administration](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543624/Tools+Topics+for+Ongoing+Administration))
- Support for Application developers/managers integrating their apps with Grouper
- Support for any end-users
- Plan to document your installation and configuration as you go along. [Please share your experience so other sites can benefit](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541859/Community+Contributions).

**Install & Test**

### III - Develop integration materials

Develop documentation, sample code, examples for use by app developers who wish to integrate their software with your Grouper installation.

To help other sites and facilitate the success of the Grouper community, please contribute your documents to the [Grouper Community Contributions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541859/Community+Contributions) area.

Consider how you will train and communicate with and educate your stakeholders about Grouper. These examples may be of interest:

### See Also

[Getting Ready for Production](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549151/Getting+Ready+for+Production+with+Grouper)

[Grouper School training](https://incommon.org/academy/grouper-school/)

[Grouper Training slides](https://spaces.at.internet2.edu/download/attachments/14517786/GrouperTraining-Apereo_part3.pdf?version=1&modificationDate=1370270516490) (including group naming best practices)

[Grouper glossary](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541893/Grouper+glossary)
