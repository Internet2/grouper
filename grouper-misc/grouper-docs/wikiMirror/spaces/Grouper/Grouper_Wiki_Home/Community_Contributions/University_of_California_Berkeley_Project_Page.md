---
title: "University of California Berkeley Project Page"
space: Grouper
pageId: 28543282
version: 10
lastUpdated: 2026-07-01T05:50:12.872Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543282/University+of+California+Berkeley+Project+Page
---

UC Berkeley Deployed Grouper in Summer 2014.

- What usage scenarios are you using Grouper to solve?
  
  - User creates a Special Purpose Account, a delegated account allowing users to login using their own credentials. Grouper is used to store the user groups authorizing use of the SPA.
  - Admin controls access to their app using a combination of adhoc and official data driven groups.
  - User sends email to official data driven or adhoc groups via central message app or google groups.
  - Application determines user affiliations via isMemberOf attribute in LDAP which has been provisioned by Grouper
  - Admin manages AD application security groups via groups provisioned from Grouper.
  - User is authorized to access to Service Providers via official or adhoc groups using IsMemberOf or entitlement attributes
  - Duo second factor is enforced using IsMemberOf group info

- How does Grouper fit into your environment? Do you also run another authorization management app? Did Grouper displace an existing centralized authorization management application?
  
  - Grouper is our only authorization management app.

- What integrations have been integrated with Grouper?
  
  - Grouper provisions groups to AD LDAP, OpenDJ LDAP and Google groups
  - Several apps provision group information via the web services interface which are pushed to the above systems.

- What customizations to Grouper have you applied? Custom UI?
  
  - Our Change Log Consumers are custom.
  - Utilizing Unicon's [https://github.com/Unicon/grouper-provisioning-target-ui](https://github.com/Unicon/grouper-provisioning-target-ui) which allows Grouper users to select which downstream systems to provision their groups.

- What EFT is used to maintain? What’s the EFT’s skillset to run/maintain Grouper? 
  
  - .25 FTE
  - general sysadmin plus programming skills for Change Log Consumers

- How long do you retain your audit/point in time tables? What is your current database size?
  
  - We haven't removed any PIT tables yet.
  - DB size is 24 GB

**See Also**

- [CalGroups Resources](https://calnetweb.berkeley.edu/calnet-departments/calgroups/calgroups-resources)
- [Berkeley Lab Commons Grouper Overview](https://commons.lbl.gov/display/IDMgmt/Grouper+Overview)

For more info, calnet-admin@berkeley.edu
