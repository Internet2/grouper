---
title: "v7 Upgrade instructions from v7"
space: Grouper
pageId: 28549096
version: 33
lastUpdated: 2026-10-11T04:40:21.180Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549096/v7+Upgrade+instructions+from+v7
---

When upgrading from Grouper v7 to another v7 image, this wiki will consolidate all the steps needed to perform that upgrade.

When upgrading from Grouper v4 to v6, also see [v6 Upgrade Instructions from v4](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547828/v6+Upgrade+Instructions+from+v4+v5) for additional instructions which must be applied first. Also see the [v5 to v5 upgrade steps](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549165/v5+Upgrade+instructions+from+v5) and [v6 to v6 upgrade steps](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549643/v6+Upgrade+instructions+from+v6).

> See information on [Grouper Versioning here](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544481/Versioning+Support+Policy)

Note, these are in reverse order, so go from bottom to top

| Date | Upgrading from version | Upgrading to Version | Note for version | Importance | Jira | Step needed if... | Description |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 2026/10/11 | 7.7.x and earlier | ALL | 7.8.0 | Medium important | [GRP-7462](https://grouper.atlassian.net/browse/GRP-7462) | You use the Grouper MCP server | Upgrade task 46 adds the column `grouper_mcp_tool_log.entry_path` (and the AI assistant tables). Run upgrade task 46, or have auto-DDL on, before 7.8.0 serves MCP traffic.  Until it runs, every MCP tool log insert fails. The error is logged and tool calls still work, but the audit rows are lost, MCP throttling (which counts those rows) does not limit anyone, and the MCP tool logs screen in the UI shows an error. |
| 2026/10/05 | ALL | ALL | 7.7.0 | Important | [GRP-7418](https://grouper.atlassian.net/browse/GRP-7418) | Users who are not Grouper sysadmins are in the MCP SQL or admin groups: `grouper.mcp.users.canRunSqlReadonly`, `grouper.mcp.users.adminReadonly` or `grouper.mcp.users.adminReadWrite` | The sysadmin requirement documented for these groups is now enforced. A member of `canRunSqlReadonly` or `adminReadWrite` who is not a sysadmin, or a member of `adminReadonly` who is neither a sysadmin nor a readonly sysadmin, gets no access from that group.  To keep giving those users access, move them to the new limited groups ([GRP-7415](https://grouper.atlassian.net/browse/GRP-7415)): `grouper.mcp.users.canRunSqlReadonlyLimited`, `grouper.mcp.users.adminReadonlyLimited` and `grouper.mcp.users.adminReadWriteLimited`. Then open specific databases, tools, LDAP and external systems to them with the `limitedAccessGroup` settings (e.g. `grouper.mcp.sql.<externalSystemId>.limitedAccessGroup`, `grouper.mcp.tool.<toolName>.limitedAccessGroup`). Those groups must be in the etc:mcp folder. Limited admin readwrite can only run loader jobs of groups the user could refresh in the UI. |
| 2026/10/04 | ALL | ALL | 7.7.0 | Medium important | [GRP-7436](https://grouper.atlassian.net/browse/GRP-7436) | You configured `ignoreIfMatchesValue` on any provisioner `targetGroupAttribute`, `targetEntityAttribute` or `targetMembershipAttribute` | This setting was loaded but never applied. It now is: a group or entity matching the value (on the Grouper side or in the target) is never inserted, updated or deleted, and neither are its memberships. A matching membership is left alone too. Ignored objects are counted as `ignoredGroups`, `ignoredEntities` and `ignoredMemberships` in the job log.  Review each configured value before upgrading, since it will start taking effect. For groupAttributes / entityAttributes, a value on the membership attribute itself ignores just those memberships, not the group or entity. |
| 2026/09/28 | 7.4.x and earlier | ALL | 7.6.0 | Medium important | [GRP-7269](https://grouper.atlassian.net/browse/GRP-7269) | You use the MCP SQL tools (sql_select, sql_get_schema) against the Grouper database | As of 7.5.0 no database is available to the MCP SQL tools by default, and `grouper.mcp.sqlGrouperExternalSystem` is no longer used. Without a configured database the SQL tools are not offered to AI clients at all.  In grouper.properties add `grouper.mcp.sql.grouper.grouperDatabase = true`. Better, point it at a read-only connection, e.g. `grouper.mcp.sql.grouperReadOnly.grouperDatabase = true` with `grouperClient.jdbc.grouperReadOnly.*` configured. If you had set `sqlGrouperExternalSystem`, use that external system id instead of `grouper`. |
| 2026/09/27 | ALL | ALL | 7.6.0 | Not important | [GRP-7291](https://grouper.atlassian.net/browse/GRP-7291) | You have processes that add groups as members of other groups | Circular group memberships are now vetoed at any depth, not just a group added to itself (e.g. A in B and B in A). A process that creates such a chain will now get an error. Review loaders, scripts, and provisioning that add group members. |
| 2026/09/26 | ALL | ALL | 7.5.1 | Important | [GRP-7359](https://grouper.atlassian.net/browse/GRP-7359) | Users who are not sysadmins create or edit rules in the UI | Security fix. Non-sysadmins can now only add or edit rules from the predefined patterns, and the rule must match the selected pattern. Custom rules and rules that use expression language (EL) are sysadmin-only.  Set `rules.restrictRulesUiToMembersOfThisGroupName` (grouper.properties) to a group of trusted power users, and audit existing rules. See [GRP-7359](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/240549893/Grouper+bug+GRP-7359+-+Grouper+UI+rules+privilege+escalation). |
| 2026/09/26 | ALL | ALL | 7.5.1 | Medium important | [GRP-7381](https://grouper.atlassian.net/browse/GRP-7381) | Users who are not sysadmins create rules that send email | If neither `rules.restrictRulesEmailSendersToMembersOfThisGroupName` nor `rules.restrictRulesUiToMembersOfThisGroupName` is set, only sysadmins can now create rules that send email. Set the rules UI group to your rule authors, or set `rules.allowSendEmailRulesWhenNoGroupConfigured = true` to restore the previous behavior. |
| 2026/07/05 | ALL | ALL | 7.3.0 | Medium important | [GRP-7076](https://grouper.atlassian.net/browse/GRP-7076) | If you use Postgres | Follow the steps in the Jira to widen    grouper_members.subject_identifierX |
| 2026/07/05 | ALL | ALL | 7.3.0 | Not important | [GRP-7076](https://grouper.atlassian.net/browse/GRP-7076) | You use Grouper | DDL upgrade tasks ([43](https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)) |
| 2026/07/05 | ALL | ALL | 7.3.0 | Important | [GRP-7012](https://grouper.atlassian.net/browse/GRP-7012) | You have provisioning failsafes configured | Follow the steps in the Jira to review your configs |
| 2026/05/24 | ALL | ALL | 7.2.0 | Medium important | [GRP-6902](https://grouper.atlassian.net/browse/GRP-6902) | If you have encrypted configs which shouldn't be labeled as such | Follow the steps in the Jira to review your configs |
| 2026/05/18 | ALL | ALL | 7.2.0 | Not important | [GRP-6817](https://grouper.atlassian.net/browse/GRP-6817),    [GRP-6654](https://grouper.atlassian.net/browse/GRP-6654),   [GRP-6876](https://grouper.atlassian.net/browse/GRP-6876),   [GRP-6655](https://grouper.atlassian.net/browse/GRP-6655),   [GRP-6968](https://grouper.atlassian.net/browse/GRP-6968) | You use Grouper | DDL upgrade tasks ([41, 42](https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)) |
| 2026/05/18 | ALL | ALL | 7.2.0 | Important | [GRP-6964](https://grouper.atlassian.net/browse/GRP-6964) | If you have ".serviceUrl" in grouper-loader.properties | This config was renamed since it was causing issues.   You must refactor your configs. See the Jira. |
| 2026/04/05 | ALL | ALL | 7.1.0 | Not important | [GRP-6876](https://grouper.atlassian.net/browse/GRP-6876)   [GRP-6817](https://grouper.atlassian.net/browse/GRP-6817)   [GRP-6654](https://grouper.atlassian.net/browse/GRP-6654) | You use Grouper | Two minor DDL upgrade tasks ([39, 40](https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28549372/Grouper+upgrade+tasks)) |
| 2026/04/05 | ALL | ALL | 7.1.0 | Not important | [GRP-6838](https://grouper.atlassian.net/browse/GRP-6838) | You use history in ABAC | If you are accumulating history, it might not be working correctly.    Get number of rows in grouper_data_row_assign_hst, grouper_data_row_field_asn_hst   If it is abnormally large (e.g. you had composite keys on data rows which were not strings)   You should truncate those tables and start history collection again.    Before v7.1.0/v6.2.0 the data was not used anyways.   ``` TRUNCATE TABLE grouper_data_row_field_asn_hst; TRUNCATE TABLE grouper_data_row_assign_hst; ```  Note, if that doesnt work, you can delete from or commit or work with it until they are both empty. in postgres   you can trunctate both in one line: TRUNCATE TABLE grouper_data_row_field_asn_hst, grouper_data_row_assign_hst; |
| 2026/04/05 | ALL | ALL | 7.1.0 | Not important | [GRP-6872](https://grouper.atlassian.net/browse/GRP-6872) | You use ABAC | Set your abac default and available subject sources: [GRP-6872](https://grouper.atlassian.net/browse/GRP-6872) |
| 2026/03/21 | ALL | ALL | 7.0.3 | Not important | [GRP-6805](https://grouper.atlassian.net/browse/GRP-6805) | You use TeamDynamix provisioning | Run your team dynamix provisioner and adjust the batch sizes in the provisioner config as needed |
| 2026/03/07 | ALL | ALL | 7.0.1 | Not important |  | You want to enable MCP in Grouper for AI | Set this env variable in your container:   ``` GROUPER_MCP=true ```  You might want to edit the etc:mcp* groups and add eligibility to them (e.g. some sort of workforce reference group)    In v7.0.1 ONLY, you should run the this GSH before using MCP (e.g. at first startup)   ``` edu.internet2.middleware.grouper.authentication.GrouperOAuthSigningKey.getPublicKey();  ``` |
| 2026/03/06 | ALL | ALL | 7.0.1 | Medium important |  | You use Grouper | Upgrade task 38 has DDL with new tables for MCP |
| 2026/03/06 | ALL | ALL | 7.0.1 | Important |  | You use Grouper | In the grouper.hibernate.properties, set this to a v7 version, e.g. 7.*.*   ``` registry.auto.ddl.upToVersion = 7.*.* ``` |
| 2026/03/06 | ALL | ALL | 7.0.1 | Important |  | You use Grouper | Make sure you have reviewed all [v6 to v6 upgrade steps](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549165/v5+Upgrade+instructions+from+v5) |

If you want to run v7 locally, you can do something like this (change port, version, database url if not on mac): (use your own random password, not 'jM0wlZE3xDGyHSo0lujl')

```
$ docker run --name postgres -e POSTGRES_PASSWORD=jM0wlZE3xDGyHSo0lujl -d -p 5432:5432 postgres:14
$ docker exec -it -u postgres postgres psql
# CREATE USER grouper PASSWORD 'jM0wlZE3xDGyHSo0lujl';
# CREATE DATABASE grouper;
# GRANT ALL PRIVILEGES ON DATABASE grouper TO grouper;
# \q
```

```
    docker run -d -p 8081:8080 --name my-grouper \
           -e GROUPER_UI_GROUPER_AUTH=true \
           -e GROUPER_SELF_SIGNED_CERT=true \
           -e GROUPER_AUTO_DDL_UPTOVERSION='v7.*.*' \
           -e GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES='0.0.0.0/0' \
           -e GROUPERSYSTEM_QUICKSTART_PASS=jM0wlZE3xDGyHSo0lujl \
           -e GROUPER_UI=true \
           -e GROUPER_DATABASE_URL="jdbc:postgresql://docker.for.mac.localhost:5433/grouper?currentSchema=public" \
           -e GROUPER_DATABASE_USERNAME=grouper \
           -e GROUPER_DATABASE_PASSWORD=jM0wlZE3xDGyHSo0lujl \
           i2incommon/grouper:7.X.Y ui

```
