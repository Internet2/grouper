<!-- ./webapp/WEB-INF/grouperUi2/index/indexColumnMenu.jsp -->

<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<%-- note, make sure ${col} will resolve to an int of 0, 1, 2 which is the column
     to put the content in --%>
                    <!-- start indexColumnMenu.jsp -->
                    <div class="pull-right">
                      <ul class="nav">
                        <li class="dropdown"><a data-toggle="dropdown" href="#" class="dropdown-toggle"><i class="fa fa-cog edit-widget dropdown"></i></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                          	<!-- HJ : 20150319 -->
                            <li class="nav-header">${textContainer.text['indexSelectWidgetToDisplay']}</li>
                            <li class="divider"></li>
                            <!-- HJ : 20150319 -->
                            <li><a href="#" onclick="ajax('UiV2Main.indexColMyFavorites?col=${col}'); return false;">${textContainer.text['myFavoritesBreadcrumb']}</a></li>
                            <!-- HJ : 20150319 -->
                            <li><a href="#" onclick="ajax('UiV2Main.indexColGroupsImanage?col=${col}'); return false;">${textContainer.text['myGroupsTabMyGroups']}</a></li>
                            <!-- HJ : 20150319 
                            <li><a href="#" onclick="ajax('UiV2Main.indexColMyServices?col=${col}'); return false;">${textContainer.text['myServicesBreadcrumb']}</a></li>
                            -->
                            <!-- HJ : 20150319 -->
                            <li><a href="#" onclick="ajax('UiV2Main.indexColStemsImanage?col=${col}'); return false;">${textContainer.text['myStemsBreadcrumb']}</a></li>
                            <!-- HJ : 20150319 -->
                            <li><a href="#" onclick="ajax('UiV2Main.indexColMyMemberships?col=${col}'); return false;">${textContainer.text['myGroupsTabMyMemberships']}</a></li>
                            <!-- HJ : 20150319 -->
                            <li><a href="#" onclick="ajax('UiV2Main.indexColRecentlyUsed?col=${col}'); return false;">${textContainer.text['indexRecentlyUsedRecentlyUsed']}</a></li>
                          </ul>
                        </li>
                      </ul>
                    </div>
                    <!-- end indexColumnMenu.jsp -->

