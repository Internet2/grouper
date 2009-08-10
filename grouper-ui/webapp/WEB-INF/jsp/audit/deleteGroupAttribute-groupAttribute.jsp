<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: deleteGroupAttribute-groupAttribute.jsp,v 1.2 2009-08-10 14:03:01 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:set var="group" value="${viewObject.fieldObjects.groupId}"/>
<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>
<em><c:out value="${viewObject.fields.fieldName}"/>=<c:out value="${viewObject.fields.value}"/></em>
<br/><grouper:message bundle="${nav}" key="audit.result.label.from"/> 
<c:choose>
    	<c:when test="${!empty group }">
    <c:set target="${group}" property="callerPageId" value="${thisPageId}"/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="group"/>
		  <tiles:put name="view" value="groupSearchResultLink"/>
	</tiles:insert></c:when>
		<c:otherwise>
		<grouper:message bundle="${nav}" key="audit.result.label.unavailable"/> (<c:out value="${viewObject.fields.id}"/> - <c:out value="${viewObject.fields.groupName}"/>)
		</c:otherwise>
	</c:choose>