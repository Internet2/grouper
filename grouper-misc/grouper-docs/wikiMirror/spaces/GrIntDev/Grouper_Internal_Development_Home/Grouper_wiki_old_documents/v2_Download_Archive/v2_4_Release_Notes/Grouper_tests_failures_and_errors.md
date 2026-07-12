---
title: "Grouper tests - failures and errors"
space: GrIntDev
pageId: 48795877
version: 39
lastUpdated: 2026-07-12T07:02:39.647Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48795877/Grouper+tests+-+failures+and+errors
---

Current failures as of

```
Time: 20,707.905
There were 22 errors:
1) testProcessMessagesHappyPath(edu.internet2.middleware.grouper.app.messaging.MessageConsumerDaemonTest)java.lang.NoClassDefFoundError: org/mortbay/jetty/Handler
        at edu.internet2.middleware.grouper.app.messaging.MessageConsumerDaemonTest.testProcessMessagesHappyPath(MessageConsumerDaemonTest.java:107)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: java.lang.ClassNotFoundException: org.mortbay.jetty.Handler
        at java.net.URLClassLoader.findClass(URLClassLoader.java:381)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
        at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:349)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
        ... 28 more
2) testProcessMessagesInvalidInputMessages(edu.internet2.middleware.grouper.app.messaging.MessageConsumerDaemonTest)java.lang.NoClassDefFoundError: org/mortbay/jetty/Handler
        at edu.internet2.middleware.grouper.app.messaging.MessageConsumerDaemonTest.testProcessMessagesInvalidInputMessages(MessageConsumerDaemonTest.java:133)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: java.lang.ClassNotFoundException: org.mortbay.jetty.Handler
        at java.net.URLClassLoader.findClass(URLClassLoader.java:381)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
        at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:349)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
        ... 28 more
3) testPersonSyncFull(edu.internet2.middleware.grouper.app.tableSync.TableSyncTest)java.lang.RuntimeException: Cant find grouping column! testgrouper_sync_subject_from
        at edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata.getGroupingColumnMetadata(GcTableSyncTableMetadata.java:118)
        at edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync.fullSync(GcTableSync.java:634)
        at edu.internet2.middleware.grouper.app.tableSync.TableSyncTest.testPersonSyncFull(TableSyncTest.java:337)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
4) testRetrieveMembershipsByMember(edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAOTest)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSF8FC,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@5a28a108,
Problem in HibernateSession: HibernateSession (20337bf7): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (22d55aa2),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:i2, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (470195bc): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (22d55aa2)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAOTest.testRetrieveMembershipsByMember(Hib3MembershipDAOTest.java:83)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSF8FC,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@5a28a108,
Problem in HibernateSession: HibernateSession (20337bf7): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (22d55aa2),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 35 more
5) testGetMembershipsSources(edu.internet2.middleware.grouper.member.TestMember)java.lang.RuntimeException: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGC8H,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@c149c0a,
Problem in HibernateSession: HibernateSession (5ca41ae0): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (143bfe54),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:uofc, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (2c828a0b): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (143bfe54)
        at edu.internet2.middleware.grouper.helper.T.e(T.java:72)
        at edu.internet2.middleware.grouper.helper.GroupHelper.addMember(GroupHelper.java:84)
        at edu.internet2.middleware.grouper.member.TestMember.testGetMembershipsSources(TestMember.java:562)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGC8H,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@c149c0a,
Problem in HibernateSession: HibernateSession (5ca41ae0): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (143bfe54),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:uofc, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (2c828a0b): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (143bfe54)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:901)
        at edu.internet2.middleware.grouper.helper.GroupHelper.addMember(GroupHelper.java:80)
        ... 28 more
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGC8H,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@c149c0a,
Problem in HibernateSession: HibernateSession (5ca41ae0): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (143bfe54),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 33 more
6) testAddGroupMemberWithNonGroupMember(edu.internet2.middleware.grouper.member.TestAddMember)java.lang.RuntimeException: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGFLJ,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@1914955,
Problem in HibernateSession: HibernateSession (72b8825f): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (419db978),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:uofc, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (1a5d1b7b): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (419db978)
        at edu.internet2.middleware.grouper.helper.T.e(T.java:72)
        at edu.internet2.middleware.grouper.helper.GroupHelper.addMember(GroupHelper.java:84)
        at edu.internet2.middleware.grouper.member.TestAddMember.testAddGroupMemberWithNonGroupMember(TestAddMember.java:118)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGFLJ,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@1914955,
Problem in HibernateSession: HibernateSession (72b8825f): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (419db978),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:uofc, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (1a5d1b7b): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (419db978)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:901)
        at edu.internet2.middleware.grouper.helper.GroupHelper.addMember(GroupHelper.java:80)
        ... 28 more
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGFLJ,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@1914955,
Problem in HibernateSession: HibernateSession (72b8825f): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (419db978),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 33 more
7) testAddMember(edu.internet2.middleware.grouper.member.TestAddMember)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGFSH,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@43205deb,
Problem in HibernateSession: HibernateSession (3e2632bc): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (7ba0a2d0),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:i2, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (67f7ff69): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (7ba0a2d0)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.helper.GroupHelper.addMember(GroupHelper.java:108)
        at edu.internet2.middleware.grouper.member.TestAddMember.testAddMember(TestAddMember.java:89)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGFSH,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@43205deb,
Problem in HibernateSession: HibernateSession (3e2632bc): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (7ba0a2d0),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 34 more
8) testChangeSubjectDidExist(edu.internet2.middleware.grouper.member.TestMemberChangeSubject)java.lang.RuntimeException: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGFZ2,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@1c77aa1c,
Problem in HibernateSession: HibernateSession (646a3390): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (7c9016f9),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:group, subject: Subject id: test.subject.0, sourceId: jdbc, field: members,
Problem in HibernateSession: HibernateSession (3c6c9904): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (7c9016f9)
        at edu.internet2.middleware.grouper.member.TestMemberChangeSubject.setUp(TestMemberChangeSubject.java:283)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGFZ2,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@1c77aa1c,
Problem in HibernateSession: HibernateSession (646a3390): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (7c9016f9),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:group, subject: Subject id: test.subject.0, sourceId: jdbc, field: members,
Problem in HibernateSession: HibernateSession (3c6c9904): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (7c9016f9)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.member.TestMemberChangeSubject.setUp(TestMemberChangeSubject.java:200)
        ... 22 more
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGFZ2,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@1c77aa1c,
Problem in HibernateSession: HibernateSession (646a3390): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (7c9016f9),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 28 more
9) testChangeSubjectSameSubject(edu.internet2.middleware.grouper.member.TestMemberChangeSubject)java.lang.RuntimeException: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGF42,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@59fefe78,
Problem in HibernateSession: HibernateSession (99ab306): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (5f8d9071),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:group, subject: Subject id: test.subject.0, sourceId: jdbc, field: members,
Problem in HibernateSession: HibernateSession (12f3e975): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (5f8d9071)
        at edu.internet2.middleware.grouper.member.TestMemberChangeSubject.setUp(TestMemberChangeSubject.java:283)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGF42,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@59fefe78,
Problem in HibernateSession: HibernateSession (99ab306): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (5f8d9071),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:group, subject: Subject id: test.subject.0, sourceId: jdbc, field: members,
Problem in HibernateSession: HibernateSession (12f3e975): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (5f8d9071)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.member.TestMemberChangeSubject.setUp(TestMemberChangeSubject.java:200)
        ... 22 more
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGF42,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@59fefe78,
Problem in HibernateSession: HibernateSession (99ab306): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (5f8d9071),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 28 more
10) testChangeSubjectDidExistAudit(edu.internet2.middleware.grouper.member.TestMemberChangeSubject)java.lang.RuntimeException: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGF92,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@38ea00b8,
Problem in HibernateSession: HibernateSession (22e3ecf5): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (31a9e7dc),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:group, subject: Subject id: test.subject.0, sourceId: jdbc, field: members,
Problem in HibernateSession: HibernateSession (57d86527): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (31a9e7dc)
        at edu.internet2.middleware.grouper.member.TestMemberChangeSubject.setUp(TestMemberChangeSubject.java:283)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGF92,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@38ea00b8,
Problem in HibernateSession: HibernateSession (22e3ecf5): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (31a9e7dc),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:group, subject: Subject id: test.subject.0, sourceId: jdbc, field: members,
Problem in HibernateSession: HibernateSession (57d86527): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (31a9e7dc)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.member.TestMemberChangeSubject.setUp(TestMemberChangeSubject.java:200)
        ... 22 more
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGF92,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@38ea00b8,
Problem in HibernateSession: HibernateSession (22e3ecf5): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (31a9e7dc),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 28 more
11) testChangeSubjectDidntExist(edu.internet2.middleware.grouper.member.TestMemberChangeSubject)java.lang.RuntimeException: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGGE2,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@2c31e079,
Problem in HibernateSession: HibernateSession (3a7fbd90): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (41c3419b),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:group, subject: Subject id: test.subject.0, sourceId: jdbc, field: members,
Problem in HibernateSession: HibernateSession (7ea4f6ec): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (41c3419b)
        at edu.internet2.middleware.grouper.member.TestMemberChangeSubject.setUp(TestMemberChangeSubject.java:283)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGGE2,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@2c31e079,
Problem in HibernateSession: HibernateSession (3a7fbd90): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (41c3419b),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:group, subject: Subject id: test.subject.0, sourceId: jdbc, field: members,
Problem in HibernateSession: HibernateSession (7ea4f6ec): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (41c3419b)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.member.TestMemberChangeSubject.setUp(TestMemberChangeSubject.java:200)
        ... 22 more
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGGE2,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@2c31e079,
Problem in HibernateSession: HibernateSession (3a7fbd90): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (41c3419b),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 28 more
12) testGetDefaultSortIndex(edu.internet2.middleware.grouper.member.TestMemberAttributes)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGG2G,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@5d0c2a36,
Problem in HibernateSession: HibernateSession (4fac1f35): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (16975b6),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:allowGroup1, subject: Subject id: test.subject.0, sourceId: jdbc, field: members,
Problem in HibernateSession: HibernateSession (3a2d43c2): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (16975b6)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.member.TestMemberAttributes.testGetDefaultSortIndex(TestMemberAttributes.java:923)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGG2G,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@5d0c2a36,
Problem in HibernateSession: HibernateSession (4fac1f35): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (16975b6),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 33 more
13) testGetDefaultSearchIndex(edu.internet2.middleware.grouper.member.TestMemberAttributes)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGHU7,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@33871dac,
Problem in HibernateSession: HibernateSession (7d499d92): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (5f4181d2),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:allowGroup1, subject: Subject id: test.subject.0, sourceId: jdbc, field: members,
Problem in HibernateSession: HibernateSession (5d91844f): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (5f4181d2)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.member.TestMemberAttributes.testGetDefaultSearchIndex(TestMemberAttributes.java:809)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGHU7,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@33871dac,
Problem in HibernateSession: HibernateSession (7d499d92): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (5f4181d2),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 33 more
14) test_findMembers_findOneMember(edu.internet2.middleware.grouper.membership.TestMembershipFinder)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGY4K,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@77daf4d4,
Problem in HibernateSession: HibernateSession (3bbda65d): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (6cbf8842),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: top:child, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (2d388cf7): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (6cbf8842)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.membership.TestMembershipFinder.test_findMembers_findOneMember(TestMembershipFinder.java:741)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSGY4K,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@77daf4d4,
Problem in HibernateSession: HibernateSession (3bbda65d): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (6cbf8842),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 33 more
15) testHierarchies(edu.internet2.middleware.grouper.permissions.PermissionEntryTest)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSG9VX,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@370c6ec6,
Problem in HibernateSession: HibernateSession (67b1f1c0): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (4abe9b2a),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: top:roleParent, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (13da3a2a): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (4abe9b2a)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.permissions.PermissionEntryTest.testHierarchies(PermissionEntryTest.java:751)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSG9VX,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@370c6ec6,
Problem in HibernateSession: HibernateSession (67b1f1c0): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (4abe9b2a),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 32 more
16) testByOwnerImmediateMembership(edu.internet2.middleware.grouper.pit.PITAttributeAssignTests)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSHE4R,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@404af24f,
Problem in HibernateSession: HibernateSession (419d9dec): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (617b4812),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:testGroup, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (72892f3c): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (617b4812)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.pit.PITAttributeAssignTests.testByOwnerImmediateMembership(PITAttributeAssignTests.java:435)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSHE4R,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@404af24f,
Problem in HibernateSession: HibernateSession (419d9dec): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (617b4812),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 33 more
17) testByOwnerEffectiveMembership(edu.internet2.middleware.grouper.pit.PITAttributeAssignTests)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSHE7B,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@3cda5092,
Problem in HibernateSession: HibernateSession (e991f6e): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (602f1b2d),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:testGroup, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (53d83769): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (602f1b2d)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.pit.PITAttributeAssignTests.testByOwnerEffectiveMembership(PITAttributeAssignTests.java:507)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSHE7B,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@3cda5092,
Problem in HibernateSession: HibernateSession (e991f6e): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (602f1b2d),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 33 more
18) testMembershipEnableDisable(edu.internet2.middleware.grouper.pit.PITAttributeAssignValueTests)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSHF8E,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@1b6f1e68,
Problem in HibernateSession: HibernateSession (317549d2): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (38b60a91),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:testGroup, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (7196f108): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (38b60a91)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.pit.PITAttributeAssignValueTests.testMembershipEnableDisable(PITAttributeAssignValueTests.java:1071)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSHF8E,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@1b6f1e68,
Problem in HibernateSession: HibernateSession (317549d2): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (38b60a91),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 33 more
19) testByOwnerImmediateMembership(edu.internet2.middleware.grouper.pit.PITAttributeAssignValueTests)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSHGF2,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@1ed753c7,
Problem in HibernateSession: HibernateSession (6e00b9e0): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (38872bed),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:testGroup, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (2455d807): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (38872bed)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.pit.PITAttributeAssignValueTests.testByOwnerImmediateMembership(PITAttributeAssignValueTests.java:731)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSHGF2,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@1ed753c7,
Problem in HibernateSession: HibernateSession (6e00b9e0): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (38872bed),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 33 more
20) testByOwnerEffectiveMembership(edu.internet2.middleware.grouper.pit.PITAttributeAssignValueTests)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSHGIM,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@58d285fe,
Problem in HibernateSession: HibernateSession (1ce8826c): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (3641db69),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: edu:testGroup, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (7c2315f4): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (3641db69)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.pit.PITAttributeAssignValueTests.testByOwnerEffectiveMembership(PITAttributeAssignValueTests.java:812)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSHGIM,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@58d285fe,
Problem in HibernateSession: HibernateSession (1ce8826c): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (3641db69),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 33 more
21) testRuleVetoSubjectAssignInFolderInherit(edu.internet2.middleware.grouper.rules.RuleApiTest)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSIDSP,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@872a55a,
Problem in HibernateSession: HibernateSession (74fb57a8): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (96fb880),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: a:group, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (7933541f): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (96fb880)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.rules.RuleApiTest.testRuleVetoSubjectAssignInFolderInherit(RuleApiTest.java:1659)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSIDSP,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@872a55a,
Problem in HibernateSession: HibernateSession (74fb57a8): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (96fb880),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 33 more
22) testRuleLonghandIfElMoreApi(edu.internet2.middleware.grouper.rules.RuleTest)java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSIJFK,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@71e9f556,
Problem in HibernateSession: HibernateSession (3e049f92): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (22c91870),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: etc:rulesAccessToApi, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (552da9e9): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (22c91870)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.rules.RuleTest.testRuleLonghandIfElMoreApi(RuleTest.java:485)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSIJFK,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@71e9f556,
Problem in HibernateSession: HibernateSession (3e049f92): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (22c91870),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 32 more
There were 5 failures:
1) testSubjectFinder(edu.internet2.middleware.grouper.entity.EntityFinderTest)junit.framework.AssertionFailedError: Expected: <null> but was: Subject id: b695abb524e843a29172b56a6b57d0e2, sourceId: grouperEntities, name: test:testEntity
        at edu.internet2.middleware.grouper.entity.EntityFinderTest.testSubjectFinder(EntityFinderTest.java:470)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
2) testReadonlyViewonlyAdmin(edu.internet2.middleware.grouper.group.TestGroupReadonlyViewonly)junit.framework.AssertionFailedError: Expected: <null> but was: AttributeDef[name=test:testAttributeDef,uuid=cceb436c0f3b4411b04601e5f07abece]
        at edu.internet2.middleware.grouper.group.TestGroupReadonlyViewonly.testReadonlyViewonlyAdmin(TestGroupReadonlyViewonly.java:228)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
3) test_hasPrivilege_cacheSize(edu.internet2.middleware.grouper.privs.Test_privs_CachingAccessResolver)junit.framework.AssertionFailedError: expected:<8> but was:<16>
        at edu.internet2.middleware.grouper.privs.Test_privs_CachingAccessResolver.test_hasPrivilege_cacheSize(Test_privs_CachingAccessResolver.java:202)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
4) testUpdateOkDoNotAddMissingGroups(edu.internet2.middleware.grouper.xml.TestXml)junit.framework.AssertionFailedError: UNEXPECTED EXCEPTION: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSJDU6,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@86adc23,
Problem in HibernateSession: HibernateSession (1295221a): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (7ea66e4f),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: i2:a:b, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (65789376): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (7ea66e4f)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.xml.TestXml.testUpdateOkDoNotAddMissingGroups(TestXml.java:1295)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSJDU6,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@86adc23,
Problem in HibernateSession: HibernateSession (1295221a): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (7ea66e4f),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 33 more

        at edu.internet2.middleware.grouper.helper.GrouperTest.unexpectedException(GrouperTest.java:983)
        at edu.internet2.middleware.grouper.xml.TestXml.testUpdateOkDoNotAddMissingGroups(TestXml.java:1330)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
5) testUpdateOkDoNotAddMissingGroups(edu.internet2.middleware.grouper.xml.importXml.XmlLegacyTest)junit.framework.AssertionFailedError: UNEXPECTED EXCEPTION: java.lang.RuntimeException: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSJKIF,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@36e24fcc,
Problem in HibernateSession: HibernateSession (6a5388f2): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (5a85f03b),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null,
, group name: i2:a:b, subject: Subject id: GrouperSystem, sourceId: g:isa, field: members,
Problem in HibernateSession: HibernateSession (e50e01): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (5a85f03b)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1412)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.Group.internal_addMember(Group.java:1366)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:940)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:859)
        at edu.internet2.middleware.grouper.Group.addMember(Group.java:822)
        at edu.internet2.middleware.grouper.xml.importXml.XmlLegacyTest.testUpdateOkDoNotAddMissingGroups(XmlLegacyTest.java:795)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)
Caused by: edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException: Cannot find (or not allowed to find) attribute def name with name: 'etc:attribute:cannotAddSelfToGroup:cannotAddSelfAttributeDefName',
Hook MembershipCannotAddSelfToGroupHook.membershipPreAddMember id: UETSJKIF,
Exception in save: edu.internet2.middleware.grouper.Membership, edu.internet2.middleware.grouper.hibernate.ByObject@36e24fcc,
Problem in HibernateSession: HibernateSession (6a5388f2): notNew, notReadonly, READ_WRITE_NEW, activeTransaction, session (5a85f03b),
Exception in save: edu.internet2.middleware.grouper.Membership, ByObjectStatic, query: ', cacheable: null, cacheRegion: null, entityName: ImmediateMembershipEntry, tx type: null
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByName(Hib3AttributeDefNameDAO.java:1494)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO.findByNameSecure(Hib3AttributeDefNameDAO.java:261)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:392)
        at edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(AttributeDefNameFinder.java:405)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$5.callback(MembershipCannotAddSelfToGroupHook.java:244)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName(MembershipCannotAddSelfToGroupHook.java:240)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook$6.callback(MembershipCannotAddSelfToGroupHook.java:297)
        at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:976)
        at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1024)
        at edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook.membershipPreAddMember(MembershipCannotAddSelfToGroupHook.java:292)
        at sun.reflect.GeneratedMethodAccessor1586.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.util.GrouperUtil.invokeMethod(GrouperUtil.java:4190)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.executeHook(GrouperHooksUtils.java:493)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:293)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:232)
        at edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils.callHooksIfRegistered(GrouperHooksUtils.java:195)
        at edu.internet2.middleware.grouper.Membership.onPreSave(Membership.java:1798)
        at edu.internet2.middleware.grouper.hibernate.ByObject.save(ByObject.java:208)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic$7.callback(ByObjectStatic.java:494)
        at edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:703)
        at edu.internet2.middleware.grouper.hibernate.ByObjectStatic.save(ByObjectStatic.java:481)
        at edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO.save(Hib3MembershipDAO.java:2175)
        at edu.internet2.middleware.grouper.Membership.internal_addImmediateMembership(Membership.java:1271)
        at edu.internet2.middleware.grouper.Group$4.callback(Group.java:1394)
        ... 35 more

        at edu.internet2.middleware.grouper.helper.GrouperTest.unexpectedException(GrouperTest.java:983)
        at edu.internet2.middleware.grouper.xml.importXml.XmlLegacyTest.testUpdateOkDoNotAddMissingGroups(XmlLegacyTest.java:834)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.AllTests.main(AllTests.java:162)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.handleSpecialCase(GrouperShell.java:224)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShell.main(GrouperShell.java:158)
        at edu.internet2.middleware.grouper.app.gsh.GrouperShellWrapper.main(GrouperShellWrapper.java:31)

FAILURES!!!
Tests run: 2434,  Failures: 5,  Errors: 22

[mchyzer@i2midev6 grouper]$ 
```

