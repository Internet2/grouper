---
title: "Grouper container postgres database auto install example"
space: Grouper
pageId: 28560446
version: 16
lastUpdated: 2026-07-01T05:35:33.606Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560446/Grouper+container+postgres+database+auto+install+example
---

Install postgres (use a random password not 'FNNxP4valJeqm0VJ36JC')

```
mchyzer@ISC20-0637-WL:~$ docker run --name postgres -e POSTGRES_PASSWORD=FNNxP4valJeqm0VJ36JC -d -p 5432:5432 postgres:9
mchyzer@ISC20-0637-WL:~$ docker exec -it -u postgres postgres psql
psql (9.6.20)
Type "help" for help.

postgres=# CREATE USER grouper PASSWORD 'FNNxP4valJeqm0VJ36JC';
CREATE ROLE
postgres=# CREATE DATABASE grouper;
CREATE DATABASE
postgres=# GRANT ALL PRIVILEGES ON DATABASE grouper TO grouper;
GRANT

# optional?
postgres=# ALTER DATABASE grouper OWNER TO grouper;

postgres=# ALTER SCHEMA public OWNER TO grouper;
postgres=# GRANT USAGE ON SCHEMA public TO grouper;
postgres=# GRANT CREATE ON SCHEMA public TO grouper;

postgres=# \q
mchyzer@ISC20-0637-WL:~$

```

Install grouper files, note on windows I can use docker.for.win.localhost as my ip address in the DB url. You can also use docker networking or get your ip address and use that

```
mchyzer@ISC20-0637-WL:~/containerScript$ export DIR=`pwd`
mchyzer@ISC20-0637-WL:~/containerScript$ mkdir -p slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes/

# whatever the database connection info is
mchyzer@ISC20-0637-WL:~/containerScript$ echo 'hibernate.connection.url = jdbc:postgresql://docker.for.win.localhost:5432/grouper' >> slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties
mchyzer@ISC20-0637-WL:~/containerScript$ echo 'hibernate.connection.username = grouper' >> slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties
mchyzer@ISC20-0637-WL:~/containerScript$ echo 'hibernate.connection.password = FNNxP4valJeqm0VJ36JC' >> slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties

# adapt this secret to something random
mchyzer@ISC20-0637-WL:~/containerScript$ echo 'encrypt.key = dfjkb345poiuqSFD' >> slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes/morphString.properties

mchyzer@ISC20-0637-WL:~/containerScript$ mkdir -p logs/grouper-ui-logs
mchyzer@ISC20-0637-WL:~/containerScript$ chmod o+rwx logs/grouper-ui-logs

# create a script that can assign a UI password
mchyzer@ISC20-0637-WL:~/containerScript$ echo 'new GrouperPasswordSave().assignApplication(GrouperPassword.Application.UI).assignUsername("GrouperSystem").assignPassword("FNNxP4valJeqm0VJ36JC").save();' > slashRoot/opt/grouper/grouperWebapp/WEB-INF/bin/assignGrouperSystemPassword.gsh

# temporary container to init db
mchyzer@ISC20-0637-WL:~/containerScript$ docker run --detach --mount type=bind,source=$DIR/logs/grouper-ui-logs,target=/opt/grouper/logs \
  --mount type=bind,source=$DIR/slashRoot,target=/opt/grouper/slashRoot \
  -e GROUPER_AUTO_DDL_UPTOVERSION='v2.5.*' \
  -e GROUPER_LOG_TO_HOST=true \
  --name grouper-init-db i2incommon/grouper:2.5.39 

# init the db and assign a password
mchyzer@ISC20-0637-WL:~/containerScript$ docker exec -u tomcat -it grouper-init-db bash -c 'cd /opt/grouper/grouperWebapp/WEB-INF/bin/; ./gsh.sh assignGrouperSystemPassword.gsh'mchyzer@ISC20-0637-WL:~/containerScript$ docker rm -f grouper-init-db
```

Make a run script

```
mchyzer@ISC20-0637-WL:~/containerScript$ vi dockerUiRun.sh
#!/bin/bash

DIR=/home/mchyzer/containerScript

docker run --detach --restart always \
  --publish 443:443 \
  --mount type=bind,source=$DIR/logs/grouper-ui-logs,target=/opt/grouper/logs \
  --mount type=bind,source=$DIR/slashRoot,target=/opt/grouper/slashRoot \
  -e GROUPER_SELF_SIGNED_CERT=true \
  -e GROUPER_RUN_SHIB_SP=false \
  -e GROUPER_AUTO_DDL_UPTOVERSION='v2.5.*' \
  -e GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES=0.0.0.0/0 \
  -e GROUPER_UI_GROUPER_AUTH=true \
  -e GROUPER_LOG_TO_HOST=true \
  --name grouper-ui i2incommon/grouper:2.5.39 ui

mchyzer@ISC20-0637-WL:~/containerScript$ chmod +x dockerUiRun.sh
mchyzer@ISC20-0637-WL:~/containerScript$ ./dockerUiRun.sh
```

At this point the DB is initialized since the container started and we specified to auto-init GSH, and we can assign a user password via script

[https://localhost](https://localhost)

Log in!

To troubleshoot, you can see the pid file and what processes are running

```
mchyzer@ISC20-0637-WL:~/containerScript$ docker exec -it grouper-ui bash -c 'cat /run/httpd/httpd.pid'
91
mchyzer@ISC20-0637-WL:~/containerScript$ docker exec -it grouper-ui bash -c 'ps -ef'
```
