---
title: "Get Members"
space: Grouper
pageId: 28547469
version: 13
lastUpdated: 2026-07-01T05:46:42.535Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547469/Get+Members
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Get members will retrieve subjects assigned to a group.

#### Features

- Can base member list based on memberfilter (e.g. All, Immediate, Effective)
- Lookup subjects by subject lookup (by id, source, identifier, etc)
- Lookup groups by group lookup (by name or uuid)
- Returns group / subject information, can be detailed or not
- Can actAs another user
- For 2.0+, you can pass in pointInTimeFrom and pointInTimeTo to get the member list at a certain point in time in the past, or in a date range. This should be formatted: yyyy/MM/dd HH:mm:ss.SSS
- In 2.1.3+ you can [page or sort the results](https://bugs.internet2.edu/jira/browse/GRP-845)

#### Get members Lite service

- Accepts one group to get members for
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getMembersLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.member.WsMemberFilter-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.Field-boolean-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.sql.Timestamp-java.sql.Timestamp-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-) (click on getMembersLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getMembersLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersLiteRequest-) (click on getMembersLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): GET /grouper-ws/servicesRest/v1_3_000/groups/aStem%3AaGroup/members
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/member/WsRestGetMembersLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetMembersLiteResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetMembersLiteResult.WsGetMembersLiteResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getMembers/) (all files with "Lite" in them, click on "download" to see file)

#### Get members service

- Accepts multiple groups to retrieve lists of lists of subjects
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getMembers-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup:A-edu.internet2.middleware.grouper.ws.member.WsMemberFilter-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.Field-boolean-boolean-java.lang.String:A-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-java.lang.String:A-java.sql.Timestamp-java.sql.Timestamp-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-) (click on getMembers), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getMembers-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersRequest-) (click on getMembers)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v1_3_000/groups/aStem%3AaGroup
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/member/WsRestGetMembersRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetMembersResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetMembersResults.WsGetMembersResultsCode.html), [response codes for each assignment](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetMembersResult.WsGetMembersResultCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getMembers/) (all files without "Lite" in them, click on "download" to see files)

#### Example get netIds from subject source

- Get members from a Group in json for a certain source and include netId in response
- [https://grouperws.school.edu/grouper-ws/servicesRest/v2_6_000/groups/penn%3Aisc%3Aait%3Aapps%3Aopenshift%3Aservice%3AnonprodPolicy%3Aroot%3Aapp%3Acim-bridge%3AXCat2%3Ais_student/members?wsLiteObjectType=WsRestGetMembersLiteRequest&sourceIds=pennperson&subjectAttributeNames=pennname](https://grouperws.school.edu/grouper-ws/servicesRest/v2_6_000/groups/penn%3Aisc%3Aait%3Aapps%3Aopenshift%3Aservice%3AnonprodPolicy%3Aroot%3Aapp%3Acim-bridge%3AXCat2%3Ais_student/members?wsLiteObjectType=WsRestGetMembersLiteRequest&sourceIds=pennperson&subjectAttributeNames=pennname)
- Response
  
  
  ```
  {
     "WsGetMembersLiteResult":{
        "resultMetadata":{
           "success":"T",
           "resultCode":"SUCCESS",
           "resultMessage":"Success for: clientVersion: 2.6.0, wsGroupLookups: Array size: 1: [0]: WsGroupLookup[pitGroups=[],groupName=penn:isc:ait:apps:openshift:service:nonprodPolicy:root:app:cim-bridge:XCat2:is_student]\n\n, memberFilter: All, includeSubjectDetail: false, actAsSubject: null, fieldName: null, subjectAttributeNames: Array size: 1: [0]: pennname\n\n, paramNames: \n, params: null\n, sourceIds: Array size: 1: [0]: pennperson\n\n, pointInTimeFrom: null, pointInTimeTo: null, pageSize: null, pageNumber: null, sortString: null, ascending: null"
        },
        "wsGroup":{
           "extension":"is_student",
           "displayName":"penn:isc:ait:apps:openshift:service:nonprodPolicy:root:app:cim-bridge:XCat2:is_student",
           "uuid":"82799bf5a1694d9c811e53b2c9ff52ed",
           "enabled":"T",
           "displayExtension":"is_student",
           "name":"penn:isc:ait:apps:openshift:service:nonprodPolicy:root:app:cim-bridge:XCat2:is_student",
           "typeOfGroup":"group",
           "idIndex":"559379"
        },
        "subjectAttributeNames":[
           "pennname"
        ],
        "responseMetadata":{
           "serverVersion":"2.5.55",
           "resultWarnings":", Client version: 2.6.0 is greater than (major/minor) server version: 2.5.55",
           "millis":"69"
        },
        "wsSubjects":[
           {
              "sourceId":"pennperson",
              "success":"T",
              "attributeValues":[
                   "wabe"
              ],
              "name":"Terry Johnson",
              "resultCode":"SUCCESS",
              "id":"212346",
              "memberId":"bf1e6c3b068147c9a7c90c252d4cf161"
           },
           {
              "sourceId":"pennperson",
              "success":"T",
              "attributeValues":[
                 "cabd"
              ],
              "name":"Daniel J Johnson",
              "resultCode":"SUCCESS",
              "id":"312347",
              "memberId":"206934ec147e4a4eb73f3367e1dba3f9"
           },
           {
              "sourceId":"pennperson",
              "success":"T",
              "attributeValues":[
                 "mabs"
              ],
              "name":"Anome Johnson",
              "resultCode":"SUCCESS",
              "id":"312347",
              "memberId":"9ec3b16b-03d3-4742-be63-bbb9c2526631"
           },
           {
              "sourceId":"pennperson",
              "success":"T",
              "attributeValues":[
                 "wabh"
              ],
              "name":"William G Johnson",
              "resultCode":"SUCCESS",
              "id":"612341",
              "memberId":"096da10b-0fc8-4a51-bfbc-deef91707ac6"
           }
        ]
     }
  }
  
  
  
  ```
