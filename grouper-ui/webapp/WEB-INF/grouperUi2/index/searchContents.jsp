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
                      <td><i class="fa fa-folder"></i> <a href="view-folder.html">Wiki</a>
                      </td>
                    </tr>
                    <tr>
                      <td>N/A</td>
                      <td><i class="fa fa-user"></i> <a href="view-person.html">Wiki Test Account</a>
                      </td>
                    </tr>
--%>
                    <c:forEach items="${grouperRequestContainer.indexContainer.searchGuiObjectsResults}" var="guiObjectBase">
                      <tr>
                        <td>
                          <c:choose>
                            <c:when test="${guiObjectBase.value == null}">
                              ${guiObjectBase.key.pathColonSpaceSeparated }
                            </c:when>
                            <c:otherwise>
                             ${guiObjectBase.value}
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td>${guiObjectBase.key.shortLinkWithIcon }</td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
                <div class="data-table-bottom gradient-background">
                  <grouper:paging2 guiPaging="${grouperRequestContainer.indexContainer.searchGuiPaging}" formName="searchPagingForm" ajaxFormIds="searchPageForm"
                    refreshOperation="../app/UiV2Main.searchFormSubmit" />
                </div>
<%-- ?searchFormQuery=${grouper:escapeUrl(grouperRequestContainer.indexContainer.searchQuery)} --%>
