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
                        <td><i class="icon-chevron-up"></i> <a href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.parentUuid}');">${textContainer.text['stemUpOneFolder'] }</a></td>
                      </tr>
                    </c:if>
                    <%--
                    <tr>
                      <td><i class="icon-folder-close"></i><a href="#"> Directories</a>
                      </td>
                    </tr>
                    --%>
                    <c:forEach items="${grouperRequestContainer.stemContainer.childGuiObjectsAbbreviated}" var="guiObjectBase">
                      <tr>
                        <td>${guiObjectBase.shortLinkWithIcon }</td>
                      </tr>
                    </c:forEach>
                    
                  </tbody>
                </table>
                <grouper:paging2 guiPaging="${grouperRequestContainer.stemContainer.guiPaging}" formName="stemPagingForm" ajaxFormIds="stemFilterFormId"
                  refreshOperation="../app/UiV2Stem.filter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&filterText=${grouper:escapeUrl(grouperRequestContainer.stemContainer.filterText)}" />
 