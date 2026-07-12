---
title: "Grouper loader from attributes example"
space: Grouper
pageId: 28554633
version: 24
lastUpdated: 2026-07-12T05:15:42.301Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554633/Grouper+loader+from+attributes+example
---

The design here is to intersect multiple groups of unions that are easy to setup and don't pollute the namespace with tons of intermediate groups.

An example of this use case is someone can mark an arbitrary group as "people in any of these 4 jobs", while also "in any of these 2 departments", while also "with any of these 5 titles". That would normally be a bunch of helper groups composited together. This allows you to do it in one group. Granted, you lose visualization and maybe other things. Always pros and cons.

## Sample data

Add the groups, some memberships, and the attributes.

Note: the basis groups would normally be loaded, but here they are just GSH'ed for an example

```
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.attr.*;
import edu.internet2.middleware.grouper.attr.assign.*;
import edu.internet2.middleware.grouper.attr.finder.*;

GrouperSession grouperSession = GrouperSession.startRootSession();
new GroupSave(grouperSession).assignName("basis:employee:orgUnitCodes:orgUnitCode_180101").assignCreateParentStemsIfNotExist(true).save();
new GroupSave(grouperSession).assignName("basis:employee:orgUnitCodes:orgUnitCode_123456").save();
new GroupSave(grouperSession).assignName("basis:employee:orgUnitCodes:orgUnitCode_234567").save();
new GroupSave(grouperSession).assignName("basis:employee:orgUnitCodes:orgUnitCode_345678").save();
addMember("basis:employee:orgUnitCodes:orgUnitCode_180101", "test.subject.0");
addMember("basis:employee:orgUnitCodes:orgUnitCode_123456", "test.subject.0");
addMember("basis:employee:orgUnitCodes:orgUnitCode_234567", "test.subject.0");
addMember("basis:employee:orgUnitCodes:orgUnitCode_180101", "test.subject.1");
addMember("basis:employee:orgUnitCodes:orgUnitCode_123456", "test.subject.1");
addMember("basis:employee:orgUnitCodes:orgUnitCode_234567", "test.subject.1");
addMember("basis:employee:orgUnitCodes:orgUnitCode_345678", "test.subject.1");
addMember("basis:employee:orgUnitCodes:orgUnitCode_180101", "test.subject.2");
new GroupSave(grouperSession).assignName("basis:employee:jobClasses:jobClass_EFX").assignCreateParentStemsIfNotExist(true).save();
new GroupSave(grouperSession).assignName("basis:employee:jobClasses:jobClass_ABC").save();
new GroupSave(grouperSession).assignName("basis:employee:jobClasses:jobClass_DEF").save();
new GroupSave(grouperSession).assignName("basis:employee:jobClasses:jobClass_EFG").save();
addMember("basis:employee:jobClasses:jobClass_EFX", "test.subject.0");
addMember("basis:employee:jobClasses:jobClass_ABC", "test.subject.0");
addMember("basis:employee:jobClasses:jobClass_DEF", "test.subject.0");
addMember("basis:employee:jobClasses:jobClass_EFX", "test.subject.1");
addMember("basis:employee:jobClasses:jobClass_ABC", "test.subject.1");
addMember("basis:employee:jobClasses:jobClass_DEF", "test.subject.1");
addMember("basis:employee:jobClasses:jobClass_EFG", "test.subject.1");
addMember("basis:employee:jobClasses:jobClass_ABC", "test.subject.3");
new GroupSave(grouperSession).assignName("basis:employee:jobCodes:jobCode_C902IT").assignCreateParentStemsIfNotExist(true).save();
new GroupSave(grouperSession).assignName("basis:employee:jobCodes:jobCode_B32931").save();
new GroupSave(grouperSession).assignName("basis:employee:jobCodes:jobCode_A46192").save();
addMember("basis:employee:jobCodes:jobCode_C902IT", "test.subject.0");
addMember("basis:employee:jobCodes:jobCode_B32931", "test.subject.0");
addMember("basis:employee:jobCodes:jobCode_C902IT", "test.subject.1");
addMember("basis:employee:jobCodes:jobCode_B32931", "test.subject.1");
addMember("basis:employee:jobCodes:jobCode_A46192", "test.subject.1");
addMember("basis:employee:jobCodes:jobCode_C902IT", "test.subject.2");
addMember("basis:employee:jobCodes:jobCode_C902IT", "test.subject.3");

AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName("attr:employeeIntersectDef").assignCreateParentStemsIfNotExist(true).assignCreateParentStemsIfNotExist(true).assignToGroup(true).assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false).assignMultiValued(false).assignValueType(AttributeDefValueType.marker).save();
AttributeDef attributeValueDef = new AttributeDefSave(grouperSession).assignName("attr:employeeIntersectValueDef").assignCreateParentStemsIfNotExist(true).assignToGroupAssn(true).assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false).assignMultiValued(true).assignValueType(AttributeDefValueType.string).save();

attributeDef.getAttributeDefActionDelegate().configureActionList("assign");
attributeValueDef.getAttributeDefActionDelegate().configureActionList("assign");

employeeIntersect = new AttributeDefNameSave(grouperSession, attributeDef).assignName("attr:employeeIntersect").assignCreateParentStemsIfNotExist(true).assignDisplayName("attr:employeeIntersect").save();

attributeValueDef.getAttributeDefScopeDelegate().assignOwnerNameEquals("attr:employeeIntersect");

jobClass = new AttributeDefNameSave(grouperSession, attributeValueDef).assignName("attr:jobClass").assignCreateParentStemsIfNotExist(true).assignDisplayName("attr:jobClass").save();
jobCode = new AttributeDefNameSave(grouperSession, attributeValueDef).assignName("attr:jobCode").assignCreateParentStemsIfNotExist(true).assignDisplayName("attr:jobCode").save();
orgUnitCode = new AttributeDefNameSave(grouperSession, attributeValueDef).assignName("attr:orgUnitCode").assignCreateParentStemsIfNotExist(true).assignDisplayName("attr:orgUnitCode").save();

Group someList1 = new GroupSave(grouperSession).assignName("app:emailLists:someList1").assignCreateParentStemsIfNotExist(true).save();

attributeAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group).assignAttributeDefName(employeeIntersect).assignOwnerGroup(someList1);

attributeAssignOnAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group_asgn).assignAttributeDefName(jobClass).addValue("EFX").addValue("ABC").addValue("DEF");

attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave);

attributeAssignOnAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group_asgn).assignAttributeDefName(jobCode).addValue("C902IT").addValue("B32931");

attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave);

attributeAssignOnAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group_asgn).assignAttributeDefName(orgUnitCode).addValue("123456").addValue("180101");

attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave);

attributeAssignSave.save();

Group someList2= new GroupSave(grouperSession).assignName("app:emailLists:someList2").assignCreateParentStemsIfNotExist(true).save();

attributeAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group).assignAttributeDefName(employeeIntersect).assignOwnerGroup(someList2);

attributeAssignOnAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group_asgn).assignAttributeDefName(jobClass).addValue("EFX").addValue("ABC").addValue("EFG");

attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave);

attributeAssignOnAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group_asgn).assignAttributeDefName(jobCode).addValue("C902IT").addValue("A46192");

attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave);

attributeAssignSave.save();

```

