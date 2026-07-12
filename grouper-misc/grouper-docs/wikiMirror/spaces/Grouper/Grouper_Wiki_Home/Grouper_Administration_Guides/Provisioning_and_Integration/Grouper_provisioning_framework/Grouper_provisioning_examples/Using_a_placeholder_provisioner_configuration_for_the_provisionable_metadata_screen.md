---
title: "Using a placeholder provisioner configuration for the provisionable metadata screen"
space: Grouper
pageId: 28554518
version: 4
lastUpdated: 2026-07-01T05:40:16.643Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554518/Using+a+placeholder+provisioner+configuration+for+the+provisionable+metadata+screen
---

This example shows how to setup a dummy provisioner (does not actually provision or even have a provisioning daemon), but allows the provisionable screen to have metadata.

Alternatives: use the attribute framework UI, or a GSH template

Advantages of doing this:

1. Can delegate using the provisioning security
2. Can have a more usable screen with labels and documentation
3. Can easily see which groups are managed
4. Summary provisioning screen shows number of objects

Disadvantages

1. Document this internally so admins know this is not a real provisioner

## Placeholder provisioner configuration

There is a provisioner without provisioning daemons that exists just so things can be easily assigned with a form for the settings of days back and entity id

grouper-loader.properties

| `provisioner.webloginServiceProviderRecentUsersToGroup.``class` `= edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner`   `provisioner.webloginServiceProviderRecentUsersToGroup.configureMetadata =``true`   `provisioner.webloginServiceProviderRecentUsersToGroup.customizeGroupCrud =``true`   `provisioner.webloginServiceProviderRecentUsersToGroup.dbExternalSystemConfigId = grouper`   `provisioner.webloginServiceProviderRecentUsersToGroup.deleteGroups =``false`   `provisioner.webloginServiceProviderRecentUsersToGroup.groupAttributeValueCacheHas =``true`   `provisioner.webloginServiceProviderRecentUsersToGroup.groupTableIdColumn = id`   `provisioner.webloginServiceProviderRecentUsersToGroup.groupTableName = some_table_doesnt_exist`   `provisioner.webloginServiceProviderRecentUsersToGroup.insertGroups =``false`   `provisioner.webloginServiceProviderRecentUsersToGroup.metadata.``0``.name = md_entityId`   `provisioner.webloginServiceProviderRecentUsersToGroup.metadata.``0``.required =``true`   `provisioner.webloginServiceProviderRecentUsersToGroup.metadata.``0``.showForGroup =``true`   `provisioner.webloginServiceProviderRecentUsersToGroup.metadata.``1``.name = md_days`   `provisioner.webloginServiceProviderRecentUsersToGroup.metadata.``1``.required =``true`   `provisioner.webloginServiceProviderRecentUsersToGroup.metadata.``1``.showForGroup =``true`   `provisioner.webloginServiceProviderRecentUsersToGroup.metadata.``1``.valueType = integer`   `provisioner.webloginServiceProviderRecentUsersToGroup.numberOfGroupAttributes =``1`   `provisioner.webloginServiceProviderRecentUsersToGroup.numberOfMetadata =``2`   `provisioner.webloginServiceProviderRecentUsersToGroup.operateOnGrouperGroups =``true`   `provisioner.webloginServiceProviderRecentUsersToGroup.selectGroups =``false`   `provisioner.webloginServiceProviderRecentUsersToGroup.startWith =``this` `is start with read only`   `provisioner.webloginServiceProviderRecentUsersToGroup.targetGroupAttribute.``0``.name = id`   `provisioner.webloginServiceProviderRecentUsersToGroup.targetGroupAttribute.``0``.translateExpressionType = grouperProvisioningGroupField`   `provisioner.webloginServiceProviderRecentUsersToGroup.targetGroupAttribute.``0``.translateFromGrouperProvisioningGroupField = id`   `provisioner.webloginServiceProviderRecentUsersToGroup.updateGroups =``false` |
| --- |

Externalized text in [grouper.textNg.en.us](http://grouper.textNg.en.us).base.properties

| `md_days_webloginServiceProviderRecentUsersToGroup_label = Days back`   `md_days_webloginServiceProviderRecentUsersToGroup_description = Number of days from``1` `to``60``. i.e.``if` `7` `is entered then the group will be loaded with users who have logged in within the last week.`       `md_entityId_webloginServiceProviderRecentUsersToGroup_label = Service provider entity id`   `md_entityId_webloginServiceProviderRecentUsersToGroup_description = SAML entity id of the application the user is logging in to. You can get``this` `from SAML tracer (Firefox) when logging in to the app.` |
| --- |

## Using the provisionable metadata in a daemon (not a provisioning daemon)

[See this document for an example of using the metadata from a script daemon](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560387/Grouper+daemon+other+job+GSH+script+to+manage+group+memberships+from+Splunk+WS)
