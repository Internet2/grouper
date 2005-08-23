<%-- @annotation@
		  Tile which shows parent nodes of the current node as links
		  which can be used to navigate to the parent node allowing 
		  the user to navigate the hierarchy
--%><%--
  @author Gary Brown.
  @version $Id: browseStemsLocation.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="browseStemsLocation">
<c:choose>
	<c:when test="${! isFlat}">
<a href="<c:out value="${pageUrl}"/>#skipCurrentLocation" class="noCSSOnly"><fmt:message bundle="${nav}" key="page.skip.current-location"/><br/></a>
<strong><fmt:message bundle="${nav}" key="find.browse.here"/></strong>

	<c:choose>	
		<c:when test="${navMap['stem.root.display-name'] != '*' && !empty browseParent}">
			<c:set var="rootNode" value="ROOT"/>
			<span class="browseStemsLocationPart">
				<html:link 
					page="/browseStems${browseMode}.do" 
					paramId="currentNode" 
					paramName="rootNode" 
					title="${navMap['browse.to.parent-stem']} ${navMap['stem.root.display-name']}">
						<c:out value="${navMap['stem.root.display-name']}"/><c:out value="${stemSeparator}"/>
				</html:link>
			</span>
		</c:when>
		<c:when test="${navMap['stem.root.display-name'] != '*' && empty browseParent}">
			<c:out value="${navMap['stem.root.display-name']}"/>
		</c:when>
	</c:choose>
	
	<c:forEach var="stem" items="${browsePath}">
		<span class="browseStemsLocationPart">
			<html:link 
				page="/browseStems${browseMode}.do" 
				paramId="currentNode" 
				paramName="stem" 
				paramProperty="id"
				title="${navMap['browse.to.parent-stem']} ${stem.displayExtension}">
					<c:out value="${stem.displayExtension}"/><c:out value="${stemSeparator}"/>
			</html:link>
		</span>
	</c:forEach>
	<span class="browseStemsLocationHere">
	<c:if test="${browseParent.isGroup}">[</c:if><c:out value="${browseParent.displayExtension}"/><c:if test="${browseParent.isGroup}">]</c:if></span>
<a id="skipCurrentLocation" name="skipCurrentLocation"></a>
</c:when>
<c:otherwise>

</c:otherwise>
</c:choose>
</div>
</grouper:recordTile>