---
title: "Assign Attributes Batch"
space: Grouper
pageId: 28547447
version: 12
lastUpdated: 2026-07-01T05:46:45.631Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547447/Assign+Attributes+Batch
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

This operation is available in servers v2.1.2+. Assign or remove attributes and values of attribute assignments. These attributes can be on groups, stems, members, memberships (immediate or any), attribute definitions, or on assignments of attributes (one level deep). You can pass in multiple of these operations in one batch which can be transactional or not (default). This is what differentiates assignAttributesBatch with assignAttributes. There is no Lite version of this operation, since you can just use assignAttributesLite.

You can lookup attributes by attribute definition name, or attribute definition id

All assignments will be filtered for security based on the logged in or acted as user (security rules are on [attribute framework wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework))

The returned data will include the attribute assignments, value(s) on those assignments, and a normalized list of references (owner objects e.g. group/stem/etc, attribute definitions, attribute names, etc), if things changed or were already assigned, etc

You can assign attributes to one owner, definition, action, etc, though you can pass in multiple assignments.

attributeAssignType is a required field, must be: group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn

attributeAssignOperation is required and is the operation to perform for attribute on owners, from enum AttributeAssignOperation: assign_attr, add_attr, remove_attr, replace_attrs. In this case, assigning an attribute will not assign if already there. add_attr will add this assignment even if it is already there (attribute definition must allow multi assignments)

attributeAssignValueOperation is required if passing values to assign. It is the operation to perform for attribute value on attribute assignments: assign_value, add_value, remove_value, replace_values. Like the attribute assign operation, assign_value will assign if not there, or ignore if already there. add_value will add even if assigned. And replace_values will remove orphans not in the assign list.

#### Features

- Can pass owners, actions, values, etc.
- Lookup owner or other objects by object lookup (by id, name, etc)
- Note, if you have an assignment that is to a group, stem, etc (not on an assignment), then you can pass in another assignment in the batch that refers to the ID of a previous item in the batch. You do this with the WsAttributeAssignLookup.batchIndex (0 indexed). This means you can assign a marker attribute to an owner, along with metadata on that assignment, all in one web service operation.
- Returns group / subject information, can be detailed or not
- Can actAs another user

#### Assign attributes service

- Accepts multiple groups or subjects or membershipIds (or combination) etc, attribute definitions, actions, etc to assign
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#assignAttributesBatch-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeBatchEntry:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-boolean-edu.internet2.middleware.grouper.hibernate.GrouperTransactionType-java.lang.String:A-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on assignAttributesBatch), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#assignAttributesBatch-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesBatchRequest-) (click on assignAttributesBatch)
- REST request (colon is escaped to %3A):
  
  - POST /grouper-ws/servicesRest/v2_1_002/assignAttributes
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAssignAttributesBatchRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignAttributesBatchResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignAttributesBatchResults.WsAssignAttributesBatchResultsCode.html)
- Returns an overall status
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/assignAttributesBatch/) (all files without "Lite" in them, click on "download" to see files)

#### Example 1 effective memberships

Make multiple assignments in a batch

