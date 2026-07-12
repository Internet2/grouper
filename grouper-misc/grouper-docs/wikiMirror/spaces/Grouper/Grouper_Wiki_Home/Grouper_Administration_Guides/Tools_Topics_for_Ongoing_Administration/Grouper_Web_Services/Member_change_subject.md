---
title: "Member change subject"
space: Grouper
pageId: 28549205
version: 9
lastUpdated: 2026-07-01T05:42:32.450Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549205/Member+change+subject
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

"[Member change subject](https://bugs.internet2.edu/jira/browse/GRP-151)" will change the subject that a member refers to. You would want to do this when a person or entity changes their id, or if they were loaded wrong in the system. If the new subject does not have a member associated with it, this is a simple case, where the subject data is put in the member object. If the new subject does have a member object, then all data in all tables that referred to the old member object, will now refer to the new member object. The old member is deleted from the member table by default, though this is an option. Generally you will want it removed, unless there is a foreign key problem where you need to do as much work as possible. In GSH you can get a dry-run report of what will be done.

The operation is potentially time consuming only when two formerly separate Subjects are being merged into one, and that the time required is to replace the memberships (and audit fields e.g. modifiedBy) of the formerly separate Subject that is being retired with new ones associated with the other Subject.

#### Features

- Will not fail if the new subject is the same as the old subject
- Lookup subjects/members by subject lookup (by id, source, identifier, etc)
- Returns subject information of the new subject
- Can actAs another user

#### Member change subject Lite service

- Accepts one subject (new) and one member (old) to change the subject information
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#memberChangeSubjectLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-boolean-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on memberChangeSubjectLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#memberChangeSubjectLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectLiteRequest-) (click on memberChangeSubjectLite)
- For REST, a request body is required (probably via POST)
- REST request (colon is escaped to %3A): PUT /grouper-ws/servicesRest/v1_4_000/members/10021368
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/member/WsRestMemberChangeSubjectLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsMemberChangeSubjectLiteResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsMemberChangeSubjectLiteResult.WsMemberChangeSubjectLiteResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/memberChangeSubject/) (all files with "Lite" in them, click on "download" to see file)

#### Member change subject service

- Accepts a list of subjects to change, and subjects to change to (and if the old should be deleted), to change the subjects of members
- Can operate in one transaction, or can let each change occur in its own separate unit
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#memberChangeSubject-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsMemberChangeSubject:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.hibernate.GrouperTransactionType-boolean-java.lang.String:A-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on memberChangeSubject), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#memberChangeSubject-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectRequest-) (click on memberChangeSubject)
- REST request (colon is escaped to %3A): PUT /grouper-ws/servicesRest/v1_4_000/members
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/member/WsRestMemberChangeSubjectRequest.html), [response object](//software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsMemberChangeSubjectResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsMemberChangeSubjectResults.WsMemberChangeSubjectResultsCode.html), [response codes for each assignment](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsMemberChangeSubjectResult.WsMemberChangeSubjectResultCode.html)
- Returns an overall status, and a status for each change
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/memberChangeSubject/) (all files without "Lite" in them, click on "download" to see files)
