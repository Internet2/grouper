<%-- @annotation@
		  Dynamic tile Which allows user to select fields to search, query text and whether to and/or/not with next term
--%><%--
  @author Gary Brown.
  @version $Id: fieldATTRIBUTESearchValueView.jsp,v 1.4 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${itemPos==1}">
	<tr class="formTableRow">
		<td class="formTableLeft">
			<grouper:message key="find.search.field-title"/>
		</td>
		<td class="formTableRight">
			<grouper:message key="find.search.input-title"/>			
		</td>
	</tr>
</c:if>
	<tr class="formTableRow">
		<td class="formTableLeft">
			<c:out value="${itemPos}"/><input type="checkbox" name="attr.<c:out value="${viewObject.name}"/>.doSearch" value="true"/><c:out value="${viewObject.name}"/>
		</td>
		<td class="formTableRight">
			<input type="text" name="attr.<c:out value="${viewObject.name}"/>.searchValue" size="25"/>
			<select name="attr.<c:out value="${viewObject.name}"/>.searchAndOr">
				<option value="or"><grouper:message key="find.search.or"/></option>
				<option value="and"><grouper:message key="find.search.and"/></option>
			</select>
			
			
			
		</td>
	</tr>
   



