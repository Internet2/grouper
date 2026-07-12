---
title: "Grouper Training - Administration - Lesson: Loader simple SQL"
space: Grouper
pageId: 28544381
version: 7
lastUpdated: 2025-04-09T18:21:50.141Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544381/Grouper+Training+-+Administration+-+Lesson+Loader+simple+SQL
---

### **Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

### Errata

1. In the video you will see this page as the following, but you are on the right guide if you are here. Just ignore that from the video.

### Lesson guide

Loader query

```
SELECT subject_id, subject_source subject_source_id FROM grouper_members WHERE subject_id like '800001%'
```

Schedule cron config string

```
0 0 6 * * ?
```
