<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                        <p>${textContainer.text['subjectSearchResultsDescription'] }</p>
                        <table class="table table-hover table-bordered table-striped table-condensed data-table">
                          <thead>
                            <tr>
                              <th class="sorted">${textContainer.text['subjectSearchResultsColumnHeaderStem']}</th>
                              <th>${textContainer.text['subjectSearchResultsColumnHeaderGroupName'] }</th>
                            </tr>
                          </thead>
                          <tbody>
                            <c:forEach items="${grouperRequestContainer.subjectContainer.guiGroupsAddMember}" 
                              var="guiGroup" >
                              <%-- <tr>
                                <td>Root : Applications : Directories</td>
                                <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Admins</a></td>
                              </tr> --%>
                              <tr>
                                <td>${guiGroup.pathColonSpaceSeparated}</td>
                                <td><a href="#" onclick="dijit.byId('groupAddMemberComboId').set('displayedValue', '${grouper:escapeJavascript(guiGroup.group.displayName)}'); dijit.byId('groupAddMemberComboId').set('value', '${guiGroup.group.id}'); return true;" data-dismiss="modal">${grouper:escapeHtml(guiGroup.group.displayExtension)}</a></td>
                              </tr>

                              </c:forEach>
                            </tbody>
                          </table>
                          
                          <div class="data-table-bottom clearfix">
                            <grouper:paging2 guiPaging="${grouperRequestContainer.subjectContainer.guiPagingSearchGroupResults}" 
                              formName="subjectSearchGroupPagingForm" ajaxFormIds="addGroupSearchFormId"
                              refreshOperation="../app/UiV2Subject.addGroupSearch?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" />
                          </div>
