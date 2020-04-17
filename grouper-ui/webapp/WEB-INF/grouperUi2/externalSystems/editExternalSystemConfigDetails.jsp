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
			  <div class="span12">
			  	<form class="form-inline form-small form-filter" id="editConfigDetails">
			  		<input type="hidden" name="previousExternalSystemConfigId" value="${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem.configId}" />
					${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem.html}
					
					<div class="span6">
                   
                     <input type="submit" class="btn btn-primary"
                          aria-controls="workflowConfigSubmitId" id="submitId"
                          value="${textContainer.text['workflowJoinGroupInitiateWorkflowButtonSubmit'] }"
                          onclick="ajax('../app/UiV2ExternalSystem.editExternalSystemConfigDetailsSubmit?externalSystemConfigId=${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem.configId}&&externalSystemType=${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem['class'].name}', {formIds: 'editConfigDetails'}); return false;">
                          &nbsp; <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2GrouperWorkflow.viewForms&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                          >${textContainer.text['workflowConfigButtonCancel'] }</a>
                   
                   </div>
				</form>
			  </div>
			</div>