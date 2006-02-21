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
<html:form styleId="EditGroupAttributesForm" action="/saveGroupAttributes">
<html:hidden property="groupId"/>
<div class="groupInfo">
<div class="groupTypes">
<div class="typesHeader">
	<div class="formRow">
		<div class="formLeft">
			<fmt:message bundle="${nav}" key="groups.edit-attributes.types-header"/>
		</div>
		<div class="formRight">
			<div class="formRow">
				<div class="formLeft">
					<fmt:message bundle="${nav}" key="groups.edit-attributes.attributes-header"/>
				</div>
				<div class="formRight">
					<fmt:message bundle="${nav}" key="groups.edit-attributes.values-header"/>
				</div>
			</div>
		</div>
	</div>
</div>
<tiles:insert definition="dynamicTileDef">
	<tiles:put name="viewObject" beanName="group" beanProperty="types"/>
	<tiles:put name="view" value="editGroupAttributes"/>
	<tiles:put name="itemView" value="editGroupAttributes"/>
	<tiles:put name="listless" value="TRUE"/>
</tiles:insert>
</div>
<div class="formRow">
 <html:submit property="submit.save" value="${navMap['groups.action.attr-save']}"/>
 <html:submit property="submit.save_add" value="${navMap['groups.action.attr-save-add']}"/>
 </div>
 </div>
</html:form>

