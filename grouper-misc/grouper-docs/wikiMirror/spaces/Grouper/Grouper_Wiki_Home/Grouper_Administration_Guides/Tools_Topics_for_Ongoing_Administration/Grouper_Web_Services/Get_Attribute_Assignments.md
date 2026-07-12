---
title: "Get Attribute Assignments"
space: Grouper
pageId: 28548305
version: 13
lastUpdated: 2026-07-01T05:44:52.614Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548305/Get+Attribute+Assignments
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Get attribute assignments. These [attributes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework) can be on groups, stems, members, memberships (immediate or any), or attribute definitions. If you want to retrieve attribute assignments assigned to other attributes, then pass a flag to the assignment lookup to include assignments on the returned assignments.

You can lookup attributes by attribute definition, attribute definition name, attribute assign id, or the owner lookup (e.g. group name or stem uuid).

All returned attribute assignments will be filtered for security based on the logged in or acted as user (security rules are on [attribute framework wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework))

The returned data will include the attribute assignments, value(s) on those assignments, and a normalized list of references (owner objects e.g. group/stem/etc, attribute definitions, attribute names, etc)

You can lookup assignments by multiple owners, definitions, etc

AttributeAssignType is a required field, but cannot be an assignment on assignment, must be: group, member, stem, any_mem, imm_mem, attr_def

In 2.1.1+ you can search by more params:

```
You can search by value (you need to pass the value type and value), You can also search for assignments on assignments,
and include the assignments from assignments (e.g. to know which group it is assigned to). You can filter by attributeDefType.
In all, these are the new params for lite or non-lite operations including grouper client:

attributeDefValueType: required if sending theValue, can be: floating, integer, memberId, string, timestamp
theValue: value if you are passing in one attributeDefNameLookup
includeAssignmentsFromAssignments: T|F if you are finding an assignment that is an assignmentOnAssignment, then get the assignment which tells you the owner as well
attributeDefType: null for all, or specify an AttributeDefType e.g. attr, limit, service, type, limit, perm
wsAssignAssignOwnerAttributeAssignLookups: if looking for assignments on assignments, this is the assignment the assignment is assigned to
wsAssignAssignOwnerAttributeDefLookups: if looking for assignments on assignments, this is the attribute definition of the assignment the assignment is assigned to
wsAssignAssignOwnerAttributeDefNameLookups: if looking for assignments on assignments, this is the attribute def name of the assignment the assignment is assigned to
wsAssignAssignOwnerActions: if looking for assignments on assignments, this are the actions of the assignment the assignment is assigned to

```

#### Features

- Can base attribute assign list based on action, active, etc
- Lookup owner or other objects by object lookup (by id, name, etc)
- Returns group / subject information, can be detailed or not
- Can actAs another user

#### Get attribute assignments lite service

- Accepts one group, or one subject, or stem, etc to get attribute assignments for
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getAttributeAssignmentsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.attr.assign.AttributeAssignType-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-boolean-java.lang.String-java.lang.String-java.lang.String-boolean-java.lang.String-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.attr.AttributeDefValueType-java.lang.Object-boolean-edu.internet2.middleware.grouper.attr.AttributeDefType-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on getAttributeAssignmentsLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getAttributeAssignmentsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsLiteRequest-) (click on getAttributeAssignmentsLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A):
  
  - GET /grouper-ws/servicesRest/v1_6_000/attributeAssignments
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestGetAttributeAssignmentsLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/soap/WsGetAttributeAssignmentsResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetAttributeAssignmentsResults.WsGetAttributeAssignmentsResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getAttributeAssignments/) (all files with "Lite" in them, click on "download" to see file)

#### Get attribute assignments service

