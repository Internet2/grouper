---
title: "Install docker mysql database"
space: Grouper
pageId: 28555773
version: 14
lastUpdated: 2026-07-01T05:37:30.686Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555773/Install+docker+mysql+database
---

Run the container (use your own random password not 'YB9QZdwBx4M89YMiQjtF')

```
docker run --name=mysql8 -d -e MYSQL_ROOT_PASSWORD=YB9QZdwBx4M89YMiQjtF -p 3306:3306 mysql/mysql-server:8.0
```

Create a database and user, note you need to put the source IP address in there. You can see what it is from the error message if you try to connect and you are wrong:

```
message from server: "Host '172.17.0.1' is not allowed to connect to this MySQL server"
```

Note, if you get a mysql.sock error, wait until the database is started to connect. i.e. try again.

```
mchyzer@ISC20-0637-WL:~$ docker exec -it mysql8 mysql -uroot -p
Enter password:
mysql> create database grouper_v2_5 character set UTF8 collate utf8_bin;
mysql> create user 'grouper_v2_5'@'1.2.3.4' IDENTIFIED WITH mysql_native_password by 'YB9QZdwBx4M89YMiQjtF';
mysql> grant all on grouper_v2_5.* to 'grouper_v2_5'@'1.2.3.4';
mysql> flush privileges;
mysql> quit
Bye
mchyzer@ISC20-0637-WL:~$
```

Start a grouper to point to that database. Note, this works on windows, on non-windows you can adjust the IP address of database

```
docker run --detach -e GROUPER_RUN_SHIB_SP='false' -e GROUPER_UI_GROUPER_AUTH=true \
-e GROUPER_MORPHSTRING_ENCRYPT_KEY=abc123def456 -e GROUPER_AUTO_DDL_UPTOVERSION='v2.5.*' \
-e GROUPER_DATABASE_URL=jdbc:mysql://docker.for.win.localhost:3306/grouper_v2_5?useSSL=false \
-e GROUPER_DATABASE_USERNAME=grouper_v2_5 -e GROUPER_DATABASE_PASSWORD=YB9QZdwBx4M89YMiQjtF \
-e SELF_SIGNED_CERT='true' --name grouper-ui --publish 443:443 i2incommon/grouper:2.5.39 ui

```

Watch the logs until the database is initialized

```
docker logs -f grouper-ui
```

Go in and set a GrouperSystem UI password

```
mchyzer@ISC20-0637-WL:~$ docker exec -it -u tomcat grouper-ui bash
[tomcat@f6c8e2b68215 WEB-INF]$ cd bin
[tomcat@f6c8e2b68215 bin]$ ./gsh.sh
groovy:000> new GrouperPasswordSave().assignApplication(GrouperPassword.Application.UI).assignUsername("GrouperSystem").assignPassword("YB9QZdwBx4M89YMiQjtF").save();
groovy:000> :quit
[tomcat@f6c8e2b68215 bin]$ exit
mchyzer@ISC20-0637-WL:~$
```

Log in

[https://localhost](https://localhost)
