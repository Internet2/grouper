<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeAssignAddValue.jsp -->
<div class="simpleAttributeAssignAddValue">

  <div class="section">
  
    <grouper:subtitle key="simpleAttributeAssign.assignAddValueSubtitle" 
      infodotValue="${grouper:message('simpleAttributeAssign.assignAddValueSubtitleInfodot', true, false)}"/>
  
    <div class="sectionBody">
    <form id="simpleAttributeAssignAddValueForm" name="simpleAttributeAssignAddValueFormName">
      
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
              <grouper:message key="simpleAttributeUpdate.assignAddValue" />
            </td>
            <td class="formTableRight"><input type="text" name="valueToAdd"  id="valueToAddId" /></td>
          </tr>
                    
          <tr>
            <td colspan="2" align="right"  class="buttonRow">

              <button class="simplemodal-close blueButton"><grouper:message key="simpleAttributeAssign.assignAddValueCancelButton" /></button> 
              &nbsp;
              <%-- add member button --%>
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimpleAttributeUpdate.assignAddValueSubmit', {formIds: 'simpleAttributeAssignAddValueForm, simpleAttributeFilterForm'}); return false;" 
              value="${grouper:message('simpleAttributeAssign.assignAddValueSubmitButton', true, false)}" />
            </td>
          </tr>
          
      </table>
      </form>
    </div>
  </div>
</div>
<!-- End: simpleAttributeAssignAddValue.jsp -->


