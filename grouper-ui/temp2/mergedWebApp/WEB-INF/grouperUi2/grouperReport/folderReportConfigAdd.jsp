<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="../stem/stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
                  <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
                  </c:if>
                  <c:if test="${grouperRequestContainer.stemContainer.canReadPrivilegeInheritance}">
                    <%@ include file="../stem/stemMoreTab.jsp" %>
                  </c:if>
                </ul>
                 <div class="row-fluid">
							     <div class="lead span9">${textContainer.text['grouperReportStemAddConfigTitle'] }</div>
							     <div class="span3" id="grouperReportFolderMoreActionsButtonContentsDivId">
							       <%@ include file="grouperReportFolderMoreActionsButtonContents.jsp"%>
							     </div>
							   </div>
                
                <form class="form-inline form-small form-filter" id="addReportConfigFormId">
                  <input type="hidden" name="stemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
                  <table class="table table-condensed table-striped">
                    <tbody>
                      <c:set var="ObjectType" 
                          value="Folder" />
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
                          onclick="ajax('../app/UiV2GrouperReport.reportOnFolderAddEditSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'addReportConfigFormId'}); return false;">
                          &nbsp; <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2GrouperReport.viewReportConfigsOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
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