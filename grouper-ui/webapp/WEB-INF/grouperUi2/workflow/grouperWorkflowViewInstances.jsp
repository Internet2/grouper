<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

<%@ include file="../group/groupHeader.jsp" %>

<div class="row-fluid">
  <div class="span12">
    <div id="messages"></div>
        
    <div class="tab-interface">
      <c:set var="grouperCurrentTab" value="none" />
      <%@ include file="../group/groupTabs.jsp" %>
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
      <c:when test="${fn:length(grouperRequestContainer.workflowContainer.workflowInstances) > 0}">
       <table class="table table-hover table-bordered table-striped table-condensed data-table">
          <thead>
            <tr>
              <th style="white-space: nowrap; text-align: left;">
                ${textContainer.text['workflowInstanceTableColumnHeaderInstanceConfigName'] }
              </th>
              <th style="white-space: nowrap; text-align: left;">
                ${textContainer.text['workflowInstanceTableColumnHeaderInstanceInitiatorSubject'] }
              </th>
              <th style="white-space: nowrap; text-align: left;">
                ${textContainer.text['workflowInstanceTableColumnHeaderInstanceState'] }
              </th>
              <th style="white-space: nowrap; text-align: left;">
                ${textContainer.text['workflowInstanceTableColumnHeaderInstanceLastUpdated'] }
              </th>
              <th style="white-space: nowrap; text-align: left;">
                ${textContainer.text['workflowInstanceTableColumnHeaderInstanceActions'] }
              </th>
            </tr>
          </thead>
          <tbody>
            <c:forEach items="${grouperRequestContainer.workflowContainer.workflowInstances}" var="guiInstance">
            
              <c:set var="instance" value="${guiInstance.grouperWorkflowInstance}" />
              <tr>
                
                <td style="white-space: nowrap;">
                  ${instance.grouperWorkflowConfig.workflowConfigName}
                </td>
                
                <td style="white-space: nowrap;">
                  ${guiInstance.guiInitiatorSubject.shortLink}
                </td>
                   
                <td style="white-space: nowrap;">
                 ${instance.workflowInstanceState}
                </td>
                   
                <td style="white-space: nowrap;">
                 ${instance.workflowInstanceLastUpdatedDate}
                </td>
                   
                <td>
                  <div class="btn-group">
                    <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                      aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                      ${textContainer.text['workflowInstanceTableColumnHeaderInstanceActions'] }
                      <span class="caret"></span>
                    </a>
                      <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                      
                        <li><a href="?operation=UiV2GrouperWorkflow.viewInstance&attributeAssignId=${instance.attributeAssignId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperWorkflow.viewInstance&attributeAssignId=${instance.attributeAssignId}');">${textContainer.text['workflowInstanceTableColumnHeaderInstanceActionsViewInstanceForm'] }</a></li>
                             
                      </ul>
                  </div>
                </td>
                
                
              </tr>
            </c:forEach>
          </tbody>
        </table>
        </c:when>
        <c:otherwise>
        <div class="row-fluid">
          <div class="span9"> <p><b>${textContainer.text['grouperWorkflowConfigNoInstancesFound'] }</b></p></div>
        </div>
      </c:otherwise>
    </c:choose>
       
     </div>
   </div>