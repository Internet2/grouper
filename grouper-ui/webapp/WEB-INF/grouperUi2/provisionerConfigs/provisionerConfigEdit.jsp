<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <c:set value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration" />

	 <div class="bread-header-container">
       <ul class="breadcrumb">
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li class="active">${textContainer.text['miscellaneousProvisionerConfigEditBreadcrumb'] }</li>
       </ul>
       
       <div class="page-header blue-gradient">
       
         <div class="row-fluid">
           <div class="lead span9 pull-left"><h1>${textContainer.text['miscellaneousProvisionerConfigurationsEditProvisionerDescription'] }</h1></div>
           <div class="span2 pull-right">
             <c:set var="buttonSize" value="btn-medium" />
             <c:set var="buttonBlock" value="btn-block" />
             <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
           </div>
         </div>
       </div>
     </div>
     
     
     <div class="row-fluid">
	  <div class="span12">
	   <div id="messages"></div>
         
         <form class="form-inline form-small form-filter" id="provisionerConfigDetails">
         	<input type="hidden" name="previousProvisionerConfigId" value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}" />
            <table class="table table-condensed table-striped">
              <tbody>
              
              <tr>
				  <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['provisionerConfigIdLabel']}</label></strong></td>
				    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
				    <td>
				     ${grouper:escapeHtml(grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId)}
				    </td>
				</tr>
              
               	
               	<c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.subSections}" var="subSection">
			  		<tbody>
			  			<c:if test="${!grouper:isBlank(subSection.label) and subSection.show}">
				  			<tr>
				  				<th colspan="3">
                    <%-- the header needs to be on a field to subsitute the name in the label if there --%>
                    <c:set target="${grouperRequestContainer.provisionerConfigurationContainer}"
                      property="currentConfigSuffix"
                      value="${subSection.label}.header" />  
				  					<h4>${subSection.title}</h4>
				  					<p style="font-weight: normal;">${subSection.description} </p>
				  				</th>
				  			</tr>
			  			
			  			</c:if>
			  			
			  			<c:forEach items="${subSection.attributesValues}" var="attribute">
			  				
			  				<c:set target="${grouperRequestContainer.provisionerConfigurationContainer}"
				               	property="index"
				               	value="${attribute.repeatGroupIndex}" />
                <c:set target="${grouperRequestContainer.provisionerConfigurationContainer}"
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
			  					ajaxCallback="ajax('../app/UiV2ProvisionerConfiguration.editProvisionerConfiguration?focusOnElementName=config_${attribute.configSuffix}&provisionerConfigId=${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration['class'].name}', {formIds: 'provisionerConfigDetails'}); return false;"
			  					valuesAndLabels="${attribute.dropdownValuesAndLabels }"
			  					checkboxAttributes="${attribute.checkboxAttributes}"
			  				/>
			  				
			  			</c:forEach>
			  			
			  		</tbody>
			  
			  	</c:forEach>
               
               
              </tbody>
            </table>
            
            <div class="span6">
                   
              <input type="submit" class="btn btn-primary"
                   aria-controls="workflowConfigSubmitId" id="submitId"
                   value="${textContainer.text['provisionerConfigEditFormSubmitButton'] }"
                   onclick="ajax('../app/UiV2ProvisionerConfiguration.editProvisionerConfigurationSubmit?provisionerConfigId=${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration['class'].name}', {formIds: 'provisionerConfigDetails'}); return false;">
                   &nbsp;
              <a class="btn btn-cancel" role="button"
                   onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations'); return false;"
                   >${textContainer.text['provisionerConfigEditFormCancelButton'] }</a>
            
            </div>
            
          </form>
	  	
	  </div>
	</div>
