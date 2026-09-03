<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start grouperTypes/grouperTypesFolderMoreActionsButtonContents.jsp -->

                    <div class="btn-group btn-block">
                    
                      <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreObjectTypeActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" onclick="$('#grouperTypes-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#grouperTypes-more-options li').first().focus();return true;});">
                          ${textContainer.text['objectTypeMoreActionsButton'] } <span class="caret"></span></button>

                      <ul class="dropdown-menu dropdown-menu-right" id="grouperTypes-more-options">

                        <c:if test="${grouperRequestContainer.objectTypeContainer.canReadObjectType}" >
                          <li><a href="?operation=UiV2GrouperObjectTypes.viewObjectTypesOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperObjectTypes.viewObjectTypesOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['objectTypeMoreActionsViewSettings'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.objectTypeContainer.canWriteObjectType}" >
	                        <li><a href="?operation=UiV2GrouperObjectTypes.editObjectTypesOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperObjectTypes.editObjectTypesOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
	                            >${textContainer.text['objectTypeMoreActionsEditSettings'] }</a></li>
                        </c:if>
                        
                        <c:if test="${grouperRequestContainer.objectTypeContainer.canWriteObjectType}" >
                          <li><a href="?operation=UiV2GrouperObjectTypes.findAutoAssignTypes&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperObjectTypes.findAutoAssignTypes&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['objectTypeMoreActionsAutoAssignTypes'] }</a></li>
                        </c:if>
                        
                      </ul>
                    </div>

                    <!-- end grouperTypes/grouperTypesFolderMoreActionsButtonContents.jsp -->
