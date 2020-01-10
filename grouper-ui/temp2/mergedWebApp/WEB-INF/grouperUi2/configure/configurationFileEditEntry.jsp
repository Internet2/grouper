  <%@ include file="../assetsJsp/commonTaglib.jsp"%>
  
  <td colspan="4" style="vertical-align: top">
            <div class="row-fluid">
            
              <div id="configuration-select-container">
               <form id="configurationEditForm" class="form-horizontal" method="post" action="UiV2Configure.configure" >
                 <input type="hidden" name="configFile" value="${grouperRequestContainer.configurationContainer.configFileName}" />
                 <input type="hidden" name="propertyNameName" value="${grouperRequestContainer.configurationContainer.currentConfigPropertyName}" />

                 <div class="control-group">
                   <label class="control-label">${textContainer.text['configurationFilesAddEntryExpressionLanguage'] }</label>
                   <div class="controls">
                     <select id="expressionLanguageId" class="span6" name="expressionLanguageName">
                       <option value="false" ${!grouperRequestContainer.configurationContainer.currentGuiConfigProperty.scriptlet ? 'selected="selected"' : '' } 
                          >${textContainer.text['configurationFilesAddEntryExpressionLanguageFalse'] }</option>
                       <option value="true"  ${grouperRequestContainer.configurationContainer.currentGuiConfigProperty.scriptlet ? 'selected="selected"' : '' }
                          >${textContainer.text['configurationFilesAddEntryExpressionLanguageTrue'] }</option>
                     
                     </select>
                     
                     <span class="help-block">${textContainer.text['configurationFilesAddEntryExpressionLanguageDescription'] }</span>
                   
                   </div>
                 </div>
                 <div class="control-group">
                   <label class="control-label">${textContainer.text['configurationFilesAddEntryPasswordLabel'] }</label>
                   <div class="controls">
                     <select id="passwordId" class="span6" name="passwordName" onchange="return ajax('../app/UiV2Configure.configurationFileSelectPassword', {formIds: 'configurationEditForm'}); return false;">
                       <option value="false" ${!grouperRequestContainer.configurationContainer.currentGuiConfigProperty.encryptedInDatabase ? 'selected="selected"' : '' }
                         >${textContainer.text['configurationFilesAddEntryPasswordFalse'] }</option>
                       <option value="true" ${grouperRequestContainer.configurationContainer.currentGuiConfigProperty.encryptedInDatabase ? 'selected="selected"' : '' }
                         >${textContainer.text['configurationFilesAddEntryPasswordTrue'] }</option>
                     
                     </select>
                     
                     <span class="help-block">${textContainer.text['configurationFilesAddEntryPasswordDescription'] }</span>
                   
                   </div>
                 </div>
                <div class="control-group" id="passwordValueDivId" 
                   style="display: ${grouperRequestContainer.configurationContainer.currentGuiConfigProperty.encryptedInDatabase ? 'block' : 'none'}">
                 <label class="control-label">${textContainer.text['configurationFilesAddEntryPasswordFieldLabel'] }</label>
                 <div class="controls">
                   <input type="password" id="passwordValueId" class="span6" name="passwordValueName" />
                   
                   <span class="help-block">${textContainer.text['configurationFilesAddEntryPasswordFieldDescription'] }</span>
                 
                 </div>
               </div>
               <div class="control-group" id="valueDivId" 
                   style="display: ${!grouperRequestContainer.configurationContainer.currentGuiConfigProperty.encryptedInDatabase ? 'block' : 'none'}">
                 <label class="control-label">${textContainer.text['configurationFilesAddEntryValue'] }</label>
                 <div class="controls">
                   <input type="text" id="valueId" class="span6" name="valueName" 
                     value="${grouper:escapeHtml(grouperRequestContainer.configurationContainer.currentGuiConfigProperty.configItemMetadata.sampleProperty ? '' : 
                         (grouperRequestContainer.configurationContainer.currentGuiConfigProperty.scriptlet ?
                         grouperRequestContainer.configurationContainer.currentGuiConfigProperty.scriptletForUi : 
                         grouperRequestContainer.configurationContainer.currentGuiConfigProperty.propertyValue))}" />
                   <span class="help-block">${textContainer.text['configurationFilesAddEntryValueDescription'] }</span>
                 </div>
               </div>

                 <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Configure.configurationFileItemEditSubmit', {formIds: 'configurationEditForm'}); return false;">${textContainer.text['configurationFilesAddEntrySubmit'] }</a> 
                 <a href="#" onclick="$('.configFormRow').hide('slow');$('.configFormRow').remove();return false;" class="btn btn-cancel">${textContainer.text['configurationFilesAddEntryCancel'] }</a></div>

                </form>
             
              </div>
            </div>
</td>