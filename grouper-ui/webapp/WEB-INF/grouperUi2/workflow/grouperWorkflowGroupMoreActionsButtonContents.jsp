<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <div class="btn-group btn-block">
                    
                      <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreGrouperWorkflowActions']}" id="grouper-workflow-more-action-button" aria-controls="grouper-workflow-more-options" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" onclick="$('#grouper-workflow-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#grouper-workflow-more-options li').first().focus();return true;});">
                          ${textContainer.text['grouperWorkflowMoreActionsButton'] } <span class="caret"></span></button>

                      <ul class="dropdown-menu dropdown-menu-right" id="grouper-workflow-more-options">
                        
                         <li><a href="?operation=UiV2GrouperWorkflow.viewForms&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperWorkflow.viewForms&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                             >${textContainer.text['grouperWorkflowMoreActionsViewForms'] }</a></li>

                        <c:if test="${grouperRequestContainer.workflowContainer.canConfigureWorkflow}" >
                              
                          <li><a href="?operation=UiV2GrouperWorkflow.formAdd&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperWorkflow.formAdd&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['grouperWorkflowMoreActionsAddForm'] }</a></li>
                          
                        </c:if>

                      </ul>

                    </div>