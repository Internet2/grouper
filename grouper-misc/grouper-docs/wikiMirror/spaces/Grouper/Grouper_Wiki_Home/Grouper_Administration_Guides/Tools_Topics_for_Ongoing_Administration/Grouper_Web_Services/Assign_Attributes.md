---
title: "Assign Attributes"
space: Grouper
pageId: 28548365
version: 17
lastUpdated: 2026-07-01T05:44:40.851Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548365/Assign+Attributes
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Assign or remove attributes and values of attribute assignments. These attributes can be on groups, stems, members, memberships (immediate or any), attribute definitions, or on assignments of attributes (one level deep).

You can lookup attributes by attribute definition name, or attribute definition id

All assignments will be filtered for security based on the logged in or acted as user (security rules are on [attribute framework wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework))

The returned data will include the attribute assignments, value(s) on those assignments, and a normalized list of references (owner objects e.g. group/stem/etc, attribute definitions, attribute names, etc), if things changed or were already assigned, etc

You can assign attributes to multiple owners, definitions, actions, etc

attributeAssignType is a required field, must be: group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn

attributeAssignOperation is required and is the operation to perform for attribute on owners, from enum AttributeAssignOperation: assign_attr, add_attr, remove_attr, replace_attrs. In this case, assigning an attribute will not assign if already there. add_attr will add this assignment even if it is already there (attribute definition must allow multi assignments)

attributeAssignValueOperation is required if passing values to assign. It is the operation to perform for attribute value on attribute assignments: assign_value, add_value, remove_value, replace_values. Like the attribute assign operation, assign_value will assign if not there, or ignore if already there. add_value will add even if assigned. And replace_values will remove orphans not in the assign list.

#### Features

- Can pass owners, actions, values, etc. If multiples are passed, then each attribute def name will be assigned for each action on each owner.
- Lookup owner or other objects by object lookup (by id, name, etc)
- Returns group / subject information, can be detailed or not
- Can actAs another user

#### Assign attributes lite service

- Accepts one group, or one subject, or stem, one attribute definition name, one action to assign, one value (optional)
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#assignAttributesLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.attr.assign.AttributeAssignType-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.attr.assign.AttributeAssignOperation-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.sql.Timestamp-java.sql.Timestamp-edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable-edu.internet2.middleware.grouper.attr.value.AttributeAssignValueOperation-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-boolean-java.lang.String-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on assignAttributesLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#assignAttributesLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesLiteRequest-) (click on assignAttributesLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A):
  
  - PUT /grouper-ws/servicesRest/v1_6_000/assignAttributes
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAssignAttributesLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignAttributesLiteResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignAttributesResults.WsAssignAttributesResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/assignAttributes/) (all files with "Lite" in them, click on "download" to see file)
- [Samples with value assignment](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/assignAttributesWithValue/)

#### Assign attributes service

- Accepts multiple groups or subjects or memberhipIds (or combination) etc, attribute definitions, actions, etc to assign
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#assignAttributes-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.attr.assign.AttributeAssignType-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup:A-edu.internet2.middleware.grouper.attr.assign.AttributeAssignOperation-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignValue:A-java.lang.String-java.sql.Timestamp-java.sql.Timestamp-edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable-edu.internet2.middleware.grouper.attr.value.AttributeAssignValueOperation-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsMembershipLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsMembershipAnyLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignLookup:A-java.lang.String:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-boolean-java.lang.String:A-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup:A-java.lang.String:A-java.lang.String:A-) (click on assignAttributes), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#assignAttributes-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesRequest-) (click on assignAttributes)
- REST request (colon is escaped to %3A):
  
  - POST /grouper-ws/servicesRest/v1_6_000/assignAttributes
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAssignAttributesRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignAttributesResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignAttributesResults.WsAssignAttributesResultsCode.html)
- Returns an overall status
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/assignAttributes/) (all files without "Lite" in them, click on "download" to see files)
- [Samples with value assignment](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/assignAttributesWithValue/)

#### Assign attribute to any membership (doesnt have to be direct)

