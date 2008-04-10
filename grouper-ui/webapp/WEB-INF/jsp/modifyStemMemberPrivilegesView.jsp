<%-- @annotation@
		  Form embedded in another page which allows a Subject`s
		  privileges for the active stem to be changed
--%><%--
  @author Gary Brown.
  @version $Id: modifyStemMemberPrivilegesView.jsp,v 1.5 2008-04-10 19:50:25 mchyzer Exp $
--%>	
<%@include file="/WEB-INF/jsp/include.jsp"%>

<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<html:form styleId="GroupOrStemMemberFormBean" action="/saveStemMember" method="post">
	<html:hidden property="asMemberOf"/>
	<html:hidden property="contextGroup"/>
	<html:hidden property="contextSubjectId"/>
	<html:hidden property="contextSubject"/>
	<html:hidden property="contextSubjectType"/>
	<html:hidden property="subjectId"/>
	<html:hidden property="subjectType"/>
	<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
	
	<c:if test="${authUserPriv.STEM}">
		<c:forEach var="priv" items="${possiblePrivs}">
			<html:multibox property="privileges" value="${priv}"  styleId="priv${priv}"/> 
				<label for="priv<c:out value="${priv}"/>"><grouper:message bundle="${nav}" key="priv.${priv}"/></label><br/>
		</c:forEach>
	</c:if>
	<html:submit styleClass="blueButton" property="submit.group.member" value="${navMap['priv.assign']}"/> 
</html:form>
<tiles:insert definition="effectivePrivsDef"/>
</grouper:recordTile>