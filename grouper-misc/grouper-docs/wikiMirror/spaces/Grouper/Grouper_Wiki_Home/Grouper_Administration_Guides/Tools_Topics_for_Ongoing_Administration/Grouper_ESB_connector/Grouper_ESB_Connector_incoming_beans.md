---
title: "Grouper ESB Connector incoming beans"
space: Grouper
pageId: 28673884
version: 5
lastUpdated: 2026-07-01T05:35:05.028Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28673884/Grouper+ESB+Connector+incoming+beans
---

Beans for incoming data:

 Incoming data must be parseable into [an EsbListenerEvents bean](http://anonsvn.internet2.edu/cgi-bin/viewvc.cgi/i2mi/tags/GROUPER_1_6_0/grouper/src/esb/edu/internet2/middleware/grouper/esb/listener/EsbListenerEvents.java?sortdir=down&view=log) containing a single array of multiple events.

 Each event must be parsable into [the EsbListenerEvent class](http://anonsvn.internet2.edu/cgi-bin/viewvc.cgi/i2mi/tags/GROUPER_1_6_0/grouper/src/esb/edu/internet2/middleware/grouper/esb/listener/EsbListenerEvent.java?sortdir=down&view=log).
