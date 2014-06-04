<%-- @annotation@
		  Dynamic tile used to display a custom field name and value
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: fieldATTRIBUTEWithValueView.jsp,v 1.4 2008-09-25 04:54:16 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<tr class="formTableRow">
	<td class="formTableLeft">
      <grouper:message value="${fieldList[viewObject.name].displayName}" 
         valueTooltipKey="groupFields.${fieldList[viewObject.name].displayName}" />
	</td>
	<td class="formTableRight">
	<c:choose>
	<c:when test="${groupPrivResolver.canReadField[viewObject.name]}">${group[viewObject.name]}</c:when>
			<c:otherwise>
			<grouper:message key="group.view-attribute.insufficient-privileges"/>
			</c:otherwise>
		</c:choose>
	</td>
</tr>
   



