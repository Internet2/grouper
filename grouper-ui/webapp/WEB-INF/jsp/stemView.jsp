<%-- @annotation@
		  Dynamic tile which renders a stem
--%><%--
  @author Gary Brown.
  @version $Id: stemView.jsp,v 1.1.1.1.2.1 2006-03-10 10:23:18 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
<span class="stemView"><c:out value="${viewObject[mediaMap['stem.default.attribute']]}"/></span>