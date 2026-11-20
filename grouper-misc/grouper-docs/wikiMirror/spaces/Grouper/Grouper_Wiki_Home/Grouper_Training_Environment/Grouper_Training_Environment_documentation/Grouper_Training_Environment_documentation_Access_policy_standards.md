---
title: "Grouper Training Environment documentation - Access policy standards"
space: Grouper
pageId: 330727426
version: 1
lastUpdated: 2026-10-23T19:42:30.428Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/330727426/Grouper+Training+Environment+documentation+-+Access+policy+standards
---

> Simulated institutional documentation for the Grouper Training Environment (GTE). The Institute of Higher Education (IHE) is a fictional university. Owners, ticket queues, and turnaround times on this page are made up for the training.

Every application that uses Grouper for access control gets its own folder under `app`, created from the IHE application template.

## Application folder layout

For an application named `vpn`:

| Path | What it is |
| --- | --- |
| `app:vpn:security` | Groups that hold Grouper privileges on the other folders: `admins`, `readers`, `updaters` |
| `app:vpn:service:ref` | Reference groups used only by this application |
| `app:vpn:service:policy` | The access policy and the groups that feed it |

## Building an access policy

An access policy is the group that is provisioned to the application. At IHE every policy is built the same way:

1. `<app>_allow`: who should have access, made from reference groups and an `_allow_manual` group for exceptions
2. `<app>_deny`: who should not, which always includes `ref:iam:global_deny`
3. `<app>` (the policy): `_allow` minus `_deny`, as a composite

- Give the policy group the `policy` type, and the `_allow` and `_deny` groups the `intermediate` type
- Give the policy a description that says who has access and why, in plain language
- Only the policy group is provisioned. Never provision `_allow`, `_deny`, or a reference group

## Manual exceptions

- Add exceptions to `<app>_allow_manual` with an end date. IHE allows at most 1 year for a manual exception
- The application owner must attest `<app>_allow_manual` every 90 days

## Requesting a new application folder

Open a ticket in the **IAM-Grouper** queue with:

- The application name and owner
- Who should have access, in plain language (e.g. "all staff in Human Resources")
- The target system to provision to (e.g. LDAP group, Entra ID)

IAM creates the folder from the template within 3 business days and makes the requester an admin.
