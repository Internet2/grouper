---
title: "Countdown groups for password reset requirement"
space: Grouper
pageId: 28544654
version: 5
lastUpdated: 2026-07-01T05:48:16.769Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544654/Countdown+groups+for+password+reset+requirement
---

We need a group that holds people who have not changed their password after Feb 2, 2021.

Those people should flow through count down group so our authn can give them a countdown warning to go and set their pass or they will be locked.

Setup the group is a simple LDAP loader. The match to get to the integer in AD that represents the date is the only trick

Now we need some queries to make countdown groups loader job. We want from 30 → 0 where 0 is March 14th, 2021

Group query: (probably a better way to do this, but this is quick and dirty)

```
create view authz_o365_countdown_names_v as
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_0' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_1' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_2' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_3' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_4' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_5' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_6' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_7' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_8' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_9' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_10' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_11' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_12' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_13' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_14' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_15' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_16' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_17' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_18' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_19' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_20' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_21' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_22' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_23' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_24' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_25' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_26' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_27' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_28' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_29' as group_name)
union all
(select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_30' as group_name)

```

Make the loader query

```
create view authz_o365_countdown_member_v as
select 'penn:isc:ait:apps:O365:o365oneAdPasswordLastChangedCountdown:oneAdCountdown_' || greatest(to_date('2021/03/14', 'yyyy/mm/dd') - current_date, 0) as group_name, 
'penn:isc:ait:apps:O365:o365oneAdPasswordLastChanged' as subject_identifier, 'g:gsa' as subject_source_id
where to_date('2021/03/14', 'yyyy/mm/dd') - current_date <= 30
```
