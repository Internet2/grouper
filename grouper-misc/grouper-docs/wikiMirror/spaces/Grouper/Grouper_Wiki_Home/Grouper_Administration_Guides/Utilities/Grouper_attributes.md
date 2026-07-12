---
title: "Grouper attributes"
space: Grouper
pageId: 28544033
version: 18
lastUpdated: 2026-07-01T05:48:56.903Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544033/Grouper+attributes
---

| Attribute Def/Attribute def name | Store in audit/point in time | Notes |
| --- | --- | --- |
| deprovisioning | No | Not being stored already according to GrouperConfig.java |
| grouperObjectTypeDef | Yes | GrouperObjectTypesJob recalculates all the values daily and updates them. Actions from the UI also trigger inserts/updates. |
| provisioningDef | Yes | GrouperProvisioningJob recalculates all the values daily and updates them. Actions from the UI also trigger inserts/updates. |
| reportConfigDef | No | GrouperReportJob whose frequency is defined by the users through the UI, inserts/updates entries. GrouperReportClearJob which runs daily also inserts entries in audit tables. |
| subjectResolutionDef | No | USDU job runs once a week and inserts/updates the attributes. |
| workflowConfigDef | No | Attributes are inserted/updated when users perform actions like request to be added to a group, approve a request, etc. On the backend, GrouperWorkflowDaemonJob updates the entries based on if emails need to be sent. |
| attestationDef | No | Not being stored already according to GrouperConfig.java |
| externalSubjectInviteDef | No | It looks like entries are inserted based on users' actions from the UI. There's no daemon job. |
| attributeDefLoaderTypeDef | Yes | It looks like, entries are inserted/updated only once when the loader job is configured. |
| entitySubjectIdentifierDef | Yes | It looks like it's not getting updated with any daemon jobs. |
| grouperLoaderLdapDef | Yes | It looks like entries are inserted/updated only once when the loader job is configured. |
| loaderMetadataDef | No | It stores metadata of loader run instances and gets updated every time loader runs. |
| grouperMessageQueueDef | Yes | It is used to store grouper builtin messaging queue names |
| grouperMessageTopicDef | Yes | It is used to store grouper builtin messaging topic names |
| limitsDef | Yes |  |
| limitsDefInt | Yes |  |
| limitsDefMarker | Yes |  |
| rulesTypeDef | Yes |  |
| rulesAttrDef | Yes |  |
| upgradeTasksDef | Yes |  |
| grouperUserDataDef | No |  |
| legacyGroupTypeDef_grouperLoader | Yes | It looks like, entries are inserted/updated only once when the loader job is configured. |
