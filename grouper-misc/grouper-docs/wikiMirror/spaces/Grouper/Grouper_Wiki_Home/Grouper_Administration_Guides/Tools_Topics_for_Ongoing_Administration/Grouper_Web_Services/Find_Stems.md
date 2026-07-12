---
title: "Find Stems"
space: Grouper
pageId: 28547915
version: 13
lastUpdated: 2026-07-01T05:45:45.697Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547915/Find+Stems
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Find stems search for stems based on name, attribute, parent stem, etc. Can build queries with group math (AND / OR / MINUS)

#### Features

- Use [query type](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/query/WsStemQueryFilterType.html) to build one query object
- For AND|OR|MINUS you can link up multiple queries into one
- Returns stems
- Can actAs another user
- v2.1 and later, you can sort and/or page the results of individual filter types: parent stem, and approximate group name. Can sort on name, displayName, extension, or displayExtension.

#### Find stems Lite service

- Accepts one query to search (cannot use group math)
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#findStemsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.query.WsStemQueryFilterType-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.query.StemScope-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on findStemsLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#findStemsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsLiteRequest-) (click on findStemsLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): GET /grouper-ws/servicesRest/v1_3_000/stems
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/stem/WsRestFindStemsLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindStemsResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindStemsResults.WsFindStemsResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/findStems/) (all files with "Lite" in them, click on "download" to see file)

#### Find stems service

- Accepts multiple query objects in a graph (can use group math)
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#findStems-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsStemQueryFilter-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup:A-) (click on findStems), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#findStems-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsRequest-) (click on findStems)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v1_3_000/stems
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/stem/WsRestFindStemsRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/soap/WsFindStemsResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindStemsResults.WsFindStemsResultsCode.html), [response codes for each assignment](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/soap/WsFindStemsResult.WsFindStemsResultCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/findStems/) (all files without "Lite" in them, click on "download" to see files)

#### Example: find stems directly under another stem

```
[mchyzer@flash pennGroupsClient-2.6.0]$ java -jar grouperClient-2.6.19.jar --operation=findStemsWs --stemQueryFilterType=FIND_BY_PARENT_STEM_NAME --parentStemName=test --parentStemNameScope=ONE_LEVEL --debug=true
Reading resource: grouper.client.properties, from: /home/mchyzer/grouper/pennGroupsClient-2.6.0/grouper.client.properties

################ REQUEST START (indented) ###############

POST /grouperWs/servicesRest/2.6.19/stems HTTP/1.1
Connection: close
Authorization: Basic xxxxxxxxxxxxxxxx
User-Agent: Jakarta Commons-HttpClient/3.1
Host: grouperWs.apps.upenn.edu:-1
Content-Length: 157
Content-Type: application/json; charset=UTF-8

{
  "WsRestFindStemsRequest":{
    "wsStemQueryFilter":{
      "stemQueryFilterType":"FIND_BY_PARENT_STEM_NAME",
      "parentStemName":"test",
      "parentStemNameScope":"ONE_LEVEL"
    }
  }
}

################ REQUEST END ###############

################ RESPONSE START (indented) ###############

HTTP/1.1 200 OK
Date: Tue, 16 May 2023 19:48:30 GMT
Content-Type: application/json;charset=UTF-8
Content-Length: 973
Connection: close
Server: Apache/2.4.6 (CentOS) OpenSSL/1.0.2k-fips
Strict-Transport-Security: max-age=15768000
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE

{
  "WsFindStemsResults":{
    "resultMetadata":{
      "success":"T",
      "resultCode":"SUCCESS",
      "resultMessage":"Success for: clientVersion: 2.6.19, wsStemQueryFilter: WsStemQueryFilter[stemQueryFilterType=FIND_BY_PARENT_STEM_NAME,parentStemName=test,parentStemNameScope=ONE_LEVEL], actAsSubject: null\n, params: null\n, wsStemLookups: null"
    },
    "stemResults":[
      {
        "displayExtension":"isc",
        "extension":"isc",
        "displayName":"test:isc",
        "name":"test:isc",
        "description":"isc testing",
        "idIndex":"18374",
        "uuid":"62aa6040-a78f-48d5-bd21-dccb339ca4bc"
      },
      {
        "displayExtension":"ldapTesting",
        "extension":"ldapTesting",
        "displayName":"test:ldapTesting",
        "name":"test:ldapTesting",
        "description":"ldapTesting",
        "idIndex":"29086",
        "uuid":"df41ed1e-f94d-4f36-b0aa-046163f694ed"
      },
      {
        "displayExtension":"seas",
        "extension":"seas",
        "displayName":"test:seas",
        "name":"test:seas",
        "description":"seas",
        "idIndex":"22781",
        "uuid":"957319ac-e0ee-48dc-a0d2-90673ee0d0e6"
      }
    ]
    ,
    "responseMetadata":{
      "serverVersion":"2.6.19",
      "millis":"4549"
    }
  }
}

################ RESPONSE END ###############

Output template: Index ${index}: name: ${wsStem.name}, displayName: ${wsStem.displayName}, available variables: wsFindStemsResults, resultMetadata, grouperClientUtils, index, wsStem
Index 0: name: test:isc, displayName: test:isc
Index 1: name: test:ldapTesting, displayName: test:ldapTesting
Index 2: name: test:seas, displayName: test:seas
Elapsed time: 5709ms
[mchyzer@flash pennGroupsClient-2.6.0]$ 
```
