<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeAssignEdit.jsp -->
<div class="simpleAttributeAssignEdit">

  <div class="section">
  
    <grouper:subtitle key="simpleAttributeAssign.assignEditSubtitle" 
      infodotValue="${grouper:message('simpleAttributeAssign.assignEditSubtitleInfodot', true, false)}"/>
  
    <div class="sectionBody">
    <form id="simpleAttributeAssignEditForm" name="simpleAttributeAssignEditFormName">
      <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign != null}">
        <input name="attributeAssignId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id }" />
      </c:if>
      <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign == null}">
        <input name="attributeAssignId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.id }" />
      </c:if>
      <table class="formTable formTableSpaced">
      
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="${attributeUpdateRequestContainer.attributeAssignTypeLabelKey}" />
              <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign != null}">
                <grouper:message key="simpleAttributeUpdate.assignMetadataLabelSuffix" />
              </c:if>
              
            </td>
            <td class="formTableRight">
              <grouper:message 
                 value="${grouper:escapeHtml(attributeUpdateRequestContainer.guiAttributeAssign.screenLabelLong)}"  />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeUpdate.assignHeaderAttributeName" />
              <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign != null}">
                <grouper:message key="simpleAttributeUpdate.assignMetadataLabelSuffix" />
              </c:if>
            </td>
            <td class="formTableRight">
              <grouper:message value="${grouper:escapeHtml(attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeDefName.displayName)}" />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeUpdate.assignHeaderAttributeDef" />
              <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign != null}">
                <grouper:message key="simpleAttributeUpdate.assignMetadataLabelSuffix" />
              </c:if>
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
              ${attributeUpdateRequestContainer.guiAttributeAssignAssign == null ? attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.id : attributeUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id}
            </td>
          </tr>

          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeUpdate.assignEditEnabledDate" />
            </td>
            <td class="formTableRight"><input type="text" name="enabledDate"  id="enabledDateId"
            value="${attributeUpdateRequestContainer.guiAttributeAssignAssign == null ? attributeUpdateRequestContainer.guiAttributeAssign.enabledDate : attributeUpdateRequestContainer.guiAttributeAssignAssign.enabledDate}" 
            style="width: 8em" />
            <span class="simpleMembershipUpdateDisabled"
              ><grouper:message key="simpleAttributeUpdate.assignEditEnabledDisabledDateMask" /></span>
            </td>
          </tr>
          
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeUpdate.assignEditDisabledDate" />
            </td>
            <td class="formTableRight"><input type="text" name="disabledDate"  id="disabledDateId"
            value="${attributeUpdateRequestContainer.guiAttributeAssignAssign == null ? attributeUpdateRequestContainer.guiAttributeAssign.disabledDate : attributeUpdateRequestContainer.guiAttributeAssignAssign.disabledDate}" 
            style="width: 8em" />
            <span class="simpleMembershipUpdateDisabled"
              ><grouper:message key="simpleAttributeUpdate.assignEditEnabledDisabledDateMask" /></span>
            </td>
          </tr>
          
          <tr>
            <td colspan="2" align="right"  class="buttonRow">

              <button class="simplemodal-close blueButton"><grouper:message key="simpleAttributeAssign.assignEditCancelButton" /></button> 
              &nbsp;
              <%-- edit assignment button --%>
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


