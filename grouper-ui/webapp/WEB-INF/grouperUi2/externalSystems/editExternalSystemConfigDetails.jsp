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
			  	<form class="form-inline form-small form-filter" id="externalSystemConfigDetails">
			  		<input type="hidden" name="previousExternalSystemConfigId" value="${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem.configId}" />
					
					${grouperRequestContainer.externalSystemContainer.html}
					
					<div class="span6">
                   
                     <input type="submit" class="btn btn-primary"
                          aria-controls="workflowConfigSubmitId" id="submitId"
                          value="${textContainer.text['grouperExternalSystemConfigEditFormSubmitButton'] }"
                          onclick="ajax('../app/UiV2ExternalSystem.editExternalSystemConfigDetailsSubmit?externalSystemConfigId=${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem.configId}&&externalSystemType=${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem['class'].name}', {formIds: 'externalSystemConfigDetails'}); return false;">
                          &nbsp; 
                     <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2ExternalSystem.viewExternalSystems'); return false;"
                          >${textContainer.text['grouperExternalSystemConfigEditFormCancelButton'] }</a>
                   
                   </div>
				</form>
			  </div>
			</div>