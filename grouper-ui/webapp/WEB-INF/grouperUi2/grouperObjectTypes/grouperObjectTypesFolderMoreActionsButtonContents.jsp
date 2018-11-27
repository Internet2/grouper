<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start grouperTypes/grouperTypesFolderMoreActionsButtonContents.jsp -->

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreObjectTypeActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#grouperTypes-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#grouperTypes-more-options li').first().focus();return true;});">
                          ${textContainer.text['objectTypeMoreActionsButton'] } <span class="caret"></span></a>

                      <ul class="dropdown-menu dropdown-menu-right" id="grouperTypes-more-options">

                        <c:if test="${grouperRequestContainer.objectTypeContainer.canReadObjectType}" >
                          <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperObjectTypes.viewObjectTypesOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['objectTypeMoreActionsViewSettings'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.objectTypeContainer.canWriteObjectType}" >
	                        <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperObjectTypes.editObjectTypesOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
	                            >${textContainer.text['objectTypeMoreActionsEditSettings'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.objectTypeContainer.canRunDaemon}" >
                          <li><a href="#" onclick="ajax('../app/UiV2Deprovisioning.runDaemon'); return false;"
                              >${textContainer.text['groupObjectTypeMoreActionsRunDaemon'] }</a></li>
                        </c:if>

                      </ul>
                    </div>

                    <!-- end grouperTypes/grouperTypesFolderMoreActionsButtonContents.jsp -->
