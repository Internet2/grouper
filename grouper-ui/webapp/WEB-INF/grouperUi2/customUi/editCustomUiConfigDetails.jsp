<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
		           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
		           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
		           <li><a href="#" onclick="return guiV2link('operation=UiV2CustomUiConfig.viewCustomUiConfigs');">${textContainer.text['miscellaneousCustomUiOverallBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
		           <li class="active">${textContainer.text['miscellaneousCustomUiEditBreadcrumb'] }</li>
		       </ul>
              
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousCustomUiMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="customUiConfigsMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
              
			<div class="row-fluid">
			  <div class="span12">
			  	<form class="form-inline form-small form-filter" id="customUiConfigDetails">
			  		<input type="hidden" name="previousCustomUiConfigId" value="${grouperRequestContainer.customUiContainer.guiCustomUiConfiguration.customUiConfiguration.configId}" />
					
					<table class="table table-condensed table-striped">
              		  <tbody>
              		  
              		  	<tr>
						  <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['customUiConfigIdLabel']}</label></strong></td>
						    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
						    <td>
						     ${grouper:escapeHtml(grouperRequestContainer.customUiContainer.guiCustomUiConfiguration.customUiConfiguration.configId)}
						    </td>
						</tr>
              		  
              		  	
              		  	<c:forEach items="${grouperRequestContainer.customUiContainer.guiCustomUiConfiguration.customUiConfiguration.subSections}" var="subSection">
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
					  				
					  				<c:set target="${grouperRequestContainer.customUiContainer}"
		                  property="index"
		                  value="${attribute.repeatGroupIndex}" />
                  
				            <c:set target="${grouperRequestContainer.customUiContainer}"
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
					  					ajaxCallback="ajax('../app/UiV2CustomUiConfig.editCustomUiConfig?customUiConfigId=${grouperRequestContainer.customUiContainer.guiCustomUiConfiguration.customUiConfiguration.configId}', {formIds: 'customUiConfigDetails'}); return false;"
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
                          onclick="ajax('../app/UiV2CustomUiConfig.editCustomUiConfigSubmit?customUiConfigId=${grouperRequestContainer.customUiContainer.guiCustomUiConfiguration.customUiConfiguration.configId}', {formIds: 'customUiConfigDetails'}); return false;">
                          &nbsp; 
                     <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2CustomUiConfig.viewCustomUiConfigs'); return false;"
                          >${textContainer.text['grouperExternalSystemConfigEditFormCancelButton'] }</a>
                   
                   </div>
				</form>
			  </div>
			</div>