<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeAssignValueEdit.jsp -->
<div class="simpleAttributeAssignEditValue">

  <div class="section">
  
    <grouper:subtitle key="simpleAttributeAssign.assignValueEditSubtitle" 
      infodotValue="${grouper:message('simpleAttributeAssign.assignValueEditSubtitleInfodot', true, false)}"/>
  
    <div class="sectionBody">
    <form id="simpleAttributeAssignValueEditForm" name="simpleAttributeAssignValueEditFormName">
      <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign != null}">
        <input name="attributeAssignId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id }" />
      </c:if>
      <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign == null}">
        <input name="attributeAssignId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.id }" />
      </c:if>
      <input name="attributeAssignValueId" type="hidden" value="${attributeUpdateRequestContainer.attributeAssignValue.id }" />
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
              <grouper:message key="simpleAttributeAssign.assignValueEditId" />
            </td>
            <td class="formTableRight">
              ${attributeUpdateRequestContainer.attributeAssignValue.id}
            </td>
          </tr>

          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeUpdate.assignValueEdit" />
            </td>
            <td class="formTableRight"><input type="text" name="valueToEdit"  id="valueToEditId" 
              value="${attributeUpdateRequestContainer.attributeAssignValue.valueFriendly}" /></td>
          </tr>

          <tr>
            <td colspan="2" align="right"  class="buttonRow">

              <button class="simplemodal-close blueButton"><grouper:message key="simpleAttributeAssign.assignValueEditCancelButton" /></button> 
              &nbsp;
              <%-- add member button --%>
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimpleAttributeUpdate.assignValueEditSubmit', {formIds: 'simpleAttributeAssignValueEditForm, simpleAttributeFilterForm'}); return false;" 
              value="${grouper:message('simpleAttributeAssign.assignValueEditSubmitButton', true, false)}" />
            </td>
          </tr>
          
      </table>
      </form>
    </div>
  </div>
</div>
<!-- End: simpleAttributeAssignValueEdit.jsp -->


