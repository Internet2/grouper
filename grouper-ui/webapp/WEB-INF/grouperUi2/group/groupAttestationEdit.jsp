<%@ include file="../assetsJsp/commonTaglib.jsp"%>


                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['groupAttestationEditTitle'] }</div>
                  <div class="span3" id="groupAttestationMoreActionsButtonContentsDivId">
                    <%@ include file="groupAttestationMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                

                <c:choose>
                  <c:when test="${grouperRequestContainer.attestationContainer.directGroupAttestationAssignment}">
                    <p>${textContainer.text['attestationConfiguredForGroup'] }</p>
                  </c:when>

                  <c:when test="${grouperRequestContainer.attestationContainer.ancestorStemAttestationAssignment}">
                    <p>${textContainer.text['attestationConfiguredOnGroupForAncestorStem'] }</p>
                  </c:when>
                  
                  <c:otherwise>
                    <p>${textContainer.text['noAttestationConfiguredOnGroup'] }</p>
                  </c:otherwise>
                                    
                </c:choose>


<form id="editGroupAttestationFormId" class="form-horizontal">
  <table class="table table-condensed table-striped">
    <tbody>

      <tr>
        <td style="vertical-align: top; white-space: nowrap;"><strong><label
            for="grouperAttestationHasAttestationId">${textContainer.text['attestationDirectAssignmentLabel']}</label></strong></td>
        <td><select name="grouperAttestationHasAttestationName"
          id="grouperAttestationHasAttestationId" style="width: 25em"
          onchange="ajax('../app/UiV2Attestation.editGroupAttestation', {formIds: 'editGroupAttestationFormId'}); return false;">
            <option value="false"
              ${grouperRequestContainer.attestationContainer.editAttestationIsAssigned ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['grouperAttestationNoDoesNotHaveAttestationLabel']}</option>
            <option value="true"
              ${grouperRequestContainer.attestationContainer.editAttestationIsAssigned ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['grouperAttestationYesHasAttestationLabel']}</option>
        </select> <br /> <span class="description">${textContainer.text['grouperAttestationHasAttestationDescription']}</span>
        </td>
      </tr>

      <c:if
        test="${grouperRequestContainer.attestationContainer.editAttestationShowSendEmail}">
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label
              for="grouperAttestationSendEmailId">${textContainer.text['attestationSendEmailLabel']}</label></strong></td>
          <td><select name="grouperAttestationSendEmailName"
            id="grouperAttestationSendEmailId" style="width: 25em"
            onchange="ajax('../app/UiV2Attestation.editGroupAttestation', {formIds: 'editGroupAttestationFormId'}); return false;">
              <option value="true"
                ${grouperRequestContainer.attestationContainer.editAttestationSendEmail ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['grouperAttestationYesSendEmailLabel']}</option>
              <option value="false"
                ${grouperRequestContainer.attestationContainer.editAttestationSendEmail ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['grouperAttestationNoDoNotSendEmailLabel']}</option>
          </select> <br /> <span class="description">${textContainer.text['grouperAttestationSendEmailDescription']}</span>
          </td>
        </tr>
        <c:if
          test="${grouperRequestContainer.attestationContainer.editAttestationShowEmailSettings}">
          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong><label
                for="grouperAttestationEmailGroupManagersId">${textContainer.text['attestationEmailManagersLabel']}</label></strong></td>
            <td><select
              name="grouperAttestationEmailGroupManagersName"
              id="grouperAttestationEmailGroupManagersId"
              style="width: 25em"
              onchange="ajax('../app/UiV2Attestation.editGroupAttestation', {formIds: 'editGroupAttestationFormId'}); return false;">
                <option value="true"
                  ${grouperRequestContainer.attestationContainer.editAttestationEmailGroupManagers ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['grouperAttestationEmailManagersLabel']}</option>
                <option value="false"
                  ${grouperRequestContainer.attestationContainer.editAttestationEmailGroupManagers ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['grouperAttestationDontEmailManagersLabel']}</option>
            </select> <br /> <span class="description">${textContainer.text['grouperAttestationEmailManagersDescription']}</span>
            </td>
          </tr>
          <c:if
            test="${grouperRequestContainer.attestationContainer.editAttestationShowEmailAddresses}">
            <tr>
              <td style="vertical-align: top; white-space: nowrap;"><strong><label
                  for="grouperAttestationEmailAddressesId">${textContainer.text['attestationEmailAddressesLabel']}</label></strong></td>
              <td>
              
                <span style="white-space: nowrap">
                  <input type="text" style="width: 35em" value="${grouper:escapeHtml(grouperRequestContainer.attestationContainer.editAttestationEmailAddresses)}"
                     name="grouperAttestationEmailAddressesName" id="grouperAttestationEmailAddressesId" />
                  <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                    data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                </span>
              
                <br /> <span class="description">${textContainer.text['grouperAttestationEmailAddressesDescription']}</span>
                
              </td>
            </tr>
          </c:if>
        </c:if>
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label
              for="grouperAttestationDefaultCertifyId">${textContainer.text['attestationDefaultCertifyLabel']}</label></strong></td>
          <td><select
            name="grouperAttestationDefaultCertifyName"
            id="grouperAttestationDefaultCertifyId"
            style="width: 25em"
            onchange="ajax('../app/UiV2Attestation.editGroupAttestation', {formIds: 'editGroupAttestationFormId'}); return false;">
              <option value="true"
                ${grouperRequestContainer.attestationContainer.editAttestationDefaultCertify ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['attestationDoDefaultCertifyLabel']}</option>
              <option value="false"
                ${grouperRequestContainer.attestationContainer.editAttestationDefaultCertify ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['attestationDontDefaultCertifyLabel']}</option>
          </select> <br /> <span class="description">${textContainer.text['attestationDefaultCertifyDescription']}</span>
          </td>
        </tr>
        <c:if
          test="${!grouperRequestContainer.attestationContainer.editAttestationDefaultCertify}">
          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong><label
                for="grouperAttestationCustomRecertifyDaysId">${textContainer.text['attestationDaysUntilRecertifyLabel']}</label></strong></td>
            <td>
            
              <span style="white-space: nowrap">
                <input type="text" style="width: 35em" value="${grouper:escapeHtml(grouperRequestContainer.attestationContainer.editAttestationCustomRecertifyDays)}"
                   name="grouperAttestationCustomRecertifyDaysName" id="grouperAttestationCustomRecertifyDaysId" />
                <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                  data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
              </span>
            
              <br /> <span class="description">${textContainer.text['attestationDaysUntilRecertifyDescription']}</span>
              
            </td>
          </tr>
        </c:if>
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label
              for="grouperAttestationMarkAsReviewedId">${textContainer.text['grouperAttestationMarkAsReviewedLabel']}</label></strong></td>
          <td><select
            name="grouperAttestationMarkAsReviewedName"
            id="grouperAttestationMarkAsReviewedId"
            style="width: 25em"
            onchange="ajax('../app/UiV2Attestation.editGroupAttestation', {formIds: 'editGroupAttestationFormId'}); return false;">
              <option value="true"
                ${grouperRequestContainer.attestationContainer.editAttestationResetCertifiedToToday ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['grouperAttestationYesMarkAsReviewedLabel']}</option>
              <option value="false"
                ${grouperRequestContainer.attestationContainer.editAttestationResetCertifiedToToday ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['grouperAttestationNoDontMarkAsReviewedLabel']}</option>
          </select> <br /> <span class="description">${textContainer.text['grouperAttestationMarkAsReviewedDescription']}</span>
          </td>
        </tr>        
      </c:if>
      <tr>
        <td></td>
        <td
          style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
          <input type="submit" class="btn btn-primary"
          aria-controls="groupFilterResultsId" id="filterSubmitId"
          value="${textContainer.text['grouperAttestationEditButtonSave'] }"
          onclick="ajax('../app/UiV2Attestation.editGroupAttestationSave', {formIds: 'editGroupAttestationFormId'}); return false;">
          &nbsp; <a class="btn btn-cancel" role="button"
          onclick="return guiV2link('operation=UiV2Attestation.groupAttestation?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;">${textContainer.text['grouperAttestationEditButtonCancel'] }</a>
        </td>
      </tr>
    </tbody>
  </table>

    <%-- button class="btn"
      onclick="return guiV2link('operation=UiV2Attestation.updateGroupAttestationLastCertifiedDate&groupId=${grouperRequestContainer.attestationContainer.guiAttestation.attributeAssignable.id}'); return false;">${textContainer.text['updateAttestationDateCertifiedGroupButton'] }</button> --%>

</form>