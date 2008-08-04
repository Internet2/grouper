<%-- @annotation@
		  Dynamic tile used to render a field which is a list field
--%><%--
  @author Gary Brown.
  @version $Id: fieldLISTView.jsp,v 1.3.4.1 2008-08-04 13:05:30 isgwb Exp $
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
   



