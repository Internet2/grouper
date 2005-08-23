<%-- @annotation@
		  Form embedded in another page which allows a Subject`s
		  privileges for the active stem to be changed
--%><%--
  @author Gary Brown.
  @version $Id: modifyStemMemberPrivilegesView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>	
<%@include file="/WEB-INF/jsp/include.jsp"%>
<html:form styleId="MemberPrivilegesForm" action="/saveStemMember">
	<html:hidden property="asMemberOf"/>
	<html:hidden property="subjectId"/>
	<html:hidden property="subjectType"/>
	
	<c:if test="${authUserPriv.STEM}">
		<c:forEach var="priv" items="${possiblePrivs}">
			<html:multibox property="privileges" value="${priv}"  styleId="priv${priv}"/> 
				<label for="priv<c:out value="${priv}"/>"><fmt:message bundle="${nav}" key="priv.${priv}"/></label><br/>
		</c:forEach>
	</c:if>
	<html:submit property="submit.group.member" value="${navMap['priv.assign']}"/> 
</html:form>