- Accepts multiple groups or subjects or membershipIds (or combination) etc to retrieve lists of attribute assignments
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getAttributeAssignments-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.attr.assign.AttributeAssignType-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsMembershipLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsMembershipAnyLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup:A-java.lang.String:A-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-boolean-java.lang.String:A-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-java.lang.String-edu.internet2.middleware.grouper.attr.AttributeDefValueType-java.lang.Object-boolean-edu.internet2.middleware.grouper.attr.AttributeDefType-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup:A-java.lang.String:A-) (click on getAttributeAssignments), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getAttributeAssignments-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsRequest-) (click on getAttributeAssignments)
- REST request (colon is escaped to %3A):
  
  - POST /grouper-ws/servicesRest/v1_6_000/attributeAssignments
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestGetAttributeAssignmentsRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/soap/WsGetAttributeAssignmentsResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetAttributeAssignmentsResults.WsGetAttributeAssignmentsResultsCode.html)
- Returns an overall status
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getAttributeAssignments/) (all files without "Lite" in them, click on "download" to see files)

#### Example getting groups with attribute and value assigned

```
[mchyzer@flash pennGroupsClient-2.6.0]$ java -jar grouperClient.jar --operation=getAttributeAssignmentsWs --attributeAssignType=group --attributeDefNameNames=test:testGroupAttrValue --value=someVal --attributeDefValueType=string --debug=true
Reading resource: grouper.client.properties, from: /home/mchyzer/grouper/pennGroupsClient-2.6.0/grouper.client.properties
WebService: connecting as user: 'fast/medley.isc-seo.upenn.edu'
WebService: connecting to URL: 'https://grouperWs.institution.edu/grouperWs/servicesRest/2.6.19/attributeAssignments'

################ REQUEST START (indented) ###############

POST /grouperWs/servicesRest/2.6.19/attributeAssignments HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperWs.apps.upenn.edu:-1
Content-Length: 191
Content-Type: application/json; charset=UTF-8

{
  "WsRestGetAttributeAssignmentsRequest":{
    "attributeDefValueType":"string",
    "theValue":"someVal",
    "attributeAssignType":"group",
    "wsAttributeDefNameLookups":[
      {
        "name":"test:testGroupAttrValue"
      }
    ]
  }
}

################ REQUEST END ###############

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Date: Thu, 14 Mar 2024 00:48:17 GMT
Content-Type: application/json;charset=UTF-8
Content-Length: 2175
Connection: close
Server: Apache/2.4.37 (rocky) OpenSSL/1.1.1k
Strict-Transport-Security: max-age=15768000
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE

{
  "WsGetAttributeAssignmentsResults":{
    "resultMetadata":{
      "success":"T",
      "resultCode":"SUCCESS",
      "resultMessage":", Found 1 results."
    },
    "wsAttributeAssigns":[
      {
        "attributeAssignDelegatable":"FALSE",
        "disallowed":"F",
        "createdOn":"2024/03/12 15:44:47.009",
        "enabled":"T",
        "attributeAssignType":"group",
        "attributeDefId":"89eb153583944075ba5edd5b47d42939",
        "lastUpdated":"2024/03/12 15:44:47.009",
        "attributeAssignActionId":"ef30fa3e2a9348e38bfeaa2898f85979",
        "ownerGroupName":"test:testGroup3",
        "id":"263ab9ef1f504176a67ca598b1900abe",
        "wsAttributeAssignValues":[
          {
            "id":"cfdf9c0651194fa68fe8cea1063da040",
            "valueSystem":"someVal"
          }
        ]
        ,
        "ownerGroupId":"6f6c6b952dcd4b328d436219e9055a44",
        "attributeDefName":"test:testGroupAttrValueDef",
        "attributeDefNameName":"test:testGroupAttrValue",
        "attributeAssignActionName":"assign",
        "attributeDefNameId":"db90aeca0830454f84eec65ab0f8648e",
        "attributeAssignActionType":"immediate"
      }
    ]
    ,
    "wsMemberships":[
    ]
    ,
    "wsStems":[
    ]
    ,
    "wsAttributeDefs":[
      {
        "attributeDefType":"attr",
        "assignToAttributeDef":"F",
        "assignToStemAssignment":"F",
        "extension":"testGroupAttrValueDef",
        "assignToMemberAssignment":"F",
        "assignToEffectiveMembership":"F",
        "uuid":"89eb153583944075ba5edd5b47d42939",
        "assignToImmediateMembershipAssignment":"F",
        "assignToEffectiveMembershipAssignment":"F",
        "assignToStem":"F",
        "assignToGroupAssignment":"F",
        "assignToMember":"F",
        "multiAssignable":"F",
        "valueType":"string",
        "name":"test:testGroupAttrValueDef",
        "assignToAttributeDefAssignment":"F",
        "idIndex":"1000026",
        "multiValued":"F",
        "assignToGroup":"F",
        "assignToImmediateMembership":"F"
      }
    ]
    ,
    "responseMetadata":{
      "serverVersion":"4.11.2",
      "millis":"28"
    },
    "wsGroups":[
      {
        "extension":"testGroup3",
        "displayName":"test:testGroup3",
        "uuid":"6f6c6b952dcd4b328d436219e9055a44",
        "enabled":"T",
        "displayExtension":"testGroup3",
        "name":"test:testGroup3",
        "typeOfGroup":"group",
        "idIndex":"273142"
      }
    ]
    ,
    "wsAttributeDefNames":[
      {
        "attributeDefId":"89eb153583944075ba5edd5b47d42939",
        "displayExtension":"testGroupAttrValue",
        "extension":"testGroupAttrValue",
        "displayName":"test:testGroupAttrValue",
        "name":"test:testGroupAttrValue",
        "attributeDefName":"test:testGroupAttrValueDef",
        "idIndex":"1021690",
        "uuid":"db90aeca0830454f84eec65ab0f8648e"
      }
    ]
    ,
    "wsSubjects":[
    ]
  }
}

################ RESPONSE END ###############

```

