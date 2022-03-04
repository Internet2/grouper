<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Configure.index');">${textContainer.text['miscellaneousConfigureBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousConfigurationFilesBreadcrumb'] }</li>
              </ul>
                            
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h1>${textContainer.text['miscellaneousConfigurationMainDescription'] }</h1></div>
                  <div class="span2 pull-right">
                    <%@ include file="configureFilesMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                <div class="row-fluid">
                  <div class="span12">
                    <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousConfigurationMainSubtitle']}</p>
                  </div>
                </div>

                <div id="configuration-select-container">
                 <form id="configurationSelectForm" class="form-horizontal" method="post" action="UiV2Configure.configure" >
                   <div class="control-group">
                     <label class="control-label">${textContainer.text['configurationSelectConfigFile'] }</label>
                     <div class="controls">
                       <%-- --%>
                       <select id="configFileSelect" class="span4" name="configFile" 
                            onchange="guiV2link('operation=UiV2Configure.configureSelectFile', {optionalFormElementNamesToSend: 'configFile'}); return false;"
                            <%--   onchange="return guiV2link('operation=UiV2Configure.configure', {optionalFormElementNamesToSend: 'configFile'});" --%>
                              >
                          <option value=""></option>
                          <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_CACHE_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_CACHE_PROPERTIES">grouper.cache.properties</option>
                          <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_CLIENT_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_CLIENT_PROPERTIES">grouper.client.properties</option>
                          <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_LOADER_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_LOADER_PROPERTIES">grouper-loader.properties</option>
                          <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_PROPERTIES">grouper.properties</option>
                          <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_TEXT_EN_US_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_TEXT_EN_US_PROPERTIES">grouper.text.en.us.properties</option>
                          <c:if test="${ grouperRequestContainer.configurationContainer.hasFrench }">
                            <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_TEXT_FR_FR_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_TEXT_FR_FR_PROPERTIES">grouper.text.fr.fr.properties</option>
                          </c:if>
                          <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_UI_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_UI_PROPERTIES">grouper-ui.properties</option>
                          <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_WS_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_WS_PROPERTIES">grouper-ws.properties</option>
                          <option ${grouperRequestContainer.configurationContainer.configFileName == 'SUBJECT_PROPERTIES' ? 'selected="selected"' : '' } value="SUBJECT_PROPERTIES">subject.properties</option>
                       </select>
                       
                       <span class="help-block">${textContainer.text['configurationSelectConfigFileDescription'] }</span>
                     
                     </div>
                   </div>
                   
                  </form>
               
                </div>

              </div>
              
              <!-- a <div class="row-fluid">
                     
                    </div> -->
              
            </div>

            <div class="row-fluid" id="configurationMainDivId">
            
              <c:if test="${grouperRequestContainer.configurationContainer.configFileName != null}">
              	
              	<form id="configureFilterForm" class="form-inline form-small form-filter">
              	
              		<div class="row-fluid">
	                    <div class="span1">
	                      <span><label for="config-source-filter" style="white-space: nowrap;">${textContainer.text['configSourceFilterFor'] }</label></span>
	                    </div>
	                    <div class="span4">
	                      <select id="config-source-filter" name="configSource" class="span12">
	                        <option value="" >${textContainer.text['configSourceAll']}</option>
	                        <option ${grouperRequestContainer.configurationContainer.configSource == 'nonBase' ? 'selected="selected"' : '' } value="nonBase">${textContainer.text['configSourceNonBase']}</option>
	                        <option ${grouperRequestContainer.configurationContainer.configSource == 'db' ? 'selected="selected"' : '' } value="db">${textContainer.text['configSourceDbConfigOnly']}</option>
	                      </select>
	                    </div>
	                  </div>
              		
              		
              		<div class="row-fluid" style="margin-top: 5px;">
						<div class="span1"></div>              			              		             		
						<div class="span4">
	                      <input type="text" placeholder="${textContainer.textEscapeXml['configurationFilterTextPlaceholder']}" 
	                         name="filter" id="table-filter" class="span12" value="${grouperRequestContainer.configurationContainer.filter}" />
	                    </div>
						<div class="span4">
							<input type="submit" class="btn" aria-controls="propertiesResultTableId"  id="filterSubmitId" value="${textContainer.textEscapeDouble['configurationFilterApplyButton'] }"
	                        onclick="ajax('../app/UiV2Configure.configureFilterSubmit?configFile=${grouperRequestContainer.configurationContainer.configFileName}', {formIds: 'configureFilterForm'}); return false;"> 
	                      <a class="btn" role="button" onclick="$('#table-filter').val(''); $('#config-source-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['configurationFilterResetButton'] }</a>
	                    </div>
                    </div>
              	</form>
              	
              	<div class="row-fluid">
	              	<div class="span12">
	                	<h3>${grouperRequestContainer.configurationContainer.configFileName.configFileName}</h3>
	                </div>
                </div>

                  <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable" id="propertiesResultTableId">
                      <c:set var="i" value="0" />
                      <c:forEach items="${grouperRequestContainer.configurationContainer.guiConfigFile.guiConfigSections}" var="guiConfigSection">
                        
                        <c:if test="${guiConfigSection.guiConfigProperties.size() > 0}">
                        	<tbody class="config_tbody">
		                        <tr>
		                          <th colspan="4">
		                            <h4 style="margin-top: 2em">${grouper:escapeHtml(guiConfigSection.configSectionMetadata.title) }</h4>
		                            <c:if test="${! grouper:isBlank(guiConfigSection.configSectionMetadata.comment)}">
		                              <p style="font-weight: normal;">${grouper:escapeHtml(guiConfigSection.configSectionMetadata.comment.trim()) }</p>
		                            </c:if>
		                          </th>
		                        </tr>
		                        
			                    <tr>
			                        <th style="white-space: nowrap; background-color: white">
			                          ${textContainer.text['configurationColumnAction']}
			                        </th>
			                        <th style="white-space: nowrap; background-color: white">
			                          ${textContainer.text['configurationColumnPropertyName']}
			                        </th>
			                        <th style="white-space: nowrap; background-color: white">
			                          ${textContainer.text['configurationColumnValue']}
			                        </th>
			                        <th style="white-space: nowrap; background-color: white">
			                          ${textContainer.text['configurationColumnConfiguredIn']}
			                        </th>
			                    </tr>
		
		                      <c:forEach items="${guiConfigSection.guiConfigProperties}" var="guiConfigProperty">
		                        <c:set value="${guiConfigProperty.configItemMetadata}" var="configItemMetadata" />
		                        <tr id="row_${i}">
		                          <td style="vertical-align: top">
		                             
		                            <div class="btn-group"><a data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiConfigurationFilesActions']}" href="#" class="btn btn-mini dropdown-toggle"
		                              aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
		                                ${textContainer.text['groupViewActionsButton'] } 
		                                <span class="caret"></span>
		                              </a>
		                              <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
		                                <li><a href="#" onclick="return ajax('../app/UiV2Configure.configurationFileItemEdit?configFile=${grouper:escapeUrl(grouperRequestContainer.configurationContainer.configFileName.configFileName)}&propertyNameName=${grouper:escapeUrl(configItemMetadata.keyOrSampleKey)}&index=${i}');" class="actions-revoke-membership">${textContainer.text['configurationFilesActionEdit'] }</a></li>
		                                <c:if test="${guiConfigProperty.fromDatabase }">
		                                  <li><a href="#" onclick="return ajax('../app/UiV2Configure.configurationFileItemDelete?configFile=${grouper:escapeUrl(grouperRequestContainer.configurationContainer.configFileName.configFileName)}&propertyNameName=${grouper:escapeUrl(configItemMetadata.keyOrSampleKey)}');" class="actions-revoke-membership">${textContainer.text['configurationFilesActionDelete'] }</a></li>
		                                </c:if>
		                              </ul>
		                            </div>
		                          </td>
		                          <td style="vertical-align: top">
		                            <b>${grouper:escapeHtml(configItemMetadata.keyOrSampleKey)}</b><br />
		                            <c:if test="${guiConfigProperty.hasType}">
		                              <span style="font-size: 90%">${textContainer.text['configurationTypeLabel']} ${grouper:escapeHtml(guiConfigProperty.type) }</span>
		                            </c:if>
		                            <c:if test="${configItemMetadata.multiple}">
		                              <span style="font-size: 90%">${textContainer.text['configurationMultiple']}</span>
		                            </c:if>
		                            <c:if test="${! grouper:isBlank(configItemMetadata.mustExtendClass)}">
		                              <span style="font-size: 90%">${textContainer.text['configurationMustExtendClass']} ${configItemMetadata.mustExtendClass }</span>
		                            </c:if>
		                            <c:if test="${! grouper:isBlank(configItemMetadata.mustImplementInterface)}">
		                              <span style="font-size: 90%">${textContainer.text['configurationMustImplementInterface']} ${configItemMetadata.mustImplementInterface }</span>
		                            </c:if>
		                            
		                          </td>
		                          <td style="vertical-align: top">
		                            <%-- keep whitespace out of the equation to make copy/paste on screen easier --%>
		                            <b>
                                <c:choose>
                                  <%-- newlines means code so pre-format --%>
                                  <c:when test="${grouperUtil.containsNewline(guiConfigProperty.propertyValue)}">
                                     <pre><grouper:abbreviateTextarea text="${guiConfigProperty.propertyValue}"  showCharCount="50" cols="20" rows="3"/></pre>
                                  </c:when>
                                  <c:otherwise>
                                     <grouper:abbreviateTextarea text="${guiConfigProperty.propertyValue}"  showCharCount="50" cols="20" rows="3"/>
                                  </c:otherwise>
                                </c:choose>
		                            </b>
		                            
		                            <c:if test="${guiConfigProperty.scriptlet}"><br />
		                              <span style="font-size: 90%">${textContainer.text['configurationElScriptletLabel']} ${grouper:escapeHtml(guiConfigProperty.scriptletForUi) }</span>
		                            </c:if><c:if test="${! grouper:isBlank(guiConfigProperty.unprocessedValueIfDifferent)}"><br />
		                              <span style="font-size: 90%">${textContainer.text['configurationUnprocessedValueIfDifferentLabel']} ${grouper:escapeHtml(guiConfigProperty.unprocessedValueIfDifferent) }</span>
		                            </c:if><c:if test="${! grouper:isBlank(guiConfigProperty.cronDescription)}"><br />
		                              <span style="font-size: 90%">${textContainer.text['configurationCronLabel']} ${grouper:escapeHtml(guiConfigProperty.cronDescription) }</span>
		                            </c:if><c:if test="${! grouper:isBlank(configItemMetadata.sampleValue)}"><br />
		                              <span style="font-size: 90%">${textContainer.text['configurationSampleValueLabel']} ${grouper:escapeHtml(configItemMetadata.sampleValue) }</span>
		                            </c:if><c:if test="${! grouper:isBlank(configItemMetadata.comment)}"><br />
		                              <span style="font-size: 90%">${grouper:escapeHtml(configItemMetadata.comment) }</span>
		                            </c:if></td>
		                          <td style="vertical-align: top; white-space: nowrap">
		                            ${guiConfigProperty.valueFromWhere}
		                            <c:if test="${! grouper:isBlank(guiConfigProperty.baseValueIfDifferent)}">
		                              <br />
		                              <span style="font-size: 90%">${textContainer.text['configurationBaseValueIfDifferent']} ${grouper:escapeHtml(guiConfigProperty.baseValueIfDifferent) }</span>
		                            </c:if>
		                          </td>
		                        </tr>
		                        <c:set var="i" value="${i+1}" />
		                        
		                      </c:forEach>
		                      </tbody>
                        
                        </c:if>
                        
                  </c:forEach>
                  </table>

              </c:if>
            </div>
