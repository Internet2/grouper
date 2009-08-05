<%@ include file="../common/commonTaglib.jsp"%>
<!-- simpleMembershipUpdate/simpleMembershipUpdateMain.html: main page on simpleMembershipUpdate screen -->
<div id="simpleMain">
<h1>${guiSettings.text.simpleMembershipUpdateTitle} <a class="infodotLink"
  onclick="return guiToggle(event, '#simpleUpdateTopHelp');" href="#"
> <img src="../public/assets/infodot.gif" border="0" alt="Help main" /> </a></h1>
<div id="simpleUpdateTopHelp" class="helpText" style="display: none;">
${guiSettings.text.simpleMembershipUpdateMainHelp}</div>
<div class="section">
<div class="sectionHeader">Group</div>
<div class="sectionBody">
<table border="0" cellpadding="0" cellspacing="0">
  <tr valign="top">
    <td>
<grouperGui:groupBreadcrumb
  groupName="${simpleMembershipUpdateContainer.group.displayName}"
/></td><td> &nbsp; &nbsp; &nbsp; <a class="smallLink" href="#operation=SimpleMembershipUpdate.index">Change location</a>
</td>
</tr>
</table>
<table class="formTable" cellspacing="2" style="margin-bottom: 0;">
  <tbody>
    <tr class="formTableRow">
      <td class="formTableLeft"><span class="tooltip"
        onmouseover="grouperTooltip('${guiSettings.text.tooltipGroupDisplayExtension}');"
        onmouseout="UnTip();"
      >${guiSettings.text.labelGroup} ${guiSettings.text.labelGroupDisplayExtension}</span></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.group.displayExtension)}</td>
    </tr>
    <tr class="formTableRow">
      <td class="formTableLeft"><span class="tooltip" onmouseout="UnTip();"
        onmouseover="grouperTooltip('${guiSettings.text.tooltipGroupDisplayName}');"
      >${guiSettings.text.labelGroupDisplayName}</span></td>
      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.group.displayName)}</td>
    </tr>

    <tr class="formTableRow">

      <td class="formTableLeft"><span class="tooltip" onmouseout="UnTip();"
        onmouseover="grouperTooltip(
              '${guiSettings.text.tooltipGroupDescription}');"
      >${guiSettings.text.labelGroupDescription}</span></td>
      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.group.description)}</td>
    </tr>
  </tbody>
</table>

<table class="formTable groupHidden" cellspacing="2" style="margin: 0;">
  <tbody>
    <tr class="formTableRow ">
      <td class="formTableLeft"><span class="tooltip" onmouseout="UnTip();"
        onmouseover="grouperTooltip(
              '${guiSettings.text.tooltipGroupExtension}');"
      >${guiSettings.text.labelGroupExtension}</span></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.group.extension)}</td>

    </tr>
    <tr class="formTableRow ">
      <td class="formTableLeft"><span class="tooltip" onmouseout="UnTip();"
        onmouseover="grouperTooltip(
              '${guiSettings.text.tooltipGroupName}');"
      >${guiSettings.text.labelGroupName}</span></td>
      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.group.name)}</td>
    </tr>
    <tr class="formTableRow ">
      <td class="formTableLeft"><span class="tooltip" onmouseout="UnTip();"
        onmouseover="grouperTooltip(
              '${guiSettings.text.tooltipGroupId}');"
      >${guiSettings.text.labelGroupId}</span></td>

      <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.group.uuid)}</td>
    </tr>

  </tbody>
</table>
<div style="margin-bottom: 8px;"><a class="smallLink"
  onclick="return guiToggle(event, '.groupHidden');" href="#"
>Group details</a></div>

</div>
</div>

<div class="section">
  <div class="sectionHeader">Add member</div>
  <div class="sectionBody">

    <table>
      <tr valign="top">
        <td>
          <form id="simpleMembershipUpdateAddMemberForm" name="simpleMembershipUpdateAddMemberForm">
          
          <%-- note, this div will be the combobox --%>
          <div id="simpleMembershipUpdateAddMember" style="width:400px;"></div>

          <script> 
                    guiRegisterDhtmlxCombo('simpleMembershipUpdateAddMember', 400, 
                        true, "../app/SimpleMembershipUpdate.filterUsers" );    
                    
          </script> 
          </form>
        </td>
        <td>
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleMembershipUpdate.addMember', {formIds: 'simpleMembershipUpdateAddMemberForm'}); return false;" 
          value="Add member" style="margin-top: 2px" />
        </td>
      </tr>
    </table>
    <%-- input class="blueButton" type="submit" onclick="return confirm('You are about to remove some members of this group. You cannot undo this operation. Are you sure?')" value="Remove selected members" name="submit.remove.selected"/ --%>
    <%-- input class="blueButton" type="submit" onclick="return confirm('You are about to remove all members of this group. You cannot undo this operation. Are you sure?')" value="Remove all members" name="submit.remove.all"/ --%>

    <br />
  </div>
</div>

<div id="simpleMembershipResultsList"></div>

</div>

