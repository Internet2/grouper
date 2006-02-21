<%-- @annotation@
		  Dynamic tile Which allows user to select fields to search, query text and whether to and/or/not with next term
--%><%--
  @author Gary Brown.
  @version $Id: fieldATTRIBUTESearchValueView.jsp,v 1.1 2006-02-21 16:50:18 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${itemPos==1}">
<div class="searchFieldTitle">
	<div class="formRow">
		<div class="formLeft">
			<fmt:message bundle="${nav}" key="find.search.field-title"/>
		</div>
		<div class="formRight">
			<fmt:message bundle="${nav}" key="find.search.input-title"/>			
		</div>
	</div>
</div>
</c:if>
<div class="searchField">
	<div class="formRow">
		<div class="formLeft">
			<c:out value="${itemPos}"/><input type="checkbox" name="attr.<c:out value="${viewObject.name}"/>.doSearch" value="true"/><c:out value="${viewObject.name}"/>
		</div>
		<div class="formRight">
			<input type="text" name="attr.<c:out value="${viewObject.name}"/>.searchValue" size="25"/>
			<select name="attr.<c:out value="${viewObject.name}"/>.searchAndOr">
				<option value="or"><fmt:message bundle="${nav}" key="find.search.or"/></option>
				<option value="and"><fmt:message bundle="${nav}" key="find.search.and"/></option>
			</select>
			
			
			
		</div>
	</div>
</div>
   



