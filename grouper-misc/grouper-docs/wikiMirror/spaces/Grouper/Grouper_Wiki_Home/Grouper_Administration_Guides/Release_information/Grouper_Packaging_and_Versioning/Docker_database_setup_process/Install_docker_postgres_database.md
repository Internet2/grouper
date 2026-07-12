---
title: "Install docker postgres database"
space: Grouper
pageId: 28555530
version: 15
lastUpdated: 2026-07-01T05:38:02.399Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555530/Install+docker+postgres+database
---

Use your own random password, not 'mwC4FU2b55qT2uwCqqVs'

```
$ docker run --name postgres -e POSTGRES_PASSWORD=mwC4FU2b55qT2uwCqqVs -d -p 5432:5432 postgres:14
$ docker exec -it -u postgres postgres psql
# CREATE USER grouper PASSWORD 'mwC4FU2b55qT2uwCqqVs';
# CREATE DATABASE grouper;
# GRANT ALL PRIVILEGES ON DATABASE grouper TO grouper;
# (NOTE, DONT DO THIS ON A SHARED DATABASE):  ALTER SCHEMA public OWNER TO grouper;
# GRANT USAGE ON SCHEMA public TO grouper;
# GRANT ALL ON SCHEMA public TO grouper;
# GRANT CREATE ON SCHEMA public TO grouper;
# alter database grouper_v5 owner to grouper_v5; 
# \q

Get your IP address

$ ifconfig

Database url: jdbc:postgresql://a.b.c.d:5432/grouper    (sub in ip address)

```
