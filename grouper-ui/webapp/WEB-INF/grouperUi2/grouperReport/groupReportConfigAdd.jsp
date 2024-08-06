<%@ include file="../assetsJsp/commonTaglib.jsp"%>

						<%-- for the new group or new stem button --%>
						<input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />
						
						<%@ include file="../group/groupHeader.jsp" %>
						
						<div class="row-fluid">
						  <div class="span12">
						    <div id="messages"></div>
						        
						    <div class="tab-interface">
						      <ul class="nav nav-tabs">
						        <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
						        <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
						          <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
						        </c:if>
						        <%@ include file="../group/groupMoreTab.jsp" %>
						      </ul>
						    </div>
						    <div class="row-fluid">
						      <div class="lead span9">${textContainer.text['groupReportOnGroupDescription'] }</div>
						      <div class="span3" id="grouperReportGroupMoreActionsButtonContentsDivId">
						        <%@ include file="grouperReportGroupMoreActionsButtonContents.jsp"%>
						      </div>
						    </div>
                
                <form class="form-inline form-small form-filter" id="addReportConfigFormId">
                  <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                  <table class="table table-condensed table-striped">
                    <tbody>
                      <c:set var="ObjectType" 
                          value="Group" />
                      <%@ include file="reportConfigObjectAddHelper.jsp" %>
                      <tr>
                        <td>
                          <input type="hidden" name="mode" value="add">
                        </td>
                        <td
                          style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                          <input type="submit" class="btn btn-primary"
                          aria-controls="reportConfigSubmitId" id="submitId"
                          value="${textContainer.text['reportAddConfigButtonSave'] }"
                          onclick="ajax('../app/UiV2GrouperReport.reportOnGroupAddEditSubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'addReportConfigFormId'}); return false;">
                          &nbsp; <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                          >${textContainer.text['reportAddConfigButtonCancel'] }</a>
                        </td>
                      </tr>

                    </tbody>
                  </table>
                  
                </form>
              </div>
            </div>
            <c:if test="${grouperRequestContainer.indexContainer.menuRefreshOnView}">
              <script>dojoInitMenu(${grouperRequestContainer.indexContainer.menuRefreshOnView});</script>
            </c:if>