---
title: "Grouper Web Services"
space: Grouper
pageId: 28544233
version: 69
lastUpdated: 2026-07-12T15:26:14.617Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services
---

Core web service API

## Grouper Web Services

#### Introduction

Grouper web services (grouper-ws) is a J2EE web application which exposes common Grouper business logic REST. See [Web Services FAQ.](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548593/Web+Services+FAQ) and architectural diagram.

Note: there is a command line and java API web service client called [Grouper Client](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545215/Grouper+Client). You can run all operations and see REST/JSON examples with the client.

To implement a web service client:

1. Understand the object model. All grouper-ws services are operations based on simple data structures. The structures support Strings, ints, arrays, and structure references.
  
  1. [Core web service API](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html)
  2. [Example structure](http://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsResponseMeta.html) (only "getters" and "setters" are applicable properties)
  3. Each operation has many samples (automated captures, versioned, and up to date). [Here is an example](https://raw.githubusercontent.com/Internet2/grouper/master/grouper-ws/grouper-ws/doc/samples/addMember/WsSampleAddMemberLite_soap.txt)
  4. Most options has a sensible default (e.g. MemberFilter defaults to All members)
  5. Lookup objects in various (consistent) ways. e.g. to delete a group, you can pass the name or uuid of the group.
2. You should use JSON/REST only even though other options are available.
3. Each operation has two levels of complexity, the normal one, and the Lite one.
  
  1. Normal operation: can usually be batched (support a list of inputs, e.g. add multiple groups at once), supports complex inputs (arrays or structures)
  2. Lite operation: supports only inputs of scalars (no structures, no arrays... only Strings, ints, etc). In REST this also means that the request can be sent via query string only
4. Decide what format you want to send and receive data. grouper-ws supports JSON, as well as query strings for input (in URL or message body)
  
  1. For example, in the URL you can set the content type you want back:

```
/grouper-ws/servicesRest/json/v2_1_000/groups/aStem%3AaGroup/members/10021368

```

1. 1. Or you can set the content type of the request and it will use that for the response
  2. There are many [samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/src/test/edu/internet2/middleware/grouper/ws/samples/rest/)
2. [Understand versioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548558/Grouper+Web+Services+Versioning)

.NET client development guide

#### Operations

#### Guidelines For Working With Grouper Web Services

1. Test if server up  
    
  https://grouperws.whatever.school.edu/grouper-ws/status?diagnosticType=all  
  https://grouperws.whatever.school.edu/grouper-ws/servicesRest/v2_6_000/subjects?wsLiteObjectType=WsRestGetSubjectsLiteRequest&searchString=GrouperSystem
2. Code clients with a mindset that the service might change in subtle ways. e.g. a result code might be added (check for success flag element, not success result code), an element might be added in a result object, another input element might be added to end of list, etc. Expect elements to be added in data. Note if you send the same version in the request, you will never get a response with a different structure. Grouper WS are backwards compatible.
3. Make sure there is a property in the client of the URL and version for the service. The version of the service might change the URL ([up to](https://myaws.co.nz/exploring-pci-compliance-issues-in-the-public-cloud/) service deployer)...
4. Note that Grouper WS can be setup with multiple instances. If you have database replication (even readonly), then you can setup Grouper WS application servers is multiple data centers.

#### Features

- **API**
  
  - Batched operations (e.g. add 100 subjects to a group at once). There is a separate server-side max-in-batch param in the grouper-ws.properties.
  - Transaction support (if any fails in one batch request, rollback all in that single batch request)
- **[Authentication](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548787/Grouper+Web+Services+Authentication)**
  
  - Let container or web server handle  
    o PKI  
    o http-simple-auth  
    o Source IP address filtering (TODO)
  - Custom authenticator
  - JWT
  - LDAP authn
  - Proxying. The web service can execute operations based on an underlying user, not the authenticating user. Note the authenticating user must have appropriate permissions
- **Error Handling**
  
  - Error codes and error messages are sent in responses, as well as warnings. In batched mode, batches of response codes are returned. In REST, the http status code is used as well.

#### Quick start

Note the WS is included in the Grouper Installer.

#### Subject attributes

1. The default attribute names (comma separated) sent back for each request are specified in grouper-ws.properties under the key:

ws.subject.result.attribute.names

2. If the caller sets T to retrieve subject detail, then the attributes will be appended to that list in grouper-ws.properties key:

ws.subject.result.detail.attribute.names

3. If the caller specifies subjectAttributeNames in the request (comma separated), then those will be appended to the list (independent of the detail attributes).

So there are central settings, and caller settings that you need to design for and specify...

Note if subjectId and subjectIdentifier are filled in with the same value, it will find by subject id or identifier.

#### Logging requests and responses

You can do this via the client or a proxy. If you must do this via the server, there is an experimental way to do this in v2.1.1+. You should not do this in prod, only in a testing environment.

Set the filter logger to log at debug level (this is optional)

```
log4j.logger.edu.internet2.middleware.grouper.ws.j2ee.ServletFilterLogger = DEBUG

```

You might want to log to a dedicated file instead of putting in the grouper log... in log4j2.xml

Set this in grouper-ws.properties:

```
ws.ServletFilterLogger.logRequests = true
ws.ServletFilterLogger.logForSourceIpCidrs = 0.0.0.0/0
```

You will see log entries like this

```
2012-05-03 09:13:18,575: [http-8088-1] DEBUG ServletFilterLogger.logStuff(98) -  - IP: 127.0.0.1, url: /grouperWs/servicesRest/v2_1_001/groups/aStem%3AaGroup/members, queryString: null, method: PUT, content-type: text/x-json; charset=UTF-8
request params:
request body: {"WsRestAddMemberRequest":{"actAsSubjectLookup":{"subjectId":"GrouperSystem"},"replaceAllExisting":"F","subjectLookups":[{"subjectId":"10021368"},{"subjectId":"10039438"}]}}
respone headers: (note, not all headers captured, and not in this order)
X-Grouper-resultCode: SUCCESS
X-Grouper-success: T
X-Grouper-resultCode2: NONE
HTTP/1.1 201
Content-Type: text/x-json; charset=UTF-8
response: {"WsAddMemberResults":{"responseMetadata":{"millis":"237","serverVersion":"2.1.1"},"resultMetadata":{"resultCode":"SUCCESS","resultMessage":"Success for: clientVersion: 2.1.1, wsGroupLookup: WsGroupLookup[pitGroups=[],groupName=aStem:aGroup], subjectLookups: Array size: 2: [0]: WsSubjectLookup[subjectId=10021368]\n[1]: WsSubjectLookup[subjectId=10039438]\n\n, replaceAllExisting: false, actAsSubject: WsSubjectLookup[subjectId=GrouperSystem], fieldName: null, txType: NONE, includeGroupDetail: false, includeSubjectDetail: false, subjectAttributeNames: null\n, params: null\n, disabledDate: null, enabledDate: null","success":"T"},"results":[{"resultMetadata":{"resultCode":"SUCCESS_ALREADY_EXISTED","success":"T"},"wsSubject":{"id":"10021368","name":"10021368","resultCode":"SUCCESS","sourceId":"jdbc","success":"T"}},{"resultMetadata":{"resultCode":"SUCCESS_ALREADY_EXISTED","success":"T"},"wsSubject":{"id":"10039438","name":"10039438","resultCode":"SUCCESS","sourceId":"jdbc","success":"T"}}],"wsGroupAssigned":{"description":"a group description","displayExtension":"a group","displayName":"a stem:a group","extension":"aGroup","name":"aStem:aGroup","typeOfGroup":"group","uuid":"d9094e4a7c6e4f399d7e1489c875b9f0"}}}

```

At some point we can make it more granular which requests get logged and give an option to format the request/response (indent, etc)

#### Fields and permissions

If you want to check to see if a subject as a group permission, or to get a list of people with a certain permissions on a group, use hasMember or getMembers, and pass the name of the field (note this list depends on your configuration):

select name from grouper_fields where type != 'naming';

admins  
description  
displayExtension  
displayName  
extension  
members  
name  
optins  
optouts  
readers  
requireActiveEmployee  
requireAlsoInGroups  
updaters  
viewers

#### High availability

See the always available client for more info on this slide

#### See Also

Always Available Web Services

Grouper Failover Client

[Grouper Diagnostics](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548954/Grouper+health+check+endpoint+healthcheck+status+diagnostics)

[Grouper SCIM web service](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549538/Grouper+SCIM+web+service)
