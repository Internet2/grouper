<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/simpleAttributeAssignInit.jsp: main page -->

<%--Attribute assignment --%>
<grouper:title label="${attributeUpdateSessionContainer.text.assignIndexTitle}"  
  infodotValue="${attributeUpdateSessionContainer.text.assignIndexTitleInfodot}" />

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleAttributeUpdate.indexSectionHeader" />

  <div class="sectionBody">
    <form id="simpleAttributeFilterForm" name="simpleAttributeFilterFormName" onsubmit="return false;" >
    <table width="900" cellpadding="0" cellspacing="0">
      <tr valign="top">
        <td style="padding: 0px" width="710">
          Owner type:
          <select name="ownerType">
            <option value="group">group</option>
          </select>
          
        </td>
      </tr>
      <tr>
        <td>
          <input class="blueButton" type="submit" 
          onclick="location.href = 'grouper.html?operation=SimpleAttributeUpdate.assignInit&groupId=' + guiFieldValue($('input[name=ownerType]')[0]); return false;" 
          value="${grouper:message('simpleAttributeUpdate.selectOwnerTypeButton', true, false) }" style="margin-top: 2px" />
        </td>
      </tr>
    </table>
    </form>
    <br />
  </div>
</div>


<!-- End: simpleAttributeUpdate/simpleAttributeAssignInit.jsp: main page -->