## Attribute assignments on groups

Note, if you delegate who has access to the employee intersect attribute def names, then others can control their own groups

Two groups with attributes:

## Control views

The attribute assignment queries are complicated and have performance implications. So to simplify things, we will make two views, and sync those into tables, so when we do the loader query, it is simpler and performs better.

### View for which groups are employeeIntersects

Note, these are for mysql but will either work or will work with minor edits in other databases

```
create view employee_intersect_owners_v as
select distinct gaagv.group_id, gaagv.group_name,
case when exists 
(select 1 from grouper_aval_asn_asn_group_v gaaagv where gaaagv.attribute_def_name_name2 = 'attr:jobClass' and gaaagv.group_name = gaagv.group_name and gaaagv.enabled2 = 'T')
then 'T'
else 'F'
end as has_job_class,
case when exists 
(select 1 from grouper_aval_asn_asn_group_v gaaagv where gaaagv.attribute_def_name_name2 = 'attr:jobCode' and gaaagv.group_name = gaagv.group_name and gaaagv.enabled2 = 'T')
then 'T'
else 'F'
end as has_job_code,
case when exists 
(select 1 from grouper_aval_asn_asn_group_v gaaagv where gaaagv.attribute_def_name_name2 = 'attr:orgUnitCode' and gaaagv.group_name = gaagv.group_name and gaaagv.enabled2 = 'T')
then 'T'
else 'F'
end as has_org_unit_code
from  grouper_attr_asn_group_v gaagv
where gaagv.attribute_def_name_name = 'attr:employeeIntersect'
and gaagv.enabled = 'T';

```

