<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <table class="table table-hover table-bordered table-striped table-condensed data-table table-paths">
                  <thead>
                    <tr>
                      <th>${textContainer.text['viewServiceParentStemHeader'] }</th>
                      <th>${textContainer.text['viewServiceStemNameHeader'] }</th>
                    </tr>
                  </thead>
                  <tbody>

                    <c:forEach items="${grouperRequestContainer.serviceContainer.guiStemsInService}" var="guiStem">

                      <%-- <tr>
                        <td>Root : Applications</td>
                        <td><i class="fa fa-folder"></i> <a href="#">Directories</a>
                        </td>
                      </tr> --%>

                      <tr>
                        <td>${guiStem.pathColonSpaceSeparated}</td>
                        <td>${guiStem.shortLinkWithIcon}</td>
                      </tr>

                    </c:forEach>
                  </tbody>
                </table>
                <div class="data-table-bottom gradient-background">
                  <grouper:paging2 guiPaging="${grouperRequestContainer.serviceContainer.guiPaging}" 
                    formName="viewServicePagingForm" ajaxFormIds="viewServiceForm"
                    refreshOperation="../app/UiV2Service.viewServiceSubmit" />
                </div>