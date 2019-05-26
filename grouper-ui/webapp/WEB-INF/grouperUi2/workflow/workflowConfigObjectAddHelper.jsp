<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigHasTypeId">${textContainer.text['grouperWorkflowConfigTypeLabel']}</label></strong></td>
    <td>
      <select name="grouperWorkflowConfigType" id="grouperWorkflowConfigHasTypeId" style="width: 30em"
          onchange="ajax('../app/UiV2GrouperWorkflow.formAdd', {formIds: 'addWorkflowConfigFormId'}); return false;">
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.workflowContainer.allConfigTypes}" var="workflowConfigType">
          <option value="${configType}"
              ${grouperRequestContainer.workflowContainer.workflowConfig.workflowConfigType == workflowConfigType ? 'selected="selected"' : '' }
              >${workflowConfigType}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigTypeHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigNameId">${textContainer.text['grouperWorkflowConfigNameLabel']}</label></strong></td>
    <td>
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.workflowContainer.workflowConfig.workflowConfigName)}"
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
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.workflowContainer.workflowConfig.workflowConfigId)}"
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
      <textarea id="grouperWorkflowConfigDescriptionId" name="grouperWorkflowConfigDescription" rows="6" cols="60" class="input-block-level">
        ${grouper:escapeHtml(grouperRequestContainer.workflowContainer.workflowConfig.workflowConfigDescription)}</textarea>
         
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigDescriptionHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigViewersGroupIdId">${textContainer.text['grouperWorkflowConfigViewersGroupIdLabel']}</label></strong></td>
    <td>
      <grouper:combobox2 idBase="grouperWorkflowConfigViewersGroupCombo" style="width: 30em" 
         value="${grouperRequestContainer.workflowContainer.workflowConfig.workflowConfigViewersGroupId}"
         filterOperation="../app/UiV2Group.groupUpdateFilter" />
      
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigViewersGroupIdHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperWorkflowConfigSendEmailId">${textContainer.text['grouperWorkflowConfigSendEmailLabel']}</label></strong></td>
    <td>
      <select name="grouperWorkflowConfigSendEmail" id="grouperWorkdlowConfigSendEmailId" style="width: 30em">
        <option value="false" ${grouperRequestContainer.workflowContainer.workflowConfig.workflowConfigSendEmail ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['grouperWorkflowConfigNoDoNotSendEmailLabel']}</option>
        <option value="true" ${grouperRequestContainer.workflowContainer.workflowConfig.workflowConfigSendEmail ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['grouperWorkflowConfigYesSendEmailLabel']}</option>
      </select>
      <br />
      <span class="description">${textContainer.text['grouperWorkflowConfigSendEmailHint']}</span>
    </td>
  </tr>