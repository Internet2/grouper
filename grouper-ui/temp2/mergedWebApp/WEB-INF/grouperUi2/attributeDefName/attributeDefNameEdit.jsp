<%@ include file="../assetsJsp/commonTaglib.jsp"%>


            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-cog"></i> ${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.displayExtension)}
                <br /><small>${textContainer.text['attributeDefNameEditTitle'] }</small></h1>
              </div>

            </div>

            <div class="row-fluid">
              <div class="span12">
                <form id="editAttributeDefNameForm" class="form-horizontal">
                
                  <input type="hidden" name="attributeDefNameId" value="${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}" />
                  
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['attributeDefNameEditAttributeDefLabel'] }</label>
                    <div class="controls">
                      <span>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.shortLinkWithIcon}</span>
                      <span class="help-block">${textContainer.text['attributeDefNameEditIntoAttributeDefDescription'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="folder-path" class="control-label">${textContainer.text['attributeDefNameEditFolderLabel'] }</label>
                    <div class="controls">
                      <span>${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.guiStem.shortLinkWithIcon}</span>
                      <span class="help-block">${textContainer.text['attributeDefNameEditIntoFolderDescription'] }</span>
                    </div>
                  </div>

                  <div class="control-group">
                    <label for="attributeDefNameId" class="control-label">${textContainer.text['attributeDefNameIdLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="name" name="attributeDefNameToEditExtension" 
                        value="${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.extension)}" />
                      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                      <span class="help-block">${textContainer.text['attributeDefNameIdDescription'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="name" class="control-label">${textContainer.text['attributeDefNameNameLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="name" name="attributeDefNameToEditDisplayExtension" 
						value="${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.displayExtension)}" />
						 <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                    		data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                      <span class="help-block">${textContainer.text['attributeDefNameNameDescription'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="attributeDefNameDescription" class="control-label">${textContainer.text['attributeDefNameDescriptionLabel'] }</label>
                    <div class="controls">
                      <textarea id="attributeDefNameDescription" name=attributeDefNameToEditDescription rows="3" cols="40" class="input-block-level">${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.description)}</textarea>
                      <span class="help-block">${textContainer.text['attributeDefNameDescriptionDescription'] }</span>
                    </div>
                  </div>
                 
                  <div class="form-actions">
                    <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2AttributeDefName.attributeDefNameEditSubmit', {formIds: 'editAttributeDefNameForm'}); return false;">${textContainer.text['attributeDefNameEditSaveButton'] }</a> 
                    <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2AttributeDefName.viewAttributeDefName&attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}');" >${textContainer.text['attributeDefNameDeleteCancelButton'] }</a>
                  </div>
                </form>
              </div>
            </div>
            
            
            