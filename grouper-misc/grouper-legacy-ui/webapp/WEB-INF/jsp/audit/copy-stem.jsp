<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: copy-stem.jsp,v 1.3 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>

<c:set var="stem" value="${viewObject.fieldObjects.oldStemId}"/>
<c:set var="copiedTo" value="${viewObject.fieldObjects.newStemId}"/>
<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>

<c:choose>
    	<c:when test="${!empty stem }">
    <c:set target="${stem}" property="callerPageId" value="${thisPageId}"/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="stem"/>
		  <tiles:put name="view" value="stemSearchResultLink"/>
	</tiles:insert></c:when>
		<c:otherwise>
		<grouper:message key="audit.result.label.unavailable"/> (<c:out value="${viewObject.fields.oldStemId}"/> - <c:out value="${viewObject.fields.oldGroupName}"/>)
		</c:otherwise>
	</c:choose>
	<br/><grouper:message key="audit.result.label.to-object"/> 
	<c:choose>
    	<c:when test="${!empty copiedTo }">
    <c:set target="${copiedTo}" property="callerPageId" value="${thisPageId}"/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="copiedTo"/>
		  <tiles:put name="view" value="stemSearchResultLink"/>
	</tiles:insert></c:when>
		<c:otherwise>
		<grouper:message key="audit.result.label.unavailable"/> (<c:out value="${viewObject.fields.newGroupId}"/> - <c:out value="${viewObject.fields.newGroupName}"/>)
		</c:otherwise>
	</c:choose><br/>
		
	