<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <table class="table table-hover table-bordered table-striped table-condensed data-table table-paths">
                  <thead>
                    <tr>
                      <th>${textContainer.text['myStemsParentStemHeader'] }</th>
                      <th>${textContainer.text['myStemsStemHeader'] }</th>
                    </tr>
                  </thead>
                  <tbody>

                    <c:forEach items="${grouperRequestContainer.indexContainer.guiStemsUserManagesAbbreviated}" var="guiStem">

                      <%-- <tr>
                        <td>Root : Applications</td>
                        <td><i class="icon-folder-close"></i> <a href="#">Directories</a>
                        </td>
                      </tr> --%>

                      <tr>
                        <td>${guiStem.pathColonSpaceSeparated}</td>
                        <td>${guiStem.shortLinkWithIcon}</td>
                      </tr>

                    </c:forEach>
                  </tbody>
                </table>
                <grouper:paging2 guiPaging="${grouperRequestContainer.indexContainer.myStemsGuiPaging}" 
                  formName="myStemsPagingForm" ajaxFormIds="myStemsForm"
                  refreshOperation="../app/UiV2Main.myStemsSubmit" />
