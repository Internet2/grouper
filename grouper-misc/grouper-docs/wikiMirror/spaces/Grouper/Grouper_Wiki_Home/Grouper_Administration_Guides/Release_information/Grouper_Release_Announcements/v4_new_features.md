---
title: "v4 new features"
space: Grouper
pageId: 28547840
version: 16
lastUpdated: 2026-07-12T15:26:48.980Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547840/v4+new+features
---

## New Features in Grouper v4

Grouper v4 includes many helpful new features, as listed below, as well as the enhancements provided in Grouper 2.5 updates.

**** As of March 2023,**[**Grouper v4 is**](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)**the stable no-enhancement version of v2.6. See**[**versioning**](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544481/Versioning+Support+Policy)**page ****

The upgrade from 2.5 (more recent) to v4 is not a substantial change. The database did not change much.

You are required to use a container when running Grouper. This will ensure you have consistent directory structure, the correct version of libraries, and low risk and low effort upgrades. There are [instructions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544489/Grouper+Packaging+and+Versioning) to make using the container as easy as possible.

See also [Grouper 2.6 Release Notes (renamed to v4) (build info)](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793558/v2.6+renamed+to+v4+Release+Notes)

| **Provisioning framework** | [The Grouper provisioning framework is being maintained and finished in v4](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544760/Grouper+provisioning+framework) |
| --- | --- |
| **Provisioning configuration 'start with'** | [Common patterns for provisioning configuration](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560139/Grouper+provisioning+configuration+scaffolding+start+with) |
| **JEXL scripted groups** | [More complex policies than traditional composites](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups) |
| **Edit select attributes on group screen** | [Certain common attributes can be viewed/edited on group screen](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548837/Grouper+attribute+framework+attributes+editable+in+group+edit+screen) |
| **Load data from provisioners** | Add loader for provisioners (not SQL or LDAP) like Duo or Zoom |
| **Add WS authn options** | [Trusted JWT WS, self-service JWT WS, OIDC WS](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549360/Grouper+Built-in+Basic+Authentication+to+UI+and+Web+Services) |
| **Duo role provisioner** | [Admin roles provisioned to Duo](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555002/Grouper+Duo+Administrator+Role+Provisioner) |
| **Box provisioner** | Provisioning to box |
| **SQL provisioner** | [Provisioning to databases](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545506/Grouper+SQL+database+sync) |
| **Azure provisioner** | [Provisioning to azure](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555049/Grouper+Entra+ID+Provisioner+Legacy) |
| **Provisioning diagnstics** | Get feedback on provisioning configuration |
| **Google provisioner** | [Provisioning to google](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554610/Grouper+Google+GCP+provisioner) |
| **Folder security and performance improvements** | Users only see folders they should be able to see, with good performance. Add folder VIEW privilege |
| **OSGI plugins** | Ability to sandbox plugins in their own classloader |
