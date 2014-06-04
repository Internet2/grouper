<%-- @annotation@
		Tile which displays a  list of parent stems to show the location of the current group
		or stem. The stem names are not links
--%><%--
  @author Gary Brown.
  @version $Id: showStemsLocation.jsp,v 1.10 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endLocation" class="noCSSOnly"><grouper:message key="page.skip.current-location"/><br/></a>
<div class="browseStemsLocation"><strong><grouper:message key="find.browse.here"/></strong>
<%-- CH 20080324 change spacing: --%>  <br /><div class="currentLocationList">
	<c:forEach var="stem" items="${browsePath}">
		<img <grouper:tooltip key="stem.icon.tooltip"/> 
    src="grouper/images/folderOpen.gif" class="groupIcon" alt="Folder" 
    /><span class="browseStemsLocationPart"><c:out 
    value="${stem.displayExtension}"/><c:out value="${stemSeparator}"/></span>
	</c:forEach>
	<%-- this is the last entry in the "current location" at top of most screens --%>
  <span class="browseStemsLocationHere">
	<c:choose>
    <c:when test="${browseParent.isGroup}"><img src="grouper/images/group.gif" 
  <grouper:tooltip key="group.icon.tooltip"/> class="groupIcon" alt="Group"
  /></c:when><c:otherwise><img src="grouper/images/folderOpen.gif" alt="Folder"
  <grouper:tooltip key="folder.icon.tooltip"/> class="groupIcon" 
  /></c:otherwise></c:choose><c:out value="${browseParent.displayExtension}"/></span>
    </div>
</div>
<a name="endLocation" id="endLocation"></a>
</grouper:recordTile>