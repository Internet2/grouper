---
title: "Grouper Training - Use cases - Lesson 05: Diagnostics"
space: Grouper
pageId: 28544276
version: 12
lastUpdated: 2026-11-17T20:28:35.778Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544276/Grouper+Training+-+Use+cases+-+Lesson+05+Diagnostics
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Learning objectives

- Use the Grouper status (diagnostics) URLs to check that Grouper is healthy
- Know which diagnostic type to use for monitoring

## Hands on

Each URL returns a page that starts with “Grouper status SUCCESS” and HTTP status 200 when the checks pass, or an error and HTTP status 500 when something fails. Monitoring tools (e.g. Nagios) call these URLs and alert on a non-200 status. Accept any SSL security warnings.

### Run the diagnostics

1. Web services, trivial check (the web service is up). Paste in your browser:
  
  
  ```
  https://localhost:8443/grouper-ws/status?diagnosticType=trivial
  ```
2. UI, trivial check (memory only, no database):
  
  
  ```
  https://localhost:8443/grouper/status?diagnosticType=trivial
  ```
3. UI, trivial plus a database check:
  
  
  ```
  https://localhost:8443/grouper/status?diagnosticType=db
  ```
4. UI, database check plus the subject sources:
  
  
  ```
  https://localhost:8443/grouper/status?diagnosticType=sources
  ```
5. UI, everything: sources plus daemon and loader jobs. This can take longer:
  
  
  ```
  https://localhost:8443/grouper/status?diagnosticType=all
  ```
6. UI, only one daemon job (the change log temp to change log job):
  
  
  ```
  https://localhost:8443/grouper/status?diagnosticType=daemonJobsOnly&includeOnly=loader_CHANGE_LOG_changeLogTempToChangeLog
  ```

Note: includeOnly (or exclude) can take a comma separated list of daemon job names.
