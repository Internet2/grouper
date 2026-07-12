---
title: "GSH script to restore a mistakenly deleted attribute definition"
space: Grouper
pageId: 28549473
version: 6
lastUpdated: 2026-07-01T05:41:57.368Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549473/GSH+script+to+restore+a+mistakenly+deleted+attribute+definition
---

Note: [this was done with Claude code and MCP, you can have AI do something similar for your needs](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555704/Grouper+MCP+example+restore+deleted+data)

Note: these instructions show:

1. the etc folder is penn:etc instead of just "etc", you can translate for your env. In fact these instructions are not consistent with this, so pay attention
2. these instructions are for postgres, you can translate to your database
3. these instructions are for pspng but you could extrapolate to something else that needs to be restored

Steps

- Turn off your pspng daemons
- Identify the attribute, in this case "provision_to"

- Look in the point in time tables to see the history of the attribute

```
SELECT name, active, start_time, end_time,
  TO_TIMESTAMP(CAST(start_time AS BIGINT) / 1000000) AS created_date,
  TO_TIMESTAMP(CAST(end_time AS BIGINT) / 1000000) AS deleted_date
FROM grouper_pit_attr_def_name
WHERE name LIKE '%:provision_to%'
```

Mine aren't deleted but you would see them here

- Now lets look at the attribute definition

```
SELECT name, active, start_time, end_time,
  TO_TIMESTAMP(CAST(start_time AS BIGINT) / 1000000) AS created_date,
  TO_TIMESTAMP(CAST(end_time AS BIGINT) / 1000000) AS deleted_date
FROM grouper_pit_attribute_def
WHERE name LIKE '%:provision_to_def%'
```

Mine aren't deleted but you would see them here

- Since we turned off pspng daemons, re-create those attributes (maybe search source code for specifics, but here are some simple instructions)

```
// Recreate provision_to_def attribute definition
AttributeDef provisionToAttributeDef = new AttributeDefSave().assignName("etc:pspng:provision_to_def").assignCreateParentStemsIfNotExist(true).assignToGroup(true).assignToStem(true).assignAttributeDefType(AttributeDefType.type).assignMultiAssignable(true).assignMultiValued(false).assignValueType(AttributeDefValueType.string).save();

// Recreate provision_to attribute def name
AttributeDefName provisionToAttributeDefName = new AttributeDefNameSave(provisionToAttributeDef).assignName("etc:pspng:provision_to").assignCreateParentStemsIfNotExist(true).assignDescription("Defines what provisioners should process a group or groups within a folder").assignDisplayName("etc:pspng:provision_to").save();

```

- Lets look at recent assignments of this name

```
SELECT 
  CASE WHEN paa.owner_group_id IS NOT NULL THEN 'group' ELSE 'stem' END AS type,
  COALESCE(pg.name, ps.name) AS name,
  paav.value_string AS provisioner,
  paa.active,
  paa.start_time,
  TO_TIMESTAMP(CAST(paa.start_time AS BIGINT) / 1000000) AS start_date,
  paa.end_time,
  TO_TIMESTAMP(CAST(paa.end_time AS BIGINT) / 1000000) AS end_date
FROM grouper_pit_attribute_assign paa
  LEFT JOIN grouper_pit_groups pg ON paa.owner_group_id = pg.id
  LEFT JOIN grouper_pit_stems ps ON paa.owner_stem_id = ps.id
  JOIN grouper_pit_attr_assn_value paav ON paav.attribute_assign_id = paa.id
WHERE paa.attribute_def_name_id = (
  SELECT id FROM grouper_pit_attr_def_name
  WHERE name = 'etc:pspng:provision_to'
)
  AND (pg.name IS NOT NULL OR ps.name IS NOT NULL)
ORDER BY paa.end_time DESC NULLS LAST, paa.start_time DESC

```

- Pick a restore timestamp. note, this is the "disabled_time" (scroll right in results), which is the micros (not millis), since 1970. Note the attribute def name assignments are removed before the attribute name and def are deleted. Give some leeway

- Write a query (based on your restore micros time value) which will restore the attribute assignments for groups and folders.

```
SELECT DISTINCT
  CASE 
    WHEN paa.owner_group_id IS NOT NULL THEN
      'new AttributeAssignSave(GrouperSession.staticGrouperSession()).assignAttributeDefNameName("etc:pspng:provision_to").assignOwnerGroupName("' || g.name || '").addAttributeValueString("' || TRIM(paav.value_string) || '").save()'
    ELSE
      'new AttributeAssignSave(GrouperSession.staticGrouperSession()).assignAttributeDefNameName("etc:pspng:provision_to").assignOwnerStemName("' || s.name || '").addAttributeValueString("' || TRIM(paav.value_string) || '").save()'
  END AS gsh
FROM grouper_pit_attribute_assign paa
  LEFT JOIN grouper_pit_groups pg ON paa.owner_group_id = pg.id
  LEFT JOIN grouper_pit_stems ps ON paa.owner_stem_id = ps.id
  LEFT JOIN grouper_groups g ON pg.source_id = g.id
  LEFT JOIN grouper_stems s ON ps.source_id = s.id
  JOIN grouper_pit_attr_assn_value paav ON paav.attribute_assign_id = paa.id
WHERE paa.attribute_def_name_id = (
  SELECT id FROM grouper_pit_attr_def_name
  WHERE name = 'etc:pspng:provision_to'
)
  AND paa.end_time >= 1650299711928000
  AND paa.active = 'F'
  AND (g.name IS NOT NULL OR s.name IS NOT NULL)
ORDER BY gsh

```

- Copy all that GSH and run the script and you should be back up and running
