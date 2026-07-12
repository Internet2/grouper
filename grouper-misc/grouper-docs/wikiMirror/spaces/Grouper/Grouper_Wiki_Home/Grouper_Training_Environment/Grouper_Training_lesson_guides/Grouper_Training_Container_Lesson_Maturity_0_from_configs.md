---
title: "Grouper Training - Container - Lesson: Maturity 0 from configs"
space: Grouper
pageId: 28545554
version: 25
lastUpdated: 2026-07-12T15:26:44.517Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545554/Grouper+Training+-+Container+-+Lesson+Maturity+0+from+configs
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson steps**

## Stop current running container

| `docker ps -a`   `docker stop``101.1``.``1` |
| --- |

## Start the lesson and get a database

Use your own random password not 'rPV2WVGTBay11EZ6qiOH'

```Bash
cd; mkdir maturity0
cd maturity0
docker run --name postgres -e \
POSTGRES_PASSWORD=pass -d -p 5432:5432 \
postgres:14

```

Init the database

`docker exec -it -u postgres postgres psql`

```Bash
CREATE USER grouper PASSWORD 'rPV2WVGTBay11EZ6qiOH';
CREATE DATABASE grouper;
GRANT ALL PRIVILEGES ON DATABASE grouper TO grouper;
\q

```

## Decide if we want logs going to STDOUT, or to files

Files? Let's make a directory to mount

```Bash
mkdir -p logs/ui
mkdir -p logs/daemon
mkdir -p logs/gsh
chmod a+rwx logs/ui logs/daemon logs/gsh

```

## Set up Grouper configs

Make a single place for Grouper configs. At runtime these will need to be in the container directory /opt/grouper/grouperWebapp/WEB-INF/classes.

```Bash
mkdir -p config/grouper

```

Create a folder to store the SQL for db initialization

```
mkdir ddlScripts
```

### Generate a random morphString encryption key and save to a file

```Bash
echo encrypt.key = $(openssl rand -base64 15) > config/grouper/morphString.properties

cat config/grouper/morphString.properties
```

### database connection properties

cat > config/grouper/grouper.hibernate.properties

```bash
hibernate.connection.url = jdbc:postgresql://172.17.0.1:5432/grouper
hibernate.connection.username = grouper
hibernate.connection.password = rPV2WVGTBay11EZ6qiOH
grouper.is.ui.basicAuthn = true
grouper.is.ws.basicAuthn = true
registry.auto.ddl.upToVersion = 5.*.*

```

CTRL-d

### subject properties (empty placeholder)

```Bash
touch config/grouper/subject.properties

```

## Init the database

Note, we generally want mounted files going to the staging directory /opt/grouper/slashRoot, so they get copied to their final location

```Bash
docker run --rm -it \
     --mount type=bind,src=$PWD/config/grouper,dst=/opt/grouper/slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes \
     --mount type=bind,src=$PWD/ddlScripts,dst=/opt/grouper/grouperWebapp/WEB-INF/ddlScripts \
     i2incommon/grouper:5.13.0 gsh -registry -check -runscript -noprompt

```

## Set the GrouperSystem password for built-in authentication

```Bash
docker run --rm -it \
    -e GROUPER_USE_PIPES=false \
    --mount type=bind,src=$PWD/logs/gsh,dst=/opt/grouper/logs \
    --mount type=bind,src=$PWD/config/grouper,dst=/opt/grouper/slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes \
    i2incommon/grouper:5.13.0 gsh

```

```
new GrouperPasswordSave().
    assignUsername("GrouperSystem").
    assignPassword("rPV2WVGTBay11EZ6qiOH").
    assignApplication(GrouperPassword.Application.UI).
    save()

:q
```

## Create a script for starting up a ui and daemon

nano runUi.sh

```Bash
#!/bin/bash

function start() {
    echo "Starting maturity0-ui..."
    docker run --detach \
        --name maturity0-ui \
        --publish 8443:8443 \
        -e GROUPER_LOG_TO_PIPE=false \
        -e GROUPER_LOG_TO_HOST=true \
        --mount type=bind,src=$PWD/logs/ui,dst=/opt/grouper/logs \
        --mount type=bind,src=$PWD/config/grouper,dst=/opt/grouper/slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes \
        i2incommon/grouper:5.13.0 ui
}

function stop() {
    echo "Stopping maturity0-ui..."
    docker stop maturity0-ui; docker rm maturity0-ui
}

function restart() {
    stop
    start
}

case "$1" in
    start) start;;
    stop) stop;;
    restart) restart;;
    *) echo "Invalid command - Valid->start|stop|restart";;
esac 
```

nano runDaemon.sh

```Bash
#!/bin/bash

function start() {
    echo "Starting grouper daemon..."
    docker run --detach \
        --name maturity0-daemon \
        -e GROUPER_LOG_TO_PIPE=false \
        -e GROUPER_LOG_TO_HOST=true \
        --mount type=bind,src=$PWD/logs/daemon,dst=/opt/grouper/logs \
        --mount type=bind,src=$PWD/config/grouper,dst=/opt/grouper/slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes \
        i2incommon/grouper:5.13.0 daemon
}

function stop() {
    echo "Stopping grouper daemon..."
    docker stop maturity0-daemon; docker rm maturity0-daemon
}

function restart() {
    stop
    start
}

case "$1" in
    start) start;;
    stop) stop;;
    restart) restart;;
    *) echo "Invalid command - Valid->start|stop|restart";;
esac 
```

## Start up the ui and daemon

```Bash
chmod a+rx runUi.sh runDaemon.sh
./runUi.sh start
./runDaemon.sh start

```

## Verify running

Log into the container ([https://localhost:8443/grouper/](https://localhost:8443/grouper/), login is GrouperSystem/rPV2WVGTBay11EZ6qiOH)

Run the OTHER_JOB_upgradeTasks job to get the installation to its correct final state.

## Look at the logs

(Fix read permissions on the files outside the container: *find logs -type f | sudo xargs chmod a+r*)

```bash
2205 Feb 18 13:30 logs/ui/grouper.log
5524 Feb 18 13:30 logs/ui/catalina.out

2525 Feb 18 13:30 logs/daemon/grouper.log
5676 Feb 18 13:30 logs/daemon/catalina.out

docker logs maturity0-ui
docker logs maturity0-daemon
```

## Edit the runUi

nano runUi.sh

Add

```
        -e GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES='0.0.0.0/0' \

```

Stop and run again

```
./runUi.sh restart
```

Go to Miscellaneous → Configure

## Stop and remove the containers

```
./runUi.sh stop
./runDaemon.sh stop
```

Note that the postgres container is still running. We can use the same database for the next lesson.
