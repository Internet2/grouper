<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-group"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${textContainer.text['groupEditCompositeTitle'] }</small></h1>
              </div>

            </div>
            
            <div id="left-group-search" tabindex="-1" role="dialog" aria-labelledby="left-group-search-label" aria-hidden="true" class="modal hide fade">
              <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                <h3 id="left-group-search-label">${textContainer.text['groupCompositeLeftFactorSearchTitle']}</h3>
              </div>

              <div class="modal-body">
                <form class="form form-inline" id="leftFactorSearchFormId">
                  <input id="leftFactorSearchId" name="leftFactorSearchName" type="text" placeholder="${textContainer.text['groupCompositeLeftFactorPlaceholder']}" />
                  <button class="btn" onclick="ajax('../app/UiV2Group.leftGroupFactorSearch', {formIds: 'leftFactorSearchFormId'}); return false;" >${textContainer.text['groupCompositeSearchButton'] }</button>
                  <br />
                  <span style="white-space: nowrap;"><input type="checkbox" name="matchExactId" value="true"/> ${textContainer.text['groupCompositeSearchExactIdMatch'] }</span>
                </form>
                <div id="leftFactorGroupResults">
                </div>
              </div>
              <div class="modal-footer">
                <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['groupCompositeSearchCloseButton']}</button>
              </div>
            </div>
            
            
            <div class="row-fluid">
              <div class="span12">
                <form id="editGroupForm" class="form-horizontal">

                  <div class="control-group">
                    <label for="groupComposite" class="control-label">${textContainer.text['groupEditComposite'] }</label>
                    <div class="controls">
                      <label class="radio">
                        <input type="radio" name="groupComposite" id="composite_no" value="false" 
                          onclick="if($('input[name=groupComposite]:checked').val() == 'false') {$('.compositeDivClass').hide('slow');} return true;"
                          ${!grouperRequestContainer.groupContainer.guiGroup.group.hasComposite ? 'checked="checked"' : '' }  
                        >${textContainer.text['groupEditCompositeNo'] }
                      </label>
                      <label class="radio">
                        <input type="radio" name="groupComposite" id="composite_yes" value="true" 
                          onclick="if($('input[name=groupComposite]:checked').val() == 'true') {$('.compositeDivClass').show('slow');} return true;"
                          ${grouperRequestContainer.groupContainer.guiGroup.group.hasComposite ? 'checked="checked"' : '' }  
                        >${textContainer.text['groupEditCompositeYes'] }
                      </label>
                    </div>
                  </div>
                  <div class="control-group compositeDivClass" id="groupEditCompositeOfId" 
                      ${grouperRequestContainer.groupContainer.guiGroup.group.hasComposite ? '' : 'style="display:none"' }  >
                    <label for="groupCompositeLeftFactorComboId" class="control-label">${textContainer.text['groupCompositeLeftFactor'] }</label>
                    <div class="controls">
                    
                      <%-- placeholder: Enter the name of a group --%>
                      <grouper:combobox2 idBase="groupCompositeLeftFactorCombo" style="width: 30em"
                        filterOperation="../app/UiV2Group.groupCompositeFactorFilter"/>
                    
                      <span class="help-block">${textContainer.text['groupCompositeLeftFactorComboLabel']}</span>
                    </div>
                  </div>
                  <div class="control-group compositeDivClass" id="groupEditCompositeOfId" 
                      ${grouperRequestContainer.groupContainer.guiGroup.group.hasComposite ? '' : 'style="display:none"' }  >
                    <label for="groupCompositeLeftFactorComboId" class="control-label">${textContainer.text['groupCompositeOperationLabel'] }</label>
                    <div class="controls">

                      <select>
                        <option>UNION</option>
                        <option>INTERSECT</option>
                        <option>MINUS</option>
                      </select>
                      <span class="help-block">Composite operation</span>
                      <br />
                      <input type="text" id="rightFactorId" name="rightFactor"
                        value="some:folder:anotherGroup"
                        /><span class="help-block">Right factor group</span>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Group.groupEditCompositeSubmit', {formIds: 'editGroupForm'}); return false;">${textContainer.text['groupCompositeSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" class="btn btn-cancel">${textContainer.text['groupCompositeCancelButton'] }</a></div>
                </form>
              </div>
            </div>
            
            
            