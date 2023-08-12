<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%@ include file="../group/groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
                
                <c:if test="${fn:length(grouperRequestContainer.workflowContainer.errors) > 0}">
                  <div class="workflowConfigErrors alert alert-error">
                   <button type="button" class="close" data-dismiss="alert" aria-label="Close">x</button>
                   <c:forEach var="error" items="${grouperRequestContainer.workflowContainer.errors}">
                    <div>${error}</div>
                   </c:forEach>
                  </div>
                </c:if>
                
                <form class="form-inline form-small form-filter" id="groupJoinInitiateWorkflowId">
                  <div class="row-fluid">
                   
                   <div class="span12">                   
                    ${grouperRequestContainer.workflowContainer.htmlForm}
                   </div>
                   
                   <div class="span6">
                   
                     <input type="submit" class="btn btn-primary"
                          aria-controls="workflowConfigSubmitId" id="submitId"
                          value="${textContainer.text['workflowJoinGroupInitiateWorkflowButtonSubmit'] }"
                          onclick="ajax('../app/UiV2GrouperWorkflow.workflowInitiateSubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&workflowAttributeAssignId=${grouperRequestContainer.workflowContainer.guiGrouperWorkflowConfig.grouperWorkflowConfig.attributeAssignmentMarkerId}', {formIds: 'groupJoinInitiateWorkflowId'}); return false;">
                          &nbsp; <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2GrouperWorkflow.viewForms&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                          >${textContainer.text['workflowConfigButtonCancel'] }</a>
                   
                   </div>

                  </div>
                </form>
              </div>
            </div>
