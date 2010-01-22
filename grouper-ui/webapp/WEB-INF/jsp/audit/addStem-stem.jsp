<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: addStem-stem.jsp,v 1.2 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
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
		<grouper:message key="audit.result.label.unavailable"/> (<c:out value="${viewObject.fields.id}"/> - <c:out value="${viewObject.fields.name}"/>)
		</c:otherwise>
	</c:choose>