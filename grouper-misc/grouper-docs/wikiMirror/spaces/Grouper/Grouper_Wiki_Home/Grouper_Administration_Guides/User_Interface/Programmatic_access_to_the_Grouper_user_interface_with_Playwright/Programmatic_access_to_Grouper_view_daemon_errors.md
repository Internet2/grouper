---
title: "Programmatic access to Grouper - view daemon errors"
space: Grouper
pageId: 28549572
version: 5
lastUpdated: 2026-07-01T05:41:46.333Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549572/Programmatic+access+to+Grouper+-+view+daemon+errors
---

# Programmatic access to Grouper - view daemon errors

This class is used to view daemon errors in the ui. Clicks on the miscellaneous page, then daemon jobs, then filters by errors. This is only going to return the maximum number of errors that can fit on one page, which is 100 by default.

### Get the errors:

 GrouperUiBrowserDaemonViewErrors grouperUiBrowserDaemonViewErrors = new GrouperUiBrowserDaemonViewErrors(page).browse();  
 List<String> jobNamesWithErrors = grouperUiBrowserDaemonViewErrors.getGrouperUiBrowserDaemonErrors();
