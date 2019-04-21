<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />
            
            <%@ include file="../group/groupHeader.jsp" %>
            
            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
                    
                <div class="tab-interface">
                  <ul class="nav nav-tabs">
                    <li><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['groupMembersTab'] }</a></li>
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
                
                <c:choose>
                  <c:when test="${fn:length(grouperRequestContainer.grouperReportContainer.reportConfigBeans) > 0}">
                  
                    <form class="form-inline form-small form-filter" id="editReportConfigFormId">
		                  <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
		                  <table class="table table-condensed table-striped">
		                    <tbody>
		                      <c:set var="ObjectType"
		                          value="Group" />
		                      <%@ include file="reportConfigObjectEditHelper.jsp" %>
		                      <tr>
		                        <td>
		                          <input type="hidden" name="mode" value="edit">
		                        </td>
		                        <td
		                          style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
		                          <input type="submit" class="btn btn-primary"
		                          aria-controls="reportConfigSubmitId" id="submitId"
		                          value="${textContainer.text['reportAddConfigButtonSave'] }"
		                          onclick="ajax('../app/UiV2GrouperReport.reportOnGroupAddEditSubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'editReportConfigFormId'}); return false;">
		                          &nbsp; <a class="btn btn-cancel" role="button"
		                          onclick="return guiV2link('operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
		                          >${textContainer.text['reportAddConfigButtonCancel'] }</a>
		                        </td>
		                      </tr>
		
		                    </tbody>
		                  </table>
		                  
		                </form>
                  
                  </c:when>
                  <c:otherwise>
                  <div class="row-fluid">
					          <div class="span9"> <p><b>${textContainer.text['grouperReportNoEntitiesFound'] }</b></p></div>
					        </div>
                  </c:otherwise>
                </c:choose>
                
              </div>
            </div>
            <c:if test="${grouperRequestContainer.indexContainer.menuRefreshOnView}">
              <script>dojoInitMenu(${grouperRequestContainer.indexContainer.menuRefreshOnView});</script>
            </c:if>