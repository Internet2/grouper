<%-- @annotation@
		  	Tile which provides contents for HEAD tags
			on all pages derived from baseDef unless
			overridden
--%><%--
  @author Gary Brown.
  @version $Id: head.jsp,v 1.11 2009-11-30 17:14:02 mchyzer Exp $
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
<title><grouper:message key="app.name"/></title>

<c:if test="${!debugPrefs.doHideStyles}">
<c:if test="${mediaMap['grouper-css.hide']!='true'}">
<link href="i2mi/signet.css" rel="stylesheet" type="text/css"/>
<link href="grouper/grouper.css" rel="stylesheet" type="text/css" />
<link href="grouper/grouperTooltip.css" rel="stylesheet" type="text/css" id="grouperTooltipStylesheet"/>
<link href="grouper/grouperInfodot.css" rel="stylesheet" type="text/css" id="grouperInfodotStylesheet"/>
<link href="grouper/grouperPrint.css" rel="stylesheet" type="text/css" media="print" />
</c:if>
<c:if test="${!empty mediaMap['css.additional']}">
	<%
		String css;
		String modulePrefix = (String)request.getAttribute("modulePrefix");
	%>
	<!-- modulePrefix=<%=modulePrefix%>-->
	<c:forTokens var="cssRef" items="${mediaMap['css.additional']}" delims=" ">
	<%
		css = (String)pageContext.findAttribute("cssRef");
		if(modulePrefix == null || "".equals(modulePrefix) ||(css !=null && css.startsWith(modulePrefix))) {
	%>
	<link href="<c:out value="${cssRef}"/>" rel="stylesheet" type="text/css" />
	<%}%>
	</c:forTokens>
</c:if>
</c:if>
<script src="grouperExternal/public/OwaspJavaScriptServlet"></script>
<script type="text/javascript" src="<%=base%>i2mi/signet.js"></script>
<script type="text/javascript" src="scripts/grouper.js"></script> 
</grouper:recordTile>
