---
title: "Grouper Training - Administration - Lesson: Web Services"
space: Grouper
pageId: 28544908
version: 2
lastUpdated: 2026-07-12T15:26:31.538Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544908/Grouper+Training+-+Administration+-+Lesson+Web+Services
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Web Services wiki

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

## Swagger documentation

[https://grouperdemo.internet2.edu/grouper_v5/docs/](https://grouperdemo.internet2.edu/grouper_v5/docs/)

## Hands On

### 1. Create a password for GrouperSystem

In your VM command line:

`gte-gsh`When you get the GSH prompt:

```groovy
new GrouperPasswordSave().assignApplication(GrouperPassword.Application.WS).assignUsername("GrouperSystem").assignPassword("password").save();

:q
```

### 2. Use the grouper client

In your VM command line:

`gte-shell`Get the groups banderson is a member of

``

```bash
java -jar /opt/grouper/grouperWebapp/WEB-INF/lib/grouperClient-*.jar \
  --wsUser=GrouperSystem \
  --wsPass=password \
  --wsEndpoint=http://localhost/grouper-ws/servicesRest \
  --operation=getGroupsWs \
  --subjectIdentifiers=banderson
```

Same with debugging to show the syntax

```bash
java -jar /opt/grouper/grouperWebapp/WEB-INF/lib/grouperClient-*.jar \
  --wsUser=GrouperSystem \
  --wsPass=password \
  --wsEndpoint=http://localhost/grouper-ws/servicesRest \
  --operation=getGroupsWs \
  --subjectIdentifiers=banderson \
  --debug=true
```

### 3. Use a direct REST call

```bash
curl http://localhost/grouper-ws/servicesRest/5.16.0/subjects -X POST --user GrouperSystem:password -H 'Content-Type: application/json' --data '
{
  "WsRestGetGroupsRequest":{
    "subjectLookups":[
      {
        "subjectIdentifier":"banderson"
      }
    ]
    ,
    "enabled":"T"
  }
}
'

```

### 4. Same but format using the jq command

```bash
curl http://localhost/grouper-ws/servicesRest/5.16.0/subjects -X POST --user GrouperSystem:password -H 'Content-Type: application/json' --data '
{
  "WsRestGetGroupsRequest":{
    "subjectLookups":[
      {
        "subjectIdentifier":"banderson"
      }
    ]
    ,
    "enabled":"T"
  }
}
' | jq .
```

``
