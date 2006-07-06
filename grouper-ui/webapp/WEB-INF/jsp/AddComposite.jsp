<%-- @annotation@ Top level JSP which allows user to create a composite --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:insert definition="showStemsLocationDef"/>

<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="groups.composite.add">

	</fmt:message>
</h2>
<c:if test="${savedSubjectsSize<2}">
<fmt:message bundle="${nav}" key="groups.composite.add.insufficient-saved"/>
</c:if>
<html:form action="/saveComposite">
<html:hidden property="groupId"/>
<fieldset>
<div class="formRow">
	<div class="formLeft"><fmt:message bundle="${nav}" key="groups.composite.leftGroup"/></div>
	<div class="formRight">
		<html:select property="leftGroup">
			<html:options collection="savedSubjects" property="id" labelProperty="displayName"/>		
		</html:select>
	</div>
</div>
<div class="formRow">
	<div class="formLeft"><fmt:message bundle="${nav}" key="groups.composite.type"/></div>
	<div class="formRight">
		<select name="compositeType">
			<option value="union"><fmt:message bundle="${nav}" key="union"/></option>
			<option value="intersection"><fmt:message bundle="${nav}" key="intersection"/></option>
			<option value="complement"><fmt:message bundle="${nav}" key="complement"/></option>
		</select>
	</div>
</div>
<div class="formRow">
	<div class="formLeft"><fmt:message bundle="${nav}" key="groups.composite.rightGroup"/></div>
	<div class="formRight">
		<html:select property="rightGroup">
			<html:options collection="savedSubjects" property="id" labelProperty="displayName"/>		
		</html:select>
	</div>
</div>
</fieldset>
<c:if test="${savedSubjectsSize>1}">
<html:submit property="submit.saveAndAssign" value="${navMap['groups.composite.add']}"/>
</c:if>
</html:form>
<div class="linkButton"><tiles:insert definition="callerPageButtonDef"/></div>

