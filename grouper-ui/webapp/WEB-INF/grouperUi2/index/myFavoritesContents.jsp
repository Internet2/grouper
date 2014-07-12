<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <table class="table table-hover table-bordered table-striped table-condensed data-table table-paths">
                  <thead>
                    <tr>
                      <th>${textContainer.text['myFavoritesParentStemHeader'] }</th>
                      <th>${textContainer.text['myFavoritesStemHeader'] }</th>
                    </tr>
                  </thead>
                  <tbody>

                    <c:forEach items="${grouperRequestContainer.indexContainer.guiObjectFavorites}" var="guiObject">

                      <%-- <tr>
                        <td>Root : Applications</td>
                        <td><i class="fa fa-folder"></i> <a href="#">Directories</a>
                        </td>
                      </tr> --%>

                      <tr>
                        <td>${guiObject.pathColonSpaceSeparated}</td>
                        <td>${guiObject.shortLinkWithIcon}</td>
                      </tr>

                    </c:forEach>
                  </tbody>
                </table>
                <div class="data-table-bottom gradient-background">
                  <grouper:paging2 guiPaging="${grouperRequestContainer.indexContainer.myFavoritesGuiPaging}" 
                    formName="myFavoritesPagingForm" ajaxFormIds="myFavoritesForm"
                    refreshOperation="../app/UiV2Main.myFavoritesSubmit" />
                </div>