<%-- @annotation@ 
		  Allow user to select which fields they want to search
--%><%--
  @author Gary Brown.
  @version $Id: selectGroupSearchFields.jsp,v 1.1 2006-02-21 16:50:18 isgwb Exp $
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
<c:if test="${empty maxFields}"><c:set var="maxFields" value="5"/></c:if>
<input type="hidden" name="maxFields" value="<c:out value="${maxFields}"/>"/>
<c:forEach begin="1" end="${maxFields}" varStatus="fieldCount">
<div class="searchField">
	<div class="formRow">
		<div class="formLeft">
			<select name="searchField.<c:out value="${fieldCount.count}"/>">
				<c:choose>
					<c:when test="${fieldCount.count==1}">
						<option value="displayName">displayName</option>
					</c:when>
					<c:otherwise>
						<option value=""></option>
					</c:otherwise>	
				</c:choose>
				<option value="_any">Any attribute</option>
				<c:forEach var="field" items="${fields}">
					<option value="<c:out value="${field.name}"/>"><c:out value="${field.name}"/></option>
				</c:forEach>
			</select>
		</div>
		<div class="formRight">
			<input type="text" name="searchField.<c:out value="${fieldCount.count}"/>.query" size="25"/>
		
			<c:if test="${maxFields != fieldCount.count}">
			<select name="searchField.<c:out value="${fieldCount.count}"/>.searchAndOrNot">
				<option value="or"><fmt:message bundle="${nav}" key="find.search.or"/></option>
				<option value="and"><fmt:message bundle="${nav}" key="find.search.and"/></option>
				<option value="not"><fmt:message bundle="${nav}" key="find.search.not"/></option>
			</select>
			</c:if>
		</div>
	</div>
</div>
   
</c:forEach>

