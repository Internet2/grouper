<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div id="groupDetailsId">
	<strong>${textContainer.text['noAttestationConfigured']}</strong>
	<c:if test="${isWheelGroupMember || grouperRequestContainer.groupContainer.canAdmin}">
     <a href="javascript:void(0)" 
           onclick="return guiV2link('operation=UiV2Attestation.addGroupAttestation&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;">${textContainer.text['configureAttestationGroupButton'] }
     </a>
    </c:if>
</div>