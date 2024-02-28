<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.groupContainer}" property="showAddMember" value="false" />
            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                  </c:if>
                  <%@ include file="groupMoreTab.jsp" %>
                </ul>
               
                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['rulesFolderSettingsTitle'] }</div>
                  <div class="span3" id="grouperRulesFolderMoreActionsButtonContentsDivId">
                    <%@ include file="rulesMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                
                 <form class="form-inline form-small form-filter" id="addRuleConfigFormId">
                  <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                  <input type="hidden" name="ruleId" value="${grouperRequestContainer.rulesContainer.attributeAssignId}" />
                  <table class="table table-condensed table-striped">
                    <tbody>
                      <c:set var="ObjectType" 
                          value="Folder" />
                      <%@ include file="ruleAddHelper.jsp" %>
                      <tr>
                        <td>
                          <input type="hidden" name="mode" value="add">
                        </td>
                        <td
                          style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                          <input type="submit" class="btn btn-primary"
                          aria-controls="reportConfigSubmitId" id="submitId"
                          value="${textContainer.text['reportAddConfigButtonSave'] }"
                          onclick="ajax('../app/UiV2Group.addRuleOnGroupSubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'addRuleConfigFormId'}); return false;">
                          &nbsp; <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2Group.viewGroupRules&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                          >${textContainer.text['reportAddConfigButtonCancel'] }</a>
                        </td>
                      </tr>

                    </tbody>
                  </table>
                  
                </form>

              </div>
            </div>