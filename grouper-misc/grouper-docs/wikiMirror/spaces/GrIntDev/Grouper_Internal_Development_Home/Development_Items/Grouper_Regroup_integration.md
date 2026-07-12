---
title: "Grouper Regroup integration"
space: GrIntDev
pageId: 48792873
version: 3
lastUpdated: 2026-07-12T06:45:49.942Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792873/Grouper+Regroup+integration
---

Regroup ([http://www.regroup.com/](http://www.regroup.com/)) is a SaaS notification system.

Regroup can be used for:   
• A Mass Notifications / Emergency Messaging platform that can have its group members managed externally. This is done through Regroup’s unique feature of membership and messaging APIs  
• Messaging to people in grouper. e.g. emailing, texting grouper groups

On July 22, 2015, the Grouper team had a conference call with Regroup to discuss how Regroup can be integrated with Grouper so that groups in Regroup can be managed from Grouper.

There are no plans to develop an integration until we have an institution which is a client of Regroup and which uses Grouper and wants to integrate the two.

### How the integration could work

The Regroup API is membership based, there are not operations to update users without updating memberships. Maybe there should be a feed for a group of all users which might not be used for notification which manages the users, emails, phones, etc. Maybe that feed would be outside of Grouper? Then the Grouper integration can focus on matching up the groups in Regroup with the userIds of users in those groups. Not sure if all groups in Regroup would be managed by Grouper or just some of them.
