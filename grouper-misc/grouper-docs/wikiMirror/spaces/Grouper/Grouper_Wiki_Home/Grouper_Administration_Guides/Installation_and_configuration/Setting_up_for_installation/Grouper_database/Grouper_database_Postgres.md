---
title: "Grouper database - Postgres"
space: Grouper
pageId: 28555261
version: 5
lastUpdated: 2026-07-01T05:38:39.816Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555261/Grouper+database+-+Postgres
---

Postgresql is the recommended database for Grouper. It is free, it performs well, many institutions use it, and there are managed options in IAAS (e.g. AWS Aurora).

If you want to quickly get postgres up and running in a container, [here are some steps](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555530/Install+docker+postgres+database)

## Configuration tips

1. If you migrate to postgres or if you upgrade postgres major versions, you need to vacuum analyze all tables
2. If you are not in the "public" schema, you can set your schema name in grouper.properties: ddlutils.schema
