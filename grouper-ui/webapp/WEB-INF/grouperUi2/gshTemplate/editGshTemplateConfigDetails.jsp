<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
		           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
		           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
		           <li><a href="#" onclick="return guiV2link('operation=UiV2GshTemplateConfig.viewGshTemplates');">${textContainer.text['miscellaneousGshTemplatesOverallBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
		           <li class="active">${textContainer.text['miscellaneousGshTemplatesEditBreadcrumb'] }</li>
		       </ul>
              
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousGshTemplatesMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="gshTemplateConfigsMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
              
			<div class="row-fluid">
			  <div class="span12">
			  	<form class="form-inline form-small form-filter" id="gshTemplateConfigDetails">
			  		<input type="hidden" name="previousGshTemplateConfigId" value="${grouperRequestContainer.gshTemplateContainer.guiGshTemplateConfiguration.gshTemplateConfiguration.configId}" />
					
					<table class="table table-condensed table-striped">
              		  <tbody>
              		  
              		  	<tr>
						  <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['gshTemplateConfigIdLabel']}</label></strong></td>
						    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
						    <td>
						     ${grouper:escapeHtml(grouperRequestContainer.gshTemplateContainer.guiGshTemplateConfiguration.gshTemplateConfiguration.configId)}
						    </td>
						</tr>
              		  
              		  	
              		  	<c:forEach items="${grouperRequestContainer.gshTemplateContainer.guiGshTemplateConfiguration.gshTemplateConfiguration.subSections}" var="subSection">
					  		<tbody>
					  			<c:if test="${!grouper:isBlank(subSection.label) and subSection.show }">
						  			<tr>
						  				<th colspan="3">
						  					<h4>${subSection.title}</h4>
						  					<p style="font-weight: normal;">${subSection.description} </p>
						  				</th>
						  			</tr>
					  			
					  			</c:if>
					  			
					  			<c:forEach items="${subSection.attributesValues}" var="attribute">
					  				
					  				<c:set target="${grouperRequestContainer.gshTemplateContainer}"
						               	property="index"
						               	value="${attribute.repeatGroupIndex}" />
					               		
					        		<c:set target="${grouperRequestContainer.gshTemplateContainer}"
						                property="currentConfigSuffix"
						                value="${attribute.configSuffix}" />
					  				
					  				<grouper:configFormElement 
					  					formElementType="${attribute.formElement}" 
					  					configId="${attribute.configSuffix}" 
					  					label="${attribute.label}"
					  					readOnly="${attribute.readOnly}"
					  					helperText="${attribute.description}"
					  					helperTextDefaultValue="${attribute.defaultValue}"
					  					required="${attribute.required}"
					  					shouldShow="${attribute.show}"
					  					value="${attribute.valueOrExpressionEvaluation}"
					  					hasExpressionLanguage="${attribute.expressionLanguage}"
					  					ajaxCallback="ajax('../app/UiV2GshTemplateConfig.editGshTemplate?gshTemplateConfigId=${grouperRequestContainer.gshTemplateContainer.guiGshTemplateConfiguration.gshTemplateConfiguration.configId}', {formIds: 'gshTemplateConfigDetails'}); return false;"
					  					valuesAndLabels="${attribute.dropdownValuesAndLabels }"
					  					checkboxAttributes="${attribute.checkboxAttributes}"
                      indent="${attribute.configItemMetadata.indent}"
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
                          onclick="ajax('../app/UiV2GshTemplateConfig.editGshTemplateSubmit?gshTemplateConfigId=${grouperRequestContainer.gshTemplateContainer.guiGshTemplateConfiguration.gshTemplateConfiguration.configId}', {formIds: 'gshTemplateConfigDetails'}); return false;">
                          &nbsp; 
                     <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2GshTemplateConfig.viewGshTemplates'); return false;"
                          >${textContainer.text['grouperExternalSystemConfigEditFormCancelButton'] }</a>
                   
                   </div>
				</form>
			  </div>
			</div>