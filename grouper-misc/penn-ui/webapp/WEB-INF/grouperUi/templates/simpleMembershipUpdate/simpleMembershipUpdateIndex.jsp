<%@ include file="../common/commonTaglib.jsp"%>
<!-- misc/index.jsp: main page -->



<div class="section" style="min-width: 900px">

  <grouperGui:subtitle key="simpleMembershipUpdate.indexSectionHeader" />

  <div class="sectionBody">
    <form id="simpleMembershipUpdatePickGroupForm" name="simpleMembershipUpdatePickGroupForm">
    <div class="combohint"><grouperGui:message key="simpleMembershipUpdate.selectGroupCombohint"/></div>
    <table width="900" cellpadding="0" cellspacing="0">
      <tr valign="top">
        <td style="padding: 0px" width="710">
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
    <br />
  </div>
</div>

