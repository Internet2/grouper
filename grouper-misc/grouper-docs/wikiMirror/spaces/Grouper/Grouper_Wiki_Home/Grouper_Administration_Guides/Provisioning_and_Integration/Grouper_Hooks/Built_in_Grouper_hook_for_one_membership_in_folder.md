---
title: "Built in Grouper hook for one membership in folder"
space: Grouper
pageId: 28548461
version: 7
lastUpdated: 2026-07-01T05:44:28.558Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548461/Built+in+Grouper+hook+for+one+membership+in+folder
---

If a folder is configured for this via attribute, and the hook is enabled, it will make sure that as members are added to groups, if they are members of other groups in the immediate folder, they will be removed from the other groups.

[JIRA GRP-1231](https://bugs.internet2.edu/jira/browse/GRP-1231)

Note, this is enabled on the demo server, for the groups (ask Chris for access if you want to try it out, you need to be in the test:oneMembershipAdmin group):

- test:oneMembership:test1
- test:oneMembership:test2
- test:oneMembership:test3

### Grouper version

This is available in 2.2.2+ (and 2.2.2 has to be fully patched, since this is in API patch #3)

### Configure

to use this, first enabled the hook in grouper.properties, then restart the JVM

```
hooks.membership.class = edu.internet2.middleware.grouper.hooks.examples.MembershipOneInFolderMaxHook
```

if you are installing the 2.2.2 patch for this (API#3), then if the ehcache.example.xml doesnt match up, you might need to copy it from the "old" dir (adjust your dirs appropriately):

```
[appadmin@i2midev1 patches]$ cp /opt/grouper/2.2/patches/grouper_v2_2_2_api_patch_3/old/classes/ehcache.example.xml /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes
```

you will see this in the logs on startup (or wherever attribute built ins live in your grouper):

Grouper note: auto-created attributeDefName: etc:attribute:hooks:hookMembershipOneInFolder

assign that attribute to a folder with GSH or the UI

```
gsh 0% grouperSession = GrouperSession.startRootSession();
gsh 1% stem = StemFinder.findByName(grouperSession, "test:oneMembership");
gsh 2% hookAttribute = AttributeDefNameFinder.findByName("etc:attribute:hooks:hookMembershipOneInFolder", true);
gsh 3% stem.getAttributeDelegate().assignAttribute(hookAttribute);
```
