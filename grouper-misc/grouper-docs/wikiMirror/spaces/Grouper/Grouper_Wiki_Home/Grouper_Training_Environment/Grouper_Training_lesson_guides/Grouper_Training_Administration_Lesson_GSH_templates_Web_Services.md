---
title: "Grouper Training - Administration - Lesson: GSH templates - Web Services"
space: Grouper
pageId: 28544913
version: 3
lastUpdated: 2026-07-12T15:26:31.983Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544913/Grouper+Training+-+Administration+-+Lesson+GSH+templates+-+Web+Services
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Create WS password for GrouperSystem

gte-gsh

```groovy
new 
GrouperPasswordSave().assignApplication(GrouperPassword.Application.WS).assignUsername("GrouperSystem").assignPassword("password").save()

:q
```

## Sample request with an error

REQUEST

```bash
curl --insecure -X POST -H "Content-Type: application/json" https://localhost:8443/grouper-ws/servicesRest/5.0.0/gshTemplateExec --user GrouperSystem:password --data '
{
  "WsRestGshTemplateExecRequest": {
    "configId": "customApp",
    "ownerType": "stem",
    "ownerStemLookup": {
      "stemName": "test:testTemplates"
    },
    "inputs": [
      {
        "name": "gsh_input_appName",
        "value": "badApp"
      },
      {
        "name": "gsh_input_subjectIds",
        "value": "banderson, x"
      }
    ],
    "clientVersion": "v5_0_000"
  }
}
' | jq .
```

## Sample request with success

Correct response

```bash
curl --insecure -X POST -H "Content-Type: application/json" https://localhost:8443/grouper-ws/servicesRest/5.0.0/gshTemplateExec --user GrouperSystem:password --data '
{
 "WsRestGshTemplateExecRequest": {
   "configId": "customApp",
   "ownerType": "stem",
   "ownerStemLookup": {
     "stemName": "test:testTemplates"
   },
   "inputs": [
     {
       "name": "gsh_input_appName",
       "value": "testAppWS"
     },
     {
       "name": "gsh_input_subjectIds",
       "value": "banderson, lmiller"
     }
   ],
   "clientVersion": "v5_0_000"
 }
}
' | jq .
```
