---
title: "Grouper components overview"
space: Grouper
pageId: 28543703
version: 84
lastUpdated: 2026-07-12T15:26:10.622Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543703/Grouper+components+overview
---

Here is an overview of components in the Grouper Toolkit.

| Component (with link to more info) | Description |
| --- | --- |
| Installer (deprecated) | Installs the Grouper API, quickstart data, UI, WS, client, and PSP. |
| API | The core of the Grouper system. |
| Subject API | Handles the connection with a site's existing Identity Management operations |
| Grouper Database | Registry/repository of Grouper data |
| [Grouper Shell](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh) | Command line for interacting with the Grouper API, including XML import / export |
| [Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services) | Allows application developers to leverage Grouper (SOAP and REST) |
| [Grouper Client](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545215/Grouper+Client) | A java client for Grouper web services |
| Grouper Loader | Synchronizes group memberships based on an external data source. This also runs various daemons and should be run in any Grouper installation |
| [Grouper Daemon](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545241/Grouper+Daemon) | A command line process that can handle many Grouper tasks |
| [Grouper User Interface](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543095/User+Interface) | Allows browsing tree structure, managing groups, managing favorites and more. |
| [Attribute Framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework) | Allows you to attach metadata to objects in the registry |
| Notifications/ChangeLog | Logs changes and allows for notification to external systems (using XMPP, HTTPS, and other connectors ) |
| [Access Management Features](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544689/Access+Management+Features+Overview) | See the overview to learn about roles and permissions, rules, enable/disable dates and more |
| [Rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules) | Configurable declarative scripts that run at certain times and perform specified actions |
| [External Users](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545569/Grouper+external+subjects) | Supports external or federated users |
| Diagnostics | Reports on the health of Grouper |
| [Provisioning Service Provider Next Generation (PSPNG)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548413/Grouper+Provisioning+PSPNG+Legacy)  (deprecated) | Provisions Grouper objects to LDAP/AD |
| [Visualization](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548433/Visualization+UI) | Graphs Grouper objects |
| [Reporting](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554409/Grouper+reporting) | Provides a report on a group or folder |
| [Attestation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545015/Grouper+attestation) | Marks a group or folder so that owners must review the membership list periodically |

.  
.  
.

| Connectors (with link to more info) | Description |
| --- | --- |
| [ESB Connector](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545514/Grouper+ESB+connector) | Enable Grouper to interface with an ESB |
| Kuali Integration | Allows integration with Kuali Rice |
| [SCIM Integration](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548072/Grouper+SCIM+change+log+client) | Allows integration with SCIM (available in Grouper 2.2 and above) |
| Atlassian Connector | Implements the Atlassian access and profile providers |
| Grouper VOOT Connector | implements the [VOOT specification](https://github.com/andreassolberg/voot/wiki/Protocol) |
| [Another Grouper Instance](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549198/Syncing+groups+between+group+management+systems) | Allows sharing a group between two Grouper management systems |

#### See Also

[Grouper Architecture Diagram](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548688/Architectural+and+High-Level+Diagram)