```
[mchyzer@flash pennGroupsClient-2.6.0]$ java -jar grouperClient-2.6.13.jar --operation=assignAttributesBatchWs --entry_0_attributeAssignType=any_mem --entry_0_attributeAssignOperation=assign_attr --entry_0_nameOfAttributeDefName=test:testAttestation:testAttr --entry_0_ownerMembershipAnySubjectIdentifier=mchyzer --entry_0_ownerMembershipAnyGroupName=test:testGroup1 --entry_0_attributeAssignValueOperation=assign_value --entry_0_values0System=hey --entry_1_attributeAssignType=any_mem --entry_1_attributeAssignOperation=assign_attr --entry_1_nameOfAttributeDefName=test:testAttestation:testAttr --entry_1_ownerMembershipAnySubjectIdentifier=kwilso --entry_1_ownerMembershipAnyGroupName=test:testGroup2 --entry_1_attributeAssignValueOperation=assign_value --entry_1_values0System=there --debug=true
Reading resource: grouper.client.properties, from: /home/mchyzer/grouper/pennGroupsClient-2.6.0/grouper.client.properties
WebService: connecting as user: 'abc123.upenn.edu'
WebService: connecting to URL: 'https://server.whatever.edu/grouperWs/servicesRest/2.6.13/attributeAssignments'

################ REQUEST START (indented) ###############

POST /grouperWs/servicesRest/2.6.13/attributeAssignments HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperWs.apps.upenn.edu:-1
Content-Length: 779
Content-Type: application/json; charset=UTF-8

{
  "WsRestAssignAttributesBatchRequest":{
    "wsAssignAttributeBatchEntries":[
      {
        "attributeAssignType":"any_mem",
        "wsAttributeDefNameLookup":{
          "name":"test:testAttestation:testAttr"
        },
        "attributeAssignOperation":"assign_attr",
        "values":[
          {
            "valueSystem":"hey"
          }
        ]
        ,
        "attributeAssignValueOperation":"assign_value",
        "wsOwnerMembershipAnyLookup":{
          "wsGroupLookup":{
            "groupName":"test:testGroup1"
          },
          "wsSubjectLookup":{
            "subjectIdentifier":"mchyzer"
          }
        }
        
      },
      {
        "attributeAssignType":"any_mem",
        "wsAttributeDefNameLookup":{
          "name":"test:testAttestation:testAttr"
        },
        "attributeAssignOperation":"assign_attr",
        "values":[
          {
            "valueSystem":"there"
          }
        ]
        ,
        "attributeAssignValueOperation":"assign_value",
        "wsOwnerMembershipAnyLookup":{
          "wsGroupLookup":{
            "groupName":"test:testGroup2"
          },
          "wsSubjectLookup":{
            "subjectIdentifier":"kwilso"
          }
        }
      }
    ]
  }
}

################ REQUEST END ###############

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Date: Sat, 17 Sep 2022 14:09:53 GMT
Content-Type: application/json;charset=UTF-8
Content-Length: 4504
Connection: close
Server: Apache/2.4.6 (CentOS) OpenSSL/1.0.2k-fips
Strict-Transport-Security: max-age=15768000
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE

{
  "WsAssignAttributesBatchResults":{
    "resultMetadata":{
      "success":"T",
      "resultCode":"SUCCESS",
      "resultMessage":"Success for: clientVersion: 2.6.13, includeSubjectDetail: false, actAsSubject: null, subjectAttributeNames: null\n, paramNames: \n, params: null\n, wsAssignAttributeBatchEntries: 0. wsAssignAttributeBatchEntry: attributeAssignOperation: assign_attr, attributeAssignType: any_mem, attributeAssignValueOperation: assign_value, values: 0. valueSystem: hey, , wsAttributeDefNameLookup: WsAttributeDefNameLookup[pitAttributeDefNames=[],name=test:testAttestation:testAttr], wsOwnerMembershipAnyLookup: WsMembershipAnyLookup[\n  wsGroupLookup=WsGroupLookup[pitGroups=[],groupName=test:testGroup1],\n  wsSubjectLookup=WsSubjectLookup[subjectIdentifier=mchyzer]],"
    },
    "wsAssignAttributeBatchResultArray":[
      {
        "wsAttributeAssignValueResults":[
          {
            "deleted":"F",
            "wsAttributeAssignValue":{
              "id":"46ddf78f20f74103b4ae5e34d206aefa",
              "valueSystem":"hey"
            },
            "changed":"T"
          }
        ]
        ,
        "resultMetadata":{
          "success":"T",
          "resultCode":"SUCCESS",
          "resultMessage":", Found 1 results."
        },
        "deleted":"F",
        "wsAttributeAssigns":[
          {
            "attributeAssignDelegatable":"FALSE",
            "ownerMemberSourceId":"pennperson",
            "disallowed":"F",
            "createdOn":"2022/09/17 10:08:51.811",
            "enabled":"T",
            "attributeAssignType":"any_mem",
            "attributeDefId":"4f7796d2aacf4688bb177fc668be699f",
            "lastUpdated":"2022/09/17 10:08:51.811",
            "attributeAssignActionId":"531b0bee1a5d47d38fce853423ba8612",
            "ownerGroupName":"test:testGroup1",
            "id":"01bc86f79b2d4ff59e7c15fc6fda2102",
            "wsAttributeAssignValues":[
              {
                "id":"46ddf78f20f74103b4ae5e34d206aefa",
                "valueSystem":"hey"
              }
            ]
            ,
            "ownerGroupId":"87f3010b795349ad89d2b52f9067f325",
            "ownerMemberSubjectId":"10021368",
            "ownerMemberId":"c5c8ef55-76be-4b0d-9910-9efbf465cff3",
            "attributeDefName":"test:testAttestation:testDef",
            "attributeDefNameName":"test:testAttestation:testAttr",
            "attributeAssignActionName":"assign",
            "attributeDefNameId":"f1aa75834d2248d0999c469875bfe08d",
            "attributeAssignActionType":"immediate"
          }
        ]
        ,
        "valuesChanged":"T",
        "changed":"F"
      },
      {
        "wsAttributeAssignValueResults":[
          {
            "deleted":"F",
            "wsAttributeAssignValue":{
              "id":"303a307e169e45a193ccde9d53d11923",
              "valueSystem":"there"
            },
            "changed":"T"
          }
        ]
        ,
        "resultMetadata":{
          "success":"T",
          "resultCode":"SUCCESS",
          "resultMessage":", Found 1 results."
        },
        "deleted":"F",
        "wsAttributeAssigns":[
          {
            "attributeAssignDelegatable":"FALSE",
            "ownerMemberSourceId":"pennperson",
            "disallowed":"F",
            "createdOn":"2022/09/17 10:08:52.224",
            "enabled":"T",
            "attributeAssignType":"any_mem",
            "attributeDefId":"4f7796d2aacf4688bb177fc668be699f",
            "lastUpdated":"2022/09/17 10:08:52.224",
            "attributeAssignActionId":"531b0bee1a5d47d38fce853423ba8612",
            "ownerGroupName":"test:testGroup2",
            "id":"7c8ce4895dc647179525287970ed75f4",
            "wsAttributeAssignValues":[
              {
                "id":"303a307e169e45a193ccde9d53d11923",
                "valueSystem":"there"
              }
            ]
            ,
            "ownerGroupId":"29946725aa514e0cb7bb4864625cfda0",
            "ownerMemberSubjectId":"89505485",
            "ownerMemberId":"632371c4be434c68b1f27bf546aca6e3",
            "attributeDefName":"test:testAttestation:testDef",
            "attributeDefNameName":"test:testAttestation:testAttr",
            "attributeAssignActionName":"assign",
            "attributeDefNameId":"f1aa75834d2248d0999c469875bfe08d",
            "attributeAssignActionType":"immediate"
          }
        ]
        ,
        "valuesChanged":"T",
        "changed":"F"
      }
    ]
    ,
    "wsAttributeDefs":[
      {
        "attributeDefType":"attr",
        "assignToAttributeDef":"F",
        "assignToStemAssignment":"F",
        "extension":"testDef",
        "assignToMemberAssignment":"F",
        "assignToEffectiveMembership":"F",
        "uuid":"4f7796d2aacf4688bb177fc668be699f",
        "assignToImmediateMembershipAssignment":"F",
        "assignToEffectiveMembershipAssignment":"F",
        "assignToStem":"F",
        "assignToGroupAssignment":"F",
        "assignToMember":"F",
        "multiAssignable":"F",
        "valueType":"string",
        "name":"test:testAttestation:testDef",
        "assignToAttributeDefAssignment":"F",
        "idIndex":"10544",
        "multiValued":"F",
        "assignToGroup":"F",
        "assignToImmediateMembership":"F"
      }
    ]
    ,
    "responseMetadata":{
      "serverVersion":"2.6.13",
      "millis":"272"
    },
    "wsGroups":[
      {
        "extension":"testGroup1",
        "displayName":"test:testGroup1",
        "uuid":"87f3010b795349ad89d2b52f9067f325",
        "enabled":"T",
        "displayExtension":"testGroup1",
        "name":"test:testGroup1",
        "typeOfGroup":"group",
        "idIndex":"500713"
      }
    ]
    ,
    "wsAttributeDefNames":[
      {
        "attributeDefId":"4f7796d2aacf4688bb177fc668be699f",
        "displayExtension":"testAttr",
        "extension":"testAttr",
        "displayName":"test:testAttestation:testAttr",
        "name":"test:testAttestation:testAttr",
        "attributeDefName":"test:testAttestation:testDef",
        "idIndex":"68544",
        "uuid":"f1aa75834d2248d0999c469875bfe08d"
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

```

