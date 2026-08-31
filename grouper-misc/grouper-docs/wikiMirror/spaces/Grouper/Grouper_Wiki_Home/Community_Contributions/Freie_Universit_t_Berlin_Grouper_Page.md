---
title: "Freie Universität Berlin Grouper Page"
space: Grouper
pageId: 28543638
version: 15
lastUpdated: 2026-07-01T05:49:19.004Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543638/Freie+Universit+t+Berlin+Grouper+Page
---

### Statistics

- Using Grouper 2.2.2
- members
  
  - 86503 members total
  - on average 47 new members per day
- memberships
  
  - 2752863 memberships total
  - on average 582 new memberships per day
- groups
  
  - 14060 groups total
  - on average 12 new groups per day
- folders
  
  - 4940 folders total
  - on average 5 new folders per day
- loader jobs
  
  - 15 loader groups
  - on average 4095320 loader jobs per day

### Servers

- 2 PostgreSQL database servers for imported sources
- 1 OpenLDAP server for users and targets (redundant)
- 1 Tomcat server for UIs and Web Services (redundant)
- 1 Apache Reverse Proxy (redundant)

### Sources

- Employees
  
  - organzational units
  - functions
  - cost centers
- Students
  
  - faculties
  - degrees
- Alumnis
- Guests
- Members of affiliated institutes
- Projects

### Utilization

- web portal access management
- software download rights management
- file system access management
- computing capacity management
- roll-outs
- mailing lists
- Shibboleth entitlements

### Customizations

- We're using hooks for
  
  - [sending mails](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543974/FU+Berlin+-+Using+Hooks) to the wheel members if important member groups are deleted
  - [setting privileges](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543974/FU+Berlin+-+Using+Hooks) automatically if groups are created within a certain stem
  - [writing log entries into syslog](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543974/FU+Berlin+-+Using+Hooks) if members or groups are deleted/added within a certain stem
  - [supplying unixGroups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543974/FU+Berlin+-+Using+Hooks) with gidNumbers
- [We're using Jabber](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543981/FU+Berlin+-+Using+Jabber)for
  
  - sending notifications if a wheel group member is deleted from the group
  - sending notifications if a target group is deleted
- We're using various grouper functions for a daily maintenance routine, such as
  
  - usdu(usdu.DELETE)
  - findBadMemberships()
  - loaderRunOneJob("MAINTENANCE_cleanLogs")
  - loaderRunOneJob("CHANGE_LOG_changeLogTempToChangeLog")
  - new edu.internet2.middleware.grouper.misc.SyncPITTables().syncAllPITTables()
- We're using self made sql queries for cleaning up the database daily by
  
  - deleting empty sources groups
  - deleting empty sources stems
  - removing members without memberships from grouper_members table after a certain expiration period
- We're using Grouper Web Services for a NAGIOS check by
  
  - checking LDAP connection
  - checking database connection
  - checking the group size of crucial groups
  - checking if loader jobs were running
- We have been customizing the UIs by
  
  - fitting them to the corporate design
  - enabling gidNumber editing for unixGroups
  - displaying standard accounts different from nonstandard accounts
  - enabling German umlauts in incremental search
  - setting labels according to user requests
  - removing the "Lite UI" link as well as the "Browse folders" tile for non-admin users
