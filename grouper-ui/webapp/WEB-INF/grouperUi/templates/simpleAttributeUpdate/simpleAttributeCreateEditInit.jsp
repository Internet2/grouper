<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/simpleAttributeCreateEdit.jsp: main page -->

<%--Attribute assignment --%>
<grouper:title label="${attributeUpdateRequestContainer.text.createEditIndexTitle}"  
  infodotValue="${attributeUpdateRequestContainer.text.createEditIndexTitleInfodot}" />

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleAttributeUpdate.indexSectionHeader" />

  <div class="sectionBody">
    <form id="simpleAttributeUpdatePickAttributeDefFormId" name="simpleAttributeUpdatePickAttributeDefFormName" onsubmit="return false;" >
    <div class="combohint"><grouper:message key="simpleAttributeUpdate.selectAttributeDefCombohint"/></div>
    <table width="900" cellpadding="0" cellspacing="0">
      <tr valign="top">
        <td style="padding: 0px" width="710">
          <grouper:combobox filterOperation="SimpleAttributeUpdateFilter.filterAttributeDefs" id="simpleAttributeUpdatePickAttributeDef" 
            width="700"/>
          
        </td>
        <td>
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleAttributeUpdateFilter.editAttributeDefsButton', {formIds: 'simpleAttributeUpdatePickAttributeDefFormId'}); return false;" 
          value="${attributeUpdateRequestContainer.text.filterAttributeDefButton}" style="margin-top: 2px" />
        </td>
        <td>
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleAttributeUpdateFilter.newAttributeDefButton'); return false;" 
          value="${attributeUpdateRequestContainer.text.newAttributeDefButton}" style="margin-top: 2px" />
        </td>
      </tr>
    </table>
    </form>
    <br />
  </div>
</div>

<div id="attributeEditPanel">
</div>

<!-- End: simpleAttributeUpdate/simpleAttributeCreateEdit.jsp -->
