<%@ include file="../common/commonTaglib.jsp"%>

<div class="simpleMembershipDownload">
  <grouper:message value="${simpleMembershipUpdateContainer.text.downloadAllLabel}" /> <br /><br />
  <a href="../app/SimpleMembershipUpdateImportExport.exportAllCsv/${simpleMembershipUpdateContainer.guiGroup.exportAllFileName}"
    >${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.displayName)}</a>
</div>
