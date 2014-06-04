<%-- @annotation@
		  Dynamic tile used to render a field which is a list field
--%><%--
  @author Gary Brown.
  @version $Id: fieldSchemaView.jsp,v 1.1 2009-08-10 14:03:01 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
	<tr>
		<td class="fieldName"><c:out value="${viewObject.name}"/></td>
		<td><c:out value="${viewObject.type}"/></td>
		<td><c:out value="${viewObject.readPrivilege}"/></td>
		<td><c:out value="${viewObject.writePrivilege}"/></td>
	
	</tr>
   



