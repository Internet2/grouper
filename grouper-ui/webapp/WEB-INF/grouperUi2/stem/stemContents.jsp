<%@ include file="../assetsJsp/commonTaglib.jsp"%>

               <table class="table table-hover table-bordered table-striped table-condensed data-table">
                  <thead>
                    <tr>
                      <th class="sorted">${textContainer.text['stemObjectName'] }</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:if test="${ ! grouperRequestContainer.stemContainer.guiStem.stem.rootStem}">
                      <tr>
                        <td><i class="fa fa-chevron-up"></i> <a href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.parentUuid}');">${textContainer.text['stemUpOneFolder'] }</a></td>
                      </tr>
                    </c:if>
                    <%--
                    <tr>
                      <td><i class="fa fa-folder"></i><a href="#"> Directories</a>
                      </td>
                    </tr>
                    --%>
                    <c:forEach items="${grouperRequestContainer.stemContainer.childGuiObjectsAbbreviated}" var="guiObjectBase">
                      <tr>
                        <td>${guiObjectBase.key.shortLinkWithIcon }</td>
                      </tr>
                    </c:forEach>
                    
                  </tbody>
                </table>
                <div class="data-table-bottom gradient-background">
                  <grouper:paging2 guiPaging="${grouperRequestContainer.stemContainer.guiPaging}" formName="stemPagingForm" ajaxFormIds="stemFilterFormId"
                    refreshOperation="../app/UiV2Stem.filter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
                </div>