```
WebService: connecting to URL: 'http://localhost:8080/grouper-ws/servicesRest/2.6.0/attributeAssignments'

################ REQUEST START (indented) ###############

POST /grouper-ws/servicesRest/2.6.0/attributeAssignments HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: localhost:8080
Content-Length: 311
Content-Type: application/json; charset=UTF-8

{
  "WsRestAssignAttributesRequest":{
    "attributeAssignOperation":"assign_attr",
    "attributeAssignType":"any_mem",
    "wsAttributeDefNameLookups":[
      {
        "name":"test:attr"
      }
    ]
    ,
    "wsOwnerMembershipAnyLookups":[
      {
        "wsGroupLookup":{
          "groupName":"etc:wsGroup"
        },
        "wsSubjectLookup":{
          "subjectId":"GrouperSystem",
          "subjectSourceId":"g:isa"
        }
      }
    ]
  }
}

################ REQUEST END ###############

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Set-Cookie: JSESSIONID=xxxxxxxxxxxx; HttpOnly
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE
Content-Type: application/json;charset=UTF-8
Content-Length: 2253
Date: Tue, 11 Oct 2022 17:45:37 GMT
Connection: close

{
  "WsAssignAttributesResults":{
    "resultMetadata":{
      "success":"T",
      "resultCode":"SUCCESS",
      "resultMessage":", Found 1 results."
    },
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
        "extension":"attrDef",
        "assignToMemberAssignment":"F",
        "assignToEffectiveMembership":"F",
        "uuid":"77c905289a374962b779a74d579cb2cd",
        "assignToImmediateMembershipAssignment":"F",
        "assignToEffectiveMembershipAssignment":"F",
        "assignToStem":"F",
        "assignToGroupAssignment":"F",
        "assignToMember":"F",
        "multiAssignable":"F",
        "valueType":"marker",
        "name":"test:attrDef",
        "assignToAttributeDefAssignment":"F",
        "idIndex":"10080",
        "multiValued":"F",
        "assignToGroup":"F",
        "assignToImmediateMembership":"F"
      }
    ]
    ,
    "responseMetadata":{
      "serverVersion":"2.6.0",
      "millis":"653"
    },
    "wsGroups":[
      {
        "extension":"wsGroup",
        "displayName":"etc:wsGroup",
        "description":"wsGroup",
        "uuid":"bd7831a9c01e435bba1a628f0feef150",
        "enabled":"T",
        "displayExtension":"wsGroup",
        "name":"etc:wsGroup",
        "typeOfGroup":"group",
        "idIndex":"10068"
      }
    ]
    ,
    "wsAttributeDefNames":[
      {
        "attributeDefId":"77c905289a374962b779a74d579cb2cd",
        "displayExtension":"attr",
        "extension":"attr",
        "displayName":"test:attr",
        "name":"test:attr",
        "attributeDefName":"test:attrDef",
        "idIndex":"10298",
        "uuid":"16c46d4aca6c4ef7aaf02209a31c3242"
      }
    ]
    ,
    "wsAttributeAssignResults":[
      {
        "deleted":"F",
        "wsAttributeAssigns":[
          {
            "attributeAssignDelegatable":"FALSE",
            "ownerMemberSourceId":"g:isa",
            "disallowed":"F",
            "createdOn":"2022/10/11 13:45:36.844",
            "enabled":"T",
            "attributeAssignType":"any_mem",
            "attributeDefId":"77c905289a374962b779a74d579cb2cd",
            "lastUpdated":"2022/10/11 13:45:36.844",
            "attributeAssignActionId":"de9ea3d237cb4fe09124791f43fc9b31",
            "ownerGroupName":"etc:wsGroup",
            "id":"bdb45214baad436c8402630e6571f059",
            "ownerGroupId":"bd7831a9c01e435bba1a628f0feef150",
            "ownerMemberSubjectId":"GrouperSystem",
            "ownerMemberId":"585c7be534fe4926bbe9c571a36d2e22",
            "attributeDefName":"test:attrDef",
            "attributeDefNameName":"test:attr",
            "attributeAssignActionName":"assign",
            "attributeDefNameId":"16c46d4aca6c4ef7aaf02209a31c3242",
            "attributeAssignActionType":"immediate"
          }
        ]
        ,
        "valuesChanged":"F",
        "changed":"T"
      }
    ]
    ,
    "wsSubjects":[
      {
        "sourceId":"g:isa",
        "success":"T",
        "name":"GrouperSysAdmin",
        "resultCode":"SUCCESS",
        "id":"GrouperSystem"
      }
    ]
  }
}

################ RESPONSE END ###############

```

#### Assign value example

