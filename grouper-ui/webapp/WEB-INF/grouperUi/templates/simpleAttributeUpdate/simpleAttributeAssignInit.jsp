<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/simpleAttributeAssignInit.jsp: main page -->

<%--Attribute assignment --%>
<grouper:title label="${grouper:message('simpleAttributeAssign.assignIndexTitle', true, false)}"  
  infodotValue="${grouper:message('simpleAttributeAssign.assignIndexTitleInfodot', true, false)}" />

<div class="section">

  <grouper:subtitle key="simpleAttributeAssign.indexSectionHeader" />

  <div class="sectionBody">
    <form id="simpleAttributeFilterForm" name="simpleAttributeFilterFormName" onsubmit="return false;" >
    <table class="formTable formTableSpaced" cellspacing="2" style="margin-bottom: 0px">
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="ownerType">
            <grouper:message key="simpleAttributeAssign.ownerType" />
          </label>
          <sup class="requiredIndicator">*</sup>
        </td>
        <td class="formTableRight">
          <select name="attributeAssignType" onchange="ajax('SimpleAttributeUpdate.assignSelectOwnerType', {formIds: 'simpleAttributeFilterForm'}); return false;" >
            <option></option>
            <option value="group">${grouper:message('simpleAttributeAssign.ownerTypeGroup', true, false)}</option>
            <option value="stem">${grouper:message('simpleAttributeAssign.ownerTypeFolder', true, false)}</option>
            <option value="member">${grouper:message('simpleAttributeAssign.ownerTypeMember', true, false)}</option>
            <option value="any_mem">${grouper:message('simpleAttributeAssign.ownerTypeMembership', true, false)}</option>
            <option value="imm_mem">${grouper:message('simpleAttributeAssign.ownerTypeImmediateMembership', true, false)}</option>
            <option value="attr_def">${grouper:message('simpleAttributeAssign.ownerTypeAttributeDefinition', true, false)}</option>
          </select>
        </td>
      </tr>
    </table>
    <div id="attributeAssignFilter">
    </div>
    </form>
  </div>
</div>

<div id="attributeAssignAssignments">
</div>

<!-- End: simpleAttributeUpdate/simpleAttributeAssignInit.jsp: main page -->
