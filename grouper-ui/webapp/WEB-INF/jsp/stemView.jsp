<%-- @annotation@
		  Dynamic tile which renders a stem
--%><%--
  @author Gary Brown.
  @version $Id: stemView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
<span class="stemView"><c:out value="${viewObject.displayExtension}"/></span>