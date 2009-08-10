<%@ include file="../common/commonTaglib.jsp"%>

<div>
  Download the members:<br /><br />
  <a href="../app/SimpleMembershipUpdate.exportSubjectIdsCsv/${simpleMembershipUpdateContainer.guiGroup.exportSubjectIdsFileName}?groupId=${simpleMembershipUpdateContainer.guiGroup.group.uuid}"
    >${fn:escapeXml(simpleMembershipUpdateContainer.guiGroup.group.displayName)}</a>
</div>
