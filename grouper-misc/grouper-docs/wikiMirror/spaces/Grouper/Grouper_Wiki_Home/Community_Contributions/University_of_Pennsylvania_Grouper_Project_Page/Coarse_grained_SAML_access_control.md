---
title: "Coarse grained SAML access control"
space: Grouper
pageId: 28544592
version: 6
lastUpdated: 2026-07-01T05:48:21.996Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544592/Coarse+grained+SAML+access+control
---

We don't want traffic going to apps that is not authenticated and authorized.

- We can restrict SaaS traffic from IdP based on Grouper group
- We restrict traffic from an Apache reverse proxy to not send traffic that is not authenticated and authorized.
- This can help with deprovisioning

Example is PeopleSoft alumni and giving application

It used to run with weblogic accepting connections. Now we have an Apache reverse proxy.

But who is allowed? They get provisioned inside the application. Get a feed.

Restrict traffic from the apache SP with this Apache config:

```
Require shib-attr entitlement urn:mace:upenn.edu:penn:isc:ait:apps:peoplesoft:service:policy:psProdServer
```
