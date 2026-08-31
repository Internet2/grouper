---
title: "Grouper Training - Container - Lesson: Maturity 1 derived image"
space: Grouper
pageId: 28545600
version: 51
lastUpdated: 2026-07-12T15:26:45.856Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545600/Grouper+Training+-+Container+-+Lesson+Maturity+1+derived+image
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson steps**

## Stop current running GTE container (if running)

| `docker ps -a`   `docker stop``101.1``.``1` |
| --- |

## Copy the maturity0 folder to a new maturity1 folder

```
cd
cp -pr maturity0 maturity1
cd maturity1

find logs -type f | xargs rm
```

## Edit Dockerfile (nano Dockerfile)

```bash
# this matches the version you decided on from release notes
FROM i2incommon/grouper:5.11.2

# this will overlay all the files from slashRoot on to /
COPY config/grouper /opt/grouper/grouperWebapp/WEB-INF/classes
```

## Build docker derived image

```
docker build -t my-grouper:latest .
```

## Edit startup scripts

### cat > runUi.sh

```
#!/bin/bash
 
function start() {
    echo "Starting maturity1-ui..."
    docker run --detach \
        --name maturity1-ui \
        --publish 8443:8443 \
        -e GROUPER_LOG_TO_PIPE=false \
        -e GROUPER_LOG_TO_HOST=true \
        -e GROUPER_UI_CONFIGURATION_EDITOR_SOURCEIPADDRESSES='0.0.0.0/0' \
        --mount type=bind,src=$PWD/logs/ui,dst=/opt/grouper/logs \
        my-grouper:latest ui
}
 
function stop() {
    echo "Stopping maturity1-ui..."
    docker stop maturity1-ui; docker rm maturity1-ui
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

CTRL-d

### cat > runDaemon.sh

```
#!/bin/bash
 
function start() {
    echo "Starting grouper daemon..."
    docker run --detach \
        --name maturity1-daemon \
        -e GROUPER_LOG_TO_PIPE=false \
        -e GROUPER_LOG_TO_HOST=true \
        --mount type=bind,src=$PWD/logs/daemon,dst=/opt/grouper/logs \
        my-grouper:latest daemon
}
 
function stop() {
    echo "Stopping grouper daemon..."
    docker stop maturity1-daemon; docker rm maturity1-daemon
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

CTRL-d

## Delete config files (to show they are not mounted to container), run

```
mv config/grouper config/grouperUNUSED
```

## Run institutional image

```
./runUi.sh start
./runDaemon.sh start
```

## Show a change of image

Change the Dockerfile to another version. See the release notes and upgrade instructions. Build, and restart.

## Clean up

```
./runUi.sh stop
./runDaemon.sh stop

docker rm -f postgres maturity1-ui maturity1-daemon

# (optional remove images) docker rmi my-grouper:latest
```
