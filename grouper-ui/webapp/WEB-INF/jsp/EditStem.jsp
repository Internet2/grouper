<%-- @annotation@ 
			Form for creating newstems or editing existing ones.
--%><%--
  @author Gary Brown.
  @version $Id: EditStem.jsp,v 1.2 2005-11-08 15:57:15 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insert definition="showStemsLocationDef"/>
<div class="editForm">

    <html:form action="/saveStem" styleId="StemFormBean">
		<fieldset>
    <html:hidden property="stemId"/>

	<div class="formRow">
		<div class="formLeft">
			<label for="stemName"><fmt:message bundle="${nav}" key="stems.edit.name"/></label>
		</div>
   		<div class="formRight">
			<html:text property="stemName" size="50" maxlength="50" disabled="${!isNewStem}" styleId="stemName"/>
    	</div>
	</div>
	<div class="formRow">
		<div class="formLeft">
			<label for="stemDisplayName"><fmt:message bundle="${nav}" key="stems.edit.display-name"/></label>
		</div>
    	<div class="formRight">
			<html:text property="stemDisplayName" size="50" maxlength="50" styleId="stemDisplayName"/>
    	</div>
	</div>
	<div class="formRow">
		<div class="formLeft">
			<label for="stemDescriptionName"><fmt:message bundle="${nav}" key="stems.edit.description"/></label>
		</div>
    	<div class="formRight">
			<html:text property="stemDescription" size="50" maxlength="100" styleId="stemDescription"/> 
		</div>
	</div>
	<div class="formRow">
 		<html:submit property="submit.save" value="${navMap['stems.action.save']}"/> 
		<c:if test="${isNewStem}">
    		<html:submit property="submit.save_work_in_new" value="${navMap['stems.action.save-work-in-new-stem']}"/>
		</c:if>
		<html:submit property="submit.save_assign" value="${navMap['stems.action.save-assign']}"/>
	</div>
</fieldset>
</html:form>

<div class="linkButton">
<html:link page="/populate${browseMode}Groups.do">
	<fmt:message bundle="${nav}" key="stems.edit.cancel"/>
</html:link>
</div>
</div>



