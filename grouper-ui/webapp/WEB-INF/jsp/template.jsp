<%-- @annotation@
		  template.jsp controls the order in which page components are output and which 
		  components are nested in which structural DIV tags. Individual pages may be 
		  customised by overriding the various definitions referred to by the 'put' tags. 
		  CSS can, to some extent, be used to re-position components, however, some 
		  re-arrangements may require a replacement template.jsp, or post processing of 
		  the generated XHTML.
--%><%--
  @author Gary Brown.
  @version $Id: template.jsp,v 1.2 2005-11-08 16:25:55 isgwb Exp $
--%><?xml version="1.0" encoding="iso-8859-1"?>

<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">




<%@include file="/WEB-INF/jsp/include.jsp"%>
<%@page import="java.io.PrintWriter"%>
<%
StringBuffer pageUrl = request.getRequestURL();
request.setAttribute("pageUrlMinusQueryString",pageUrl.toString());
char delim = '?';
if(request.getQueryString()!=null) {
	pageUrl.append("?" + request.getQueryString());
	delim='&';
}
request.setAttribute("pageUrl",pageUrl.toString());
pageUrl.append(delim);
request.setAttribute("pageUrlWithDelim",pageUrl.toString());
%>

<tiles:insert attribute="init"/>
<html:html lang="en" xhtml="true">
<html:xhtml/>
<head>
    <tiles:insert attribute="head"/>
</head>
<body>
<% try {
	%>
   		<!--ContentSpace-->
        <div id="ContentSpace">
            <div id="TitleBox">
               <tiles:insert attribute="title" />
            </div>
			
            <c:if test="${!empty message}">
                    <tiles:insert attribute="message" />   
            </c:if>
            <!--content-->
            <div id="Content">
                <tiles:insert attribute='contentwrapper'>
					<tiles:put name="tile"><tiles:getAsString name="content"/></tiles:put>
				</tiles:insert>
            </div>
            <!--/content-->
        	<!--Right-->
			<div id="Right">
				<tiles:insert attribute="right" />
			</div><!--/Right-->
			<!--NavBar-->
			<div id="Navbar">
				<tiles:insert attribute='subheader'/>
			</div><!--/NavBar-->
			<!--Left-->
			<div id="left">
				<tiles:insert attribute="left" />
			</div><!--/Left-->
			<c:if test="${!empty authUser}">
				<!--SideBar-->
				<div id="Sidebar">
					<tiles:insert attribute="menu" />
				</div><!--/SideBar-->
			</c:if>
			<!--Header-->
			 <div id="Header">
				<tiles:insert attribute="header" />
			 </div><!--/Header-->
    		<!--Footer-->
			<div id="Footer">
				<tiles:insert attribute="footer" />
			</div><!--Footer-->
    </div><!--/ContentSpace--> 
	<%
	
		}catch(Exception e) {
		pageContext.setAttribute("templateException",e);
	%>
		<c:if test="${!debugPrefs.isActive}">
			<c:set var="throwTemplateException" value="y"/>
		</c:if>
	<%}%>
	<tiles:insert attribute="debug" />
	
    <c:if test="${!empty templateException && debugPrefs.isActive}"><pre>
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
