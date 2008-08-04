<%-- @annotation@
		  Dynamic tile used to display a List field with value
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: fieldLISTWithValueView.jsp,v 1.3.4.1 2008-08-04 13:05:30 isgwb Exp $
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
   



