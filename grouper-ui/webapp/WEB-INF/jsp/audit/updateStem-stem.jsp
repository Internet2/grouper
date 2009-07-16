<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: updateStem-stem.jsp,v 1.1 2009-07-16 11:33:35 isgwb Exp $
--%><%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:set var="stem" value="${viewObject.fieldObjects.id}"/>
<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>
<c:choose>
    	<c:when test="${!empty stem }">
    <c:set target="${stem}" property="callerPageId" value="${thisPageId}"/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="stem"/>
		  <tiles:put name="view" value="stemSearchResultLink"/>
	</tiles:insert></c:when>
		<c:otherwise>
		<grouper:message bundle="${nav}" key="audit.result.label.unavailable"/> (<c:out value="${viewObject.fields.id}"/> - <c:out value="${viewObject.fields.name}"/>)
		</c:otherwise>
	</c:choose>