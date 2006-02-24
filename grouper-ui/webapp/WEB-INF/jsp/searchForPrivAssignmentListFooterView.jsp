<%-- @annotation@
		  Dynamic tile used by genericList mechanism to render
		  content below list items - i.e. is an alternative footer
		  when searching for subjects in 'Find' mode
--%><%--
  @author Gary Brown.
  @version $Id: searchForPrivAssignmentListFooterView.jsp,v 1.3 2006-02-24 13:47:00 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
<c:if test="${pager.count>0}"><div class="linkButton">
	<c:if test="${pager.prev}">
		<html:link page="${pager.target}.do" name="pager" property="prevParams"><fmt:message bundle="${nav}" key="find.previous-page"/></html:link>&#160;&#160;		
	</c:if>
	<c:if test="${pager.next}">
		<html:link page="${pager.target}.do" name="pager" property="nextParams"><fmt:message bundle="${nav}" key="find.next-page"/></html:link>&#160;&#160;		
	</c:if>
	</div>
</c:if>
<div class="linkButton">
<tiles:insert definition="callerPageButtonDef"/>
<c:if test="${!forStems}">
<html:link page="/cancelFindNewMembers.do" paramId="groupId" paramName="findForNode"  >
	<fmt:message bundle="${nav}" key="find.for-groups.cancel"/>
</html:link></c:if>
<c:if test="${forStems}">
<html:link page="/cancelFindNewMembers.do" paramId="currentNode" paramName="findForNode"  >
	<fmt:message bundle="${nav}" key="find.for-stems.cancel"/>
</html:link></c:if>
</div>

<c:if test="${pager.count>0}">
		<p><br/><input type="submit" name="submit.group.member" value="<c:out value="${navMap['priv.assign']}"/>"/></p>
	</c:if>
	</form>
	</div>
