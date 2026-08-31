---
title: "Grouper training VM documentation"
space: Grouper
pageId: 28543607
version: 30
lastUpdated: 2026-07-01T05:49:23.975Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543607/Grouper+training+VM+documentation
---

## Introduction

When connecting to the AWS training environment via SSH, students will be interacting with a [Bash shell](https://www.gnu.org/software/bash/). If you are not familiar, the shell is a dollar-sign prompt you can type commands at (e.g. **[student@ip ~]$**) . The only commands you really need to know for the [Grouper Training Environment (GTE)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541839/Grouper+Training+Environment)are listed below. Before you can go to your Grouper instance for a course, you need to "run the course" (below).

| Purpose | Command | Description | Example |
| --- | --- | --- | --- |
| List courses | ./gte | List all available courses to run. For most courses, 2 docker images exist-- A starting image named *COURSE.1*, and an image that is in the expected end state called *COURSE.end*. E.g. "201.1.1" and "201.1.end". Normally, you are not expected to start and stop containers between exercises. | ```bash [student@ip ~]$ ./gte 101.1.1 etc ```   ```bash [student@ip ~]$ ./gte 101.1.1 201.1.1 201.1.end 201.2.1 201.2.end 201.3.1 201.3.end 201.4.1 201.4.end 201.5.1 201.5.end 211.1.1 301.4.1 401.1.1 401.1.end 401.2.1 401.2.end 401.3.1 401.3.end 401.4.1 401.4.end full_demo ``` |
| Run a course | ./gte <course> | This will stop all GTE docker containers running, and will start the one specified. It will also stop/start the rabbitmq messaging container. Only one course can run at a time due to network port requirements. | ``` [student@ip ~]$ ./gte 101.1.1 ``` |
| See what is running | docker ps | If a course is running, or other docker containers, this will display | ``` [student@ip ~]$ docker ps ``` |
| Shell into running container | ./gte-shell   exit | A container is a virtual operating system that you can go into and investigate, look around, troubleshoot. Note, this will not work if you are not running a container. Start a container before shelling in. Note, containers are ephemeral, if you change the contents in a container they will be lost when the container stops. Note, when done with the shell, type exit to return to the host (outside container). | ```bash [student@ip ~]$ ./gte-shell [root@1ead0a02c7fc WEB-INF]# exit ``` |
| GSH into a container | ./gte-gsh   :q | GSH or Grouper Shell is a command line Java interface into Grouper. It is useful to run commands or to script multiple commands. It is only available for Grouper admins. Note, this will not work if you are not running a container. Start a container before GSH'ing in. Note, when done type :q to quit and return to the host. | ```bash [student@ip ~]$ ./gte-gsh groovy:000> :q ``` |
| View logs of a running container | ./gte-logs   CTRL-c | Note, this will not work if you are not running a container. Start a container before viewing logs. Once a container is stopped in the GTE the logs are gone. Type CTRL and c to return to the host. | ```bash [student@ip ~]$ ./gte-logs ``` |
| Restart docker | sudo service docker start | If docker fails for some reason:   ``` Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running? ``` | ``` [student@ip ~]$ sudo /sbin/service docker start Redirecting to /bin/systemctl start docker.service ``` |

##
