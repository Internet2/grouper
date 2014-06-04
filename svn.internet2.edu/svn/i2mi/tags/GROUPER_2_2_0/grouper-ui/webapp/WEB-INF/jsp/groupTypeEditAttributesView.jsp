<%-- @annotation@
		  Dynamic tile used to render a GroupType so fields can be edited
--%><%--
  @author Gary Brown.
  @version $Id: groupTypeEditAttributesView.jsp,v 1.3 2008-09-25 04:54:16 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<tr class="formTableRow">
	<td class="formTableLeft">
      <grouper:message value="${viewObject.name}" 
      valueTooltipKey="groupTypes.${viewObject.name}" />
	</td>
  <td>&nbsp;</td>
  <td>&nbsp;</td>
</tr>
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="viewObject" beanProperty="fields"/>
			<tiles:put name="view" value="editAttributesFields"/>
			<tiles:put name="itemView" value="editValue"/>
			<tiles:put name="listless" value="TRUE"/>
		</tiles:insert>
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="viewObject" beanProperty="legacyAttributes"/>
			<tiles:put name="view" value="editAttributesFields"/>
			<tiles:put name="itemView" value="editValue"/>
			<tiles:put name="listless" value="TRUE"/>
		</tiles:insert>
			
