<%-- @annotation@
		  Dynamic tile used to render a GroupType - for group summary
--%><%--
  @author Gary Brown.
  @version $Id: groupTypeSummaryView.jsp,v 1.3 2008-09-25 04:54:16 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<tr class="formTableRow">
	<td class="formTableLeft">
    <grouper:message value="${viewObject.name}" 
      valueTooltipKey="groupTypes.${viewObject.name}" />
	</td>
	<td class="formTableRight">
    <table class="formTable">
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="viewObject" beanProperty="fields"/>
			<tiles:put name="view" value="groupSummaryFields"/>
			<tiles:put name="itemView" value="withValue"/>
			<tiles:put name="listless" value="TRUE"/>
		</tiles:insert>
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="viewObject" beanProperty="legacyAttributes"/>
			<tiles:put name="view" value="groupSummaryFields"/>
			<tiles:put name="itemView" value="withValue"/>
			<tiles:put name="listless" value="TRUE"/>
		</tiles:insert>
    </table>
	</td>
</tr>
			
