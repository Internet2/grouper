<%@ include file="../common/commonTaglib.jsp"%>

<div class="simpleMembershipDownload">
  <grouperGui:message key="simpleMembershipUpdate.downloadAllLabel" /> <br /><br />
  <a href="../app/SimpleMembershipUpdate.exportAllCsv/${simpleMembershipUpdateContainer.guiGroup.exportAllFileName}"
    >${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.displayName)}</a>
</div>
