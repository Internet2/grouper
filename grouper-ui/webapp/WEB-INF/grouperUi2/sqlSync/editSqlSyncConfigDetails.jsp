<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
		           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
		           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
		           <li><a href="#" onclick="return guiV2link('operation=UiV2SqlSyncConfiguration.viewSqlSyncConfigurations');">${textContainer.text['miscellaneousSqlSyncOverallBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
		           <li class="active">${textContainer.text['miscellaneousSqlSyncEditBreadcrumb'] }</li>
		       </ul>
              
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousSqlSyncMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="sqlSyncConfigsMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
              
			<div class="row-fluid">
			  <div class="span12">
			  	<form class="form-inline form-small form-filter" id="sqlSyncConfigDetails">
			  		<input type="hidden" name="previousSqlSyncConfigId" value="${grouperRequestContainer.sqlSyncConfigurationContainer.guiSqlSyncConfiguration.sqlSyncConfiguration.configId}" />
					
					<table class="table table-condensed table-striped">
              		  <tbody>
              		  
              		  	<tr>
						  <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['sqlSyncConfigIdLabel']}</label></strong></td>
						    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
						    <td>
						     ${grouper:escapeHtml(grouperRequestContainer.sqlSyncConfigurationContainer.guiSqlSyncConfiguration.sqlSyncConfiguration.configId)}
						    </td>
						</tr>
              		  
              		  	
              <c:forEach items="${grouperRequestContainer.sqlSyncConfigurationContainer.guiSqlSyncConfiguration.sqlSyncConfiguration.subSections}" var="subSection">
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
					  					ajaxCallback="ajax('../app/UiV2SqlSyncConfiguration.editSqlSyncConfig?sqlSyncConfigId=${grouperRequestContainer.sqlSyncConfigurationContainer.guiSqlSyncConfiguration.sqlSyncConfiguration.configId}', {formIds: 'sqlSyncConfigDetails'}); return false;"
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
                          value="${textContainer.text['sqlSyncConfigAddFormSubmitButton'] }"
                          onclick="ajax('../app/UiV2SqlSyncConfiguration.editSqlSyncConfigSubmit?sqlSyncConfigId=${grouperRequestContainer.sqlSyncConfigurationContainer.guiSqlSyncConfiguration.sqlSyncConfiguration.configId}', {formIds: 'sqlSyncConfigDetails'}); return false;">
                          &nbsp; 
                     <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2SqlSyncConfiguration.viewSqlSyncConfigurations'); return false;"
                          >${textContainer.text['sqlSyncConfigAddFormCancelButton'] }</a>
                   
                   </div>
				</form>
			  </div>
			</div>