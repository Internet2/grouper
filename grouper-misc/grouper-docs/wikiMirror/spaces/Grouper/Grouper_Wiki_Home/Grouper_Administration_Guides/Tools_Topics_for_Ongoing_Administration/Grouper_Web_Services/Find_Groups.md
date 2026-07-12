---
title: "Find Groups"
space: Grouper
pageId: 28548694
version: 19
lastUpdated: 2026-07-01T05:43:47.781Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548694/Find+Groups
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Find groups search for groups based on name, attribute, parent stem, etc. Can build queries with group math (AND / OR / MINUS)

#### Features

- Use [query type](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/query/WsQueryFilterType.html) to build one query object
- For AND|OR|MINUS you can link up multiple queries into one
- Returns groups, can be detailed or not
- Can actAs another user
- v2.1 and later, you can sort and/or page the results of individual filter types: parent stem, and approximate group name. Can sort on name, displayName, extension, or displayExtension.
- v2.1 and later, you can pass in typeOfGroups to filter by groups/roles or entities

#### Find groups Lite service

- Accepts one query to search (cannot use group math)
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#findGroupsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.query.WsQueryFilterType-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.query.StemScope-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.GroupType-java.lang.String-java.lang.String-java.lang.String-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on findGroupsLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#findGroupsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsLiteRequest-) (click on findGroupsLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): GET /grouper-ws/servicesRest/v1_3_000/groups
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/group/WsRestFindGroupsLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindGroupsResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindGroupsResults.WsFindGroupsResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/findGroups/) (all files with "Lite" in them, click on "download" to see file)

#### Find groups service

- Accepts multiple query objects in a graph (can use group math)
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#findGroups-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsQueryFilter-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup:A-) (click on findGroups), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#findGroups-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsRequest-) (click on findGroups)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v1_3_000/groups
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/group/WsRestFindGroupsRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindGroupsResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindGroupsResults.WsFindGroupsResultsCode.html), [response codes for each assignment](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindGroupsResult.WsFindGroupsResultCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/findGroups/) (all files without "Lite" in them, click on "download" to see files)

#### Example 1

find groups e.g. in a combobox where you have a string and you split by whitespace and find groups which match all part of the name anywhere

You've got two options:

1. The simple one, do a subject search with source "g:gsa" and put the free form string in the search, and it will do what you want
2. Slightly more complex, There is a unit test case based on the java equivalent of the WS API, which works, you can translate this to json or xml or whatever and it will work over WS

```
GrouperServiceUtils.testSession = GrouperSession.startRootSession();
wsQueryFilter.assignGrouperSession(null);
WsQueryFilter firstQueryFilter = new WsQueryFilter();
firstQueryFilter.setQueryFilterType(WsQueryFilterType.FIND_BY_GROUP_NAME_APPROXIMATE.name());
firstQueryFilter.setGroupName("oup2");

WsQueryFilter secondQueryFilter = new WsQueryFilter();
secondQueryFilter.setQueryFilterType(WsQueryFilterType.FIND_BY_GROUP_NAME_APPROXIMATE.name());
secondQueryFilter.setGroupName("test");

WsQueryFilter complexQueryFilter = new WsQueryFilter();
complexQueryFilter.setQueryFilterType(WsQueryFilterType.AND.name());
complexQueryFilter.setQueryFilter0(firstQueryFilter);
complexQueryFilter.setQueryFilter1(secondQueryFilter);

wsGroups = GrouperServiceLogic.findGroups(
GROUPER_VERSION, complexQueryFilter, null, false, null, null).getGroupResults();

```

#### Example 2 find by uuid

```
{
   "WsRestFindGroupsRequest":{
    "wsQueryFilter":{
      "queryFilterType":"FIND_BY_GROUP_UUID",
      "groupUuid":"c8cdc3ca0705424ca68b40780fde8f6b"
    }
  }
}
```

#### Example 3 find groups in a stem (directly in stem)

