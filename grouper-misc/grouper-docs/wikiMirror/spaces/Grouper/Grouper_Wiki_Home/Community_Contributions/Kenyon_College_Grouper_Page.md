---
title: "Kenyon College Grouper Page"
space: Grouper
pageId: 28543446
version: 11
lastUpdated: 2026-07-01T05:49:46.144Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543446/Kenyon+College+Grouper+Page
---

The first step for our Grouper implementation at Kenyon College is to migrate our legacy mailing lists to Google Groups. We are using Grouper to load list data from our ERP and manage manual mailing list memberships.

We tried using the PSP and were not able to get it configured correctly. Instead, we wrote a custom powershell app to create and populate Grouper groups in Active Directory. We are using Google Apps Directory Sync (GADS) to create and sync memberships to Google Groups.We plan to switch all mailing lists to Google Groups by July 2016 to be ready for the fall semester.

**See also**

[Info from Kenyon College website on using Grouper](https://lbis.kenyon.edu/facilities-technology/help/email/mailinglists/updategroup)

Jared Hoffman. Kenyon College Information Technology Services. hoffmanj@example.com
