<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div id="groupDetailsId">
	<strong>${textContainer.text['noAttestationConfigured']}</strong>
	<c:if test="${isWheelGroupMember || grouperRequestContainer.stemContainer.canAdminPrivileges}">
     <a href="javascript:void(0)" 
           onclick="return guiV2link('operation=UiV2Attestation.addStemAttestation&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;">${textContainer.text['configureAttestationStemButton'] }
     </a>
    </c:if>
</div>