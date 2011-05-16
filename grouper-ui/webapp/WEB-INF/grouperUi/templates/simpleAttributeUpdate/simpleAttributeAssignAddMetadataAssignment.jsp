<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeAssignAddMetadataAssignment.jsp -->
<div class="simpleAttributeAssignAddValue">

  <div class="section">
  
    <grouper:subtitle key="simpleAttributeAssign.assignMetadataAddSubtitle" 
      infodotValue="${grouper:message('simpleAttributeAssign.assignMetadataAddSubtitleInfodot', true, false)}"/>
  
    <div class="sectionBody">
    <form id="simpleAttributeMetadataAddForm" name="simpleAttributeMetadataAddFormName">
      <input name="attributeAssignId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.id }" />

      <table class="formTable formTableSpaced">
      
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="${attributeUpdateRequestContainer.attributeAssignTypeLabelKey}" />
              <grouper:message key="simpleAttributeUpdate.assignMetadataLabelSuffix" />
            </td>
            <td class="formTableRight">
              <grouper:message 
                 value="${grouper:escapeHtml(attributeUpdateRequestContainer.guiAttributeAssign.screenLabelLong)}"  />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeUpdate.assignHeaderAttributeName" />
              <grouper:message key="simpleAttributeUpdate.assignMetadataLabelSuffix" />
            </td>
            <td class="formTableRight">
              <grouper:message value="${grouper:escapeHtml(attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeDefName.displayName)}" />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeUpdate.assignHeaderAttributeDef" />
              <grouper:message key="simpleAttributeUpdate.assignMetadataLabelSuffix" />
            </td>
            <td class="formTableRight">
              <grouper:message value="${grouper:escapeJavascript(attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeDef.name)}" />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeAssign.assignEditId" />
              <grouper:message key="simpleAttributeUpdate.assignMetadataLabelSuffix" />
            </td>
            <td class="formTableRight">
              ${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.id}
            </td>
          </tr>

          <tr class="formTableRow">
            <td class="formTableLeft" style="vertical-align: middle">
              <label for="attributeName">
                <grouper:message key="simpleAttributeAssign.attributeName" />
              </label>
            </td>
            <td class="formTableRight">
               <grouper:combobox 
                 filterOperation="SimpleAttributeUpdateFilter.filterAttributeNamesByOwnerType?attributeAssignType=${attributeUpdateRequestContainer.attributeAssignType.assignmentOnAssignmentType}" 
                 id="attributeAssignAssignAttributeName" 
                 width="700"/>
            </td>
          </tr>
                    
          <tr>
            <td colspan="2" align="right"  class="buttonRow">

             <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimpleAttributeUpdate.assignFilter', {formIds: 'simpleAttributeFilterForm'}); return false;" 
              value="${grouper:message('simpleAttributeAssign.assignMetadataAddCancelButton', true, false) }" style="margin-top: 2px" />
              &nbsp;
              <%-- add member button --%>
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimpleAttributeUpdate.assignMetadataAddSubmit', {formIds: 'simpleAttributeMetadataAddForm, simpleAttributeFilterForm'}); return false;" 
              value="${grouper:message('simpleAttributeAssign.assignMetadataAddSubmitButton', true, false)}" />
            </td>
          </tr>
          
      </table>
      </form>
    </div>
  </div>
</div>
<!-- End: simpleAttributeAssignAddMetadataAssignment.jsp -->


