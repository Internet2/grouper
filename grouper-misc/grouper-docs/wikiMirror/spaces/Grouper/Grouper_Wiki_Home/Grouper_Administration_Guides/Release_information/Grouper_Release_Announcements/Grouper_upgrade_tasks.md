---
title: "Grouper upgrade tasks"
space: Grouper
pageId: 28549372
version: 41
lastUpdated: 2026-08-14T17:27:15.518Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks
---

View upgrade tasks in v7.3.0+ in the UI: Miscellaneous → Configure → Upgrade tasks

## Upgrade tasks

| **Task #** | **Notes** | **DDL?** | **Run on new install?** | **Released in version** | **Applicable version** | **Substantial change?** |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | Add missing self group sets For groups | N | N | 2.5.1 | v4+ | Yes |
| 2 | Move subject resolution status attributes to member table ([GRP-2841](https://grouper.atlassian.net/browse/GRP-2841)) | N | N | 2.5.30 | v4+ | No |
| 3 | Refactor group recent memberships ([GRP-2874](https://grouper.atlassian.net/browse/GRP-2874)) | N | N | 2.5.30 | v4+ | No |
| 4 | Reconfigure recent memberships ([GRP-2893](https://grouper.atlassian.net/browse/GRP-2893)) | N | N | 2.5.32 | v4+ | No |
| 5 | Change inherited privs to act as GrouperSystem ([GRP-2926](https://grouper.atlassian.net/browse/GRP-2926)) | N | N | 2.5.36 | v4+ | No |
| 6 | Add missing self group sets for stems | N | N | 2.6.6 | v4+ | No |
| 7 | Store folderUuidToShow in db for grouper gsh templates | N | N | 2.6.8 | v4+ | No |
| 8 | Set grouper_members id_index column to non nullable ([GRP-4332](https://grouper.atlassian.net/browse/GRP-4332)) | Y | N | 2.6.16 | v4+ | No |
| 9 | Make permissions limits not public ([GRP-4633](https://grouper.atlassian.net/browse/GRP-4633)) | N | N | 4.1.0 | v4+ | No |
| 10 | Add indexes to grouper loader logs ([GRP-5195](https://grouper.atlassian.net/browse/GRP-5195)) | Y | N | 4.10.0 | v4+ | No |
| 11 | Create grouper sync dependency table ([GRP-5302](https://grouper.atlassian.net/browse/GRP-5302)) | Y | N | 4.10.4 | v4+ | No |
| 12 | Create provisioning SCIM loader tables ([GRP-5514](https://grouper.atlassian.net/browse/GRP-5514)) | Y | N | 4.14.0 | v4+ | No |
| 13 | Create provisioning azure loader table ([GRP-5625](https://grouper.atlassian.net/browse/GRP-5625)) | Y | N | 4.15.0 | v4+ | No |
| 14 | Create provisioning adobe loader table | Y | N | 4.16.0 | v4+ | No |
| 15 | Moved some DDL from v45/v46 sql script files which included: ([GRP-4759](https://grouper.atlassian.net/browse/GRP-4759))    - Add internal_id to members table along with index - Add internal_id to fields table along with index - Add internal_id to groups table along with index - Create grouper_sql_cache_group table along with index - Create grouper_sql_cache_mship table along with indexes - Add source_internal_id to pit members table along with index - Add source_internal_id to pit fields table along with index - Add source_internal_id to pit groups table along with index - Create grouper_sql_cache_group_v view  Populate internal id on groups and fields tables and make not null  For Oracle, add a few constraints to grouper_fields, grouper_groups and grouper_sql_cache_group | Y | N | 5.8.0 | v5+ | Yes |
| 16 | Make grouper members internal id not null | Y | N | 5.8.0 | v5+ | Yes |
| 17 | Update point in time internal id where null ([GRP-4799](https://grouper.atlassian.net/browse/GRP-4799)) | Y | N | 5.8.2 | v5+ | Yes |
| 18 | Remove group sync job ([GRP-5346](https://grouper.atlassian.net/browse/GRP-5346)) | N | N | 5.8.5 | v5+ | No |
| 19 | Remove grouper report job ([GRP-5331](https://grouper.atlassian.net/browse/GRP-5331)) | N | N | 5.11.0 | v5+ | No |
| 20 | Remove maintenance jobs ([GRP-5315](https://grouper.atlassian.net/browse/GRP-5315) \| [GRP-5316](https://grouper.atlassian.net/browse/GRP-5316) \| [GRP-5321](https://grouper.atlassian.net/browse/GRP-5321) \| [GRP-5322](https://grouper.atlassian.net/browse/GRP-5322)) | N | N | 5.12.0 | v5+ | No |
| 21 | Refactor sql group cache and membership table ([GRP-5717](https://grouper.atlassian.net/browse/GRP-5717)) | Y | N | 5.13.0 | v5+ | No |
| 22 | Add id index to point in time stem and attribute def tables ([GRP-5717](https://grouper.atlassian.net/browse/GRP-5717)) | Y | N | 5.13.0 | v5+ | No |
| 23 | Delete sql cacheable attributes ([GRP-5737](https://grouper.atlassian.net/browse/GRP-5737)) | N | N | 5.13.1 | v5+ | No |
| 24 | Convert membership cache timestamp to number ([GRP-5781](https://grouper.atlassian.net/browse/GRP-5781)) | Y | N | 5.14.0 | v5+ | Yes |
| 25 | Create or recreate membership history cache table ([GRP-5792](https://grouper.atlassian.net/browse/GRP-5792)) | Y | N | 5.14.0 | v5+ | No |
| 26 | None, skipped | N | N |  | v5+ | No |
| 27 | Add functions to convert millis/micros from epoch to timestamp ([GRP-5822](https://grouper.atlassian.net/browse/GRP-5822) \| [GRP-6010](https://grouper.atlassian.net/browse/GRP-6010)) | Y | N | 5.14.0  5.17.0 (Oracle) | v5+ | No |
| 28 | Add DDL for sql cache dependency tables ([GRP-5822](https://grouper.atlassian.net/browse/GRP-5822)) | Y | N | 5.14.0 | v5+ | No |
| 29 | Add index grouper_sync_mship_mem_idx ([GRP-5877](https://grouper.atlassian.net/browse/GRP-5877)) | Y | N | 4.17.0 | v4+ | No |
| 30 | Add history tables for data fields ([GRP-5972](https://grouper.atlassian.net/browse/GRP-5972)) | Y | N | 5.15.5 | v5+ | No |
| 31 | Update tables for data fields ([GRP-6032](https://grouper.atlassian.net/browse/GRP-6032)) | Y | N | 5.17.0 | v5+ | No |
| 32 | Add views grouper_sql_dependency_group_v,    grouper_sql_dependency_row_v,   grouper_sql_dependency_attr_v ([GRP-6175](https://grouper.atlassian.net/browse/GRP-6175)) | Y | N | 5.18.0  5.18.2 (Oracle) | v5+ | No |
| 33 | Adds dependencies for sql cache full sync to run in v4 | Y | Y | 4.19.0 | v4 only | No |
| 34 | Run sql cache full sync ([GRP-6285](https://grouper.atlassian.net/browse/GRP-6285)) | N | N | 5.20.1 | v5+ | Yes |
| 35 | Added indexes fld_assgn_mem_df_dict_idx and dtrwfldasg_df_dict_dra_idx ([GRP-6329](https://grouper.atlassian.net/browse/GRP-6329)) | Y | N | 5.21.1 | v5+ | No |
| 36 | grouper_prov_duo_user primary key should be (user_id, config_id), not just user_name ([GRP-6468](https://grouper.atlassian.net/browse/GRP-6468)) | Y | N | 6.0.0 | v4+ | No |
| 37 | Tables: grouper_lifecycle_event, grouper_lifecycle_event_config ([GRP-6541](https://grouper.atlassian.net/browse/GRP-6541))   Constraint for oracle: grouper_stems_id_index_unq   ``` Success: upgrade task output: , created table grouper_lifecycle_event_config,  added index grouper_lcycle_evnt_cnfg_idx, added foreign key group_internal_id_fk,  added foreign key stem_id_index_fk, added foreign key data_field_internal_id_fk,  added foreign key data_row_internal_id_fk, created table grouper_lifecycle_event,  added index grouper_lifecycle_event_uniq_idx,  added foreign key lcycl_evnt_cnfg_intrnl_id_fk,  added foreign key member_internal_id_fk,  added foreign key lng_priv_dic_intrnl_id_fk,  added foreign key lng_unpriv_dic_intrnl_id_fk  Upgraded to version V37.    ``` | Y | N | 6.0.0 | v7+ | No |
| 38 | Tables: grouper_oauth_client, grouper_oauth_code, grouper_oauth_pend_authz_req, grouper_mcp_tool_log   ``` Success: created table grouper_oauth_client, added index grouper_oauth_client_idx,  created table grouper_oauth_code, added index grouper_oauth_code_idx,  added index grouper_oauth_code_exp_idx, added index grp_oauth_code_client_idx,  created table grouper_oauth_pend_authz_req, added index grp_oauth_pend_req_idx,  added index grp_oauth_pend_exp_idx, added index grp_oauth_pend_client_idx,  created table grouper_mcp_tool_log, added index grp_mcp_tool_log_member_idx,  added index grp_mcp_tool_log_started_idx, added index grp_mcp_tool_log_name_idx,  added index grp_mcp_tool_log_oauth_idx  Upgraded to version V38.  ``` | Y | N |  | v7+ | No |
| 39 | Comments on tables: grouper_lifecycle_event_config, grouper_lifecycle_event  Add indexes: fld_assgn_field_dict_idx, fld_assgn_field_int_idx | Y | N | 6.2.0, 7.1.0 | v6+ | No |
| 40 | Redo index grouper_duo_user_user_name_idx (non unique), and add index: group_set_member_member_field_idx | Y | N | 4.23.0, 6.2.0, 7.1.0 | v4+ | No |
| 41 | Add generic grouper prov tables to store target data | Y | N | 7.2.0 | v7+ | Yes |
| 42 | Drop unnecessary indexes on grouper_change_log_entry_temp | Y | N | 7.2.0 | v7+ | No |
| 43 | Widen grouper_members.subject_identifier0  Add primary key grouper_stem_view_privilege.grouper_stem_v_priv_pk | Y | N | 7.3.0 | v7+ | No |
| 44 | Encrypt the OAuth JWT signing private key at rest in database config | N | N | 7.4.0 | v7+ | No |

## Skip an upgrade task

You need an attribute and value assigned to the ***etc:attribute:upgradeTasks:upgradeTasksMetadataGroup*** group

Note, Grouper will not start by default if there is an upgrade task error, you can temporarily set this grouper.hibernate.properties config to allow grouper to start when there is an upgrade task failure. You can go into the UI and set upgrade tasks to skip and set this back.

```
upgradeTasksFailOnStartupIfError = true
```

Or you can set this env variable

```
GROUPER_UPGRADE_TASKS_FAIL_ON_STARTUP_IF_ERROR=false
```
