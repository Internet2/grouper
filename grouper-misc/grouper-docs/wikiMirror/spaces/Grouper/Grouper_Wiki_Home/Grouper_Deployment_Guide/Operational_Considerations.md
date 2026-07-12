---
title: "Operational Considerations"
space: Grouper
pageId: 28543134
version: 19
lastUpdated: 2026-07-12T15:26:04.129Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543134/Operational+Considerations
---

Here are key places in the Grouper documentation and training with information on operational considerations:

- [Tools & Topics for Ongoing Administration](https://spaces.internet2.edu/pages/viewpage.action?pageId=15173596)
- [Ongoing Administration Tasks](https://spaces.internet2.edu/display/Grouper/Ongoing+Administration+Tasks)
- [Grouper Training - Admin - Maintenance - Take 1](https://www.youtube.com/watch?v=2ItbT6QIyDc) (deprecated)
- [Grouper diagnostics](https://spaces.internet2.edu/display/Grouper/Grouper+diagnostics)
- [Grouper report](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545058/Grouper+overall+summary+administrative+report)

You can monitor the health of the Grouper using the  [Grouper diagnostics](https://spaces.internet2.edu/display/Grouper/Grouper+diagnostics)  URLs at http://{hostname}/grouper/status?diagnosticType=[trivial|db|all] for the Grouper UI, and http://{hostname}/grouperWS/status?diagnosticType=[trivial|db|all] for Grouper WS. If everything is ok, a 200 HTTP code will be returned, otherwise a 500 is returned with a description of the issue. The diagnostic URL has many options and is suitable for monitoring by systems like Nagios, Big Brother, etc. If you do not see the word SUCCESS on the “all” page, then something is wrong. Have monitoring tools like Nagios look for SUCCESS.

Use the  [Unresolvable Subject Deletion Utility (USDU)](https://spaces.internet2.edu/pages/viewpage.action?pageId=14517820)  to clean up membership assignments for subjects that are no longer resolved by the Subject API.

Previous: [Provisioning Models](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543297/Provisioning+Models)

Next: [Conclusion](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543239/Conclusion)
