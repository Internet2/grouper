---
title: "Provisioning and Integration"
space: Grouper
pageId: 28543540
version: 6
lastUpdated: 2026-07-12T15:26:07.658Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543540/Provisioning+and+Integration
---

Integrating Grouper with an application may involve [Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services), [Grouper Client](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545215/Grouper+Client), LDAP, SAML, XMPP notification or other tools. You design the integration to suit the requirements of your site.

Important decisions when designing the integration include:

- Should authorization to access the application be groups-based or permissions-based?
- Use LDAP or [Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services) or SAML entitlements?
- Use cached data versus live calls?
- Use the Grouper API or a local representation?

Below are some of the methods of connecting with Grouper and examples of how to use them:
