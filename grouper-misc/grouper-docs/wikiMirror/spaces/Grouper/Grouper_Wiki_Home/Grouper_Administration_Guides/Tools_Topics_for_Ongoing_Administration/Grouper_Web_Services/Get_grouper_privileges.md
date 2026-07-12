---
title: "Get grouper privileges"
space: Grouper
pageId: 28547854
version: 9
lastUpdated: 2026-07-01T05:46:01.870Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547854/Get+grouper+privileges
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

"[Get grouper privileges](https://bugs.internet2.edu/jira/browse/GRP-179)" will retrieve the privileges for a subject and or (group or stem). If you dont specify the privilege name, you will get all permissions for the user and or (group or stem). If you specify the subject, (group or stem) and privilege you are looking for, you will get also get the response in the return code (which is an HTTP header). You must specify a subject or stem or group. You cannot specify a group and a stem at once.

#### Features

- Will only get the privileges the user (or actAs) is allowed to see
- Lookup subjects/members by subject lookup (by id, source, identifier, etc)
- Lookup groups/stems by group lookup or stem lookup (name or uuid)
- Returns subject information of the subject
- Returns the group or stem information
- Can actAs another user

#### Get grouper privileges Lite service

- Accepts one subject and one group or stem lookup
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getGrouperPrivilegesLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.privs.PrivilegeType-edu.internet2.middleware.grouper.privs.Privilege-java.lang.String-java.lang.String-java.lang.String-boolean-java.lang.String-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on getGrouperPrivileges), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getGrouperPrivilegesLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGrouperPrivilegesLiteRequest-) (click on getGrouperPrivileges)
- For REST, a request body is required (probably via POST)
- REST request (colon is escaped to %3A): GET /grouper-ws/servicesRest/v1_4_000/grouperPrivileges
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/group/WsRestGetGrouperPrivilegesLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/soap/WsGetGrouperPrivilegesLiteResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetGrouperPrivilegesLiteResult.WsGetGrouperPrivilegesLiteResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getGrouperPrivileges/) (all files with "Lite" in them, click on "download" to see file)

Example grouper client output

```
C:\temp>java -jar grouperClient.jar --operation=getGrouperPrivilegesLiteWs --groupName=aStem:aGroup
Index 0: success: T: code: SUCCESS: group: aStem:aGroup: subject: 10021368: access: admin
Index 1: success: T: code: SUCCESS: group: aStem:aGroup: subject: 10021368: access: read
Index 2: success: T: code: SUCCESS: group: aStem:aGroup: subject: 10021368: access: update
Index 3: success: T: code: SUCCESS: group: aStem:aGroup: subject: 10021368: access: view
Index 4: success: T: code: SUCCESS: group: aStem:aGroup: subject: GrouperAll: access: read
Index 5: success: T: code: SUCCESS: group: aStem:aGroup: subject: GrouperAll: access: view
Index 6: success: T: code: SUCCESS: group: aStem:aGroup: subject: GrouperSystem: access: admin
Index 7: success: T: code: SUCCESS: group: aStem:aGroup: subject: GrouperSystem: access: read
Index 8: success: T: code: SUCCESS: group: aStem:aGroup: subject: GrouperSystem: access: update
Index 9: success: T: code: SUCCESS: group: aStem:aGroup: subject: GrouperSystem: access: view
Index 10: success: T: code: SUCCESS: group: aStem:aGroup: subject: test.subject.0: access: admin
Index 11: success: T: code: SUCCESS: group: aStem:aGroup: subject: test.subject.0: access: read
Index 12: success: T: code: SUCCESS: group: aStem:aGroup: subject: test.subject.0: access: view

C:\temp>java -jar grouperClient.jar --operation=getGrouperPrivilegesLiteWs --subjectId=10021368
Index 0: success: T: code: SUCCESS: stem: aStem: subject: 10021368: naming: create
Index 1: success: T: code: SUCCESS: stem: aStem: subject: 10021368: naming: stem
Index 2: success: T: code: SUCCESS: stem: aStem:aStem0: subject: 10021368: naming: create
Index 3: success: T: code: SUCCESS: stem: aStem:aStem0: subject: 10021368: naming: stem
Index 4: success: T: code: SUCCESS: group: aStem:aGroup: subject: 10021368: access: admin
Index 5: success: T: code: SUCCESS: group: aStem:aGroup: subject: 10021368: access: read
Index 6: success: T: code: SUCCESS: group: aStem:aGroup: subject: 10021368: access: update
Index 7: success: T: code: SUCCESS: group: aStem:aGroup: subject: 10021368: access: view
Index 8: success: T: code: SUCCESS: group: aStem:activeEmployee: subject: 10021368: access: admin
Index 9: success: T: code: SUCCESS: group: aStem:activeEmployee: subject: 10021368: access: read
Index 10: success: T: code: SUCCESS: group: aStem:activeEmployee: subject: 10021368: access: update
Index 11: success: T: code: SUCCESS: group: aStem:activeEmployee: subject: 10021368: access: view
Index 12: success: T: code: SUCCESS: group: aStem:activeStudent: subject: 10021368: access: admin
Index 13: success: T: code: SUCCESS: group: aStem:activeStudent: subject: 10021368: access: read
Index 14: success: T: code: SUCCESS: group: aStem:activeStudent: subject: 10021368: access: update
Index 15: success: T: code: SUCCESS: group: aStem:activeStudent: subject: 10021368: access: view
Index 16: success: T: code: SUCCESS: group: etc:sysadmingroup: subject: 10021368: access: admin
Index 17: success: T: code: SUCCESS: group: etc:sysadmingroup: subject: 10021368: access: read
Index 18: success: T: code: SUCCESS: group: etc:sysadmingroup: subject: 10021368: access: update
Index 19: success: T: code: SUCCESS: group: etc:sysadmingroup: subject: 10021368: access: view
Index 20: success: T: code: SUCCESS: group: etc:webServiceActAsGroup: subject: 10021368: access: admin
Index 21: success: T: code: SUCCESS: group: etc:webServiceActAsGroup: subject: 10021368: access: read
Index 22: success: T: code: SUCCESS: group: etc:webServiceActAsGroup: subject: 10021368: access: update
Index 23: success: T: code: SUCCESS: group: etc:webServiceActAsGroup: subject: 10021368: access: view
Index 24: success: T: code: SUCCESS: group: etc:webServiceClientUsers: subject: 10021368: access: admin
Index 25: success: T: code: SUCCESS: group: etc:webServiceClientUsers: subject: 10021368: access: read
Index 26: success: T: code: SUCCESS: group: etc:webServiceClientUsers: subject: 10021368: access: update
Index 27: success: T: code: SUCCESS: group: etc:webServiceClientUsers: subject: 10021368: access: view
Index 28: success: T: code: SUCCESS: group: penn:etc:sysAdminGroup: subject: 10021368: access: admin
Index 29: success: T: code: SUCCESS: group: penn:etc:sysAdminGroup: subject: 10021368: access: read
Index 30: success: T: code: SUCCESS: group: penn:etc:sysAdminGroup: subject: 10021368: access: update
Index 31: success: T: code: SUCCESS: group: penn:etc:sysAdminGroup: subject: 10021368: access: view
Index 32: success: T: code: SUCCESS: group: penn:etc:userInterfaceUsers: subject: 10021368: access: admin
Index 33: success: T: code: SUCCESS: group: penn:etc:userInterfaceUsers: subject: 10021368: access: read
Index 34: success: T: code: SUCCESS: group: penn:etc:userInterfaceUsers: subject: 10021368: access: update
Index 35: success: T: code: SUCCESS: group: penn:etc:userInterfaceUsers: subject: 10021368: access: view
Index 36: success: T: code: SUCCESS: group: penn:etc:webServiceActAsGroup: subject: 10021368: access: admin
Index 37: success: T: code: SUCCESS: group: penn:etc:webServiceActAsGroup: subject: 10021368: access: read
Index 38: success: T: code: SUCCESS: group: penn:etc:webServiceActAsGroup: subject: 10021368: access: update
Index 39: success: T: code: SUCCESS: group: penn:etc:webServiceActAsGroup: subject: 10021368: access: view
Index 40: success: T: code: SUCCESS: group: penn:etc:webServiceClientUsers: subject: 10021368: access: admin
Index 41: success: T: code: SUCCESS: group: penn:etc:webServiceClientUsers: subject: 10021368: access: read
Index 42: success: T: code: SUCCESS: group: penn:etc:webServiceClientUsers: subject: 10021368: access: update
Index 43: success: T: code: SUCCESS: group: penn:etc:webServiceClientUsers: subject: 10021368: access: view

C:\temp>java -jar grouperClient.jar --operation=getGrouperPrivilegesLiteWs --stemName=aStem
Index 0: success: T: code: SUCCESS: stem: aStem: subject: 10021368: naming: create
Index 1: success: T: code: SUCCESS: stem: aStem: subject: 10021368: naming: stem
Index 2: success: T: code: SUCCESS: stem: aStem: subject: GrouperSystem: naming: stem
Index 3: success: T: code: SUCCESS: stem: aStem: subject: test.subject.0: naming: create
Index 4: success: T: code: SUCCESS: stem: aStem: subject: test.subject.0: naming: stem

C:\temp>java -jar grouperClient.jar --operation=getGrouperPrivilegesLiteWs --subjectId=10021368 --privilegeType=naming
Index 0: success: T: code: SUCCESS: stem: aStem: subject: 10021368: naming: create
Index 1: success: T: code: SUCCESS: stem: aStem: subject: 10021368: naming: stem
Index 2: success: T: code: SUCCESS: stem: aStem:aStem0: subject: 10021368: naming: create
Index 3: success: T: code: SUCCESS: stem: aStem:aStem0: subject: 10021368: naming: stem

C:\temp>java -jar grouperClient.jar --operation=getGrouperPrivilegesLiteWs --stemName=aStem --privilegeName=create
Index 0: success: T: code: SUCCESS: stem: aStem: subject: 10021368: naming: create
Index 1: success: T: code: SUCCESS: stem: aStem: subject: test.subject.0: naming: create

C:\temp>java -jar grouperClient.jar --operation=getGrouperPrivilegesLiteWs --stemName=aStem --privilegeName=create --subjectId=10021368
Index 0: success: T: code: SUCCESS_ALLOWED: stem: aStem: subject: 10021368: naming: create

```

Here is an example to get all groups a user has UPDATE on... note, you would need to call again for ADMIN as well since admins can update, and calling credential needs to be all powerful or have ADMIN on whatever groups are intended to be returned

```
[mchyzer@flash pennGroupsClient-2.6.0]$ java -jar grouperClient-2.6.19.jar --operation=getGrouperPrivilegesLiteWs --subjectIdentifier=kwilso --privilegeName=update --debug=true
Reading resource: grouper.client.properties, from: /home/mchyzer/grouper/pennGroupsClient-2.6.0/grouper.client.properties
WebService: connecting as user: 'fast/medley.isc-seo.upenn.edu'
WebService: connecting to URL: 'https://server.school.edu/grouperWs/servicesRest/2.6.19/grouperPrivileges'

################ REQUEST START (indented) ###############

POST /grouperWs/servicesRest/2.6.19/grouperPrivileges HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperWs.apps.upenn.edu:-1
Content-Length: 97
Content-Type: application/json; charset=UTF-8

{
  "WsRestGetGrouperPrivilegesLiteRequest":{
    "subjectIdentifier":"kwilso",
    "privilegeName":"update"
  }
}

################ REQUEST END ###############

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Date: Thu, 16 Feb 2023 20:25:45 GMT
Content-Type: application/json;charset=UTF-8
Content-Length: 814
Connection: close
Server: Apache/2.4.6 (CentOS) OpenSSL/1.0.2k-fips
Strict-Transport-Security: max-age=15768000
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE

{
  "WsGetGrouperPrivilegesLiteResult":{
    "resultMetadata":{
      "success":"T",
      "resultCode":"SUCCESS"
    },
    "responseMetadata":{
      "serverVersion":"2.6.19",
      "millis":"7862"
    },
    "privilegeResults":[
      {
        "revokable":"T",
        "wsGroup":{
          "extension":"testGroup",
          "displayName":"test:testGroup",
          "description":"testGroup",
          "alternateName":"testdd:testGroupdd",
          "uuid":"dbfa18c3-a025-47b6-a9a0-be5ac02e8270",
          "enabled":"T",
          "displayExtension":"testGroup",
          "name":"test:testGroup",
          "typeOfGroup":"group",
          "idIndex":"197979"
        },
        "ownerSubject":{
          "sourceId":"pennperson",
          "success":"T",
          "name":"Katherine R Wilson",
          "resultCode":"SUCCESS",
          "id":"89505485"
        },
        "allowed":"T",
        "wsSubject":{
          "sourceId":"pennperson",
          "identifierLookup":"kwilso",
          "success":"T",
          "name":"Katherine R Wilson",
          "resultCode":"SUCCESS",
          "id":"89505485"
        },
        "privilegeType":"access",
        "privilegeName":"update"
      }
    ]
  }
}

################ RESPONSE END ###############

Output template: Index ${index}: success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: ${objectType}: ${objectName}: subject: ${wsSubject.id}: ${wsGrouperPrivilegeResult.privilegeType}: ${wsGrouperPrivilegeResult.privilegeName}, available variables: wsGetGrouperPrivilegesLiteResult, grouperClientUtils, resultMetadata, index, wsGrouperPrivilegeResult, wsSubject, wsGroup, wsStem, objectType, objectName
Index 0: success: T: code: SUCCESS: group: test:testGroup: subject: 89505485: access: update
Elapsed time: 9589ms
[mchyzer@flash pennGroupsClient-2.6.0]$ 
```
