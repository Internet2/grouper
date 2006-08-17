<%-- @annotation@ 
		  Allow user to select which fields they want to search
--%><%--
  @author Gary Brown.
  @version $Id: selectGroupSearchFields.jsp,v 1.3 2006-08-17 08:53:37 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="searchFieldTitle">
		<div class="formRow">
			<div class="formLeft">
				<strong><fmt:message bundle="${nav}" key="find.search.in-field"/></strong>	
			</div>
			<div class="formRight">
				<strong><fmt:message bundle="${nav}" key="find.search.in-field-input"/></strong>	
			</div>
			</div>
</div>
<c:if test="${empty maxFields}"><c:set var="maxFields" value="${mediaMap['search.max-fields']}"/></c:if>
<input type="hidden" name="maxFields" value="<c:out value="${maxFields}"/>"/>
<c:forEach begin="1" end="${maxFields}" varStatus="fieldCount">
<div class="searchField">
	<div class="formRow">
		<div class="formLeft">
		<c:set var="searchFieldName">searchField.<c:out value="${fieldCount.count}"/></c:set>
		<c:set var="searchFieldQuery">searchField.<c:out value="${fieldCount.count}"/>.query</c:set>
		<c:set var="searchFieldAndOrNot">searchField.<c:out value="${fieldCount.count}"/>.searchAndOrNot</c:set>
			<select name="<c:out value="${searchFieldName}"/>">
				<c:choose>
					<c:when test="${fieldCount.count==1}">
						<option value="displayName">displayName</option>
					</c:when>
					<c:otherwise>
						<option value=""></option>
					</c:otherwise>	
				</c:choose>
				<option value="_any" <c:if test="${advancedSearchFieldParams[searchFieldName]=='_any'}">selected="selected"</c:if>
					>Any attribute</option>
				<c:forEach var="field" items="${fields}">
				<c:if test="${!(fieldCount.count==1 && field.name=='displayName')}">
					<option value="<c:out value="${field.name}"/>" 
					<c:if test="${advancedSearchFieldParams[searchFieldName]==field.name}">selected="selected"</c:if>
					><c:out value="${field.name}"/>
						</option>
						</c:if>
				</c:forEach>
			</select>
		</div>
		<div class="formRight">
			<input type="text" 
			       name="<c:out value="${searchFieldQuery}"/>" 
				   value="<c:out value="${advancedSearchFieldParams[searchFieldQuery]}"/>" size="25" <c:if test="${fieldCount.count==1}">tabindex="1"</c:if>/>
		
			<c:if test="${maxFields != fieldCount.count}">
			<select name="<c:out value="${searchFieldAndOrNot}"/>">
				<option value="or" <c:if test="${advancedSearchFieldParams[searchFieldAndOrNot]=='or'}">selected="selected"</c:if>><fmt:message bundle="${nav}" key="find.search.or"/></option>
				<option value="and"  <c:if test="${advancedSearchFieldParams[searchFieldAndOrNot]=='and'}">selected="selected"</c:if>><fmt:message bundle="${nav}" key="find.search.and"/></option>
				<option value="not" <c:if test="${advancedSearchFieldParams[searchFieldAndOrNot]=='not'}">selected="selected"</c:if>><fmt:message bundle="${nav}" key="find.search.not"/></option>
			</select>
			</c:if>
		</div>
	</div>
</div>
   
</c:forEach>

