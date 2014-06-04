<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: queryResult.jsp,v 1.5 2009-10-01 13:43:13 isgwb Exp $
--%><%@include file="/WEB-INF/jsp/include.jsp"%>
<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>
<tiles:importAttribute ignore="true"/>
<c:set var="auditEntry" value="${viewObject}"/>
<c:set var="actionKey">audit.query.<c:out value="${auditEntry.auditType.actionName}"/>-<c:out value="${auditEntry.auditType.auditCategory}"/></c:set>	
<tr><td>${auditEntry.formatLastUpdated}</td>
    
    <td><c:set var="actor" value="${auditEntry.loggedInMember}"/><c:set var="actAsMember" value="${auditEntry.actAsMember}"/>
    <c:choose>
    	<c:when test="${!empty actor }">
    
    <c:set target="${linkParams}" property="callerPageId" value="${thisPageId}"/>
	<c:set target="${linkParams}" property="subjectId" value="${actor.id}"/>
	<c:set target="${linkParams}" property="subjectType" value="${actor.subjectType}"/>
	<c:set target="${linkParams}" property="sourceId" value="${actor.sourceId}"/>
    <c:set var="linkTitle"><grouper:message key="audit.result.label.logged-in-member.link.title" tooltipDisable="true"/></c:set>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="actor"/>
		  <tiles:put name="view" value="subjectSummaryLink"/>
		  <tiles:put name="params" beanName="linkParams"/>
		  <tiles:put name="linkTitle" beanName="linkTitle"/>
	</tiles:insert></c:when>
		<c:otherwise>
		Unavailable (<c:out value="${auditEntry.loggedInMemberId}"/>)
		</c:otherwise>
	</c:choose>
	<c:if test="${!empty actAsMember}">
		<grouper:message key="audit.result.label.acting-as"/><br/>
		<c:set target="${linkParams}" property="callerPageId" value="${thisPageId}"/>
		<c:set target="${linkParams}" property="subjectId" value="${actAsMember.id}"/>
		<c:set target="${linkParams}" property="subjectType" value="${actAsMember.subjectType}"/>
		<c:set target="${linkParams}" property="sourceId" value="${actAsMember.sourceId}"/>
    	<c:set var="linkTitle"><grouper:message key="audit.result.label.act-as-member.link.title" tooltipDisable="true"/></c:set>
    	<tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="actAsMember"/>
		  <tiles:put name="view" value="subjectSummaryLink"/>
		  <tiles:put name="params" beanName="linkParams"/>
		  <tiles:put name="linkTitle" beanName="linkTitle"/>
	</tiles:insert>	
	</c:if>
	
	
	</td>
	<td><c:out value="${auditEntry.grouperEngine}"/></td>
    <td> 
    <!--<c:out value="${auditEntry.auditType.actionName}"/>-<c:out value="${auditEntry.auditType.auditCategory}"/>-->
    <strong><grouper:message key="${actionKey}"/></strong><br/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="auditEntry"/>
		  <tiles:put name="view" value="summary"/>
	</tiles:insert></td>
	<c:if test="${extendedResults}">
	<td align="right"><c:out value="${auditEntry.duration}"/></td>
			<td align="right"><c:out value="${auditEntry.queryCount}"/></td>
			<td><c:out value="${auditEntry.serverUserName}"/></td>
			<td><c:out value="${auditEntry.serverHost}"/><br/><c:out value="${auditEntry.userIpAddress}"/></td>
			<td><c:out value="${auditEntry.id}"/></td>	
			<td><c:out value="${auditEntry.description}"/></td>	
			</c:if>
	</tr>