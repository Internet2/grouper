<%-- @annotation@ 
		 Standard tile used in baseDef which appears at the left
		 of all pages unless otherwise configured. Currently contains
		 the menu and can be 'relocated' to the menu bar by CSS - 
		 should separateso that left and menu are not one and same 
--%><%--
  @author Gary Brown.
  @version $Id: left.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<jsp:useBean id="tabStyle" class="java.util.HashMap"/>
<c:set  target="${tabStyle}" property="CreateGroups" value="tab"/>
<c:set  target="${tabStyle}" property="ManageGroups" value="tab"/>
<c:set  target="${tabStyle}" property="MyGroups" value="tab"/>
<c:set  target="${tabStyle}" property="JoinGroups" value="tab"/>
<c:set  target="${tabStyle}" property="AllGroups" value="tab"/>
<c:set  target="${tabStyle}" property="Help" value="tab"/>
<c:set  target="${tabStyle}" property="${functionalArea}" value="selectedTab"/>

<c:if test="${user!='SuperUser'}">
	<div class="actionbox"><%-- @annotation@ MyGroups tab --%>
		<html:link styleClass="${tabStyle.MyGroups}" page="/populateMyGroups.do" title="${navMap['groups.my.link.title']}">
			<fmt:message bundle="${nav}" key="groups.my"/>
		</html:link>
	</div>
</c:if>              
    <div class="actionbox"><%-- @annotation@ CreateGroups tab --%>
		<html:link styleClass="${tabStyle.CreateGroups}" page="/populateCreateGroups.do" title="${navMap['groups.create.link.title']}">
			<fmt:message bundle="${nav}" key="groups.create"/>
		</html:link>
	</div>
    <div class="actionbox"><%-- @annotation@ ManageGroups tab --%>
		<html:link styleClass="${tabStyle.ManageGroups}" page="/populateManageGroups.do" title="${navMap['groups.manage.link.title']}">
			<fmt:message bundle="${nav}" key="groups.manage"/>
		</html:link>
	</div>
	<c:if test="${user!='SuperUser'}">
    	<div class="actionbox"><%-- @annotation@ JoinGroups tab --%>
			<html:link styleClass="${tabStyle.JoinGroups}" page="/populateJoinGroups.do" title="${navMap['groups.join.link.title']}">
				<fmt:message bundle="${nav}" key="groups.join"/>
			</html:link>
		</div>
	</c:if> 
	<div class="actionbox"><%-- @annotation@ AllGroups tab --%>
			<html:link styleClass="${tabStyle.AllGroups}" page="/populateAllGroups.do" title="${navMap['groups.all.link.title']}">
				<fmt:message bundle="${nav}" key="groups.all"/>
			</html:link>
		</div>
		<div class="actionbox"><%-- @annotation@ AllGroups tab --%>
			<html:link styleClass="${tabStyle.Help}" page="/help.do" title="${navMap['groups.help.link.title']}">
				<fmt:message bundle="${nav}" key="groups.help"/>
			</html:link>
		</div>            
</grouper:recordTile>