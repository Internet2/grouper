<%@ include file="../common/commonTaglib.jsp"%>
<!-- misc/index.jsp: main page -->


<div class="section">
  
  <grouperGui:subtitle key="simpleMembershipUpdate.indexSectionHeader" />

  <div class="sectionBody">
    <form id="simpleMembershipUpdatePickGroupForm" name="simpleMembershipUpdatePickGroupForm">
    
    <table>
      <tr valign="top">
        <td>
          <grouperGui:combobox filterOperation="SimpleMembershipUpdate.filterGroups" id="simpleMembershipUpdatePickGroup" 
            width="700"/>
        </td>
        <td>
          <input class="blueButton" type="submit" 
          onclick="location.href = 'grouper.html#operation=SimpleMembershipUpdate.init&groupId=' + guiFieldValue(guiGetElementByName('simpleMembershipUpdatePickGroup')); return false;" 
          value="${grouperGui:message('simpleMembershipUpdate.selectGroupButton', true, false) }" style="margin-top: 2px" />
        </td>
      </tr>
    </table>
    </form>

  </div>
</div>