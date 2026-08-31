---
title: "Example Zabbix monitoring"
space: Grouper
pageId: 28555675
version: 7
lastUpdated: 2026-07-01T05:37:39.778Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555675/Example+Zabbix+monitoring
---

## Example Zabbix rules

Monitoring is performed using the Zabbix HTTP agent item to ingest Grouper status pages and parse them for relevant data using Dependent items and preprocessing. Some items have triggers applied that will generate alarms and possibly incidents in your event management system (e.g. ServiceNow). A graphical view of the monitoring data along with any current problems can be exported to a data visualization platform (e.g. Grafana). This  template can be used as a starting point.

| Monitoring item | Status page URL | Check | CheckType | Trigger |
| --- | --- | --- | --- | --- |
| Raw Item pull for Grouper UI Up/Down, Memory Test | https://your.grouper.fqdn/status_grouper/status?diagnosticType=trivial | Pulls status page as a raw item for consumption | HTTP Agent |  |
| Grouper UI Up/Down |  | Checks for a "SUCCESS" at the top of the page | Dependent | Trigger: Warning(P4) - Page Error (The Success string is not found)  Trigger: Warning (P4) - No Data Received on Grouper Trivial Status for 20 minutes. (This likely means the page is not reachable from Zabbix) |
| Grouper UI Memory Test |  | Checks for “SUCCESS” on the line where memoryTest occurs  Can optionally track elapsed time or allocated bytes | Dependent | Trigger: Warning(P4) - Memory Error (The Success string is not found)  Trigger: Warning(P4) - Memory Perfomance degradation (elapsed time goes above x ms over y minutes) |
|  |  |  |  |  |
| Raw Item pull for Grouper UI Sources | https://your.grouper.fqdn/status_grouper/status?diagnosticType=sources | Pulls sources page as a raw item for consumption | HTTP Agent |  |
| Grouper UI Sources |  | Checks for a SUCCESS on the line where the source name occurs (e.g. source_uncg-person)  Sources Status Available - Numerical representation of the instances of “SUCCESS” allows for graphing availability with services such as Grafana | Dependent | Same as Up/Down |
|  |  |  |  |  |
| Raw Item pull for Grouper Database Connection Status | https://your.grouper.fqdn/status_grouper/status?diagnosticType=db | Pulls db page as a raw item for consumption | HTTP Agent |  |
| Grouper Database Connection Status |  | DB Status - checks for a "SUCCESS" on the line where dbTest_grouper occcurs  Numerical representation of the instances of “SUCCESS” allows for graphing availability. | Dependent | Trigger: Warning (P4) - No Data Received on Grouper DB Status(i.e. page not reachable from Zabbix) for 20 mins  Trigger: Warning (P4) - DB page Error (The Success string is not found) |
|  |  | DB Test - Checks for retrieved object from cache and time elapsed | Dependent | DB Test - Checks for retrieved object from cache and time elapsed |
|  |  |  |  |  |
| Raw Item pull for Grouper Database Connection Status | https://your.uncg.fqdn/status_grouper/status?diagnosticType=all&includeOnly={$JOB#} | Pull status page for loader jobs as a raw item for consumption | HTTP Agent |  |
| Loader jobs |  | The following filters are applied for each of the jobs as an item to be monitored  loader_CHANGE_LOG_consumer_example_incremental  loader_OTHER_JOB_example_full  The following items and are monitored on the page along with any triggers applied (The {$JOB#} macro is populated by the specific loader job to parse for:  Loader Job - {$JOB1}  Loader Job - {$JOB2}  Loader Job - {$JOB3}  Loader Job - {$JOB4}  Loader Job - {$JOB5}  Loader Job - {$JOB6} | Dependent | Trigger: Warning (P4) No Data Received on {$JOB#} for 10 minutes (This likely means the page is not reachable from Zabbix)  Trigger: Warning (P4) - Job Error – {JOB#} (The Success string is not found) |
