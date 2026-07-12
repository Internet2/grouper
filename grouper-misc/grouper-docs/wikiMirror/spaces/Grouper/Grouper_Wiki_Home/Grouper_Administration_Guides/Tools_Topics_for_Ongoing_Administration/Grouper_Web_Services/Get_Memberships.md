---
title: "Get Memberships"
space: Grouper
pageId: 28547791
version: 13
lastUpdated: 2026-07-01T05:46:10.465Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547791/Get+Memberships
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Get memberships will retrieve membership objects by group, by subject, or by id (or a combination).

#### Features

- Can base membership list based on memberfilter (e.g. All, Immediate, Effective)
- Lookup subjects by subject lookup (by id, source, identifier, etc)
- Lookup groups by group lookup (by name or uuid)
- Returns group / subject information, can be detailed or not
- Can actAs another user
- Can filter by a list name (currently only can return group members memberships, not privilege memberships)
- Can filter by "scope" which is a sql "like" string in the namespace for group name.
- Can filter for all memberships directly in a stem, or in any substem of a stem
- Can filter by subject source so only people memberships are returned, or groups, or etc.
- In v2.2+ you can pass in the serviceLookup (service name or id) and serviceRole (admin|user) to get the memberships of a service based on role. This will essentially send you the users of a service.
- In v2.1.5+ you can get privileges on groups, stems, attribute definitions. You can pass in fieldType to filter on the type of memberships: list|access|naming|attributeDef
  
  - ownerStemNames, ownerStemUuids, ownerNamesOfAttributeDefs, ownerIdsOfAttributeDefs
- In v2.5+, you can get point in time information as well

#### Get memberships Lite service

- Accepts one group, or one subject, or multiple membership ids to get members for (or combination)
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getMembershipsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.member.WsMemberFilter-boolean-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.Field-java.lang.String-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.query.StemScope-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.FieldType-edu.internet2.middleware.grouper.service.ServiceRole-java.lang.String-java.lang.String-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-java.lang.Boolean-java.sql.Timestamp-java.sql.Timestamp-) (click on getMembershipsLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getMembershipsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsLiteRequest-) (click on getMembershipsLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A):
  
  - GET /grouper-ws/servicesRest/v1_6_000/groups/aStem%3AaGroup/memberships
  - GET /grouper-ws/servicesRest/v1_6_000/subjects/12345/memberships
  - GET /grouper-ws/servicesRest/v1_6_000/memberships
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/membership/WsRestGetMembershipsLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/soap/WsGetMembershipsResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetMembershipsResults.WsGetMembershipsResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getMemberships/) (all files with "Lite" in them, click on "download" to see file)

#### Get memberships service

- Accepts multiple groups or subjects or memberhipIds (or combination) to retrieve lists of memberships
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getMemberships-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup:A-edu.internet2.middleware.grouper.ws.member.WsMemberFilter-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.Field-boolean-java.lang.String:A-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-java.lang.String:A-java.lang.String-edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup-edu.internet2.middleware.grouper.ws.query.StemScope-java.lang.String-java.lang.String:A-edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup:A-edu.internet2.middleware.grouper.FieldType-edu.internet2.middleware.grouper.service.ServiceRole-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-java.lang.Boolean-java.sql.Timestamp-java.sql.Timestamp-) (click on getMemberships), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getMemberships-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsRequest-) (click on getMemberships)
- REST request (colon is escaped to %3A):
  
  - POST /grouper-ws/servicesRest/v1_6_000/groups/aStem%3AaGroup/memberships
  - POST /grouper-ws/servicesRest/v1_6_000/subjects/12345/memberships
  - POST /grouper-ws/servicesRest/v1_6_000/memberships
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/membership/WsRestGetMembershipsRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/soap/WsGetMembershipsResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetMembershipsResults.WsGetMembershipsResultsCode.html)
- Returns an overall status
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getMemberships/) (all files without "Lite" in them, click on "download" to see files)

## Example of filtering by PIT and getting PIT data back

