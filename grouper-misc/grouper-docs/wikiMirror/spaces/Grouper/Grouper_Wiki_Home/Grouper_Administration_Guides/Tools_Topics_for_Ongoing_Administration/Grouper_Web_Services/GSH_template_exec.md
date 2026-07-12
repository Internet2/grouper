---
title: "GSH template exec"
space: Grouper
pageId: 28548352
version: 5
lastUpdated: 2026-07-01T05:44:42.853Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548352/GSH+template+exec
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Execute a GSH template via web service. v2.5.43+

#### Features

- Identify the template by ID
- Specify the owner object (e.g. folder)
- Can actAs another user
- This is only available with REST/JSON (not SOAP, not XML, etc)

#### GSH template exec

- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getMembers-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup:A-edu.internet2.middleware.grouper.ws.member.WsMemberFilter-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.Field-boolean-boolean-java.lang.String:A-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-java.lang.String:A-java.sql.Timestamp-java.sql.Timestamp-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-) (click on getMembers), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getMembers-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersRequest-) (click on getMembers)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v1_3_000/groups/aStem%3AaGroup
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/gshTemplate/WsRestGshTemplateExecRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGshTemplateExecResult.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGshTemplateExecResult.WsGshTemplateExecResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/executeGshTemplate/) (all files without "Lite" in them, click on "download" to see files)

#### Example of sending JSON output in an output line

```
POST https://grouperWs.school.edu/grouper-ws/servicesRest/2.6.0/gshTemplateExec
Content-Type: application/json
Authorization: sas9f8d7sa9df87asd98f

{
  "WsRestGshTemplateExecRequest":{
    "ownerStemLookup":{
      "stemName":"penn:etc:templates:membershipCount"
    },
    "ownerType":"stem",
    "configId":"membershipCount",
    "wsInput": {},  // new in v4.9.4+ and v5.6.1+. arbitrary input based on the template and its needs
    "inputs":[
      {
        "name":"gsh_input_groupName",
        "value":"test:testGroup"
      }
    ]
  }
}

RESPONSE
STATUS: 200
x-grouper-resultcode: SUCCESS
x-grouper-resultcode2: NONE
x-grouper-success: T
{
   "WsGshTemplateExecResult":{
      "resultMetadata":{
         "success":"T",
         "resultCode":"SUCCESS",
         "resultMessage":"Success for: clientVersion: 2.6.0, configId: membershipCount, ownerType: stem , inputs: Array size: 1: [0]: edu.internet2.middleware.grouper.ws.coresoap.WsGshTemplateInput@106d7e5e\n\n, actAsSubject: null, paramNames: \n, params: null"
      },
      "gshOutputLines":[
         {
            "messageType":"success",
            "text":"{\"totalMembershipCount\":3,\"immediateMembershipCount\":3}"
         }
      ],
      "responseMetadata":{
         "serverVersion":"2.5.55",
         "resultWarnings":", Client version: 2.6.0 is greater than (major/minor) server version: 2.5.55, Client version: 2.6.0 is greater than (major/minor) server version: 2.5.55",
         "millis":"4046"
      },
      "gshValidationLines":[
         
      ],
      "transaction":true
   }
}

```
