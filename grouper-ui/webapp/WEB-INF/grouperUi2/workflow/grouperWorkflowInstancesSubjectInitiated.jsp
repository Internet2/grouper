<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <div class="bread-header-container">
     <ul class="breadcrumb">
       <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
       <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
       <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperWorkflow.forms');">${textContainer.text['workflowMiscFormsLink'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
       <li class="active">${textContainer.text['workflowMiscFormsSubjectInitiatedTitle'] }</li>
     </ul>
     
     <div class="page-header blue-gradient">
       <h1>${textContainer.text['workflowMiscFormsSubjectInitiatedTitle'] }</h1>
     </div>
              
   </div>
   
   <div class="row-fluid">
     <div class="span12">
     
       <table class="table table-hover table-bordered table-striped table-condensed data-table">
          <thead>
            <tr>
              <th style="white-space: nowrap; text-align: left;">
                ${textContainer.text['workflowInstanceTableColumnHeaderInstanceConfigName'] }
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
            <c:forEach items="${grouperRequestContainer.workflowContainer.workflowInstances}" var="instance">
              <tr>
                
                <td style="white-space: nowrap;">
                  ${instance.grouperWorkflowConfig.workflowConfigName}
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
                      
                        <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperWorkflow.viewInstance&attributeAssignId=${instance.attributeAssignId}');">${textContainer.text['workflowInstanceTableColumnHeaderInstanceActionsViewInstanceForm'] }</a></li>
                             
                      </ul>
                  </div>
                </td>
                
                
              </tr>
            </c:forEach>
          </tbody>
        </table>
       
     </div>
   </div>