```
[mchyzer@flash pennGroupsClient-2.6.0]$ java -jar grouperClient-2.6.0.jar --operation=getMembershipsWs --groupNames=test:testGroup --pointInTimeFrom=2021/01/01_18:00:00.123 --pointInTimeTo=2021/01/09_11:07:20.234 --debug=true   
Reading resource: grouper.client.properties, from: /home/mchyzer/grouper/pennGroupsClient-2.6.0/grouper.client.properties
WebService: connecting as user: 'someUser'
WebService: connecting to URL: 'https://grouperWs.server.institution.edu/grouperWs/servicesRest/2.6.0/memberships'

################ REQUEST START (indented) ###############

POST /grouperWs/servicesRest/2.6.0/memberships HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperWs.apps.upenn.edu:-1
Content-Length: 272
Content-Type: text/xml; charset=UTF-8

<WsRestGetMembershipsRequest>
  <wsGroupLookups>
    <WsGroupLookup>
      <groupName>test:testGroup</groupName>
    </WsGroupLookup>
  </wsGroupLookups>
  <pointInTimeFrom>2021/01/01 18:00:00.123</pointInTimeFrom>
  <pointInTimeTo>2021/01/09 11:07:20.234</pointInTimeTo>
</WsRestGetMembershipsRequest>

################ REQUEST END ###############

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Date: Thu, 30 Sep 2021 03:10:40 GMT
Content-Type: application/xml;charset=UTF-8
Transfer-Encoding: chunked
Connection: close
Server: Apache/2.4.6 (CentOS) OpenSSL/1.0.2k-fips
Strict-Transport-Security: max-age=15768000
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE

<WsGetMembershipsResults>
  <wsMemberships>
    <WsMembership>
      <membershipId>d2ee3b81754545f5a1b3a24a185090e7</membershipId>
      <enabled>F</enabled>
      <enabledTime>2020/11/07 15:22:23.577</enabledTime>
      <disabledTime>2021/05/08 12:58:50.049</disabledTime>
      <memberId>c5c8ef55-76be-4b0d-9910-9efbf465cff3</memberId>
      <groupId>dbfa18c3-a025-47b6-a9a0-be5ac02e8270</groupId>
      <subjectId>10021368</subjectId>
      <subjectSourceId>pennperson</subjectSourceId>
      <groupName>test:testGroup</groupName>
    </WsMembership>
    <WsMembership>
      <membershipId>cb8e112632e244aab04521eaf2b776e6</membershipId>
      <enabled>F</enabled>
      <enabledTime>2020/10/03 09:22:39.808</enabledTime>
      <disabledTime>2021/02/02 21:19:58.002</disabledTime>
      <memberId>3fd098df-f4b6-4f0b-ab8c-2b6c81cd1972</memberId>
      <groupId>dbfa18c3-a025-47b6-a9a0-be5ac02e8270</groupId>
      <subjectId>10031144</subjectId>
      <subjectSourceId>pennperson</subjectSourceId>
      <groupName>test:testGroup</groupName>
    </WsMembership>
  </wsMemberships>
  <wsGroups>
    <WsGroup>
      <extension>testGroup</extension>
      <name>test:testGroup</name>
      <uuid>dbfa18c3-a025-47b6-a9a0-be5ac02e8270</uuid>
      <enabled>T</enabled>
      <enabledTime>2011/02/25 01:02:04.691</enabledTime>
    </WsGroup>
  </wsGroups>
  <wsSubjects>
    <WsSubject>
      <resultCode>SUCCESS</resultCode>
      <success>T</success>
      <memberId>c5c8ef55-76be-4b0d-9910-9efbf465cff3</memberId>
      <id>10021368</id>
      <sourceId>pennperson</sourceId>
    </WsSubject>
    <WsSubject>
      <resultCode>SUCCESS</resultCode>
      <success>T</success>
      <memberId>3fd098df-f4b6-4f0b-ab8c-2b6c81cd1972</memberId>
      <id>10031144</id>
      <sourceId>pennperson</sourceId>
    </WsSubject>
  </wsSubjects>
  <resultMetadata>
    <resultCode>SUCCESS</resultCode>
    <resultMessage>Found 2 results involving 1 groups and 2 subjects</resultMessage>
    <success>T</success>
  </resultMetadata>
  <responseMetadata>
    <resultWarnings>, Client version: 2.6.0 is greater than (major/minor) server version: 2.5.55, Client version: 2.6.0 is greater than (major/minor) server version: 2.5.55</resultWarnings>
    <millis>184</millis>
    <serverVersion>2.5.55</serverVersion>
  </responseMetadata>
</WsGetMembershipsResults>

################ RESPONSE END ###############

Output template: Index: ${index}: ${type}: ${ownerName}, subject: ${wsSubject.id}, list: ${wsMembership.listName}, type: ${wsMembership.membershipType}, enabled: ${wsMembership.enabled}, available variables: wsGetMembershipsResults, grouperClientUtils, index, wsMembership, type, ownerName
Index: 0: group: test:testGroup, subject: 10021368, list: , type: , enabled: F
Index: 1: group: test:testGroup, subject: 10031144, list: , type: , enabled: F
Elapsed time: 1144ms
[mchyzer@flash pennGroupsClient-2.6.0]$ 
```
