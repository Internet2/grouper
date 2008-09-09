<%-- @annotation@
		  Dynamic tile used to display a custom field name and value
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: fieldATTRIBUTEWithValueView.jsp,v 1.3 2008-09-09 20:03:40 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<tr class="formTableRow">
	<td class="formTableLeft">
		<c:out value="${fieldList[viewObject.name].displayName}"/>
	</td>
	<td class="formTableRight">
		<c:out value="${group[viewObject.name]}"/>
	</td>
</tr>
   



