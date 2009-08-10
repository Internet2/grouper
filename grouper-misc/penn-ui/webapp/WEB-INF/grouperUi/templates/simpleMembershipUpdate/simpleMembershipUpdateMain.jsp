<%@ include file="../common/commonTaglib.jsp"%>
<!-- simpleMembershipUpdate/simpleMembershipUpdateMain.html: main page on simpleMembershipUpdate screen -->
<div id="simpleMain">

<grouperGui:title key="simpleMembershipUpdate.updateTitle" />

<div class="section">

<grouperGui:subtitle key="simpleMembershipUpdate.groupSubtitle" />

<div class="sectionBody">
<table border="0" cellpadding="0" cellspacing="0">
  <tr valign="top">
    <td>
<grouperGui:groupBreadcrumb
  groupName="${simpleMembershipUpdateContainer.guiGroup.group.displayName}"
/></td><td> &nbsp; &nbsp; &nbsp; <a class="smallLink" href="#operation=SimpleMembershipUpdate.index"
  ><grouperGui:message key="simpleMembershipUpdate.changeLocation" /></a>
</td>
</tr>
</table>

<table class="formTable" cellspacing="2" style="margin-bottom: 0;">
  <tbody>
    <tr class="formTableRow">
      <td class="formTableLeft"><grouperGui:message key="groups.summary.display-extension" /></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.displayExtension)}</td>
    </tr>
    <tr class="formTableRow">
      <td class="formTableLeft"><grouperGui:message key="groups.summary.display-name" /></td>
      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.displayName)}</td>
    </tr>

    <tr class="formTableRow">

      <td class="formTableLeft"><grouperGui:message key="groups.summary.description" /></td>
      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.description)}</td>
    </tr>
  </tbody>
</table>

<table class="formTable shows_simpleMembershipUpdateGroupDetails" cellspacing="2" 
    style="margin: 0; ${grouperGui:hideShowStyle('simpleMembershipUpdateGroupDetails', true)}">
  <tbody>
    <tr class="formTableRow ">
      <td class="formTableLeft"><grouperGui:message key="groups.summary.extension" /></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.extension)}</td>

    </tr>
    <tr class="formTableRow ">
      <td class="formTableLeft"><grouperGui:message key="groups.summary.name" /></td>
      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.name)}</td>
    </tr>
    <tr class="formTableRow ">
      <td class="formTableLeft"><grouperGui:message key="groups.summary.id" /></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.uuid)}</td>
    </tr>

  </tbody>
</table>
<div style="margin-bottom: 8px;"><a class="smallLink buttons_simpleMembershipUpdateGroupDetails"
  onclick="return guiHideShow(event, 'simpleMembershipUpdateGroupDetails');" href="#"
>${grouperGui:hideShowButtonText('simpleMembershipUpdateGroupDetails') }</a>

<a id="advancedLink" href="#" class="smallLink" onclick="this.oncontextmenu(event); return false">Advanced</a>

<span id="advancedMenu" ></span>
<script type="text/javascript">
guiInitDhtmlxMenu("advancedMenu", "SimpleMembershipUpdate.advancedMenu", 
    "SimpleMembershipUpdate.advancedMenuStructure", true, "#advancedLink");
</script>

</div>

</div>
</div>

<div class="section">

  <grouperGui:subtitle key="simpleMembershipUpdate.addMemberSubtitle" />

  <div class="sectionBody">
    <img src="../public/assets/images/spacer.gif" alt="" width="900" height="1" />
    <form id="simpleMembershipUpdateAddMemberForm" name="simpleMembershipUpdateAddMemberForm" action="whatever">
    <table width="900">
      <tr valign="top">
        <td>
          
          <grouperGui:combobox filterOperation="SimpleMembershipUpdate.filterUsers" id="simpleMembershipUpdateAddMember" 
            width="700"/>
          
        </td>
        <td>
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleMembershipUpdate.addMember', {formIds: 'simpleMembershipUpdateAddMemberForm'}); return false;" 
          value="${grouperGui:message('simpleMembershipUpdate.addMemberButton', true, false) }" style="margin-top: 2px" />
        </td>
      </tr>
    </table>
    </form>
    <br />
  </div>
</div>

<div id="simpleMembershipResultsList"></div>

</div>