#### Example of getting membership attribute assignments by group and subject

```
[mchyzer@flash pennGroupsClient-2.6.0]$ java -jar grouperClient-2.6.19.jar --operation=getAttributeAssignmentsWs --attributeAssignType=any_mem  --ownerMembershipAny0GroupName=test:isc:astt:chris:dateMembership:testMfa --ownerMembershipAny0SubjectId=10021368 --ownerMembershipAny0SubjectSource=pennperson --debug=true

Reading resource: grouper.client.properties, from: /home/mchyzer/grouper/pennGroupsClient-2.6.0/grouper.client.properties
WebService: connecting as user: 'abc'
WebService: connecting to URL: 'https://grouperws.institution.edu/grouperWs/servicesRest/2.6.19/attributeAssignments'

################ REQUEST START (indented) ###############

POST /grouperWs/servicesRest/2.6.19/attributeAssignments HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperWs.apps.upenn.edu:-1
Content-Length: 257
Content-Type: application/json; charset=UTF-8

{
  "WsRestGetAttributeAssignmentsRequest":{
    "attributeAssignType":"any_mem",
    "wsOwnerMembershipAnyLookups":[
      {
        "wsGroupLookup":{
          "groupName":"test:isc:astt:chris:dateMembership:testMfa"
        },
        "wsSubjectLookup":{
          "subjectId":"10021368",
          "subjectSourceId":"pennperson"
        }
      }
    ]
  }
}

################ REQUEST END ###############

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Date: Wed, 30 Aug 2023 01:31:02 GMT
Content-Type: application/json;charset=UTF-8
Content-Length: 2640
Connection: close
Server: Apache/2.4.6 (CentOS) OpenSSL/1.0.2k-fips
Strict-Transport-Security: max-age=15768000
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE

{
  "WsGetAttributeAssignmentsResults":{
    "resultMetadata":{
      "success":"T",
      "resultCode":"SUCCESS",
      "resultMessage":", Found 1 results."
    },
    "wsAttributeAssigns":[
      {
        "attributeAssignDelegatable":"FALSE",
        "ownerMemberSourceId":"pennperson",
        "disallowed":"F",
        "createdOn":"2023/08/29 21:16:45.695",
        "enabled":"T",
        "attributeAssignType":"any_mem",
        "attributeDefId":"25ff4cb572af452f938a7f227b798cf4",
        "lastUpdated":"2023/08/29 21:16:45.695",
        "attributeAssignActionId":"9650f306626f4fa6a47741e69859727e",
        "ownerGroupName":"test:isc:astt:chris:dateMembership:testMfa",
        "id":"c190fd14f71945e697890e63f7e7f0db",
        "wsAttributeAssignValues":[
          {
            "id":"403d7f537831469bac113a20fd36cdee",
            "valueSystem":"2023/10/01 00:00:00.000"
          }
        ]
        ,
        "ownerGroupId":"107f17444e8a419cadfed6063e2c6632",
        "ownerMemberSubjectId":"10021368",
        "ownerMemberId":"c5c8ef55-76be-4b0d-9910-9efbf465cff3",
        "attributeDefName":"test:isc:astt:chris:dateMembership:dateMembershipDef",
        "attributeDefNameName":"test:isc:astt:chris:dateMembership:dateMembership",
        "attributeAssignActionName":"assign",
        "attributeDefNameId":"d6297bfdaeb94b8b8b5051e88004e4e0",
        "attributeAssignActionType":"immediate"
      }
    ]
    ,
    "wsMemberships":[
    ]
    ,
    "wsStems":[
    ]
    ,
    "wsAttributeDefs":[
      {
        "attributeDefType":"attr",
        "assignToAttributeDef":"F",
        "assignToStemAssignment":"F",
        "extension":"dateMembershipDef",
        "assignToMemberAssignment":"F",
        "assignToEffectiveMembership":"F",
        "uuid":"25ff4cb572af452f938a7f227b798cf4",
        "assignToImmediateMembershipAssignment":"F",
        "assignToEffectiveMembershipAssignment":"F",
        "assignToStem":"F",
        "assignToGroupAssignment":"F",
        "assignToMember":"F",
        "multiAssignable":"F",
        "valueType":"timestamp",
        "name":"test:isc:astt:chris:dateMembership:dateMembershipDef",
        "assignToAttributeDefAssignment":"F",
        "idIndex":"1000001",
        "multiValued":"F",
        "assignToGroup":"F",
        "assignToImmediateMembership":"F"
      }
    ]
    ,
    "responseMetadata":{
      "serverVersion":"2.6.19",
      "millis":"137"
    },
    "wsGroups":[
      {
        "extension":"testMfa",
        "displayName":"test:isc:astt:chris:dateMembership:testMfa",
        "uuid":"107f17444e8a419cadfed6063e2c6632",
        "enabled":"T",
        "displayExtension":"testMfa",
        "name":"test:isc:astt:chris:dateMembership:testMfa",
        "typeOfGroup":"group",
        "idIndex":"1010171"
      }
    ]
    ,
    "wsAttributeDefNames":[
      {
        "attributeDefId":"25ff4cb572af452f938a7f227b798cf4",
        "displayExtension":"dateMembership",
        "extension":"dateMembership",
        "displayName":"test:isc:astt:chris:dateMembership:dateMembership",
        "name":"test:isc:astt:chris:dateMembership:dateMembership",
        "attributeDefName":"test:isc:astt:chris:dateMembership:dateMembershipDef",
        "idIndex":"1010853",
        "uuid":"d6297bfdaeb94b8b8b5051e88004e4e0"
      }
    ]
    ,
    "wsSubjects":[
      {
        "sourceId":"pennperson",
        "success":"T",
        "name":"Chris Hyzer",
        "resultCode":"SUCCESS",
        "id":"10021368"
      }
    ]
  }
}

################ RESPONSE END ###############

Output template: Index: ${index}: attributeAssignType: ${wsAttributeAssign.attributeAssignType}, owner: ${ownerName}, attributeDefNameName: ${wsAttributeDefName.name}, action: ${wsAttributeAssign.attributeAssignActionName}, values: ${valuesString}, enabled: ${wsAttributeAssign.enabled}, id: ${wsAttributeAssign.id}, available variables: wsGetAttributeAssignmentsResults, grouperClientUtils, index, wsAttributeAssignment
Index: 0: attributeAssignType: any_mem, owner: test:isc:astt:chris:dateMembership:testMfa - pennperson - 10021368, attributeDefNameName: test:isc:astt:chris:dateMembership:dateMembership, action: assign, values: 2023/10/01 00:00:00.000, enabled: T, id: c190fd14f71945e697890e63f7e7f0db
Elapsed time: 1302ms
[mchyzer@flash pennGroupsClient-2.6.0]$ 
```
