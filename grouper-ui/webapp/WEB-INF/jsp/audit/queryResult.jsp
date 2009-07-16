<%@include file="/WEB-INF/jsp/include.jsp"%>
<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>
<tiles:importAttribute ignore="true"/>
<c:set var="auditEntry" value="${viewObject}"/>
<c:set var="actionKey">audit.query.<c:out value="${auditEntry.auditType.actionName}"/>-<c:out value="${auditEntry.auditType.auditCategory}"/></c:set>	
<tr><td><fmt:formatDate pattern="${mediaMap['audit.query.display-date-format']}" value="${auditEntry.lastUpdated}" /></td>
    
    <td><c:set var="actor" value="${auditEntry.loggedInMember}"/>
    <c:choose>
    	<c:when test="${!empty actor }">
    
    <c:set target="${linkParams}" property="callerPageId" value="${thisPageId}"/>
	<c:set target="${linkParams}" property="subjectId" value="${actor.id}"/>
	<c:set target="${linkParams}" property="subjectType" value="${actor.subjectType}"/>
	<c:set target="${linkParams}" property="sourceId" value="${actor.sourceId}"/>
    <c:set var="linkTitle" value="View details of Entity responsible for action"/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="actor"/>
		  <tiles:put name="view" value="subjectSummaryLink"/>
		  <tiles:put name="params" beanName="linkParams"/>
		  <tiles:put name="linkTitle" beanName="linkTitle"/>
	</tiles:insert></c:when><%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: queryResult.jsp,v 1.1 2009-07-16 11:33:35 isgwb Exp $
--%>
		<c:otherwise>
		Unavailable (<c:out value="${auditEntry.loggedInMemberId}"/>)
		</c:otherwise>
	</c:choose></td>
	<td><c:out value="${auditEntry.grouperEngine}"/></td>
    <td> 
    <!--<c:out value="${auditEntry.auditType.actionName}"/>-<c:out value="${auditEntry.auditType.auditCategory}"/>-->
    <strong><grouper:message bundle="${nav}" key="${actionKey}"/></strong><br/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="auditEntry"/>
		  <tiles:put name="view" value="summary"/>
	</tiles:insert></td></tr>