<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                       <p>${textContainer.text['inviteExtneralSearchInstructions']}</p>
                        <table class="table table-hover table-bordered table-striped table-condensed data-table">
                          <thead>
                            <tr>
                              <th class="sorted">${textContainer.text['inviteExternalSearchResultsColumnHeaderStem']}</th>
                              <th>${textContainer.text['inviteExternalSearchResultsColumnHeaderGroupName'] }</th>
                            </tr>
                          </thead>
                          <tbody>
                            <c:forEach items="${grouperRequestContainer.inviteExternalContainer.guiGroupsSearch}" 
                              var="guiGroup" >
                              <%-- <tr>
                                <td>Root : Applications : Directories</td>
                                <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Admins</a></td>
                              </tr> --%>
                              <tr>
                                <td>${guiGroup.pathColonSpaceSeparated}</td>
                                <td><a href="#" onclick="dijit.byId('inviteAddGroupComboId').set('displayedValue', '${grouper:escapeJavascript(guiGroup.group.displayName)}'); dijit.byId('inviteAddGroupComboId').set('value', '${guiGroup.group.id}'); return true;" data-dismiss="modal">${grouper:escapeHtml(guiGroup.group.displayExtension)}</a></td>
                              </tr>

                              </c:forEach>
                            </tbody>
                          </table>
                          
                          <div class="data-table-bottom clearfix">
                            <grouper:paging2 guiPaging="${grouperRequestContainer.inviteExternalContainer.guiPaging}" 
                              formName="inviteSearchGroupPagingForm" ajaxFormIds="groupSearchFormId"
                              refreshOperation="../app/UiV2ExternalEntities.inviteSearchGroupFormSubmit" />
                          </div>