#### Example 2 immediate membership

Assign ADMIN to calling user to the group, and read/update on the attribute definitions

```
[mchyzer@flash pennGroupsClient-2.6.0]$ java -jar grouperClient-2.6.17.jar --operation=assignAttributesBatchWs --entry_0_attributeAssignType=imm_mem --entry_0_attributeAssignOperation=assign_attr --entry_0_ownerMembershipUuid=5cdd24ed262f4249b3cc7b45a2425cdd --entry_0_nameOfAttributeDefName=test:isc:astt:chris:attributeBatchTest:oarRequestId --entry_0_attributeAssignValueOperation=assign_value --entry_0_values0System=54321 --entry_1_attributeAssignType=imm_mem --entry_1_attributeAssignOperation=assign_attr --entry_1_ownerMembershipUuid=5cdd24ed262f4249b3cc7b45a2425cdd --entry_1_nameOfAttributeDefName=test:isc:astt:chris:attributeBatchTest:oarApproverPsuuuid --entry_1_attributeAssignValueOperation=assign_value --entry_1_values0System=26c2431d-dce3-4e70-9107-b67f20edfd0b --entry_2_attributeAssignType=imm_mem --entry_2_attributeAssignOperation=assign_attr --entry_2_ownerMembershipUuid=5cdd24ed262f4249b3cc7b45a2425cdd --entry_2_nameOfAttributeDefName=test:isc:astt:chris:attributeBatchTest:oarSupervisorPsuuuid --entry_2_attributeAssignValueOperation=assign_value --entry_2_values0System=48685df3-a064-4f46-bf81-180d4b9f7c1a --debug=true
Reading resource: grouper.client.properties, from: /home/mchyzer/grouper/pennGroupsClient-2.6.0/grouper.client.properties
WebService: connecting as user: 'fast/medley.isc-seo.upenn.edu'
WebService: connecting to URL: 'https://grouperWs.apps.upenn.edu/grouperWs/servicesRest/2.6.17/attributeAssignments'

################ REQUEST START (indented) ###############

POST /grouperWs/servicesRest/2.6.17/attributeAssignments HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperWs.apps.upenn.edu:-1
Content-Length: 1101
Content-Type: application/json; charset=UTF-8

{
  "WsRestAssignAttributesBatchRequest":{
    "wsAssignAttributeBatchEntries":[
      {
        "attributeAssignType":"imm_mem",
        "wsAttributeDefNameLookup":{
          "name":"test:isc:astt:chris:attributeBatchTest:oarRequestId"
        },
        "attributeAssignOperation":"assign_attr",
        "values":[
          {
            "valueSystem":"54321"
          }
        ]
        ,
        "attributeAssignValueOperation":"assign_value",
        "wsOwnerMembershipLookup":{
          "uuid":"5cdd24ed262f4249b3cc7b45a2425cdd"
        }
        
      },
      {
        "attributeAssignType":"imm_mem",
        "wsAttributeDefNameLookup":{
          "name":"test:isc:astt:chris:attributeBatchTest:oarApproverPsuuuid"
        },
        "attributeAssignOperation":"assign_attr",
        "values":[
          {
            "valueSystem":"26c2431d-dce3-4e70-9107-b67f20edfd0b"
          }
        ]
        ,
        "attributeAssignValueOperation":"assign_value",
        "wsOwnerMembershipLookup":{
          "uuid":"5cdd24ed262f4249b3cc7b45a2425cdd"
        }
        
      },
      {
        "attributeAssignType":"imm_mem",
        "wsAttributeDefNameLookup":{
          "name":"test:isc:astt:chris:attributeBatchTest:oarSupervisorPsuuuid"
        },
        "attributeAssignOperation":"assign_attr",
        "values":[
          {
            "valueSystem":"48685df3-a064-4f46-bf81-180d4b9f7c1a"
          }
        ]
        ,
        "attributeAssignValueOperation":"assign_value",
        "wsOwnerMembershipLookup":{
          "uuid":"5cdd24ed262f4249b3cc7b45a2425cdd"
        }
      }
    ]
  }
}

################ REQUEST END ###############

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Date: Thu, 29 Dec 2022 01:22:19 GMT
Content-Type: application/json;charset=UTF-8
Content-Length: 5755
Connection: close
Server: Apache/2.4.6 (CentOS) OpenSSL/1.0.2k-fips
Strict-Transport-Security: max-age=15768000
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE

{
  "WsAssignAttributesBatchResults":{
    "resultMetadata":{
      "success":"T",
      "resultCode":"SUCCESS",
      "resultMessage":"Success for: clientVersion: 2.6.17, includeSubjectDetail: false, actAsSubject: null, subjectAttributeNames: null\n, paramNames: \n, params: null\n, wsAssignAttributeBatchEntries: 0. wsAssignAttributeBatchEntry: attributeAssignOperation: assign_attr, attributeAssignType: imm_mem, attributeAssignValueOperation: assign_value, values: 0. valueSystem: 54321, , wsAttributeDefNameLookup: WsAttributeDefNameLookup[pitAttributeDefNames=[],name=test:isc:astt:chris:attributeBatchTest:oarRequestId], wsOwnerMembershipLookup: WsMembershipLookup[uuid=5cdd24ed262f4249b3cc7b45a2425cdd],"
    },
    "wsAssignAttributeBatchResultArray":[
      {
        "wsAttributeAssignValueResults":[
          {
            "deleted":"F",
            "wsAttributeAssignValue":{
              "id":"af8407e92dc9408ab3148dfe0bbb0e20",
              "valueSystem":"54321"
            },
            "changed":"T"
          }
        ]
        ,
        "resultMetadata":{
          "success":"T",
          "resultCode":"SUCCESS",
          "resultMessage":", Found 1 results."
        },
        "deleted":"F",
        "wsAttributeAssigns":[
          {
            "attributeAssignDelegatable":"FALSE",
            "disallowed":"F",
            "createdOn":"2022/12/28 20:22:19.374",
            "enabled":"T",
            "attributeAssignType":"imm_mem",
            "attributeDefId":"3259895efc82404b86f79c16ecd6600f",
            "lastUpdated":"2022/12/28 20:22:19.374",
            "attributeAssignActionId":"e6977eba2e524b6da50d18e5ed3562f8",
            "ownerMembershipId":"5cdd24ed262f4249b3cc7b45a2425cdd",
            "id":"b03f7007eb4547ff842be17bb4e98081",
            "wsAttributeAssignValues":[
              {
                "id":"af8407e92dc9408ab3148dfe0bbb0e20",
                "valueSystem":"54321"
              }
            ]
            ,
            "attributeDefName":"test:isc:astt:chris:attributeBatchTest:oarMembershipIntDef",
            "attributeDefNameName":"test:isc:astt:chris:attributeBatchTest:oarRequestId",
            "attributeAssignActionName":"assign",
            "attributeDefNameId":"bd4a0d08fe1b43dd94811a8b752eda4c",
            "attributeAssignActionType":"immediate"
          }
        ]
        ,
        "valuesChanged":"T",
        "changed":"T"
      },
      {
        "wsAttributeAssignValueResults":[
          {
            "deleted":"F",
            "wsAttributeAssignValue":{
              "id":"45bcd64d2d5647b7b9a8565fe1021115",
              "valueSystem":"26c2431d-dce3-4e70-9107-b67f20edfd0b"
            },
            "changed":"T"
          }
        ]
        ,
        "resultMetadata":{
          "success":"T",
          "resultCode":"SUCCESS",
          "resultMessage":", Found 1 results."
        },
        "deleted":"F",
        "wsAttributeAssigns":[
          {
            "attributeAssignDelegatable":"FALSE",
            "disallowed":"F",
            "createdOn":"2022/12/28 20:22:19.542",
            "enabled":"T",
            "attributeAssignType":"imm_mem",
            "attributeDefId":"d2db62290f5b4942993b05272d18a384",
            "lastUpdated":"2022/12/28 20:22:19.542",
            "attributeAssignActionId":"88449ce2ea684b28a1cfa633d437417e",
            "ownerMembershipId":"5cdd24ed262f4249b3cc7b45a2425cdd",
            "id":"dd8889d0fb6144e09857a45efce4134e",
            "wsAttributeAssignValues":[
              {
                "id":"45bcd64d2d5647b7b9a8565fe1021115",
                "valueSystem":"26c2431d-dce3-4e70-9107-b67f20edfd0b"
              }
            ]
            ,
            "attributeDefName":"test:isc:astt:chris:attributeBatchTest:oarMembershipStringDef",
            "attributeDefNameName":"test:isc:astt:chris:attributeBatchTest:oarApproverPsuuuid",
            "attributeAssignActionName":"assign",
            "attributeDefNameId":"1319070ab4954f81b93dda216534c218",
            "attributeAssignActionType":"immediate"
          }
        ]
        ,
        "valuesChanged":"T",
        "changed":"T"
      },
      {
        "wsAttributeAssignValueResults":[
          {
            "deleted":"F",
            "wsAttributeAssignValue":{
              "id":"fd2aa58b79bb46b985d7fcdaf31ba14f",
              "valueSystem":"48685df3-a064-4f46-bf81-180d4b9f7c1a"
            },
            "changed":"T"
          }
        ]
        ,
        "resultMetadata":{
          "success":"T",
          "resultCode":"SUCCESS",
          "resultMessage":", Found 1 results."
        },
        "deleted":"F",
        "wsAttributeAssigns":[
          {
            "attributeAssignDelegatable":"FALSE",
            "disallowed":"F",
            "createdOn":"2022/12/28 20:22:19.601",
            "enabled":"T",
            "attributeAssignType":"imm_mem",
            "attributeDefId":"d2db62290f5b4942993b05272d18a384",
            "lastUpdated":"2022/12/28 20:22:19.601",
            "attributeAssignActionId":"88449ce2ea684b28a1cfa633d437417e",
            "ownerMembershipId":"5cdd24ed262f4249b3cc7b45a2425cdd",
            "id":"1ef514244d024a7cb42262262abdd9a0",
            "wsAttributeAssignValues":[
              {
                "id":"fd2aa58b79bb46b985d7fcdaf31ba14f",
                "valueSystem":"48685df3-a064-4f46-bf81-180d4b9f7c1a"
              }
            ]
            ,
            "attributeDefName":"test:isc:astt:chris:attributeBatchTest:oarMembershipStringDef",
            "attributeDefNameName":"test:isc:astt:chris:attributeBatchTest:oarSupervisorPsuuuid",
            "attributeAssignActionName":"assign",
            "attributeDefNameId":"ee53fd7dda114177b8ca7cf9a4958a18",
            "attributeAssignActionType":"immediate"
          }
        ]
        ,
        "valuesChanged":"T",
        "changed":"T"
      }
    ]
    ,
    "wsMemberships":[
      {
        "membershipType":"immediate",
        "immediateMembershipId":"5cdd24ed262f4249b3cc7b45a2425cdd",
        "groupId":"68c4aafed3694700bd8ef14485ff8f7f",
        "membershipId":"5cdd24ed262f4249b3cc7b45a2425cdd:c2ad84dd02e34769b68f9e0e1fa69b8b",
        "listType":"list",
        "enabled":"T",
        "subjectId":"10021368",
        "groupName":"test:isc:astt:chris:attributeBatchTest:testGroupAttributeBatch",
        "createTime":"2022/12/28 20:06:19.266",
        "listName":"members",
        "subjectSourceId":"pennperson",
        "memberId":"c5c8ef55-76be-4b0d-9910-9efbf465cff3"
      }
    ]
    ,
    "wsAttributeDefs":[
      {
        "attributeDefType":"attr",
        "assignToAttributeDef":"F",
        "assignToStemAssignment":"F",
        "extension":"oarMembershipIntDef",
        "assignToMemberAssignment":"F",
        "assignToEffectiveMembership":"F",
        "uuid":"3259895efc82404b86f79c16ecd6600f",
        "assignToImmediateMembershipAssignment":"F",
        "assignToEffectiveMembershipAssignment":"F",
        "assignToStem":"F",
        "assignToGroupAssignment":"F",
        "assignToMember":"F",
        "multiAssignable":"F",
        "valueType":"integer",
        "name":"test:isc:astt:chris:attributeBatchTest:oarMembershipIntDef",
        "assignToAttributeDefAssignment":"F",
        "idIndex":"10584",
        "multiValued":"F",
        "assignToGroup":"F",
        "assignToImmediateMembership":"F"
      }
    ]
    ,
    "responseMetadata":{
      "serverVersion":"2.6.17",
      "millis":"457"
    },
    "wsAttributeDefNames":[
      {
        "attributeDefId":"3259895efc82404b86f79c16ecd6600f",
        "displayExtension":"oarRequestId",
        "extension":"oarRequestId",
        "displayName":"test:isc:astt:chris:attributeBatchTest:oarRequestId",
        "name":"test:isc:astt:chris:attributeBatchTest:oarRequestId",
        "attributeDefName":"test:isc:astt:chris:attributeBatchTest:oarMembershipIntDef",
        "idIndex":"75772",
        "uuid":"bd4a0d08fe1b43dd94811a8b752eda4c"
      }
    ]
  }
}

################ RESPONSE END ###############

Output template: Index: ${assignIndex}, itemIndex: ${assignItemIndex}: attributeAssignType: ${wsAttributeAssign.attributeAssignType}, owner: ${ownerName}, attributeDefNameName: ${wsAttributeDefName.name}, action: ${wsAttributeAssign.attributeAssignActionName}, values: ${valuesString}, enabled: ${wsAttributeAssign.enabled}, id: ${wsAttributeAssign.id}, changed: ${wsAssignAttributeBatchResult.changed}, deleted: ${wsAssignAttributeBatchResult.deleted}, valuesChanged: ${wsAssignAttributeBatchResult.valuesChanged}, available variables: wsAssignAttributesBatchResults, wsAssignAttributeBatchResult, grouperClientUtils, assignIndex, assignItemIndex, wsAttributeAssign, ownerName, valuesString, wsOwnerAttributeDef, wsAttributeDef, wsAttributeDefName, wsOwnerMemberSubject, wsOwnerMembership, wsOwnerGroup
Index: 0, itemIndex: 0: attributeAssignType: imm_mem, owner: 5cdd24ed262f4249b3cc7b45a2425cdd, attributeDefNameName: test:isc:astt:chris:attributeBatchTest:oarRequestId, action: assign, values: 54321, enabled: T, id: b03f7007eb4547ff842be17bb4e98081, changed: T, deleted: F, valuesChanged: T
Index: 1, itemIndex: 0: attributeAssignType: imm_mem, owner: 5cdd24ed262f4249b3cc7b45a2425cdd, attributeDefNameName: , action: assign, values: 26c2431d-dce3-4e70-9107-b67f20edfd0b, enabled: T, id: dd8889d0fb6144e09857a45efce4134e, changed: T, deleted: F, valuesChanged: T
Index: 2, itemIndex: 0: attributeAssignType: imm_mem, owner: 5cdd24ed262f4249b3cc7b45a2425cdd, attributeDefNameName: , action: assign, values: 48685df3-a064-4f46-bf81-180d4b9f7c1a, enabled: T, id: 1ef514244d024a7cb42262262abdd9a0, changed: T, deleted: F, valuesChanged: T
Elapsed time: 2104ms
[mchyzer@flash pennGroupsClient-2.6.0]$ 
```
