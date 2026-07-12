---
title: "Grouper selective loader from attributes example"
space: Grouper
pageId: 28554837
version: 7
lastUpdated: 2026-07-12T05:12:59.326Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554837/Grouper+selective+loader+from+attributes+example
---

The design here is to not load all groups from a loader, but be able to selectively load groups as needed, with certain options, and located anywhere in the registry.

An example of this use case is someone can mark an arbitrary group as "load people from a course", while also selecting if that means students, instructors, or both.

## Sample data

Add the groups, some memberships, and the attributes.

```
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.attr.*;
import edu.internet2.middleware.grouper.attr.assign.*;
import edu.internet2.middleware.grouper.attr.finder.*;

GrouperSession grouperSession = GrouperSession.startRootSession();

AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName("attr:courseDef").assignCreateParentStemsIfNotExist(true).assignCreateParentStemsIfNotExist(true).assignToGroup(true).assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false).assignMultiValued(false).assignValueType(AttributeDefValueType.marker).save();
AttributeDef attributeValueDef = new AttributeDefSave(grouperSession).assignName("attr:courseValueDef").assignCreateParentStemsIfNotExist(true).assignToGroupAssn(true).assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false).assignMultiValued(false).assignValueType(AttributeDefValueType.string).save();

AttributeDef attributeValueMarkerDef = new AttributeDefSave(grouperSession).assignName("attr:courseValueMarkerDef").assignCreateParentStemsIfNotExist(true).assignToGroupAssn(true).assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false).assignMultiValued(false).assignValueType(AttributeDefValueType.marker).save();

attributeDef.getAttributeDefActionDelegate().configureActionList("assign");
attributeValueDef.getAttributeDefActionDelegate().configureActionList("assign");
attributeValueMarkerDef.getAttributeDefActionDelegate().configureActionList("assign");

course = new AttributeDefNameSave(grouperSession, attributeDef).assignName("attr:course").assignCreateParentStemsIfNotExist(true).save();

attributeValueDef.getAttributeDefScopeDelegate().assignOwnerNameEquals("attr:course");
attributeValueMarkerDef.getAttributeDefScopeDelegate().assignOwnerNameEquals("attr:course");

courseId = new AttributeDefNameSave(grouperSession, attributeValueDef).assignName("attr:courseId").assignCreateParentStemsIfNotExist(true).save();
includeStudents = new AttributeDefNameSave(grouperSession, attributeValueMarkerDef).assignName("attr:includeStudents").assignCreateParentStemsIfNotExist(true).save();
includeInstructors = new AttributeDefNameSave(grouperSession, attributeValueMarkerDef).assignName("attr:includeInstructors").assignCreateParentStemsIfNotExist(true).save();

Group math101 = new GroupSave(grouperSession).assignName("org:artsAndSciences:courses:math101").assignCreateParentStemsIfNotExist(true).save();

attributeAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group).assignAttributeDefName(course).assignOwnerGroup(math101);

attributeAssignOnAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group_asgn).assignAttributeDefName(courseId).addValue("math101");

attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave);

attributeAssignOnAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group_asgn).assignAttributeDefName(includeStudents);

attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave);

attributeAssignSave.save();

Group clinical101 = new GroupSave(grouperSession).assignName("org:nursing:classes:clinical101").assignCreateParentStemsIfNotExist(true).save();

attributeAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group).assignAttributeDefName(course).assignOwnerGroup(clinical101);

attributeAssignOnAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group_asgn).assignAttributeDefName(courseId).addValue("clinical101");

attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave);

attributeAssignOnAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group_asgn).assignAttributeDefName(includeStudents);

attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave);

attributeAssignOnAssignSave = new AttributeAssignSave(grouperSession).assignAttributeAssignType(AttributeAssignType.group_asgn).assignAttributeDefName(includeInstructors);

attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave);

attributeAssignSave.save();

```

## Attribute assignments on groups

Note, if you delegate who has access to the course attribute def names, then others can control their own groups

Two groups with attributes:

## Control views

The attribute assignment queries are complicated and have performance implications. So to simplify things, we will make a view, and sync that into a table, so when we do the loader query, it is simpler and performs better.

### View for which groups are courses

Note, this is for mysql but will either work or will work with minor edits in other databases