```
If this is an assignment on an assignment, you first need to query to get the attribute assign id where the value is

[mchyzer@flash pennGroupsClient-2.3.0]$ java -jar grouperClient.jar --operation=getAttributeAssignmentsWs --attributeAssignType=group --includeAssignmentsOnAssignments=true --ownerGroupNames=a:b:c:groupName --debug=true

POST /grouperWs/servicesRest/v2_3_000/attributeAssignments HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperws.apps.upenn.edu:-1
Content-Length: 321
Content-Type: text/xml; charset=UTF-8

<WsRestGetAttributeAssignmentsRequest>
  <attributeAssignType>group</attributeAssignType>
  <wsOwnerGroupLookups>
    <WsGroupLookup>
      <groupName>a:b:c:groupName</groupName>
    </WsGroupLookup>
  </wsOwnerGroupLookups>
  <includeAssignmentsOnAssignments>T</includeAssignmentsOnAssignments>
</WsRestGetAttributeAssignmentsRequest>

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Date: Mon, 23 Apr 2018 12:43:25 GMT
Set-Cookie: JSESSIONID=xxxxxxxxxxxx; Path=/grouperWs
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE
Content-Type: text/xml;charset=UTF-8
Vary: Accept-Encoding
Connection: close
Transfer-Encoding: chunked
Set-Cookie: BIGipServerPFA-grouperws_pool=3810792620.20480.0000; expires=Mon, 23-Apr-2018 18:43:26 GMT; path=/; Httponly; Secure

<WsGetAttributeAssignmentsResults>
    <WsAttributeDefName>
      <idIndex>12892</idIndex>
      <extension>legacyAttribute_grouperLoaderQuartzCron</extension>
      <displayExtension>legacyAttribute_grouperLoaderQuartzCron</displayExtension>
      <displayName>etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron</displayName>
      <name>etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron</name>
      <uuid>ff79e20c06314ee380bb492def9de796</uuid>
      <attributeDefId>ef7039cf58b34783b6b012cc4799073e</attributeDefId>
      <attributeDefName>etc:legacy:attribute:legacyAttributeDef_grouperLoader</attributeDefName>
    </WsAttributeDefName>
    <WsAttributeAssign>
      <disallowed>F</disallowed>
      <attributeAssignActionType>immediate</attributeAssignActionType>
      <attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
      <attributeAssignActionId>d459f64e432e4820a4b79de775fd0ab1</attributeAssignActionId>
      <attributeAssignActionName>assign</attributeAssignActionName>
      <attributeAssignType>group_asgn</attributeAssignType>
      <attributeDefNameId>ff79e20c06314ee380bb492def9de796</attributeDefNameId>
      <attributeDefNameName>etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron</attributeDefNameName>
      <attributeDefId>ef7039cf58b34783b6b012cc4799073e</attributeDefId>
      <attributeDefName>etc:legacy:attribute:legacyAttributeDef_grouperLoader</attributeDefName>
      <wsAttributeAssignValues>
        <WsAttributeAssignValue>
          <id>285dd3e3e3bf4f78955a67395de7e8df</id>
          <valueSystem>0 15 7 * * ?</valueSystem>
        </WsAttributeAssignValue>
      </wsAttributeAssignValues>
      <createdOn>2016/07/09 23:06:37.253</createdOn>
      <enabled>T</enabled>
      <id>781992a9dc0e4b58a0202e9b24e94f87</id>
      <lastUpdated>2016/07/09 23:06:37.253</lastUpdated>
      <ownerAttributeAssignId>ddf1e57538af4b3aaf4b0673b5803511</ownerAttributeAssignId>
    </WsAttributeAssign>
  </wsAttributeAssigns>
  <resultMetadata>
    <resultCode>SUCCESS</resultCode>
    <resultMessage>, Found 11 results.  </resultMessage>
    <success>T</success>
  </resultMetadata>
  <responseMetadata>
    <resultWarnings></resultWarnings>
    <millis>590</millis>
    <serverVersion>2.3.0</serverVersion>
  </responseMetadata>
  <wsGroups>
    <WsGroup>
      <extension>groupName</extension>
      <typeOfGroup>group</typeOfGroup>
      <displayExtension>groupName</displayExtension>
      <displayName>a:b:c:groupName</displayName>
      <name> a:b:c:groupName </name>
      <uuid>d836336699e24404a442a1cfb2027a6c</uuid>
      <idIndex>194760</idIndex>
    </WsGroup>
  </wsGroups>
  <wsStems/>
  <wsMemberships/>
  <wsSubjects/>
</WsGetAttributeAssignmentsResults>

####################################################
Then assign the value:

[mchyzer@flash pennGroupsClient-2.3.0]$ java -jar grouperClient.jar --operation=assignAttributesWs --attributeAssignOperation=assign_attr --attributeAssignType=group_asgn --ownerAttributeAssignUuids=ddf1e57538af4b3aaf4b0673b5803511 --attributeDefNameNames=penn:etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron --attributeAssignValueOperation=replace_values --values0System="0 16 7 * * ?" --debug=true

################ REQUEST START (indented) ###############

POST /grouperWs/servicesRest/v2_3_000/attributeAssignments HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperws.apps.upenn.edu:-1
Content-Length: 707
Content-Type: text/xml; charset=UTF-8

<WsRestAssignAttributesRequest>
  <attributeAssignOperation>assign_attr</attributeAssignOperation>
  <attributeAssignValueOperation>replace_values</attributeAssignValueOperation>
  <wsOwnerAttributeAssignLookups>
    <WsAttributeAssignLookup>
      <uuid>ddf1e57538af4b3aaf4b0673b5803511</uuid>
    </WsAttributeAssignLookup>
  </wsOwnerAttributeAssignLookups>
  <values>
    <WsAttributeAssignValue>
      <valueSystem>0 16 7 * * ?</valueSystem>
    </WsAttributeAssignValue>
  </values>
  <attributeAssignType>group_asgn</attributeAssignType>
  <wsAttributeDefNameLookups>
    <WsAttributeDefNameLookup>
      <name>penn:etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron</name>
    </WsAttributeDefNameLookup>
  </wsAttributeDefNameLookups>
</WsRestAssignAttributesRequest>

################ REQUEST END ###############

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Date: Mon, 23 Apr 2018 13:09:14 GMT
Set-Cookie: JSESSIONID=xxxxxxxxxxxx; Path=/grouperWs
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE
Content-Type: text/xml;charset=UTF-8
Vary: Accept-Encoding
Connection: close
Transfer-Encoding: chunked
Set-Cookie: BIGipServerPFA-grouperws_pool=3861124268.20480.0000; expires=Mon, 23-Apr-2018 19:09:15 GMT; path=/; Httponly; Secure

<WsAssignAttributesResults>
  <wsAttributeDefs>
    <WsAttributeDef>
      <idIndex>10046</idIndex>
      <extension>legacyAttributeDef_grouperLoader</extension>
      <name>etc:legacy:attribute:legacyAttributeDef_grouperLoader</name>
      <uuid>ef7039cf58b34783b6b012cc4799073e</uuid>
      <attributeDefType>attr</attributeDefType>
      <multiAssignable>F</multiAssignable>
      <multiValued>F</multiValued>
      <valueType>string</valueType>
      <assignToAttributeDef>F</assignToAttributeDef>
      <assignToAttributeDefAssignment>F</assignToAttributeDefAssignment>
      <assignToEffectiveMembership>F</assignToEffectiveMembership>
      <assignToEffectiveMembershipAssignment>F</assignToEffectiveMembershipAssignment>
      <assignToGroup>F</assignToGroup>
      <assignToGroupAssignment>F</assignToGroupAssignment>
      <assignToImmediateMembership>F</assignToImmediateMembership>
      <assignToImmediateMembershipAssignment>F</assignToImmediateMembershipAssignment>
      <assignToMember>F</assignToMember>
      <assignToMemberAssignment>F</assignToMemberAssignment>
      <assignToStem>F</assignToStem>
      <assignToStemAssignment>F</assignToStemAssignment>
    </WsAttributeDef>
  </wsAttributeDefs>
  <wsAttributeDefNames>
    <WsAttributeDefName>
      <idIndex>12892</idIndex>
      <extension>legacyAttribute_grouperLoaderQuartzCron</extension>
      <displayExtension>legacyAttribute_grouperLoaderQuartzCron</displayExtension>
      <displayName>etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron</displayName>
      <name>etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron</name>
      <uuid>ff79e20c06314ee380bb492def9de796</uuid>
      <attributeDefId>ef7039cf58b34783b6b012cc4799073e</attributeDefId>
      <attributeDefName>etc:legacy:attribute:legacyAttributeDef_grouperLoader</attributeDefName>
    </WsAttributeDefName>
  </wsAttributeDefNames>
  <wsAttributeAssignResults>
    <WsAssignAttributeResult>
      <wsAttributeAssignValueResults>
        <WsAttributeAssignValueResult>
          <changed>T</changed>
          <deleted>T</deleted>
          <wsAttributeAssignValue>
            <id>285dd3e3e3bf4f78955a67395de7e8df</id>
            <valueSystem>0 15 7 * * ?</valueSystem>
          </wsAttributeAssignValue>
        </WsAttributeAssignValueResult>
        <WsAttributeAssignValueResult>
          <changed>T</changed>
          <deleted>F</deleted>
          <wsAttributeAssignValue>
            <id>4b6c8b5df143448994ceaf6aed68856c</id>
            <valueSystem>0 16 7 * * ?</valueSystem>
          </wsAttributeAssignValue>
        </WsAttributeAssignValueResult>
      </wsAttributeAssignValueResults>
      <wsAttributeAssigns>
        <WsAttributeAssign>
          <disallowed>F</disallowed>
          <attributeAssignActionType>immediate</attributeAssignActionType>
          <attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
          <attributeAssignActionId>d459f64e432e4820a4b79de775fd0ab1</attributeAssignActionId>
          <attributeAssignActionName>assign</attributeAssignActionName>
          <attributeAssignType>group_asgn</attributeAssignType>
          <attributeDefNameId>ff79e20c06314ee380bb492def9de796</attributeDefNameId>
          <attributeDefNameName>etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron</attributeDefNameName>
          <attributeDefId>ef7039cf58b34783b6b012cc4799073e</attributeDefId>
          <attributeDefName>etc:legacy:attribute:legacyAttributeDef_grouperLoader</attributeDefName>
          <wsAttributeAssignValues>
            <WsAttributeAssignValue>
              <id>4b6c8b5df143448994ceaf6aed68856c</id>
              <valueSystem>0 16 7 * * ?</valueSystem>
            </WsAttributeAssignValue>
          </wsAttributeAssignValues>
          <createdOn>2016/07/09 23:06:37.253</createdOn>
          <enabled>T</enabled>
          <id>781992a9dc0e4b58a0202e9b24e94f87</id>
          <lastUpdated>2016/07/09 23:06:37.253</lastUpdated>
          <ownerAttributeAssignId>ddf1e57538af4b3aaf4b0673b5803511</ownerAttributeAssignId>
        </WsAttributeAssign>
      </wsAttributeAssigns>
      <changed>F</changed>
      <valuesChanged>T</valuesChanged>
      <deleted>F</deleted>
    </WsAssignAttributeResult>
  </wsAttributeAssignResults>
  <resultMetadata>
    <resultCode>SUCCESS</resultCode>
    <resultMessage>, Found 1 results.  </resultMessage>
    <success>T</success>
  </resultMetadata>
  <responseMetadata>
    <resultWarnings></resultWarnings>
    <millis>1216</millis>
    <serverVersion>2.3.0</serverVersion>
  </responseMetadata>
  <wsGroups/>
  <wsStems/>
  <wsMemberships/>
  <wsSubjects/>
</WsAssignAttributesResults>

```

