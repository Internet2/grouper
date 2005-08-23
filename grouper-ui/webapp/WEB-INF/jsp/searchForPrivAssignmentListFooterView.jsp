<%-- @annotation@
		  Dynamic tile used by genericList mechanism to render
		  content below list items - i.e. is an alternative footer
		  when searching for subjects in 'Find' mode
--%><%--
  @author Gary Brown.
  @version $Id: searchForPrivAssignmentListFooterView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
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

<c:if test="${pager.count>0}">
		<p><br/><input type="submit" name="submit.group.member" value="<c:out value="${navMap['priv.assign']}"/>"/></p>
	</c:if>
	</form>
	</div>
