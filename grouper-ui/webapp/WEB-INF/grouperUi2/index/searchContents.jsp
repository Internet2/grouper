<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <table class="table table-hover table-bordered table-striped table-condensed data-table">
                  <thead>
                    <tr>
                      <th>${textContainer.text['searchFolderHeader'] }</th>
                      <th>${textContainer.text['searchNameHeader'] }</th>
                    </tr>
                  </thead>
                  <tbody>
<%--
                    <tr>
                      <td>Root : Applications</td>
                      <td><i class="icon-folder-close"></i> <a href="view-folder.html">Wiki</a>
                      </td>
                    </tr>
                    <tr>
                      <td>N/A</td>
                      <td><i class="icon-user"></i> <a href="view-person.html">Wiki Test Account</a>
                      </td>
                    </tr>
--%>
                    <c:forEach items="${grouperRequestContainer.indexContainer.searchGuiObjectsResults}" var="guiObjectBase">
                      <tr>
                        <td>${guiObjectBase.subject ? textContainer.text['searchResultsStemNotApplicable'] : guiObjectBase.pathColonSpaceSeparated }</td>
                        <td>${guiObjectBase.shortLinkWithIcon }</td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
                <grouper:paging2 guiPaging="${grouperRequestContainer.indexContainer.searchGuiPaging}" formName="searchPagingForm" ajaxFormIds="searchPageForm"
                  refreshOperation="../app/UiV2Main.searchFormSubmit" />
<%-- ?searchFormQuery=${grouper:escapeUrl(grouperRequestContainer.indexContainer.searchQuery)} --%>
