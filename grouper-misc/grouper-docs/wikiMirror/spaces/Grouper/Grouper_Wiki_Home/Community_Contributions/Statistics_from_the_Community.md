---
title: "Statistics from the Community"
space: Grouper
pageId: 28543154
version: 33
lastUpdated: 2026-07-01T05:50:17.238Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543154/Statistics+from+the+Community
---

## 

### Statistics snapshot - data taken from the Grouper Daily Report for Production deployments

Collection of interesting statistics from sites running Grouper. If you enter a line into the table please complete all cells in the table.  
Follow the format as noted in the first line in the table. **Do not pollute cells with commentary - use Comments column**

- For the Grouper Server Count - please only include Grouper servers, not DB servers or messaging or other components.

| Date | Institutional   Domain Name | Memberships | Members   Total | Groups | Folders | Loader    Jobs | Loader    Memberships | Grouper   Server   Count | Database   Used | Grouper   Version | First   Deploy   Date | Comments |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 2017-01-16 | psu.edu | 7,893,710 | 406,719 | 79,215 | 30,145 | 101,932 | 13,950,454 | 3 | Oracle | 2.3.0 + patches | 2016-06 | Not using PSP/PSPNG - using modified GAP from CMU |
| 2017-01-16 | brown.edu | 21,413,019 | 2,173,899 | 1,111,307 | 239,374 | 5,976 | n/a | 3 | Oracle | 2.3.0 + patches | 2007-08 | we do not use PSP or PSPNG for provisioning; one server is Grouper, two are Oracle databases for HA |
| 2017-01-16 | duke.edu | 19,974,955 | 1,796,783 | 884,517 | 578,717 | 13,778 | n/a | 4 | Oracle | 2.1.5 | 2005-12 | We do not use PSP or PSPNG for provisioning. We also do not use the loader. We have our own processes for both. Three servers for UI/WS, one for daemon. |
| 2017-01-17 | ncl.ac.uk | 961,908 | 74,185 | 14,874 | 918 | 9,740 | 261,783 | 1 | MySQL | 2.2.2 | 2007 | Currently using PSP but planning to upgrade to 2.3 and move to PSPNG due to performance issues. |
| 2019-07-31 | wisc.edu | 49,660,850 | 1,143,912 | 110,380 | 26,292 | 234,491 | 17,870,909 | 2 | Oracle Subject Source, Oracle Grouper DB | 2.3.0 | 2012 | (Daemon only runs on 1 server currently of the two. Plans to change that in upgrade to 2.3) We do not use PSP or PSPNG for provisioning. Our "raw" memberships are loaded in by an external process. |
| 2017-01-17 | rice.edu | 815,526 | 274,825 | 8,194 | 4,835 | 5,814 | 95,472 | 3 | PostgreSQL  (moving to Oracle) | 2.2.2 | 2008-04 | Using PSP; working on moving to 2.3.0 once I have a PSPNG configuration ready to go. Three servers are 1 each for db, daemon, & ui/ws access. |
| 2017-01-17 | oregonstate.edu | 1,356,821 | 106,559 | 2,844 | 89 | 19,785 | 3,526,348 | 1 | Oracle | 2.3.0 + patches | 2015 | Using PSP |
| 2017-01-17 | upenn.edu | 9,308,078 | 921,223 | 235,495 | 23,371 | 19,741 | 2,466,203 | 5 | Oracle | 2.3.0 + patches | 2008 | Use PSPNG a little |
| 2017-01-18 | fu-berlin.de | 2,614,814 | 88,185 | 13,927 | 4,900 | 3,458,383 | 38,000,859 | 5 | PostgreSQL | 2.2.2 | 2013 | Using PSP; GrouperDB on 1 server with daemon; plus 2 for LDAP, 1 for WebApp, 1 for source DB. |
| 2017-01-18 | uchicago.edu | 13,012,040 | 400,050 | 190,359 | 102,621 | 95,491 | 60,735,333 | 4 | Oracle | 2.2.2 | 2007 | Using custom provisioners, would like to move to PSPNG at some point. |
| 2017-01-18 | hawaii.edu | 14,243,680 | 829,827 | 446,585 | 313,121 | n/a | n/a | 2 | MySQL | 2.2.2 | 2011 | We do not use PSP or PSPNG for provisioning. We rely on enterprise messages via RabbitMQ to update our groups live (or close to live) |
| 2017-01-19 | columbia.edu | 1,835,612 | 198,584 | 43,167 | 5,396 | 31,266 | 511,603 | 3 | Oracle | 2.3.0 | 2016-11 | Using PSP |
| 2017-01-19 | lafayette.edu | 342,142 | 39,441 | 705 | 159 | 85,909 | 39,260 | 2 | MySQL | 2.2.2 | 2015-03 | Using locally developed provisioning systems. Only deployed UI and daemons-- no web services. |
| 2017-01-19 | washington.edu | 4,503,000 | 1,262,000 | 107,000 | 44,000 | n/a | n/a | 3 | PostgreSQL | 2.1.2 | 2010-04 | Don't use loader or psp(ng) |
|  |  |  |  |  |  |  |  |  |  |  |  |  |
|  |  |  |  |  |  |  |  |  |  |  |  |  |
