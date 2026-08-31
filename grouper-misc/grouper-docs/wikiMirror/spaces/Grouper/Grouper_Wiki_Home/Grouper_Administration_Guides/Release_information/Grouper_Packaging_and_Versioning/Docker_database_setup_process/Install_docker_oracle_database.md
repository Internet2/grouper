---
title: "Install docker oracle database"
space: Grouper
pageId: 28555795
version: 4
lastUpdated: 2026-07-01T05:37:27.655Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555795/Install+docker+oracle+database
---

## Pre-built container

Use your own random password, not 'B8JWRf4vNA6GV6HZQmj3'

```
docker run --name oracle -e ORACLE_PASSWORD=B8JWRf4vNA6GV6HZQmj3 -d -p 1521:1521 gvenzl/oracle-xe:18-slim

docker exec -it oracle sqlplus
system
B8JWRf4vNA6GV6HZQmj3

create role UserRole;
GRANT connect, resource, CREATE view TO UserRole;
CREATE USER grouper_v2_6 IDENTIFIED BY grouper_v2_61;
GRANT UserRole TO grouper_v2_6;
GRANT UNLIMITED TABLESPACE TO grouper_v2_6;  

Get your IP address
$ ifconfig

Database url: jdbc:oracle:thin:@a.b.c.d:1521:XE    (sub in ip address)
grouper_v2_6
grouper_v2_61
```

## Build your own

```
git clone --depth 1 https://github.com/oracle/docker-images.git oracle-docker-images

cd oracle-docker-images/OracleDatabase/SingleInstance/dockerfiles
./buildContainerImage.sh -v 18.4.0 -x

    Parameters:
    -v: version to build
        Choose one of: 11.2.0.2  12.1.0.2  12.2.0.1  18.3.0  18.4.0  19.3.0  
    -t: image_name:tag for the generated docker image
    -e: creates image based on 'Enterprise Edition'
    -s: creates image based on 'Standard Edition 2'
    -x: creates image based on 'Express Edition'
    -i: ignores the MD5 checksums
    -o: passes on container build option

docker run -d --hostname webauthn-reg-ora --name webauthn-reg-ora --volume ~/.oracle/data/webauthn/oradata:/opt/oracle/oradata -e ORACLE_PWD=661e3c759dd9eb89 -p 1521:1521 -p 5500:5500 -e ORACLE_SID=XE oracle/database:18.4.0-xe

docker exec -it webauthn-reg-ora sqlplus / AS SYSDBA
docker exec -it webauthn-reg-ora sqlplus WEBAUTHN@XEPDB1

show con_name;
--CDB$ROOT

show pdbs;
/*
CON_ID | NAME     | OPEN_MODE  | RESTRICTED
-------+----------+------------+-----------
     2 | PDB$SEED | READ ONLY  | NO        
     3 | XEPDB1   | READ WRITE | NO        
URL: jdbc:oracle:thin:@//localhost:1521/XE
User: SYS AS SYSDBA
Password: B8JWRf4vNA6GV6HZQmj3
OR connect to service db:
URL: jdbc:oracle:thin:@//localhost:1521/XEPDB1
User/Password: (created by sysdba) (edited) 
```
