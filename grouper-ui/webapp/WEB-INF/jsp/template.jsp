<%-- @annotation@
		  template.jsp controls the order in which page components are output and which 
		  components are nested in which structural DIV tags. Individual pages may be 
		  customised by overriding the various definitions referred to by the 'put' tags. 
		  CSS can, to some extent, be used to re-position components, however, some 
		  re-arrangements may require a replacement template.jsp, or post processing of 
		  the generated XHTML.
--%><%--
  @author Gary Brown.
  @version $Id: template.jsp,v 1.9 2008-05-01 09:44:22 isgwb Exp $
--%><?xml version="1.0" encoding="utf-8"?>

<!DOCTYPE html PUBLIC 
"-//W3C//DTD XHTML 1.0 Transitional//EN" 
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 

<%@page import="org.apache.struts.tiles.ComponentContext"%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<grouper:recordTile key="Not dynamic" tile="/WEB-INF/jsp/template.jsp">
<html:html lang="en" xhtml="true">
<html:xhtml/>
<%@page import="java.io.PrintWriter"%>
<%
StringBuffer pageUrl = (StringBuffer) request.getAttribute("_pageUrl");
if(pageUrl==null) pageUrl=new StringBuffer();
String pageUrlMinusQuery = pageUrl.toString();

request.setAttribute("pageUrlMinusQueryString", pageUrlMinusQuery); 
char delim = '?';
if(request.getQueryString()!=null) {
	pageUrl.append("?" + request.getQueryString());
	delim='&';
}
request.setAttribute("pageUrl",pageUrl.toString());
pageUrl.append(delim);
request.setAttribute("pageUrlWithDelim",pageUrl.toString());
%>
<!--init-->
<tiles:insert attribute="init"/>
<!--/init--><head>
    <tiles:insert attribute="head"/>
</head>


<% try {
	
ComponentContext tContext = ComponentContext.getContext(request);
pageContext.setAttribute("parentTilesContext",tContext);
String prefix = org.apache.struts.util.ModuleUtils.getInstance().getModuleConfig(request).getPrefix();
request.setAttribute("modulePrefix",prefix);
%>
<tiles:insert definition="bodyDef" controllerUrl="${modulePrefix}/propogateTilesAttributes.do">
	<tiles:put name="parentTilesContext" beanName="parentTilesContext"/>
</tiles:insert>
	
	<%
	
		}catch(Exception e) {
		pageContext.setAttribute("templateException",e);
	%>
		<c:if test="${!debugPrefs.isActive}">
			<c:set var="throwTemplateException" value="y"/>
		</c:if>
	<%}%>
	<tiles:insert attribute="debug" />
	
    <c:if test="${!empty templateException && debugPrefs.isActive}">


		
<pre>
		<%
			Exception te = (Exception)pageContext.getAttribute("templateException");
			if(te.getMessage()!=null) out.write("\n" + te.getMessage() + "\n");
			te.printStackTrace(new PrintWriter(out));
		%>
		</pre>
		
	</c:if>
<%
	if(pageContext.getAttribute("throwTemplateException")!=null) {
				session.setAttribute("templateException",pageContext.getAttribute("templateException"));		
	}
%>
 <c:if test="${!empty sessionScope.templateException && !debugPrefs.isActive}">
	<script type="text/javascript">
		document.location.replace("error.do");
	</script>
  </c:if>	        
</body>
</html:html>
</grouper:recordTile>