Example of assigning an attribute to an attribute

```
[mchyzer@flash pennGroupsClient-2.5.0]$ java -jar grouperClient-2.5.36.jar --operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr --ownerGroupNames=test:testGroup --attributeDefNameNames=test:testGroupAttrMarker --debug=true
Reading resource: grouper.client.properties, from: /home/mchyzer/grouper/pennGroupsClient-2.5.0/grouper.client.properties
WebService: connecting as user: 'fast/medley.isc-seo.upenn.edu'
WebService: connecting to URL: 'https://grouperWs.apps.upenn.edu/grouperWs/servicesRest/2.5.36/attributeAssignments'

################ REQUEST START (indented) ###############

POST /grouperWs/servicesRest/2.5.36/attributeAssignments HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperWs.apps.upenn.edu:-1
Content-Length: 431
Content-Type: text/xml; charset=UTF-8

<WsRestAssignAttributesRequest>
  <attributeAssignOperation>assign_attr</attributeAssignOperation>
  <attributeAssignType>group</attributeAssignType>
  <wsAttributeDefNameLookups>
    <WsAttributeDefNameLookup>
      <name>test:testGroupAttrMarker</name>
    </WsAttributeDefNameLookup>
  </wsAttributeDefNameLookups>
  <wsOwnerGroupLookups>
    <WsGroupLookup>
      <groupName>test:testGroup</groupName>
    </WsGroupLookup>
  </wsOwnerGroupLookups>
</WsRestAssignAttributesRequest>

################ REQUEST END ###############

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Date: Thu, 22 Oct 2020 17:02:29 GMT
Content-Type: application/xml;charset=UTF-8
Transfer-Encoding: chunked
Connection: close
Server: Apache/2.4.6 (CentOS) OpenSSL/1.0.2k-fips
Strict-Transport-Security: max-age=15768000
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE

<WsAssignAttributesResults>
  <wsAttributeDefs>
    <WsAttributeDef>
      <idIndex>10415</idIndex>
      <extension>testGroupAttrMarkerDef</extension>
      <name>test:testGroupAttrMarkerDef</name>
      <uuid>b80e9e8c13f54093ab297aeea7b23974</uuid>
      <attributeDefType>attr</attributeDefType>
      <multiAssignable>F</multiAssignable>
      <multiValued>F</multiValued>
      <valueType>marker</valueType>
      <assignToAttributeDef>F</assignToAttributeDef>
      <assignToAttributeDefAssignment>F</assignToAttributeDefAssignment>
      <assignToEffectiveMembership>F</assignToEffectiveMembership>
      <assignToEffectiveMembershipAssignment>F</assignToEffectiveMembershipAssignment>
      <assignToGroup>F</assignToGroup>
      <assignToGroupAssignment>F</assignToGroupAssignment>
      <assignToImmediateMembership>F</assignToImmediateMembership>
      <assignToImmediateMembershipAssignment>F</assignToImmediateMembershipAssignment>
      <assignToMember>F</assignToMember>
      <assignToMemberAssignment>F</assignToMemberAssignment>
      <assignToStem>F</assignToStem>
      <assignToStemAssignment>F</assignToStemAssignment>
    </WsAttributeDef>
  </wsAttributeDefs>
  <wsAttributeDefNames>
    <WsAttributeDefName>
      <idIndex>30234</idIndex>
      <extension>testGroupAttrMarker</extension>
      <displayExtension>testGroupAttrMarker</displayExtension>
      <displayName>test:testGroupAttrMarker</displayName>
      <name>test:testGroupAttrMarker</name>
      <uuid>ba2c76b810ce4881a9db8c0bd871a53e</uuid>
      <attributeDefId>b80e9e8c13f54093ab297aeea7b23974</attributeDefId>
      <attributeDefName>test:testGroupAttrMarkerDef</attributeDefName>
    </WsAttributeDefName>
  </wsAttributeDefNames>
  <wsAttributeAssignResults>
    <WsAssignAttributeResult>
      <wsAttributeAssigns>
        <WsAttributeAssign>
          <disallowed>F</disallowed>
          <attributeAssignActionType>immediate</attributeAssignActionType>
          <attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
          <attributeAssignActionId>b43313a40f064697b14c38e60afce22b</attributeAssignActionId>
          <attributeAssignActionName>assign</attributeAssignActionName>
          <attributeAssignType>group</attributeAssignType>
          <attributeDefNameId>ba2c76b810ce4881a9db8c0bd871a53e</attributeDefNameId>
          <attributeDefNameName>test:testGroupAttrMarker</attributeDefNameName>
          <attributeDefId>b80e9e8c13f54093ab297aeea7b23974</attributeDefId>
          <attributeDefName>test:testGroupAttrMarkerDef</attributeDefName>
          <createdOn>2020/10/22 13:02:21.773</createdOn>
          <enabled>T</enabled>
          <id>8ee4ff556a6a46c1b650de26100699d0</id>
          <lastUpdated>2020/10/22 13:02:21.773</lastUpdated>
          <ownerGroupId>dbfa18c3-a025-47b6-a9a0-be5ac02e8270</ownerGroupId>
          <ownerGroupName>test:testGroup</ownerGroupName>
        </WsAttributeAssign>
      </wsAttributeAssigns>
      <changed>F</changed>
      <valuesChanged>F</valuesChanged>
      <deleted>F</deleted>
    </WsAssignAttributeResult>
  </wsAttributeAssignResults>
  <resultMetadata>
    <resultCode>SUCCESS</resultCode>
    <resultMessage>, Found 1 results.  </resultMessage>
    <success>T</success>
  </resultMetadata>
  <responseMetadata>
    <resultWarnings></resultWarnings>
    <millis>62</millis>
    <serverVersion>2.5.35</serverVersion>
  </responseMetadata>
  <wsGroups>
    <WsGroup>
      <extension>testGroup</extension>
      <typeOfGroup>group</typeOfGroup>
      <displayExtension>testGroup</displayExtension>
      <description>testGroup</description>
      <displayName>test:testGroup</displayName>
      <name>test:testGroup</name>
      <uuid>dbfa18c3-a025-47b6-a9a0-be5ac02e8270</uuid>
      <alternateName>testdd:testGroupdd</alternateName>
      <idIndex>197979</idIndex>
      <enabled>T</enabled>
    </WsGroup>
  </wsGroups>
  <wsStems/>
  <wsMemberships/>
  <wsSubjects/>
</WsAssignAttributesResults>

################ RESPONSE END ###############

Output template: Index: ${index}: attributeAssignType: ${wsAttributeAssign.attributeAssignType}, owner: ${ownerName}, attributeDefNameName: ${wsAttributeDefName.name}, action: ${wsAttributeAssign.attributeAssignActionName}, values: ${valuesString}, enabled: ${wsAttributeAssign.enabled}, id: ${wsAttributeAssign.id}, changed: ${wsAssignAttributeResult.changed}, deleted: ${wsAssignAttributeResult.deleted}, valuesChanged: ${wsAssignAttributeResult.valuesChanged}, available variables: wsAssignAttributesResults, grouperClientUtils, index, wsAttributeAssignment
Index: 0: attributeAssignType: group, owner: test:testGroup, attributeDefNameName: test:testGroupAttrMarker, action: assign, values: none, enabled: T, id: 8ee4ff556a6a46c1b650de26100699d0, changed: F, deleted: F, valuesChanged: F
Elapsed time: 1111ms
[mchyzer@flash pennGroupsClient-2.5.0]$ 

[mchyzer@flash pennGroupsClient-2.5.0]$ java -jar grouperClient-2.5.36.jar --operation=assignAttributesWs --attributeAssignType=group_asgn --attributeAssignOperation=assign_attr --ownerAttributeAssignUuids=8ee4ff556a6a46c1b650de26100699d0 --attributeDefNameNames=test:testGroupAttr --debug=true
Reading resource: grouper.client.properties, from: /home/mchyzer/grouper/pennGroupsClient-2.5.0/grouper.client.properties
WebService: connecting as user: 'fast/medley.isc-seo.upenn.edu'
WebService: connecting to URL: 'https://grouperWs.apps.upenn.edu/grouperWs/servicesRest/2.5.36/attributeAssignments'

################ REQUEST START (indented) ###############

POST /grouperWs/servicesRest/2.5.36/attributeAssignments HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperWs.apps.upenn.edu:-1
Content-Length: 478
Content-Type: text/xml; charset=UTF-8

<WsRestAssignAttributesRequest>
  <attributeAssignOperation>assign_attr</attributeAssignOperation>
  <wsOwnerAttributeAssignLookups>
    <WsAttributeAssignLookup>
      <uuid>8ee4ff556a6a46c1b650de26100699d0</uuid>
    </WsAttributeAssignLookup>
  </wsOwnerAttributeAssignLookups>
  <attributeAssignType>group_asgn</attributeAssignType>
  <wsAttributeDefNameLookups>
    <WsAttributeDefNameLookup>
      <name>test:testGroupAttr</name>
    </WsAttributeDefNameLookup>
  </wsAttributeDefNameLookups>
</WsRestAssignAttributesRequest>

################ REQUEST END ###############

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Date: Thu, 22 Oct 2020 17:07:47 GMT
Content-Type: application/xml;charset=UTF-8
Transfer-Encoding: chunked
Connection: close
Server: Apache/2.4.6 (CentOS) OpenSSL/1.0.2k-fips
Strict-Transport-Security: max-age=15768000
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE

<WsAssignAttributesResults>
  <wsAttributeDefs>
    <WsAttributeDef>
      <idIndex>10416</idIndex>
      <extension>testGroupAttrDef</extension>
      <name>test:testGroupAttrDef</name>
      <uuid>ffa61e0293e54619bb0fd557673ead84</uuid>
      <attributeDefType>attr</attributeDefType>
      <multiAssignable>F</multiAssignable>
      <multiValued>F</multiValued>
      <valueType>string</valueType>
      <assignToAttributeDef>F</assignToAttributeDef>
      <assignToAttributeDefAssignment>F</assignToAttributeDefAssignment>
      <assignToEffectiveMembership>F</assignToEffectiveMembership>
      <assignToEffectiveMembershipAssignment>F</assignToEffectiveMembershipAssignment>
      <assignToGroup>F</assignToGroup>
      <assignToGroupAssignment>F</assignToGroupAssignment>
      <assignToImmediateMembership>F</assignToImmediateMembership>
      <assignToImmediateMembershipAssignment>F</assignToImmediateMembershipAssignment>
      <assignToMember>F</assignToMember>
      <assignToMemberAssignment>F</assignToMemberAssignment>
      <assignToStem>F</assignToStem>
      <assignToStemAssignment>F</assignToStemAssignment>
    </WsAttributeDef>
  </wsAttributeDefs>
  <wsAttributeDefNames>
    <WsAttributeDefName>
      <idIndex>30235</idIndex>
      <extension>testGroupAttr</extension>
      <displayExtension>testGroupAttr</displayExtension>
      <displayName>test:testGroupAttr</displayName>
      <name>test:testGroupAttr</name>
      <uuid>bda1e2f99f3f49b2911275315bbf0350</uuid>
      <attributeDefId>ffa61e0293e54619bb0fd557673ead84</attributeDefId>
      <attributeDefName>test:testGroupAttrDef</attributeDefName>
    </WsAttributeDefName>
  </wsAttributeDefNames>
  <wsAttributeAssignResults>
    <WsAssignAttributeResult>
      <wsAttributeAssigns>
        <WsAttributeAssign>
          <disallowed>F</disallowed>
          <attributeAssignActionType>immediate</attributeAssignActionType>
          <attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
          <attributeAssignActionId>326b49d96bfe4e71a147559d99bed6d1</attributeAssignActionId>
          <attributeAssignActionName>assign</attributeAssignActionName>
          <attributeAssignType>group_asgn</attributeAssignType>
          <attributeDefNameId>bda1e2f99f3f49b2911275315bbf0350</attributeDefNameId>
          <attributeDefNameName>test:testGroupAttr</attributeDefNameName>
          <attributeDefId>ffa61e0293e54619bb0fd557673ead84</attributeDefId>
          <attributeDefName>test:testGroupAttrDef</attributeDefName>
          <createdOn>2020/10/22 13:07:47.223</createdOn>
          <enabled>T</enabled>
          <id>d2bdb490a64b42598e061a21df13184a</id>
          <lastUpdated>2020/10/22 13:07:47.223</lastUpdated>
          <ownerAttributeAssignId>8ee4ff556a6a46c1b650de26100699d0</ownerAttributeAssignId>
        </WsAttributeAssign>
      </wsAttributeAssigns>
      <changed>T</changed>
      <valuesChanged>F</valuesChanged>
      <deleted>F</deleted>
    </WsAssignAttributeResult>
  </wsAttributeAssignResults>
  <resultMetadata>
    <resultCode>SUCCESS</resultCode>
    <resultMessage>, Found 1 results.  </resultMessage>
    <success>T</success>
  </resultMetadata>
  <responseMetadata>
    <resultWarnings></resultWarnings>
    <millis>192</millis>
    <serverVersion>2.5.35</serverVersion>
  </responseMetadata>
  <wsGroups/>
  <wsStems/>
  <wsMemberships/>
  <wsSubjects/>
</WsAssignAttributesResults>

################ RESPONSE END ###############

Output template: Index: ${index}: attributeAssignType: ${wsAttributeAssign.attributeAssignType}, owner: ${ownerName}, attributeDefNameName: ${wsAttributeDefName.name}, action: ${wsAttributeAssign.attributeAssignActionName}, values: ${valuesString}, enabled: ${wsAttributeAssign.enabled}, id: ${wsAttributeAssign.id}, changed: ${wsAssignAttributeResult.changed}, deleted: ${wsAssignAttributeResult.deleted}, valuesChanged: ${wsAssignAttributeResult.valuesChanged}, available variables: wsAssignAttributesResults, grouperClientUtils, index, wsAttributeAssignment
Index: 0: attributeAssignType: group_asgn, owner: 8ee4ff556a6a46c1b650de26100699d0, attributeDefNameName: test:testGroupAttr, action: assign, values: none, enabled: T, id: d2bdb490a64b42598e061a21df13184a, changed: T, deleted: F, valuesChanged: F
Elapsed time: 1917ms
[mchyzer@flash pennGroupsClient-2.5.0]$ 
```

