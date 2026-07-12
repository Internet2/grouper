---
title: "Build new grouper client"
space: GrIntDev
pageId: 48793024
version: 3
lastUpdated: 2026-07-12T06:45:59.931Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793024/Build+new+grouper+client
---

If libraries need to be updated in grouper client

1. Look at the libraries in the ext folder
2. Update the version in the parent pom
3. Get the source and refactor the package with search and replace to be in edu.internet2.middleware.grouperClientExt.
4. Run the grouper client tests
