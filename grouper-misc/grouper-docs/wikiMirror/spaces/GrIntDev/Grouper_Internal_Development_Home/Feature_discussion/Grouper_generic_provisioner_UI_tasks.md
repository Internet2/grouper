---
title: "Grouper generic provisioner UI tasks"
space: GrIntDev
pageId: 48793073
version: 12
lastUpdated: 2026-07-12T17:02:44.391Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793073/Grouper+generic+provisioner+UI+tasks
---

These tasks generally use the provisioner logic (to see if things are applicable), and the "sync" tables to get the data to show on screen. The new provisioning attributes will be used when assigning folders or groups as provisionable. The existing provisioning attribute screens will be merged into these new screens. Grouper messages will be used to tell the daemon to do provisioning tasks. See design here.

| Task | Subtask | Description | Date | Status |
| --- | --- | --- | --- | --- |
| Overall provisioner list |  | UI screen that displays provisioners |  | Done |
|  | Most recent jobs | Display the most recent jobs (full, incremental, etc), number of records in queue |  | Later |
|  | Estimated time to completion |  |  | Later |
|  | Stats | Number of objects provisioned (users, groups, memberships) |  | Later |
|  | Errors | Error count |  | Later |
|  | Security | Need to be grouper admin or readonly admin or in a provisioning managers group (one group for grouper, auto created) |  | Later |
|  | Auditing | All provisioning actions should be audited |  | Later |
| Overall provisioner |  | Main screen for provisioner  Make a link from the configId to go to this screen |  | Done |
|  | Details | Details of above. Include number of records skipped for processing |  | Done |
|  | Display errors | Display which objects have errors and what the error is. Only show the first 200 errors |  | Later |
|  | Run full sync | If applicable. Send message based on options:    - if synchronous / asynchronous (if applicable) - if clear out subject API (if applicable) - if clear out user/group link (if applicable) |  | Run full sync is done without clearing out subject/user/group |
|  | Fix errors (if errors) | Send message to fix errors |  | Later |
|  | Provisioned groups |  |  | Later |
|  | Provisioned users |  |  | Later |
|  | Edit provisioner | Link to provisioner configuration |  | Done |
|  | Daemon config buttons | Links to any daemons for this provisioner |  | Later |
| Folder provisioning |  | Show all provisioners applicable to this folder |  | Done |
|  | Add / remove provisioner | Might already be done |  | Done |
|  | Assign specific provisioning location |  |  | Later |
|  | Stats |  |  |  |
|  | Errors | Error count |  |  |
|  | Security | If has inherited ADMIN on folder then can see screen |  |  |
| Folder provisioner |  | Folder screen for provisioner |  |  |
|  | Details | Details of above, Number of objects provisioned (users, groups, memberships) |  |  |
|  | Display errors | Display which objects have errors and what the error is. Only show the first 200 errors |  |  |
|  | Run folder sync | If applicable. Send message based on options:    - if synchronous / asynchronous (if applicable) |  | Later |
|  | Fix errors (if errors) | Send message to fix folder errors |  | Later |
| Group provisioning |  | Show all provisioners applicable to this group |  |  |
|  | Add / remove provisioner | Might already be done |  |  |
|  | Assign specific provisioning location |  |  | LATER |
|  | Stats |  |  |  |
|  | Errors | Error count |  |  |
|  | Security | If has group READ then can see screen. If has UPDATE on group then can click provisioning fixing buttons |  |  |
| Group provisioner |  | Group screen for provisioner |  |  |
|  | Details | Details of above, if provisioned, when, Number of objects provisioned (users, memberships) |  |  |
|  | Display errors | Display which objects have errors and what the error is. Only show the first 200 errors |  |  |
|  | Run group sync | If applicable. Send message based on options:    - if synchronous / asynchronous (if applicable) |  | Later |
|  | Fix errors (if errors) | Send message to fix group errors |  | Later |
| Subject provisioning |  | Show all provisioners applicable to this subject |  |  |
|  | Stats |  |  |  |
|  | Errors | Error count |  |  |
|  | Security | Need to be grouper admin or readonly admin or in a provisioning managers group (one group for grouper, auto created) |  |  |
| Subject provisioner |  | Subject screen for provisioner |  |  |
|  | Details | Details of above, if provisioned, when Number of objects provisioned memberships |  |  |
|  | Display errors | Display which objects have errors and what the error is. Only show the first 200 errors |  |  |
|  | Run user sync | If applicable. Send message based on options:    - if synchronous / asynchronous (if applicable) |  | Later |
|  | Fix errors (if errors) | Send message to fix user errors |  | Later |
| Membership provisioning |  | Show all provisioners applicable to this subject |  |  |
|  | Stats | If provisioned, and when |  |  |
|  | Error? | If error |  |  |
|  | Security | If has group READ then can see screen. If has UPDATE on group then can click provisioning fixing buttons |  |  |
| Membership provisioner |  | Subject screen for provisioner |  |  |
|  | Details | Details of above |  |  |
|  | Display error | Display specific error |  |  |
|  | Run membership sync | If applicable. Send message |  | Later |
