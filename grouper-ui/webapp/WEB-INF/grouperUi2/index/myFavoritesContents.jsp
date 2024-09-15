<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <table class="table table-hover table-bordered table-striped table-condensed data-table table-paths">
                  <thead>
                    <tr>
                      <th>${textContainer.text['myFavoritesParentStemHeader'] }</th>
                      <th>${textContainer.text['myFavoritesStemHeader'] }</th>
                    </tr>
                  </thead>
                  <tbody>
                  <c:forEach items="${grouperRequestContainer.indexContainer.guiObjectFavorites}" var="guiObjectBase">
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
                  <grouper:paging2 guiPaging="${grouperRequestContainer.indexContainer.myFavoritesGuiPaging}" 
                    formName="myFavoritesPagingForm" ajaxFormIds="myFavoritesForm"
                    refreshOperation="../app/UiV2Main.myFavoritesSubmit" />
                </div>