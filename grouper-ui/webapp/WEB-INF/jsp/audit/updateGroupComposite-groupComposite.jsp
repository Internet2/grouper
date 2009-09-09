<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: updateGroupComposite-groupComposite.jsp,v 1.3 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>

<c:set var="group" value="${viewObject.fieldObjects.ownerId}"/>
<c:set var="leftFactor" value="${viewObject.fieldObjects.leftFactorId}"/>
<c:set var="rightFactor" value="${viewObject.fieldObjects.rightFactorId}"/>
<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>

<c:choose>
    	<c:when test="${!empty group }">
    <c:set target="${group}" property="callerPageId" value="${thisPageId}"/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="group"/>
		  <tiles:put name="view" value="groupSearchResultLink"/>
	</tiles:insert></c:when>
		<c:otherwise>
		<grouper:message key="audit.result.label.unavailable"/> (<c:out value="${viewObject.fields.ownerId}"/> - <c:out value="${viewObject.fields.ownerName}"/>)
		</c:otherwise>
	</c:choose>
	=<br/>
	<c:choose>
    	<c:when test="${!empty leftFactor }">
    <c:set target="${group}" property="callerPageId" value="${thisPageId}"/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="leftFactor"/>
		  <tiles:put name="view" value="groupSearchResultLink"/>
	</tiles:insert></c:when>
		<c:otherwise>
		<grouper:message key="audit.result.label.unavailable"/> (<c:out value="${viewObject.fields.leftFactorId}"/> - <c:out value="${viewObject.fields.leftFactorName}"/>)
		</c:otherwise>
	</c:choose><br/>
		<c:out value="${viewObject.fields.type}"/>
		<br/>
	<c:choose>
    	<c:when test="${!empty group }">
    <c:set target="${group}" property="callerPageId" value="${thisPageId}"/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="rightFactor"/>
		  <tiles:put name="view" value="groupSearchResultLink"/>
	</tiles:insert></c:when>
		<c:otherwise>
		<grouper:message key="audit.result.label.unavailable"/> (<c:out value="${viewObject.fields.rightfactorId}"/> - <c:out value="${viewObject.fields.rightFactorId}"/>)
		</c:otherwise>
	</c:choose>