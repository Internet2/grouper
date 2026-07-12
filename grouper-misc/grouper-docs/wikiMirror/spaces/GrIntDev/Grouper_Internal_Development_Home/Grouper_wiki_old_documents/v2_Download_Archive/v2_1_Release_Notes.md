---
title: "v2.1 Release Notes"
space: GrIntDev
pageId: 48793451
version: 22
lastUpdated: 2026-07-12T17:03:07.314Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793451/v2.1+Release+Notes
---

# Release Notes for Grouper v2.1

 Grouper v2.1.5 includes 11 fixes and improvements over v2.1.4. See the [full list in Jira](https://bugs.internet2.edu/jira/browse/GRP-911?jql=project%20%3D%20GRP%20AND%20fixVersion%20%3D%20%222.1.5%22%20AND%20status%20%3D%20Resolved%20ORDER%20BY%20priority%20DESC).

 Grouper v2.1.4 includes 19 fixes and improvements over v2.1.3. See the [full list in Jira](https://bugs.internet2.edu/jira/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+GRP+AND+fixVersion+%3D+%222.1.4%22+AND+status+%3D+Resolved+ORDER+BY+priority+DESC&mode=hide).

 Grouper v2.1.3 includes 20 fixes and improvements over v2.1.2. See the [full list in Jira](https://bugs.internet2.edu/jira/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+GRP+AND+fixVersion+%3D+%222.1.3%22+AND+status+%3D+Resolved+ORDER+BY+priority+DESC).

 Grouper v2.1.2 includes 10 fixes and improvements over v2.1.1. See the [full list in Jira](https://bugs.internet2.edu/jira/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+GRP+AND+fixVersion+%3D+%222.1.2%22+AND+status+%3D+Resolved+ORDER+BY+priority+DESC).

 Grouper v2.1.1 includes 31 fixes and improvements over v2.1.0. See the [full list in Jira](https://bugs.internet2.edu/jira/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+GRP+AND+fixVersion+%3D+10821+AND+status+in+%28Resolved%2C+Closed%29+ORDER+BY+priority+DESC).

 Grouper v2.1.0 includes 55 fixes and improvements over v2.0.3. See the [full list in Jira](https://bugs.internet2.edu/jira/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+GRP+AND+fixVersion+%3D+10520+AND+status+in+%28Resolved%2C+Closed%29+ORDER+BY+priority+DESC).

 

## New Features

 

| PSP | Changes to user access can now happen in real time courtesy of real time and incremental provisioning based on Grouper's changelog. Group and folder moves and renames can be provisioned as well. This is accomplished with the new Provisioning Service Provider (PSP), which replaces LDAPPCNG. |
| --- | --- |
| [Provision from LDAP](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548254/Grouper+-+Loader+LDAP) | Grouper can now be updated from LDAP via PSP or Grouper Loader. |
| More Web Services | Improved web services support for applications that outsource their     internal access management to Grouper: Operations [1](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548441/Assign+Attribute+Definition+Name+Inheritance),[2](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548312/Attribute+Definition+Name+Save),[3](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547901/Attribute+Definition+Name+Delete),[4](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548599/Find+Attribute+Definition+Names) |
| [Local Entities](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549387/Grouper+local+entities) | Improved management of access by service principals to info stored in Grouper using a new "local entity" object type. |
| Grouper Installer | It's quick and easy to get started using Grouper with the new     installer. Really! |
| [Subject Filter and Attribute Decorator](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548921/Grouper+subject+filter+and+attribute+decorator) | Manage who can see which subject attributes with new subject attribute security support. You can also decorate subjects after retrieval from their source. |
| Grouper Failover Client | Higher availability of web services using a new failover client library and discovery service library. |

 For more information about upcoming plans, see the [Grouper+Product+Roadmap](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541781/Grouper+Product+Roadmap).

 Many other fixes and improvements were also made to all components of the Grouper Toolkit: Grouper API, Administrative & Lite UIs, Grouper Web Services, Grouper Client, Grouper Shell, Grouper Loader, PSP, and the Subject API.

  

### See Also

 [Grouper Release Announcements](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545317/Grouper+Release+Announcements)
