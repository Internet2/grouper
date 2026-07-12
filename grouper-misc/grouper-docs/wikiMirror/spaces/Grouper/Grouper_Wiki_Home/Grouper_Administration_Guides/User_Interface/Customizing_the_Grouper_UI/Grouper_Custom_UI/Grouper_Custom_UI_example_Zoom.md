---
title: "Grouper Custom UI example Zoom"
space: Grouper
pageId: 28554923
version: 4
lastUpdated: 2026-07-01T05:39:20.589Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554923/Grouper+Custom+UI+example+Zoom
---

You can use the zoom query type to get user information into variables. You can get the summary:

```
{
  "variableToAssign":"cu_zoomSummary",
  "userQueryType":"zoom",
  "variableType":"string",
  "configId":"pennZoomProd",
  "script":"${summary}",
  "variableToAssignOnError":"cu_zoomSummaryError",
  "label":"${textContainer.text['penn_zoom_cu_zoomSummary']}",
  "errorLabel":"${textContainer.text['penn_zoom_cu_zoomSummaryError']}",
  "order":300
}
```

Results in:

Then you can use those variables:

```
{
  "variableToAssign":"cu_zoomHasActiveAccount",
  "userQueryType":"zoom",
  "variableType":"boolean",
  "configId":"pennZoomProd",
  "script":"${hasEmail && userFound && userActive && 'licensed' == typeString}",
  "label":"${textContainer.text['penn_zoom_cu_zoomHasActiveAccount']}",
  "errorLabel":"${textContainer.text['penn_zoom_cu_zoomHasActiveAccountError']}",
  "order":38,
  "variableToAssignOnError":"cu_zoomHasActiveAccountError"
}
```
