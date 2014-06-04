<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <table class="table table-hover table-bordered table-striped table-condensed data-table table-paths">
                  <thead>
                    <tr>
                      <th>${textContainer.text['myServicesParentStemHeader'] }</th>
                      <th>${textContainer.text['myServicesStemHeader'] }</th>
                    </tr>
                  </thead>
                  <tbody>

                    <c:forEach items="${grouperRequestContainer.indexContainer.guiAttributeDefNamesMyServices}" var="guiServiceAttributeDefName">

                      <%-- <tr>
                        <td>Root : Applications</td>
                        <td><i class="fa fa-folder"></i> <a href="#">Directories</a>
                        </td>
                      </tr> --%>

                      <tr>
                        <td>${guiServiceAttributeDefName.pathColonSpaceSeparated}</td>
                        <td>${guiServiceAttributeDefName.shortLinkWithIcon}</td>
                      </tr>

                    </c:forEach>
                  </tbody>
                </table>
                <div class="data-table-bottom gradient-background">
                  <grouper:paging2 guiPaging="${grouperRequestContainer.indexContainer.myServicesGuiPaging}" 
                    formName="myServicesPagingForm" ajaxFormIds="myServicesForm"
                    refreshOperation="../app/UiV2Main.myServicesSubmit" />
                </div>