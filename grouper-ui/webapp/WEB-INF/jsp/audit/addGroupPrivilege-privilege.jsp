<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: addGroupPrivilege-privilege.jsp,v 1.1 2009-07-16 11:33:34 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:set var="subject" value="${viewObject.fieldObjects.memberId}"/>
<c:set var="group" value="${viewObject.fieldObjects.groupId}"/>
<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>

<em><c:out value="${viewObject.fields.privilegeName}"/></em> <grouper:message bundle="${nav}" key="audit.result.label.to-subject"/> 
<c:choose>
    	<c:when test="${!empty subject }">
    
    <c:set target="${linkParams}" property="callerPageId" value="${thisPageId}"/>
	<c:set target="${linkParams}" property="subjectId" value="${subject.id}"/>
	<c:set target="${linkParams}" property="subjectType" value="${subject.subjectType}"/>
	<c:set target="${linkParams}" property="sourceId" value="${subject.sourceId}"/>
    <c:set var="linkTitle" value="View details of Entity added as member"/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="subject"/>
		  <tiles:put name="view" value="subjectSummaryLink"/>
		  <tiles:put name="params" beanName="linkParams"/>
		  <tiles:put name="linkTitle" beanName="linkTitle"/>
	</tiles:insert></c:when>
		<c:otherwise>
		<grouper:message bundle="${nav}" key="audit.result.label.unavailable"/> (<c:out value="${viewObject.fields.memberId}"/>)
		</c:otherwise>
	</c:choose> <grouper:message bundle="${nav}" key="audit.result.label.for"/><br/>
	<c:choose>
    	<c:when test="${!empty group }">
    <c:set target="${group}" property="callerPageId" value="${thisPageId}"/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="group"/>
		  <tiles:put name="view" value="groupSearchResultLink"/>
	</tiles:insert></c:when>
		<c:otherwise>
		<grouper:message bundle="${nav}" key="audit.result.label.unavailable"/> (<c:out value="${viewObject.fields.groupId}"/> - <c:out value="${viewObject.fields.groupName}"/>)
		</c:otherwise>
	</c:choose>
	
	