<%-- @annotation@
		  Dynamic tile which displays content after the elements of 
		  a dynamic list - used to allow paging of lists > than the
		  current page size
--%><%--
  @author Gary Brown.
  @version $Id: genericListFooterView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
<c:if test="${pager.count>0}">
<div class="linkButton">
	<c:if test="${pager.prev}">
		<html:link page="${pager.target}.do" name="pager" property="prevParams"><fmt:message bundle="${nav}" key="find.previous-page"/></html:link>		
	</c:if>
	<c:if test="${pager.next}">
		<html:link page="${pager.target}.do" name="pager" property="nextParams"><fmt:message bundle="${nav}" key="find.next-page"/></html:link>		
	</c:if>
	
	</div>
	<br/>
</c:if>
