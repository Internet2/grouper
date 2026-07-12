---
title: "Penn Grouper deployment"
space: Grouper
pageId: 28544106
version: 2
lastUpdated: 2018-09-10T13:17:14.075Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544106/Penn+Grouper+deployment
---

We use:

- WS
- Daemon
- UI
- PSPNG
- Custom ldap provisioner

Authorizations are provisioned with

- SAML
- LDAP
- WS
- SQL

We have:

| Count | Object | Query |
| --- | --- | --- |
| 230k | Groups | select count(1) from grouper_groups |
| 23k | Folders | select count(1) from grouper_stems |
| 12m | Memberships | select count(1) from grouper_memberships_all_v gmav |

Servers:

Note, our servers are shared with 20 java applications, Grouper UI, WS, and Daemon being three of those 20.

| Server type | Server count | Memory allocated |
| --- | --- | --- |
| UI | 5 | 1.5g |
| WS | 5 | 750m |
| Daemon | 5 | 2.8g |

Instrumentation: /grouper/grouperUi/app/UiV2Main.index?operation=UiV2Admin.instrumentation

| Operation | Count |
| --- | --- |
| Membership changes / day | 20k |
| UI servlet requests / day | 500 |
