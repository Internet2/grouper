---
title: "Grouper Training - Use cases - Lesson 06: Configuration"
space: Grouper
pageId: 28544324
version: 12
lastUpdated: 2026-04-22T01:27:29.120Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544324/Grouper+Training+-+Use+cases+-+Lesson+06+Configuration
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**View config files**

```
$ ./gte-shell

cd classes
pwd
ls -al
less grouper.hibernate.base.properties

# See hierarchy
less grouper.hibernate.properties
```

Exit out so you see the student user

```
[root@a21d40213e27 WEB-INF]# exit
exit
[student@ip-172-31-4-56 ~]$ 

```
