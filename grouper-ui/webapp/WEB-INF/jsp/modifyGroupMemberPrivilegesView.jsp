<%-- @annotation@
		  Form embedded in another page which allows a Subject`s
		  membership / privileges for the active group to be changed
--%><%--
  @author Gary Brown.
  @version $Id: modifyGroupMemberPrivilegesView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<html:form styleId="MemberPrivilegesForm" action="/saveGroupMember">
<fieldset>
	<html:hidden property="asMemberOf"/>
	<html:hidden property="subjectId"/>
	<html:hidden property="subjectType"/>
	<html:hidden property="privilege"/>
	
	<c:if test="${authUserPriv.UPDATE || authUserPriv.ADMIN}">
	<html:multibox property="privileges" value="MEMBER" /> <fmt:message bundle="${nav}" key="priv.member"/><br/>
	</c:if>
	<c:if test="${authUserPriv.ADMIN}">
		<c:forEach var="priv" items="${possiblePrivs}">
			<html:multibox property="privileges" value="${priv}" styleId="priv${priv}"/> 
				<label for="priv<c:out value="${priv}"/>"><fmt:message bundle="${nav}" key="priv.${priv}"/></label><br/>
		</c:forEach>
	</c:if>
	<html:submit property="submit.group.member" value="${navMap['priv.assign']}"/> 
</fieldset>
</html:form>
</grouper:recordTile>