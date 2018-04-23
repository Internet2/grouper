<%-- @annotation@
		  Dynamic tile used to display a List field with value
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: fieldLISTWithValueView.jsp,v 1.5 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
	<tr class="formTableRow">
		<td class="formTableLeft">
			<c:out value="${fieldList[viewObject.name].displayName}"/>
		</td>
		<td class="formTableRight">
			<span class="fieldIsList"><grouper:message key="groups.summary.field-is-list"/></span>
		</td>
	</tr>
   



