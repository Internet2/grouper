<%-- @annotation@
		  Form embedded in another page which allows a Subject`s
		  membership / privileges for the active group to be changed
--%><%--
  @author Gary Brown.
  @version $Id: modifyGroupMemberPrivilegesView.jsp,v 1.14 2009-11-07 15:50:38 isgwb Exp $
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
	<html:hidden property="contextSourceId"/>
	<html:hidden property="listField"/>
	<html:hidden property="subjectId"/>
	<html:hidden property="subjectType"/>
	<html:hidden property="sourceId"/>
	<html:hidden property="privilege"/>
	<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
	
	<c:if test="${!(browseParent.hasComposite && (empty listField || listField=='members')) && subject.subjectId !='GrouperAll'}">
	<c:set var="disabled">true</c:set>
	<c:choose>
		<c:when test="${empty listField}">
			<c:set var="label"><grouper:message key="priv.member"/></c:set>
			<c:set var="disabled"><c:out value="${!groupPrivResolver.canManageMembers}"/></c:set>
		</c:when>
		<c:otherwise>
			<c:set var="label"><grouper:message key="priv.member-list-field">
				<grouper:param value="${listField}"/>
			</grouper:message></c:set>
			<c:set var="disabled"><c:out value="${!groupPrivResolver.canManageField[listField]}"/></c:set>
		</c:otherwise>
	</c:choose>
	<html:multibox property="privileges" value="member" disabled="${disabled}"/> <c:out value="${label}" escapeXml="false"/>
	<br/>
	</c:if>
	<c:if test="${authUserPriv.admin}">
	<c:set var="disabled">true</c:set>
	<c:if test="${groupPrivResolver.canManagePrivileges}">
	<c:set var="disabled">false</c:set>
	</c:if>
		<c:forEach var="priv" items="${possiblePrivs}">
			<html:multibox property="privileges" value="${priv}" styleId="priv${priv}" disabled="${disabled}"/> 
				<label for="priv<c:out value="${priv}"/>"><grouper:message key="priv.${priv}"/></label><br/>
		</c:forEach>
	</c:if>
	<c:if test="${groupPrivResolver.canManagePrivileges || groupPrivResolver.canManageMembers}">
		<br /><html:submit styleClass="blueButton" property="submit.group.member" value="${navMap['priv.assign']}"/> 
	</c:if>
</fieldset>
</html:form>
</grouper:recordTile>
