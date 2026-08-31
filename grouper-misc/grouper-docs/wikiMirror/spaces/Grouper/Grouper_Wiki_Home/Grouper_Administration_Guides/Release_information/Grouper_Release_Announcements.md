---
title: "Grouper Release Announcements"
space: Grouper
pageId: 28545317
version: 162
lastUpdated: 2026-07-12T15:26:42.538Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545317/Grouper+Release+Announcements
---

> As of Grouper v2.5+ it is required to use the InCommon Trusted Access Platform packaging approach.

 *Learn how to report Grouper security concerns* [here](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541872/Contact+Information)

  

## View the Grouper demo

 [View the Grouper demo](https://grouperdemo.internet2.edu/) (if you have [issues registering for the demo, click here for info](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541850/Grouper+Demo))

 

## Grouper releases

 

### Grouper v9 is the database rewrite version of Grouper

 Read the release notes for Grouper v9 (not yet available).

 

### Grouper v7 is the enhancement version of v6

 [Read the release notes for Grouper v7](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549113/v7+Release+Notes)

 

### Grouper v6 is the latest supported version

 [Read the release notes for Grouper v6](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547614/v6+Release+Notes)

 

### Grouper v4 is the legacy supported version

 [Read the release notes for Grouper v4](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)

 

## Which version should be used

 

| Use version | In these conditions |
| --- | --- |
| v9.x.x | Do not use this version yet, we are rewriting the database |
| v7.x.x | You want new Grouper features as they are released |
| You have a non-trivial testing process on upgrades |
| Upgrade to latest stable v7.x.x at least every quarter |
| Upgrade to latest stable v7.x.x when security advisories are released |
| v6.x.x  (was v5) | Latest stable Grouper version |
| You do not want to test thoroughly when upgrading |
| Upgrade to latest stable v6.x.x at least every quarter |
| Upgrade to latest stable v6.x.x when security advisories are released |
| v4.x.x  (was v2.6) | You want as little change/risk as possible when upgrading containers (for OS / security / critical bug fixes) |
| You do not want to test thoroughly when upgrading |
| Upgrade to latest stable v4.x.x at least every quarter |
| Upgrade to latest stable v4.x.x when security advisories are released |
| Using the Grouper Provisioning Framework |
| Upgrade to v6 by Feb 2027 |
| v2.5.x | Do not use |
| v2.4.x | Do not use |
| v2.3.x | Do not use |
| v2.2.x | Do not use |
| v2.1 or earlier | Seriously? |

 

## Release components

 Grouper in v4 is a container. If your institution does not run containers regularly, it's ok, you can run Grouper without making investments in container orchestration.

 For the Grouper container, which runs the Grouper UI, WS, daemon, SCIM, and GSH, see:

 

- [v4 release notes to see which version to use](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes)
- [Install instructions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554540/Install+the+Grouper+container+with+maturity+level+0)
- [Container maturity level -1 (quick start)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555747/Install+the+Grouper+container+maturity+level+-1+quick+start+v2.6.4+and+prior+quickstart)
- [Container maturity level 0 instructions (manual install)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554540/Install+the+Grouper+container+with+maturity+level+0)
- [v4 upgrade instructions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549792/v4+Upgrade+instructions+from+v4)
- The Grouper client is a Java library for Grouper web services which can be used on the command line and can show example web service calls: 
  
  - [Download the version you want from Maven](https://repo1.maven.org/maven2/edu/internet2/middleware/grouper/grouperClient/) (download the grouperClient-*.jar).
  - Get a grouper.client.base.properties, e.g. for v2.5.x: [grouper.client.base.properties](https://raw.githubusercontent.com/Internet2/grouper/GROUPER_RELEASE_2.5.XX/grouper-misc/grouperClient/conf/grouper.client.base.properties)
  - Create a blank grouper.client.properties and use any settings from the base properties.
  - Run: `java -jar grouperClient-2.5.X.jar`

 

## Grouper source code

 The [Grouper source code](https://github.com/Internet2/grouper) is on GitHub.

 

## Previous Grouper versions

 For previous Grouper versions, click the link for the desired version:

 v2.4.0: [2.4.0](https://software.internet2.edu/grouper/release/2.4.0)  
 v2.3.0: [2.3.0](https://software.internet2.edu/grouper/release/2.3.0)  
 v2.2.2: [2.2.2](https://software.internet2.edu/grouper/release/2.2.2)  
 v2.2.1: [2.2.1](https://software.internet2.edu/grouper/release/2.2.1)  
 v2.2.0: [2.2.0](https://software.internet2.edu/grouper/release/2.2.0)  
 v2.1: [2.1.5](https://software.internet2.edu/grouper/release/2.1.5), [2.1.4](https://software.internet2.edu/grouper/release/2.1.4), [2.1.3](https://software.internet2.edu/grouper/release/2.1.3), [2.1.2](https://software.internet2.edu/grouper/release/2.1.2), [2.1.1](https://software.internet2.edu/grouper/release/2.1.1), [2.1.0](https://software.internet2.edu/grouper/release/2.1.0)  
 v2.0: [2.0.3](https://software.internet2.edu/grouper/release/2.0.3), [2.0.2](https://software.internet2.edu/grouper/release/2.0.2), [2.0.1](https://software.internet2.edu/grouper/release/2.0.1), [2.0.0](https://software.internet2.edu/grouper/release/2.0.0)  
 v1.7: [1.7.0](https://software.internet2.edu/grouper/release/1.7.0)  
 v1.6: [1.6.3](https://software.internet2.edu/grouper/release/1.6.3), [1.6.2](https://software.internet2.edu/grouper/release/1.6.2), [1.6.1](https://software.internet2.edu/grouper/release/1.6.1), [1.6.0](https://software.internet2.edu/grouper/release/1.6.0)

 

## License

 Grouper is licensed under the Apache 2.0 license. See [http://www.apache.org/licenses/LICENSE-2.0.html](http://www.apache.org/licenses/LICENSE-2.0.html) for a copy of this license.

 Development of this software was supported with funding from Internet2, the University of Chicago, University of Pennsylvania, Duke University, University of Washington, University of Memphis, University of Bristol (UK), the NSF Middleware Initiative (NSF 02-028, Grant No. OCI-0330626, Grant No. OCI-0330626, OCI-0721896, and OCI-1032468), and JISC. Any opinions, findings and conclusions or recommendations expressed in this material are those of the author(s) and do not necessarily reflect the views of the National Science Foundation (NSF).
