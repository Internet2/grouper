<%-- @annotation@
			Tile which lists any privileges the user attempted to revoke - but which failed
--%><%--
  @author Gary Brown.
  @version $Id: failedRevocations.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="faileRevocations"><fmt:message bundle="${nav}" key="priv.action.revocation-failure"/>
	<ul>
		<c:forEach var="priv" items="${failedRevocations}">
		<li><c:out value="${priv}"/></li>
		</c:forEach>
	</ul>
</div>
</grouper:recordTile>