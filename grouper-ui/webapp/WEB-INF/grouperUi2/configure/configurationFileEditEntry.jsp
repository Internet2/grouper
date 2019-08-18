            <div class="row-fluid" id="configurationMainDivId">
            
              <div id="configuration-select-container">
               <form id="configurationSelectForm" class="form-horizontal" method="post" action="UiV2Configure.configure" >
                 <div class="control-group">
                   <label class="control-label">${textContainer.text['configurationSelectConfigFile'] }</label>
                   <div class="controls">
                     <%-- --%>
                     <select id="configFileSelect" class="span4" name="configFile" 
                            >
                        <option value=""></option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_CACHE_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_CACHE_PROPERTIES">grouper.cache.properties</option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_CLIENT_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_CLIENT_PROPERTIES">grouper.client.properties</option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_LOADER_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_LOADER_PROPERTIES">grouper-loader.properties</option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_PROPERTIES">grouper.properties</option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_UI_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_UI_PROPERTIES">grouper-ui.properties</option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_WS_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_WS_PROPERTIES">grouper-ws.properties</option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'SUBJECT_PROPERTIES' ? 'selected="selected"' : '' } value="SUBJECT_PROPERTIES">subject.properties</option>
                     </select>
                     
                     <span class="help-block">${textContainer.text['configurationFilesAddEntrySelectConfigFileDescription'] }</span>
                   
                   </div>
                 </div>
                 
                 <div class="control-group">
                   <label class="control-label">${textContainer.text['configurationFilesAddEntryPropertyName'] }</label>
                   <div class="controls">
                     <input type="text" id="propertyNameId" class="span6" name="propertyNameName" />
                     
                     <span class="help-block">${textContainer.text['configurationFilesAddEntryPropertyNameDescription'] }</span>
                   
                   </div>
                 </div>

                 <div class="control-group">
                   <label class="control-label">${textContainer.text['configurationFilesAddEntryExpressionLanguage'] }</label>
                   <div class="controls">
                     <select id="expressionLanguageId" class="span6" name="expressionLanguageName">
                       <option value="false">${textContainer.text['configurationFilesAddEntryExpressionLanguageFalse'] }</option>
                       <option value="true">${textContainer.text['configurationFilesAddEntryExpressionLanguageTrue'] }</option>
                     
                     </select>
                     
                     <span class="help-block">${textContainer.text['configurationFilesAddEntryExpressionLanguageDescription'] }</span>
                   
                   </div>
                 </div>

                 <div class="control-group">
                   <label class="control-label">${textContainer.text['configurationFilesAddEntryValue'] }</label>
                   <div class="controls">
                     <input type="text" id="valueId" class="span6" name="valueName" />
                     
                     <span class="help-block">${textContainer.text['configurationFilesAddEntryValueDescription'] }</span>
                   
                   </div>
                 </div>

                 <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Configure.configurationFileAddConfigSubmit', {formIds: 'configurationSelectForm'}); return false;">${textContainer.text['configurationFilesAddEntrySubmit'] }</a> 
                 <a href="#" onclick="return guiV2link('operation=UiV2Configure.configure&configFile=${grouperRequestContainer.configurationContainer.configFileName}');" class="btn btn-cancel">${textContainer.text['configurationFilesAddEntryCancel'] }</a></div>

                </form>
             
              </div>
            </div>
