<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual group members - which are not links
--%><%--
  @author Gary Brown.
  @version $Id: memberWithoutLinkView.jsp,v 1.3 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>


  <span class="memberLink">
   <c:choose>
		<c:when test="${viewObject.noWays gt 1}">
		
		 <grouper:message key="groups.membership.chain.multiple">
		 	<grouper:param value="${viewObject.noWays}"/>
		 </grouper:message> 
		
	</c:when>
  	<c:when test="${!empty viewObject.viaGroup}">
		
		 <grouper:message key="groups.membership.chain.indirect-member"/> 
	</c:when>
	<c:otherwise>
 		<grouper:message key="groups.membership.chain.member"/>
	</c:otherwise> 
  </c:choose>	

</span>
