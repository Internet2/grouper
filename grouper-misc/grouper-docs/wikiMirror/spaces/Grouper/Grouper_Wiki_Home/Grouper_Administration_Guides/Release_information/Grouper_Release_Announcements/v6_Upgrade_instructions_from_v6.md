---
title: "v6 Upgrade instructions from v6"
space: Grouper
pageId: 28549643
version: 23
lastUpdated: 2026-07-19T00:32:40.916Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549643/v6+Upgrade+instructions+from+v6
---

When upgrading from Grouper v6 to another v6 container, this wiki will consolidate all the steps needed to perform that upgrade.

When upgrading from Grouper v4 to v6, also see [v6 Upgrade Instructions from v4](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547828/v6+Upgrade+Instructions+from+v4) for additional instructions which must be applied first. Also see the [v5 to v5 upgrade steps](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549165/v5+Upgrade+instructions+from+v5).

[v6 release notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547614/v6+Release+Notes)

> See information on [Grouper Versioning here](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544481/Versioning+Support+Policy)

Note, these are in reverse order, so go from bottom to top

| **Date** | **Upgrading from version** | **Upgrading to Version** | **Note for version** | **Importance** | **Jira** | **Step needed if...** | **Description** |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 2026/07/05 | ALL | ALL | 6.3.0 | Important | [GRP-7012](https://grouper.atlassian.net/browse/GRP-7012) | You have provisioning failsafes configured | Follow the steps in the Jira to review your configs |
| 2026/05/24 | ALL | ALL | 6.2.0 | Medium important | [GRP-6902](https://grouper.atlassian.net/browse/GRP-6902) | If you have encrypted configs which shouldn't be labeled as such | Follow the steps in the Jira to review your configs |
| 2026/05/18 | ALL | ALL | 6.2.0 | Important | [GRP-6964](https://grouper.atlassian.net/browse/GRP-6964) | If you have ".serviceUrl" in grouper-loader.properties | This config was renamed since it was causing issues.   You must refactor your configs. See the Jira. |
| 2026/03/21 | ALL | ALL | 6.1.1 | Not important | [GRP-6805](https://grouper.atlassian.net/browse/GRP-6805) | You use TeamDynamix provisioning | Run your team dynamix provisioner and adjust the batch sizes in the provisioner config as needed |
| 2026/02/09 | ALL | 6.0.1 | 6.0.1 | Important | [GRP-6652](https://grouper.atlassian.net/browse/GRP-6652) | You use Oracle for Grouper's DB and are installing 6.0.1 only (fixed in future versions). | Run this before the upgrade   ``` ALTER TABLE grouper_stems ADD CONSTRAINT grouper_stems_id_index_unq unique (id_index);   ``` |
| 2026/02/09 | ALL | ALL | 6.0.0 | Important |  | You use Grouper. | Grouper UI libraries were refactored. Thoroughly test the Grouper UI for your environment. |
| 2026/02/09 | ALL | ALL | 6.0.0 | Important |  | You use Grouper and have custom Java, GSH templates, GSH daemons, GSH scripts | Lots of libraries have changed. Make sure your Java compiles against Grouper v6.  If you have commons lang, change the import to commons lang 3 and optionally refactor called to deprecated methods.  Test your daemons, templates, scripts, hooks, etc.  This is a useful query for the commons lang if config is in database. Note in oracle you need to do something differently since clobs do not query like this   ``` SELECT config_file_name, config_key, config_value, config_value_clob FROM grouper_config WHERE config_value LIKE '%commons.lang.%' or config_value_clob like '%commons.lang.%' ;  -- just change commons.lang to commons.lang3  -- ##################  SELECT config_file_name, config_key, config_value, config_value_clob FROM grouper_config WHERE config_value LIKE '%LoaderLdapElUtils%' or config_value_clob like '%LoaderLdapElUtils%' ;  -- ask AI what to refactor, e.g.  -- ${edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapElUtils.normalize("NFKD", edu.internet2.middleware.grouper.util.GrouperUtil.defaultString(gcGrouperSyncMember.getEntityAttributeValueCache1(), grouperProvisioningEntity.getSubjectIdentifier0())).replaceAll("\p{M}", "")}  -- can take out normalization and just configure in provisioner: Advanced -> Remove accented characters: ${edu.internet2.middleware.grouper.util.GrouperUtil.defaultString(gcGrouperSyncMember.getEntityAttributeValueCache1(), grouperProvisioningEntity.getSubjectIdentifier0())}  -- or can be ${edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapElUtils.normalize("NFKD", edu.internet2.middleware.grouper.util.GrouperUtil.defaultString(gcGrouperSyncMember.getEntityAttributeValueCache1(), grouperProvisioningEntity.getSubjectIdentifier0())).replaceAll("\\p{M}", "")}   -- ###############  SELECT config_file_name, config_key, config_value, config_value_clob FROM grouper_config WHERE config_value LIKE '%SubjectApiUtils%' or config_value_clob like '%SubjectApiUtils%' ;  SELECT config_file_name, config_key, config_value, config_value_clob FROM grouper_config WHERE config_value LIKE '%SubjectUtils%' or config_value_clob like '%SubjectUtils%' ;  SELECT config_file_name, config_key, config_value, config_value_clob FROM grouper_config WHERE config_value LIKE '%GrouperUtilElSafe%' or config_value_clob like '%GrouperUtilElSafe%' ; ```  Just edit those GSH templates or daemons and change from commons.lang. to commons.lang3. |
| 2026/02/09 | ALL | ALL | 6.0.0 | Important |  | You use Grouper | In the grouper.hibernate.properties, set this to a v6 version, e.g. 6.*.*   ``` registry.auto.ddl.upToVersion = 6.*.* ``` |
| 2026/02/09 | ALL | ALL | 6.0.0 | Important |  | You use Grouper | Make sure you have reviewed all [v5 to v5 upgrade steps](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549165/v5+Upgrade+instructions+from+v5) |

If you want to run v6 locally, you can do something like this (change port, version, database url if not on mac): (use a random password, not 'ymxHNQnH0aYgxlcQx1iQ')

```
$ docker run --name postgres -e POSTGRES_PASSWORD=ymxHNQnH0aYgxlcQx1iQ -d -p 5432:5432 postgres:14
$ docker exec -it -u postgres postgres psql
# CREATE USER grouper PASSWORD 'ymxHNQnH0aYgxlcQx1iQ';
# CREATE DATABASE grouper;
# GRANT ALL PRIVILEGES ON DATABASE grouper TO grouper;
# \q
```

```
    docker run -d -p 8081:8080 --name my-grouper \
           -e GROUPER_UI_GROUPER_AUTH=true \
           -e GROUPER_SELF_SIGNED_CERT=true \
           -e GROUPER_AUTO_DDL_UPTOVERSION='v6.*.*' \
           -e GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES='0.0.0.0/0' \
           -e GROUPERSYSTEM_QUICKSTART_PASS=ymxHNQnH0aYgxlcQx1iQ \
           -e GROUPER_UI=true \
           -e GROUPER_DATABASE_URL="jdbc:postgresql://docker.for.mac.localhost:5433/grouper?currentSchema=public" \
           -e GROUPER_DATABASE_USERNAME=grouper \
           -e GROUPER_DATABASE_PASSWORD=ymxHNQnH0aYgxlcQx1iQ \
           i2incommon/grouper:6.0.0 ui

```
