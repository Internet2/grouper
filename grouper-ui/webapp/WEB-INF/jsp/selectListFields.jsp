<%-- @annotation@
		Tile which lets user select alternative list field for Manipulation
--%><%--
  @author Gary Brown.
  @version $Id: selectListFields.jsp,v 1.2 2006-07-19 11:10:38 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<c:if test="${listFieldsSize gt 0}">
<div id="selectListField">
<h3><fmt:message bundle="${nav}" key="groups.summary.select-list"/></h3>
<form action="populateGroupMembers.do">
<input type="hidden" name="groupId" value="<c:out value="${browseParent.id}"/>"/>
	<select name="listField">
		<c:forEach var="listField" items="${listFields}">
			<option><c:out value="${listField}"/></option>
		</c:forEach>
	</select><input type="submit" name="submit.listMembers" value="<fmt:message bundle="${nav}" key="groups.action.edit-members"/>"/>
	<!--<input type="submit" name="submit.addMembers" value="<fmt:message bundle="${nav}" key="find.groups.add-new-members"/>"/>-->

</form>
</div>
</c:if>
</grouper:recordTile>