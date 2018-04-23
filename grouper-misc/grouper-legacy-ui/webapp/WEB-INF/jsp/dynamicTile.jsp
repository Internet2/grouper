<%-- @annotation@
		  Tile which is configured to load dynamic tiles based on a 'view'
		  and a 'viewObject'
--%><%--
  @author Gary Brown.
  @version $Id: dynamicTile.jsp,v 1.7 2009-09-09 15:10:03 mchyzer Exp $
--%><%@page import="org.apache.struts.tiles.ComponentContext"%><%@include file="/WEB-INF/jsp/include.jsp"%><tiles:importAttribute ignore="true"/><c:if test="${empty inLink}">
<!--       view=<c:out value="${view}"/>
    object type=<c:out value="${dynamicObjectType}"/>
       from key=<c:out value="${dynamicTemplateKey}"/>
    dynamicTile=<c:out value="${dynamicTemplate}"/>
--></c:if><%
ComponentContext tContext = ComponentContext.getContext(request);
pageContext.setAttribute("parentTilesContext",tContext);
%>	<grouper:recordTile view="${view}" 
                        type="${dynamicObjectType}" 
						key="${dynamicTemplateKey}" 
						tile="${dynamicTemplate}"
						silent="${inLink}">
		<c:catch var="uiException">	
		<c:if test="${!empty dynamicTemplate}">
			<c:set var="safeObject" value="${viewObject}"/>
			<c:set var="safeTemplate" value="${dynamicTemplate}"/>
			
			<tiles:insert controllerUrl="${modulePrefix}/propogateTilesAttributes.do" page="${dynamicTemplate}">
				<tiles:put name="parentTilesContext" beanName="parentTilesContext"/>
			</tiles:insert>
			<c:set var="viewObject" scope="request" value="${safeObject}"/>
			
		</c:if>
		</c:catch>
<c:if test="${not empty uiException && empty silent}">
	<span class="jspError"><grouper:message key="jsp.error"/></span>
	<!-- <c:out escapeXml="false" value="${uiException.message}"/> -->
</c:if>	
<c:if test="${not empty uiException}">
	<c:set var="uiException" scope="request" value="${uiException}"/>
</c:if>	
	</grouper:recordTile>