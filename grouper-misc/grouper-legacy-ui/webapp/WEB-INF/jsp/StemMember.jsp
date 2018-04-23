<%-- @annotation@
			Form which allows user to change an individual
			Subject`s membership of / privileges for, the
			active stem
--%><%--
  @author Gary Brown.
  @version $Id: StemMember.jsp,v 1.5 2009-09-09 15:10:03 mchyzer Exp $
--%> 
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insert definition="showStemsLocationDef"/>
<h2 class="actionheader">
	<grouper:message key="find.heading.select-privs">
	<grouper:param><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="subject"/>
	  <tiles:put name="view" value="groupMember"/>
</tiles:insert></grouper:param>
</grouper:message>
</h2>
<c:set var="subject" value="${subject}" scope="request"/>

<c:if test="${authUserPriv.create || authUserPriv.stemAdmin}">
	<tiles:insert definition="stemMemberPrivsDef"/>
</c:if>
<div class="linkButton">
<c:choose>
	<c:when test="${GroupOrStemMemberFormBean.map.contextSubject=='true'}">
				
		<tiles:insert definition="callerPageButtonDef"/>
	
		<html:link page="/populateSubjectSummary.do">
			<grouper:message key="groups.action.summary.return-to-subject-summary"/>
		</html:link>
	</c:when>
	<c:when test="${!empty GroupOrStemMemberFormBean.map.callerPageId}">
				<tiles:insert definition="callerPageButtonDef"/>
	</c:when>
	<c:otherwise>
		<html:link page="/populateStemPriviligees.do" paramName="GroupOrStemMemberFormBean" paramProperty="asMemberOf" paramId="stemId">
			<grouper:message key="cancel"/>
		</html:link>
	</c:otherwise>
</c:choose>
</div>
