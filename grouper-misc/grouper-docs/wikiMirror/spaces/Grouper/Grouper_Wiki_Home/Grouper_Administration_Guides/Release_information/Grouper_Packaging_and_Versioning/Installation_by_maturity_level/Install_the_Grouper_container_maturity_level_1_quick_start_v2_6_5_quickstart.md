---
title: "Install the Grouper container maturity level -1 quick start v2.6.5+ (quickstart)"
space: Grouper
pageId: 28555721
version: 23
lastUpdated: 2026-07-01T05:37:35.700Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555721/Install+the+Grouper+container+maturity+level+-1+quick+start+v2.6.5+quickstart
---

Note: you can adjust the ports below, the default is to listen on 443 with a self-signed cert. Note: there are different sections for v4 vs v5

## Quick start with docker (not docker-compose)

Install postgres (note, do not use this password 'HZTle3MftmAOLlC9TGc0', generate a random one)

```
docker run --name postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=HZTle3MftmAOLlC9TGc0 -d -p 5432:5432 postgres:14
```

v4: Install grouper. Note, get your IP address and substitute that in the database connect string (w.x.y.z)

```
docker run --name grouper -e GROUPERSYSTEM_QUICKSTART_PASS=HZTle3MftmAOLlC9TGc0 -e GROUPER_MORPHSTRING_ENCRYPT_KEY=abcd1234 \
    -e GROUPER_DATABASE_PASSWORD=HZTle3MftmAOLlC9TGc0 -e GROUPER_DATABASE_USERNAME=postgres \
    -e GROUPER_DATABASE_URL=jdbc:postgresql://w.x.y.z:5432/postgres -e GROUPER_AUTO_DDL_UPTOVERSION='v4.*.*' \
    -d -p 443:443 i2incommon/grouper:4.8.0 quickstart
```

v5.7+: Install grouper. Note, get your IP address and substitute that in the database connect string (w.x.y.z)

```
docker run --name grouper -e GROUPERSYSTEM_QUICKSTART_PASS=HZTle3MftmAOLlC9TGc0 -e GROUPER_MORPHSTRING_ENCRYPT_KEY=abcd1234 \
    -e GROUPER_DATABASE_PASSWORD=HZTle3MftmAOLlC9TGc0 -e GROUPER_DATABASE_USERNAME=postgres \
    -e GROUPER_DATABASE_URL=jdbc:postgresql://w.x.y.z:5432/postgres -e GROUPER_AUTO_DDL_UPTOVERSION='v5.*.*' \
    -d -p 443:8443 i2incommon/grouper:5.7.0 quickstart
```

## Quick start with docker-compose

You need to have the "docker" and "docker-compose" commands available (e.g. in linux, mac, or windows)

Make a directory with a docker compose yaml inside

```
$ mkdir quickstart
$ cd quickstart
$ vi docker-compose.yml

type 'i' for insert, then paste the following
```

v4

```
version: '3'
services:
  postgres:
    image: "postgres:14"
    restart: always
    ports:
      - '5432:5432'
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=HZTle3MftmAOLlC9TGc0
  grouper:
    image: "i2incommon/grouper:4.8.0"
    restart: always
    ports:
      - '443:443'
    command:
      - quickstart
    environment:
      - GROUPERSYSTEM_QUICKSTART_PASS=HZTle3MftmAOLlC9TGc0
      - GROUPER_MORPHSTRING_ENCRYPT_KEY=abcd1234
      - GROUPER_DATABASE_PASSWORD=HZTle3MftmAOLlC9TGc0
      - GROUPER_DATABASE_USERNAME=postgres
      - GROUPER_DATABASE_URL=jdbc:postgresql://postgres:5432/postgres
      - GROUPER_AUTO_DDL_UPTOVERSION=v4.*.*

```

v5.7+

```
version: '3'
services:
  postgres:
    image: "postgres:14"
    restart: always
    ports:
      - '5432:5432'
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=HZTle3MftmAOLlC9TGc0
  grouper:
    image: "i2incommon/grouper:5.7.0"
    restart: always
    ports:
      - '443:8443'
    command:
      - quickstart
    environment:
      - GROUPERSYSTEM_QUICKSTART_PASS=HZTle3MftmAOLlC9TGc0
      - GROUPER_MORPHSTRING_ENCRYPT_KEY=abcd1234
      - GROUPER_DATABASE_PASSWORD=HZTle3MftmAOLlC9TGc0
      - GROUPER_DATABASE_USERNAME=postgres
      - GROUPER_DATABASE_URL=jdbc:postgresql://postgres:5432/postgres
      - GROUPER_AUTO_DDL_UPTOVERSION=v5.*.*

```

Save and exit. If using the vi editor, type:

```
<esc> :wq
```

Start the quickstart

```
$ docker compose up -d
```

Or might be

```
$ docker-compose up -d
```

Login:

[https://localhost](https://localhost)

User: GrouperSystem

Pass: HZTle3MftmAOLlC9TGc0 (your random one instead of this one)

## Run individual docker or podman commands if you dont have docker-compose

Start and init database

```
$ docker run --name postgres -e POSTGRES_PASSWORD=HZTle3MftmAOLlC9TGc0 -d -p 5432:5432 postgres:14
$ docker exec -it -u postgres postgres psql
# CREATE USER grouper PASSWORD 'HZTle3MftmAOLlC9TGc0';
# CREATE DATABASE grouper;
# GRANT ALL PRIVILEGES ON DATABASE grouper TO grouper;
# \q

Get your IP address

$ ifconfig

Database url: jdbc:postgresql://a.b.c.d:5432/grouper (sub in ip address)
```

Run grouper (put in right database url, and the correct mount point for slashRoot on host if applicable)

```
docker run --detach --restart always --name grouper-ui \
  --mount type=bind,src=/opt/grouper/slashRoot,dst=/opt/grouper/slashRoot \
  --publish 443:443 -e GROUPER_MORPHSTRING_ENCRYPT_KEY=abc123 \
  -e GROUPER_DATABASE_URL=jdbc:postgresql://192.168.13.33:5432/postgres \
  -e GROUPER_DATABASE_USERNAME=grouper \
  -e GROUPER_DATABASE_PASSWORD=HZTle3MftmAOLlC9TGc0 \
  -e GROUPER_AUTO_DDL_UPTOVERSION='v2.6.*' \
  -e GROUPER_RUN_SHIB_SP=false \
  -e GROUPER_UI_GROUPER_AUTH=true \
  -e GROUPERSYSTEM_QUICKSTART_PASS=HZTle3MftmAOLlC9TGc0 i2incommon/grouper:2.6.9 ui
```
