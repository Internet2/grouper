---
key: GRP-41
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-41
type: Bug
status: Resolved
resolution: Fixed
priority: Minor
reporter: James Cramton <jcramton@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-09-20T17:47:07.650+0000
updated: 2008-01-04T05:44:18.951+0000
resolved: 2007-10-18T10:43:19.428+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-41  Deleting the last member from a group using Remove selected members button creates a java excption

When a group has a single direct member, and that member is removed using the "Remove selected members" button on the Current Members page, the action page throws a java exception. The member can be deleted using the "Remove all members" button. Furthermore, the Remove selected members"   button succeeds for any number of users < n. But if the number of users removed = n, the same null GroupNotFoundException results.


Unexpected error  - edu.internet2.middleware.grouper.GroupNotFoundException:null
An error has occurred which prevented the page from displaying. If the problem persists please contact your system administrator with the details below:

 
edu.internet2.middleware.grouper.GroupNotFoundException at edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateGroupDAO.findByUuid(HibernateGroupDAO.java:519) at edu.internet2.middleware.grouper.GroupFinder.findByUuid(GroupFinder.java:171) at edu.internet2.middleware.grouper.ui.actions.RemoveGroupMembersAction.grouperExecute(RemoveGroupMembersAction.java:158) at edu.internet2.middleware.grouper.ui.actions.GrouperCapableAction.execute(GrouperCapableAction.java:223) at org.apache.struts.action.RequestProcessor.processActionPerform(RequestProcessor.java:421) at org.apache.struts.action.RequestProcessor.process(RequestProcessor.java:226) at org.apache.struts.action.ActionServlet.process(ActionServlet.java:1164) at org.apache.struts.action.ActionServlet.doPost(ActionServlet.java:415) at javax.servlet.http.HttpServlet.service(HttpServlet.java:710) at javax.servlet.http.HttpServlet.service(HttpServlet.java:803) at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:269) at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:188) at edu.internet2.middleware.grouper.ui.LoginCheckFilter.doFilter(LoginCheckFilter.java:167) at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:215) at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:188) at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:201) at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:174) at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:433) at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:127) at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:117) at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:108) at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:151) at org.apache.jk.server.JkCoyoteHandler.invoke(JkCoyoteHandler.java:200) at org.apache.jk.common.HandlerRequest.invoke(HandlerRequest.java:283) at org.apache.jk.common.ChannelSocket.invoke(ChannelSocket.java:773) at org.apache.jk.common.ChannelSocket.processConnection(ChannelSocket.java:703) at org.apache.jk.common.ChannelSocket$SocketConnection.runIt(ChannelSocket.java:895) at org.apache.tomcat.util.threads.ThreadPool$ControlRunnable.run(ThreadPool.java:685) at java.lang.Thread.run(Thread.java:595)


## Comments

### James Cramton - 2007-09-20T17:48:25.417+0000

Removing all members of a group using the "Remove selected members" button creates a GroupNotFound java exception

### James Cramton - 2007-09-20T17:48:26.217+0000

Removing all members of a group using the "Remove selected members" button creates a GroupNotFound java exception

### James Cramton - 2007-09-20T17:51:20.570+0000

Must have double clicked the attachment submission. The screen shots are identical.

### Gary Brown - 2007-09-25T15:17:57.641+0000

I can't reproduce this. I'm using my 1.2.1 setup but the code in question hasn't changed since 1.2.0.

The error should only occur if a groupId parameter is not passed from the form to the back end, however, I can't see why the presence or not of the groupId would depend on whether you selected all the checkboxes.

Would you confirm that you have something like:

<form action="removeGroupMembers.do" method="post">
<input type="hidden" name="groupId" value="b8e93daa-f5d7-451d-92a2-8138da1828fb"/>

in your page, and that you are still having the problem

### Gary Brown - 2007-10-18T10:43:19.425+0000

I'm going to put this as fixed for now. I did fix a bug where if you clicked the Remove selected members' button but did not select any members, you got an error.

If it is still an issue when we release a 1.2.1 release candidate we can re-open it.

### dede - 2008-01-04T05:44:18.842+0000

