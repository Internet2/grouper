---
title: "Grouper highlights 2.0"
space: GrIntDev
pageId: 48792894
version: 38
lastUpdated: 2026-07-12T17:02:41.035Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792894/Grouper+highlights+2.0
---

> This page is designed for Grouper Developers to list and briefly describe their work items for the Grouper 2.0 release

 

#### Chris

 

| Feature | Description | Additional Links |
| --- | --- | --- |
| [Rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules) | Similar to Grouper Hooks, but instead of Java logic, built in actions or expression language scripts can be executed |  |
| [External subjects](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545569/Grouper+external+subjects) | If your Identity Management System does not support external users (e.g. via EPPN), then Grouper can manage that with self registration and or invitations which will can provision memberships |  |
| [Syncing groupers](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549198/Syncing+groups+between+group+management+systems) | A group in one Grouper can be sync'ed with a group in another Grouper. For instance if two institutions want to share a group of subjects but store them in their own Grouper |  |
| Attribute and Permissions UI | User interface to define, view, and assign attributes and permissions in Grouper. The attributes can be assigned to many types of Grouper objects including Groups, Folders, Members, Memberships, etc. The permissions are used as a central permissions management system for other applications at your institution |  |
| Grouper-Atlassian connector | If you cannot connect Atlassian applications (e.g Jira, Confluence) to your Grouper managed LDAP, then you can use this connector which used Grouper Web Services to manage your Atlassian groups and person information |  |
| [Permissions Allow/disallow](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547660/Grouper+permissions+allow+and+disallow) | A permission assignment can be an allow or disallow (to filter out allows inherited from another assignment) |  |
| [Permission limits](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548474/Grouper+permission+limits) | A run-time decision can be applied to immediate permission allows so that context environment variables can change an allow to a disallow. e.g. permissions are only allowed at a certain time of day or from a certain IP address. Grouper can calculate this on the server or the client can get the limits and calculate them. |  |
| [Web service versioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548558/Grouper+Web+Services+Versioning) | Grouper 2.0 web servers will accept clients coded against the 1.6 or previous WS API's |  |

 

#### Shilen

 

| Feature | Description | Additional Links |
| --- | --- | --- |
| [Point in Time Audit](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548000/Point+in+Time+Auditing) | This allows you to query the state of Grouper at a point in time in the past or a date range in the past. You can query for memberships, privileges and permissions. |  |
| [Member Search and Sort](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548031/Member+search+and+sort+columns) | Additional data is now stored about subjects in Grouper. This allows you to sort a list of members and search a list of members without having to go to the subject source to query attributes for each subject in the list that you would then use for the sort or search operation. |  |

 

#### TomZ

 

| Feature | Description | Additional Links |
| --- | --- | --- |
| ldappcng caching (performance) | The SPMLDataConnector supports caching similar     to other Shibboleth DataConnectors | [https://bugs.internet2.edu/jira/browse/GRP-503](https://bugs.internet2.edu/jira/browse/GRP-503) |
| real-time / incremental provisioning (tentative) | scheduled for 2.1 | [https://bugs.internet2.edu/jira/browse/GRP-592](https://bugs.internet2.edu/jira/browse/GRP-592) |
