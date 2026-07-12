---
title: "Get Subjects"
space: Grouper
pageId: 28548291
version: 11
lastUpdated: 2026-07-01T05:44:54.630Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548291/Get+Subjects
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Get subjects will retrieve subject objects by subject lookups (source (optional), id or identifier), or by search string (free-form string that sources can search on), and optionally a list of sources to narrow the search.

#### Features

- Can search by subject lookup(s): e.g. by subjectId, by subjectIdentifier, by subjectId and source, by subjectIdentifier and source
- Can search by freeform search string: e.g. search for "Chris Hyzer" and the jdbc2 source will find all subjects with chris and hyzer in the description
- Can actAs another user
- Can filter by list of sources for freeform search string
- Can filter for member of group, though this is not exact, will get the list of subjects, then see which are in the group. If the number of subjects found is more than a limit, it will not allow the search
- In 2.1.6+ you can search for members in a group that will be efficient since it uses a member search field. If you pass in the param SearchStringEnumZeroIndexed with a value of 0-4 (if there are 5 search fields), then that is the search field it will use, otherwise the default search field will be used

#### Get subjects Lite service

- Accepts one subjectLookup or one search string and optional source. Also a group lookup for filtering
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getSubjectsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.member.WsMemberFilter-edu.internet2.middleware.grouper.Field-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on getSubjectsLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getSubjectsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.rest.subject.WsRestGetSubjectsLiteRequest-) (click on getSubjectsLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A):
  
  - GET /grouper-ws/servicesRest/v1_6_000/subjects/12345
  - GET /grouper-ws/servicesRest/v1_6_000/subjects/sources/abc/subjectId/1234
  - GET subjects?wsLiteObjectType=WsRestGetSubjectsLiteRequest&subjectId=1234
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/subject/WsRestGetSubjectsLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetSubjectsResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetSubjectsResults.WsGetSubjectsResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getSubjects/) (all files with "Lite" in them, click on "download" to see file)

#### Get subjects service

- Accepts multiple subject lookups or a search string and option source id. Also a group lookup for filtering
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getSubjects-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup:A-java.lang.String-boolean-java.lang.String:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-java.lang.String:A-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup-edu.internet2.middleware.grouper.ws.member.WsMemberFilter-edu.internet2.middleware.grouper.Field-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on getSubjects), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getSubjects-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.rest.subject.WsRestGetSubjectsRequest-) (click on getSubjects)
- REST request (colon is escaped to %3A):
  
  - POST /grouper-ws/servicesRest/v1_6_000/subjects
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/subject/WsRestGetSubjectsRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetSubjectsResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetSubjectsResults.WsGetSubjectsResultsCode.html)
- Returns an overall status
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getSubjects/) (all files without "Lite" in them, click on "download" to see files)
