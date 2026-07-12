---
title: "Grouper provisioning framework"
space: Grouper
pageId: 28544760
version: 89
lastUpdated: 2026-07-12T15:26:29.069Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544760/Grouper+provisioning+framework
---

**This page is your starting point for the Grouper provisioning framework in Grouper v4+.**  
See the [Versioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544481/Versioning+Support+Policy) page for clarification on Grouper version numbering.

For background, see these blogs

- [New Provisioning Framework blog - January 2021](https://incommon.org/news/flexible-and-powerful-to-address-community-needs-new-grouper-provisioning-framework/)
- [News from the Grouper Project blog - November 2021](https://incommon.org/news/news-from-the-grouper-project/?_ga=2.254687115.2072662432.1658943707-1153598486.1658943707&_gl=1*mnmv7t*_ga*MTE1MzU5ODQ4Ni4xNjU4OTQzNzA3*_ga_8P1JY9SZF0*MTY1OTAxNDY4NS41LjEuMTY1OTAxNTA3Ny4w)
- [Major Improvements to the Grouper Provisioning Framework - February 2023](https://incommon.org/news/major-improvements-to-the-grouper-provisioning-framework/)

[Provisioning Glossary](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548708/Grouper+provisioning+glossary)

## Overview of features

Key features of the provisioning framework:

- All parts of provisioning configuration are performed in the [Grouper UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548587/Grouper+provisioning+in+UI)with helpful documentation, wizard-like interfaces, descriptive [validations](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554271/Grouper+provisioning+validation), and diagnostic tests.
- All provisioners have consistent [configuration concepts](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555478/Generic+provisioning+configuration) and terms so adding the next provisioner will be easy.
- Configuring new provisioners or editing existing provisioners do not require Grouper to be redeployed/restarted.
- [Provisioning configuration](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555478/Generic+provisioning+configuration) starts with an “external system”, which is the connection information to connect to the target to provisioning to.
- [External systems](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544712/Grouper+External+systems+configuration) can be re-used among provisioners, or for other parts of Grouper (e.g. loader, “custom UI”). The provisioner itself is configured next.
- [Scaffolding (start with)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560139/Grouper+provisioning+configuration+scaffolding+start+with) can help you get started with provisioning configuration.
- There are the [daemon jobs](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545241/Grouper+Daemon) for the full or incremental sync which are scheduled.
- All provisioners have a standard object model. [Translating](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555413/Grouper+provisioning+translations) data from Grouper to the target format is consistent across all provisioners.
- You configure which data from Grouper gets sent to the target and how it is formatted
- There are provisioning-specific screens to identify which objects (groups, users, memberships, attributes) are sent to the target.
- Provisioner specific [metadata](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555763/Grouper+provisioning+custom+metadata) can be assigned to Grouper objects to inform provisioning actions.
- When the provisioner is up and running, data propagation is verified and errors info is readily available

## When asking for support

Turn on "Log all objects verbose" to DB under advanced in provisioning configuration. You can optionally also enter in a group name and subject id to focus on.

Do a provisioning run that shows an error

Go to the daemon log for that run, scroll to the right, and copy the full output, send in slack snippet to incommon-grouper (or to Chris Hyzer if private info)

Note, if you are doing a large run you might want to turn off command logging

Export the grouper-loader properties file from UI. Take the section for that provisioner (no need to send configs for daemon). Send that along too.

Report which version your Grouper is.

If the external system is involved, send relevant non private info too.

## High level design

**Notes on High Level Design diagram**

1. The provisioner has CRUD (Create Read Update Delete) operations implemented for the endpoint.
  
  
  
  1. Note: there is a concept of an "external system" in Grouper. Perhaps some or all of the CRUD is implemented in the "External System". Perhaps the external system builds on top of an External System
  2. Its possible to integrate ConnID here or wrap around it in the future
2. There is an object model for [TargetGroup, TargetSubject, TargetMembership, TargetAttribute](https://github.com/Internet2/grouper/tree/master/grouper/src/grouper/edu/internet2/middleware/grouper/provisioning).
  
  
  
  1. The Provisioner instance can use the built in Target objects or can subclass those as needed
  2. Note: "Target attribute" is just the object model for being able to have arbitrary fields in target groups, subjects, and memberships. Its not really related to the attribute framework though it could probably be translated from it
3. The Custom Provisioner Implementation extends the [Base implementation](https://github.com/Internet2/grouper/blob/master/grouper/src/grouper/edu/internet2/middleware/grouper/provisioning/TargetProvisionerBase.java), and has methods for whatever is necessary. The Base provisioner has built in logic:
  
  
  
  1. Deal with Grouper objects
  2. Compute what needs to be inserted/updated/deleted
  3. Do the logging and error handling
  4. Interface with ["Sync" objects](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554422/Grouper+provisioning+daemon+tables)
  5. Triggered from real time change log, full sync daemon, and UI to fix one-offs
  6. Interface with UI so provisioning state is known by users or people troubleshooting
4. The provisioner uses standard configuration
  
  
  
  1. The UI will configure the provisioner via wizard. Has standard [validation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555162/Grouper+provisioning+configuration+validation) and documentation. This is a pass through to grouper-loader.properties configs in the database. You can configure directly in config files instead if you like (not recommended)
    
    
    
    1. Option for grouperIsAuthoritative to delete objects not in Grouper
  2. There is standard configuration for external systems with wizards
  3. [Configuration for daemons](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547876/Daemon+configuration) (real time and full sync)
5. The real time change log consumer has a [workflow built for provisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555332/Grouper+provisioning+incremental+workflow)
  
  
  
  1. Compute which events really need to be processed
  2. Convert between individual events and group-sync or full-sync
  3. Reduces the amount of logic needed in provisioners
  4. Check message queue for UI events to fix a membership or group etc
  5. Check message queue for "async" computations. i.e. a full sync that runs in the background and doesn't block other processes and sends messages to fix certain suggested objects
  6. Note, this runs as Java in the Grouper Daemon, not as an async provisioner. This is so we can tightly couple and keep track of things and not require extra processes
6. The real time change log for provisioners will consult the ["sync" tables](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554422/Grouper+provisioning+daemon+tables) to see if a task is already done, see if it is provisionable, etc
7. The full sync daemon runs as asynchronous or synchronous. Generally it is probably ok to run daily off hours as synchronous, but maybe an async run will be sufficient.
  
  
  
  1. Synchronous runs will block other processing, and uses the "sync tables" to block. This allows daemons to run on multiple servers and not need to run multiple times
  2. Asynchronous runs will not block and will send messages for individual objects to check, or will see there are so many updates it requires a blocking full sync
  3. Full syncs will bulk fetch from Grouper and the Target system (if the API supports it) and process everything in a short amount of time but using a lot of resources (e.g. memory). Note its possible that there is batch size to reduce the resources needed and not need as much resources.
  4. Full syncs will also try to batch inserts/updates/deletes (if API supports it)
  5. Note, this runs as Java in the Grouper Daemon, not as an async provisioner. This is so we can tightly couple and keep track of things and not require extra processes
8. Full syncs will bulk fetch from sync tables to consult and true up what Grouper thinks the state of the target system is
  
  
  
  1. Errors will trigger a recalc where cached data is ignored and updated as the real underlying data is fetched from grouper, subject source, and target system. There are options to take into consideration e.g. grouperIsAuthoritative
9. There is a new format for provisioning attributes on groups and folders. This format precalculates inheritance and has more options than the legacy PSPNG attributes
  
  
  
  1. Grouper will be able to migrate from one format to the new
  2. All provisioners will use standard provisioning attributes
  3. It is possible to use these attributes to override where an object is provisioned in the target (e.g. point a group or folder at a different OU in ldap
  4. This data (if provisionable and where) is copied to the "sync tables" so that there are no foreign keys and PIT is not needed when a group/member/membership is deleted
10. Grouper tables have Grouper's state for groups and memberships. Again as these are deleted or moved, the sync tables can facilitate efficient and accurate provisioning
11. Sync tables keep a [copy of state](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554422/Grouper+provisioning+daemon+tables) that does not have a foreign key to groups/members/memberships. There is a table for
  
  
  
  1. Provisioner
  2. Job (e.g. full or real time)
  3. Sync group: cache state from Grouper and Target. Is provisionable. Some stats. Timestamps. Error messages
  4. Sync member: cache state from Grouper and Target. Is provisoinable. Some stats. Timestamps. Error messages
  5. Sync membership: cache state from Grouper and Target. Is provisionable. Some stats. Timestamps. Error messages
  6. Log: keep log messages temporarily about a sync of a job/group/member/membership
12. Provisioning targets that need subject data might not use the subject ID or identifier in the member table, or might use other attributes
  
  
  
  1. The new USDU will resolve all subjects from all subject sources nightly, and while that data is being processed, the sync tables can be updated for what that provisioner needs
  2. Might need a different identifier
  3. Might need other attributes stored in JSON
13. The provisioner uses the sync table data to make provisioning more efficient, communicate and keep track of errors, log progress, survive Grouper object deletion
14. There is translation between the Grouper id's and names, the Target id's and names, and the Subject API id's and names
  
  
  
  1. Hopefully most of that can be handled with EL scripts
  2. Whatever is needed for the translation needs to be cached in sync tables (that is configurable)

## Data Model diagram

**Notes on Data Model diagram**

1. The [DAO](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555666/Grouper+provisioning+target+DAO) is how Java talks to the target. This would implement CRUD operations and hopefully do so in bulk
2. "Target beans" can be subclassed if needed
3. The provisioner specifies the class names of all the mandatory and optional subclasses
4. The logic is what takes the beans and decides what has changed and runs the actions to sync or notify or log
5. The configuration class will translate from the properties file to java, and validate the configuration
6. The translator will translate from Grouper objects to Target objects (and vise versa), based on configuration and potentially cache
7. The Grouper DAO will get objects from Grouper efficiently based on which field is being retrieved
8. The target classes can be subclassed for Grouper
9. The Subject DAO will get objects from the subject source
10. The target objects for subjects can be subclassed

## Provisioning tasks (for more [detail see this page](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=grouper&title=Grouper%20provisioner%20framework%20tasks&linkCreation=true&fromPageId=28544760))

| Task | Description | Status |
| --- | --- | --- |
| Ldap Dao | New interface implementation for efficient LDAP operations | In progress |
| External systems in UI | Endpoints, usernames, passwords, with wizards in UI | In progress |
| New [USDU](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548392/Universal+Subject+Daemon+Utility+USDU) daemon | Resolve all subjects, update unresolvable status, and cache provisioning sync attributes | Almost started |
| Provisioning configuration in UI | Mark folders and groups as provisionable and provide specific configuration | Done |
| Provisioning change log consumer | Look at "sync" objects and provide provisioner with "processed" view of work to do | Done |
| Shadow objects in database | "sync" tables keep track of target system status and cache attribute | Done |

## Targets

- [LDAP](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554930/Grouper+LDAP+provisioner)
- SQL
- SCIM
- google
- drop box (SCIM?) (todo)
- box
- [Duo](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554905/Grouper+Duo+provisioning)
- azure / o365
- zoom (todo)
- slack (SCIM?) (todo)
- servicenow (jeffW) (todo)
- canvas (Unicon) (todo)
- adobe (jeffW, liam) (todo)
- teamDynamix (liam) (todo)
- remedy
- papercut (liam) (todo)
- tableau (liam) (todo)
- salesforce (liam) (todo)

## Export a config

If you are reporting an issue with the provisioning framework, please follow these steps:

1. Ensure you are on a recent supported release of Grouper
2. Report your version number
3. Send sanitized configs for the provisioner either to the list or to a Grouper developer in a private slack
  
  
  
  1. Export your grouper-loader.properties from the UI
  2. Search for your provisioner config id
  3. Send that block of configs
  4. No need to send the daemon configs
  5. If there is a problem making calls to the target you might want to send the sanitized (no passwords or keys) configs of the external system for the provisioner

## Provisioning in LDAP/AD

See the [info here](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554930/Grouper+LDAP+provisioner).

## Provisioning to SQL

See the info [here](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554228/Grouper+SQL+provisioner)

## Provisioning to SCIM

See [this page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555423/Grouper+provisioning+SCIM)

## Provisioning to Box

See[this page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555350/Grouper+Box+Provisioner)

## Provisioning to Azure

See [this page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555567/Grouper+Entra+ID+Provisioner+Current+Azure+O365)

Provisioning to other systems

You could implement your own provisioner or use the provisioning framework to send messages

## See Also
