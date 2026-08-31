---
title: "University of Edinburgh Grouper Page"
space: Grouper
pageId: 28543344
version: 6
lastUpdated: 2026-07-01T05:49:59.522Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543344/University+of+Edinburgh+Grouper+Page
---

# 

# Introduction/History

At the University of Edinburgh we chose Grouper as part of the implementation of an in-house Identity Management System. We needed a Group Management solution and after evaluating a few products, both commercial and free open source ones, we chose Grouper.

We originally deployed Grouper 1.5.0 to live in around 2012, and as part of that did an upgrade to 1.5.3. In 2015, we have upgraded Grouper to version 2.2.0, mainly to take advantage of the new UI and change based PSP.

Overall Grouper has been an invaluable addition to us, as it has "just worked" with a minimal amount of fuss, which is always a good sign in a piece of software!

# Grouper Usage

We use Grouper for a few main reasons:

- We need a centrally provided group store which other systems can use as an authoritative source of group and membership information
- We need our Identity Management System to use groups to make decisions on which services to provision with identities.
- We need to provision an Open LDAP directory with groups for other systems and services to make use of centrally provided groups
- We need to optionally allow the creation of devolved adhoc groups which can be then used for other systems to make use of.

# Grouper structure

Our Grouper top level (root) structure is as follows:

- Organisational Hierarchy - The organisational structure of the University
- Affiliation groups - Groups containing how identities are affiliated with the university, e.g. staff, undergraduate student etc
- Courses
- Programmes of Study - These are also attached to the Organisational Groups
- Adhoc groups - An area for devolved creation of groups
- Service groups - Service level groups to which individual identities can manually be added

### Subject (Identity) Source

Grouper is set up to use our Identity Management System as a source of identities. This is set up as a simple query on the IDM database.
