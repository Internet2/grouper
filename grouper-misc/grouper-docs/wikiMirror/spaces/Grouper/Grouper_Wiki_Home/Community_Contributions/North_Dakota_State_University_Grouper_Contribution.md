---
title: "North Dakota State University Grouper Contribution"
space: Grouper
pageId: 28543431
version: 7
lastUpdated: 2026-07-01T05:49:48.181Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543431/North+Dakota+State+University+Grouper+Contribution
---

North Dakota State University migrated from a custom IAM solution to one using Grouper and MidPoint in 2020. Usage has since grown considerably, and Grouper is now used to manage ~21,000 groups and ~6.2 million memberships. Working alongside MidPoint, Grouper is the sole user-presenting interface for NDSU's community of group managers across campus, which makes support easier.

As the legacy system used RabbitMQ, the initial Grouper deployment also relied heavily on RabbitMQ messaging. Subsequent work has focused on leveraging the Grouper provisioning framework.

A number of Grouper group types exist for sharing information with other systems. To create these groups according to their types, a custom web application uses Grouper Web Services to trigger a GSH template.

**Use Cases**

*RabbitMQ*

- Active Directory Groups: a Groovy daemon handles RabbitMQ messages from Grouper related to AD group memberships. The custom group-creation web app allows AD administrators to select from a list of groups in AD that are not yet in Grouper, to easily create a corresponding group.
- eduPersonEntitlement: applied to Grouper groups as attributes, entitlements provide access to several custom web applications. Entitlement attributes are flung out via RabbitMQ and added to an AD user via a Powershell script.

*Provisioning*

- Entra ID: Grouper automates membership in a number of Entra ID groups. The main use case is managing MS Teams team memberships. For this, the provisioner populates security groups from Grouper, while the MS Teams group uses Entra dynamic membership rules to populate the team based on its associated security group membership.
- Google: in a pattern similar to Entra ID, memberships are provisioned to Google groups for various cohorts, and these groups are used to manage access in Google, such as populating Google Drive shared folders.
- LDAP: NDSU's Center for Computationally Assisted Science and Technology (CCAST) high-performance computing cluster uses a LDAP provisioner to manage research group memberships in Active Directory.
- Drupal Content Management System (CMS): a custom Drupal provisioner has been developed at NDSU which runs inside the Grouper containers. This connects to a bespoke Drupal API to manage membership in various CMS website groups.
- Freshservice Requester: a custom Freshservice Requester provisioner was developed to manage requester group memberships within NDSU’s Freshervice ticketing system. While this is still running as the original separate .jar, work has been done by NDSU and the Grouper development team to include this provisioner in the Grouper source code, and NDSU will soon transition to the provisioner that is now included in Grouper.
- MidPoint: the MidPoint provisioner manages group memberships for a number of MidPoint orgs, including Duo, Adobe, Zoom, Qualtrics, and the base population of Freshservice agents and requesters. Those orgs are used to create accounts (using a different bridge into the old system). For these applications, Grouper makes the policy decisions, while MidPoint executes those decisions.
- Avigilon Unity Access: a SQL database provisioner is under way to manage group memberships in Avigilon for keycard access to campus buildings. On the Avigilon side, a “Collaboration” process runs which syncs memberships in various access groups based on the SQL Server database Grouper populates.

*Miscellaneous*

- LISTSERV: Grouper automatically populates a number of LISTSERV lists each morning. Groovy code crawls that app stem looking for groups with the listserv marker, and populates the corresponding LISTSERV list with members.
