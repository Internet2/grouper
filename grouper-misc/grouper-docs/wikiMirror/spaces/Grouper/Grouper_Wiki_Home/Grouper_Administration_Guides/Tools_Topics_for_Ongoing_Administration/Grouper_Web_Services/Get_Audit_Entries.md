---
title: "Get Audit Entries"
space: Grouper
pageId: 28548223
version: 12
lastUpdated: 2026-07-12T15:26:52.677Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548223/Get+Audit+Entries
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Get audit entries for groups, stems, and subjects. Available in Grouper v2.5 or later.

#### Features

- Returns audit entries only to admins or read-only admins
- Can actAs another user
- For audit type and action, run this query: "select audit_category, action_name from grouper_audit_type gat order by 1, 2", or see this class: edu.internet2.middleware.grouper.audit.AuditTypeBuiltin

#### Get audit entries Lite service

- Accepts one owner group, owner stem, or a subject
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getAuditEntriesLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.Integer-java.lang.String-java.lang.Boolean-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-java.sql.Timestamp-java.sql.Timestamp-) (click on getAuditEntriesLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getAuditEntriesLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.audit.WsRestGetAuditEntriesLiteRequest-) (click on getAuditEntriesLite)
- For REST, the request can put data in the query string (in URL or request body)
- REST request (colon is escaped to %3A): GET /grouper-ws/servicesRest/v2_5_000/audits
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/audit/WsRestGetAuditEntriesLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetAuditEntriesResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetAuditEntriesResults.WsGetAuditEntriesResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/GRP-2153_add_audit_logs_web_service/grouper-ws/grouper-ws/doc/samples/getAuditEntries) (all files with "Lite" in them, click on "download" to see file)

#### Get audit entries service

- Accepts multiple owner groups, owner stems, or subjects
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getAuditEntries-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup-edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-java.lang.Integer-java.lang.String-java.lang.Boolean-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-java.sql.Timestamp-java.sql.Timestamp-) (click on getAuditEntries), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getAuditEntries-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.audit.WsRestGetAuditEntriesRequest-) (click on getAuditEntries)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v2_5_000/audits
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/audit/WsRestGetAuditEntriesRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetAuditEntriesResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetAuditEntriesResults.WsGetAuditEntriesResultsCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/GRP-2153_add_audit_logs_web_service/grouper-ws/grouper-ws/doc/samples/getAuditEntries) (all files without "Lite" in them, click on "download" to see files)

**See Also**

[Point in Time Auditing](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548000/Point+in+Time+Auditing)

[User Audit Log](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548937/User+Audit+Log)
