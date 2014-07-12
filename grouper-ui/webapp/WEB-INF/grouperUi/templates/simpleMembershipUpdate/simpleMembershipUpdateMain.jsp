<%@ include file="../common/commonTaglib.jsp"%>
<!-- start simpleMembershipUpdateMain.jsp: main page on simpleMembershipUpdate screen -->
<div id="simpleMain">
<%--Group membership update lite --%>
<grouper:title label="${simpleMembershipUpdateContainer.text.updateTitle}"  
  infodotValue="${simpleMembershipUpdateContainer.text.updateTitleInfodot}" />
<%-- grey box --%>
<div class="section">
<%-- Group --%>
<grouper:subtitle label="${simpleMembershipUpdateContainer.text.groupSubtitle}" />

<div class="sectionBody" style="min-width: 500px">
<table border="0" cellpadding="0" cellspacing="0" class=" ${simpleMembershipUpdateContainer.showBreadcrumbRowByDefault ? '' : 'shows_simpleMembershipUpdateGroupDetails'}"
<c:if test="${!simpleMembershipUpdateContainer.showBreadcrumbRowByDefault}">
style="${grouper:hideShowStyle('simpleMembershipUpdateGroupDetails', true)}"
</c:if>
>
  <tr valign="top">
    <td>
    <%-- Breadcrumbs of the folder hierarchy, and change location button --%>
<grouper:groupBreadcrumb label="${simpleMembershipUpdateContainer.text.breadcrumbLabel}"
  groupName="${simpleMembershipUpdateContainer.guiGroup.group.displayName}"
/></td><td> &nbsp; &nbsp; &nbsp; <a class="smallLink" href="grouper.html?operation=SimpleMembershipUpdate.index"
  ><grouper:message value="${simpleMembershipUpdateContainer.text.changeLocation}" valueTooltip="" /></a>
  <c:if test="${mediaMap['ui-lite.link-from-admin-ui']=='true'}">
  &nbsp; &nbsp; &nbsp; <a class="smallLink" href="../../populateGroupSummary.do?groupId=${simpleMembershipUpdateContainer.guiGroup.group.id}"
  ><grouper:message valueTooltip="${simpleMembershipUpdateContainer.text.viewInAdminUiTooltip}"  
  value="${simpleMembershipUpdateContainer.text.viewInAdminUi}"  /></a>
  </c:if>
  <c:if test="${mediaMap['ui-new.link-from-admin-ui']=='true'}">
  &nbsp; &nbsp; &nbsp; <a class="smallLink" href="../../grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=${simpleMembershipUpdateContainer.guiGroup.group.id}"
  ><grouper:message valueTooltip="${simpleMembershipUpdateContainer.text.viewInNewUiTooltip}"  
  value="${simpleMembershipUpdateContainer.text.viewInNewUi}"  /></a>
  </c:if>
  &nbsp; &nbsp; &nbsp;
  <a class="smallLink" id="menuLink" href="#"><grouper:message key="mainMenu.liteLink"/></a>
        <%-- register the menu, and attach it to the link --%>
        <grouper:menu menuId="liteMenu"
        operation="MiscMenu.miscMenu" 
        structureOperation="MiscMenu.miscMenuStructure" 
        contextZoneJqueryHandle="#menuLink" contextMenu="true" />
  
