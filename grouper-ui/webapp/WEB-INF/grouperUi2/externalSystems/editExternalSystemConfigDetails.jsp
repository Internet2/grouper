<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2ExternalSystem.viewExternalSystems');">${textContainer.text['miscellaneousGrouperExternalSystemsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousGrouperExternalSystemsEditBreadcrumb'] }</li>
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
					
					<table class="table table-condensed table-striped">
              		  <tbody>
              		  
              		  	<tr>
						  <td style="vertical-align: top; white-space: nowrap;"><strong><label for="externalSystemConfigId">${textContainer.text['grouperExternalSystemConfigIdLabel']}</label></strong></td>
						    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
						    <td>
						     ${grouper:escapeHtml(grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem.configId)}
						    </td>
						</tr>
              		  
              		  	
              		  	<c:forEach items="${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem.subSections}" var="subSection">
					  		<tbody>
					  			<c:if test="${!grouper:isBlank(subSection.label)}">
						  			<tr>
						  				<th colspan="3">
						  					<h4>${subSection.title}</h4>
						  					<p style="font-weight: normal;">${subSection.description} </p>
						  				</th>
						  			</tr>
					  			
					  			</c:if>
					  			
					  			<c:forEach items="${subSection.attributesValues}" var="attribute">
					  				
					  				<grouper:configFormElement 
					  					formElementType="${attribute.formElement}" 
					  					configId="${attribute.configSuffix}" 
					  					label="${attribute.label}"
					  					helperText="${attribute.description}"
					  					helperTextDefaultValue="${attribute.defaultValue}"
					  					required="${attribute.required}"
					  					shouldShow="${attribute.show}"
					  					value="${attribute.valueOrExpressionEvaluation}"
					  					hasExpressionLanguage="${attribute.expressionLanguage}"
					  					ajaxCallback="ajax('../app/UiV2ExternalSystem.editExternalSystemConfigDetails?externalSystemConfigId=${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem.configId}&externalSystemType=${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem['class'].name}', {formIds: 'externalSystemConfigDetails'}); return false;"
					  					valuesAndLabels="${attribute.dropdownValuesAndLabels }"
					  				/>
					  				
					  			</c:forEach>
					  			
					  		</tbody>
					  
					  </c:forEach>
              		  	
              		  </tbody>
              		</table>
					
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