<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual group members - which are not links
--%><%--
  @author Gary Brown.
  @version $Id: memberWithoutLinkView.jsp,v 1.2 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>


  <span class="memberLink">
   <c:choose>
		<c:when test="${viewObject.noWays gt 1}">
		
		 <grouper:message bundle="${nav}" key="groups.membership.chain.multiple">
		 	<grouper:param value="${viewObject.noWays}"/>
		 </grouper:message> 
		
	</c:when>
  	<c:when test="${!empty viewObject.viaGroup}">
		
		 <grouper:message bundle="${nav}" key="groups.membership.chain.indirect-member"/> 
	</c:when>
	<c:otherwise>
 		<grouper:message bundle="${nav}" key="groups.membership.chain.member"/>
	</c:otherwise> 
  </c:choose>	

</span>
