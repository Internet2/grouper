<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.parentUuid}" />

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['guiBreadcrumbsHomeLabel'] } </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['stemAssignAttributeBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['stemAssignAttributeTitle'] }</h1>
              </div>
            </div>
            
            <div class="row-fluid">
              <div class="span12">
              	
                <div id="folder-search" tabindex="-1" role="dialog" aria-labelledby="stem-search-label" aria-hidden="true" class="modal hide fade">
                  <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                    <h3 id="stem-search-label">${textContainer.text['stemCreateSearchForFolderTitle'] }</h3>
                  </div>
                  <div class="modal-body">
                    <form class="form form-inline" id="stemSearchFormId">
                      <input name="stemSearch" type="text" placeholder="${textContainer.textEscapeXml['stemCreateSearchPlaceholder'] }" value=""/> 
                      <button class="btn" onclick="ajax('../app/UiV2Stem.stemSearchFormSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemSearchFormId'}); return false;">${textContainer.text['stemCreateSearchButton'] }</button>
                    </form>
                    <div id="folderSearchResultsId"></div>
                    
                  </div>
                  <div class="modal-footer">
                    <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['stemCreateSearchClose'] }</button>
                  </div>
                </div>
                
                <form id="assignAttributeStemForm" class="form-horizontal">

                  <input type="hidden" name="stemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
                  
                  <div class="control-group">
                    <label for="folder-path" class="control-label">${textContainer.text['stemAssignAttributeOwnerFolderLabel'] }</label>
                    <div class="controls">
                      <%-- placeholder: Enter a folder name --%>
                      <grouper:combobox2 idBase="parentFolderCombo" style="width: 30em" 
                        filterOperation="../app/UiV2Stem.createStemParentFolderFilter"
                        value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.objectStemId)}"
                        />
                      <span class="help-block">${textContainer.text['stemAssignAttributeOwnerFolderDescription'] }</span>
                    
                    </div>
                  </div>
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['stemAssignAttributeAttributeDefLabel'] }</label>
                    <div class="controls">
                      <input type="hidden" name="attributeAssignType" value="stem" /> 	                    	
                      <grouper:combobox2 idBase="attributeDefCombo" style="width: 30em"
                        filterOperation="UiV2AttributeDef.attributeDefFilter"
                        additionalFormElementNames="attributeAssignType"
                        value="${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.objectAttributeDefId)}"
                        />
                      <span class="help-block">${textContainer.text['stemAssignAttributeAttributeDefDescription'] }</span>
                    
                    </div>
                  </div>
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['stemAssignAttributeAttributeDefNameLabel'] }</label>
                    <div class="controls">
                    	                    	
                      <grouper:combobox2 idBase="attributeDefNameCombo" style="width: 30em"
                        filterOperation="UiV2AttributeDefName.attributeDefNameFilter"
                        additionalFormElementNames="attributeDefComboName,attributeAssignType"
                        value="${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.objectAttributeDefId)}"
                        />
                      <span class="help-block">${textContainer.text['stemAssignAttributeAttributeDefNameDescription'] }</span>
                    
                    </div>
                  </div>
                  
                  <div class="control-group">
                  	<label class="control-label">${textContainer.text['stemAssignAttributeEnabledDateLabel'] }</label>
                  	<div class="controls">
                  		<input id="enabled-date" name="enabledDate" placeholder="${textContainer.text['stemAssignAttributeDatePlaceholder'] }">
                  		<span class="help-block">${textContainer.text['stemAssignAttributeEnabledDateDescription'] }</span>
                  	</div>
                  </div>
                  
                  <div class="control-group">
                  	<label class="control-label">${textContainer.text['stemAssignAttributeDisabledDateLabel'] }</label>
                  	<div class="controls">
                  		<input id="disabled-date" name="disabledDate" placeholder="${textContainer.text['stemAssignAttributeDatePlaceholder'] }">
                  		<span class="help-block">${textContainer.text['stemAssignAttributeDisabledDateDescription'] }</span>
                  	</div>
                  </div>
                  
                  <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Stem.assignAttributeSubmit', {formIds: 'assignAttributeStemForm'}); return false;">${textContainer.text['groupCreateSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');" class="btn btn-cancel" role="button">${textContainer.text['stemCreateCancelButton'] }</a></div>
                </form>
              </div>
            </div>