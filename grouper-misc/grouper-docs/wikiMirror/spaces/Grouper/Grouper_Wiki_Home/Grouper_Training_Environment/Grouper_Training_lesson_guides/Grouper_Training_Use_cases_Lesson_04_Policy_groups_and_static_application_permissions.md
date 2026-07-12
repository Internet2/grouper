---
title: "Grouper Training - Use cases - Lesson 04: Policy groups and static application permissions"
space: Grouper
pageId: 28544252
version: 18
lastUpdated: 2026-07-12T15:26:15.698Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544252/Grouper+Training+-+Use+cases+-+Lesson+04+Policy+groups+and+static+application+permissions
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Learning Objectives

- Understand ACM Policy groups and static application permissions
- Implement grouper security model
- Configure provisioning to LDAP for eduPersonEntitlement
- Configure Shibboleth to release eduPersonEntitlement

## Hands On

### Create policy for wiki application

- Navigate to the *app* folder
- create a new application (More actions -> New template -> Application)
  
  - Key: `wiki`
  - Description: `Student wiki`

### Create policy for wiki application

wiki_user is an application-specific role. Subjects in this role have general access to the wiki. The natural language policy is "All students have access to the student wiki, unless they are in the global deny group"

- Navigate to *app:wiki:service:policy*
- Create new policy template:
  
  - Key: `wiki_user`
  - Description: `Access policy for student wiki`
- Add *ref:student:students* as a member to _wiki_user_allow
- Review the membership of _wiki_user_deny
- Visualize policy definition of *wiki_user*
  
  - set visualization option *Show number of sibling objects* to 15

### Configure external system for provisioning

Provisioning targets depend on an external system that includes basic connection and configuration. For this lesson, the LDAP system has already been set up with config id "demo".

- Navigate to Miscellaneous > External Systems.
- Location entry for "demo"
- Under Actions, view the details, then test the system

### Configure provisioner

A provisioner has already been set up, eduPersonEntitlement. Edit its properties

- Go to Miscellaneous -> Provisioning -> eduPersonEntitlement -> Actions -> Edit provisioner

In this provisioner, members of a group will have their user record in LDAP updated in the eduPersonEntitlement attribute. The entitlement value can be a static string, or will fall back to the group name.

### **Create a full sync provisioning job**

The provisioner exists, but needs to have either a full sync or incremental job to perform the provisioning.

If the provisioner actions does not show an option for "Daemon - full", do the following:

- While editing the provisioner, find the Advanced section and select Show advanced = true
- Select True for *Add disabled full sync daemon* and *Add disabled incremental sync daemon*
- Click Submit

### Configure provisioning on group

- Navigate back to the *app:wiki:service:policy:wiki_user* group
- Under More actions, choose *Provisioning* and assign provisioning for *eduPersonEntitlement*. In the dialog box, enter Entitlement String: `http://sp.example.org/wiki`

### Run provisioner job

- In Miscellaneous > Provisioning > eduPersonEntitlement, choose action "Daemon - full", enable and run the job
- Click on *Apply filter* until the job completes

### Verify Provisioning results

- From the GTE Jump page (https://localhost:8443/) launch the LDAP manager and login.
- Verify users in Grouper *wiki_users* have an entitlement of http://sp.example.org/wiki (e.g. uid=aalexan2)

### Configure Shib to release eduPersonEntitlement

- Open a private browser, and log in to https://localhost:8443/app with username *abrown* and password *password*
- Look at value for *eduPersonEntitlement*