</td>
</tr>
</table>
<%-- shows the group information --%>
<table class="formTable " cellspacing="2" style="margin-bottom: 0;">
  <tbody>
    <tr class="formTableRow ${simpleMembershipUpdateContainer.showNameRowByDefault ? '' : 'shows_simpleMembershipUpdateGroupDetails'}"
    <c:if test="${!simpleMembershipUpdateContainer.showNameRowByDefault}">
      style="${grouper:hideShowStyle('simpleMembershipUpdateGroupDetails', true)}"
    </c:if>
    >
      <td class="formTableLeft"><grouper:message value="${simpleMembershipUpdateContainer.text.groupDisplayExtension}"
        valueTooltip="${simpleMembershipUpdateContainer.text.groupDisplayExtensionTooltip}" /></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.displayExtension)}</td>
    </tr>
    <tr class="formTableRow  ${simpleMembershipUpdateContainer.showPathRowByDefault ? '' : 'shows_simpleMembershipUpdateGroupDetails'}"
        <c:if test="${!simpleMembershipUpdateContainer.showPathRowByDefault}">
          style="${grouper:hideShowStyle('simpleMembershipUpdateGroupDetails', true)}"
        </c:if>
    >
      <td class="formTableLeft"><grouper:message value="${simpleMembershipUpdateContainer.text.groupDisplayName}"
        valueTooltip="${simpleMembershipUpdateContainer.text.groupDisplayNameTooltip}" /></td>
      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.displayName)}</td>
    </tr>

    <tr class="formTableRow ${simpleMembershipUpdateContainer.showDescriptionRowByDefault ? '' : 'shows_simpleMembershipUpdateGroupDetails'}"
        <c:if test="${!simpleMembershipUpdateContainer.showDescriptionRowByDefault}">
          style="${grouper:hideShowStyle('simpleMembershipUpdateGroupDetails', true)}"
        </c:if>
    >

      <td class="formTableLeft"><grouper:message value="${simpleMembershipUpdateContainer.text.groupDescription}"
        valueTooltip="${simpleMembershipUpdateContainer.text.groupDescription}" /></td>
      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.description)}</td>
    </tr>
    <tr class="formTableRow ${simpleMembershipUpdateContainer.showIdRowByDefault ? '' : 'shows_simpleMembershipUpdateGroupDetails'}"
        <c:if test="${!simpleMembershipUpdateContainer.showIdRowByDefault}">
          style="${grouper:hideShowStyle('simpleMembershipUpdateGroupDetails', true)}"
        </c:if>
    >
      <td class="formTableLeft"><grouper:message value="${simpleMembershipUpdateContainer.text.groupExtension}"
        valueTooltip="${simpleMembershipUpdateContainer.text.groupExtensionTooltip}" /></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.extension)}</td>

    </tr>
    <tr class="formTableRow ${simpleMembershipUpdateContainer.showIdPathRowByDefault ? '' : 'shows_simpleMembershipUpdateGroupDetails'}"
        <c:if test="${!simpleMembershipUpdateContainer.showIdPathRowByDefault}">
          style="${grouper:hideShowStyle('simpleMembershipUpdateGroupDetails', true)}"
        </c:if>
    >
      <td class="formTableLeft"><grouper:message value="${simpleMembershipUpdateContainer.text.groupName}"
        valueTooltip="${simpleMembershipUpdateContainer.text.groupNameTooltip}" /></td>
      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.name)}</td>
    </tr>
    <tr class="formTableRow ${simpleMembershipUpdateContainer.showAlternateIdPathRowByDefault ? '' : 'shows_simpleMembershipUpdateGroupDetails'}"
        <c:if test="${!simpleMembershipUpdateContainer.showAlternateIdPathRowByDefault}">
          style="${grouper:hideShowStyle('simpleMembershipUpdateGroupDetails', true)}"
        </c:if>
    >
      <td class="formTableLeft"><grouper:message value="${simpleMembershipUpdateContainer.text.groupAlternateName}"
        valueTooltip="${simpleMembershipUpdateContainer.text.groupAlternateNameTooltip}" /></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.alternateNameDb)}</td>

    </tr>
    <tr class="formTableRow ${simpleMembershipUpdateContainer.showUuidRowByDefault ? '' : 'shows_simpleMembershipUpdateGroupDetails'}"
        <c:if test="${!simpleMembershipUpdateContainer.showUuidRowByDefault}">
          style="${grouper:hideShowStyle('simpleMembershipUpdateGroupDetails', true)}"
        </c:if>
    >
      <td class="formTableLeft"><grouper:message value="${simpleMembershipUpdateContainer.text.groupId}"
        valueTooltip="${simpleMembershipUpdateContainer.text.groupIdTooltip}" /></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.uuid)}</td>
    </tr>

  </tbody>
</table>
<div style="margin-bottom: 8px;">
<%-- advanced button --%>
<a id="advancedLink" href="#" 
><grouper:message value="${simpleMembershipUpdateContainer.text.advancedButton}"/></a>

<%-- register the menu, and attach it to the advanced button --%>
<grouper:menu menuId="advancedMenu"
operation="SimpleMembershipUpdateMenu.advancedMenu" 
structureOperation="SimpleMembershipUpdateMenu.advancedMenuStructure" 
contextZoneJqueryHandle="#advancedLink" contextMenu="true" />

</div>

</div>
</div>

<%-- grey box for the add member form --%>
<div class="section" style="min-width: 750px">
  <%-- add member --%>
  <grouper:subtitle label="${simpleMembershipUpdateContainer.text.addMemberSubtitle}" />

  <div class="sectionBody">
    <form id="simpleMembershipUpdateAddMemberForm" name="simpleMembershipUpdateAddMemberForm" action="whatever">
    <%-- describe the combobox, since it doesnt look like something you would type in to --%>
    <div class="combohint"><grouper:message value="${simpleMembershipUpdateContainer.text.addMemberCombohint}"/></div>
    <%-- note, the combobox does not currently auto adjust its width, so just make it really wide --%>

    <%-- show the combobox --%>
    <grouper:combobox filterOperation="SimpleMembershipUpdateFilter.filterUsers" id="simpleMembershipUpdateAddMember" 
      width="700"/>

       <div style="margin-top: 5px;">
          <%-- add member button --%>
          <input class="blueButton" type="submit" name="addMemberButton"
          onclick="ajax('../app/SimpleMembershipUpdate.addMember', {formIds: 'simpleMembershipUpdateAddMemberForm'}); return false;" 
          value="${simpleMembershipUpdateContainer.text.addMemberButton}" style="margin-top: 2px" />
       </div>
    </form>
    <br />
  </div>
</div>
<%-- div placeholder where the member list will go --%>
<div id="simpleMembershipResultsList" style="min-width: 500px"></div>

</div>
<!-- end simpleMembershipUpdateMain.jsp: main page on simpleMembershipUpdate screen -->

