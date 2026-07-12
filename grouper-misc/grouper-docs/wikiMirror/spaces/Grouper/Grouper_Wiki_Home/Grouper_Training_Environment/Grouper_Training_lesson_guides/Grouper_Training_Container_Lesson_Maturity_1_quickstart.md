---
title: "Grouper Training - Container - Lesson: Maturity -1 quickstart"
space: Grouper
pageId: 28545593
version: 55
lastUpdated: 2026-07-12T15:26:45.388Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545593/Grouper+Training+-+Container+-+Lesson+Maturity+-1+quickstart
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson steps**

Stop current running container

```
docker ps -a
docker stop 101.1.1
```

Work in a directory that can be easily cleaned up

```
mkdir quickstart
cd quickstart
```

Edit the compose config file

```
nano docker-compose.yml
```

Note: we changed the port from the default config to 8443. Note: use your own random password, do not use: 'BlKIS9FeyezRnAw7gW7K'.

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
      - POSTGRES_PASSWORD=BlKIS9FeyezRnAw7gW7K
  grouper:
    image: "i2incommon/grouper:5.13.0"
    restart: always
    ports:
      - '8443:8443'
    command:
      - quickstart
    environment:
      - GROUPERSYSTEM_QUICKSTART_PASS=BlKIS9FeyezRnAw7gW7K
      - GROUPER_MORPHSTRING_ENCRYPT_KEY=abcd1234
      - GROUPER_DATABASE_PASSWORD=BlKIS9FeyezRnAw7gW7K
      - GROUPER_DATABASE_USERNAME=postgres
      - GROUPER_DATABASE_URL=jdbc:postgresql://postgres:5432/postgres
      - GROUPER_AUTO_DDL_UPTOVERSION=5.*.*
```

Install docker-compose (it it's not already installed)

```
sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose

sudo chmod +x /usr/local/bin/docker-compose
```

Run the quickstart (use --detach to run in background)

```
docker-compose up --detach 
```

Temporary fix, if docker-compose gives an error: *urllib3 v2.0 only supports OpenSSL 1.1.1+*

*pip install urllib3==1.26.6*

Log in: [https://localhost:8443/grouper](https://localhost:8443/grouper)

```
GrouperSystem
BlKIS9FeyezRnAw7gW7K
```

Copy WS client out of container

```
docker cp quickstart_grouper_1:/opt/grouper/grouperWebapp/WEB-INF/lib/grouperClient-5.13.0.jar ./grouperClient.jar

```

Might need to run:

```
sudo yum install java-17-amazon-corretto-headless
```

Edit config file for WS client

```
nano grouper.client.properties
```

Contents of config file

```
############
grouperClient.webService.url = https://localhost:8443/grouper/servicesRest
grouperClient.webService.login = GrouperSystem
grouperClient.webService.password = BlKIS9FeyezRnAw7gW7K  
# turn off SSL until a real SSL certificate is installed
# NOTE, THIS IS NOT GOOD SECURITY AND IS FOR THE QUICK START ONLY!
grouperClient.https.customSocketFactory = edu.internet2.middleware.grouperClient.ssl.EasySslSocketFactory
############
```

Call WS using GrouperClient

```
java -jar grouperClient.jar --operation=getSubjectsWs --subjectIds=GrouperSystem --debug=true
```

Call WS endpoint directly

```
curl -X GET -k -H 'Content-Type: application/json' --user GrouperSystem:BlKIS9FeyezRnAw7gW7K -i https://localhost:8443/grouper/servicesRest/json/v5_11_002/subjects/GrouperSystem

```

Use jq command to parse important fields from the json result

Example 1: Pretty-print json output

```
curl --silent -X GET -k -H 'Content-Type: application/json' --user GrouperSystem:BlKIS9FeyezRnAw7gW7K https://localhost:8443/grouper/servicesRest/json/v5_7_000/subjects/GrouperSystem | jq .
```

Example 2: Extract the a value from the json result (WsGetSubjectsResults.wsSubjects[0].id)

```
curl --silent -X GET -k -H 'Content-Type: application/json' --user GrouperSystem:BlKIS9FeyezRnAw7gW7K https://localhost:8443/grouper/servicesRest/json/v5_7_000/subjects/GrouperSystem | jq .WsGetSubjectsResults.wsSubjects[].id
```

Verify image hash

```
docker image inspect i2incommon/grouper:5.13.0 | grep i2incommon/grouper@sha256
```

Compare hash vs. value listed in [v5 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes)

Make command line script

```
nano grouperQsDockerRun.sh
```

Command line script contents

```
#!/bin/bash
docker-compose up --detach
```

Make executable

```
chmod +x grouperQsDockerRun.sh
```

Cleanup

Stop the quick start (and remove the container)

```
docker-compose down
```
