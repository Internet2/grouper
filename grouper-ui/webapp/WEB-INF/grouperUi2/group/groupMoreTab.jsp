<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                  <li class="dropdown"><a data-toggle="dropdown" href="#" class="dropdown-toggle">More <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                      <li><a href="#" onclick="return guiV2link('operation=UiV2Group.thisGroupsMemberships&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});">${textContainer.text['thisGroupsMembershipsTab'] }</a></li>
                      <li><a href="view-group-group-privileges.html">This Group's Privileges</a></li>
                    </ul>
                  </li>