### View for which groups are unioned before intersected.

```
create view employee_intersect_unions_v as
select distinct gaaagv_union.group_id as group_id_owner,
gaaagv_union.group_name as group_name_owner,
case
  when gaaagv_union.attribute_def_name_name2 = 'attr:jobClass' then 'jobClass'
  when gaaagv_union.attribute_def_name_name2 = 'attr:jobCode' then 'jobCode'
  when gaaagv_union.attribute_def_name_name2 = 'attr:orgUnitCode' then 'orgUnitCode'
end as union_attribute,
case
  when gaaagv_union.attribute_def_name_name2 = 'attr:jobClass' then concat('basis:employee:jobClasses:jobClass_', upper(trim(gaaagv_union.value_string)))
  when gaaagv_union.attribute_def_name_name2 = 'attr:jobCode' then concat('basis:employee:jobCodes:jobCode_', upper(trim(gaaagv_union.value_string)))
  when gaaagv_union.attribute_def_name_name2 = 'attr:orgUnitCode' then concat('basis:employee:orgUnitCodes:orgUnitCode_', upper(trim(gaaagv_union.value_string)))
end as group_name_union
from  grouper_aval_asn_asn_group_v gaaagv_union
where gaaagv_union.attribute_def_name_name1 = 'attr:employeeIntersect'
  and gaaagv_union.attribute_def_name_name2 in ('attr:jobClass', 'attr:jobCode', 'attr:orgUnitCode')
  and gaaagv_union.enabled2 = 'T'
order by 2, 3, 4
;
```

## Control tables

To simplify things and improve performance, we will sync those two views to two tables

Convert to tables:

```
create table employee_intersect_owners as select * from employee_intersect_owners_v;
ALTER TABLE employee_intersect_owners ADD PRIMARY KEY(group_id);
CREATE INDEX employee_int_owners_name_idx ON employee_intersect_owners (group_name);

create table employee_intersect_unions as select * from employee_intersect_unions_v;
ALTER TABLE employee_intersect_unions MODIFY group_name_union varchar(1024);
ALTER TABLE employee_intersect_unions ADD PRIMARY KEY(group_id_owner, group_name_union(300));
CREATE INDEX employee_int_unions_name_idx ON employee_intersect_unions (group_name_owner);
```

Sync the views to tables.

grouper.client.properties

