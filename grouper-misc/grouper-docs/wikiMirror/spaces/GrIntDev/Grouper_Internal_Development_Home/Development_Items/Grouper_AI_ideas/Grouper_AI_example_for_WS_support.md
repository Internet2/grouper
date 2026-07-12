---
title: "Grouper AI example for WS support"
space: GrIntDev
pageId: 48794092
version: 4
lastUpdated: 2026-07-12T06:46:30.555Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48794092/Grouper+AI+example+for+WS+support
---

You could train an AI tool on institution specific Grouper Web Services (WS) documentation, and on generic Grouper WS. This is a starting point proof of concept.

See this prompt and response from AI which is correct:

Lets look at curl too

Have AI troubleshoot a request:

The training data for the trained GPT is:

```
Use the grouperWs_swagger.json to see the REST API Grouper definitions.

The Grouper WS endpoint is: https://grouperws.institution.edu/grouperWs

The subject source for people is "pennperson", add this whenever a person is referred to.

Do not include actAsSubjectLookup in requests unless the user is asking about an "act as" call.

Tell the user they need a kerberos service principal with password, the user needs to be in the WS allowed group, and it is basic authentication.

This is an example of adding a member

Grouper web service sample of service: addMember, WsSampleAddMemberRest, manually written lite/rest, format: json, for version: 2.5.0

#########################################
##
## HTTP request sample (could be formatted for view by
## indenting or changing dates or other data)
##
#########################################

PUT /grouper-ws/servicesRest/v2_5_000/groups/aStem%3AaGroup/members HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxxx==
User-Agent: Jakarta Commons-HttpClient/3.1
Host: localhost:8092
Content-Length: 173
Content-Type: application/json; charset=UTF-8

{
  "WsRestAddMemberRequest":{
    "subjectLookups":[
      {
        "subjectId":"10021368"
      },
      {
        "subjectId":"10039438"
      }
    ]
  }
}

#########################################
##
## HTTP response sample (could be formatted for view by
## indenting or changing dates or other data)
##
#########################################

HTTP/1.1 201
Set-Cookie: JSESSIONID=0E1884B25B19DA54AFA439AF39E83104;path=/grouper-ws/;HttpOnly
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE
Content-Type: application/json;charset=UTF-8
Content-Length: 1255
Date: Tue, 31 Mar 2020 20:12:48 GMT
Connection: close
Server: Apache TomEE

{
  "WsAddMemberResults":{
    "responseMetadata":{
      "millis":"662",
      "serverVersion":"2.5.0"
    },
    "resultMetadata":{
      "resultCode":"SUCCESS",
      "resultMessage":"Success for: clientVersion: 2.5.0, wsGroupLookup: WsGroupLookup[pitGroups=[],groupName=aStem:aGroup], subjectLookups: Array size: 2: [0]: WsSubjectLookup[subjectId=10021368]\n[1]: WsSubjectLookup[subjectId=10039438]\n\n, replaceAllExisting: false, actAsSubject: WsSubjectLookup[subjectId=GrouperSystem], fieldName: null, txType: NONE, includeGroupDetail: false, includeSubjectDetail: false, subjectAttributeNames: null\n, params: null\n, disabledDate: null, enabledDate: null",
      "success":"T"
    },
    "results":[
      {
        "resultMetadata":{
          "resultCode":"SUCCESS_ALREADY_EXISTED",
          "success":"T"
        },
        "wsSubject":{
          "id":"10021368",
          "name":"10021368",
          "resultCode":"SUCCESS",
          "sourceId":"jdbc",
          "success":"T"
        }
        
      },
      {
        "resultMetadata":{
          "resultCode":"SUCCESS_ALREADY_EXISTED",
          "success":"T"
        },
        "wsSubject":{
          "id":"10039438",
          "name":"10039438",
          "resultCode":"SUCCESS",
          "sourceId":"jdbc",
          "success":"T"
        }
      }
    ]
    ,
    "wsGroupAssigned":{
      "description":"a group description",
      "displayExtension":"a group",
      "displayName":"a stem:a group",
      "enabled":"T",
      "extension":"aGroup",
      "idIndex":"10009",
      "name":"aStem:aGroup",
      "typeOfGroup":"group",
      "uuid":"32ca90db41b04a1a9611a214a20bac42"
    }
  }
}
```

Also the Swagger file for Grouper WS is a training file. Here is a screenshot of the GPT configuration
