<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                        <p>${textContainer.text['subjectSearchAttributeDefResultsDescription'] }</p>
                        <table class="table table-hover table-bordered table-striped table-condensed data-table">
                          <thead>
                            <tr>
                              <th class="sorted">${textContainer.text['subjectSearchResultsColumnHeaderStem']}</th>
                              <th>${textContainer.text['subjectSearchResultsColumnHeaderAttributeDefName'] }</th>
                            </tr>
                          </thead>
                          <tbody>
                            <c:forEach items="${grouperRequestContainer.attributeDefContainer.guiAttributeDefSearchResults}" 
                              var="guiAttributeDef" >
                              <%-- <tr>
                                <td>Root : Applications : Directories</td>
                                <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Admins</a></td>
                              </tr> --%>
                              <tr>
                                <td>${guiAttributeDef.pathColonSpaceSeparated}</td>
                                <td><a href="#" onclick="dijit.byId('attributeDefAddMemberComboId').set('displayedValue', '${grouper:escapeJavascript(guiAttributeDef.attributeDef.name)}'); dijit.byId('attributeDefAddMemberComboId').set('value', '${guiAttributeDef.attributeDef.id}'); return true;" data-dismiss="modal">${grouper:escapeHtml(guiAttributeDef.attributeDef.extension)}</a></td>
                              </tr>

                              </c:forEach>
                            </tbody>
                          </table>
                          
                          <div class="data-table-bottom clearfix">
                            <grouper:paging2 guiPaging="${grouperRequestContainer.attributeDefContainer.guiPaging}" 
                              formName="subjectSearchAttributeDefPagingForm" ajaxFormIds="addAttributeDefSearchFormId"
                              refreshOperation="../app/UiV2Subject.addAttributeDefSearch?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" />
                          </div>
