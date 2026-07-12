---
title: "Grouper bug - GRP-5107 - authentication bypass remove mediation"
space: Grouper
pageId: 28554572
version: 2
lastUpdated: 2026-07-01T05:40:09.072Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554572/Grouper+bug+-+GRP-5107+-+authentication+bypass+remove+mediation
---

If you installed the remediation for this issue, and are in a later version than: v2.5 after v2.5.69, v4 after v4.8.0, and v5 after v5.5.0, then you can remove the remediation.

1. You can remove this script daemon: grouperTempUserDaemon
2. You can remove passwords for alternate subject IDs or identifiers in the grouper password table. Run this SQL.
  
  
  ```
  delete from grouper_password_recently_used gpru 
  where gpru.grouper_password_id in (select gp.id from grouper_password gp
  where gp.the_password like 'xXxXx%');
  
  delete from grouper_password gp where gp.the_password like 'xXxXx%';
  
  commit;
  ```
3. You can remove the loader job on your WS users group (this will loosely couple your credentials and who is allowed to use WS). Generally this is the group: etc:webServiceClientUsers , but it is whatever is configured in grouper-ws.properties for: ws.client.user.group.name Note: any time you add a credential, if you have this group configured, you need to add the subject to this group. If you want to leave the loader job so that there is a 1 to 1 mapping between users who can use WS and users with passwords, feel free.
4. It is recommended to keep the WS client user group so that you can control who can use WS. If you want to remove this, it is up to you.
