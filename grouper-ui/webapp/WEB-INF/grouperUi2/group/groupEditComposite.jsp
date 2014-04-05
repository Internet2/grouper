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
            
            <div id="right-group-search" tabindex="-1" role="dialog" aria-labelledby="right-group-search-label" aria-hidden="true" class="modal hide fade">
              <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                <h3 id="right-group-search-label">${textContainer.text['groupCompositeRightFactorSearchTitle']}</h3>
              </div>

              <div class="modal-body">
                <form class="form form-inline" id="rightFactorSearchFormId">
                  <input id="rightFactorSearchId" name="rightFactorSearchName" type="text" placeholder="${textContainer.text['groupCompositeRightFactorPlaceholder']}" />
                  <button class="btn" onclick="ajax('../app/UiV2Group.rightGroupFactorSearch', {formIds: 'rightFactorSearchFormId'}); return false;" >${textContainer.text['groupCompositeSearchButton'] }</button>
                  <br />
                  <span style="white-space: nowrap;"><input type="checkbox" name="matchExactId" value="true"/> ${textContainer.text['groupCompositeSearchExactIdMatch'] }</span>
                </form>
                <div id="rightFactorGroupResults">
                </div>
              </div>
              <div class="modal-footer">
                <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['groupCompositeSearchCloseButton']}</button>
              </div>
            </div>
            
            
            <div class="row-fluid">
              <div class="span12">
                <form id="editGroupCompositeForm" class="form-horizontal">

                  <c:set var="hasComposite" 
                    value="${grouperRequestContainer.groupContainer.guiGroup.group.hasComposite}" />


                  <div class="control-group">
                    <label for="groupComposite" class="control-label">${textContainer.text['groupEditComposite'] }</label>
                    <div class="controls">
                      <label class="radio">
                        <input type="radio" name="groupComposite" id="composite_no" value="false" 
                          onclick="if($('input[name=groupComposite]:checked').val() == 'false') {$('.compositeDivClass').hide('slow');} return true;"
                          ${!hasComposite ? 'checked="checked"' : '' }  
                        >${textContainer.text['groupEditCompositeNo'] }
                      </label>
                      <label class="radio">
                        <input type="radio" name="groupComposite" id="composite_yes" value="true" 
                          onclick="if($('input[name=groupComposite]:checked').val() == 'true') {$('.compositeDivClass').show('slow');} return true;"
                          ${hasComposite ? 'checked="checked"' : '' }  
                        >${textContainer.text['groupEditCompositeYes'] }
                      </label>
                    </div>
                  </div>
                  <div class="control-group compositeDivClass" id="groupEditCompositeOfId" 
                      ${hasComposite ? '' : 'style="display:none"' }  >
                    <label for="groupCompositeLeftFactorComboId" class="control-label">${textContainer.text['groupCompositeLeftFactor'] }</label>
                    <div class="controls">
                    
                      <%-- placeholder: Enter the name of a group --%>
                      <grouper:combobox2 idBase="groupCompositeLeftFactorCombo" style="width: 30em"
                        value="${grouperRequestContainer.groupContainer.compositeLeftFactorGuiGroup.group.uuid}"
                        filterOperation="../app/UiV2Group.groupCompositeFactorFilter"/>
                      <span class="help-block">${textContainer.text['groupCompositeLeftFactorComboLabel']}</span>
                    </div>
                  </div>
                  <div class="control-group compositeDivClass" id="groupEditCompositeOfId" 
                      ${hasComposite ? '' : 'style="display:none"' }  >
                    <label for="groupCompositeLeftFactorComboId" class="control-label">${textContainer.text['groupCompositeOperationLabel'] }</label>
                    <div class="controls">
                      <c:set var="compositeType" 
                        value="${hasComposite ? grouperRequestContainer.groupContainer.guiGroup.composite.type.name : null}" />
                      <select name="compositeOperation" id="compositeOperationId">
                        <option value="">${textContainer.text['groupCompositeOperationDefault'] }</option>
                        <option value="intersection"  
                           ${compositeType == 'intersection' ? 'selected="selected"' : '' }
                        >${textContainer.text['intersection']}</option>
                        <option value="complement"
                           ${compositeType == 'complement' ? 'selected="selected"' : '' }
                        >${textContainer.text['complement']}</option>
                        <c:if test="${compositeType == 'UNION'}">
                          <option value="union" 
                            ${compositeType == 'union' ? 'selected="selected"' : '' }
                          >${textContainer.text['union']}</option>
                        </c:if>
                      </select>
                      <span class="help-block">${textContainer.text['groupCompositeOperationHelp']}</span>
                    </div>
                  </div>
                  <div class="control-group compositeDivClass" id="groupEditCompositeOfId" 
                      ${hasComposite ? '' : 'style="display:none"' }  >
                    <label for="groupCompositeRightFactorComboId" class="control-label">${textContainer.text['groupCompositeRightFactor'] }</label>
                    <div class="controls">

                      <%-- placeholder: Enter the name of a group --%>
                      <grouper:combobox2 idBase="groupCompositeRightFactorCombo" style="width: 30em"
                        value="${grouperRequestContainer.groupContainer.compositeRightFactorGuiGroup.group.uuid}"
                        filterOperation="../app/UiV2Group.groupCompositeFactorFilter"/>
                      <span class="help-block">${textContainer.text['groupCompositeRightFactorComboLabel']}</span>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Group.groupEditCompositeSubmit', {formIds: 'editGroupCompositeForm'}); return false;">${textContainer.text['groupCompositeSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" class="btn btn-cancel">${textContainer.text['groupCompositeCancelButton'] }</a></div>
                  <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.uuid}" />
                </form>
              </div>
            </div>
            
            
            