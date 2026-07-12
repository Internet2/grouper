---
title: "v2.4 Upgrade Instructions from v2.3"
space: GrIntDev
pageId: 48793491
version: 17
lastUpdated: 2026-07-12T17:03:10.316Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793491/v2.4+Upgrade+Instructions+from+v2.3
---

## Upgrading to Grouper 2.4 from Grouper 2.3

Using the Grouper Upgrader can simplify your upgrade process. Here is a [movie demonstrating the Grouper upgrader](http://youtu.be/JvcKUzHnzLY). The upgrader can upgrade an installed env of the API, UI, WS, client, PSP, etc. If you dont have a build script to manage multiple envs, you might want to use the upgrader.

#### Upgrade Steps

1. Make sure you are using Java8/Tomcat8.5
2. Download the [grouper installer](https://software.internet2.edu/grouper/release/2.4.0/grouperInstaller.jar)
3. Run the upgrader for each env (test, prod, ui, ws, etc)

#### Important Changes in Grouper 2.4 that impact the upgrade

**GSH next generation**: GSH now uses Groovy shell. If your scripts dont work, set this in grouper.properties. (Though also inform the Grouper developers via Jira or email in case a fix is needed since the legacy GSH will be removed in the future.)

```
gsh.useLegacy = true
```

#### Convert the ehcache xml file to properties

This is taken care of by the upgrader above. If not, then you can do this manually

1. If you have not customized Grouper caching (generally the case), just delete the ehcache xml and use the properties file shipped with Grouper.
2. If you have customized Grouper caching
  
  1. Run the grouperInstaller → upgradeTask to convert this file
  2. Copy the grouper.cache.properties to all envs (prod, test, ui, ws, etc)

#### Convert the sources.xml to subject.properties

This is taken care of by the upgrader above. If not, then you can do this manually

1. Run the grouperInstaller → admin → upgradeTask to convert this file
2. Copy the subject.properties to all envs (prod, test, ui, ws, etc)

#### vt-ldap was converted to Ldaptive

1. Test your LDAP loader and subject sources to make sure they work correctly. Note that if you use LDAP for your subject source, your configuration **must** change. For more details, visit the [vt-ldap to ldaptive migration for LDAP access](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792839/vt-ldap+to+ldaptive+migration+for+LDAP+access) page. Also, try the subject source diagnostics to confirm your configuration.

#### Note that the admin and lite UIs are removed

1. If you know of applications which deep link to the admin or lite UIs, update those links.
2. If you have a reason to continue using these old UIs, there is an option to manually redeploy them. (Though let us know if there is a missing feature or issue in the New UI.)

#### Custom member fields have been deprecated

1. This was a very uncommon feature so most people should not be impacted. Previously, each individual group could have multiple membership lists. There would be the default list of members, but then you could also specify separate membership lists as well. Going forward, the best practice is to use separate groups for those custom lists instead. Custom lists are not supported in the New UI, but if you still use them, they will be available in the Admin UI if you choose to manually redeploy it.

#### Schema updates

1. In previous Grouper upgrades, it was very common for large schema updates that required downtime to make the adjustments. For the 2.3 to 2.4 upgrade, the only updates are new indexes and a new table. The upgrader will automatically add these without requiring a downtime.

#### Check logs

1. Login to your UI, do some WS calls, and check your logs for errors

#### Internationalization

1. if you use internationalization, change grouper.properties   
  from: grouper.text.bundle.1.fileNamePrefix = grouperText/[grouper.text.fr.fr](http://grouper.textNg.fr.fr)   
  to: grouper.text.bundle.1.fileNamePrefix = grouperText/[grouper.textNg.fr.fr](http://grouper.textNg.fr.fr)

See Also

[Release Notes for Grouper 2.4](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793480/v2.4+Release+Notes)
