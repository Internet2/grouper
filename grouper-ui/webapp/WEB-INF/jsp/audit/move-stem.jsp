<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: move-stem.jsp,v 1.2 2009-08-10 14:03:01 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>

<c:set var="copiedTo" value="${viewObject.fieldObjects.stemId}"/>
<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>
<c:out value="${viewObject.fields.oldStemName}"/> 
<br/><grouper:message bundle="${nav}" key="audit.result.label.to-object"/> 


<c:choose>
	<c:when test="${!empty copiedTo && viewObject.fields.newStemName != copiedTo.name}"><c:out value="${viewObject.fields.newStemName}"/></c:when>
	<c:otherwise>

	<c:choose>
    	<c:when test="${!empty copiedTo}">
    <c:set target="${copiedTo}" property="callerPageId" value="${thisPageId}"/>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="copiedTo"/>
		  <tiles:put name="view" value="stemSearchResultLink"/>
	</tiles:insert></c:when>
		<c:otherwise>
		<grouper:message bundle="${nav}" key="audit.result.label.unavailable"/> (<c:out value="${viewObject.fields.stemId}"/> - <c:out value="${viewObject.fields.newStemName}"/>)
		</c:otherwise>
	</c:choose>
	
	</c:otherwise>
	</c:choose>
	<br/>
		
	