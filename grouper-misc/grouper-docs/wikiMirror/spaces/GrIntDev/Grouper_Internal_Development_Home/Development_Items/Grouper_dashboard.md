---
title: "Grouper dashboard"
space: GrIntDev
pageId: 48792768
version: 3
lastUpdated: 2026-07-12T07:01:15.498Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792768/Grouper+dashboard
---

Grouper could have a dashboard that shows the current state and history of Grouper. This is an item for the future

Example is number of groups over time to show the adoption of your instance

```
select the_timestamp,
(select count(1) from grouper_pit_groups gpg
where gpg.start_time < (extract(epoch from the_timestamp) * 1000000)
and gpg.name not like 'penn:community:student:course:%'
and gpg.end_time  is null or gpg.end_time > (extract(epoch from the_timestamp) * 1000000)) as group_count
from (
select current_timestamp as the_timestamp 
union select TO_TIMESTAMP('2020-01-01','YYYY-MM-DD')
union select TO_TIMESTAMP('2019-01-01','YYYY-MM-DD')
union select TO_TIMESTAMP('2018-01-01','YYYY-MM-DD')
union select TO_TIMESTAMP('2017-01-01','YYYY-MM-DD')
union select TO_TIMESTAMP('2016-01-01','YYYY-MM-DD')
union select TO_TIMESTAMP('2015-01-01','YYYY-MM-DD')
union select TO_TIMESTAMP('2014-01-01','YYYY-MM-DD')
union select TO_TIMESTAMP('2013-01-01','YYYY-MM-DD')
union select TO_TIMESTAMP('2012-01-01','YYYY-MM-DD')
union select TO_TIMESTAMP('2011-01-01','YYYY-MM-DD')
) as timestamps order by 1
```

| Timestamp | Group count |
| --- | --- |
| the_timestamp | group_count |
| 1/1/2011 0:00 | 17228 |
| 1/1/2012 0:00 | 19923 |
| 1/1/2013 0:00 | 20652 |
| 1/1/2014 0:00 | 18105 |
| 1/1/2015 0:00 | 18359 |
| 1/1/2016 0:00 | 32055 |
| 1/1/2017 0:00 | 32629 |
| 1/1/2018 0:00 | 46546 |
| 1/1/2019 0:00 | 111503 |
| 1/1/2020 0:00 | 166846 |
| 10/2/2020 17:46 | 214476 |
