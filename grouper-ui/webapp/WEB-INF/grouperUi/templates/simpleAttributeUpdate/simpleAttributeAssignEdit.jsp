<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeAssignEdit.jsp -->
<div class="simpleAttributeAssignEdit">

  <div class="section">
  
    <grouper:subtitle key="simpleAttributeAssign.assignEditSubtitle" 
      infodotValue="${grouper:message('simpleAttributeAssign.assignEditSubtitleInfodot', true, false)}"/>
  
    <div class="sectionBody">
    <form id="simpleAttributeAssignEditForm" name="simpleAttributeAssignEditFormName">
      <input name="attributeAssignId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.id }" />
      <table class="formTable formTableSpaced">
      
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="${attributeUpdateRequestContainer.attributeAssignTypeLabelKey}" />
            </td>
            <td class="formTableRight">
              <grouper:message 
                 value="${grouper:escapeHtml(attributeUpdateRequestContainer.guiAttributeAssign.screenLabelLong)}"  />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeUpdate.assignHeaderAttributeName" />
            </td>
            <td class="formTableRight">
              <grouper:message value="${grouper:escapeHtml(attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeDefName.displayName)}" />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeUpdate.assignHeaderAttributeDef" />
            </td>
            <td class="formTableRight">
              <grouper:message value="${grouper:escapeJavascript(attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeDef.name)}" />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeAssign.assignEditId" />
            </td>
            <td class="formTableRight">
              ${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.id}
            </td>
          </tr>

          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeUpdate.assignEditEnabledDate" />
            </td>
            <td class="formTableRight"><input type="text" name="enabledDate"  id="enabledDateId"
            value="${attributeUpdateRequestContainer.guiAttributeAssign.enabledDate}" style="width: 8em" />
            <span class="simpleMembershipUpdateDisabled"
              ><grouper:message key="simpleAttributeUpdate.assignEditEnabledDisabledDateMask" /></span>
            </td>
          </tr>
          
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeUpdate.assignEditDisabledDate" />
            </td>
            <td class="formTableRight"><input type="text" name="disabledDate"  id="disabledDateId"
            value="${attributeUpdateRequestContainer.guiAttributeAssign.disabledDate}" style="width: 8em" />
            <span class="simpleMembershipUpdateDisabled"
              ><grouper:message key="simpleAttributeUpdate.assignEditEnabledDisabledDateMask" /></span>
            </td>
          </tr>
          
          <tr>
            <td colspan="2" align="right"  class="buttonRow">

              <button class="simplemodal-close blueButton"><grouper:message key="simpleAttributeAssign.assignEditCancelButton" /></button> 
              &nbsp;
              <%-- add member button --%>
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimpleAttributeUpdate.assignEditSubmit', {formIds: 'simpleAttributeAssignEditForm, simpleAttributeFilterForm'}); return false;" 
              value="${grouper:message('simpleAttributeAssign.assignEditSubmitButton', true, false)}" />
            </td>
          </tr>
          
      </table>
      </form>
    </div>
  </div>
</div>
<!-- End: simpleAttributeAssignEdit.jsp -->


