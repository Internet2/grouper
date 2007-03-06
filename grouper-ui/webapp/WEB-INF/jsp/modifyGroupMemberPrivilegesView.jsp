<%-- @annotation@
		  Form embedded in another page which allows a Subject`s
		  membership / privileges for the active group to be changed
--%><%--
  @author Gary Brown.
  @version $Id: modifyGroupMemberPrivilegesView.jsp,v 1.6 2007-03-06 11:05:49 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<html:form styleId="GroupOrStemMemberFormBean" action="/saveGroupMember" method="post">
<fieldset>
	<html:hidden property="asMemberOf"/>
	<html:hidden property="contextGroup"/>
	<html:hidden property="contextSubjectId"/>
	<html:hidden property="contextSubject"/>
	<html:hidden property="contextSubjectType"/>
	<html:hidden property="listField"/>
	<html:hidden property="subjectId"/>
	<html:hidden property="subjectType"/>
	<html:hidden property="privilege"/>
	<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
	
	<c:if test="${(authUserPriv.UPDATE || authUserPriv.ADMIN) && !(browseParent.composite && (empty listField || listField=='members')) && subject.subjectId !='GrouperAll'}">
	<html:multibox property="privileges" value="MEMBER" /> 
	<c:choose>
		<c:when test="${empty listField}">
			<fmt:message bundle="${nav}" key="priv.member"/>
		</c:when>
		<c:otherwise>
			<fmt:message bundle="${nav}" key="priv.member-list-field">
				<fmt:param value="${listField}"/>
			</fmt:message>
		</c:otherwise>
	</c:choose>
	<br/>
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
<tiles:insert definition="effectivePrivsDef"/>
</grouper:recordTile>