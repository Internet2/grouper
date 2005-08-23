<%-- @annotation@
		Tile which displays a select list of parent stems to scope a search
		to a branch of the groups hierarchy
--%><%--
  @author Gary Brown.
  @version $Id: searchFrom.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<c:if test="${!empty browsePath || (!empty browseParent && navMap['stem.root.display-name'] !='*')}">
<div class="formRow">
	<div class="formLeft">
	<label for="searchFrom"><fmt:message bundle="${nav}" key="find.search-from"/></label>
	</div>
	<div class="formRight">
	<select name="searchFrom" id="searchFrom">
	<c:if test="${!empty browseParent && navMap['stem.root.display-name'] !='*'}">
	<option value=""><fmt:message bundle="${nav}" key="stem.root.display-name"/></option>
	</c:if>
	<c:if test="${!empty browsePath}">
	<c:forEach var="stem" items="${browsePath}">
		<option value="<c:out value="${stem.name}"/>">
			<c:out value="${stem.displayExtension}"/>
		</option>
	</c:forEach>
	</c:if>
	<c:if test="${currentLocation.isStem}">
		<option value="<c:out value="${currentLocation.name}"/>">
			<c:out value="${currentLocation.displayExtension}"/>
	</c:if>
</select>
	</div>
</div>


</c:if>
</grouper:recordTile>