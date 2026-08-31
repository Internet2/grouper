---
title: "Grouper Ehcache error message and resolution"
space: GrIntDev
pageId: 48792743
version: 4
lastUpdated: 2026-07-12T06:45:40.320Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792743/Grouper+Ehcache+error+message+and+resolution
---

If you get a log message like this:

```
tomee;catalina.out;${env:ENV};${env:USERTOKEN};2020-12-03 13:30:54,400 [ajp-nio-0.0.0.0-8009-exec-6] WARN  org.hibernate.cache.ehcache.internal.strategy.AbstractReadWriteEhcacheAccessStrategy- HHH020008: Cache[ImmediateMembershipEntry] Key[ImmediateMembershipEntry#61a3c0a316ca435a856bc16f2268ee38] Lockable[(null)]
A soft-locked cache entry was expired by the underlying Ehcache. If this happens regularly you should consider increasing the cache timeouts and/or capacity limits
```

See the cache name there: Cache[ImmediateMembershipEntry] or "ImmediateMembershipEntry"

You can edit your grouper.cache.properties in UI config:

WAS

```
cache.name.ImmediateMembershipEntry.maxElementsInMemory = 10000
```

INCREASE

```
cache.name.ImmediateMembershipEntry.maxElementsInMemory = 100000
```

You might want to contact the grouper team to increase the default for that cache, and look in future releases to see if it is increased you can remove your override.

If the cache name contains periods, it will have an alias which must be used in the grouper.cache.properties file.

If your entry looks like this:

```
tomee;catalina.out;local;daemon;2020-12-08 16:41:49,505 [DefaultQuartzScheduler_Worker-1] WARN  org.hibernate.cache.ehcache.internal.strategy.AbstractReadWriteEhcacheAccessStrategy- HHH020008: Cache[edu.internet2.middleware.grouper.pit.PITAttributeAssign] Key[edu.internet2.middleware.grouper.pit.PITAttributeAssign#1fea90dd500548639f3c1ab217ea6a43] Lockable[(null)]
```

Then you will need to look for that class name in the grouper.cache.base.properties file. Find the line which looks like this:

```
# name of cache referred in java
# {valueType: "string", required: true}
cache.name.pit_PITAttributeAssign.name = edu.internet2.middleware.grouper.pit.PITAttributeAssign
```

and then you can use that alias on your adjustments to the cache attributes as shown above. E.g.:

```
cache.name.pit_PITAttributeAssign.maxElementsInMemory = 10000
```
