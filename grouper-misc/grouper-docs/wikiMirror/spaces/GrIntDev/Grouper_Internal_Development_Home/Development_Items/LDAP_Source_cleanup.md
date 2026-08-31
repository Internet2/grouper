---
title: "LDAP Source cleanup"
space: GrIntDev
pageId: 48824674
version: 4
lastUpdated: 2026-07-12T07:02:41.778Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48824674/LDAP+Source+cleanup
---

There are a few improvements we want to make to the distributed LDAP source adapter: ( for version ??? )

 

### Remove the old JNDI adapter

 We presently distribute two source adapters:

 

- JNDISourceAdapter, which uses native Java ldap libraries, and
- LdapSourceAdapter, which uses the ldap library from Virginia Tech.

 The latter has more capabilitie: it provides persistent connections to the LDAP service; it provides a more convenient client certificate configuration. We want to remove the original adapter and distribute only the VT one.

 To do that the LdapSourceAdapter has to be able to process the configuration syntax of the original. The only incompatibility is the definition of the authentication parameters. LdapSourceAdapter normally reads this configuration from a properties file. It will be extended to process them from the `sources.xml` directly---as does JNDISourceAdapter.

 

### Package requests for multiple ids into a single LDAP query.

 tba
