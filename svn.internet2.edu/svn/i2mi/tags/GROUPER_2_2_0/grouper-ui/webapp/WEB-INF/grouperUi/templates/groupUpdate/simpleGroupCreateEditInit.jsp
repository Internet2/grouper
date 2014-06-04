<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleGroupUpdate/simpleGroupCreateEdit.jsp: main page -->

<grouper:title label="${grouper:message('simpleGroupUpdate.createEditIndexTitle', true, false)}"  
  infodotValue="${grouper:message('simpleGroupUpdate.createEditIndexTitleInfodot', true, false)}" />

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleGroupUpdate.indexSectionHeader" />
  <div class="sectionBody">
    <form id="simpleGroupUpdatePickGroupFormId" name="simpleGroupUpdatePickGroupFormName" onsubmit="return false;" >
      <div class="combohint"><grouper:message key="simpleGroupUpdate.selectGroupCombohint"/></div>
  
      <grouper:combobox filterOperation="SimpleGroupUpdateFilter.filterGroupsRolesEntities" id="simpleGroupUpdatePickGroup" 
        comboDefaultText="${groupUpdateRequestContainer.groupToEdit.name}" comboDefaultValue="${groupUpdateRequestContainer.groupToEdit.uuid}"
        width="700"/>
      
      <div style="margin-top: 5px;">
        <input class="blueButton" type="submit" 
        onclick="ajax('../app/SimpleGroupUpdateFilter.editGroupButton', {formIds: 'simpleGroupUpdatePickGroupFormId'}); return false;" 
        value="${grouper:message('simpleGroupUpdate.filterGroupButton', true, false)}" style="margin-top: 2px" />
    
        <input class="blueButton" type="submit" 
        onclick="ajax('../app/SimpleGroupUpdateFilter.newGroupButton'); return false;" 
        value="${grouper:message('simpleGroupUpdate.newGroupButton', true, false)}" style="margin-top: 2px" />
      </div>
    </form>
    <br />
  </div>
</div>

<div id="groupEditPanel">
</div>

<!-- End: groupUpdate/simpleGroupCreateEdit.jsp -->
