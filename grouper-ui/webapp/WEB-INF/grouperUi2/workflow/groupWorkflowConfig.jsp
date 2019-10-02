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
    
    <c:choose>
      <c:when test="${fn:length(grouperRequestContainer.workflowContainer.guiWorkflowConfigs) > 0}">
        
        <table class="table table-hover table-bordered table-striped table-condensed data-table">
          <thead>        
            <tr>
              <th>${textContainer.text['grouperWorkflowConfigTableHeaderConfigId']}</th>
              <th>${textContainer.text['grouperWorkflowConfigTableHeaderConfigName']}</th>
              <th>${textContainer.text['grouperWorkflowConfigTableHeaderConfigType']}</th>
              <th>${textContainer.text['grouperWorkflowConfigTableHeaderConfigEnabled']}</th>
              <th>${textContainer.text['grouperWorkflowConfigTableHeaderConfigActions']}</th>
            </tr>
            </thead>
            <tbody>
              <c:set var="i" value="0" />
              <c:forEach items="${grouperRequestContainer.workflowContainer.guiWorkflowConfigs}" var="guiWorkflowConfig">
              
                <tr>
                   <td style="white-space: nowrap;">
                    <a href="#" onclick="return guiV2link('operation=UiV2GrouperWorkflow.viewInstances&workflowConfigId=${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');">
                    ${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigId}</a>
                   </td>
                   
                   <td style="white-space: nowrap;">
                    ${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigName}
                   </td>
                   
                   <td style="white-space: nowrap;">
                    ${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigType}
                   </td>
                   
                   <td style="white-space: nowrap;">
                     <c:if test="${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigEnabled == 'true' }">
                      ${textContainer.text['grouperWorkflowConfigTableEnabledTrueValue']}
                     </c:if>
                     <c:if test="${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigEnabled == 'false' }">
                      ${textContainer.text['grouperWorkflowConfigTableEnabledFalseValue']}
                     </c:if>
                     <c:if test="${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigEnabled == 'noNewSubmissions' }">
                      ${textContainer.text['grouperWorkflowConfigTableEnabledNoNewSubmissionsValue']}
                     </c:if>
                   </td>
                  
                   <td>
                     <div class="btn-group">
                           <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                             aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                             ${textContainer.text['stemViewActionsButton'] }
                             <span class="caret"></span>
                           </a>
                           <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                             <c:if test="${grouperRequestContainer.workflowContainer.canConfigureWorkflow }">
                               <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperWorkflow.editWorkflowConfig&workflowConfigId=${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');">${textContainer.text['grouperWorkflowConfigTableEditDetailsActionOption'] }</a></li>                             
                             </c:if>
                             <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperWorkflow.viewWorkflowConfigDetails&workflowConfigId=${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');">${textContainer.text['grouperWorkflowConfigTableViewDetailsActionOption'] }</a></li>
                           </ul>
                         </div>
                   </td>
              </c:forEach>
             
             </tbody>
         </table>
        
      </c:when>
      <c:otherwise>
        <div class="row-fluid">
          <div class="span9"> <p><b>${textContainer.text['grouperWorkflowConfigNoEntitiesFound'] }</b></p></div>
        </div>
      </c:otherwise>
    </c:choose>
    
  </div>
</div>