---
title: "Grouper attribute framework queries"
space: Grouper
pageId: 28549459
version: 5
lastUpdated: 2026-07-01T05:41:59.316Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549459/Grouper+attribute+framework+queries
---

Some attribute queries use views like this:

Traditional query with views

```

select
	gg.id,
	gg.name,
	gg.display_name,
	gg.description,
	gg.id_index
from
	grouper_groups gg,
	grouper_aval_asn_asn_group_v gaaagv_target,
	grouper_aval_asn_asn_group_v gaaagv_do_provision
where
	gg.id = gaaagv_do_provision.group_id
	and gg.id = gaaagv_target.group_id
	and gaaagv_do_provision.enabled2 = 'T'
	and gaaagv_target.enabled2 = 'T'
	and gaaagv_target.attribute_def_name_name1 = 'etc:provisioning:provisioningMarker'
	and gaaagv_do_provision.attribute_def_name_name1 = 'etc:provisioning:provisioningMarker'
	and gaaagv_target.attribute_def_name_name2 = 'etc:provisioning:provisioningTarget'
	and gaaagv_do_provision.attribute_def_name_name2 = 'etc:provisioning:provisioningDoProvision'
	and gaaagv_target.value_string = 'eduPersonEntitlement'
	and gaaagv_do_provision.value_string = 'true'
```

Simpler query (and no views)

```
select
    gg.id,
    gg.name,
    gg.display_name,
    gg.description,
    gg.id_index
from
    grouper_groups gg,
    grouper_attribute_assign gaa_marker,
    grouper_attribute_assign gaa_target,
    grouper_attribute_assign_value gaav_target,
    grouper_attribute_assign gaa_do_provision,
    grouper_attribute_assign_value gaav_do_provision,
    grouper_attribute_def_name gadn_marker,
    grouper_attribute_def_name gadn_do_provision,
    grouper_attribute_def_name gadn_target
where
    gg.id = gaa_marker.owner_group_id
    and gaa_target.owner_attribute_assign_id = gaa_marker.id
    and gaa_do_provision.owner_attribute_assign_id = gaa_marker.id
    and gaav_target.attribute_assign_id = gaa_target.id
    and gaav_do_provision.attribute_assign_id = gaa_do_provision.id
    and gaa_marker.attribute_def_name_id = gadn_marker.id
    and gaa_target.attribute_def_name_id = gadn_target.id
    and gaa_do_provision.attribute_def_name_id = gadn_do_provision.id
    and gadn_marker.name = 'etc:provisioning:provisioningMarker'
    and gadn_target.name = 'etc:provisioning:provisioningTarget'
    and gadn_do_provision.name = 'etc:provisioning:provisioningDoProvision'
    and gaav_target.value_string = 'eduPersonEntitlement'
    and gaav_do_provision.value_string = 'true'
```

Alternate query (seems simpler but inefficient with Oracle)

```
-- cache these for 5 minutes

-- 4f49d7e7b28040d1b6a1fe55652c6b37
select id from grouper_attribute_def_name where name = 'etc:provisioning:provisioningMarker';

-- 4541cc7de4d14486b7c1d61dd568fd01
select id from grouper_attribute_def_name where name = 'etc:provisioning:provisioningDoProvision';

-- d70495da16f848079f5f76996a556003
select id from grouper_attribute_def_name where name = 'etc:provisioning:provisioningTarget';

select
	gg.id,
	gg.name,
	gg.display_name,
	gg.description,
	gg.id_index
from
	grouper_groups gg,
	grouper_attribute_assign gaa_marker,
	grouper_attribute_assign gaa_target,
	grouper_attribute_assign_value gaav_target,
	grouper_attribute_assign gaa_do_provision,
	grouper_attribute_assign_value gaav_do_provision
where
	gg.id = gaa_marker.owner_group_id
	and gaa_marker.attribute_def_name_id = '4f49d7e7b28040d1b6a1fe55652c6b37'
	and gaa_target.owner_attribute_assign_id = gaa_marker.id
	and gaa_do_provision.owner_attribute_assign_id = gaa_marker.id
	and gaa_target.attribute_def_name_id = 'd70495da16f848079f5f76996a556003'
	and gaa_do_provision.attribute_def_name_id = '4541cc7de4d14486b7c1d61dd568fd01'
	and gaav_target.attribute_assign_id = gaa_target.id
	and gaav_do_provision.attribute_assign_id = gaa_do_provision.id
	and gaav_target.value_string = 'eduPersonEntitlement'
	and gaav_do_provision.value_string = 'true';
```