```
[mchyzer@flash pennGroupsClient-2.6.0]$ java -jar grouperClient.jar --operation=findGroupsWs --queryFilterType=FIND_BY_STEM_NAME --stemName="penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV" --debug=true
Reading resource: grouper.client.properties, from: /home/mchyzer/grouper/pennGroupsClient-2.6.0/grouper.client.properties
WebService: connecting as user: 'someWsUser'
WebService: connecting to URL: 'https://grouperWs.school.edu/grouperWs/servicesRest/2.6.19/groups'

################ REQUEST START (indented) ###############

POST /grouperWs/servicesRest/2.6.19/groups HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperWs.apps.upenn.edu:-1
Content-Length: 158
Content-Type: application/json; charset=UTF-8

{
  "WsRestFindGroupsRequest":{
    "wsQueryFilter":{
      "queryFilterType":"FIND_BY_STEM_NAME",
      "stemName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV"
    }
  }
}

################ REQUEST END ###############

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Date: Fri, 08 Sep 2023 20:16:44 GMT
Content-Type: application/json;charset=UTF-8
Content-Length: 5979
Connection: close
Server: Apache/2.4.6 (CentOS) OpenSSL/1.0.2k-fips
Strict-Transport-Security: max-age=15768000
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE

{
  "WsFindGroupsResults":{
    "groupResults":[
      {
        "extension":"AP",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:AP",
        "description":"Graduate Arts & Sciences",
        "uuid":"a786546c755442bc8fcbbdf8d462d1b2",
        "enabled":"T",
        "displayExtension":"AP",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:AP",
        "typeOfGroup":"group",
        "idIndex":"1010391"
      },
      {
        "extension":"C",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:C",
        "description":"Annenberg",
        "alternateName":"penn:isc:nandt:services:aws:cognito:358010390255:PRIOCON_DEV:C",
        "uuid":"0df61c0c44914ef28c264fa90686c9b0",
        "enabled":"T",
        "displayExtension":"C",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:C",
        "typeOfGroup":"group",
        "idIndex":"1009945"
      },
      {
        "extension":"D",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:D",
        "description":"Dental Medicine",
        "alternateName":"penn:isc:nandt:services:aws:cognito:358010390255:PRIOCON_DEV:D",
        "uuid":"03ff60ac1cbb46a993690ff5c33ba855",
        "enabled":"T",
        "displayExtension":"D",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:D",
        "typeOfGroup":"group",
        "idIndex":"1009978"
      },
      {
        "extension":"E",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:E",
        "description":"Engineering & Applied Science",
        "alternateName":"penn:isc:nandt:services:aws:cognito:358010390255:PRIOCON_DEV:E",
        "uuid":"af52366b48e2427c992a800d2cf54c7f",
        "enabled":"T",
        "displayExtension":"E",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:E",
        "typeOfGroup":"group",
        "idIndex":"1009942"
      },
      {
        "extension":"F",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:F",
        "description":"Stuart Weitzman School-Design",
        "alternateName":"penn:isc:nandt:services:aws:cognito:358010390255:PRIOCON_DEV:F",
        "uuid":"17c73f826b08492db1a3ae9fd3cf963b",
        "enabled":"T",
        "displayExtension":"F",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:F",
        "typeOfGroup":"group",
        "idIndex":"1009943"
      },
      {
        "extension":"G",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:G",
        "description":"Education",
        "alternateName":"penn:isc:nandt:services:aws:cognito:358010390255:PRIOCON_DEV:G",
        "uuid":"130033e884f1422681846d93e0f84b91",
        "enabled":"T",
        "displayExtension":"G",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:G",
        "typeOfGroup":"group",
        "idIndex":"1009941"
      },
      {
        "extension":"L",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:L",
        "description":"Law",
        "alternateName":"penn:isc:nandt:services:aws:cognito:358010390255:PRIOCON_DEV:L",
        "uuid":"38302f61a0a448be9ce2873c2fc279e1",
        "enabled":"T",
        "displayExtension":"L",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:L",
        "typeOfGroup":"group",
        "idIndex":"1009979"
      },
      {
        "extension":"N",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:N",
        "description":"Nursing",
        "alternateName":"penn:isc:nandt:services:aws:cognito:358010390255:PRIOCON_DEV:N",
        "uuid":"a8b6a69893d541a5846200d5786c2335",
        "enabled":"T",
        "displayExtension":"N",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:N",
        "typeOfGroup":"group",
        "idIndex":"1009940"
      },
      {
        "extension":"S",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:S",
        "description":"Social Policy & Practice",
        "alternateName":"penn:isc:nandt:services:aws:cognito:358010390255:PRIOCON_DEV:S",
        "uuid":"a500480860c9492ea09d48c49c15f775",
        "enabled":"T",
        "displayExtension":"S",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:S",
        "typeOfGroup":"group",
        "idIndex":"1009944"
      },
      {
        "extension":"V",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:V",
        "description":"Veterinary Medicine",
        "alternateName":"penn:isc:nandt:services:aws:cognito:358010390255:PRIOCON_DEV:V",
        "uuid":"e256a0c44ba94dc7bea62a402a527920",
        "enabled":"T",
        "displayExtension":"V",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:V",
        "typeOfGroup":"group",
        "idIndex":"1009977"
      },
      {
        "extension":"WM",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:WM",
        "description":"WM Wharton MBA Division",
        "uuid":"5f027a606204407188a7bced6ea56c34",
        "enabled":"T",
        "displayExtension":"WM",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:WM",
        "typeOfGroup":"group",
        "idIndex":"1010393"
      },
      {
        "extension":"is_admin",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:is_admin",
        "alternateName":"penn:isc:nandt:services:aws:cognito:358010390255:PRIOCON_DEV:is_admin",
        "uuid":"4cf14933b56a4c3eab64dae4a077fb50",
        "enabled":"T",
        "displayExtension":"is_admin",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:is_admin",
        "typeOfGroup":"group",
        "idIndex":"1009936"
      },
      {
        "extension":"is_user",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:is_user",
        "alternateName":"penn:isc:nandt:services:aws:cognito:358010390255:PRIOCON_DEV:is_user",
        "uuid":"57d4c673f4024e989174e5fc14be6d24",
        "enabled":"T",
        "displayExtension":"is_user",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:is_user",
        "typeOfGroup":"group",
        "idIndex":"1009937"
      },
      {
        "extension":"reviewer",
        "displayName":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:reviewer",
        "description":"Review prior convictions",
        "alternateName":"penn:isc:nandt:services:aws:cognito:358010390255:PRIOCON_DEV:reviewer",
        "uuid":"69c49b3e7ef9420383c67a9bfd6de137",
        "enabled":"T",
        "displayExtension":"reviewer",
        "name":"penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV:reviewer",
        "typeOfGroup":"group",
        "idIndex":"1009997"
      }
    ]
    ,
    "resultMetadata":{
      "success":"T",
      "resultCode":"SUCCESS",
      "resultMessage":"Success for: clientVersion: 2.6.19, wsQueryFilter: WsQueryFilter[queryFilterType=FIND_BY_STEM_NAME,stemName=penn:isc:nandt:services:aws:cognito:358010390255:PRICON_DEV]\n, includeGroupDetail: false, actAsSubject: null, paramNames: \n, params: null\n, wsGroupLookups: null"
    },
    "responseMetadata":{
      "serverVersion":"2.6.19",
      "millis":"29"
    }
  }
}

################ RESPONSE END ###############

```
