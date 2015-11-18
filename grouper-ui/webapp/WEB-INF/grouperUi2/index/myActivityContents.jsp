<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <table class="table table-hover table-bordered table-striped table-condensed data-table table-paths">
                  <thead>
                    <tr>
                      <th>${textContainer.text['myActivityParentStemHeader'] }</th>
                      <th>${textContainer.text['myActivityStemHeader'] }</th>
                    </tr>
                  </thead>
                  <tbody>

                    <c:forEach items="${grouperRequestContainer.indexContainer.guiAuditEntries}" var="guiObject">

                      <tr>
                        <td>${guiObject.auditLine}</td>
                        <td>${guiObject.guiDate}</td>
                      </tr>

                    </c:forEach>
                  </tbody>
                </table>
                <div class="data-table-bottom gradient-background">
                  <grouper:paging2 guiPaging="${grouperRequestContainer.indexContainer.myActivityGuiPaging}" 
                    formName="myActivityPagingForm" ajaxFormIds="myActivityForm"
                    refreshOperation="../app/UiV2Main.myActivitySubmit" />
                </div>