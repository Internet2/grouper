<%-- @annotation@
		  Tile which shows parent nodes of the current node as links
		  which can be used to navigate to the parent node allowing 
		  the user to navigate the hierarchy
--%><%--
  @author Gary Brown.
  @version $Id: browseStemsLocation.jsp,v 1.10 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="browseStemsLocation">
<c:choose>
	<c:when test="${! isFlat}">
<a href="<c:out value="${pageUrl}"/>#skipCurrentLocation" class="noCSSOnly"><grouper:message key="page.skip.current-location"/><br/></a>
<strong><grouper:message key="find.browse.here"/></strong>
<%-- CH 20080324 change spacing: --%>  <br /><div class="currentLocationList">

<%
	int browsePathSize = ((List)request.getAttribute("browsePath")).size();
	pageContext.setAttribute("browsePathSize",new Integer(browsePathSize));
%>	
	<c:forEach var="stem" items="${browsePath}">
  <img <grouper:tooltip key="stem.icon.tooltip"/> 
    src="grouper/images/folderOpen.gif" class="groupIcon" alt="Folder" 
    /><span class="browseStemsLocationPart"><html:link 
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
		<c:choose>
      <c:when test="${browseParent.isGroup}"><img <grouper:tooltip key="group.icon.tooltip"/> 
    src="grouper/images/group.gif" class="groupIcon" alt="Group" 
    /></c:when><c:otherwise>
    <img <grouper:tooltip key="stem.icon.tooltip"/> 
    src="grouper/images/folderOpen.gif" class="groupIcon" alt="Folder" 
    /></c:otherwise></c:choose><c:out 
    value="${browseParent.displayExtension}"/></span>
<a id="skipCurrentLocation" name="skipCurrentLocation"></a>
</c:when>
<c:otherwise>

</c:otherwise>
</c:choose>
  </div>
</div>
</grouper:recordTile>