```
grouperClient.syncTable.employeeIntersectOwners.databaseFrom = grouper
grouperClient.syncTable.employeeIntersectOwners.tableFrom = employee_intersect_owners_v
grouperClient.syncTable.employeeIntersectOwners.databaseTo = grouper
grouperClient.syncTable.employeeIntersectOwners.tableTo = employee_intersect_owners
grouperClient.syncTable.employeeIntersectOwners.columns = *
grouperClient.syncTable.employeeIntersectOwners.primaryKeyColumns = group_id

grouperClient.syncTable.employeeIntersectUnions.databaseFrom = grouper
grouperClient.syncTable.employeeIntersectUnions.tableFrom = employee_intersect_unions_v
grouperClient.syncTable.employeeIntersectUnions.databaseTo = grouper
grouperClient.syncTable.employeeIntersectUnions.tableTo = employee_intersect_unions
grouperClient.syncTable.employeeIntersectUnions.columns = *
grouperClient.syncTable.employeeIntersectUnions.primaryKeyColumns = group_id_owner, group_name_union
```

Lets schedule this hourly at 50 minutes passed the hour

grouper-loader.properties

```
otherJob.employeeIntersectOwnersFull.class = edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob
otherJob.employeeIntersectOwnersFull.quartzCron = 0 50 * * * ?
otherJob.employeeIntersectOwnersFull.grouperClientTableSyncConfigKey = employeeIntersectOwners
otherJob.employeeIntersectOwnersFull.syncType = fullSyncFull

otherJob.employeeIntersectUnionsFull.class = edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob
otherJob.employeeIntersectUnionsFull.quartzCron = 0 51 * * * ?
otherJob.employeeIntersectUnionsFull.grouperClientTableSyncConfigKey = employeeIntersectUnions
otherJob.employeeIntersectUnionsFull.syncType = fullSyncFull
```

Run the jobs and make sure they are ok

## Membership view

This view has the loader results to load the groups with attributes. Note, since all the groups used are loaded basis groups (not in this example, but yes in practice) with immediate memberships, we can join to the grouper_memberships table. This is done for performance reasons. If there were effective members, then the grouper_memberships_lw_v would need to be used, and the grouper_groups/grouper_fields table wouldnt be needed since that is in the view.

```
create view employee_intersect_mships_v as
select eio.group_name as group_name,
       gm.subject_id as subject_id,
       gm.subject_source as subject_source_id
from employee_intersect_owners eio,
     grouper_members gm
     where
       (eio.has_job_class = 'T' or eio.has_job_code = 'T' or eio.has_org_unit_code = 'T')
       and  (eio.has_job_class = 'F' 
            or exists (select 1 from grouper_memberships gms, employee_intersect_unions eiu, grouper_groups gg, grouper_fields gf
                       where eiu.group_id_owner = eio.group_id and gf.name = 'members' and gf.id = gms.field_id
                         and gm.id = gms.member_id and gms.enabled = 'T'
                         and eiu.union_attribute = 'jobClass' and gg.name = eiu.group_name_union and gms.owner_id = gg.id ))
        and (eio.has_job_code = 'F' 
            or exists (select 1 from grouper_memberships gms, employee_intersect_unions eiu, grouper_groups gg, grouper_fields gf
                       where eiu.group_id_owner = eio.group_id  and gf.name = 'members' and gf.id = gms.field_id
                         and gm.id = gms.member_id and gms.enabled = 'T'
                         and eiu.union_attribute = 'jobCode' and gg.name = eiu.group_name_union and gms.owner_id = gg.id ))
       and (eio.has_org_unit_code = 'F' 
            or exists (select 1 from grouper_memberships gms, employee_intersect_unions eiu, grouper_groups gg, grouper_fields gf
                       where eiu.group_id_owner = eio.group_id  and gf.name = 'members' and gf.id = gms.field_id
                         and gm.id = gms.member_id and gms.enabled = 'T'
                         and eiu.union_attribute = 'orgUnitCode' and gg.name = eiu.group_name_union and gms.owner_id = gg.id ))
```

## Loader job

## See memberships on any groups with employee intersect attributes

## Real time updates

This is not implemented yet but you could easily have a change log consumer that looks for membership changes to any of the union groups, and looks for attribute changes for the applicable attributes, and runs a full or incremental loader job based on events. Ask the Grouper team for an example if this is a requirement. Otherwise the above will run periodically (this example is hourly)
