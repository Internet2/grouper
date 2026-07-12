---
title: "Grouper daily email notification"
space: Grouper
pageId: 28549403
version: 16
lastUpdated: 2026-07-01T05:42:05.454Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549403/Grouper+daily+email+notification
---

This feature is in v2.5.40+

The original use case is if training will expire or an affiliation will expire and the user should followup.

## Configure

Grouper wheel/sysadmin can configure an email job

Make sure SMTP is setup. You also need emailAttributeName setup in your subject source so Grouper knows the email address of users

Add daemon

Configure

| Configuration | Values | Description |
| --- | --- | --- |
| Email type | notification | Each row or group member is a person and will get an email |
| summary | Each row or group member will be available for a summary email to someone else |
| Population type | groupMembership | The population is a member of a group. e.g. a loaded group or a composite where the membership means something (user should get email or appear in summary report) |
| sqlQuery | The population is a row from a query. Columns of the query can be used in the email or in the summary report |
| DB connection id | warehouse | If this is a sql query this is the database connection to use |
| Sql query for notification | select subject_id, expire_date from training_expire_v | sql query where each row represents a subject to send an email to, or a row in the email summary. There must be a column of subject_id. There can optionally be a column email_address_to_send_to if you want to override the subject email address. Any other columns will be available for the email body and subject template. |
| Subject source ID | pennperson | If all records should be from a certain source |
| Group members for notification | a:b:c | If records are from a group enter the group name |
| Template of email subject | Notice: FERPA training for ${subject_name} is about to expire | subject of the email. Note, you can use any variables that the body uses. This is a jexl template |
|  |  | body of the email. You can use any variables from the columns of query or the subject    \| Variable \| Description \| \| --- \| --- \| \| subject_name \| subject name \| \| subject_id \| subject id \| \| subject_description \| subject description \| \| subject_attribute_firstname \| where "firstname" is a lower case subject attribute key \| \| column_some_column_name \| "some_column_name" is a lower case column name from query (if applicable). \| \| listOfRecordMaps \| list of maps with variables for each row in a summary email, in each record map is all the var above (see example below) \|  __NEWLINE__ will substitute to a newline. If a UI form does not have a textarea editor for the jexl, you can replace \n for __NEWLINE__ when you edit these templates  Uses a [JEXL template](https://commons.apache.org/proper/commons-jexl/javadocs/apidocs-2.1/org/apache/commons/jexl2/UnifiedJEXL.Template.html). The JEXL template code part starts with two dollar signs: $$ (see example below)  Notification template without newline substitution   ``` Hello ${subject_name} Your training will expire on ${column_expire_date} Please take your training now: https://mylms.edu/myTraining Thanks! ```    Notification template example:   ``` Hello ${subject_name},__NEWLINE__Your training will expire on ${column_expire_date}__NEWLINE__Please take your training now: https://mylms.edu/myTraining__NEWLINE__Thanks! ```  Notification email looks like     ``` Hello John Smith, Your training will expire on 2021/03/12 Please take your training now: https://mylms.edu/myTraining Thanks! ```    Summary template example without newline subsitution:     ``` Hello ${subject_name}, There are ${size(listOfRecordMaps)} people whose training is about to expire $$ for (var recordMap : listOfRecordMaps) { Training for ${recordMap.get('subject_id')} ${recordMap.get('subject_name')} expires on ${recordMap.get('column_expire_date')} $$} Thanks! ```    Summary template example:  If this is a summary report, then you can loop over the records to print a line per person.   ``` Hello ${subject_name},__NEWLINE__There are ${size(listOfRecordMaps)} people whose training is about to expire__NEWLINE__$$ for (var recordMap : listOfRecordMaps) {__NEWLINE__Training for ${recordMap.get('subject_id')} ${recordMap.get('subject_name')} expires on ${recordMap.get('column_expire_date')}__NEWLINE__$$}__NEWLINE__Thanks! ```  Summary email looks like:   ``` Hello John Smith, There are 2 people whose training is about to expire Training for 12345678 Janet Wilson expires on 2021/04/03 Training for 87654321 Bob Jones expires on 2021/03/01 Thanks! ``` | Variable | Description | subject_name | subject name | subject_id | subject id | subject_description | subject description | subject_attribute_firstname | where "firstname" is a lower case subject attribute key | column_some_column_name | "some_column_name" is a lower case column name from query (if applicable). | listOfRecordMaps | list of maps with variables for each row in a summary email, in each record map is all the var above (see example below) |
| Variable | Description |
| subject_name | subject name |
| subject_id | subject id |
| subject_description | subject description |
| subject_attribute_firstname | where "firstname" is a lower case subject attribute key |
| column_some_column_name | "some_column_name" is a lower case column name from query (if applicable). |
| listOfRecordMaps | list of maps with variables for each row in a summary email, in each record map is all the var above (see example below) |
| Group name of people who received emails | a:b:c | If you want max 1 email per day and see who gets the email, make a group and put name here. |
| Group name of eligible members | d:e:f | If this is a group list, then this isnt really needed. If this is a SQL list, you can intersect with members in a group. If they are in this group, then they are eligible to be in the population |
| BCC | a@b.c, d@e.f | BCC to these email address for testing |
| send to BCC only | true | If you are testing and dont want real emails going out, just send to bcc only |

## See when emails were last sent

## Sample queries

Returns people from a group 23 days out of group and 27 days out of group. Could give an email notification to users if 30 day grace period, 7 days before out of group, and again at 3 days out of group

```
SELECT distinct gpmglw.GROUP_NAME, gpmglw.SUBJECT_ID FROM grouper_pit_mship_group_lw_v gpmglw, grouper_time gt
where gpmglw.GROUP_NAME = 'test:testGroup' and gpmglw.FIELD_NAME = 'members' and gpmglw.SUBJECT_SOURCE = 'pennperson' and gpmglw.the_end_time is not null
and ((select max(gpmglw2.THE_END_TIME) / 1000000 from grouper_pit_mship_group_lw_v gpmglw2 
     where gpmglw2.FIELD_ID = gpmglw.FIELD_ID and gpmglw2.group_id = gpmglw.group_id and gpmglw2.member_id = gpmglw.member_id ) 
     between (gt.utc_millis_since_1970/1000 - (23*24*60*60)) and (gt.utc_millis_since_1970/1000 - (24*24*60*60))
     or
     (select max(gpmglw2.THE_END_TIME) / 1000000 from grouper_pit_mship_group_lw_v gpmglw2 
     where gpmglw2.FIELD_ID = gpmglw.FIELD_ID and gpmglw2.group_id = gpmglw.group_id and gpmglw2.member_id = gpmglw.member_id ) 
     between (gt.utc_millis_since_1970/1000 - (27*24*60*60)) and (gt.utc_millis_since_1970/1000 - (28*24*60*60)))
and not exists (select 1 from grouper_pit_mship_group_lw_v gpmglw2 
     where gpmglw2.FIELD_ID = gpmglw.FIELD_ID and gpmglw2.group_id = gpmglw.group_id and gpmglw2.member_id = gpmglw.member_id and gpmglw2.THE_ACTIVE = 'T' );
```
