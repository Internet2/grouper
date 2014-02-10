<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                          <table class="table table-hover table-bordered table-striped table-condensed">
                            <thead>
                              <tr>
                                <th class="sorted">${textContainer.text['groupSearchResultsColumnName'] }</th>
                              </tr>
                            </thead>
                            <tbody>

                              <c:forEach items="${grouperRequestContainer.groupContainer.guiSubjectsAddMember}" 
                                var="guiSubject" >

                                <tr>
                                 <td><a href="#" onclick="dijit.byId('groupAddMemberComboId').set('displayedValue', '${grouper:escapeJavascript(guiSubject.screenLabelShort2)}'); dijit.byId('groupAddMemberComboId').set('value', '${guiSubject.subject.sourceId}||${guiSubject.subject.id}'); return true;" data-dismiss="modal">${guiSubject.screenLabelLongWithIcon }</a></td>
                                </tr>

                              </c:forEach>
<%--                               <tr>
                                <td><a href="#" data-dismiss="modal"><i class="icon-user"></i> Smith, Jane</a></td>
                              </tr>
                              --%>
                            </tbody>
                          </table>
