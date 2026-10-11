---
title: "(Proposal) Optional source-controlled Grouper configuration deployed from Git"
space: GrIntDev
pageId: 282132484
version: 1
lastUpdated: 2026-10-07T15:15:08.177Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/282132484/Proposal+Optional+source-controlled+Grouper+configuration+deployed+from+Git
---

> This page is a **proposal for discussion**. It is not a decision, not a roadmap commitment, and none of the "new feature" items below are implemented. Details will change.
> 
> It describes **one optional way** a deployer could manage Grouper configuration if they do not want to use the default Grouper way (build features in the UI, stored in the database). The default approach stays fully supported and unchanged.

 

## Problem

 Deployers typically build features interactively in the DEV/TEST/PROD UIs: GSH templates, GSH UIs, provisioners, other jobs, loader jobs, etc. These are stored in the database by default. That is convenient, but for deployers who want traditional revision control:

 

- There is no Git history to diff, review, or roll back
- Promotion between environments is manual (copy/paste or the config import screen)
- What is running in an environment is not visible without UI or database access
- The deployment image does not fully describe the running system

 

## Goal

 Allow configuration and code to live in a Git repo that builds or overlays the container image, while keeping the UI as a development and hot-fix tool. Git is the source of truth for what is promoted; the database config history records when each environment received it.

 

## Existing behavior

 The default `grouper.config.hierarchy` is `classpath:grouper.base.properties, classpath:grouper.properties, database:grouper`, so database values already override files in the image.

 

## Proposed approach

 

### 1. Export to drop-in files

 Export features from the active config (database plus files) into a standard directory layout that can be committed to Git and added to the image. One self-contained file per feature keeps diffs readable. Example layout:

 
```
/opt/grouper/conf/registered/
  grouper.properties.d/          one file per feature, e.g. gshTemplate_myTemplate.properties
  grouper-loader.properties.d/   provisioners, other jobs, daemons
  gsh/                           externalized GSH / Java source referenced by config
  objects/                       non-config objects, e.g. loader job attributes
```

 

### 2. Auto-register files into the database at startup (new feature)

 

- At startup, scan the designated directory and sync each file into database config through the normal config API, so config history shows when each environment picked up a change
- Store a hash of each file with the rows it loaded; re-apply only when the hash changes
- Detect drift (the database value was edited in the UI after the file was loaded) and apply a per-environment policy: fileWins, dbWins, or warn (log and flag in the UI)

 

### 3. Ability to disable database config entries (new feature)

 

- Add a disabled flag on database config rows, or on a feature's group of rows
- After registering a file, startup can load it into the database and mark it disabled so the file version is effective
- The UI still shows every feature and its state, so reviewers do not need file or Git access
- For development or a hot fix, enable the database copy in the UI, edit it, then export it back to Git

 

### 4. Cover all configuration objects (new feature)

 GSH templates, provisioners, and other jobs are config properties, so steps 1-3 apply directly. Loader jobs are attribute assignments on groups, not config, so they would need a declarative file format and an idempotent register step.

 

## Possible deployment pattern

 

| Environment | Hierarchy | Drift policy | Workflow |
| --- | --- | --- | --- |
| DEV / TEST | Database over files (current default) | dbWins or warn | Develop in the UI, export, commit to Git |
| PROD | Optionally files over database | fileWins | Changes arrive by image promotion; a hot fix enables a database override, which shows as drift until committed back to Git |

 

## Open questions

 

- Granularity: one file per feature or per property, and how rows are grouped for disable/enable
- Secrets: exported files must hold only references (external system, encrypted, environment variable), never cleartext passwords
- Deletes: when a file is removed from the image, delete, disable, or only flag the database rows?
- Ordering and dependencies at registration time, e.g. a provisioner that references an external system
- Whether the config import screen should become the manual front end to the same registration code
