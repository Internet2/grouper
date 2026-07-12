---
title: "Install AWS server and docker"
space: Grouper
pageId: 28555302
version: 2
lastUpdated: 2026-07-01T05:38:34.622Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555302/Install+AWS+server+and+docker
---

## Get a linux server

Just need some server somewhere. This example will make an ec-2 from amazon

Create EC2 Red Hat Enterprise Linux 8 (HVM), SSD Volume Type linux t2.medium (4 gig ram) server, public IP address, get pem

Use the pem file to ssh to the ec-2 with your pem as ec2-user

Allow 443 in Networking and Security → Security Groups (note for 2.4 include 8443 too)

Ensure can talk to database

```
[root@ip-172-30-0-83 ~]# yum install telnet
[root@ip-172-30-0-83 ~]# yum install bind-utils
[root@ip-172-30-0-83 ~]# telnet database-3.cstlzkqw179p.us-east-1.rds.amazonaws.com 3306
Trying 172.30.3.244...
Connected to database-3.cstlzkqw179p.us-east-1.rds.amazonaws.com.
Escape character is '^]'.
J
5.7.22UW/%U6jF1pW|O
                   W;2^mysql_native_password^]
telnet> quit  
Connection closed.
[root@ip-172-30-0-83 ~]# 

```

## Install docker

Here are [docker install instructions from the docker website](https://docs.docker.com/install/linux/docker-ce/centos/) (pick which os)

Or use another containerization software e.g. podman

See if docker installed, install if not

```
[ec2-user@ip-172-30-0-157 ~]$ sudo su -
Last login: Tue Jan 14 22:13:16 UTC 2020 from isc15-0009-wd.kite.upenn.edu on pts/0
[root@ip-172-30-0-157 ~]# docker
-bash: docker: command not found
```

## Linux docker install instructions

Note, you need a linux with systemd. It is not recommended to use amazon linux if it doesnt have systemd or an easy way to install docker

The `centos-extras` repository must be enabled. This repository is enabled by default, but if you have disabled it, you need to [re-enable it](https://wiki.centos.org/AdditionalResources/Repositories).

```
[root@ip-172-30-0-157 ~]# yum install -y yum-utils \
  device-mapper-persistent-data \
  lvm2
[root@ip-172-30-0-157 ~]# sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
[root@ip-172-30-0-157 ~]# yum -y install https://download.docker.com/linux/centos/7/x86_64/stable/Packages/containerd.io-1.2.6-3.3.el7.x86_64.rpm
[root@ip-172-30-0-157 ~]# yum install docker-ce docker-ce-cli
```