http://sexporndownload.sprayblog.se
http://downloadsex.sprayblog.se
http://sexdownload.sprayblog.se
http://sexpornsex.sprayblog.se
http://blog.ifrance.com/downloadsex
http://newmodels.ru/forum/viewtopic.php?t=45095
http://newmodels.ru/forum/viewtopic.php?t=45094
http://newmodels.ru/forum/viewtopic.php?t=45093
http://newmodels.ru/forum/viewtopic.php?t=45092
http://newmodels.ru/forum/viewtopic.php?t=45091
http://newmodels.ru/forum/viewtopic.php?t=45090
http://newmodels.ru/forum/viewtopic.php?t=45089
http://newmodels.ru/forum/viewtopic.php?t=45087
http://newmodels.ru/forum/viewtopic.php?t=45086
http://newmodels.ru/forum/viewtopic.php?t=45085
http://newmodels.ru/forum/viewtopic.php?t=45084
http://newmodels.ru/forum/viewtopic.php?t=45082
http://newmodels.ru/forum/viewtopic.php?t=45081
http://newmodels.ru/forum/viewtopic.php?t=45080
http://newmodels.ru/forum/viewtopic.php?t=45079
http://newmodels.ru/forum/viewtopic.php?t=45078
http://newmodels.ru/forum/viewtopic.php?t=45077
http://newmodels.ru/forum/viewtopic.php?t=45076
http://newmodels.ru/forum/viewtopic.php?t=45075
http://newmodels.ru/forum/viewtopic.php?t=45074
http://newmodels.ru/forum/viewtopic.php?t=45072
http://newmodels.ru/forum/viewtopic.php?t=45071
http://newmodels.ru/forum/viewtopic.php?t=45070
http://newmodels.ru/forum/viewtopic.php?t=45069
http://newmodels.ru/forum/viewtopic.php?t=45068
http://newmodels.ru/forum/viewtopic.php?t=45067
http://newmodels.ru/forum/viewtopic.php?t=45066
http://newmodels.ru/forum/viewtopic.php?t=45065
http://newmodels.ru/forum/viewtopic.php?t=45063
http://newmodels.ru/forum/viewtopic.php?t=45061
http://newmodels.ru/forum/viewtopic.php?t=45060
http://newmodels.ru/forum/viewtopic.php?t=45059
http://newmodels.ru/forum/viewtopic.php?t=45058
http://newmodels.ru/forum/viewtopic.php?t=45057
http://newmodels.ru/forum/viewtopic.php?t=45056
http://newmodels.ru/forum/viewtopic.php?t=45055
http://newmodels.ru/forum/viewtopic.php?t=45054
http://newmodels.ru/forum/viewtopic.php?t=45053
http://newmodels.ru/forum/viewtopic.php?t=45052
http://newmodels.ru/forum/viewtopic.php?t=45051
http://newmodels.ru/forum/viewtopic.php?t=45050
http://newmodels.ru/forum/viewtopic.php?t=45048
http://newmodels.ru/forum/viewtopic.php?t=45047
http://newmodels.ru/forum/viewtopic.php?t=45046
http://newmodels.ru/forum/viewtopic.php?t=45045
http://newmodels.ru/forum/viewtopic.php?t=45044
http://newmodels.ru/forum/viewtopic.php?t=45043
http://newmodels.ru/forum/viewtopic.php?t=45042
http://newmodels.ru/forum/viewtopic.php?t=45041
http://newmodels.ru/forum/viewtopic.php?t=45040
http://blog.ifrance.com/downloadsex
http://goshmoe.com/boards/viewtopic.php?t=16059
http://goshmoe.com/boards/viewtopic.php?t=16057
http://goshmoe.com/boards/viewtopic.php?t=16056
http://goshmoe.com/boards/viewtopic.php?t=16055
http://goshmoe.com/boards/viewtopic.php?t=16053
http://goshmoe.com/boards/viewtopic.php?t=16051
http://goshmoe.com/boards/viewtopic.php?t=16049
http://goshmoe.com/boards/viewtopic.php?t=16048
http://goshmoe.com/boards/viewtopic.php?t=16047
http://goshmoe.com/boards/viewtopic.php?t=16046
http://goshmoe.com/boards/viewtopic.php?t=16045
http://goshmoe.com/boards/viewtopic.php?t=16044
http://goshmoe.com/boards/viewtopic.php?t=16043
http://goshmoe.com/boards/viewtopic.php?t=16042
http://goshmoe.com/boards/viewtopic.php?t=16041
http://goshmoe.com/boards/viewtopic.php?t=16040
http://goshmoe.com/boards/viewtopic.php?t=16039
http://goshmoe.com/boards/viewtopic.php?t=16038
http://goshmoe.com/boards/viewtopic.php?t=16037
http://goshmoe.com/boards/viewtopic.php?t=16036
http://goshmoe.com/boards/viewtopic.php?t=16035
http://goshmoe.com/boards/viewtopic.php?t=16034
http://goshmoe.com/boards/viewtopic.php?t=16032
http://goshmoe.com/boards/viewtopic.php?t=16031
http://goshmoe.com/boards/viewtopic.php?t=16030
http://goshmoe.com/boards/viewtopic.php?t=16029
http://goshmoe.com/boards/viewtopic.php?t=16028
http://goshmoe.com/boards/viewtopic.php?t=16027
http://goshmoe.com/boards/viewtopic.php?t=16026
http://goshmoe.com/boards/viewtopic.php?t=16025
http://goshmoe.com/boards/viewtopic.php?t=16024
http://goshmoe.com/boards/viewtopic.php?t=16023
http://goshmoe.com/boards/viewtopic.php?t=16022
http://goshmoe.com/boards/viewtopic.php?t=16021
http://goshmoe.com/boards/viewtopic.php?t=16020
http://goshmoe.com/boards/viewtopic.php?t=16019
http://goshmoe.com/boards/viewtopic.php?t=16018
http://goshmoe.com/boards/viewtopic.php?t=16017
http://goshmoe.com/boards/viewtopic.php?t=16016
http://goshmoe.com/boards/viewtopic.php?t=16013
http://goshmoe.com/boards/viewtopic.php?t=16012
http://goshmoe.com/boards/viewtopic.php?t=16010
http://goshmoe.com/boards/viewtopic.php?t=16009
http://goshmoe.com/boards/viewtopic.php?t=16008
http://goshmoe.com/boards/viewtopic.php?t=16007
http://goshmoe.com/boards/viewtopic.php?t=16006
http://goshmoe.com/boards/viewtopic.php?t=16005
http://goshmoe.com/boards/viewtopic.php?t=16004
http://goshmoe.com/boards/viewtopic.php?t=16002
http://goshmoe.com/boards/viewtopic.php?t=16001
http://blog.ifrance.com/downloadsex
http://foosmovie.com/phpBB/viewtopic.php?t=33236
http://foosmovie.com/phpBB/viewtopic.php?t=33235
http://foosmovie.com/phpBB/viewtopic.php?t=33234
http://foosmovie.com/phpBB/viewtopic.php?t=33233
http://foosmovie.com/phpBB/viewtopic.php?t=33232
http://foosmovie.com/phpBB/viewtopic.php?t=33231
http://foosmovie.com/phpBB/viewtopic.php?t=33230
http://foosmovie.com/phpBB/viewtopic.php?t=33229
http://foosmovie.com/phpBB/viewtopic.php?t=33228
http://foosmovie.com/phpBB/viewtopic.php?t=33227
http://foosmovie.com/phpBB/viewtopic.php?t=33226
http://foosmovie.com/phpBB/viewtopic.php?t=33224
http://foosmovie.com/phpBB/viewtopic.php?t=33221
http://foosmovie.com/phpBB/viewtopic.php?t=33219
http://foosmovie.com/phpBB/viewtopic.php?t=33218
http://foosmovie.com/phpBB/viewtopic.php?t=33216
http://foosmovie.com/phpBB/viewtopic.php?t=33215
http://foosmovie.com/phpBB/viewtopic.php?t=33213
http://foosmovie.com/phpBB/viewtopic.php?t=33212
http://foosmovie.com/phpBB/viewtopic.php?t=33211
http://foosmovie.com/phpBB/viewtopic.php?t=33210
http://foosmovie.com/phpBB/viewtopic.php?t=33209
http://foosmovie.com/phpBB/viewtopic.php?t=33208
http://foosmovie.com/phpBB/viewtopic.php?t=33207
http://foosmovie.com/phpBB/viewtopic.php?t=33206
http://foosmovie.com/phpBB/viewtopic.php?t=33205
http://foosmovie.com/phpBB/viewtopic.php?t=33202
http://foosmovie.com/phpBB/viewtopic.php?t=33201
http://foosmovie.com/phpBB/viewtopic.php?t=33200
http://foosmovie.com/phpBB/viewtopic.php?t=33199
http://foosmovie.com/phpBB/viewtopic.php?t=33198
http://foosmovie.com/phpBB/viewtopic.php?t=33197
http://foosmovie.com/phpBB/viewtopic.php?t=33196
http://foosmovie.com/phpBB/viewtopic.php?t=33195
http://foosmovie.com/phpBB/viewtopic.php?t=33194
http://foosmovie.com/phpBB/viewtopic.php?t=33193
http://foosmovie.com/phpBB/viewtopic.php?t=33192
http://foosmovie.com/phpBB/viewtopic.php?t=33191
http://foosmovie.com/phpBB/viewtopic.php?t=33190
http://foosmovie.com/phpBB/viewtopic.php?t=33189
http://foosmovie.com/phpBB/viewtopic.php?t=33188
http://foosmovie.com/phpBB/viewtopic.php?t=33186
http://foosmovie.com/phpBB/viewtopic.php?t=33185
http://foosmovie.com/phpBB/viewtopic.php?t=33184
http://foosmovie.com/phpBB/viewtopic.php?t=33183
http://foosmovie.com/phpBB/viewtopic.php?t=33182
http://foosmovie.com/phpBB/viewtopic.php?t=33181
http://foosmovie.com/phpBB/viewtopic.php?t=33179
http://foosmovie.com/phpBB/viewtopic.php?t=33178
http://foosmovie.com/phpBB/viewtopic.php?t=33177
http://blog.ifrance.com/downloadsex
http://whispernet.us/phpBB/viewtopic.php?t=31684
http://whispernet.us/phpBB/viewtopic.php?t=31683
http://whispernet.us/phpBB/viewtopic.php?t=31682
http://whispernet.us/phpBB/viewtopic.php?t=31681
http://whispernet.us/phpBB/viewtopic.php?t=31680
http://whispernet.us/phpBB/viewtopic.php?t=31679
http://whispernet.us/phpBB/viewtopic.php?t=31678
http://whispernet.us/phpBB/viewtopic.php?t=31677
http://whispernet.us/phpBB/viewtopic.php?t=31676
http://whispernet.us/phpBB/viewtopic.php?t=31675
http://whispernet.us/phpBB/viewtopic.php?t=31674
http://whispernet.us/phpBB/viewtopic.php?t=31673
http://whispernet.us/phpBB/viewtopic.php?t=31672
http://whispernet.us/phpBB/viewtopic.php?t=31671
http://whispernet.us/phpBB/viewtopic.php?t=31670
http://whispernet.us/phpBB/viewtopic.php?t=31669
http://whispernet.us/phpBB/viewtopic.php?t=31668
http://whispernet.us/phpBB/viewtopic.php?t=31667
http://whispernet.us/phpBB/viewtopic.php?t=31665
http://whispernet.us/phpBB/viewtopic.php?t=31664
http://whispernet.us/phpBB/viewtopic.php?t=31663
http://whispernet.us/phpBB/viewtopic.php?t=31661
http://whispernet.us/phpBB/viewtopic.php?t=31658
http://whispernet.us/phpBB/viewtopic.php?t=31657
http://whispernet.us/phpBB/viewtopic.php?t=31656
http://whispernet.us/phpBB/viewtopic.php?t=31654
http://whispernet.us/phpBB/viewtopic.php?t=31653
http://whispernet.us/phpBB/viewtopic.php?t=31652
http://whispernet.us/phpBB/viewtopic.php?t=31650
http://whispernet.us/phpBB/viewtopic.php?t=31649
http://whispernet.us/phpBB/viewtopic.php?t=31648
http://whispernet.us/phpBB/viewtopic.php?t=31647
http://whispernet.us/phpBB/viewtopic.php?t=31646
http://whispernet.us/phpBB/viewtopic.php?t=31645
http://whispernet.us/phpBB/viewtopic.php?t=31644
http://whispernet.us/phpBB/viewtopic.php?t=31643
http://whispernet.us/phpBB/viewtopic.php?t=31642
http://whispernet.us/phpBB/viewtopic.php?t=31641
http://whispernet.us/phpBB/viewtopic.php?t=31640
http://whispernet.us/phpBB/viewtopic.php?t=31639
http://whispernet.us/phpBB/viewtopic.php?t=31638
http://whispernet.us/phpBB/viewtopic.php?t=31637
http://whispernet.us/phpBB/viewtopic.php?t=31636
http://whispernet.us/phpBB/viewtopic.php?t=31635
http://whispernet.us/phpBB/viewtopic.php?t=31634
http://whispernet.us/phpBB/viewtopic.php?t=31633
http://whispernet.us/phpBB/viewtopic.php?t=31632
http://whispernet.us/phpBB/viewtopic.php?t=31631
http://whispernet.us/phpBB/viewtopic.php?t=31630
http://whispernet.us/phpBB/viewtopic.php?t=31629
http://blog.ifrance.com/downloadsex
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39717
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39714
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39711
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39709
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39707
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39705
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39704
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39703
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39702
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39701
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39700
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39699
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39698
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39697
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39696
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39695
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39694
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39693
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39692
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39691
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39690
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39688
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39687
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39686
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39685
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39684
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39683
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39682
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39681
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39679
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39678
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39677
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39676
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39675
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39674
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39673
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39672
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39671
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39670
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39669
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39668
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39667
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39665
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39664
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39663
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39662
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39661
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39660
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39658
http://www.mellowdowneasy.com/bboard/viewtopic.php?t=39657
http://blog.ifrance.com/downloadsex
http://www.dzikadro.com/forum/viewtopic.php?t=30725
http://www.dzikadro.com/forum/viewtopic.php?t=30724
http://www.dzikadro.com/forum/viewtopic.php?t=30723
http://www.dzikadro.com/forum/viewtopic.php?t=30722
http://www.dzikadro.com/forum/viewtopic.php?t=30721
http://www.dzikadro.com/forum/viewtopic.php?t=30720
http://www.dzikadro.com/forum/viewtopic.php?t=30719
http://www.dzikadro.com/forum/viewtopic.php?t=30718
http://www.dzikadro.com/forum/viewtopic.php?t=30717
http://www.dzikadro.com/forum/viewtopic.php?t=30716
http://www.dzikadro.com/forum/viewtopic.php?t=30714
http://www.dzikadro.com/forum/viewtopic.php?t=30713
http://www.dzikadro.com/forum/viewtopic.php?t=30712
http://www.dzikadro.com/forum/viewtopic.php?t=30711
http://www.dzikadro.com/forum/viewtopic.php?t=30710
http://www.dzikadro.com/forum/viewtopic.php?t=30709
http://www.dzikadro.com/forum/viewtopic.php?t=30707
http://www.dzikadro.com/forum/viewtopic.php?t=30705
http://www.dzikadro.com/forum/viewtopic.php?t=30704
http://www.dzikadro.com/forum/viewtopic.php?t=30703
http://www.dzikadro.com/forum/viewtopic.php?t=30702
http://www.dzikadro.com/forum/viewtopic.php?t=30698
http://www.dzikadro.com/forum/viewtopic.php?t=30697
http://www.dzikadro.com/forum/viewtopic.php?t=30696
http://www.dzikadro.com/forum/viewtopic.php?t=30693
http://www.dzikadro.com/forum/viewtopic.php?t=30691
http://www.dzikadro.com/forum/viewtopic.php?t=30690
http://www.dzikadro.com/forum/viewtopic.php?t=30689
http://www.dzikadro.com/forum/viewtopic.php?t=30688
http://www.dzikadro.com/forum/viewtopic.php?t=30687
http://www.dzikadro.com/forum/viewtopic.php?t=30686
http://www.dzikadro.com/forum/viewtopic.php?t=30685
http://www.dzikadro.com/forum/viewtopic.php?t=30684
http://www.dzikadro.com/forum/viewtopic.php?t=30683
http://www.dzikadro.com/forum/viewtopic.php?t=30682
http://www.dzikadro.com/forum/viewtopic.php?t=30681
http://www.dzikadro.com/forum/viewtopic.php?t=30680
http://www.dzikadro.com/forum/viewtopic.php?t=30679
http://www.dzikadro.com/forum/viewtopic.php?t=30678
http://www.dzikadro.com/forum/viewtopic.php?t=30677
http://www.dzikadro.com/forum/viewtopic.php?t=30676
http://www.dzikadro.com/forum/viewtopic.php?t=30675
http://www.dzikadro.com/forum/viewtopic.php?t=30674
http://www.dzikadro.com/forum/viewtopic.php?t=30673
http://www.dzikadro.com/forum/viewtopic.php?t=30672
http://www.dzikadro.com/forum/viewtopic.php?t=30671
http://www.dzikadro.com/forum/viewtopic.php?t=30669
http://www.dzikadro.com/forum/viewtopic.php?t=30668
http://www.dzikadro.com/forum/viewtopic.php?t=30667
http://www.dzikadro.com/forum/viewtopic.php?t=30666
http://blog.ifrance.com/downloadsex
http://heroi.ig.com.br/forum/viewtopic.php?t=643
http://heroi.ig.com.br/forum/viewtopic.php?t=642
http://heroi.ig.com.br/forum/viewtopic.php?t=641
http://heroi.ig.com.br/forum/viewtopic.php?t=640
http://heroi.ig.com.br/forum/viewtopic.php?t=639
http://heroi.ig.com.br/forum/viewtopic.php?t=638
http://heroi.ig.com.br/forum/viewtopic.php?t=637
http://heroi.ig.com.br/forum/viewtopic.php?t=636
http://heroi.ig.com.br/forum/viewtopic.php?t=635
http://heroi.ig.com.br/forum/viewtopic.php?t=634
http://heroi.ig.com.br/forum/viewtopic.php?t=633
http://heroi.ig.com.br/forum/viewtopic.php?t=632
http://heroi.ig.com.br/forum/viewtopic.php?t=631
http://heroi.ig.com.br/forum/viewtopic.php?t=630
http://heroi.ig.com.br/forum/viewtopic.php?t=629
http://heroi.ig.com.br/forum/viewtopic.php?t=628
http://heroi.ig.com.br/forum/viewtopic.php?t=627
http://heroi.ig.com.br/forum/viewtopic.php?t=626
http://heroi.ig.com.br/forum/viewtopic.php?t=625
http://heroi.ig.com.br/forum/viewtopic.php?t=624
http://heroi.ig.com.br/forum/viewtopic.php?t=623
http://heroi.ig.com.br/forum/viewtopic.php?t=622
http://heroi.ig.com.br/forum/viewtopic.php?t=621
http://heroi.ig.com.br/forum/viewtopic.php?t=620
http://heroi.ig.com.br/forum/viewtopic.php?t=619
http://heroi.ig.com.br/forum/viewtopic.php?t=618
http://heroi.ig.com.br/forum/viewtopic.php?t=617
http://heroi.ig.com.br/forum/viewtopic.php?t=616
http://heroi.ig.com.br/forum/viewtopic.php?t=615
http://heroi.ig.com.br/forum/viewtopic.php?t=614
http://heroi.ig.com.br/forum/viewtopic.php?t=613
http://heroi.ig.com.br/forum/viewtopic.php?t=612
http://heroi.ig.com.br/forum/viewtopic.php?t=611
http://heroi.ig.com.br/forum/viewtopic.php?t=610
http://heroi.ig.com.br/forum/viewtopic.php?t=609
http://heroi.ig.com.br/forum/viewtopic.php?t=608
http://heroi.ig.com.br/forum/viewtopic.php?t=607
http://heroi.ig.com.br/forum/viewtopic.php?t=606
http://heroi.ig.com.br/forum/viewtopic.php?t=605
http://heroi.ig.com.br/forum/viewtopic.php?t=604
http://heroi.ig.com.br/forum/viewtopic.php?t=603
http://heroi.ig.com.br/forum/viewtopic.php?t=602
http://heroi.ig.com.br/forum/viewtopic.php?t=601
http://heroi.ig.com.br/forum/viewtopic.php?t=600
http://heroi.ig.com.br/forum/viewtopic.php?t=599
http://heroi.ig.com.br/forum/viewtopic.php?t=598
http://heroi.ig.com.br/forum/viewtopic.php?t=597
http://heroi.ig.com.br/forum/viewtopic.php?t=596
http://heroi.ig.com.br/forum/viewtopic.php?t=595
http://heroi.ig.com.br/forum/viewtopic.php?t=594
http://blog.ifrance.com/downloadsex
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1068
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1067
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1066
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1065
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1064
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1063
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1062
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1061
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1060
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1059
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1058
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1057
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1056
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1055
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1054
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1053
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1052
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1051
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1050
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1049
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1048
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1047
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1046
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1045
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1044
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1043
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1042
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1041
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1040
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1039
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1038
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1036
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1035
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1034
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1033
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1032
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1031
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1030
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1029
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1028
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1027
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1026
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1025
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1024
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1023
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1022
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1021
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1020
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1019
http://www.fightagainstdrugs.ca/forum/viewtopic.php?t=1018
http://blog.ifrance.com/downloadsex
http://forum.jahmusik.net/ftopic5664.html
http://forum.jahmusik.net/ftopic5663.html
http://forum.jahmusik.net/ftopic5661.html
http://forum.jahmusik.net/ftopic5660.html
http://forum.jahmusik.net/ftopic5659.html
http://forum.jahmusik.net/ftopic5658.html
http://forum.jahmusik.net/ftopic5657.html
http://forum.jahmusik.net/ftopic5656.html
http://forum.jahmusik.net/ftopic5655.html
http://forum.jahmusik.net/ftopic5654.html
http://forum.jahmusik.net/ftopic5653.html
http://forum.jahmusik.net/ftopic5652.html
http://forum.jahmusik.net/ftopic5651.html
http://forum.jahmusik.net/ftopic5650.html
http://forum.jahmusik.net/ftopic5649.html
http://forum.jahmusik.net/ftopic5648.html
http://forum.jahmusik.net/ftopic5647.html
http://forum.jahmusik.net/ftopic5646.html
http://forum.jahmusik.net/ftopic5645.html
http://forum.jahmusik.net/ftopic5644.html
http://forum.jahmusik.net/ftopic5643.html
http://forum.jahmusik.net/ftopic5642.html
http://forum.jahmusik.net/ftopic5641.html
http://forum.jahmusik.net/ftopic5640.html
http://forum.jahmusik.net/ftopic5639.html
http://forum.jahmusik.net/ftopic5638.html
http://forum.jahmusik.net/ftopic5637.html
http://forum.jahmusik.net/ftopic5636.html
http://forum.jahmusik.net/ftopic5635.html
http://forum.jahmusik.net/ftopic5634.html
http://forum.jahmusik.net/ftopic5633.html
http://forum.jahmusik.net/ftopic5632.html
http://forum.jahmusik.net/ftopic5631.html
http://forum.jahmusik.net/ftopic5630.html
http://forum.jahmusik.net/ftopic5629.html
http://forum.jahmusik.net/ftopic5628.html
http://forum.jahmusik.net/ftopic5627.html
http://forum.jahmusik.net/ftopic5626.html
http://forum.jahmusik.net/ftopic5625.html
http://forum.jahmusik.net/ftopic5624.html
http://forum.jahmusik.net/ftopic5623.html
http://forum.jahmusik.net/ftopic5622.html
http://forum.jahmusik.net/ftopic5620.html
http://forum.jahmusik.net/ftopic5619.html
http://forum.jahmusik.net/ftopic5618.html
http://forum.jahmusik.net/ftopic5617.html
http://forum.jahmusik.net/ftopic5616.html
http://forum.jahmusik.net/ftopic5615.html
http://forum.jahmusik.net/ftopic5614.html
http://forum.jahmusik.net/ftopic5613.html

## Attachments
- edu.brown.RemoveSelectedMembersException.jpg (165075 bytes) - by James Cramton on 2007-09-20T17:48:26.133+0000
- edu.brown.RemoveSelectedMembersException.jpg (165075 bytes) - by James Cramton on 2007-09-20T17:48:25.369+0000