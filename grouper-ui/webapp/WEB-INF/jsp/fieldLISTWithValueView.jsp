<%-- @annotation@
		  Dynamic tile used to display a List field with value
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: fieldLISTWithValueView.jsp,v 1.4 2008-09-09 20:03:40 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
	<tr class="formTableRow">
		<td class="formTableLeft">
			<c:out value="${fieldList[viewObject.name].displayName}"/>
		</td>
		<td class="formTableRight">
			<span class="fieldIsList"><grouper:message bundle="${nav}" key="groups.summary.field-is-list"/></span>
		</td>
	</tr>
   



