<%-- @annotation@
		  Dynamic tile used to display content above list elements
		  of a dynamic list.
		  includes a form dor chaning the page size
--%><%--
  @author Gary Brown.
  @version $Id: genericListHeaderView.jsp,v 1.7 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${pager.count>0 || memberSearchValue != null}">
	<c:if test="${showMemberSearch}">
		<html:form action="/populateGroupMembers" method="post">
			<div class="sectionBody">
				<grouper:message key="member.search.filter-members-hint" /><input type="text" name="memberSearchValue" />
				<html:hidden property="groupId" />
				<html:hidden property="memberSortIndex" />
				<input type="submit" class="blueButton" value="<grouper:message key="member.search.search-members" /> "/>
			</div>
			<c:if test="${memberSearchValue != null}">
				<table class="formTable">
					<tr class="formTableRow">
						<td class="formTableLeft"><grouper:message key="member.search.filter-label" /></td>
						<td class="formTableRight">${memberSearchValue}&nbsp;&nbsp;
							<html:link page="/populateGroupMembers.do" name="groupMembership">
								<grouper:message key="member.search.filter-clear" />
							</html:link>
						</td>
					</tr>
				</table>
			</c:if>
			<br />
		</html:form>
	</c:if>
</c:if>

<c:if test="${pager.count>0 || memberSortIndex != null}">
	<c:if test="${memberSortSelections != null}">
		<html:form action="/populateGroupMembers" method="post">
			<select name="memberSortIndex">
				<c:forEach var="entry" items="${memberSortSelections}">
					<c:choose>
						<c:when test="${memberSortIndex == entry.key}">
							<option selected="selected" value="<c:out value="${entry.key}" />"><c:out value="${entry.value}" /></option>
						</c:when>
						<c:otherwise>
							<option value="<c:out value="${entry.key}" />"><c:out value="${entry.value}" /></option>
						</c:otherwise>
					</c:choose>
				</c:forEach>
			</select>
			<html:hidden property="groupId" />
			<html:hidden property="memberSearchValue" />
			<input type="submit" class="blueButton" value="<grouper:message key="member.sort.change-sort-attribute" /> "/>&nbsp;&nbsp;
		</html:form>
		<c:if test="${pager.count == 0}">
			<br /><br />	
		</c:if>
	</c:if>
</c:if>

<c:choose>
	<c:when test="${pager.count>0}">
		<c:if test="${empty allowPageSizeChange && pager.count > pager.pageSize}">
		<html:form action="${pager.target}" method="post">
		<c:forEach var="entry" items="${pager.params}">
			<c:if test="${entry.key != 'pageSize'}">
		<input type="hidden" name="<c:out value="${entry.key}"/>" value="<c:out value="${entry.value}"/>" /></c:if>
		</c:forEach>
			<label for="pageSize" class="noCSSOnly"><grouper:message key="find.browse.change-pagesize"/></label>
			<html:select property="pageSize" styleId="pageSize">
				<html:options name="pageSizeSelections"/>
			</html:select>
			<input type="submit" class="blueButton" value="<grouper:message key="find.browse.change-pagesize"/>"/>
		</html:form>
		</c:if>
		<div class="genericListHeader"><grouper:message key="find.browse.show-results">
			<grouper:param value="${pager.start1}"/>
			<grouper:param value="${pager.last}"/>
			<grouper:param value="${pager.count}"/>
		</grouper:message>
		</div>
    <div class="genericListHeader">
  		<c:if test="${!empty listInstruction}">
  			<div class="listInstructions"><grouper:message key="${listInstruction}"/></div>
  		</c:if>
    </div>		
    <br />
	</c:when>
	<c:otherwise><!--no items-->
		<c:out value="${noResultsMsg}"/>	
	</c:otherwise>
</c:choose>