## Replacing attribute values

Make a string def

Make a string name

Make a group and let your principal assign

Allow principal to assign the attribute

Replace the values

```
[mchyzer@flash pennGroupsClient-2.6.0]$ java -jar grouperClient-2.6.0.jar --operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr --attributeDefNameNames=test:isc:ait:mchyzer:someString --ownerGroupNames=test:isc:ait:mchyzer:testMchyzer --attributeAssignValueOperation=replace_values --values0System=some --values1System=val --values2System=another --debug=true 
Reading resource: grouper.client.properties, from: /home/mchyzer/grouper/pennGroupsClient-2.6.0/grouper.client.properties
WebService: connecting as user: 'fast/medley.isc-seo.upenn.edu'
WebService: connecting to URL: 'https://grouperWs.apps.upenn.edu/grouperWs/servicesRest/2.6.0/attributeAssignments'
################ REQUEST START (indented) ###############
POST /grouperWs/servicesRest/2.6.0/attributeAssignments HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperWs.apps.upenn.edu:-1
Content-Length: 792
Content-Type: text/xml; charset=UTF-8
<WsRestAssignAttributesRequest>
  <attributeAssignOperation>assign_attr</attributeAssignOperation>
  <attributeAssignValueOperation>replace_values</attributeAssignValueOperation>
  <values>
    <WsAttributeAssignValue>
      <valueSystem>some</valueSystem>
    </WsAttributeAssignValue>
    <WsAttributeAssignValue>
      <valueSystem>val</valueSystem>
    </WsAttributeAssignValue>
    <WsAttributeAssignValue>
      <valueSystem>another</valueSystem>
    </WsAttributeAssignValue>
  </values>
  <attributeAssignType>group</attributeAssignType>
  <wsAttributeDefNameLookups>
    <WsAttributeDefNameLookup>
      <name>test:isc:ait:mchyzer:someString</name>
    </WsAttributeDefNameLookup>
  </wsAttributeDefNameLookups>
  <wsOwnerGroupLookups>
    <WsGroupLookup>
      <groupName>test:isc:ait:mchyzer:testMchyzer</groupName>
    </WsGroupLookup>
  </wsOwnerGroupLookups>
</WsRestAssignAttributesRequest>
################ REQUEST END ###############
################ RESPONSE START (indented) ###############
HTTP/1.1 200 OK
Date: Mon, 14 Mar 2022 20:17:21 GMT
Content-Type: application/xml;charset=UTF-8
Transfer-Encoding: chunked
Connection: close
Server: Apache/2.4.6 (CentOS) OpenSSL/1.0.2k-fips
Strict-Transport-Security: max-age=15768000
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE
<WsAssignAttributesResults>
  <wsAttributeDefs>
    <WsAttributeDef>
      <idIndex>10521</idIndex>
      <extension>someStringDef</extension>
      <name>test:isc:ait:mchyzer:someStringDef</name>
      <uuid>9fd502bef5224ee6bd1397f30065b232</uuid>
      <attributeDefType>attr</attributeDefType>
      <multiAssignable>F</multiAssignable>
      <multiValued>T</multiValued>
      <valueType>string</valueType>
      <assignToAttributeDef>F</assignToAttributeDef>
      <assignToAttributeDefAssignment>F</assignToAttributeDefAssignment>
      <assignToEffectiveMembership>F</assignToEffectiveMembership>
      <assignToEffectiveMembershipAssignment>F</assignToEffectiveMembershipAssignment>
      <assignToGroup>F</assignToGroup>
      <assignToGroupAssignment>F</assignToGroupAssignment>
      <assignToImmediateMembership>F</assignToImmediateMembership>
      <assignToImmediateMembershipAssignment>F</assignToImmediateMembershipAssignment>
      <assignToMember>F</assignToMember>
      <assignToMemberAssignment>F</assignToMemberAssignment>
      <assignToStem>F</assignToStem>
      <assignToStemAssignment>F</assignToStemAssignment>
    </WsAttributeDef>
  </wsAttributeDefs>
  <wsAttributeDefNames>
    <WsAttributeDefName>
      <idIndex>61289</idIndex>
      <extension>someString</extension>
      <displayExtension>someString</displayExtension>
      <displayName>test:isc:ait:mchyzer:someString</displayName>
      <name>test:isc:ait:mchyzer:someString</name>
      <uuid>82eae611fa324734924a76b611fa29c4</uuid>
      <attributeDefId>9fd502bef5224ee6bd1397f30065b232</attributeDefId>
      <attributeDefName>test:isc:ait:mchyzer:someStringDef</attributeDefName>
    </WsAttributeDefName>
  </wsAttributeDefNames>
  <wsAttributeAssignResults>
    <WsAssignAttributeResult>
      <wsAttributeAssignValueResults>
        <WsAttributeAssignValueResult>
          <changed>T</changed>
          <deleted>F</deleted>
          <wsAttributeAssignValue>
            <id>64dacb6a42684b1085b441699a8125e2</id>
            <valueSystem>another</valueSystem>
          </wsAttributeAssignValue>
        </WsAttributeAssignValueResult>
        <WsAttributeAssignValueResult>
          <changed>T</changed>
          <deleted>F</deleted>
          <wsAttributeAssignValue>
            <id>087026f749ff42bcbe1daebe42a5054a</id>
            <valueSystem>some</valueSystem>
          </wsAttributeAssignValue>
        </WsAttributeAssignValueResult>
        <WsAttributeAssignValueResult>
          <changed>T</changed>
          <deleted>F</deleted>
          <wsAttributeAssignValue>
            <id>33dfac0f15b34b52bd6a62be901c71a6</id>
            <valueSystem>val</valueSystem>
          </wsAttributeAssignValue>
        </WsAttributeAssignValueResult>
      </wsAttributeAssignValueResults>
      <wsAttributeAssigns>
        <WsAttributeAssign>
          <disallowed>F</disallowed>
          <attributeAssignActionType>immediate</attributeAssignActionType>
          <attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
          <attributeAssignActionId>73fb98ab652a490b9c7bde464ad674b8</attributeAssignActionId>
          <attributeAssignActionName>assign</attributeAssignActionName>
          <attributeAssignType>group</attributeAssignType>
          <attributeDefNameId>82eae611fa324734924a76b611fa29c4</attributeDefNameId>
          <attributeDefNameName>test:isc:ait:mchyzer:someString</attributeDefNameName>
          <attributeDefId>9fd502bef5224ee6bd1397f30065b232</attributeDefId>
          <attributeDefName>test:isc:ait:mchyzer:someStringDef</attributeDefName>
          <wsAttributeAssignValues>
            <WsAttributeAssignValue>
              <id>64dacb6a42684b1085b441699a8125e2</id>
              <valueSystem>another</valueSystem>
            </WsAttributeAssignValue>
            <WsAttributeAssignValue>
              <id>087026f749ff42bcbe1daebe42a5054a</id>
              <valueSystem>some</valueSystem>
            </WsAttributeAssignValue>
            <WsAttributeAssignValue>
              <id>33dfac0f15b34b52bd6a62be901c71a6</id>
              <valueSystem>val</valueSystem>
            </WsAttributeAssignValue>
          </wsAttributeAssignValues>
          <createdOn>2022/03/14 16:17:21.402</createdOn>
          <enabled>T</enabled>
          <id>7e5d962178284c798c5bbf514c3bcd9a</id>
          <lastUpdated>2022/03/14 16:17:21.402</lastUpdated>
          <ownerGroupId>7c4d9d393ff24a29b160b6d36865292f</ownerGroupId>
          <ownerGroupName>test:isc:ait:mchyzer:testMchyzer</ownerGroupName>
        </WsAttributeAssign>
      </wsAttributeAssigns>
      <changed>T</changed>
      <valuesChanged>T</valuesChanged>
      <deleted>F</deleted>
    </WsAssignAttributeResult>
  </wsAttributeAssignResults>
  <resultMetadata>
    <resultCode>SUCCESS</resultCode>
    <resultMessage>, Found 1 results.  </resultMessage>
    <success>T</success>
  </resultMetadata>
  <responseMetadata>
    <resultWarnings></resultWarnings>
    <millis>375</millis>
    <serverVersion>2.6.5</serverVersion>
  </responseMetadata>
  <wsGroups>
    <WsGroup>
      <extension>testMchyzer</extension>
      <typeOfGroup>group</typeOfGroup>
      <displayExtension>testMchyzer</displayExtension>
      <displayName>test:isc:ait:mchyzer:testMchyzer</displayName>
      <name>test:isc:ait:mchyzer:testMchyzer</name>
      <uuid>7c4d9d393ff24a29b160b6d36865292f</uuid>
      <idIndex>116332</idIndex>
      <enabled>T</enabled>
    </WsGroup>
  </wsGroups>
  <wsStems/>
  <wsMemberships/>
  <wsSubjects/>
</WsAssignAttributesResults>
################ RESPONSE END ###############
Output template: Index: ${index}: attributeAssignType: ${wsAttributeAssign.attributeAssignType}, owner: ${ownerName}, attributeDefNameName: ${wsAttributeDefName.name}, action: ${wsAttributeAssign.attributeAssignActionName}, values: ${valuesString}, enabled: ${wsAttributeAssign.enabled}, id: ${wsAttributeAssign.id}, changed: ${wsAssignAttributeResult.changed}, deleted: ${wsAssignAttributeResult.deleted}, valuesChanged: ${wsAssignAttributeResult.valuesChanged}, available variables: wsAssignAttributesResults, grouperClientUtils, index, wsAttributeAssignment
Index: 0: attributeAssignType: group, owner: test:isc:ait:mchyzer:testMchyzer, attributeDefNameName: test:isc:ait:mchyzer:someString, action: assign, values: another,some,val, enabled: T, id: 7e5d962178284c798c5bbf514c3bcd9a, changed: T, deleted: F, valuesChanged: T
Elapsed time: 1346ms
[mchyzer@flash pennGroupsClient-2.6.0]$ 
```

See the values

Replace the values again

```
[mchyzer@flash pennGroupsClient-2.6.0]$ java -jar grouperClient-2.6.0.jar --operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr --attributeDefNameNames=test:isc:ait:mchyzer:someString --ownerGroupNames=test:isc:ait:mchyzer:testMchyzer --attributeAssignValueOperation=replace_values --values0System=some --values2System=another1 --debug=true             
```
