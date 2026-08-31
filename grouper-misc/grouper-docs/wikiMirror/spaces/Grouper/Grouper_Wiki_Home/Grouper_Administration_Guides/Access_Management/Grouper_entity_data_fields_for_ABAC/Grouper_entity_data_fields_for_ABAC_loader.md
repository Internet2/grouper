---
title: "Grouper entity data fields for ABAC loader"
space: Grouper
pageId: 28549819
version: 3
lastUpdated: 2026-07-01T05:41:11.411Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549819/Grouper+entity+data+fields+for+ABAC+loader
---

If you get data into Grouper and want to use that data there are views for the data field assigns, row assigns, and row field assigns (columns).

## Example loader from data fields

```
select subject_id, subject_source_id, 
'app:crashplan:service:ref:roles:' || value_text as group_name 
from grouper_data_field_assign_v dfa where data_field_config_id = 'cp_role'
```

## Example loader from data rows

get a row where active is true, take the org from that row, and replace invalid chars in postgres

```
select subject_id, subject_source_id, 
'app:crashplan:service:ref:orgs:' || regexp_replace(value_text, '[^0-9a-zA-Z_-]+','_','g') as group_name 
from grouper_data_row_field_asgn_v drav
where data_field_config_id = 'cp_org'
and exists (select 1 from grouper_data_row_field_asgn_v drav2
where drav2.data_row_assign_internal_id = drav.data_row_assign_internal_id 
and drav2.data_field_config_id = 'cp_active'
and drav2.value_integer = 1)
order by data_row_assign_internal_id
```
