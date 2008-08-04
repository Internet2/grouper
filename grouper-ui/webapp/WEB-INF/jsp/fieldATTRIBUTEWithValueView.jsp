<%-- @annotation@
		  Dynamic tile used to display a custom field name and value
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: fieldATTRIBUTEWithValueView.jsp,v 1.2.4.1 2008-08-04 13:05:30 isgwb Exp $
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
   



