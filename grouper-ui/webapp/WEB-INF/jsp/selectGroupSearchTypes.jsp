<%-- @annotation@ 
		  Allow user to select which grouip types they want to search
--%><%--
  @author Gary Brown.
  @version $Id: selectGroupSearchTypes.jsp,v 1.4 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<tiles:importAttribute ignore="true"/>
		<tr class="formTableRow">
			<td class="formTableLeft">
			<strong><grouper:message key="find.search.in-group-type-input"/></strong>	
			</td>
			<td class="formTableRight">
			<strong><grouper:message key="find.search.in-group-type"/></strong>		
			</td>
		</tr>
<c:if test="${empty maxTypes}"><c:set var="maxTypes" value="${mediaMap['search.max-group-types']}"/></c:if>
<input type="hidden" name="maxTypes" value="<c:out value="${maxTypes}"/>"/>
<c:forEach begin="1" end="${maxTypes}" varStatus="typeCount">
	<tr class="formTableRow">
		<td class="formTableLeft">
			<c:set var="searchTypeName">searchType.<c:out value="${typeCount.count}"/></c:set>
		<c:set var="searchTypeAndOrNot">searchType.<c:out value="${typeCount.count}"/>.searchAndOrNot</c:set>
			<select name="<c:out value="${searchTypeAndOrNot}"/>">
				<option value="or" <c:if test="${advancedSearchFieldParams[searchTypeAndOrNot]=='or'}">selected="selected"</c:if>><grouper:message key="find.search.or"/></option>
				<option value="and"  <c:if test="${advancedSearchFieldParams[searchTypeAndOrNot]=='and'}">selected="selected"</c:if>><grouper:message key="find.search.and"/></option>
				<option value="not" <c:if test="${advancedSearchFieldParams[searchTypeAndOrNot]=='not'}">selected="selected"</c:if>><grouper:message key="find.search.not"/></option>
			</select>
			
		</td>
		<td class="formTableRight">
		
			<select name="<c:out value="${searchTypeName}"/>">
				
						<option value=""></option>
				<c:forEach var="type" items="${types}">
					<option value="<c:out value="${type.name}"/>" 
					<c:if test="${advancedSearchFieldParams[searchTypeName]==type.name}">selected="selected"</c:if>
					><c:out value="${type.name}"/>
						</option>
				</c:forEach>
			</select>
			
		</td>
	</tr>
   
</c:forEach>

</grouper:recordTile>