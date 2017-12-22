<%@ include file="../assetsJsp/commonTaglib.jsp"%>


            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-cog"></i> ${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.extension)}
                </h1>
              </div>

            </div>

            <div class="row-fluid">
              <div class="span12">
              	<form class="form-horizontal">
                  <input type="hidden" name="attributeDefNameId" value="${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}" />
                  
                  <div class="control-group">
                    <label class="control-label" style="padding: 0px;">${textContainer.text['attributeDefNameEditAttributeDefLabel'] }</label>
                    <div class="controls">
                      <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.name)}</span>
                      <span class="help-block">${textContainer.text['attributeDefNameEditIntoAttributeDefDescription'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="folder-path" class="control-label" style="padding: 0px;">${textContainer.text['attributeDefNameEditFolderLabel'] }</label>
                    <div class="controls">
                      <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.stem.name)}</span>
                      <span class="help-block">${textContainer.text['attributeDefNameEditIntoFolderDescription'] }</span>
                    </div>
                  </div>

                  <div class="control-group">
                    <label for="attributeDefNameId" class="control-label" style="padding: 0px;">${textContainer.text['attributeDefNameIdLabel'] }</label>
                    <div class="controls">
                      <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.extension)}</span>
                      <span class="help-block">${textContainer.text['attributeDefNameIdDescription'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label" style="padding: 0px;">${textContainer.text['attributeDefNameNameLabel'] }</label>
                    <div class="controls">
                      <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.displayExtension)}</span>
                      <span class="help-block">${textContainer.text['attributeDefNameNameDescription'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label" style="padding: 0px;">${textContainer.text['attributeDefNameDescriptionLabel'] }</label>
                    <div class="controls">
                      <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.description)}</span>
                      <span class="help-block">${textContainer.text['attributeDefNameDescriptionDescription'] }</span>
                    </div>
                  </div>
                 
                  <div class="form-actions">
                  	<a href="#" onclick="return guiV2link('operation=UiV2AttributeDef.viewAttributeDef?attributeDefId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.attributeDefId}');" class="btn btn-cancel">${textContainer.text['attributeDefCreateCancelButton'] }</a>
                  </div>
                </form>
              </div>
            </div>