| Test | Error/Failure | Assignee | Status | Notes | Fixed in patch |
| --- | --- | --- | --- | --- | --- |
| TestStem#testStemObliterate2AttributeDef2 | Failure | Vivek | Fixed in master |  | grouper_v2_4_0_api_patch_75 |
| TestStem#testStemObliterate2AttributeDefName2 | Failure | Vivek | Fixed in master |  | grouper_v2_4_0_api_patch_75 |
| GrouperLoaderOtherJobsTest#testScheduleJobs | Failure | Shilen | Fixed in master |  | grouper_v2_4_0_api_patch_75 |
| AttributeAssignTest#testFindOwnersMembershipImmediate | Failure | Chris | Fixed in master |  | grouper_v2_4_0_api_patch_75 |
| ChangeLogTest#testGroups | Failure |  |  | Does not fail when only ChangeLogTest file is run |  |
| ChangeLogTest#testStemRenameOrder | Failure |  |  | Does not fail when only ChangeLogTest file is run |  |
| EntityFinderTest#testSubjectFinder | Failure |  |  | Seems like retrieving subjects from cache does not take privileges into account |  |
| TestGroupReadonlyViewonly#testReadonlyViewonlyAdmin | Failure |  |  | Again cache issue. It looks like attribute defs might be getting added to the flash cache for subjects who don't have access to them. |  |
| MembershipCannotAddSelfToGroupHookTest#testHook | Failure |  |  | Does not fail when only MembershipCannotAddSelfToGroupHookTest file is run |  |
| MembershipOneInFolderMaxHookTest#testGroupRemovesMembership | Failure |  |  | Does not fail when only MembershipOneInFolderMaxHookTest file is run |  |
| MembershipOneInFolderMaxHookTest#testGroupRemovesMultipleMemberships | Failure |  |  | Does not fail when only MembershipOneInFolderMaxHookTest file is run |  |
| TestMemberAttributes#testGroupUpdateDisplayExtension | Failure |  |  | Does not fail when only TestMemberAttributes file is run |  |
| TestMemberAttributes#testGroupMove | Failure |  |  | Does not fail when only TestMemberAttributes file is run |  |
| TestMemberAttributes#testStemRename | Failure |  |  | Does not fail when only TestMemberAttributes file is run |  |
| TestMemberAttributes#testGroupRename | Failure |  |  | Does not fail when only TestMemberAttributes file is run |  |
| TestMemberAttributes#testInternalMembersNonDefaultAttributes | Failure |  |  | Does not fail when only TestMemberAttributes file is run |  |
| TestMemberAttributes#testPersonMember | Failure |  |  | Does not fail when only TestMemberAttributes file is run |  |
| TestMemberAttributes#testStemDisplayExtensionUpdate | Failure |  |  | Does not fail when only TestMemberAttributes file is run |  |
| TestMemberAttributes#testGroupUpdateDescription | Failure |  |  | Does not fail when only TestMemberAttributes file is run |  |
| PITSyncTests#testSubjectIdentifierUpdate | Failure |  |  | Does not fail when only PITSyncTests file is run |  |
| Test_privs_CachingAccessResolver#test_hasPrivilege_cacheSize | Failure |  |  |  |  |
| TestPrivileges#testGrantStemToGroup | Failure |  |  | Does not fail when only TestPrivileges file is run |  |
| GrouperLoaderTest#  testIncrementalLoaderListSubjectIdAndSourceIdCaseInsensitive | Failure | Shilen | Fixed in master |  | grouper_v2_4_0_api_patch_75 |
| GrouperLoaderTest#  testIncrementalLoaderSimpleSubjectIdAndSourceCaseInsensitive | Failure | Shilen | Fixed in master |  | grouper_v2_4_0_api_patch_75 |
| GrouperLoaderTest#testLoaderTypesGroupMeta | Failure | Chad | Fixed in master had wrong config settings | since GRP-1909, description for loaded groups no longer defaulting to "auto-created by grouperLoader" |  |
| TableSyncTest.testPersonSyncFull | Failure (Postgres) |  |  | GRP-2323: SQL table sync job not working with postgres |  |
| GrouperWorkflowConfigValidatorTest#testAllBasicValidations | Failure | Chad | Fixed in master | add back the fake groupId to trigger the expected error |  |
| TestRegistrySubject#testSubjects | Error | Vivek | Fixed in master |  | grouper_v2_4_0_api_patch_75 |
| MessageConsumerDaemonTest#testProcessMessagesHappyPath | Error | Vivek | Fixed in master |  |  |
| MessageConsumerDaemonTest#testProcessMessagesInvalidInputMessages | Error | Vivek | Fixed in master |  |  |
| UpgradeTasksJobTest#testVersion1 | Error | Shilen | Cannot reproduce? |  |  |
| Test_I_API_Group_deleteMember#test_DeleteUnresolvableSubject | Error | Shilen | Fixed in master |  | grouper_v2_4_0_api_patch_75 |
| GroupUniqueNameCaseInsensitiveHookTest#testHook | Error |  |  |  |  |
| StemUniqueNameCaseInsensitiveHookTest#testHook | Error |  |  |  |  |
| TestPrivSTEM#testRevokeStem | Error | Vivek | Fixed in master | Fail only when running the whole file. Probably subject cache is the issue. |  |
| TestPrivSTEM#testModifyAttrsFail | Error | Vivek | Fixed in master | Fail only when running the whole file. Probably subject cache is the issue. |  |
| TestPrivSTEM#testGrantStem | Error | Vivek | Fixed in master | Fail only when running the whole file. Probably subject cache is the issue. |  |
| TestPrivSTEM#testModifyttrs | Error | Vivek | Fixed in master | Fail only when running the whole file. Probably subject cache is the issue. |  |
| TestPrivSTEM#testRevokeAllStem | Error | Vivek | Fixed in master | Fail only when running the whole file. Probably subject cache is the issue. |  |
| TestPrivSTEM_ADMIN#testRevokeStemAdmin | Error | Vivek | Fixed in master | Fail only when running the whole file. Probably subject cache is the issue. |  |
| TestPrivSTEM_ADMIN#testModifyAttrsFail | Error | Vivek | Fixed in master | Fail only when running the whole file. Probably subject cache is the issue. |  |
| TestPrivSTEM_ADMIN#testRevokeAllStemAdmin | Error | Vivek | Fixed in master | Fail only when running the whole file. Probably subject cache is the issue. |  |
| TestPrivSTEM_ADMIN#testModifyttrs | Error | Vivek | Fixed in master | Fail only when running the whole file. Probably subject cache is the issue. |  |
| TestPrivSTEM_ADMIN#testRevokeAllCreate | Error | Vivek | Fixed in master | Fail only when running the whole file. Probably subject cache is the issue. |  |
| AttributeAssignTest (several tests) | Error | Shilen | Done in branch GRP-2370-hsql-attribute-queries-with-order-by. Needs to be merged to master. | HSQL doesn't appear to like select count(*) from ... order by ... | grouper_v2_4_0_api_patch_76 |
| GrouperDdlUtilsTest#testBootstrapHelper | Error |  |  | Appears to fail because it's trying to find the grouper_config table after all the tables are dropped but before they are re-added. |  |
| TestMemberAttributes#testInternalMembersNonDefaultAttributes | Failure | Shilen | Fixed in master |  |  |
| RuleApiTest#testRuleVetoSubjectAssignInFolderIfNotInGroupChangeLogConsumer | Failure | Shilen | Fixed in master |  |  |
| GrouperWorkflowInstanceServiceTest#  testSaveOrUpdateWorkflowInstance | Error | Shilen | Fixed in master |  |  |
| PermissionPerformanceTest.testPerformance | Failure | Chad | WONTFIX - Passes if statistics are updated before the query | Postgres PIT attribute query takes 6 minutes instead of 90 seconds |  |
| GrouperReportConfigServiceTest | Other | Chad |  | Tests pass, but the job scheduler in the class locks the JVM so it never exits (this was seen in another class a few years ago, and is fixable) |  |
