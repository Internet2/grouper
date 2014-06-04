<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: misc/index.jsp: main page -->

<grouper:title key="simpleMembershipUpdate.topIndexTitle" />

<div class="section">
  <grouper:subtitle key="simpleMembershipUpdate.topIndexSectionHeader" />
  <div class="sectionBody">
    <ul>
      <li><a href="../../populateAllGroups.do"><grouper:message key="simpleGroupUpdate.topIndexAdminUi" /></a></li>
      <li><a href="../../grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain"><grouper:message key="simpleGroupUpdate.topIndexNewUi" /></a></li>
      <li><a href="grouper.html?operation=SimpleGroupUpdate.index"
        ><grouper:message key="simpleGroupUpdate.topIndexGroupUpdate" /></a></li>
      <li><a href="grouper.html?operation=SimpleMembershipUpdate.index"
        ><grouper:message key="simpleMembershipUpdate.topIndexMembershipUpdate" /></a></li>
      <li><a href="grouper.html?operation=SimpleAttributeUpdate.index"
        ><grouper:message key="simpleAttributeUpdate.topIndex" /></a></li>
    </ul>
  </div>
</div>

<!-- End: misc/index.jsp: main page -->
