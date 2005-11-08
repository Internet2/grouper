<%-- @annotation@
		  	Tile which provides contents for HEAD tags
			on all pages derived from baseDef unless
			overridden
--%><%--
  @author Gary Brown.
  @version $Id: head.jsp,v 1.2 2005-11-08 16:10:39 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<%
StringBuffer base = new StringBuffer("http");
if(request.isSecure()) base.append("s");
base.append("://");
base.append(request.getServerName());
int port = request.getServerPort();
//if(port!=80 && (port!=443 && request.isSecure())) {
if(port!=80){

    base.append(":" + port);
}
base.append(request.getContextPath());
base.append("/");
%>
<base href="<%=base%>index.html"/>
<c:set scope="session" var="GM"><%=base%></c:set>

<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="robots" content="noindex, nofollow" />
<title><fmt:message bundle="${nav}" key="app.name"/></title>

<c:if test="${!debugPrefs.doHideStyles}">
<link href="i2mi/signet.css" rel="stylesheet" type="text/css" />
<link href="grouper/grouper.css" rel="stylesheet" type="text/css" />
<c:if test="${!empty mediaMap['css.additional']}">
	<link href="<c:out value="${mediaMap['css.additional']}"/>" rel="stylesheet" type="text/css" />
</c:if>
</c:if>
<script type="text/javascript" src="<%=base%>i2mi/signet.js"></script>
</grouper:recordTile>
