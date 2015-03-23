<%-- ./webapp/WEB-INF/grouperUi2/stem/parentFolderSearchResults.jsp  --%>

<%@ include file="../assetsJsp/commonTaglib.jsp"%>


                    <%-- p>The table below lists folders where you are allowed to create new groups.</p>
                    <p>The table below lists folders where you are allowed to create new folders.</p --%>
                    <p>${grouperRequestContainer.stemContainer.stemSearchType.keyDescription}</p>
                    <table class="table table-hover table-bordered table-striped table-condensed data-table">
                      <thead>
                        <tr>
                          <th class="sorted">Folder Path</th>
                          <th>Folder Name</th>
                        </tr>
                      </thead>
                      <tbody>
                        <c:forEach items="${grouperRequestContainer.stemContainer.parentStemSearchResults}"
                          var="guiStem" >
                          <%-- tr>
                            <td>Root : Applications</td>
                            <td><i class="fa fa-folder"></i> <a href="#" data-dismiss="modal">Directories</a></td>
                          </tr --%>
                          <tr>
                            <td>${grouper:escapeHtml(guiStem.pathColonSpaceSeparated)}</td>
                            <td><i class="fa fa-folder"></i> <a href="#" onclick="dijit.byId('parentFolderComboId').set('displayedValue', '${grouper:escapeJavascript(guiStem.stem.displayName)}'); dijit.byId('parentFolderComboId').set('value', '${guiStem.stem.id}'); return true;" data-dismiss="modal">${grouper:escapeHtml(guiStem.stem.displayExtension) }</a></td>
                          </tr>
                        </c:forEach>
                      </tbody>
                    </table>
                    <%-- div class="data-table-bottom clearfix">
                      <div class="pull-right">Showing 1-10 of 25 &middot; <a href="#">First</a> | <a href="#">Prev</a> | <a href="#">Next</a> | <a href="#">Last</a></div>
                    </div --%>
                    <div class="data-table-bottom gradient-background">
                      <grouper:paging2 guiPaging="${grouperRequestContainer.stemContainer.parentStemGuiPaging}" formName="parentStemPagingForm" ajaxFormIds="stemSearchFormId"
                        refreshOperation="../app/UiV2Stem.${grouperRequestContainer.stemContainer.stemSearchType.operationMethod}?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
                    </div>

