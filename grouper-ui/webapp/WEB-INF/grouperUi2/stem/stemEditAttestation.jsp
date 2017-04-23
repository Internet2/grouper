<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<form id="editStemAttestationForm" class="form-horizontal">
    
	<div style="margin-bottom: 20px;">
	  <button class="btn" onclick="return guiV2link('operation=UiV2Attestation.updateAllGroupAttestationLastCertifiedDate&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;">${textContainer.text['updateAttestationCertifyAllGroupsUnderThisFolderButton'] }</button>
       <button class="btn" onclick="return guiV2link('operation=UiV2Attestation.updateUncertifiedGroupAttestationLastCertifiedDate&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;">${textContainer.text['updateAttestationCertifyUncertifiedGroupsUnderThisFolderButton'] }</button>
	</div>
	
	<div class="control-group">
	  <label for="sendEmail" class="control-label">${textContainer.text['attestationSendEmailLabel'] }</label>
	  <div class="controls">
	  	<input type="checkbox" name="grouperAttestationSendEmail" value="true" 
               ${grouperRequestContainer.stemContainer.guiAttestation.grouperAttestationSendEmail ? 'checked="checked"' : '' } />
	    <span class="help-block">${textContainer.text['attestationSendEmailDescription'] }</span>
	  </div>
	</div>
	<div class="control-group">
     	<label for="grouperAttestationEmailAddresses" class="control-label">${textContainer.text['attestationEmailAddressesLabel'] }</label>
	     <div class="controls">
	       <input type="text" id="grouperAttestationEmailAddresses" name="grouperAttestationEmailAddresses"
	       value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiAttestation.grouperAttestationEmailAddresses)}"
	        /><span class="help-block">${textContainer.text['attestationEmailAddressesDescription'] }</span>
	     </div>
   </div>
   <div class="control-group">
     <label for="grouperAttestationDaysUntilRecertify" class="control-label">${textContainer.text['attestationDaysUntilRecertifyLabel'] }</label>
     <div class="controls">
       <input type="text" id="grouperAttestationDaysUntilRecertify" name="grouperAttestationDaysUntilRecertify"
       value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiAttestation.grouperAttestationDaysUntilRecertify)}"
        /><span class="help-block">${textContainer.text['attestationDaysUntilRecertifyDescription'] }</span>
     </div>
   </div>
   <div class="control-group">
     <label for="grouperAttestationDaysBeforeToRemind" class="control-label">${textContainer.text['attestationDaysBeforeToRemindLabel'] }</label>
     <div class="controls">
       <input type="text" id="grouperAttestationDaysBeforeToRemind" name="grouperAttestationDaysBeforeToRemind"
       value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiAttestation.grouperAttestationDaysBeforeToRemind)}"
        /><span class="help-block">${textContainer.text['attestationDaysBeforeToRemindDescription'] }</span>
     </div>
   </div>
   
   <c:if test="${grouperRequestContainer.stemContainer.guiAttestation.mode == 'EDIT'}">
     <div class="control-group">
       <label class="control-label">${textContainer.text['attestationUpdateLastCertifiedLabel'] }</label>
       <div class="controls">
     	 <input type="checkbox" name="attestationUpdateLastCertified" value="true" />
	     <span class="help-block">${textContainer.text['attestationUpdateLastCertifiedFolderDescription'] }</span>
       </div>
     </div>
   </c:if>
   
   <div class="control-group">
     <label for="levels" class="control-label">${textContainer.text['attestationStemScopeLabel'] }</label>
     <div class="controls">
       <label class="radio" for="level-all">
         <input type="radio" name="levelsName" id="level-all" value="sub"
         ${grouperRequestContainer.stemContainer.guiAttestation.grouperAttestationStemScope == 'sub' ? 'checked="checked"' : '' }
         >${textContainer.text['stemPrivilegesInheritAllLabel'] }
       </label>
       <label class="radio" for="level-one">
         <input type="radio" name="levelsName" id="level-one" value="one"
         ${grouperRequestContainer.stemContainer.guiAttestation.grouperAttestationStemScope == 'one' ? 'checked="checked"' : '' }
         >${textContainer.text['stemPrivilegesInheritOneLabel'] }
       </label><%-- <span class="help-block">${textContainer.text['attestationStemScopeDescription'] }</span> --%>
     </div>
   </div>
  
   <div class="form-actions">
     <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Attestation.editStemAttestationSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'editStemAttestationForm'}); return false;">${textContainer.text['groupCreateSaveButton'] }</a> 
   </div>
   </form>