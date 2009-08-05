<%@ include file="../common/commonTaglib.jsp"%>
<!-- misc/index.jsp: main page -->
<div class="section">
  <div class="sectionHeader">Simple membership update - find group</div>
  <div class="sectionBody">
    <form id="simpleMembershipUpdatePickGroupForm" name="simpleMembershipUpdatePickGroupForm">
    
    <table>
      <tr valign="top">
        <td>
          <%-- note, this div will be the combobox --%>
          <div id="simpleMembershipUpdatePickGroup" style="width:400px;"></div>
      
          <script> 
                    guiRegisterDhtmlxCombo('simpleMembershipUpdatePickGroup', 400, 
                        true, "../app/SimpleMembershipUpdate.filterGroups" );    
                    
          </script> 
        </td>
        <td>
          <input class="blueButton" type="submit" 
          onclick="location.href = 'grouper.html#operation=SimpleMembershipUpdate.init&groupId=' + guiFieldValue(guiGetElementByName('simpleMembershipUpdatePickGroup')); return false;" 
          value="Select group" style="margin-top: 2px" />
        </td>
      </tr>
    </table>
    </form>

  </div>
</div>