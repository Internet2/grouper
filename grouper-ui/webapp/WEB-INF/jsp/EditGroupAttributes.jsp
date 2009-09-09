<%-- @annotation@ Form(s) for modifying attributes. This is a key page. Should 
some values be limited to:
1) A lookup list (how is this defined)
2) Subject to validation rules (how and where defined?)
3) Be dependent on earlier choices i.e. entered in several 
steps with page reload in between

Can save and 
1) return to GroupSummary
2) continue and view current members
3) continue to add members --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:insert definition="showStemsLocationDef" controllerUrl="/prepareBrowsePath.do"/>
<html:form styleId="EditGroupAttributesForm" action="/saveGroupAttributes" method="post">
<html:hidden property="groupId"/>
  <table class="formTable">
	<tr class="typesHeader groupTypes groupInfo formTableRow">
		<td class="formTableLeft">
			<grouper:message key="groups.edit-attributes.types-header"/>
		</td>
		<td class="formTableLeft">
			<grouper:message key="groups.edit-attributes.attributes-header"/>
		</td>
		<td class="formTableLeft" style="text-align: left">
			<grouper:message key="groups.edit-attributes.values-header"/>
		</td>
	</tr>
<c:if test="${!empty group.types}">
<tiles:insert definition="dynamicTileDef">
	<tiles:put name="viewObject" beanName="group" beanProperty="types"/>
	<tiles:put name="view" value="editGroupAttributes"/>
	<tiles:put name="itemView" value="editGroupAttributes"/>
	<tiles:put name="listless" value="TRUE"/>
</tiles:insert>
</c:if>
<c:if test="${!empty areRequiredAttributes}">
<tr class="formTableRow">
<td></td><td  class="formTableLeft"><span class="requiredAttrInfo"><grouper:message key="attribute.required.info"/></span></td><td></td></tr>
</c:if>
<tr class="formTableRow">
<td colspan="3">
 <html:submit styleClass="blueButton" property="submit.save" value="${navMap['groups.action.attr-save']}"/>
 <html:submit styleClass="blueButton" property="submit.save_add" value="${navMap['groups.action.attr-save-add']}"/>
 </td>
 </tr>
 </table>
</html:form>

