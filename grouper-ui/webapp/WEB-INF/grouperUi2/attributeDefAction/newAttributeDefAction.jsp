<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['guiBreadcrumbsHomeLabel'] } </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['attributeActionNewBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['attributeActionNewTitle'] }</h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <form id="addAttributeActionForm" class="form-horizontal">
                
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['attributeDefActionCreateAttributeDefLabel'] }</label>
                    <div class="controls">
                    	                    	
                      <grouper:combobox2 idBase="attributeDefCombo" style="width: 30em" 
                        filterOperation="UiV2AttributeDef.attributeDefFilter"
                        value="${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.objectAttributeDefId)}"
                        />
                      <span class="help-block">${textContainer.text['attributeDefActionCreateIntoAttributeDefDescription'] }</span>
                    
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="action" class="control-label">${textContainer.text['attributeDefActionNameLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="action" name="action" />
                       <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                    		data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                    </div>
                  </div>
                 
                  <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2AttributeDefAction.newAttributeDefActionSubmit', {formIds: 'addAttributeActionForm'}); return false;">${textContainer.text['attributeDefActionCreateSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');" class="btn btn-cancel" role="button">${textContainer.text['attributeDefActionCreateCancelButton'] }</a></div>
                </form>
              </div>
            </div>
            
            
            
