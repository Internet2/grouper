<%-- @annotation@
		  Dynamic tile used to render a field which is a list field
--%><%--
  @author Gary Brown.
  @version $Id: fieldLISTView.jsp,v 1.4 2008-09-09 20:03:40 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
	<tr class="formTableRow">
	<td>&nbsp;</td>
		<td class="formTableLeft">
			<c:out value="${fieldList[viewObject.name].displayName}"/>
		</td>
    <%-- on the edit screen there there 3 cols, this should take up two --%>
		<td class="formTableRight" colspan="2">
			<span class="fieldIsList"><grouper:message bundle="${nav}" key="groups.summary.field-is-list"/></span>
		</td>
	</tr>
   



