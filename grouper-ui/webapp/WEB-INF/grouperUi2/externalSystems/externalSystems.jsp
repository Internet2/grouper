<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousGrouperExternalSystemsBreadcrumb'] }</li>
              </ul>
              
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousGrouperExternalSystemsMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="externalSystemsMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
              
			<div class="row-fluid">
			  
			    <c:choose>
			      <c:when test="${fn:length(grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystems) > 0}">
			        
			        <table class="table table-hover table-bordered table-striped table-condensed data-table">
			          <thead>        
			            <tr>
			              <th>${textContainer.text['grouperExternalSystemTableHeaderConfigId']}</th>
			              <th>${textContainer.text['grouperExternalSystemTableHeaderType']}</th>
			              <th>${textContainer.text['grouperExternalSystemTableHeaderEnabled']}</th>
			              <th>${textContainer.text['grouperExternalSystemTableHeaderActions']}</th>
			            </tr>
			            </thead>
			            <tbody>
			              <c:set var="i" value="0" />
			              <c:forEach items="${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystems}" var="guiGrouperExternalSystem">
			              
			                <tr>
			                   <td style="white-space: nowrap;">
			                    <a href="#" onclick="return guiV2link('operation=UiV2GrouperWorkflow.viewInstances&workflowConfigId=${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');">
			                    ${guiGrouperExternalSystem.grouperExternalSystem.configId}</a>
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    ${guiGrouperExternalSystem.grouperExternalSystem.title}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                     <c:if test="${guiGrouperExternalSystem.grouperExternalSystem.enabled == true}">
			                      ${textContainer.text['grouperExternalSystemTableEnabledTrueValue']}
			                     </c:if>
			                     <c:if test="${guiGrouperExternalSystem.grouperExternalSystem.enabled == false }">
			                      ${textContainer.text['grouperExternalSystemTableEnabledFalseValue']}
			                     </c:if>
			                   </td>
			                  
			                   <td>
			                     <div class="btn-group">
			                           <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
			                             aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
			                             ${textContainer.text['grouperExternalSystemRowActionsButton'] }
			                             <span class="caret"></span>
			                           </a>
			                           <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
			                             <%-- <c:if test="${grouperRequestContainer.workflowContainer.canConfigureWorkflow }">
			                               <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperWorkflow.editWorkflowConfig&workflowConfigId=${guiWorkflowConfig.grouperWorkflowConfig.workflowConfigId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');">${textContainer.text['grouperWorkflowConfigTableEditDetailsActionOption'] }</a></li>                             
			                             </c:if> --%>
			                             <li><a href="#" onclick="return guiV2link('operation=UiV2ExternalSystem.viewExternalSystemConfigDetails&externalSystemConfigId=${guiGrouperExternalSystem.grouperExternalSystem.configId}');">${textContainer.text['grouperExternalSystemTableViewDetailsActionOption'] }</a></li>
			                           </ul>
			                         </div>
			                   </td>
			              </c:forEach>
			             
			             </tbody>
			         </table>
			        
			      </c:when>
			      <c:otherwise>
			        <div class="row-fluid">
			          <div class="span9"> <p><b>${textContainer.text['grouperExternalSystemNoConfiguredExternalSystemsFound'] }</b></p></div>
			        </div>
			      </c:otherwise>
			    </c:choose>
			    
			  </div>
