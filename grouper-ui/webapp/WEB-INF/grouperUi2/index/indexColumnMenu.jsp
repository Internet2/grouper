<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<%-- note, make sure ${col} will resolve to an int of 0, 1, 2 which is the column
     to put the content in --%>
                    <!-- start indexColumnMenu.jsp -->
                    <div class="pull-right">
                      <ul class="nav">
                        <li class="dropdown"><a data-toggle="dropdown" href="#" class="dropdown-toggle"><i class="fa fa-cog edit-widget dropdown"></i></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li class="nav-header">Select a widget to display</li>
                            <li class="divider"></li>
                            <li><a href="#" onclick="ajax('UiV2Main.indexColMyFavorites?col=${col}'); return false;">My favorites</a></li>
                            <li><a href="#" onclick="ajax('UiV2Main.indexColGroupsImanage?col=${col}'); return false;">Groups I manage</a></li>
                            <li><a href="#" onclick="ajax('UiV2Main.indexColMyServices?col=${col}'); return false;">My services</a></li>
                            <li><a href="#" onclick="ajax('UiV2Main.indexColStemsImanage?col=${col}'); return false;">My folders</a></li>
                            <li><a href="#" onclick="ajax('UiV2Main.indexColMyMemberships?col=${col}'); return false;">My memberships</a></li>
                            <li><a href="#" onclick="ajax('UiV2Main.indexColRecentlyUsed?col=${col}'); return false;">Recently used</a></li>
                          </ul>
                        </li>
                      </ul>
                    </div>
                    <!-- end indexColumnMenu.jsp -->
                    