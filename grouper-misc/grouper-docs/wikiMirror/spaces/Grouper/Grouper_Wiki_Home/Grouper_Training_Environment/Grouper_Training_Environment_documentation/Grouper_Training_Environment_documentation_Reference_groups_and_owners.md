---
title: "Grouper Training Environment documentation - Reference groups and owners"
space: Grouper
pageId: 330203139
version: 1
lastUpdated: 2026-10-23T19:42:29.824Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/330203139/Grouper+Training+Environment+documentation+-+Reference+groups+and+owners
---

> Simulated institutional documentation for the Grouper Training Environment (GTE). The Institute of Higher Education (IHE) is a fictional university. Owners, ticket queues, and turnaround times on this page are made up for the training.

Reference groups are the populations that access policies are built from. Use these instead of building your own population from basis groups.

| Group | Who is in it | Data owner | How to request a change |
| --- | --- | --- | --- |
| `ref:student:students` | Active students in the class years 2027 to 2030, plus students who completed in 2026 (until 12/31/2026) | Registrar | Ticket to the **Registrar-Systems** queue |
| `ref:role:emp:faculty` | All faculty, from HR | Human Resources | Fix the HR record; Grouper follows within a day |
| `ref:role:emp:staff` | All staff, from HR | Human Resources | Fix the HR record |
| `ref:role:emp:work_study` | Students with a work study job, from HR | Human Resources | Fix the HR record |
| `ref:role:emp:affiliate` | Affiliates, from HR | Human Resources | Fix the HR record |
| `ref:role:all_facstaff` | All faculty and staff | Human Resources | Fix the HR record |
| `ref:iam:active` | People with an active account | IAM | Ticket to **IAM-Grouper** |
| `ref:iam:closure` | People in an emergency closure state | IAM | Call the IAM on-call line; do not use a ticket |
| `ref:security:locked_by_ciso` | Accounts locked for security reasons, e.g. a compromised account or overdue security training | Security (the CISO's office) | Only Security staff can add or remove members |

## The global deny group

`ref:iam:global_deny` contains `ref:iam:closure` and `ref:security:locked_by_ciso`. **Every access policy at IHE must exclude `ref:iam:global_deny`.** Someone in either group loses access to every application.

## Rules for reference groups

- Do not provision a reference group directly to an application. Build an access policy in the application's `app` folder and provision that
- Do not add people directly to a reference group that is loaded from HR or SIS. Fix the source system instead
- Security department staff (`basis:hr:employee:dept:10902:staff`) can read and update `ref:security:locked_by_ciso`. Nobody else can

## Who to contact

- HR data: Human Resources, department 10830
- Student data: Registrar
- Account and Grouper questions: IAM, department 10904, queue **IAM-Grouper**
- Security locks: Security, department 10902, queue **Security-Incident**
