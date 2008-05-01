<%-- @annotation@
		  Dynamic tile used to display a custom field name and value
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: fieldATTRIBUTEWithValueView.jsp,v 1.2 2008-05-01 04:59:31 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<tr class="formTableRow">
	<td class="formTableLeft">
		<c:out value="${viewObject.name}"/>
	</td>
	<td class="formTableRight">
		<c:out value="${group[viewObject.name]}"/>
	</td>
</tr>
   



