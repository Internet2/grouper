<%-- @annotation@
		  Dynamic tile which allows users to edit custom group attributes
--%><%--
  @author Gary Brown.
  @version $Id: fieldATTRIBUTEEditValueView.jsp,v 1.5 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<tr class="formTableRow">
    <%-- empty for type name --%>
    <td>&nbsp;</td>
		<td class="formTableLeft">
			<c:if test="${viewObject.required}"><c:set var="areRequiredAttributes" 
        value="true" scope="request"/><span class="requiredAttrIndicator"><grouper:message 
        key="attribute.required.indicator"/></span></c:if><grouper:message 
        value="${fieldList[viewObject.name].displayName}" 
         valueTooltipKey="groupFields.${fieldList[viewObject.name].displayName}" /></td>
		<td class="formTableRight">
			<input type="text" name="attr.<c:out value="${viewObject.name}"/>" value="<c:out value="${group[viewObject.name]}"/>" size="50"/>
			
		</td>
</tr>   



