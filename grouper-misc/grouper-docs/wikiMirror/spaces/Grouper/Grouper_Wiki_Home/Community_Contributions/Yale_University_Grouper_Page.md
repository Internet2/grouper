---
title: "Yale University Grouper Page"
space: Grouper
pageId: 28543506
version: 5
lastUpdated: 2026-07-01T05:49:40.951Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543506/Yale+University+Grouper+Page
---

Yale University went live with Grouper in the fall of 2016 to support the launch of Canvas@Yale (LMS). The implementation was patterned after the flow and group structures used at Brown. Grouper receives nightly feeds from Banner which populate non-modifiable reference groups in grouper. Every group fed to Canvas has an associated manually maintained group to record exception inclusions as well as an associated manually maintained group to record exception exclusions. The back-end database was implemented in Oracle for performance with 250,000 groups. All maintenance of groups is performed using the out-of-the-box grouper utilities. This currently requires that administrators use the older (less friendly) admin interface for functions not yet provided in the new interface.

In the summer of 2017, Yale went live with Workday ERP. Coincident with that, we implemented reference groups based on feeds from the Workday system. Those groups are in turn used to effect access control to information in our newly implemented People Hub and to control access to our ExpressShip application. All maintenance continues to be accomplished using the standard application.

At the start of 2018, we are poised to expand Grouper functionality to accept feeds from our IAM system. With the combination of IAM, Workday and Banner feeds into Grouper, we are ready to start the transition away from direct maintenance of Active Directory (AD) groups. We believe that over time, this will allow Yale to move from slow, burdensome, opaque and error-prone AD group maintenance toward easier, more timely, more transparent, and more accurate group maintenance. We believe two of our first use cases are likely to address VPN access and Tableau reporting access control – both of which reference AD.

Going forward, we envision a continued two-pronged approach to adding value through continued one-by-one bottom-up application of the technology to identified needs, as well as embarking on increasing top-down awareness and engagement around group management as a potential enabler for improved communication, access control, transparency and efficiency more broadly.

**See Also**

[Group Management info from Yale IT website](https://yale.service-now.com/it?id=service_offering&sys_id=e6c08841db16ba402de17ecfbf9619de)
