<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleGroupUpdate/simpleGroupCreateEdit.jsp: main page -->

<grouper:title label="${grouper:message('simpleGroupUpdate.createEditIndexTitle', true, false)}"  
  infodotValue="${grouper:message('simpleGroupUpdate.createEditIndexTitleInfodot', true, false)}" />

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleGroupUpdate.indexSectionHeader" />
  <div class="sectionBody">
    <form id="simpleGroupUpdatePickGroupFormId" name="simpleGroupUpdatePickGroupFormName" onsubmit="return false;" >
    <div class="combohint"><grouper:message key="simpleGroupUpdate.selectGroupCombohint"/></div>
    <table width="900" cellpadding="0" cellspacing="0">
      <tr valign="top">
        <td style="padding: 0px" width="710">
          <grouper:combobox filterOperation="SimpleGroupUpdateFilter.filterGroups" id="simpleGroupUpdatePickGroup" 
            comboDefaultText="${groupUpdateRequestContainer.groupToEdit.name}" comboDefaultValue="${groupUpdateRequestContainer.groupToEdit.uuid}"
            width="700"/>
          
        </td>
        <td>
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleGroupUpdateFilter.editGroupButton', {formIds: 'simpleGroupUpdatePickGroupFormId'}); return false;" 
          value="${grouper:message('simpleGroupUpdate.filterGroupButton', true, false)}" style="margin-top: 2px" />
        </td>
        <td>
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleGroupUpdateFilter.newGroupButton'); return false;" 
          value="${grouper:message('simpleGroupUpdate.newGroupButton', true, false)}" style="margin-top: 2px" />
        </td>
      </tr>
    </table>
    </form>
    <br />
  </div>
</div>

<div id="groupEditPanel">
</div>

<!-- End: groupUpdate/simpleGroupCreateEdit.jsp -->
