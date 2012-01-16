<%-- @annotation@ 
			Form for creating newstems or editing existing ones.
--%><%--
  @author Gary Brown.
  @version $Id: EditStem.jsp,v 1.11 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insert definition="showStemsLocationDef"/>
<div class="editForm section">
  <div class="sectionBody">

    <html:form action="/saveStem" styleId="StemFormBean" method="post">
		<fieldset>
    <html:hidden property="stemId"/>

<table class="formTable formTableSpaced">

  <tr class="formTableRow">
    <td class="formTableLeft">
      <label for="stemDisplayName"><grouper:message key="stems.edit.display-name" /></label>
    </td>
      <td class="formTableRight">
      <html:text property="stemDisplayName" size="50" maxlength="50" styleId="stemDisplayName"/>
      </td>
  </tr>
	<tr class="formTableRow">
		<td class="formTableLeft">
			<label for="stemName"><grouper:message key="stems.edit.name" /></label>
		</td>
   		<td class="formTableRight">
			<html:text property="stemName" size="50" maxlength="50" styleId="stemName"/>
    	</td>
	</tr>
        <c:if test="${!isNewStem}">
          <tr class="formTableRow">
            <td class="formTableLeft"><label for="stemAlternateName"><grouper:message key="stems.edit.alternateName" /></label></td>
            <td class="formTableRight"><html:text property="stemAlternateName" size="50" maxlength="255" styleId="stemAlternateName" /></td>
          </tr>
        </c:if>
	<tr class="formTableRow">
		<td class="formTableLeft">
			<label for="stemDescriptionName"><grouper:message key="stems.edit.description" /></label>
		</td>
    	<td class="formTableRight">
			<html:text property="stemDescription" size="50" maxlength="100" styleId="stemDescription"/> 
		</td>
	</tr>
</fieldset>
</table>

 		<html:submit styleClass="blueButton" property="submit.save" value="${navMap['stems.action.save']}"/> 
		<c:if test="${isNewStem}">
    		<html:submit styleClass="blueButton" property="submit.save_work_in_new" value="${navMap['stems.action.save-work-in-new-stem']}"/>
		</c:if>
		<html:submit styleClass="blueButton" property="submit.save_assign" value="${navMap['stems.action.save-assign']}"/>
</html:form>

<div class="linkButton">
<html:link page="/populate${linkBrowseMode}Groups.do">
	<grouper:message key="stems.edit.cancel"/>
</html:link>
</div>

</div>
</div>



