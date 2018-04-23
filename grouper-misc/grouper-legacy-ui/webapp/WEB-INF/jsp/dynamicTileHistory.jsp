<%-- @annotation@
		  Used in debug mode to display nested list
		  of tiles used to construct current page
--%><%--
  @author Gary Brown.
  @version $Id: dynamicTileHistory.jsp,v 1.3 2007-12-13 09:33:25 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<c:forEach var="tile" items="${dynamicTiles}">
	<ul><li><c:if test="${!empty tile.view}"><span style="text-width:50px;text-align:right">view</span>=<span style="text-align:left"><c:out value="${tile.view}"/></span><br/>
	<span style="text-width:50px;text-align:right">type</span>=<span style="text-align:left"><c:out value="${tile.type}"/></span><br/>
	<span style="text-width:50px;text-align:right">key</span>=<span style="text-align:left"><c:out value="${tile.key}"/></span><br/>
	
	</c:if>
	<span style="text-width:50px;text-align:right">tile</span>=
	
	<%
	String defaultModule = (String)request.getAttribute("defaultModule");
	String tileJSP = (String)((Map)pageContext.getAttribute("tile")).get("tile");
	if("".equals(defaultModule) || !tileJSP.startsWith("/WEB-INF/jsp/" + defaultModule)) {
		pageContext.setAttribute("isI2mi","y");
	}else{
	pageContext.removeAttribute("isI2mi");
	}
	%>
	<c:choose>
		<c:when test="${!empty isI2mi && !empty debugPrefs.i2miDir}">
			<span style="text-align:left"><a target="temp" href="editJSP.do?jsp=<c:out value="${debugPrefs.i2miDir}"/><c:out value="${tile.tile}"/>" ><c:out value="${tile.tile}"/></a></span><br/>
		</c:when>
		<c:when test="${empty isI2mi && !empty debugPrefs.siteDir}">
			<span style="text-align:left"><a target="temp" href="editJSP.do?jsp=<c:out value="${debugPrefs.siteDir}"/><c:out value="${tile.tile}"/>" ><c:out value="${tile.tile}"/></a></span><br/>
		</c:when>
		<c:otherwise>
			<span style="text-align:left"><c:out value="${tile.tile}"/></span><br/>
		</c:otherwise>
	</c:choose>
	<c:if test="${!empty tile.jspErr}">
		<span style="text-width:50px;text-align:right" class="jspError">Error</span>=<span style="text-align:left"><c:out value="${tile.jspErr.class.simpleName}"/>:<c:out value="${tile.jspErr.message}"/></span><br/>
	</c:if>
	<%
		Map tile = (Map)pageContext.getAttribute("tile");
		List children = (List)tile.get("children");
		if(children.size() > 0) {
		request.setAttribute("dynamicTiles",children);
	%>
	<tiles:insert page="/WEB-INF/jsp/dynamicTileHistory.jsp"/>
	<%
	
		}
	%>
	</li></ul>
</c:forEach>