<%@ include file="../common/commonTaglib.jsp"%>
<!-- simpleMembershipUpdate/simpleMembershipUpdateIndex.jsp: main page -->



<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleMembershipUpdate.indexSectionHeader" />

  <div class="sectionBody">
    <form id="simpleMembershipUpdatePickGroupForm" name="simpleMembershipUpdatePickGroupForm" onsubmit="return false;" >
    <div class="combohint"><grouper:message key="simpleMembershipUpdate.selectGroupCombohint"/></div>
    <table width="900" cellpadding="0" cellspacing="0">
      <tr valign="top">
        <td style="padding: 0px" width="710">
          <grouper:combobox filterOperation="SimpleMembershipUpdateFilter.filterGroups" id="simpleMembershipUpdatePickGroup" 
            width="700"/>
          
        </td>
        <td>
          <input class="blueButton" type="submit" 
          onclick="location.href = 'grouper.html?operation=SimpleMembershipUpdate.init&groupId=' + guiFieldValue($('input[name=simpleMembershipUpdatePickGroup]')[0]); return false;" 
          value="${grouper:message('simpleMembershipUpdate.selectGroupButton', true, false) }" style="margin-top: 2px" />
        </td>
      </tr>
    </table>
    </form>
    <br />
  </div>
</div>

<!-- end: simpleMembershipUpdate/simpleMembershipUpdateIndex.jsp: main page -->
