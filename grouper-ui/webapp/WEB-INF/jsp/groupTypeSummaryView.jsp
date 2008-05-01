<%-- @annotation@
		  Dynamic tile used to render a GroupType - for group summary
--%><%--
  @author Gary Brown.
  @version $Id: groupTypeSummaryView.jsp,v 1.2 2008-05-01 04:59:31 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<tr class="formTableRow">
	<td class="formTableLeft">
		<c:out value="${viewObject.name}"/>
	</td>
	<td class="formTableRight">
    <table class="formTable">
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="viewObject" beanProperty="fields"/>
			<tiles:put name="view" value="groupSummaryFields"/>
			<tiles:put name="itemView" value="withValue"/>
			<tiles:put name="listless" value="TRUE"/>
		</tiles:insert>
    </table>
	</td>
</tr>
			
