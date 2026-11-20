---
title: "Grouper Training Environment documentation - LDAP directory guide"
space: Grouper
pageId: 330399748
version: 1
lastUpdated: 2026-10-23T19:42:31.048Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/330399748/Grouper+Training+Environment+documentation+-+LDAP+directory+guide
---

> Simulated institutional documentation for the Grouper Training Environment (GTE). The Institute of Higher Education (IHE) is a fictional university. Owners, ticket queues, and turnaround times on this page are made up for the training.

IHE runs one LDAP directory. In Grouper it is the external system `demo` (`ldap.demo.*` in `grouper-loader.properties`).

## Layout

- Base DN: `dc=internet2,dc=edu`
- People: `ou=people,dc=internet2,dc=edu`, one entry per person, named by netid, e.g. `uid=banderson,ou=people,dc=internet2,dc=edu`
- Groups: `ou=groups,dc=internet2,dc=edu`, as `groupOfNames` entries with `member` values

## Person attributes

| Attribute | Meaning |
| --- | --- |
| `uid` | Netid, same as the Grouper subject identifier |
| `cn`, `givenName`, `sn` | Name |
| `mail` | Email address |
| `employeeNumber` | IHE ID, same as the Grouper subject ID |
| `memberOf` | DNs of the LDAP groups the person is in, kept up to date by the directory |
| `eduPersonAffiliation` | faculty, staff, student, etc., provisioned from Grouper |
| `eduPersonEntitlement` | Entitlements provisioned from Grouper, e.g. for the wiki |

## Groups in LDAP

- `cn=vpn_users,ou=groups,dc=internet2,dc=edu` controls who can use the VPN. It should be provisioned from the VPN access policy in Grouper; until then it is maintained by hand by ITS (department 10900)
- New LDAP groups come from Grouper access policies only. Do not create groups in LDAP by hand

## Provisioning from Grouper

| Provisioner | What it writes |
| --- | --- |
| `eduPersonAffiliation` | `eduPersonAffiliation` on people |
| `eduPersonEntitlement` | `eduPersonEntitlement` on people |
| `groupOfNames` | Groups in `ou=groups` from access policies marked provisionable |

## Getting access

- Applications that read LDAP get a service account from IAM. Ticket to **IAM-Grouper**, with the application name and the attributes it needs
- Searching LDAP over Grouper MCP needs admin read-only access, or membership in `etc:mcp:mcpLdapDemoUsers` together with `etc:mcp:mcpUsersAdminReadonlyLimited`
