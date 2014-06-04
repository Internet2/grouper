<%-- @annotation@
		  Dynamic tile which displays content after the elements of 
		  a dynamic list - used to allow paging of lists > than the
		  current page size
--%><%--
  @author Gary Brown.
  @version $Id: genericListFooterView.jsp,v 1.5 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute  ignore="true"/>
<c:if test="${pager_removeFromSubjectSearch != 'true'}">

  <c:if test="${!empty pager && pager.count>0}">
  <div class="linkButton">
  	<c:if test="${pager.prev}">
  		<html:link page="${pager.target}.do" name="pager" property="prevParams"><grouper:message key="find.previous-page"/></html:link>		
  	</c:if>
  	<c:if test="${pager.next}">
  		<html:link page="${pager.target}.do" name="pager" property="nextParams"><grouper:message key="find.next-page"/></html:link>		
  	</c:if>
  	
  	</div>
  </c:if>
</c:if>