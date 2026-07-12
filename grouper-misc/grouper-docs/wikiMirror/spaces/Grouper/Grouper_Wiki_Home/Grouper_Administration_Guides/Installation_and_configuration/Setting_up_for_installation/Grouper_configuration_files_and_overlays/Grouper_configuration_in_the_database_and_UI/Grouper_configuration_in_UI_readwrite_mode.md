---
title: "Grouper configuration in UI readwrite mode"
space: Grouper
pageId: 28560375
version: 10
lastUpdated: 2026-07-01T05:35:40.707Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560375/Grouper+configuration+in+UI+readwrite+mode
---

> This page covers the read-write features of the Grouper configuration UI: importing configuration into the database, and adding, editing, deleting, and exporting individual config entries. To view configuration and see where each setting comes from, see [Grouper configuration in UI read-mode](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560155/Grouper+configuration+in+UI+read-mode).
> 
> Editing configuration from the UI is available in Grouper v2.4+ (introduced in the v2.4 patch series, 2019).

 > You must be a Grouper sysadmin (a member of the `wheel` or `root` group) to view or edit configuration in the UI. In addition, `grouperUi.configuration.enabled` must be `true` and the request must come from an IP listed in `grouperUi.configurationEditor.sourceIpAddresses`. See the read-mode page for those settings.

 

## Import configuration into the database

 Importing is the first step to managing configuration in the database: it loads the values from your existing properties files into the `grouper_config` table so you can edit them in the UI. Passwords and encrypted values are not imported.

 

 

 

## Add a config item

 Note: Grouper increasingly configures things through dedicated wizards (databases, sources, provisioners, and so on), so make sure there isn't a purpose-built way to configure what you need before adding a raw config item.

 

 

 

## Edit a config entry

 Editing a config entry adds a database config if the setting is not already in the database, or edits the existing database config.

 If a config key (case-insensitive) contains `pass`, `secret`, or `private`, Grouper encrypts its value in the database by default.

 

 

 

## Delete a config entry

 This is for configuration stored in the database; you can't delete configuration that comes from config files. The "Delete" button only shows when the config is in the database. Note that editing and blanking out a value might or might not effectively "delete" the config.

 

 

 

## Export configuration

 You can export configuration from the UI and download a properties file for each config type. Exports do not include passwords or encrypted values.
