---
title: "Grouper instrumentation I2 telemetry"
space: GrIntDev
pageId: 48793927
version: 6
lastUpdated: 2026-07-12T06:46:25.995Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793927/Grouper+instrumentation+I2+telemetry
---

In order to better maintain and have visibility into how the community is using the products, the component architects have been charged with coming up with additional data that can be centrally and anonymously collected. This table is meant to be used as a strawman to collect potential items that may be useful for Grouper.

Currently we are thinking that the data-structure is name/value pairs, and we can see graphs of values, see changes over time, and potentially be able to correlate patterns across products (if deployers configure an institutional opaque identifier across products).

| **Name** | **Example value** | **Notes** |
| --- | --- | --- |
| tbProduct | grouper | Name of product |
| tbProductVersion | 4.0.1 | Version of product |
| tbStableRelease | true | If this release is stable |
| tbSupportedRelease | false | If this is a recent release which is considered supported |
| tbMaintainer | i2incommon | Maintainer value from Container |
| tbEnvironmentUUID | acb-123-xyz-890 | UUID in database to tie together nodes in one env if component allows it) |
| tbEnvironmentLabel | prod | Optional env label, should be anonymous |
| tbInstitutionUUID | def-456-qrs-789 | Optional institution UUID to tie components together |
| gpEnhancementRelease | false | If this is an enhancement release, as opposed to the non-enhancement version |
| gpGroups | 10000 | Number of groups |
| gcGroups_type_policy (for each type) | 89 | Number of groups for each type |
| gpMemberships | 100000 | Number of memberships |
| gpFeatureProvisioning_SCIM | true | SCIM is utilized |
| gpFeatureProvisioningEventCount_SCIM | 100000 | Event count per day |
| gpFeatureProvisioning_LDAP | true | LDAP provisioning is utilized |
| gpFeatureProvisioningEventCount_LDAP | 50000 | Event count per day |
| gpFeatureLoaderLDAP | true | LDAP loader is used |
| gpFeatureLoaderSQL | false | SQL loader is unused |
| gpLoaderCount | 200 | Number of loaders |
| gpUiUsersPerDay | 400 | Number of users per day |
| gpWsCallsPerDay | 30000 | Number of WS calls per day |
| gpChangeLogEventsPerDay | 100000 | Number of change log events per day |
| gpNumberOfSubjectSources | 3 |  |
| gpNumberOfSubjects | 1000000 | Not including built in subject sources |
| gpSubjectSourceCount_Ldap | 1 | how many ldap sources |
| gpSubjectSourceCount_Sql | 0 | how many sql sources |
| gpSubjectSourcePeople | 900000 | Number of people in subject source, as opposed to systems |
| gpScriptedGroups | true | If using scripted groups |
| gpScriptedGroupCount | 10 | How many scripted groups |
| gpMemoryUtilization | 10000000000 | 10g of memory |
| gpCpuAvgPercent | 30 | 30% cpu utilization avg |
| gpGshTemplates | 10 | Number of GSH templates |
| gpGshTemplatesDailyRun | 34 | Number of GSH templates run per day |
| gpCustomUis | 15 | Number of custom UIs |
| gpCustomUisDailyRun | 89 | Custom UI run per day |
| gpUiMembershipManualAddDailyCount | 253 | Manual membership add events ( not from loader/scripts/etc) |
| gpUiMembershipManualRemoveDailyCount | 123 | Manual membership removed events ( not from loader/scripts/etc) |
| gpUiMembershipManualUpdateDailyCount | 12 | Manual membership updated events ( not from loader/scripts/etc) |
| gpUiUsersMakingChangesDailyCount | 913 | count of unique users making membership changes (vs login to UI and "just looking".) |
| gpGroupCreateDailyCount | 32 | Groups created/deleted/updated |
| gpGroupDeleteDailyCount | 23 | Groups deleted |
| gpGroupUpdateDailyCount | 12 | Groups updated |
| gpAttributesAssignedDailyCount | 65 | Attributes assigned |
| gpAttributesRemovedDailyCount | 11 | Attributes removed |
| gpAttributeValuesAssignedDailyCount | 45 | Attribute values assigned |
| gpAttributeValuesRemovedDailyCount | 9 | Attribute values removed |
| gpAttributeValuesUpdatedDailyCount | 32 | Attribute values updated |
| gpFolderCreateDailyCount | 12 | Folders created |
| gpFolderDeleteDailyCount | 18 | Folders deleted |
| gpFolderUpdateDailyCount | 20 | Folders updated |
| gpJwtCreateDailyCount | 45 | JWTs used |
| gpJwtUseDailyCount | 12 | JWTs created |
| gpJwtExpiredDailyCount | 4 | JWTs expired |
| gpRulesCreateDailyCount | 2 | Rules created |
| gpRulesDeleteDailyCount | 3 | Rules deleted |
| gpRulesUpdateDailyCount | 6 | Rules updated |
| gpRulesFiredDailyCount | 1234 | count of number of times Rule is triggered today |
| gcAttestableGroupCount | 56 |  |
| gcAttestationNeedsForGroupCount | 12 |  |
| gcAttestableReportCount | 14 |  |
| gcAttestationNeedsForReportCount | 3 |  |
