---
title: "Penn Grouper upgrade testing plan"
space: Grouper
pageId: 28545042
version: 2
lastUpdated: 2026-07-01T05:47:49.445Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545042/Penn+Grouper+upgrade+testing+plan
---

BTW, [this looks like a better guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544532/Container+update+process) from Princeton. Even though as a Penn employee I'm legally not allowed to compliment Princeton

- Make sure new nodes are running in AWS console and old nodes are not
- Test the UI
  
  - See the new version in Miscellaneous → Configure
  - Read
  - Write
  - Click around
- Test read WS calls
  
  
  
  | `[mchyzer``@flash` `pennGroupsClient-``2.6``.``0``]$ java -jar grouperClient-``2.6``.``13``.jar --operation=hasMemberWs --groupName=test:testGroup --subjectIdentifiers=mchyzer --debug=``true` |
  | --- |
- Test write WS calls
  
  
  
  | `java -jar grouperClient-``2.6``.``17``.jar --operation=addMemberWs --groupName=test:testGroup --subjectIdentifiers=convery --debug=``true`   `java -jar grouperClient-``2.6``.``17``.jar --operation=deleteMemberWs --groupName=test:testGroup --subjectIdentifiers=convery --debug=``true`   `java -jar grouperClient-``2.6``.``17``.jar --operation=addMemberWs --groupName=test:testGroup --subjectIdentifiers=convery --debug=``true` |
  | --- |
- Test daemons running
  
  - Run any daemons which havent started
- Test the ui lite screens
  
  - Try an eform popup: [https://provider.www.upenn.edu/computing/da/eforms/](https://provider.www.upenn.edu/computing/da/eforms/)
- Test the custom ui screens
- GSH command line interface
- Test GSH templates
- Test LDAP/AD provisioning
  
  - Add a group, see it in ldap
  - Update an existing group membership, see it in ldap
  - Delete a group from grouper, see it gone in ldap
  - Mark an existing group as provisionable, see it in ldap
  - Remove provisionable from provisioned group, see it gone from ldap
