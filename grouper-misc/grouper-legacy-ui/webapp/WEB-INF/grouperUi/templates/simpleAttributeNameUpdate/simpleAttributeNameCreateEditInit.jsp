<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeNameUpdate/simpleAttributeNameCreateEdit.jsp: main page -->

<%-- Attribute assignment --%>
<grouper:title label="${attributeNameUpdateRequestContainer.text.createEditIndexTitle}"  
  infodotValue="${attributeUpdateRequestContainer.text.createEditIndexTitleInfodot}" />

<div class="section" style="min-width: 950px">

  <grouper:subtitle key="simpleAttributeNameUpdate.indexSectionHeader" />

  <div class="sectionBody">
    <form id="simpleAttributeNameUpdatePickAttributeDefFormId" name="simpleAttributeNameUpdatePickAttributeDefFormName" onsubmit="return false;" >
    <table class="formTable formTableSpaced" cellspacing="2" width="750">
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle; white-space: nowrap;">
          <grouper:message key="simpleAttributeNameUpdate.attributeDef" />
        </td>
        <td class="formTableRight">
          <div class="combohint"><grouper:message key="simpleAttributeNameUpdate.selectAttributeDefCombohint"/></div>
          <table width="750" cellpadding="0" cellspacing="0">
            <tr valign="top">
              <td style="padding: 0px" width="710">
                <grouper:combobox filterOperation="SimpleAttributeNameUpdateFilter.filterAttributeDefs" id="simpleAttributeNameUpdatePickAttributeDef" 
                  comboDefaultText="${attributeNameUpdateRequestContainer.attributeDefForFilter.name}"  
                  comboDefaultValue="${attributeNameUpdateRequestContainer.attributeDefForFilter.id}"
                  width="700"/>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle; white-space: nowrap;">
          <grouper:message key="simpleAttributeNameUpdate.attributeDefName" />
        </td>
        <td class="formTableRight">
          <div class="combohint"><grouper:message key="simpleAttributeNameUpdate.selectAttributeDefNameCombohint"/></div>
          <grouper:combobox filterOperation="SimpleAttributeNameUpdateFilter.filterAttributeDefNames" id="simpleAttributeNameUpdatePickAttributeDefName" 
            comboDefaultText="${attributeNameUpdateRequestContainer.attributeDefNameToEdit.name}"  
            comboDefaultValue="${attributeNameUpdateRequestContainer.attributeDefNameToEdit.id}"
            width="700" additionalFormElementNames="simpleAttributeNameUpdatePickAttributeDef" />

           <div style="margin-top: 5px;">
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimpleAttributeNameUpdateFilter.editAttributeDefNamesButton', {formIds: 'simpleAttributeNameUpdatePickAttributeDefFormId'}); return false;" 
              value="${attributeNameUpdateRequestContainer.text.filterAttributeDefNameButton}" style="margin-top: 2px" />
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimpleAttributeNameUpdateFilter.newAttributeDefNameButton', {formIds: 'simpleAttributeNameUpdatePickAttributeDefFormId'}); return false;" 
              value="${attributeNameUpdateRequestContainer.text.newAttributeDefNameButton}" style="margin-top: 2px" />
           </div>
        </td>
      </tr>
    </table>
    </form>
    <br />
  </div>
</div>

<div id="attributeNameEditPanel">
</div>

<!-- End: simpleAttributeNameUpdate/simpleAttributeNameCreateEdit.jsp -->
