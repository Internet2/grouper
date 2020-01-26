<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set  value="${grouperRequestContainer.workflowContainer.guiGrouperWorkflowConfig}" var="guiWorkflowConfig"/>
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigHasTypeId">${textContainer.text['grouperWorkflowConfigTypeLabel']}</label></strong></td>
    <td>
      <select name="grouperWorkflowConfigType" id="grouperWorkflowConfigHasTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2GrouperWorkflow.formAdd', {formIds: 'addWorkflowConfigFormId'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.workflowContainer.allConfigTypes}" var="workflowConfigType">
          <option value="${workflowConfigType}"
              ${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigType == workflowConfigType ? 'selected="selected"' : '' }
              >${workflowConfigType}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigTypeHint']}</span>
    </td>
  </tr>
  
  <c:if test="${!grouper:isBlank(grouperRequestContainer.workflowContainer.guiGrouperWorkflowConfig.grouperWorkflowConfig.workflowConfigType)}">
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigNameId">${textContainer.text['grouperWorkflowConfigNameLabel']}</label></strong></td>
    <td>
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiWorkflowConfig.grouperWorkflowConfig.workflowConfigName)}"
         name="grouperWorkflowConfigName" id="grouperWorkflowConfigNameId" />
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigNameHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigIdId">${textContainer.text['grouperWorkflowConfigIdLabel']}</label></strong></td>
    <td>
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiWorkflowConfig.grouperWorkflowConfig.workflowConfigId)}"
         name="grouperWorkflowConfigId" id="grouperWorkflowConfigIdId" />
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigIdHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigDescriptionId">${textContainer.text['grouperWorkflowConfigDescriptionLabel']}</label></strong></td>
    <td>
      <textarea id="grouperWorkflowConfigDescriptionId" name="grouperWorkflowConfigDescription" rows="6" cols="60" class="input-block-level">${textContainer.textEscapeDouble['workflowConfigDefaultDescription']}</textarea>
         
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigDescriptionHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigApprovalsId">${textContainer.text['grouperWorkflowConfigApprovalsLabel']}</label></strong></td>
    <td>
      <textarea id="grouperWorkflowConfigApprovalsId" name="grouperWorkflowConfigApprovals" rows="10" cols="60" class="input-block-level">${grouper:escapeHtml(guiWorkflowConfig.workflowApprovalStates)}</textarea>
         
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigApprovalsHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigParamsId">${textContainer.text['grouperWorkflowConfigParamsLabel']}</label></strong></td>
    <td>
      <textarea id="grouperWorkflowConfigParamsId" name="grouperWorkflowConfigParams" rows="10" cols="60" class="input-block-level">${grouper:escapeHtml(guiWorkflowConfig.workflowConfigParams)}</textarea>
         
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigParamsHint']}</span>
    </td>
  </tr>
  
   <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigFormId">${textContainer.text['grouperWorkflowConfigFormLabel']}</label></strong></td>
    <td>
      <textarea <c:if test="${!grouperRequestContainer.workflowContainer.canEditWorkflowFormField}">disabled</c:if> id="grouperWorkflowConfigFormId" name="grouperWorkflowConfigForm" rows="10" cols="60" class="input-block-level">${grouper:escapeHtml(guiWorkflowConfig.grouperWorkflowConfig.workflowConfigForm)}</textarea>
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigFormHint']}</span>
    </td>
   </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigViewersGroupIdId">${textContainer.text['grouperWorkflowConfigViewersGroupIdLabel']}</label></strong></td>
    <td>
      <grouper:combobox2 idBase="grouperWorkflowConfigViewersGroupCombo" style="width: 30em" 
         value="${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigViewersGroupId}"
         filterOperation="../app/UiV2Group.groupUpdateFilter" />
      
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigViewersGroupIdHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigSendEmailId">${textContainer.text['grouperWorkflowConfigSendEmailLabel']}</label></strong></td>
    <td>
      <select name="grouperWorkflowConfigSendEmail" id="grouperWorkdlowConfigSendEmailId" style="width: 30em">
        <option value="false" ${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigSendEmail ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['grouperWorkflowConfigNoDoNotSendEmailLabel']}</option>
        <option value="true" ${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigSendEmail ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['grouperWorkflowConfigYesSendEmailLabel']}</option>
      </select>
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigSendEmailHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigEnabledId">${textContainer.text['grouperWorkflowConfigEnabledLabel']}</label></strong></td>
    <td>
      <select name="grouperWorkflowConfigEnabled" id="grouperWorkflowConfigEnabledId" style="width: 30em">
        <option value="true" ${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigEnabled == 'true' ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['grouperWorkflowConfigYesEnableLabel']}</option>
        <option value="false" ${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigEnabled == 'false' ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['grouperWorkflowConfigNoDoNotEnableLabel']}</option>
        <option value="noNewSubmissions" ${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigEnabled == 'noNewSubmissions' ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['grouperWorkflowConfigNoNewSubmissionsLabel']}</option>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
    </td>
  </tr>
  </c:if>