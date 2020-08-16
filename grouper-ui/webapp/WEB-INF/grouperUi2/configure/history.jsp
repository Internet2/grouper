<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Configure.index');">${textContainer.text['miscellaneousConfigureBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousConfigurationFilesHistoryBreadcrumb'] }</li>
              </ul>
                            
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h1>${textContainer.text['miscellaneousConfigurationHistoryMainDescription'] }</h1></div>
                  <div class="span2 pull-right">
                    <%@ include file="configureFilesMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                <div class="row-fluid">
                  <div class="span12">
                    <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousConfigurationHistoryMainSubtitle']}</p>
                  </div>
                </div>

              </div>
              
            </div>
            
            <div class="row-fluid" id="configurationMainDivId">
            	
              	<form id="configureHistoryFilterForm" style="height: 20px;">
					<div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['configurationFilterTextPlaceholder']}" 
                         name="filter" id="table-filter" class="span12" value="${grouperRequestContainer.configurationContainer.filter}" />
                    </div>
					<div class="span3">
						<input type="submit" class="btn" aria-controls="propertiesResultTableId"  id="filterSubmitId" value="${textContainer.textEscapeDouble['configurationFilterApplyButton'] }"
                        onclick="ajax('../app/UiV2Configure.history', {formIds: 'configureHistoryFilterForm'}); return false;"> 
                      <a class="btn" role="button" onclick="$('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['configurationFilterResetButton'] }</a>
                    </div>
              	</form>
              	
              	  <form class="form-inline form-small" name="configHistoryFormName" id="configHistoryFormId">
                  <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable" id="propertiesResultTableId">
                      
                      <thead>
                       <tr>
	                      <td colspan="8" class="table-toolbar gradient-background">
	                      <a href="#" onclick="ajax('../app/UiV2Configure.revertConfigValues', {formIds: 'configHistoryFormId,configHistoyrPagingFormId,configureHistoryFilterForm'}); return false;" class="btn" role="button">${textContainer.text['configurationHistoryRevertPropertyValues'] }</a></td>
	                    </tr>
                      
                        <tr>
                      		<th>
	                         <label class="checkbox checkbox-no-padding">
	                           <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" 
	                           	onchange="$('.configHistoryCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
	                         </label>
	                        </th>
	                        
	                        <th style="white-space: nowrap;">
	                          ${textContainer.text['configurationColumnChangeTimestamp']}
	                        </th>
	                        
	                        <th style="white-space: nowrap;">
	                          ${textContainer.text['configurationColumnConfigurationFile']}
	                        </th>
	                        
	                        <th style="white-space: nowrap;">
	                          ${textContainer.text['configurationColumnPropertyName']}
	                        </th>
	                        
	                        <th style="white-space: nowrap;">
	                          ${textContainer.text['configurationColumnPreviousValue']}
	                        </th>
	                        
	                        <th style="white-space: nowrap;">
	                          ${textContainer.text['configurationColumnNewValue']}
	                        </th>
	                        
	                        <th style="white-space: nowrap;">
	                          ${textContainer.text['configurationColumnActionLabel']}
	                        </th>
	                        
	                        <th style="white-space: nowrap;">
	                          ${textContainer.text['configurationColumnChangedBy']}
	                        </th>
	                        
	                  </tr>
	                  </thead>
	                  
	                  <c:set var="i" value="0" />
	                  
                      <c:forEach items="${grouperRequestContainer.configurationContainer.guiPitConfigs}" var="guiPitConfig">
	                      
						 <tr>
						 
						 	 <td>
	                            <label class="checkbox checkbox-no-padding">
	                               <input type="checkbox" name="configHistoryRow_${i}" aria-label="${textContainer.text['configurationHistoryRevertValueCheckboxAriaLabel'] }"
	                                  value="${guiPitConfig.pitGrouperConfigHibernate.id}" class="configHistoryCheckbox" />
	                            </label>
                         	 </td>
							 
							 <td>${grouper:escapeHtml(guiPitConfig.lastUpdated)}</td>
							 <td style="white-space: nowrap;">${grouper:escapeHtml(guiPitConfig.pitGrouperConfigHibernate.configFileName)}</td>
							 <td>${grouper:escapeHtml(guiPitConfig.pitGrouperConfigHibernate.configKey)}</td>


							 <td style="white-space: nowrap">
							 	<grouper:abbreviateTextarea text="${guiPitConfig.pitGrouperConfigHibernate.previousValue}" 
							 	showCharCount="30" cols="20" rows="3"/>
							 </td>
							 <td style="white-space: nowrap">
							 	<grouper:abbreviateTextarea text="${guiPitConfig.pitGrouperConfigHibernate.value}" 
							 	showCharCount="30" cols="20" rows="3"/>
							 </td>
							 
							 <td>
							 	
							 	<c:choose>
							 		<c:when test="${guiPitConfig.pitGrouperConfigHibernate.previousValue == null}">
							 			${textContainer.text['configurationColumnActionLabelAdd']}
							 		</c:when>
							 		<c:when test="${guiPitConfig.pitGrouperConfigHibernate.previousValue != null && guiPitConfig.pitGrouperConfigHibernate.value != null}">
							 			${textContainer.text['configurationColumnActionLabelEdit']}
							 		</c:when>
							 		<c:otherwise>
							 			${textContainer.text['configurationColumnActionLabelDelete']}
							 		</c:otherwise>
							 	</c:choose>
							 
							 </td>
							 <td style="white-space: nowrap;">
								 <c:if test="${guiPitConfig.guiSubject != null}">
									 ${guiPitConfig.guiSubject.shortLinkWithIcon}
								 </c:if>
							 </td>
	                         
                         </tr>
					
					<c:set var="i" value="${i+1}" />
                  	</c:forEach>
                  </table>
                  </form>
                  
                  <div class="data-table-bottom gradient-background">
	                <grouper:paging2 guiPaging="${grouperRequestContainer.configurationContainer.guiPaging}" formName="configHistoyrPagingForm" ajaxFormIds="configHistoryFormId"
	                    refreshOperation="../app/UiV2Configure.history" />
	              </div> 

            </div>
