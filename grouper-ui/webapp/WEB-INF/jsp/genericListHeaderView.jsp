<%-- @annotation@
		  Dynamic tile used to display content above list elements
		  of a dynamic list.
		  includes a form dor chaning the page size
--%><%--
  @author Gary Brown.
  @version $Id: genericListHeaderView.jsp,v 1.3 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:choose>
	<c:when test="${pager.count>0}">
		<c:if test="${empty allowPageSizeChange && pager.count > pager.pageSize}">
		<html:form action="${pager.target}" method="post">
		<c:forEach var="entry" items="${pager.params}">
			<c:if test="${entry.key != 'pageSize'}">
		<input type="hidden" name="<c:out value="${entry.key}"/>" value="<c:out value="${entry.value}"/>" /></c:if>
		</c:forEach>
			<label for="pageSize" class="noCSSOnly"><grouper:message bundle="${nav}" key="find.browse.change-pagesize"/></label>
			<html:select property="pageSize" styleId="pageSize">
				<html:options name="pageSizeSelections"/>
			</html:select>
			<input type="submit" value="<grouper:message bundle="${nav}" key="find.browse.change-pagesize"/>"/>
		</html:form>
		</c:if>
		<div class="genericListHeader"><grouper:message bundle="${nav}" key="find.browse.show-results">
			<grouper:param value="${pager.start1}"/>
			<grouper:param value="${pager.last}"/>
			<grouper:param value="${pager.count}"/>
		</grouper:message>
		</div>
		<c:if test="${!empty listInstruction}">
			<div class="listInstructions"><grouper:message bundle="${nav}" key="${listInstruction}"/></div>
		</c:if>
		
	</c:when>
	<c:otherwise><!--no items-->
		<c:out value="${noResultsMsg}"/>	
	</c:otherwise>
</c:choose>



