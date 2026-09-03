<!-- ./webapp/WEB-INF/grouperUi2/index/indexColumnMenu.jsp -->

<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<%-- note, make sure ${col} will resolve to an int of 0, 1, 2 which is the column
     to put the content in --%>
                    <!-- start indexColumnMenu.jsp -->
                    <div class="pull-right">
                      <ul class="nav">
                        <li class="dropdown">
                        	<a data-toggle="dropdown" href="#" class="dropdown-toggle"
                        		aria-haspopup="true" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" aria-expanded="false" role="button" onclick="$('#widget-more-options${col}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#widget-more-options${col} li').first().focus();return true;});">
                        	<i class="fa fa-cog edit-widget dropdown"></i></a>
                          <ul class="dropdown-menu dropdown-menu-right" id="widget-more-options${col}">
                          	<!-- HJ : 20150319 -->
                            <li class="nav-header">${textContainer.text['indexSelectWidgetToDisplay']}</li>
                            <li class="divider"></li>
                            <!-- HJ : 20150319 -->
                            <li><button type="button" class="grouper-menuitem" onclick="ajax('UiV2Main.indexColMyFavorites?col=${col}'); return false;">${textContainer.text['myFavoritesBreadcrumb']}</button></li>
                            <!-- HJ : 20150319 -->
                            <li><button type="button" class="grouper-menuitem" onclick="ajax('UiV2Main.indexColGroupsImanage?col=${col}'); return false;">${textContainer.text['myGroupsTabMyGroups']}</button></li>
                            <!-- HJ : 20150319 -->
                            <li><button type="button" class="grouper-menuitem" onclick="ajax('UiV2Main.indexColMyServices?col=${col}'); return false;">${textContainer.text['myServicesBreadcrumb']}</button></li>
                            <!-- HJ : 20150319 -->
                            <li><button type="button" class="grouper-menuitem" onclick="ajax('UiV2Main.indexColStemsImanage?col=${col}'); return false;">${textContainer.text['myStemsBreadcrumb']}</button></li>
                            <!-- HJ : 20150319 -->
                            <li><button type="button" class="grouper-menuitem" onclick="ajax('UiV2Main.indexColMyMemberships?col=${col}'); return false;">${textContainer.text['myGroupsTabMyMemberships']}</button></li>
                            <!-- HJ : 20150319 -->
                            <li><button type="button" class="grouper-menuitem" onclick="ajax('UiV2Main.indexColRecentlyUsed?col=${col}'); return false;">${textContainer.text['indexRecentlyUsedRecentlyUsed']}</button></li>
                          </ul>
                        </li>
                      </ul>
                    </div>
                    <!-- end indexColumnMenu.jsp -->

