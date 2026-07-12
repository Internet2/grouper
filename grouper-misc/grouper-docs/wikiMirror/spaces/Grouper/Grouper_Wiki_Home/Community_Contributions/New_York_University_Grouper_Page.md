---
title: "New York University Grouper Page"
space: Grouper
pageId: 28543287
version: 19
lastUpdated: 2026-07-01T05:50:11.830Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543287/New+York+University+Grouper+Page
---

### Grouper at New York University

********August 2016********

NYU Grouper production is now on version 2.3.0.

****July 2016****

NYU presented at the IAM Online on Grouper. [See slides here](https://www.incommon.org/docs/iamonline/20160713_IAMOnline.pdf)

**June 2016**

Dev and QA environments are running Grouper version 2.3.0 and piloting the PSPNG to replace the PSP.

**May 2016**

NYU Classes requested the ability to create Google Groups where it’s the authoritative source for memberships of a Sakai course site and keep memberships in sync. Groups in Sakai are marked as “Shared with Google Groups” whose members of those groups kept in sync with a corresponding Google Group. Each change in Sakai needs a corresponding change in Google and that's accomplished by a sync job that watches Sakai and applies changes (via a loader job) to Grouper which then the [Google Changelog consumer](https://github.com/Unicon/googleapps-grouper-provisioner/commits/master) acts on to provision to Google. Since course sites are in Grouper, it provides an integration point to expose course site membership information to other systems within NYU. For more info, NYU presented at the Open Apereo 2016 conference the presentation [Grouper in Action at NYU](https://spaces.at.internet2.edu/download/attachments/14517786/Grouper%20in%20Action%20Presentation%20-%20NYU%20May%202016.pdf).

**March 2016**

Grouper Access Management for VPN Authorization where groups where eligible members are included in a composite group which is used to provision the LDAP group used to control VPN access.

**February 2016**

Monitoring was added to Grouper with Splunk to oversee Grouper's performance and trigger alerts based on specified conditions. The Splunk dashboard contains panels for the following: NYUClasses Loader job, Google changelog consumer, Grouper daemon errors, NYUClasses to Google transactions, Number of changes to be processed over time (time chart), PSP Errors over time (time chart).

**October 2015**

Grouper service has a high availability and set up with an active/active (global availability) globally load balanced configuration between server pools in both data centers for the UI and Web Services where at the moment a manual switchover procedure is required for the grouper daemon and bulksync jobs.

**August 2015**

Added institutional groups such as alumni, affiliates, employees and faculty resulting in 46k folders, 75k groups, 1.1M members and 1.8M memberships

**June 2015**

A project needed composite groups but members of such groups would also obtain additional memberships in LDAP that ended in "systemOfRecords", "includes", "excludes", "includes and systemOfRecords" where these values are not going to be consumed by any application, only the resulting group mattered. As a result, we needed a way to exclude such groups from being provisioned as it is the "out of the box" behavior. See the June 2015 update from NYU on Selective Group Exclusion When Provisioning to LDAP.

**October, 2013**

NYU has been running Grouper for several years. Our production instance now sports some 125K folders, 204K groups and  
 500K members -- mostly consisting of groups created to mirror class enrollments.

We have only used Grouper for a few applications to date, but are now embarking on a major project to integrate groups  
 across Sakai, Grouper and Google Groups, with implementation in the Spring 2014 timeframe.

A major challenge for the future is figuring out how best to manage groups and make group data available across a number  
 of core identity management tools which have overlapping capabilities vis-a-vis groups: Grouper, Sailpoint IIq (our newly  
 adopted role/account management system), LDAP, Active Directory.

- Gary Chapman, NYU/ITS - gary.chapman @ nyu.edu
