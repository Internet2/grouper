<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <div class="btn-group btn-block">
                    
                      <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreProvisioningActions']}" id="provisioning-more-action-button" aria-controls="provisioning-more-options" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" onclick="$('#grouperTypes-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#provisioning-more-options li').first().focus();return true;});">
                          ${textContainer.text['provisioningMoreActionsButton'] } <span class="caret"></span></button>

                      <ul class="dropdown-menu dropdown-menu-right" id="provisioning-more-options">

                        <c:if test="${grouperRequestContainer.provisioningContainer.canReadProvisioningForMembership}" >
                          <li><a href="?operation=UiV2Provisioning.viewProvisioningOnSubjectMembership&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Provisioning.viewProvisioningOnSubjectMembership&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['provisioningMoreActionsViewSettings'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.provisioningContainer.canRunDaemon}" >
	                        <li><a href="?operation=UiV2Provisioning.editProvisioningOnSubjectMembership&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Provisioning.editProvisioningOnSubjectMembership&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
	                            >${textContainer.text['provisioningMoreActionsEditSettings'] }</a></li>
                        </c:if>

                      </ul>
                    </div>