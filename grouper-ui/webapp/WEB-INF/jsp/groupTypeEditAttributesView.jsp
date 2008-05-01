<%-- @annotation@
		  Dynamic tile used to render a GroupType so fields can be edited
--%><%--
  @author Gary Brown.
  @version $Id: groupTypeEditAttributesView.jsp,v 1.2 2008-05-01 04:59:31 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<tr class="formTableRow">
	<td class="formTableLeft">
		<c:out value="${viewObject.name}"/>
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
			
