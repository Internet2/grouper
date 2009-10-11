<%@ include file="../common/commonTaglib.jsp"%>
<!-- start simpleMembershipUpdateMain.jsp: main page on simpleMembershipUpdate screen -->
<div id="simpleMain">
<%--Group membership update lite --%>
<grouper:title key="simpleMembershipUpdate.updateTitle" />
<%-- grey box --%>
<div class="section">
<%-- Group --%>
<grouper:subtitle key="simpleMembershipUpdate.groupSubtitle" />

<div class="sectionBody" style="min-width: 500px">
<table border="0" cellpadding="0" cellspacing="0">
  <tr valign="top">
    <td>
    <%-- Breadcrumbs of the folder hierarchy, and change location button --%>
<grouper:groupBreadcrumb
  groupName="${simpleMembershipUpdateContainer.guiGroup.group.displayName}"
/></td><td> &nbsp; &nbsp; &nbsp; <a class="smallLink" href="#operation=SimpleMembershipUpdate.index"
  ><grouper:message key="simpleMembershipUpdate.changeLocation" /></a>
</td>
</tr>
</table>
<%-- shows the group information --%>
<table class="formTable" cellspacing="2" style="margin-bottom: 0;">
  <tbody>
    <tr class="formTableRow">
      <td class="formTableLeft"><grouper:message key="groups.summary.display-extension" /></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.displayExtension)}</td>
    </tr>
    <tr class="formTableRow">
      <td class="formTableLeft"><grouper:message key="groups.summary.display-name" /></td>
      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.displayName)}</td>
    </tr>

    <tr class="formTableRow">

      <td class="formTableLeft"><grouper:message key="groups.summary.description" /></td>
      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.description)}</td>
    </tr>
  </tbody>
</table>
<%-- shows the group details, hidden by default --%>
<table class="formTable shows_simpleMembershipUpdateGroupDetails" cellspacing="2" 
    style="margin: 0; ${grouper:hideShowStyle('simpleMembershipUpdateGroupDetails', true)}">
  <tbody>
    <tr class="formTableRow ">
      <td class="formTableLeft"><grouper:message key="groups.summary.extension" /></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.extension)}</td>

    </tr>
    <tr class="formTableRow ">
      <td class="formTableLeft"><grouper:message key="groups.summary.name" /></td>
      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.name)}</td>
    </tr>
    <tr class="formTableRow ">
      <td class="formTableLeft"><grouper:message key="groups.summary.id" /></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.uuid)}</td>
    </tr>

  </tbody>
</table>
<div style="margin-bottom: 8px;">
<%-- advanced button --%>
<a id="advancedLink" href="#" 
><grouper:message key="simpleMembershipUpdate.advancedButton"/></a>

<%-- register the menu, and attach it to the advanced button --%>
<grouper:menu menuId="advancedMenu"
operation="SimpleMembershipUpdateMenu.advancedMenu" 
structureOperation="SimpleMembershipUpdateMenu.advancedMenuStructure" 
contextZoneJqueryHandle="#advancedLink" contextMenu="true" />

</div>

</div>
</div>

<%-- grey box for the add member form --%>
<div class="section" style="min-width: 900px">
  <%-- add member --%>
  <grouper:subtitle key="simpleMembershipUpdate.addMemberSubtitle" />

  <div class="sectionBody">
    <form id="simpleMembershipUpdateAddMemberForm" name="simpleMembershipUpdateAddMemberForm" action="whatever">
    <%-- describe the combobox, since it doesnt look like something you would type in to --%>
    <div class="combohint"><grouper:message key="simpleMembershipUpdate.addMemberCombohint"/></div>
    <%-- note, the combobox does not currently auto adjust its width, so just make it really wide --%>
    <table width="900" cellpadding="0" cellspacing="0">
      <tr valign="top">
        <td style="padding: 0px" width="710">
          <%-- show the combobox --%>
          <grouper:combobox filterOperation="SimpleMembershipUpdateFilter.filterUsers" id="simpleMembershipUpdateAddMember" 
            width="700"/>
        </td>
        <td>
          <%-- add member button --%>
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleMembershipUpdate.addMember', {formIds: 'simpleMembershipUpdateAddMemberForm'}); return false;" 
          value="${grouper:message('simpleMembershipUpdate.addMemberButton', true, false) }" style="margin-top: 2px" />
        </td>
      </tr>
    </table>
    </form>
    <br />
  </div>
</div>
<%-- div placeholder where the member list will go --%>
<div id="simpleMembershipResultsList" style="min-width: 500px"></div>

</div>
<!-- end simpleMembershipUpdateMain.jsp: main page on simpleMembershipUpdate screen -->

