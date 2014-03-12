<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                        <p>${textContainer.text['groupImportResultsDescription'] }</p>
                        <table class="table table-hover table-bordered table-striped table-condensed data-table">
                          <thead>
                            <tr>
                              <th class="sorted">${textContainer.text['groupImportResultsColumnHeaderStem']}</th>
                              <th>${textContainer.text['groupImportResultsColumnHeaderGroupName'] }</th>
                            </tr>
                          </thead>
                          <tbody>
                            <c:forEach items="${grouperRequestContainer.groupContainer.guiGroups}" 
                              var="guiGroup" >
                              <%-- <tr>
                                <td>Root : Applications : Directories</td>
                                <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Admins</a></td>
                              </tr> --%>
                              <tr>
                                <td>${guiGroup.pathColonSpaceSeparated}</td>
                                <td><a href="#" onclick="dijit.byId('groupImportGroupComboId').set('displayedValue', '${grouper:escapeJavascript(guiGroup.group.displayName)}'); dijit.byId('groupImportGroupComboId').set('value', '${guiGroup.group.id}'); return true;" data-dismiss="modal">${grouper:escapeHtml(guiGroup.group.displayExtension)}</a></td>
                              </tr>

                              </c:forEach>
                            </tbody>
                          </table>
                          
                          <div class="data-table-bottom clearfix">
                            <grouper:paging2 guiPaging="${grouperRequestContainer.groupContainer.guiPaging}" 
                              formName="groupPagingForm" ajaxFormIds="addGroupSearchFormId"
                              refreshOperation="../app/UiV2Group.groupImportGroupSearch" />
                          </div>
