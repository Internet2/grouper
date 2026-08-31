---
title: "Grouper clob database type"
space: GrIntDev
pageId: 48792805
version: 14
lastUpdated: 2026-07-12T06:45:43.576Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792805/Grouper+clob+database+type
---

In Grouper 2.5.0 patches we will support large text widths in the database.

Different databases handle this differently. And it is less efficient than a varchar(4k). So

1. Have a varchar(4k) for data less than that
2. Have a column for larger data
  
  
  
  | Database | Large text column type | Max storage | DDLUtils | Hibernate | Notes |
  | --- | --- | --- | --- | --- | --- |
  | Oracle | clob | 4gig | yes | yes though might need a different mapping file |  |
  | Postgres | varchar | 10meg | no | yes with String | if we need more space, text goes up to a gig |
  | Mysql | mediumtext | 16meg | no | yes with String | if we need more space, longtext up to 4 gig |
  | Hsql | clob | 16meg | yes | yes though might need oracle mapping file |  |
  
  
  
  
  
  
  
  
  
  | Database | DDL |
  | --- | --- |
  | Oracle | ``` ALTER TABLE grouper_config ADD config_value_clob clob; ALTER TABLE grouper_config ADD config_value_clob_bytes integer; ``` |
  | Postgres | ``` ALTER TABLE grouper_config ADD COLUMN config_value_clob varchar(10000000); ALTER TABLE grouper_config ADD COLUMN config_value_clob_bytes BIGINT; ``` |
  | Mysql | ``` ALTER TABLE grouper_config ADD COLUMN config_value_clob mediumtext; ALTER TABLE grouper_config ADD COLUMN config_value_clob_bytes BIGINT; ``` |
  | Hsql | ``` ALTER TABLE grouper_config ADD COLUMN config_value_clob clob; ALTER TABLE grouper_config ADD COLUMN config_value_clob_bytes BIGINT; ``` |
3. Have a column with size of larger data column (which also indicates that the larger column is being used)
4. Note we can base64 encode binary data in here if needed

Use this for config in database, attributes, etc
