<%-- @annotation@
		  Dynamic tile which displays content after the elements of 
		  a dynamic list - used to allow paging of lists > than the
		  current page size
--%><%--
  @author Gary Brown.
  @version $Id: genericListFooterView.jsp,v 1.4 2009-08-10 14:03:01 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute  ignore="true"/>
<c:if test="${!empty pager && pager.count>0}">
<div class="linkButton">
	<c:if test="${pager.prev}">
		<html:link page="${pager.target}.do" name="pager" property="prevParams"><grouper:message bundle="${nav}" key="find.previous-page"/></html:link>		
	</c:if>
	<c:if test="${pager.next}">
		<html:link page="${pager.target}.do" name="pager" property="nextParams"><grouper:message bundle="${nav}" key="find.next-page"/></html:link>		
	</c:if>
	
	</div>
</c:if>
