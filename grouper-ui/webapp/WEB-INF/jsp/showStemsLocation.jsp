<%-- @annotation@
		Tile which displays a  list of parent stems to show the location of the current group
		or stem. The stem names are not links
--%><%--
  @author Gary Brown.
  @version $Id: showStemsLocation.jsp,v 1.3 2005-11-22 10:44:03 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endLocation" class="noCSSOnly"><fmt:message bundle="${nav}" key="page.skip.current-location"/><br/></a>
<div class="browseStemsLocation"><strong><fmt:message bundle="${nav}" key="find.browse.here"/></strong>
	<c:if test="${empty repositoryBrowser.rootNode || (!empty repositoryBrowser.rootNode && !repositoryBrowser.hidePreRootNode)}">
		<c:if test="${navMap['stem.root.display-name'] != '*'}">
				<c:out value="${navMap['stem.root.display-name']}"/><c:out value="${stemSeparator}"/>
		</c:if>
	</c:if>
	<c:forEach var="stem" items="${browsePath}">
		<span class="browseStemsLocationPart"><c:out value="${stem.displayExtension}"/><c:out value="${stemSeparator}"/></span>
	</c:forEach>
	
	<c:if test="${browseParent.isGroup}"><span class="browseStemsLocationHere">[</c:if><c:out value="${browseParent.displayExtension}"/><c:if test="${browseParent.isGroup}">]</span></c:if>
</div>
<a name="endLocation" id="endLocation"></a>
</grouper:recordTile>