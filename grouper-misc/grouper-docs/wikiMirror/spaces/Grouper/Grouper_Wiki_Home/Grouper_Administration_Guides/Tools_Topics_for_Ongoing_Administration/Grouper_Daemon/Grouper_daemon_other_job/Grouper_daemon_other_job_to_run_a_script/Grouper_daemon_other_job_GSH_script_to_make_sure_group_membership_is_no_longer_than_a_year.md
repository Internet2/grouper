---
title: "Grouper daemon \"other job\" GSH script to make sure group membership is no longer than a year"
space: Grouper
pageId: 28560012
version: 2
lastUpdated: 2026-07-01T05:36:32.660Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560012/Grouper+daemon+other+job+GSH+script+to+make+sure+group+membership+is+no+longer+than+a+year
---

## Configure

Script

```
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

    String groupName = "test:testGroup";
    GrouperSession.startRootSession();

    StringBuilder sql = new StringBuilder("select ms from MembershipEntry as ms, Group g where ms.immediateFieldId = :defaultMembersField " +
        "and ms.type = 'immediate' and (ms.disabledTimeDb is null or ms.disabledTimeDb > :queryTime) " +
        "and ms.ownerGroupId = g.uuid and g.nameDb = :groupName");
    long yearInFuture = System.currentTimeMillis()+ 365*24*60*60*1000L;

    Set<Membership> memberships = HibernateSession.byHqlStatic().createQuery(sql.toString()).setCacheable(false).
        setString("groupName", groupName).setString("defaultMembersField", Group.getDefaultList().getUuid()).
        setLong("queryTime", yearInFuture).listSet(Membership.class);
    
    for (Membership membership : memberships) {
      membership.setDisabledTimeDb(yearInFuture);
      membership.update();
    }

    OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addUpdateCount(GrouperUtil.length(memberships));

```

## See updates
