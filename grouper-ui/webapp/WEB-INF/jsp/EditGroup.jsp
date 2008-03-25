<%-- @annotation@ 
			Form for creating new groups or editing existing ones.
--%><%--
  @author Gary Brown.
  @version $Id: EditGroup.jsp,v 1.9 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insert definition="showStemsLocationDef"/>
<div class="editForm">
<html:form styleId="GroupFormBean" action="/saveGroup" method="post">
<fieldset>
<html:hidden property="groupId"/>
<div class="formRow">
	<div class="formLeft">
		<label for="groupName"><c:out value="${fieldList.extension.displayName}"/></label>
	</div>
	<div class="formRight">
		<html:text property="groupName" size="50" maxlength="50" styleId="groupName"/>
	</div>
</div>
<div class="formRow">
	<div class="formLeft">
		<label for="groupDisplayName"><c:out value="${fieldList.displayExtension.displayName}"/></label>
	</div>
	<div class="formRight">
		<html:text property="groupDisplayName" size="50" maxlength="50" styleId="groupDisplayName"/>
	</div>
</div>
<div class="formRow">
	<div class="formLeft">               
		<label for="groupDescription"><c:out value="${fieldList.description.displayName}"/></label>
	</div>
	<div class="formRight">
		<html:text property="groupDescription" size="50" maxlength="100" styleId="groupDescription"/>
	</div>
</div>

<div class="formRow">
<div class="formLeft">
<grouper:message bundle="${nav}" key="groups.create.privs-for-all"/>
</div><div class="formRight">
<c:forEach var="priv" items="${privileges}">
<span class="checkbox">
	<input <c:if test="${!empty preSelected[priv]}">checked="checked"</c:if> type="checkbox" name="privileges" value="<c:out value="${priv}"/>"  id="priv<c:out value="${priv}"/>"/> 
					<label for="priv<c:out value="${priv}"/>"><grouper:message bundle="${nav}" key="priv.${priv}"/></label></span>
</c:forEach>
					
</div>
</div>
</div>


<div class="formRow">
<div class="formLeft"><grouper:message bundle="${nav}" key="groups.edit.type"/></div>
<div class="formRight">
<tiles:insert definition="multiOptionDef">
	<tiles:put name="items" beanName="allGroupTypes"/>
	<tiles:put name="selected" beanName="selectedGroupTypes"/>
	<tiles:put name="name" value="groupTypes"/>
	<tiles:put name="property" value="name"/>
	<tiles:put name="columns" value="3"/>
</tiles:insert>
</div>
</div>



<!--<tr>
    <td valign="top"><grouper:message bundle="${nav}" key="groups.edit.type"/></td>
    <td valign="top"><html:select property="groupType" disabled="${editMode}">
    <htmlx:options name="groupTypes" />
   </html:select>
    </td>
</tr>-->
<div class="formRow">
<input type="hidden" name="groupType" value="base" />
 <html:submit property="submit.save" value="${navMap['groups.action.save']}"/>
 <c:if test="${empty GroupFormBean.map.groupId}">
 	<html:submit property="submit.saveAndAssign" value="${navMap['groups.action.save-assign']}"/>
	<html:submit property="submit.saveAndAddComposite" value="${navMap['groups.action.save-add-composite']}"/>
 </c:if>
 </div>
</fieldset>
</html:form>
<div class="linkButton">
<c:if test="${! editMode}">
<html:link page="/populate${browseMode}Groups.do">
	<grouper:message bundle="${nav}" key="groups.create.cancel"/>
</html:link>
</c:if>
<c:if test="${editMode}">
<c:set var="groupAttr" value="${GroupFormBean.map}"/>
<html:link page="/populateGroupSummary.do" paramId="groupId" paramName="groupAttr" paramProperty="groupId">
	<grouper:message bundle="${nav}" key="groups.edit.cancel"/>
</html:link>
</c:if>
</div>
</div>

