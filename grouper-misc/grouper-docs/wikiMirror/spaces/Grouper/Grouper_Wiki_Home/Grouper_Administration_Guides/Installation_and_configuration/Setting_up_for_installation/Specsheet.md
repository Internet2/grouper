---
title: "Specsheet"
space: Grouper
pageId: 28549107
version: 44
lastUpdated: 2026-07-01T05:42:49.236Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549107/Specsheet
---

## Grouper product specs and requirements

> These requirements apply to container-based Grouper (`v4+`). The default database connection pool size of 500 per container is unchanged from `v4` through `v7` (`grouper.hibernate.base.properties`, `hibernate.c3p0.max_size`).

| Component | Details |
| --- | --- |
| **RDBMS** | \| Database \| Preferred? \| Large deployments* \| Small deployments \| Notes \| \| --- \| --- \| --- \| --- \| --- \| \| Postgres \| Yes \| Yes \| Yes \| note the default database server max connections is 100 and needs to be at least the amount of connections for each container multiplied by the number of containers.  each container has a max pool size default of 500 (so if you have 3 containers in an env then the max database number of connections should be at least 1500) \| \| Oracle \| No \| Yes \| Yes \| you need to add your driver to the container \| \| MySQL \| No \| No \| Yes \| performance issues on large deployments \| \| Microsoft MSSQL \| No \| No \| No \|  \|  "Large deployment" is loosely defined as having more than 10k groups  Note the database should not limit the number of connection below whatever is necessary for your own performance needs (e.g. at least 500) | Database | Preferred? | Large deployments* | Small deployments | Notes | Postgres | Yes | Yes | Yes | note the default database server max connections is 100 and needs to be at least the amount of connections for each container multiplied by the number of containers.  each container has a max pool size default of 500 (so if you have 3 containers in an env then the max database number of connections should be at least 1500) | Oracle | No | Yes | Yes | you need to add your driver to the container | MySQL | No | No | Yes | performance issues on large deployments | Microsoft MSSQL | No | No | No |  |
| Database | Preferred? | Large deployments* | Small deployments | Notes |
| Postgres | Yes | Yes | Yes | note the default database server max connections is 100 and needs to be at least the amount of connections for each container multiplied by the number of containers.  each container has a max pool size default of 500 (so if you have 3 containers in an env then the max database number of connections should be at least 1500) |
| Oracle | No | Yes | Yes | you need to add your driver to the container |
| MySQL | No | No | Yes | performance issues on large deployments |
| Microsoft MSSQL | No | No | No |  |
| **Container** | [Grouper is required to run in a container](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544489/Grouper+Packaging+and+Versioning). This could be anything that runs containers e.g. [Docker](https://www.docker.com/resources/what-container), [Amazon ECS](https://aws.amazon.com/ecs/), etc |
| **Authentication** | User Interface - Servlet container, REMOTE_USER via servlet container, or user-installable servlet filter. See [Authentication to the Grouper UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548676/Authentication+to+the+Grouper+UI).  Web Services - Servlet container, HTTP Basic, HTTP Basic with Kerberos, Rampart, or user-installable plug-in.  Grouper also has [built in authentication for both the UI and Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549360/Grouper+Built-in+Basic+Authentication+to+UI+and+Web+Services). |
| **Java** | - Version no longer applicable since Grouper runs in a container with Java already installed (v17 Corretto). To run the Grouper client (for web services), Java8 is required. |
| **Browser Requirements** | - Modern common browser (e.g. latest Chrome, Firefox, Edge) - cookies must be enabled - javascript must be enabled - AJAX compatible |
| **Memory** | Memory requirements are depending on institution and usage. The below information may help you determine how much memory will be required for you.    - Standard recommendations for base level:      - Daemon Container: 16GB (for large deployments use 32GB or 64GB)          - Dependent on:              - number jobs       - if they overlap during run time ( "active" )       - how performant your DB is for those jobs needs,       - how performant your Subject source is       - how many total Subjects are in your system ( "active" )       - how performant your inbound loader and outbound provisioners are ( "active" )   - WS Container: 4GB          - Dependent on:              - number of concurrent WS users ( "active" )       - AND the type of activities they do via WS ( "active" )   - UI Container: 6GB          - Dependent on:              - number of concurrent UI users ( "active" )       - and ( to a lesser degree ) the type of activities they do in the UI                  - file uploads, reporting, visualization, and "other long running operations" may require "more resources" |
