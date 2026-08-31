---
title: "Programmatic access to Grouper - verify version"
space: Grouper
pageId: 28549586
version: 3
lastUpdated: 2026-07-01T05:41:44.300Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549586/Programmatic+access+to+Grouper+-+verify+version
---

# Programmatic access to Grouper - verify version

This class is used to verify the version of the UI.

### Get the current UI version

 GrouperUiBrowserGeneralVerifyVersion grouperUiBrowserGeneralVerifyVersion = new GrouperUiBrowserGeneralVerifyVersion(page).browse(); String uiVersion = grouperUiBrowserGeneralVerifyVersion.getUiVersion().toString(); 

### Confirm the current ui version

 new GrouperUiBrowserGeneralVerifyVersion(page).assignExpectedVersion("4.0.0").browse();
