<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['guiBreadcrumbsHomeLabel'] } </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['attributeNameNewBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['attributeNameNewTitle'] }</h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
              	<div id="folder-search" tabindex="-1" role="dialog" aria-labelledby="group-search-label" aria-hidden="true" class="modal hide fade">
                  <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                    <h3 id="group-search-label">${textContainer.text['attributeDefNameCreateSearchForFolderTitle'] }</h3>
                  </div>
                  <div class="modal-body">
                    <form class="form form-inline" id="stemSearchFormId">
                      <input name="stemSearch" type="text" placeholder="${textContainer.textEscapeXml['attributeDefNameCreateSearchPlaceholder'] }" value=""/> 
                      <button class="btn" onclick="ajax('../app/UiV2Stem.stemSearchAttributeDefNameFormSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemSearchFormId'}); return false;">${textContainer.text['attributeDefNameCreateSearchButton'] }</button>
                    </form>
                    <div id="folderSearchResultsId"></div>
                    
                  </div>
                  <div class="modal-footer">
                    <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['attributeDefNameCreateSearchClose'] }</button>
                  </div>
                </div>
              
                <form id="addAttributeNameForm" class="form-horizontal">
                
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['attributeDefNameCreateAttributeDefLabel'] }</label>
                    <div class="controls">
                    	                    	
                      <grouper:combobox2 idBase="attributeDefCombo" style="width: 30em" 
                        filterOperation="UiV2AttributeDef.createAttributeDefNameParentAttributeDefFilter"
                        value="${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.objectAttributeDefId)}"
                        />
                      <span class="help-block">${textContainer.text['attributeDefNameCreateIntoAttributeDefDescription'] }</span>
                    
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="folder-path" class="control-label">${textContainer.text['attributeDefNameCreateFolderLabel'] }</label>
                    <div class="controls">
                      <%-- placeholder: Enter a folder name --%>
                      <grouper:combobox2 idBase="parentFolderCombo" style="width: 30em" 
                        filterOperation="../app/UiV2Stem.createGroupParentFolderFilter"
                        value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.objectStemId)}"
                        />
                      <span class="help-block">${textContainer.text['attributeDefNameCreateIntoFolderDescription'] }</span>
                    
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="name" class="control-label">${textContainer.text['attributeDefNameNameLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="name" name="attributeDefNameToEditDisplayExtension" 
                        onkeyup="syncNameAndId('name', 'attributeDefNameId', 'nameDifferentThanIdId', false, null); return true;"
                       />
                       <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                    		data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                      <span class="help-block">${textContainer.text['attributeDefNameNameDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="attributeDefNameId" class="control-label">${textContainer.text['attributeDefNameIdLabel'] }</label>
                    <div class="controls">
                      <span onclick="syncNameAndId('name', 'attributeDefNameId', 'nameDifferentThanIdId', true, '${textContainer.textEscapeXml['groupNewAlertWhenClickingOnDisabledId']}'); return true;">
                        <input type="text" id="attributeDefNameId" name="attributeDefNameToEditExtension" disabled="disabled"  />
                      </span>
                      <span style="white-space: nowrap;">
                        <input type="checkbox" name="nameDifferentThanId" id="nameDifferentThanIdId" value="true"
                          onchange="syncNameAndId('name', 'attributeDefNameId', 'nameDifferentThanIdId', false, null); return true;"
                        /> ${textContainer.text['groupNewEditTheId'] }
                      </span>
                      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                    		data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                      <span class="help-block">${textContainer.text['attributeDefNameIdDescription'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="attributeDefNameDescription" class="control-label">${textContainer.text['attributeDefNameDescriptionLabel'] }</label>
                    <div class="controls">
                      <textarea id="attributeDefNameDescription" name=attributeDefNameToEditDescription rows="3" cols="40" class="input-block-level"></textarea>
                      <span class="help-block">${textContainer.text['attributeDefNameDescriptionDescription'] }</span>
                    </div>
                  </div>
                 
                  <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2AttributeDef.newAttributeDefNameSubmit', {formIds: 'addAttributeNameForm'}); return false;">${textContainer.text['attributeDefNameCreateSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');" class="btn btn-cancel" role="button">${textContainer.text['attributeDefNameCreateCancelButton'] }</a></div>
                </form>
              </div>
            </div>
            
            
            
