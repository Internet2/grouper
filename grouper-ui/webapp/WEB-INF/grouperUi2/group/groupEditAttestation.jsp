<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<form id="editGroupAttestationForm" class="form-horizontal">
    
	<div style="margin-bottom: 20px;">
	  <button class="btn" onclick="return guiV2link('operation=UiV2Group.updateGroupAttestationLastCertifiedDate&groupId=${grouperRequestContainer.groupContainer.guiAttestation.attributeAssignable.id}'); return false;">${textContainer.text['updateAttestationDateCertifiedGroupButton'] }</button>
	</div>
	<div class="control-group">
	  <label for="attestationDirectAssignment" class="control-label">${textContainer.text['attestationDirectAssignmentLabel'] }</label>
	  <div class="controls">
	  	<input type="checkbox" id ="attestationDirectAssignment" name="grouperAttestationDirectAssignment" value="true" 
               ${grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationDirectAssignment ? 'checked="checked"' : '' } />
	    <span class="help-block">${textContainer.text['attestationDirectAssignmentDescription'] }</span>
	  </div>
	</div>
	<div class="control-group">
	  <label for="sendEmail" class="control-label">${textContainer.text['attestationSendEmailLabel'] }</label>
	  <div class="controls">
	  	<input type="checkbox" name="grouperAttestationSendEmail" value="true" 
               ${grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationSendEmail ? 'checked="checked"' : '' } />
	    <span class="help-block">${textContainer.text['attestationSendEmailDescription'] }</span>
	  </div>
	</div>
	<div class="control-group">
     	<label for="grouperAttestationEmailAddresses" class="control-label">${textContainer.text['attestationEmailAddressesLabel'] }</label>
	     <div class="controls">
	       <input type="text" id="grouperAttestationEmailAddresses" name="grouperAttestationEmailAddresses"
	       value="${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationEmailAddresses)}"
	        /><span class="help-block">${textContainer.text['attestationEmailAddressesDescription'] }</span>
	     </div>
   </div>
   <div class="control-group">
     <label for="grouperAttestationDaysUntilRecertify" class="control-label">${textContainer.text['attestationDaysUntilRecertifyLabel'] }</label>
     <div class="controls">
       <input type="text" id="grouperAttestationDaysUntilRecertify" name="grouperAttestationDaysUntilRecertify"
       value="${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationDaysUntilRecertify)}"
        /><span class="help-block">${textContainer.text['attestationDaysUntilRecertifyDescription'] }</span>
     </div>
   </div>
   <div class="control-group">
     <label for="grouperAttestationLastEmailedDate" class="control-label">${textContainer.text['attestationLastEmailedDateLabel'] }</label>
     <div class="controls">
       ${grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationLastEmailedDate}
     </div>
   </div>
   <div class="control-group">
     <label for="grouperAttestationDaysBeforeToRemind" class="control-label">${textContainer.text['attestationDaysBeforeToRemindLabel'] }</label>
     <div class="controls">
       <input type="text" id="grouperAttestationDaysBeforeToRemind" name="grouperAttestationDaysBeforeToRemind"
       value="${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationDaysBeforeToRemind)}"
        /><span class="help-block">${textContainer.text['attestationDaysBeforeToRemindDescription'] }</span>
     </div>
   </div>
   <c:if test="${grouperRequestContainer.groupContainer.guiAttestation.mode == 'EDIT'}">
     <div class="control-group">
       <label class="control-label">${textContainer.text['attestationUpdateLastCertifiedLabel'] }</label>
       <div class="controls">
     	 <input type="checkbox" name="attestationUpdateLastCertified" value="true" />
	     <span class="help-block">${textContainer.text['attestationUpdateLastCertifiedDescription'] }</span>
       </div>
     </div>
   </c:if>   	
   <div class="control-group">
     <label class="control-label">${textContainer.text['attestationDateCertifiedLabel'] }</label>
     <div class="controls">
     	${grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationDateCertified}
     </div>
   </div>
   <div class="form-actions">
     <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Group.editAttestationSubmit', {formIds: 'editGroupAttestationForm'}); return false;">${textContainer.text['groupCreateSaveButton'] }</a> 
   </div>
   </form>