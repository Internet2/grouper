<%-- @annotation@
		  Tile used as separator between links. Intended for use in 
		  lists of results where > 1 link shown next to each other
		  scope attribute passed to allow for conditional output
--%><%--
  @author Gary Brown.
  @version $Id: linkSeparator.jsp,v 1.1 2005-11-08 15:24:34 isgwb Exp $
--%><%@page import="org.apache.struts.tiles.ComponentContext"%><%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
<span class="linkSeparator">:</span>