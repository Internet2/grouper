---
title: "Grouper Training Environment documentation - Grouper folder structure and naming"
space: Grouper
pageId: 330268675
version: 1
lastUpdated: 2026-10-23T19:42:29.030Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/330268675/Grouper+Training+Environment+documentation+-+Grouper+folder+structure+and+naming
---

> Simulated institutional documentation for the Grouper Training Environment (GTE). The Institute of Higher Education (IHE) is a fictional university. Owners, ticket queues, and turnaround times on this page are made up for the training.

IHE follows the Grouper Deployment Guide. Every group lives in one of these top-level folders:

| Folder | What goes in it | Who maintains it |
| --- | --- | --- |
| `basis` | Groups loaded from source systems: HR (`basis:hr`), Student Information System (`basis:sis`), and ad hoc imports (`basis:adhoc`) | Loader jobs; IAM fixes loader problems |
| `ref` | Reference groups (populations) used by access policies, e.g. all students, all staff | The data owner named on the group's `ref` type |
| `app` | One folder per application, holding its access policies | The application's admins |
| `org` | Groups managed by a school or department for its own use | The department |
| `test` | Temporary groups for testing; may be deleted at any time | Whoever created them |
| `etc` | Grouper system groups, including `etc:mcp` and `etc:sysadmingroup` | IAM only |

## HR basis groups

- `basis:hr:employee:dept:<department number>:<role>`, e.g. `basis:hr:employee:dept:10830:staff` is staff in Human Resources
- The department folder's extension is the department number. Its display extension is the department name
- Roles are `faculty`, `staff`, `work_study`, and `affiliate`
- Useful department numbers: 10830 Human Resources, 10900 Information Technology Services, 10902 Security, 10904 Identity and Access Management (IAM)

## SIS basis groups

- `basis:sis:prog_status:year:<status>:<year>`, e.g. `ac:2027` is active students expected to graduate in 2027
- Status codes: `ac` active, `cm` completed, `la` leave of absence, `pm` pre-matriculated, `rs` resigned, `xo` transferred out, `xp` expelled, `es` exchange student
- `basis:sis:course` has student and instructor groups for current courses, by department then course number

## Naming rules

- Folder and group extensions are lower case, with underscores between words, e.g. `all_facstaff`
- Put the friendly name in the display extension, e.g. `ref:role:emp` is displayed as "Employee by Role"
- A person's netid is their subject identifier, e.g. `banderson`
- Every group in `ref` and `app` must have a description that says who is in it and why
- Every group must have a type: `basis`, `ref`, `policy`, `intermediate`, `app`, `org`, or `test`

## Getting a new folder

Open a ticket in the **IAM-Grouper** queue with the folder name, the owner, and what it is for. IAM creates folders within 2 business days. Do not create top-level folders yourself.
