<%-- @annotation@ Top level JSP which allows user to create a composite --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<div class="section">
<grouper:subtitle key="groups.composite.add" />
  <div class="sectionBody">

<tiles:insert definition="showStemsLocationDef"/>



<c:if test="${savedSubjectsSize<2}">
<grouper:message key="groups.composite.add.insufficient-saved"/>
</c:if>
<html:form action="/saveComposite" method="post">
<input type="hidden" name="groupId" value="<c:out value="${browseParent.groupId}"/>">
<fieldset>
<table class="formTable">
<tr class="formTableRow">
	<td class="formTableLeft"><grouper:message key="groups.composite.leftGroup"/></td>
	<td class="formTableRight">
		<html:select property="leftGroup">
			<html:options collection="savedSubjects" property="id" labelProperty="displayName"/>		
		</html:select>
	</td>
</tr>
<tr class="formTableRow">
	<td class="formTableLeft"><grouper:message key="groups.composite.type"/></td>
	<td class="formTableRight">
		<select name="compositeType">
			<option value="union"><grouper:message key="union"/></option>
			<option value="intersection"><grouper:message key="intersection"/></option>
			<option value="complement"><grouper:message key="complement"/></option>
		</select>
	</td>
</tr>
<tr class="formTableRow">
	<td class="formTableLeft"><grouper:message key="groups.composite.rightGroup"/></td>
	<td class="formTableRight">
		<html:select property="rightGroup">
			<html:options collection="savedSubjects" property="id" labelProperty="displayName"/>		
		</html:select>
	</td>
</tr>
</table>
</fieldset>
<c:if test="${savedSubjectsSize>1}">
<html:submit styleClass="blueButton" property="submit.saveAndAssign" value="${navMap['groups.composite.add']}"/>
</c:if>
</html:form>
<div class="linkButton"><tiles:insert definition="callerPageButtonDef"/></div>
</div>
</div>