```
create view course_config_v as
select distinct gaagv.group_id, gaagv.group_name,
(select gaaagv.value_string from grouper_aval_asn_asn_group_v gaaagv where gaaagv.attribute_def_name_name2 = 'attr:courseId' and gaaagv.group_name = gaagv.group_name and gaaagv.enabled2 = 'T' limit 1)
  as course_id,
case when exists 
(select 1 from grouper_attr_asn_asn_group_v gaaagv where gaaagv.attribute_def_name_name2 = 'attr:includeStudents' and gaaagv.group_name = gaagv.group_name and gaaagv.enabled2 = 'T')
then 'T'
else 'F'
end as include_students,
case when exists 
(select 1 from grouper_attr_asn_asn_group_v gaaagv where gaaagv.attribute_def_name_name2 = 'attr:includeInstructors' and gaaagv.group_name = gaagv.group_name and gaaagv.enabled2 = 'T')
then 'T'
else 'F'
end as include_instructors
from  grouper_attr_asn_group_v gaagv
where gaagv.attribute_def_name_name = 'attr:course'
and gaagv.enabled = 'T';

```

## Control table

To simplify things and improve performance, we will sync this view into a table

Convert to table:

```
create table course_config as select * from course_config_v;
ALTER TABLE course_config MODIFY group_id varchar(40);
ALTER TABLE course_config ADD PRIMARY KEY(group_id);
CREATE INDEX course_config_name_idx ON course_config (group_name);
```

Sync the view to table.

grouper.client.properties

```
grouperClient.syncTable.courseConfig.databaseFrom = grouper
grouperClient.syncTable.courseConfig.tableFrom = course_config_v
grouperClient.syncTable.courseConfig.databaseTo = grouper
grouperClient.syncTable.courseConfig.tableTo = course_config
grouperClient.syncTable.courseConfig.columns = *
grouperClient.syncTable.courseConfig.primaryKeyColumns = group_id
```

Lets schedule this hourly at 50 minutes passed the hour

grouper-loader.properties

```
otherJob.courseConfigFull.class = edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob
otherJob.courseConfigFull.quartzCron = 0 50 * * * ?
otherJob.courseConfigFull.grouperClientTableSyncConfigKey = courseConfig
otherJob.courseConfigFull.syncType = fullSyncFull
```

Run the job and make sure it is ok

## Course loader data

Normally this would be a view against the SIS or an ETL to copy that data to a warehouse or something. Here we will just make some sample data

```
CREATE TABLE courses (course_id VARCHAR(20), role VARCHAR(20), subject_id VARCHAR(20));

ALTER TABLE courses ADD PRIMARY KEY(course_id, role, subject_id);

insert into courses (course_id, role, subject_id) values ('english101', 'student', 'test.subject.0');
insert into courses (course_id, role, subject_id) values ('english101', 'instructor', 'test.subject.1');
insert into courses (course_id, role, subject_id) values ('clinical101', 'student', 'test.subject.2');
insert into courses (course_id, role, subject_id) values ('clinical101', 'student', 'test.subject.3');
insert into courses (course_id, role, subject_id) values ('clinical101', 'instructor', 'test.subject.4');
insert into courses (course_id, role, subject_id) values ('math101', 'student', 'test.subject.5');
insert into courses (course_id, role, subject_id) values ('math101', 'student', 'test.subject.6');
insert into courses (course_id, role, subject_id) values ('math101', 'instructor', 'test.subject.7');
commit;
```

## Membership view

This view has the loader results to load the groups via attributes.

```
create view courses_memberships_v as
select courses.subject_id,
       'jdbc' as subject_source_id,
       course_config.group_name
from courses,
     course_config
     where
       course_config.course_id = courses.course_id and course_config.course_id is not null
       and (course_config.include_students = 'T' or course_config.include_instructors = 'T')
       and ((course_config.include_students = 'T' and courses.role = 'student')
        or (course_config.include_instructors = 'T' and courses.role = 'instructor'));
```

## Loader job

## See memberships on any groups with course attributes

## Real time updates

This is not implemented yet but you could easily have a change log consumer that looks for attribute changes for the applicable attributes, and runs a full loader job based on events. Ask the Grouper team for an example if this is a requirement. Otherwise the above will run periodically (this example is hourly)
