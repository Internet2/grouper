<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: deleteGroup-group.jsp,v 1.1 2009-07-16 11:33:34 isgwb Exp $
--%><%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:out value="${viewObject.fields.id}"/> - <c:out value="${viewObject.fields.name}"/>