<%-- @annotation@
		  The help page. Includes getting started info and general
		  help. This text is not derived from ResourceBundles as
		  it makes more sense for a site to override the whole JSP 
		  pages.
--%><%--
  @author Gary Brown.
  @version $Id: Help.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<div id="grouperIntro">
<tiles:insert definition="gettingStartedDef"/>
</div>
<div id="grouperHelpForMenu">
<tiles:insert definition="generalHelpDef"/>
</div>
  


