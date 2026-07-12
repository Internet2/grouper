---
title: "Grouper caching in future state"
space: GrIntDev
pageId: 48792658
version: 4
lastUpdated: 2026-07-12T06:45:36.993Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792658/Grouper+caching+in+future+state
---

- Grouper should ditch ehcache
- Grouper should not query cache (e.g. hibernate caching)
- Expirable caches are useful
- Grouper already has a mechanism to clear caches of all JVMs though
  
  - It is at the cache level not the element level
  - If things are change log dependent, could take a minute
- The changelog should be used for cache expiration (where possible)

## Proposal

- Caches should cache minimal data (if possible use the new numeric id)
- Allow a cache clearing through database at the element level
  
  - This means different things to different caches
- Run change log to change log temp more frequently
  
  - Every 10 seconds?
- Have some easily built ins to cache groups
  
  - e.g. Grouper admins
- Bump up expirable caches that are element cleared
  
  - e.g. 10 minutes
  - e.g. Configuration in the database

## Change log caching

- Things that are cached that flow through change log should use that for cache expiration
- Groups, stems, memberships, privileges, subjects are all candidates for this
- Caches could be somewhat long lived (maybe not longer than 30-60 minutes) and can be expired through change log reviewer (not necessarily a consumer)
- Each JVM can review change log events to update caches accordingly

## Refine

- Profile grouper database and external system activity and dial in the caching
