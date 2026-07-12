---
title: "Banner Grouper integrations at Penn"
space: Grouper
pageId: 28544752
version: 2
lastUpdated: 2026-07-01T05:48:05.193Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544752/Banner+Grouper+integrations+at+Penn
---

There are several Grouper/Banner integrations at Penn

## Loading Banner security groups

We load Banner security groups for reporting and to re-use those populations in other applications (as reference groups).

Access to the database is done over DB link, though it could use a Sql table sync.

Ask your Banner team for a query, here is ours:

```
SELECT    SPRIDEN_ID,
          GURUGRP_SGRP_CODE,
          GTVSGRP_DESC
     FROM BANSECR.GURUGRP,
          BANSECR.GTVSGRP,
          GOBEACC,
          SPRIDEN
    WHERE     GURUGRP_USER = GOBEACC_USERNAME
          AND GURUGRP_SGRP_CODE = GTVSGRP_CODE
          AND GOBEACC_PIDM = SPRIDEN_PIDM(+)
          AND SPRIDEN_CHANGE_IND IS NULL
          AND GURUGRP_SGRP_CODE LIKE 'UP%';
```

We load those up here
