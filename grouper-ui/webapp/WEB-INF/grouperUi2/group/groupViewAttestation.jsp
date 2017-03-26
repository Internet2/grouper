<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div id="groupDetailsId">
   	 <div class="span8" style="margin-bottom: 10px;">
       <button class="btn" onclick="return guiV2link('operation=UiV2Group.updateGroupAttestationLastCertifiedDate&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;">${textContainer.text['updateAttestationDateCertifiedGroupButton'] }</button>
   	 </div>
   	 <div class="span4" style="margin-bottom: 10px;">
       <c:if test="${ (isWheelGroupMember || grouperRequestContainer.groupContainer.canAdmin) && grouperRequestContainer.groupContainer.guiAttestation.type == 'DIRECT'}">
         <a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Group.editAttestation&groupId=${grouperRequestContainer.groupContainer.guiAttestation.attributeAssignable.id}'); return false;" class="btn btn-medium btn-block btn-primary" role="button">${textContainer.text['editAttestationGroupButton'] }</a>
       </c:if>
       <c:if test="${ (isWheelGroupMember || grouperRequestContainer.groupContainer.canAdmin) && grouperRequestContainer.groupContainer.guiAttestation.type == 'INDIRECT'}">
       	 <a href="javascript:void(0)"
           onclick="return guiV2link('operation=UiV2Group.addAttestation&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
           class="btn btn-medium btn-block btn-primary" role="button">${textContainer.text['configureAttestationGroupButton'] }
     	 </a>
       </c:if> 
   	 </div>
   <table class="table table-condensed table-striped">
     <tbody>
     
       <c:if test="${grouperRequestContainer.groupContainer.guiAttestation.type == 'INDIRECT'}">
         <tr>
           <td><strong>${textContainer.text['attestationParentFolderLabel']}</strong></td>
           <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiAttestation.attributeAssignable.displayName)}</td>
         </tr>
       </c:if>
       <tr>
         <td><strong>${textContainer.text['attestationSendEmailLabel']}</strong></td>
         <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationSendEmail)}</td>
       </tr>
       <tr>
         <td><strong>${textContainer.text['attestationEmailAddressesLabel']}</strong></td>
         <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationEmailAddresses)}</td>
       </tr>
       <tr>
         <td><strong>${textContainer.text['attestationDaysUntilRecertifyLabel']}</strong></td>
         <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationDaysUntilRecertify)}</td>
       </tr>
       <c:if test="${grouperRequestContainer.groupContainer.guiAttestation.type == 'DIRECT'}">
	     <tr>
	       <td><strong>${textContainer.text['attestationLastEmailedDateLabel']}</strong></td>
	       <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationLastEmailedDate)}</td>
	     </tr>
       </c:if>
       <tr>
         <td><strong>${textContainer.text['attestationDaysBeforeToRemindLabel']}</strong></td>
         <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationDaysBeforeToRemind)}</td>
       </tr>
       <c:if test="${grouperRequestContainer.groupContainer.guiAttestation.type == 'DIRECT'}">
	     <tr>
	       <td><strong>${textContainer.text['attestationDateCertifiedLabel'] }</strong></td>
	       <td>${grouperRequestContainer.groupContainer.guiAttestation.grouperAttestationDateCertified}</td>
	     </tr>
       </c:if>
     </tbody>
   </table>
 </div>