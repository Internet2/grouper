---
title: "Grouper on TIER packaging server CentOS and MySQL"
space: GrIntDev
pageId: 48792867
version: 16
lastUpdated: 2026-07-12T06:45:49.310Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792867/Grouper+on+TIER+packaging+server+CentOS+and+MySQL
---

installing Grouper on TIER packaging server: grouper.testbed.tier.internet2.edu

```
grouper#>mkdir /root/software
grouper#>cd /root/software
grouper#>yum -y install mlocate
grouper#>updatedb
grouper#>yum install java-1.8.0-openjdk.x86_64
grouper#>yum install java-1.8.0-openjdk-devel.x86_64
grouper#>yum install dos2unix
grouper#>wget http://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.11-1.el7.x86_64.rpm-bundle.tar
grouper#>tar xvf mysql-5.7.11-1.el7.x86_64.rpm-bundle.tar 
grouper#>mv mysql-community-server-minimal-5.7.11-1.el7.x86_64.rpm temp-mysql-community-server-minimal-5.7.11-1.el7.x86_64.rpm
grouper#>yum install mysql-community-{server,client,common,libs}-*
grouper#>systemctl enable mysqld.service
grouper#>systemctl start mysqld
grouper#>yum install telnet
grouper#>telnet localhost 3306
grouper#>systemctl stop mysqld
grouper#>sudo -u mysql mysqld --skip-grant-tables &
mysql> use mysql;
mysql> update user set authentication_string=password('XXXXXXX') where user='root'
Query OK, 1 row affected, 1 warning (0.00 sec)
Rows matched: 1  Changed: 1  Warnings: 1
mysql> commit;
Query OK, 0 rows affected (0.00 sec)
mysql> flush privileges;
Query OK, 0 rows affected (0.01 sec)
mysql> 
grouper#>yum install emacs
grouper#>yum install java-1.8.0-openjdk-devel.x86_64
```

Add this to /etc/my.cnf

```
character-set-server=utf8
collation-server = utf8_bin
init-connect='SET NAMES utf8'
init_connect='SET collation_connection = utf8_bin'
skip-character-set-client-handshake
```

Start mysql and connect and change root pass

```
grouper#>ps -ef | grep mysql
grouper#>kill -KILL XXXXX
grouper#>systemctl start mysqld
mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY 'XXXXXXXXXX';
Query OK, 0 rows affected (0.00 sec)
mysql> flush privileges;
mysql> create database grouper;
mysql> create user 'grouper'@'localhost' identified by 'XXXXXXXXXXX';
mysql> grant all on grouper.* to 'grouper'@'localhost' identified by 'XXXXXXXXXXX';
mysql> flush privileges;

```

Setup the grouper user and application

```
grouper#>useradd grouper
grouper#>su - grouper
[grouper@grouper ~]$ mkdir /home/grouper/2.3.0
[grouper@grouper ~]$ cd /home/grouper/2.3.0/
[grouper@grouper 2.3.0]$ wget http://software.internet2.edu/grouper/release/2.3.0/grouperInstaller.jar
[grouper@grouper 2.3.0]$ cat grouper.installer.properties
# this should be before the version number
download.server.url = http://software.internet2.edu/grouper
# default version to install
grouper.version = 2.3.0
# print out autorun keys in prompts so you can easily see how to configure the autorun
grouperInstaller.print.autorunKeys = true
# default to install or upgrade (default is install)
grouperInstaller.default.installOrUpgrade = install
# where to get grouper source from, the variable $BRANCH_NAME$ will be substituted for the branch
download.source.url = https://github.com/Internet2/grouper/archive/$BRANCH_NAME$.zip
# where to get grouper psp source from, the variable $BRANCH_NAME$ will be substituted for the branch
download.pspSource.url = https://github.com/Internet2/grouper-psp/archive/$BRANCH_NAME$.zip
##############################
## Autorun properties
##
## If you uncomment one of these properties it will be used as empty, only uncomment to use
## 
##############################
#### set this to true to try to use defaults for everything.  Only things without default values will need to be set
grouperInstaller.autorun.useDefaultsAsMuchAsAvailable = true
########## AUTORUN PROPERTIES WITH NO DEFAULT OR ARE COMMONLY CHANGED
## Note: not all of them need to be filled out for all operations
# autorun grouper system password (its not secure to have a plain text pass in a config file)
grouperInstaller.autorun.grouperSystemPassword = XXXXXXXXXX
# autorun Enter the database URL
grouperInstaller.autorun.dbUrl = jdbc:mysql://localhost:3306/grouper
# autorun database user
grouperInstaller.autorun.dbUser = grouper
# autorun database pass (note, it is not good security to have plaintext passwords in text config files)
grouperInstaller.autorun.dbPass = XXXXXXXXXX
# autorun Do you want to init the database (delete all existing grouper tables, add new ones) (t|f)? 
grouperInstaller.autorun.deleteAndInitDatabase = t
# autorun What is the location of your tomcat server.xml for the UI?
# Note, if you dont use tomcat just leave it blank or type 'blank':
grouperInstaller.autorun.locationOfTomcatServerXml = /home/grouper/2.3.0/apache-tomcat-6.0.35/conf/server.xml

[grouper@grouper 2.3.0]$ java -cp .:grouperInstaller.jar edu.internet2.middleware.grouperInstaller.GrouperInstaller 
...
Installation success!
Go here for the Grouper UI (change hostname if on different host): http://localhost:8080/grouper/
This is the Grouper WS URL (change hostname if on different host): http://localhost:8080/grouper-ws/

```
