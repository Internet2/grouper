---
title: "SCIM Call Notes 20130429"
space: GrIntDev
pageId: 48824683
version: 6
lastUpdated: 2026-07-12T06:46:35.150Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48824683/SCIM+Call+Notes+20130429
---

SURFnet looking for Grouper -> SCIM implementation.

 SURFdesires:

 

- Grouper to emit scim (speaks to endpoint which intermediates to commercial provider)
- attribute / group release configurable by endpoint (preferably
- preferably in a shibboleth compatible way
- standard way to provision groups out
- need to support OAuth

 Ultimately they are doing grouper -> apache syncope [http://syncope.apache.org/](http://syncope.apache.org/)

 Would like us to provide a rich SCIM impl & let implementers handle specific issues arising from it. SCIM Impl should handle both PUSH and PULL.

 SCIM ops coming out of Grouper:

 

- add group
- remove group
- add member
- remove member
- add / remove membership role (admin/user)

 want configurable incremental as well as bulk sync.

 if SCIM endpoint is not available then do a periodic retry. AuthN will be by BASIC Auth, IP-based Auth, though nobody would complain if we supported OAuth.

 Would like to configure provisioning such that not all groups go to all SPs. Rather, they would say this group X goes to SP Y and the SCIM provisioner would be smart enough to handle that and know not to put group X in SPs (A,B,C).

 They only want 1 stem pushed out via SCIM (other sems ignored).

 Currently using Grouper version 1.6.3 – SURFnet does NOT need this to work with 1.6.x branch and can live with it working only with current.

 normal group name coming out of grouper-SCIM

 

- though would be good to have an ability to send a custom name re-mapping managed somehow.
