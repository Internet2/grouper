<%-- @annotation@
		Tile which displays a select list of parent stems to scope a search
		to a branch of the groups hierarchy
--%><%--
  @author Gary Brown.
  @version $Id: searchFrom.jsp,v 1.2 2005-12-22 10:49:18 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<c:if test="${!empty browsePath}">
<div class="formRow">
	<div class="formLeft">
	<label for="searchFrom"><fmt:message bundle="${nav}" key="find.search-from"/></label>
	</div>
	<div class="formRight">
	<select name="searchFrom" id="searchFrom">
	
	
	<c:forEach var="stem" items="${browsePath}">
		<option value="<c:out value="${stem.name}"/>">
			<c:choose>
				<c:when test="${empty stem.displayExtension}">
					<fmt:message bundle="${nav}" key="stem.root.display-name"/>
				</c:when>
				<c:otherwise>
					<c:out value="${stem.displayExtension}"/>
				</c:otherwise>
			</c:choose>
			
		</option>
	</c:forEach>
	
	<c:if test="${currentLocation.isStem}">
		<option value="<c:out value="${currentLocation.name}"/>">
			<c:out value="${currentLocation.displayExtension}"/>
	</c:if>
</select>
	</div>
</div>


</c:if>
</grouper:recordTile>