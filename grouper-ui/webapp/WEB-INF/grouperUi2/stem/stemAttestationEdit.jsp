<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<form id="editStemAttestationFormId" class="form-horizontal">
  <table class="table table-condensed table-striped">
    <tbody>

      <tr>
        <td style="vertical-align: top; white-space: nowrap;"><strong><label
            for="grouperAttestationDirectAssignmentId">${textContainer.text['attestationDirectAssignmentLabel']}</label></strong></td>
        <td><select name="grouperAttestationDirectAssignmentName"
          id="grouperAttestationDirectAssignmentId" style="width: 25em"
          onchange="ajax('../app/UiV2Attestation.editStemAttestation?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'editStemAttestationFormId'}); return false;">
            <option value="false"
              ${grouperRequestContainer.attestationContainer.editAttestationIsAssigned ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['grouperAttestationNoDoesNotHaveAttestationLabel']}</option>
            <option value="true"
              ${grouperRequestContainer.attestationContainer.editAttestationIsAssigned ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['grouperAttestationYesHasAttestationLabel']}</option>
        </select> <br /> <span class="description">${textContainer.text['grouperAttestationStemHasAttestationDescription']}</span>
        </td>
      </tr>

      <c:if
        test="${grouperRequestContainer.attestationContainer.editAttestationShowHasAttestation}">

        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label
              for="grouperAttestationHasAttestationId">${textContainer.text['attestationHasAttestationLabel']}</label></strong></td>
          <td><select name="grouperAttestationHasAttestationName"
            id="grouperAttestationHasAttestationId" style="width: 25em"
            onchange="ajax('../app/UiV2Attestation.editStemAttestation?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'editStemAttestationFormId'}); return false;">
              <option value="true"
                ${grouperRequestContainer.attestationContainer.editAttestationHasAttestation ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['attestationHasAttestationYes']}</option>
              <option value="false"
                ${grouperRequestContainer.attestationContainer.editAttestationHasAttestation ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['attestationHasAttestationNo']}</option>
          </select> <br /> <span class="description">${textContainer.text['attestationHasAttestationDescription']}</span>
          </td>
        </tr>
      </c:if>
      <c:if
        test="${grouperRequestContainer.attestationContainer.editAttestationShowType}">

        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label
              for="grouperAttestationTypeId">${textContainer.text['attestationTypeLabel']}</label></strong></td>
          <td><select name="grouperAttestationTypeName"
            id="grouperAttestationTypeId" style="width: 25em"
            onchange="ajax('../app/UiV2Attestation.editStemAttestation?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'editStemAttestationFormId'}); return false;">
              <option value="group"
                ${grouperRequestContainer.attestationContainer.editAttestationType == 'group' ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['grouperAttestationTypeGroupLabel']}</option>
              <option value="report"
                ${grouperRequestContainer.attestationContainer.editAttestationType == 'report' ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['grouperAttestationTypeReportLabel']}</option>
          </select> <br /> <span class="description">${textContainer.text['grouperAttestationTypeDescription']}</span>
          </td>
        </tr>
      </c:if>
      <c:if
        test="${grouperRequestContainer.attestationContainer.editAttestationShowReportConfiguration}">

        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label
              for="grouperAttestationReportConfigurationId">${textContainer.text['attestationReportConfigurationLabel']}</label></strong></td>
          <td><select name="grouperAttestationReportConfigurationName" id="grouperAttestationReportConfigurationId" style="width: 25em">
            <c:forEach items="${grouperRequestContainer.attestationContainer.allReportConfigurationsOnFolder}" var="reportConfiguration">
              <option value="${reportConfiguration.getAttributeAssignmentMarkerId()}" ${grouperRequestContainer.attestationContainer.editAttestationReportConfiguration != null && grouperRequestContainer.attestationContainer.editAttestationReportConfiguration.getAttributeAssignmentMarkerId() == reportConfiguration.getAttributeAssignmentMarkerId() ? 'selected="selected"' : ''}>${grouper:escapeHtml(reportConfiguration.getReportConfigName())}</option>
            </c:forEach>
          </select> <br /> <span class="description">${textContainer.text['grouperAttestationReportConfigurationDescription']}</span>
          </td>
        </tr>
      </c:if>
      <c:if
        test="${grouperRequestContainer.attestationContainer.editAttestationShowAuthorizedGroup}">

        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label
              for="grouperAttestationAuthorizedGroupId">${textContainer.text['attestationAuthorizedGroupLabel']}</label></strong></td>
          <td id="grouperAttestationAuthorizedGroupComboTd">
            <style>#grouperAttestationAuthorizedGroupComboTd td {padding: 0; border: 0}</style>
          
            <table style="padding: 0; border-spacing: 0"><tr><td>
              <grouper:combobox2 idBase="grouperAttestationAuthorizedGroupCombo" style="width: 30em"
                value="${grouperRequestContainer.attestationContainer.editAttestationAuthorizedGroup == null ? null : grouperRequestContainer.attestationContainer.editAttestationAuthorizedGroup.id}"
                filterOperation="../app/UiV2Group.groupUpdateFilter" />
              </td><td>
              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                </td></tr></table>
            <span class="description">${textContainer.text['grouperAttestationAuthorizedGroupDescription']}</span>
          </td>
        </tr>
      </c:if>
      <c:if
        test="${grouperRequestContainer.attestationContainer.editAttestationShowSendEmail}">
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label
              for="grouperAttestationSendEmailId">${textContainer.text['attestationSendEmailLabel']}</label></strong></td>
          <td><select name="grouperAttestationSendEmailName"
            id="grouperAttestationSendEmailId" style="width: 25em"
            onchange="ajax('../app/UiV2Attestation.editStemAttestation?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'editStemAttestationFormId'}); return false;">
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
                for="grouperAttestationEmailGroupManagersId">${textContainer.text[grouperRequestContainer.attestationContainer.editAttestationShowReportConfiguration ? 'attestationReportEmailManagersLabel' : 'attestationEmailManagersLabel']}</label></strong></td>
            <td><select
              name="grouperAttestationEmailGroupManagersName"
              id="grouperAttestationEmailGroupManagersId"
              style="width: 25em"
              onchange="ajax('../app/UiV2Attestation.editStemAttestation?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'editStemAttestationFormId'}); return false;">
              <c:choose>
                <c:when test="${grouperRequestContainer.attestationContainer.editAttestationShowReportConfiguration}">
                  <option value="groupManagers"
                    ${grouperRequestContainer.attestationContainer.editAttestationEmailGroupManagers ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['grouperAttestationReportEmailManagersLabel']}</option>
                  <option value="emailList"
                    ${grouperRequestContainer.attestationContainer.editAttestationEmailGroupManagers ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['grouperAttestationDontEmailManagersLabel']}</option>
                </c:when>
                <c:otherwise>
                  <option value="groupManagers"
                    ${grouperRequestContainer.attestationContainer.editAttestationEmailGroupManagers ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['grouperAttestationEmailManagersLabel']}</option>
                  <option value="emailList"
                    ${grouperRequestContainer.attestationContainer.editAttestationEmailList ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['grouperAttestationEmailCustomListLabel']}</option>
                  <option value="emailGroup"
                    ${grouperRequestContainer.attestationContainer.editAttestationEmailToGroup ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['grouperAttestationEmailGroupLabel']}</option>
                </c:otherwise>
              </c:choose>
              
            </select> <br /> <span class="description">${textContainer.text[grouperRequestContainer.attestationContainer.editAttestationShowReportConfiguration ? 'grouperAttestationReportEmailManagersDescription' : 'grouperAttestationEmailManagersDescription']}</span>
            </td>
          </tr>
          <c:if test="${grouperRequestContainer.attestationContainer.editAttestationShowEmailAddresses}">
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
              
                <br /> <span class="description">${textContainer.text[grouperRequestContainer.attestationContainer.editAttestationShowReportConfiguration ? 'grouperAttestationReportEmailAddressesDescription' : 'grouperAttestationEmailAddressesDescription']}</span>
                
              </td>
            </tr>
          </c:if>
          <c:if test="${grouperRequestContainer.attestationContainer.editAttestationShowEmailGroup}">
            <tr>
              <td style="vertical-align: top; white-space: nowrap;"><strong><label
                  for="grouperAttestationEmailGroupComboId">${textContainer.text['attestationEmailGroupLabel']}</label></strong></td>
              <td id="grouperAttestationEmailGroupComboTd">
                <style>#grouperAttestationEmailGroupComboTd td {padding: 0; border: 0}</style>
                <table style="padding: 0; border-spacing: 0"><tr><td>
                  <grouper:combobox2 idBase="grouperAttestationEmailGroupCombo" style="width: 30em"
                    value="${grouperRequestContainer.attestationContainer.editAttestationEmailGroup == null ? null : grouperRequestContainer.attestationContainer.editAttestationEmailGroup.id}"
                    filterOperation="../app/UiV2Group.groupReadFilter" />
                  </td><td>
                  <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                    data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                    </td></tr></table>
              
                <span class="description">${textContainer.text['grouperAttestationEmailGroupDescription']}</span>
                
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
            onchange="ajax('../app/UiV2Attestation.editStemAttestation?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'editStemAttestationFormId'}); return false;">
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
        <c:if test="${grouperRequestContainer.attestationContainer.editAttestationShowFolderScope}">
          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong><label
                for="grouperAttestationStemScopeId">${textContainer.text['grouperAttestationStemScopeLabel']}</label></strong></td>
            <td><select
              name="grouperAttestationStemScopeName"
              id="grouperAttestationStemScopeId"
              style="width: 25em"
              onchange="ajax('../app/UiV2Attestation.editStemAttestation?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'editStemAttestationFormId'}); return false;">
                <option value="true"
                  ${grouperRequestContainer.attestationContainer.editAttestationStemScopeSub ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['grouperAttestationYesStemScopeLabel']}</option>
                <option value="false"
                  ${grouperRequestContainer.attestationContainer.editAttestationStemScopeSub ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['grouperAttestationNoStemScopeLabel']}</option>
            </select> <br /> <span class="description">${textContainer.text['grouperAttestationStemScopeDescription']}</span>
            </td>
          </tr>        
        </c:if>
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label
              for="grouperAttestationMarkAsReviewedId">${textContainer.text[grouperRequestContainer.attestationContainer.editAttestationShowReportConfiguration ? 'grouperAttestationReportMarkStemAsReviewedLabel' : 'grouperAttestationMarkStemAsReviewedLabel']}</label></strong></td>
          <td><select
            name="grouperAttestationMarkAsReviewedName"
            id="grouperAttestationMarkAsReviewedId"
            style="width: 25em"
            onchange="ajax('../app/UiV2Attestation.editStemAttestation?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'editStemAttestationFormId'}); return false;">
              <option value="true"
                ${grouperRequestContainer.attestationContainer.editAttestationResetCertifiedToToday ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml[grouperRequestContainer.attestationContainer.editAttestationShowReportConfiguration ? 'grouperAttestationReportYesStemMarkAsReviewedLabel' : 'grouperAttestationYesStemMarkAsReviewedLabel']}</option>
              <option value="false"
                ${grouperRequestContainer.attestationContainer.editAttestationResetCertifiedToToday ? '' : 'selected="selected"' }>${textContainer.textEscapeXml[grouperRequestContainer.attestationContainer.editAttestationShowReportConfiguration ? 'grouperAttestationReportNoStemDontMarkAsReviewedLabel' : 'grouperAttestationNoStemDontMarkAsReviewedLabel']}</option>
          </select> <br /> <span class="description">${textContainer.text[grouperRequestContainer.attestationContainer.editAttestationShowReportConfiguration ? 'grouperAttestationReportMarkStemAsReviewedDescription' : 'grouperAttestationMarkStemAsReviewedDescription']}</span>
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
          onclick="ajax('../app/UiV2Attestation.editStemAttestationSave?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'editStemAttestationFormId'}); return false;">
          &nbsp; <a class="btn btn-cancel" role="button"
          onclick="return guiV2link('operation=UiV2Attestation.stemAttestation?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;">${textContainer.text['grouperAttestationEditButtonCancel'] }</a>
        </td>
      </tr>
    </tbody>
  </table>

</form>
