---
title: "Install AWS database"
space: Grouper
pageId: 28554497
version: 8
lastUpdated: 2026-07-01T05:40:19.778Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554497/Install+AWS+database
---

## Get a database

You need an oracle, mysql, or postgres database with a user/pass where the user can do whatever they want in their schema (e.g. create tables)

Well, for this example, might as well go from scratch in aws, but this could be on prem or wherever. Create a free-tier database in AWS. Mysql RDS, publicly accessible.

RDS → Create database → Standard create → MySQL → Free tier → T2 micro → 20g → publicly accessible

Allow from everywhere (just for poc)

## Create a database, user, and pass with sqlyog (or whatever db client)

Mysql example

```
create database grouper_v2_5 character set UTF8 collate utf8_bin;
create user 'grouper_v2_5'@'localhost' identified by '**********';
grant all on grouper_v2_5.* to 'grouper_v2_5'@'localhost';
flush privileges;
```

Note: maybe your DBAs will do this for you

Create database

Create user

Allow user to access database
