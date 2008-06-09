<%-- @annotation@
		  Dynamic tile Which allows user to select fields to search, query text and whether to and/or/not with next term
--%><%--
  @author Gary Brown.
  @version $Id: fieldATTRIBUTESearchValueView.jsp,v 1.2 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${itemPos==1}">
<div class="searchFieldTitle">
	<div class="formRow">
		<div class="formLeft">
			<grouper:message bundle="${nav}" key="find.search.field-title"/>
		</div>
		<div class="formRight">
			<grouper:message bundle="${nav}" key="find.search.input-title"/>			
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
				<option value="or"><grouper:message bundle="${nav}" key="find.search.or"/></option>
				<option value="and"><grouper:message bundle="${nav}" key="find.search.and"/></option>
			</select>
			
			
			
		</div>
	</div>
</div>
   



