<%@ include file="../common/commonTaglib.jsp"%>
<!-- simpleMembershipUpdate/simpleMembershipUpdateIndex.jsp: main page -->



<div class="section" style="min-width: 750px">

  <grouper:subtitle key="simpleMembershipUpdate.indexSectionHeader" />

  <div class="sectionBody">
    <form id="simpleMembershipUpdatePickGroupForm" name="simpleMembershipUpdatePickGroupForm" onsubmit="return false;" >
    <div class="combohint"><grouper:message key="simpleMembershipUpdate.selectGroupCombohint"/></div>
      <grouper:combobox filterOperation="SimpleMembershipUpdateFilter.filterGroups" id="simpleMembershipUpdatePickGroup" 
        width="700"/>
          
       <div style="margin-top: 5px;">
          <input class="blueButton" type="submit" 
          onclick="location.href = 'grouper.html?operation=SimpleMembershipUpdate.init&groupId=' + guiFieldValue($('input[name=simpleMembershipUpdatePickGroup]')[0]); return false;" 
          value="${grouper:message('simpleMembershipUpdate.selectGroupButton', true, false) }" style="margin-top: 2px" />
        </div>
    </form>
    <br />
  </div>
</div>

<!-- end: simpleMembershipUpdate/simpleMembershipUpdateIndex.jsp: main page -->
