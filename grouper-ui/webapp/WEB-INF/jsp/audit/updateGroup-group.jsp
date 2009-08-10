<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: updateGroup-group.jsp,v 1.2 2009-08-10 14:03:01 isgwb Exp $
--%><%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>

<c:set var="group" value="${viewObject.fieldObjects.id}"/>
<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>

	<c:choose>
    	<c:when test="${!empty group }">
    <c:set target="${group}" property="callerPageId" value="${thisPageId}"/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="group"/>
		  <tiles:put name="view" value="groupSearchResultLink"/>
	</tiles:insert></c:when>
		<c:otherwise>
		<grouper:message bundle="${nav}" key="audit.result.label.unavailable"/> (<c:out value="${viewObject.fields.id}"/> - <c:out value="${viewObject.fields.name}"/>)
		</c:otherwise>
	</c:choose>
	
	