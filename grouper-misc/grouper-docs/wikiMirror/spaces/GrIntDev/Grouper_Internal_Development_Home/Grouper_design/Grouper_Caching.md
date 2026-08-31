---
title: "Grouper Caching"
space: GrIntDev
pageId: 48793141
version: 3
lastUpdated: 2026-07-12T07:01:21.431Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793141/Grouper+Caching
---

# DRAFT

 Data is cached in Grouper. We need to redo the strategy so we get fewer stale reads. Currently Grouper uses ehcache in Hibernate caching, Hibernate second level caching (I think), Hibernate query caching, ehcache global caches, and GrouperSession ehcaches.

 We should consider:

 

- Im not sure how to use caches in Hibernate and out of hibernate, maybe we shouldnt use any hibernate caching (or see how to use a hibernate cache outside of hibernate)
- Queries could return id's, and be resolved by cache service
- Caches need to be expired (or parts of caches) as objects get updated
- Do we need GrouperSession specific caches?
- Can we use ehache or terracotta distributed caches (i.e. so UI updates are reflected in WS or in clusters)
- Can we have a flag on WS calls (and API calls) which says dont use any cache
- Should we not use the query cache? Does it do any good?
- Do we need an ehcache interface so you dont have to use ehache?
- Do we need local caches or a centralized cache (e.g. terracotta or memcached)?

 We should look into eliminating non hibernate caches and only using hibernate ones... if that is possible... then keep the query caches, and the lookups from id to object would be hibernate lookups
