<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

<%@ include file="../group/groupHeader.jsp" %>

<div class="row-fluid">
  <div class="span12">
    <div id="messages"></div>
        
    <div class="tab-interface">
      <ul class="nav nav-tabs">
        <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
        <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
          <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
        </c:if>
        <%@ include file="../group/groupMoreTab.jsp" %>
      </ul>
    </div>
    <div class="row-fluid">
      <div class="lead span9">${textContainer.text['grouperWorkflowGroupConfigDescription'] }</div>
      <div class="span3" id="grouperWorkflowGroupMoreActionsButtonContentsDivId">
        <%@ include file="grouperWorkflowGroupMoreActionsButtonContents.jsp"%>
      </div>
    </div>
    
    <div class="row-fluid">
      <div class="span9"> <p>${textContainer.text['grouperWorkflowDescription'] }</p></div>
    </div>

    <c:set var="workflowConfig" value="${grouperRequestContainer.workflowContainer.guiGrouperWorkflowConfig.grouperWorkflowConfig}" />
    <table class="table table-condensed table-striped">
      <tbody>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperWorkflowConfigIdLabel']}</label></strong></td>
          <td>${workflowConfig.workflowConfigId}</td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperWorkflowConfigNameLabel']}</label></strong></td>
          <td>${workflowConfig.workflowConfigName}</td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperWorkflowConfigDescriptionLabel']}</label></strong></td>
          <td>${workflowConfig.workflowConfigDescription}</td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperWorkflowConfigApprovalsLabel']}</label></strong></td>
          <td>${grouperRequestContainer.workflowContainer.guiGrouperWorkflowConfig.workflowApprovalStates}</td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperWorkflowConfigParamsLabel']}</label></strong></td>
          <td>${grouperRequestContainer.workflowContainer.guiGrouperWorkflowConfig.workflowConfigParams}</td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperWorkflowConfigFormLabel']}</label></strong></td>
          <td>${grouper:escapeHtml(workflowConfig.workflowConfigForm)}</td>
        </tr>
                
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperWorkflowConfigViewersGroupIdLabel']}</label></strong></td>
          <td>${grouperRequestContainer.workflowContainer.guiGrouperWorkflowConfig.guiGroup.shortLinkWithIcon}</td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperWorkflowConfigSendEmailLabel']}</label></strong></td>
          <td>
            <c:choose>
              <c:when test="${workflowConfig.workflowConfigSendEmail == true}">
                ${textContainer.textEscapeXml['grouperWorkflowConfigYesSendEmailLabel']}
              </c:when>
              <c:otherwise>
                ${textContainer.textEscapeXml['grouperWorkflowConfigNoDoNotSendEmailLabel']}
              </c:otherwise>
            </c:choose>
          </td>
        </tr>

        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperWorkflowConfigEnabledLabel']}</label></strong></td>
          <td>
            <c:if test="${workflowConfig.workflowConfigEnabled == 'true' }">
              ${textContainer.text['grouperWorkflowConfigTableEnabledTrueValue']}
            </c:if>
            <c:if test="${workflowConfig.workflowConfigEnabled == 'false' }">
              ${textContainer.text['grouperWorkflowConfigTableEnabledFalseValue']}
            </c:if>
            <c:if test="${workflowConfig.workflowConfigEnabled == 'noNewSubmissions' }">
              ${textContainer.text['grouperWorkflowConfigTableEnabledNoNewSubmissionsValue']}
            </c:if>
          </td>
        </tr>

      </tbody>
    </table>
    
  </div>
</div>