<%-- @annotation@ 
			Form for creating new groups or editing existing ones.
--%><%--
  @author Gary Brown.
  @version $Id: EditGroup.jsp,v 1.2 2005-11-08 15:57:01 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insert definition="showStemsLocationDef"/>
<div class="editForm">
<html:form styleId="GroupFormBean" action="/saveGroup">
<fieldset>
<html:hidden property="groupId"/>
<div class="formRow">
	<div class="formLeft">
		<label for="groupName"><fmt:message bundle="${nav}" key="groups.edit.name"/></label>
	</div>
	<div class="formRight">
		<html:text property="groupName" size="50" maxlength="50" disabled="${editMode}" styleId="groupName"/>
	</div>
</div>
<div class="formRow">
	<div class="formLeft">
		<label for="groupDisplayName"><fmt:message bundle="${nav}" key="groups.edit.display-name"/></label>
	</div>
	<div class="formRight">
		<html:text property="groupDisplayName" size="50" maxlength="50" styleId="groupDisplayName"/>
	</div>
</div>
<div class="formRow">
	<div class="formLeft">
		<label for="groupDescription"><fmt:message bundle="${nav}" key="groups.edit.description"/></label>
	</div>
	<div class="formRight">
		<html:text property="groupDescription" size="50" maxlength="100" styleId="groupDescription"/>
	</div>
</div>

<!--<tr>
    <td valign="top"><fmt:message bundle="${nav}" key="groups.edit.type"/></td>
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
 </c:if>
 </div>
</fieldset>
</html:form>
<div class="linkButton">
<c:if test="${! editMode}">
<html:link page="/populate${browseMode}Groups.do">
	<fmt:message bundle="${nav}" key="groups.create.cancel"/>
</html:link>
</c:if>
<c:if test="${editMode}">
<c:set var="groupAttr" value="${GroupFormBean.map}"/>
<html:link page="/populateGroupSummary.do" paramId="groupId" paramName="groupAttr" paramProperty="groupId">
	<fmt:message bundle="${nav}" key="groups.edit.cancel"/>
</html:link>
</c:if>
</div>
</div>

