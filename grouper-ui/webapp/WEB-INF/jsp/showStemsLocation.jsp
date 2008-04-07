<%-- @annotation@
		Tile which displays a  list of parent stems to show the location of the current group
		or stem. The stem names are not links
--%><%--
  @author Gary Brown.
  @version $Id: showStemsLocation.jsp,v 1.6 2008-04-07 07:54:15 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endLocation" class="noCSSOnly"><grouper:message bundle="${nav}" key="page.skip.current-location"/><br/></a>
<div class="browseStemsLocation"><strong><grouper:message bundle="${nav}" key="find.browse.here"/></strong>
<%-- CH 20080324 change spacing: --%>  <br />&nbsp;&nbsp;&nbsp;
	<c:forEach var="stem" items="${browsePath}">
		<img <grouper:tooltip key="stem.icon.tooltip"/> src="grouper/images/folder.gif" class="groupIcon" /><span class="browseStemsLocationPart"><c:out value="${stem.displayExtension}"/><c:out value="${stemSeparator}"/></span>
	</c:forEach>
	<%-- this is the last entry in the "current location" at top of most screens --%>
	<c:if test="${browseParent.isGroup}"><img src="grouper/images/group.gif" <grouper:tooltip key="group.icon.tooltip"/> class="groupIcon" /><span class="browseStemsLocationHere"></c:if><c:out value="${browseParent.displayExtension}"/><c:if test="${browseParent.isGroup}"></span></c:if>
</div>
<a name="endLocation" id="endLocation"></a>
</grouper:recordTile>