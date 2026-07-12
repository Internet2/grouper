---
title: "Grouper configuration in UI read-mode"
space: Grouper
pageId: 28560155
version: 16
lastUpdated: 2026-07-01T05:36:10.373Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560155/Grouper+configuration+in+UI+read-mode
---

The Grouper UI includes a read-only configuration viewer that shows the current value of any configuration property, where that value comes from (which config file or the database), its default, its documentation, and metadata about it. This is the companion to the read-write configuration editor; this page covers viewing configuration.

 > Viewing configuration in the UI was added in the v2.4 patch series (`grouper_v2_4_0_api_patch_66`, `grouper_v2_4_0_ui_patch_39`, `grouper_v2_4_0_ws_patch_7`, 2019) and is present in all currently supported releases.

 > **Required privileges.** You must be a Grouper sysadmin (a member of the wheel group, or root) to view or edit configuration in the UI — the viewer is shown only when `PrivilegeHelper.isWheelOrRoot` is true (`ConfigurationContainer.isConfigureShow`). In addition:
> 
>  
> 
> - `grouperUi.configuration.enabled` must be `true` (the default).
> - The request must come from an allowed source IP (`grouperUi.configurationEditor.sourceIpAddresses`). The effective default is `127.0.0.1/32` (localhost only); set it to `0.0.0.0/0` to allow access from everywhere.

 

## Quick start

 The configuration viewer needs every config file present in the UI (and in the WS and loader). Before importing, make sure the UI has a `grouper-ws.properties`, a `grouper-loader.properties`, and a `grouper-ui.properties`. Those files may be blank.

 The relevant settings ship in `grouper-ui-ng.base.properties`:

 
```text
#######################################
## Configuration in ui
#######################################

# allow configuration from ui
# {valueType: "boolean", required: true}
grouperUi.configuration.enabled=true

# allow configuration only from these IP ranges, e.g. 1.2.3.4/32 or 2.3.4.5/24 or 2001:0db8:85a3:0000:0000:8a2e:0370:7334, comma separated, set to 0.0.0.0/0 if available from everywhere
# {valueType: "string", multiple: true}
grouperUi.configurationEditor.sourceIpAddresses.elConfig = ${elUtils.processEnvVarOrFile('GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES')}

# if the source IP is set by apache or proxy or whatever
# {valueType: "string", sampleValue: "X-FORWARDED-FOR"}
grouperUi.reverseProxyForwardedForHeader = 
```

 You can:

 

1. Disable UI configuration.
2. Open up the source IP address to allow a non-localhost IP address.
3. List a reverse proxy header that the source IP is read from in the incoming HTTP request, e.g. `X-FORWARDED-FOR`.

 You can also set these in the database via GSH. See the [GrouperShell](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh) wiki for more info.

 
```java
new edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig().configFileName("grouper-ui.properties").propertyName("grouperUi.configurationEditor.sourceIpAddresses").value("1.2.3.4/32").store();
```

 To debug source IP address evaluation, raise the log level for the configuration servlet in `log4j2.xml`:

 
```xml
<Logger name="edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Configure" level="debug" additivity="false">
    <AppenderRef ref="stderr"/>
</Logger>
```

 

## Viewing configuration

 Open the "Miscellaneous" menu and click "Configure".

 

 Pick a config file from the "Config file" dropdown to list its properties. (Changes take a couple minutes to propagate to all JVMs connected to this database.)

 

 Each property is shown with its current value, documentation, and where it is configured.

 

 

## What the configuration viewer shows

 For each configuration property the viewer displays the following metadata:

 

| Metadata shown | Description |
| --- | --- |
| Property name | The property name from the config file. |
| Type of value | The expected value type (e.g. boolean, string, class, integer). |
| Current value | The current processed value (after any script/EL is evaluated). |
| Documentation | The description of the configuration property. |
| Configured in | Which config file or the database the value is set in, along with the default value from the "base" config file or the configuration metadata. |
| Password masking | Passwords are masked, unless the value is a password file, in which case the file location is shown. |

  Property name from property file

 

 Type of value

 

 Current processed (if script) value

 

 Documentation of configuration property

 

 Where the configuration is set (which config file or in the database), and the default value from the "base" config file or configuration metadata

 

 Passwords are masked unless the value is a password file, in which case the file location is listed

 

  

## Configuration file names

 The UI and WS base configuration files live in the API and are named `grouper-ui-ng.base.properties` and `grouper-ws-ng.base.properties` (the older `grouper-ui.base.properties` and `grouper-ws.base.properties` names are no longer used for these settings).
