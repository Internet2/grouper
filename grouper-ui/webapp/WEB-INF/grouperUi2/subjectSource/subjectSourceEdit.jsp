<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="bread-header-container">
   <ul class="breadcrumb">
       <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
       <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
       <li><a href="#" onclick="return guiV2link('operation=UiV2SubjectSource.viewSubjectSources');">${textContainer.text['miscellaneousSubjectSourcesOverallBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
       <li class="active">${textContainer.text['miscellaneousSubjectSourceConfigEditBreadcrumb'] }</li>
   </ul>
                 
   <div class="page-header blue-gradient">
   
     <div class="row-fluid">
       <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousSubjectSourcesMainDescription'] }</h4></div>
       <div class="span2 pull-right">
         <%@ include file="subjectSourcesMoreActionsButtonContents.jsp"%>
       </div>
     </div>
   </div>
   
 </div>
 
 <div class="row-fluid">
	  <div class="span12">
	   <div id="messages"></div>
         
         <form class="form-inline form-small form-filter" id="sourceConfigDetails">
         	<input type="hidden" name="previousSubjectSourceConfigId" value="${grouperRequestContainer.subjectSourceContainer.guiSubjectSourceConfiguration.subjectSourceConfiguration.configId}" />
            <table class="table table-condensed table-striped">
              <tbody>
              
              <tr>
				  <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['subjectSourceConfigIdLabel']}</label></strong></td>
				    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
				    <td>
				     ${grouper:escapeHtml(grouperRequestContainer.subjectSourceContainer.guiSubjectSourceConfiguration.subjectSourceConfiguration.configId)}
				    </td>
				</tr>
              
               	
               	<c:forEach items="${grouperRequestContainer.subjectSourceContainer.guiSubjectSourceConfiguration.subjectSourceConfiguration.subSections}" var="subSection">
			  		<tbody>
			  			<c:if test="${!grouper:isBlank(subSection.label) and subSection.show}">
				  			<tr>
				  				<th colspan="3">
                    <%-- the header needs to be on a field to subsitute the name in the label if there --%>
                    <c:set target="${grouperRequestContainer.subjectSourceContainer}"
                      property="currentConfigSuffix"
                      value="${subSection.label}.header" />  
				  					<h4>${subSection.title}</h4>
				  					<p style="font-weight: normal;">${subSection.description} </p>
				  				</th>
				  			</tr>
			  			
			  			</c:if>
			  			
			  			<c:forEach items="${subSection.attributesValues}" var="attribute">
			  				
			  				<c:set target="${grouperRequestContainer.subjectSourceContainer}"
				               	property="index"
				               	value="${attribute.repeatGroupIndex}" />
                <c:set target="${grouperRequestContainer.subjectSourceContainer}"
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
			  					ajaxCallback="ajax('../app/UiV2SubjectSource.editSubjectSource?focusOnElementName=config_${attribute.configSuffix}&subjectSourceId=${grouperRequestContainer.subjectSourceContainer.subjectSourceId}', {formIds: 'sourceConfigDetails'}); return false;"
			  					valuesAndLabels="${attribute.dropdownValuesAndLabels }"
			  					checkboxAttributes="${attribute.checkboxAttributes}"
			  				/>
			  				
			  			</c:forEach>
			  			
			  		</tbody>
			  
			  	</c:forEach>
               
               
              </tbody>
            </table>
            
            <div class="span6">
                   
              <input type="submit" class="btn btn-primary" id="submitId"
                    value="${textContainer.text['subjectSourcesAddFormSubmitButton'] }"
                    onclick="ajax('../app/UiV2SubjectSource.editSubjectSourceSubmit?subjectSourceConfigId=${grouperRequestContainer.subjectSourceContainer.guiSubjectSourceConfiguration.subjectSourceConfiguration.configId}&subjectSourceConfigType=${grouperRequestContainer.subjectSourceContainer.guiSubjectSourceConfiguration.subjectSourceConfiguration['class'].name}', {formIds: 'sourceConfigDetails'}); return false;">
                   &nbsp;
              <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2SubjectSource.viewSubjectSources'); return false;"
                          >${textContainer.text['subjectSourcesAddFormCancelButton'] }</a>
            
            </div>
            
          </form>
	  	
	  </div>
	</div>
 

           
           
