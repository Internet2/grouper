---
key: GRP-79
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-79
type: Bug
status: Closed
resolution: Fixed
priority: Major
reporter: Chris Hyzer <mchyzer@example.com>
assignee: Shilen Patel <shilen@example.com>
created: 2008-01-11T21:04:46.937+0000
updated: 2009-10-20T17:22:57.772+0000
resolved: 2009-10-20T17:22:57.779+0000
components: [API]
fixVersions: [1.5.0]
labels: []
links: [is duplicated by GRP-47]
---

# GRP-79  HibernateMembershipDAO  GrouperDAOException: query did not return a unique result: 2

I was working, and got this exception (below).

If there are multiple HibernateMemberships by owner_id, member_id, list_name, list_type, type, then anything that calls HibernateMembershipDAO. findByOwnerAndMemberAndFieldAndType() will fail (and there are 7 methods that call it).  So I *think* we need either:
>
> 1. A constraint on those 5 columns (this table already has a unique constraint on other columns, so either that one can be changed, or another added, if its possible for a table to have multiple unique constraints, I havent seen an example of this with hibernate mappings)
> -or-
> 2. Change the method and all callers so that it returns a Set and not 
> one single membership object
> -or-
> 3. Some other re-code somewhere

Here are Gary's thoguhts:

I'm not sure if there ever was a database constraint - if there were it may have gone by the wayside when we started using Hibernate to create the schema. I wouldn't be averse to having one.

In principle it should never happen  - ImmediateMembershipValidator checks for an existing immediate membership, but I know someone (James?) reported a problem when two loader programs kicked off by accident - may be if two threads / processes try to add the same Membership at the same time they get through? I've also had the problem once and had to fix it in the database.

In this case, assuming that getMembers and getMemberships work OK for the group, I would be tempted to log an error, but simply return the first result - either ignoring the second result, or actually removing it...

(Chris again), its hard to tell exactly if the data is duplicated or not, and now it is gone...
######################

edu.internet2.middleware.grouper.internal.dao.GrouperDAOException: query did not return a unique result: 2
      at edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateMembershipDAO.findByOwnerAndMemberAndFieldAndType(HibernateMembershipDAO.java:347)
      at edu.internet2.middleware.grouper.MembershipFinder.findImmediateMembership(MembershipFinder.java:162)
      at edu.internet2.middleware.grouper.Member.isImmediateMember(Member.java:998)
      at edu.internet2.middleware.grouper.Group.hasImmediateMember(Group.java:1684)
      at edu.internet2.middleware.grouper.Group.hasImmediateMember(Group.java:1653)
      at edu.internet2.middleware.grouper.webservices.GrouperService.addMember(GrouperService.java:142)
      at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
      at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
      at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
      at java.lang.reflect.Method.invoke(Method.java:585)
      at org.apache.axis2.rpc.receivers.RPCUtil.invokeServiceClass(RPCUtil.java:194)
      at org.apache.axis2.rpc.receivers.RPCMessageReceiver.invokeBusinessLogic(RPCMessageReceiver.java:98)
      at org.apache.axis2.receivers.AbstractInOutMessageReceiver.invokeBusinessLogic(AbstractInOutMessageReceiver.java:40)
      at org.apache.axis2.receivers.AbstractMessageReceiver.receive(AbstractMessageReceiver.java:96)
      at org.apache.axis2.engine.AxisEngine.receive(AxisEngine.java:145)
      at org.apache.axis2.transport.http.HTTPTransportUtils.processHTTPPostRequest(HTTPTransportUtils.java:275)
      at org.apache.axis2.transport.http.AxisServlet.doPost(AxisServlet.java:120)
      at javax.servlet.http.HttpServlet.service(HttpServlet.java:710)
      at javax.servlet.http.HttpServlet.service(HttpServlet.java:803)
      at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:290)
      at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:206)
      at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:233)
      at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:175)
      at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:433)
      at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:128)
      at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:102)
      at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:109)
      at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:263)
      at org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:844)
      at org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:584)
      at org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:447)
      at java.lang.Thread.run(Thread.java:595)
Caused by: net.sf.hibernate.NonUniqueResultException: query did not return a unique result: 2
      at net.sf.hibernate.impl.AbstractQueryImpl.uniqueElement(AbstractQueryImpl.java:559)
      at net.sf.hibernate.impl.AbstractQueryImpl.uniqueResult(AbstractQueryImpl.java:550)
      at edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateMembershipDAO.findByOwnerAndMemberAndFieldAndType(HibernateMembershipDAO.java:339)
      ... 31 more


I think these are the two rows.  I deleted them, and then I am all set.  I couldn't reproduce it, I don't know how they both got added, Ive been working minimally with the UI and API (no loads or anything).


INSERT INTO GROUPER_MEMBERSHIPS ( ID, OWNER_ID, MEMBER_ID, LIST_NAME, LIST_TYPE, MSHIP_TYPE, VIA_ID,
DEPTH, PARENT_MEMBERSHIP, MEMBERSHIP_UUID, CREATOR_ID, CREATE_TIME ) VALUES ( 
'4028818217679b9c0117679bc9ef0004', '19284537-6118-44b2-bbbc-d5757c709cb7', 'ce99ba96-1643-43c5-b18c-037d13dd16e8'
, 'members', 'list', 'immediate', NULL, 0, NULL, '0d285a9b-c71f-453b-9655-b15b484fbe94'
, 'a4043887-a0bb-44ba-9af9-cb9d39936aa7', 1200034133639); 
INSERT INTO GROUPER_MEMBERSHIPS ( ID, OWNER_ID, MEMBER_ID, LIST_NAME, LIST_TYPE, MSHIP_TYPE, VIA_ID,
DEPTH, PARENT_MEMBERSHIP, MEMBERSHIP_UUID, CREATOR_ID, CREATE_TIME ) VALUES ( 
'4028818217679b9c0117679bc9ef0003', '19284537-6118-44b2-bbbc-d5757c709cb7', 'ce99ba96-1643-43c5-b18c-037d13dd16e8'
, 'members', 'list', 'immediate', NULL, 0, NULL, '44df61c0-af0f-46db-836e-20fcbd8b87aa'
, 'a4043887-a0bb-44ba-9af9-cb9d39936aa7', 1200034133451); 
COMMIT;


## Comments

### shilen - 2008-01-14T12:56:57.642+0000

Because a person can be a member of a group in multiple ways due to effective memberships, I think a unique constraint would at least have to include the column parent_membership.  However, since parent_membership can be null, that's going to cause a problem in some DBMS, such as Microsoft SQL Server.  So I think the solution to this problem needs to either involve a check constraint or a modification to the code.  I'll continue working on this.

### mchyzer - 2008-06-25T16:44:48.260+0000

We are currently thinking post-1.4

### shilen - 2009-08-19T12:11:14.009+0000

This will be fixed in 1.5 as we restructure the membership table to prevent duplicate memberships.

### shilen - 2009-10-20T17:22:57.740+0000

Duplicate immediate memberships are no longer possible in 1.5 due to database constraints.