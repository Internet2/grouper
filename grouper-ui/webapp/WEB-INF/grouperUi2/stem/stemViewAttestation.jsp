<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div id="stemDetailsId">
   	 <div class="span8" style="margin-bottom: 10px;">
       <button class="btn" onclick="return guiV2link('operation=UiV2Stem.updateAllGroupAttestationLastCertifiedDate&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;">${textContainer.text['updateAttestationCertifyAllGroupsUnderThisFolderButton'] }</button>
       <button class="btn" onclick="return guiV2link('operation=UiV2Stem.updateUncertifiedGroupAttestationLastCertifiedDate&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;">${textContainer.text['updateAttestationCertifyUncertifiedGroupsUnderThisFolderButton'] }</button>
   	 </div>
   	 <div class="span4" style="margin-bottom: 10px;">
       <c:if test="${ (isWheelGroupMember || grouperRequestContainer.stemContainer.canAdminPrivileges) && grouperRequestContainer.stemContainer.guiAttestation.type == 'DIRECT'}">
         <a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Stem.editAttestation&stemId=${grouperRequestContainer.stemContainer.guiAttestation.attributeAssignable.id}'); return false;" class="btn btn-medium btn-block btn-primary" role="button">${textContainer.text['editAttestationGroupButton'] }</a>
       </c:if>
       <c:if test="${ (isWheelGroupMember || grouperRequestContainer.stemContainer.canAdminPrivileges) && grouperRequestContainer.stemContainer.guiAttestation.type == 'INDIRECT'}">
       	 <a href="javascript:void(0)"
           onclick="return guiV2link('operation=UiV2Stem.addAttestation&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
           class="btn btn-medium btn-block btn-primary" role="button">${textContainer.text['configureAttestationFolderButton'] }
     	 </a>
       </c:if> 
   	 </div>
   <table class="table table-condensed table-striped">
     <tbody>
     
       <c:if test="${grouperRequestContainer.stemContainer.guiAttestation.type == 'INDIRECT'}">
         <tr>
           <td><strong>${textContainer.text['attestationParentFolderLabel']}</strong></td>
           <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiAttestation.attributeAssignable.displayName)}</td>
         </tr>
       </c:if>
       <tr>
         <td><strong>${textContainer.text['attestationSendEmailLabel']}</strong></td>
         <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiAttestation.grouperAttestationSendEmail)}</td>
       </tr>
       <tr>
         <td><strong>${textContainer.text['attestationEmailAddressesLabel']}</strong></td>
         <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiAttestation.grouperAttestationEmailAddresses)}</td>
       </tr>
       <tr>
         <td><strong>${textContainer.text['attestationDaysUntilRecertifyLabel']}</strong></td>
         <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiAttestation.grouperAttestationDaysUntilRecertify)}</td>
       </tr>
       <tr>
         <td><strong>${textContainer.text['attestationDaysBeforeToRemindLabel']}</strong></td>
         <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiAttestation.grouperAttestationDaysBeforeToRemind)}</td>
       </tr>
       <tr>
         <td><strong>${textContainer.text['attestationStemScopeLabel']}</strong></td>
         <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiAttestation.grouperAttestationStemScope)}</td>
       </tr>
     </tbody>
   </table>
 </div>