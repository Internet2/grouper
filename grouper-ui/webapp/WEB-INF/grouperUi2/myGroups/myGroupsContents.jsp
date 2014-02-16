<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <table class="table table-hover table-bordered table-striped table-condensed data-table table-paths">
                  <thead>
                    <tr>
                      <th>${textContainer.text['myGroupsFolderHeader'] }</th>
                      <th>${textContainer.text['myGroupsGroupHeader'] }</th>
                    </tr>
                  </thead>
                  <tbody>
                    <%-- <tr>
                      <td>Root : Applications : Directories</td>
                      <td><i class="icon-group"></i>&nbsp; <a href="view-group.html">Admins</a></td>
                    </tr>  --%>
                    <c:forEach items="${grouperRequestContainer.myGroupsContainer.guiGroupsUserManages}" var="guiGroup">

                    <tr>
                      <td>${guiGroup.parentGuiStem.linkWithIcon}</td>
                      <td>${guiGroup.shortLinkWithIcon}</td>
                    </tr>

                    </c:forEach>
                  </tbody>
                </table>
                <div class="data-table-bottom gradient-background">
                  <grouper:paging2 guiPaging="${grouperRequestContainer.myGroupsContainer.myGroupsGuiPaging}" 
                    formName="myGroupsPagingForm" ajaxFormIds="myGroupsForm"
                    refreshOperation="../app/UiV2MyGroups.myGroupsSubmit" />
                </div>