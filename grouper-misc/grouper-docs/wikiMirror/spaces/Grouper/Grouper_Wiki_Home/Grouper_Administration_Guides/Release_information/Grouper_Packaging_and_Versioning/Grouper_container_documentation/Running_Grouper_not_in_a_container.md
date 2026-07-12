---
title: "Running Grouper not in a container"
space: Grouper
pageId: 28555592
version: 2
lastUpdated: 2026-07-01T05:37:50.918Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555592/Running+Grouper+not+in+a+container
---

It is best to run Grouper in its container, if nothing else, in [maturity level 0](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554540/Install+the+Grouper+container+with+maturity+level+0)

If you cannot do that, then you should run the latest stable Grouper version on a host.

Ideally you would mimic the container on a host. Try to do as many of these possible.

1. Run on linux
2. Using the same filesystem paths would be nice
3. Run the same java version (openjdk), if not make sure java8
  
  1. Install this in your linux
4. Run the same tomcat/tomee (if not make sure it is tomcat 8.5 compatible)
  
  1. Copy the tomcat from the container. You can do this on a workstation or any server with docker to make a tarball
    
    
    ```
    [root@i2midev6 ~]# docker run --detach -e GROUPER_LOG_TO_HOST=true --name grouperFiles i2incommon/grouper:2.5.XX ui
    [root@i2midev6 ~]# docker cp grouperFiles:/opt/tomee .
    [root@i2midev6 ~]# docker rm -f grouperFiles
    [root@i2midev6 ~]# ls
    tomee
    [root@i2midev6 ~]# tar czf grouperTomee.tgz tomee
    [root@i2midev6 ~]# rm -rf tomee
    
    
    ```
5. Run the webapp files copied from the container
  
  1. UI
    
    
    ```
    [root@i2midev6 ~]# docker run --detach -e GROUPER_LOG_TO_HOST=true --name grouperFiles i2incommon/grouper:2.5.XX ui
    [root@i2midev6 ~]# docker cp grouperFiles:/opt/grouper/grouperWebapp .
    [root@i2midev6 ~]# docker rm -f grouperFiles
    [root@i2midev6 ~]# ls
    grouperWebapp
    [root@i2midev6 ~]# tar czf grouperWebappUi.tgz grouperWebapp
    [root@i2midev6 ~]# rm -rf grouperWebapp
    ```
  2. WS
    
    
    ```
    [root@i2midev6 ~]# docker run --detach -e GROUPER_LOG_TO_HOST=true --name grouperFiles i2incommon/grouper:2.5.XX ws
    [root@i2midev6 ~]# docker cp grouperFiles:/opt/grouper/grouperWebapp .
    [root@i2midev6 ~]# docker rm -f grouperFiles
    [root@i2midev6 ~]# ls
    grouperWebapp
    [root@i2midev6 ~]# tar czf grouperWebappWs.tgz grouperWebapp
    [root@i2midev6 ~]# rm -rf grouperWebapp
    ```
  3. Daemon
    
    
    ```
    [root@i2midev6 ~]# docker run --detach -e GROUPER_LOG_TO_HOST=true --name grouperFiles i2incommon/grouper:2.5.XX daemon
    [root@i2midev6 ~]# docker cp grouperFiles:/opt/grouper/grouperWebapp .
    [root@i2midev6 ~]# docker rm -f grouperFiles
    [root@i2midev6 ~]# ls
    grouperWebapp
    [root@i2midev6 ~]# tar czf grouperWebappDaemon.tgz grouperWebapp
    [root@i2midev6 ~]# rm -rf grouperWebapp
    ```
6. Overlay with your own config files or edit with sed. Better to script
