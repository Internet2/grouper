<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual group members - which are not links
--%><%--
  @author Gary Brown.
  @version $Id: memberWithoutLinkView.jsp,v 1.1 2006-02-21 16:50:18 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>


  <span class="memberLink">
   <c:choose>
		<c:when test="${viewObject.noWays gt 1}">
		
		 <fmt:message bundle="${nav}" key="groups.membership.chain.multiple">
		 	<fmt:param value="${viewObject.noWays}"/>
		 </fmt:message> 
		
	</c:when>
  	<c:when test="${!empty viewObject.viaGroup}">
		
		 <fmt:message bundle="${nav}" key="groups.membership.chain.indirect-member"/> 
	</c:when>
	<c:otherwise>
 		<fmt:message bundle="${nav}" key="groups.membership.chain.member"/>
	</c:otherwise> 
  </c:choose>	

</span>
