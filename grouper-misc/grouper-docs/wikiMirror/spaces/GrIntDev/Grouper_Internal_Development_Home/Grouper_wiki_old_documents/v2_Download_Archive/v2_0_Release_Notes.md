---
title: "v2.0 Release Notes"
space: GrIntDev
pageId: 48793436
version: 18
lastUpdated: 2026-07-12T17:03:06.478Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793436/v2.0+Release+Notes
---

# Release Notes for Grouper v2.0

 Grouper v2.0.3 fixes a [serious SQL problem](https://bugs.internet2.edu/jira/browse/GRP-723) with 2.0.2

 Grouper v2.0.2 fixes a [couple dozen issues](https://bugs.internet2.edu/jira/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+GRP+AND+fixVersion+%3D+%222.0.2%22+AND+status+%3D+Resolved+ORDER+BY+priority+DESC&mode=hide) including making subject searches more efficient and some UI fixes

 Grouper v2.0.1 fixes [several issues](https://bugs.internet2.edu/jira/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+GRP+AND+fixVersion+%3D+%222.0.1%22+AND+status+%3D+Resolved+ORDER+BY+priority+DESC&mode=hide)

 Grouper v2.0.0 includes 47 fixes and improvements over v1.6.3. See the [full list](https://bugs.internet2.edu/jira/secure/IssueNavigator.jspa?reset=true&&pid=10020&fixfor=10330&status=5&status=6&sorter/field=issuekey&sorter/order=DESC).

 

## New Features

 

| [Rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules) | Similar to Grouper [Grouper Hooks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545347/Grouper+Hooks), but instead of Java logic, built in actions or expression language scripts can be executed |
| --- | --- |
| [External subjects](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545569/Grouper+external+subjects) | If your Identity Management System does not support external users (e.g. via EPPN), then Grouper can manage that with self registration and or invitations which will can provision memberships |
| [Syncing groupers](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549198/Syncing+groups+between+group+management+systems) | A group in one Grouper can be sync'ed with a group in another Grouper. For instance if two institutions want to share a group of subjects but store them in their own Grouper |
| Attribute and Permissions UI | User interface to define, view, and assign attributes and permissions in Grouper. The attributes can be assigned to many types of Grouper objects including Groups, Folders, Members, Memberships, etc. The permissions are used as a central permissions management system for other applications at your institution |
| Grouper-Atlassian connector | If you cannot connect Atlassian applications (e.g. Jira, Confluence) to your Grouper managed LDAP, then you can use this connector which used Grouper Web Services to manage your Atlassian groups and person information |
| [Permissions Allow/disallow](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547660/Grouper+permissions+allow+and+disallow) | A permission assignment can be an allow or disallow (to filter out allows inherited from another assignment) |
| [Permission limits](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548474/Grouper+permission+limits) | A run-time decision can be applied to immediate permission allows so that context environment variables can change an allow to a disallow. e.g. permissions are only allowed at a certain time of day or from a certain IP address. Grouper can calculate this on the server or the client can get the limits and calculate them. |
| [Web service versioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548558/Grouper+Web+Services+Versioning) | Grouper 2.0 web servers will accept clients coded against Grouper 1.6 or previous WS API's |
| [Point in Time Audit](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548000/Point+in+Time+Auditing) | This allows you to query the state of Grouper at a point in time in the past or a date range in the past. You can query for memberships, privileges and permissions. |

 For more information about upcoming plans, see the [Grouper+Product+Roadmap](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541781/Grouper+Product+Roadmap).

 

## Improvements & Fixes

 

| [Member Search and Sort](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548031/Member+search+and+sort+columns) | Additional data is now stored about subjects in Grouper. This allows you to sort a list of members and search a list of members without having to go to the subject source to query attributes for each subject in the list that you would then use for the sort or search operation. |
| --- | --- |
| [ldappcng caching (performance)](https://bugs.internet2.edu/jira/browse/GRP-503) | The SPMLDataConnector supports caching similar to other Shibboleth DataConnectors |
| [Notification improvements](https://bugs.internet2.edu/jira/browse/GRP-456) | Additional notifications are available now for permissions and the attribute framework. |

 Many other fixes and improvements were also made to all components of the Grouper Toolkit: Grouper API, Administrative & Lite UIs, Grouper Web Services, Grouper Client, Grouper Shell, Grouper Loader, Ldappc, Ldappc-ng, and the Subject API.
