---
title: "Grouper bug - GRP-5107 - authentication bypass"
space: Grouper
pageId: 28548270
version: 7
lastUpdated: 2026-07-01T05:44:56.660Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548270/Grouper+bug+-+GRP-5107+-+authentication+bypass
---

In certain versions and configurations of Grouper there is an authentication bypass. The issue was discovered internally on October 5th and institutions were notified so they could install a remediation before the details were announced.

If you have a version of Grouper where this is fixed, you are not affected.

- This is fixed in v2.5.69, v4.8.0, and v5.5.0

If you have [applied the remediation, you are not affected](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555274/Grouper+bug+-+GRP-5107+-+authentication+bypass+remediation).

[Test to see if you are affected](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554556/Grouper+bug+-+GRP-5107+-+authentication+bypass+testing)

If you are running a fixed version, and you applied the remediation, [you can remove (most of) it (see details)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554572/Grouper+bug+-+GRP-5107+-+authentication+bypass+remove+mediation)

## Are you affected?

If you are using basic auth with passwords encrypted in the database.

Look in all your environments (dev/test/prod/etc), in the grouper.hibernate.properties, and see if either of these are set:

```
grouper.is.ws.basicAuthn = true

grouper.is.ui.basicAuthn = true
```

Or look in your container to see if either of these env variables are set:

```
GROUPER_WS_GROUPER_AUTH=true

GROUPER_UI_GROUPER_AUTH=true
```

If those are not set, you are not at risk and you do not need to do anything.

If you WS is only accessible from trusted source IPs and only used by trusted clients, then you are less at risk.
