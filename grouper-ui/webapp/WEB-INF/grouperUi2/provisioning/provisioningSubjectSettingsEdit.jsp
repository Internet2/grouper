<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.stemContainer}" property="showAddMember" value="true" />
            
            <%@ include file="../subject/subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../subject/subjectTabs.jsp" %>
                
                <div class="row-fluid">
			      <div class="lead span9">${textContainer.text['provisioningSubjectProvisioningTitle'] }</div>
			      <div class="span3" id="grouperProvisioningSubjectMoreActionsButtonContentsDivId">
			        <%@ include file="provisioningSubjectMoreActionsButtonContents.jsp"%>
			      </div>
			    </div>
			    
			    <%-- <div class="row-fluid">
			      <div class="span9"> <p>${textContainer.text['provisioningGroupProvisioningDescription'] }</p></div>
			    </div> --%>

                <form class="form-inline form-small form-filter" id="editProvisioningFormId">
                  <input type="hidden" name="subjectId" value="${grouperRequestContainer.subjectContainer.guiSubject.subject.id}" />
                  <table class="table table-condensed table-striped">
                    <tbody>
                      <%@ include file="provisioningSubjectSettingsEditHelper.jsp" %>
                      <tr>
                        <td></td>
                        <td
                          style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                          <input type="submit" class="btn btn-primary"
                          aria-controls="objectTypeSubmitId" id="submitId"
                          value="${textContainer.text['provisioningEditButtonSave'] }"
                          onclick="ajax('../app/UiV2Provisioning.editProvisioningOnSubjectSave?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}', {formIds: 'editProvisioningFormId'}); return false;">
                          &nbsp; <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningOnSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}'); return false;"
                          >${textContainer.text['provisioningEditButtonCancel'] }</a>
                        </td>
                      </tr>

                    </tbody>
                  </table>
                  
                </form>
				
              </div>
            </div>
